package controller;

import database.BookDAO;
import database.SQLiteBookDAO;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import model.Book;

public class LenderDashboardController extends BaseDashboardController {

    @FXML private TextField isbnField;
    @FXML private TextField titleField;
    @FXML private TextField authorField;
    @FXML private TextField genreField;
    @FXML private Label statusLabel;

    @FXML private Button btnAddBook;
    @FXML private Button btnInventory;

    @FXML private VBox addBookPanel;
    @FXML private VBox inventoryPanel;

    @FXML private TableView<Book> bookTable;
    @FXML private TableColumn<Book, String> isbnColumn;
    @FXML private TableColumn<Book, String> titleColumn;
    @FXML private TableColumn<Book, String> authorColumn;
    @FXML private TableColumn<Book, String> genreColumn;
    @FXML private TableColumn<Book, String> statusColumn;
    @FXML private Button editBookBtn;
    @FXML private Button toggleAvailBtn;
    @FXML private Button deleteBookBtn;
    @FXML private Label inventoryStatusLabel;

    private final BookDAO bookDAO = new SQLiteBookDAO();
    private final ObservableList<Book> bookList = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        isbnColumn.setCellValueFactory(data ->
                new javafx.beans.property.SimpleStringProperty(data.getValue().getIsbn()));
        titleColumn.setCellValueFactory(data ->
                new javafx.beans.property.SimpleStringProperty(data.getValue().getTitle()));
        authorColumn.setCellValueFactory(data ->
                new javafx.beans.property.SimpleStringProperty(data.getValue().getAuthor()));
        genreColumn.setCellValueFactory(data ->
                new javafx.beans.property.SimpleStringProperty(data.getValue().getGenre()));
        statusColumn.setCellValueFactory(data ->
                new javafx.beans.property.SimpleStringProperty(data.getValue().isAvailable() ? "Available" : "Unavailable"));

        // Setup button actions
        editBookBtn.setOnAction(e -> {
            Book selected = bookTable.getSelectionModel().getSelectedItem();
            if (selected != null) handleEditBook(selected);
        });
        toggleAvailBtn.setOnAction(e -> {
            Book selected = bookTable.getSelectionModel().getSelectedItem();
            if (selected != null) handleToggleAvailability(selected);
        });
        deleteBookBtn.setOnAction(e -> {
            Book selected = bookTable.getSelectionModel().getSelectedItem();
            if (selected != null) handleDeleteBook(selected);
        });

        // Enable/disable buttons based on table selection
        bookTable.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            boolean hasSelection = newVal != null;
            editBookBtn.setDisable(!hasSelection);
            toggleAvailBtn.setDisable(!hasSelection);
            deleteBookBtn.setDisable(!hasSelection);

            // Update toggle button text based on current status
            if (hasSelection) {
                toggleAvailBtn.setText(newVal.isAvailable() ? "⚠️ Mark Unavailable" : "✓ Mark Available");
            }
        });

        bookTable.setItems(bookList);
        loadBooks();
        showAddBookPanel();
    }

    // ── Member-only: switch to Borrower Dashboard ──

    // ── Member-only: switch to Borrower Dashboard ──

    @FXML
    private void switchToBorrower() {
        // Uses the shared navigateTo method from BaseDashboardController
        System.out.println("Button Clicked!");
        navigateTo("/view/BorrowerDashboard.fxml", "Library Nexus - Member Portal", (Object controller) -> {
            // Passes the current username to the new dashboard
            ((BaseDashboardController) controller).setUsername(currentUsername);
        });
    }

    // ── Sidebar panel switching ──

    @FXML
    public void showAddBookPanel() {
        addBookPanel.setVisible(true);
        addBookPanel.setManaged(true);
        inventoryPanel.setVisible(false);
        inventoryPanel.setManaged(false);
        setActiveButton(btnAddBook);
        setInactiveButton(btnInventory);
    }

    @FXML
    public void showInventoryPanel() {
        inventoryPanel.setVisible(true);
        inventoryPanel.setManaged(true);
        addBookPanel.setVisible(false);
        addBookPanel.setManaged(false);
        setActiveButton(btnInventory);
        setInactiveButton(btnAddBook);
        loadBooks();
    }

    // ── Book actions ──

    @FXML
    private void handleAddBook() {
        String isbn   = isbnField.getText().trim();
        String title  = titleField.getText().trim();
        String author = authorField.getText().trim();
        String genre  = genreField.getText().trim();

        if (isbn.isEmpty() || title.isEmpty() || author.isEmpty() || genre.isEmpty()) {
            statusLabel.setTextFill(Color.RED);
            statusLabel.setText("Please fill in all fields.");
            return;
        }

        Book book = new Book(isbn, title, author, genre, currentUserId);
        bookDAO.insert(book);
        bookList.add(book);

        isbnField.clear();
        titleField.clear();
        authorField.clear();
        genreField.clear();

        statusLabel.setTextFill(Color.GREEN);
        statusLabel.setText("Book \"" + title + "\" added successfully!");
    }

    private void loadBooks() {
        bookList.clear();
        bookList.addAll(bookDAO.getByOwnerId(currentUserId));
    }

    private void handleEditBook(Book book) {
        // Show edit dialog
        Dialog<Object> dialog = new Dialog<>();
        dialog.setTitle("Edit Book");
        dialog.setHeaderText("Edit book details for: " + book.getTitle());

        TextField editIsbnField = new TextField(book.getIsbn());
        TextField editTitleField = new TextField(book.getTitle());
        TextField editAuthorField = new TextField(book.getAuthor());
        TextField editGenreField = new TextField(book.getGenre());

        editIsbnField.setDisable(true); // ISBN shouldn't be changed
        editIsbnField.setStyle("-fx-opacity: 0.6;");

        javafx.scene.layout.GridPane grid = new javafx.scene.layout.GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new javafx.geometry.Insets(10));
        grid.add(new Label("ISBN:"), 0, 0);
        grid.add(editIsbnField, 1, 0);
        grid.add(new Label("Title:"), 0, 1);
        grid.add(editTitleField, 1, 1);
        grid.add(new Label("Author:"), 0, 2);
        grid.add(editAuthorField, 1, 2);
        grid.add(new Label("Genre:"), 0, 3);
        grid.add(editGenreField, 1, 3);

        dialog.getDialogPane().setContent(grid);
        dialog.getDialogPane().getButtonTypes().addAll(javafx.scene.control.ButtonType.OK, javafx.scene.control.ButtonType.CANCEL);

        if (dialog.showAndWait().orElse(javafx.scene.control.ButtonType.CANCEL) == javafx.scene.control.ButtonType.OK) {
            Book updatedBook = new Book(
                book.getIsbn(),
                editTitleField.getText().trim(),
                editAuthorField.getText().trim(),
                editGenreField.getText().trim(),
                book.getOwnerId(),
                book.isAvailable(),
                book.getBorrowedBy()
            );

            if (bookDAO.updateBook(updatedBook)) {
                inventoryStatusLabel.setTextFill(Color.GREEN);
                inventoryStatusLabel.setText("Book updated successfully!");
                loadBooks();
            } else {
                inventoryStatusLabel.setTextFill(Color.RED);
                inventoryStatusLabel.setText("Failed to update book.");
            }
        }
    }

    private void handleToggleAvailability(Book book) {
        boolean newAvailability = !book.isAvailable();
        Book updatedBook = new Book(
            book.getIsbn(),
            book.getTitle(),
            book.getAuthor(),
            book.getGenre(),
            book.getOwnerId(),
            newAvailability,
            newAvailability ? null : book.getBorrowedBy()
        );

        if (bookDAO.updateBook(updatedBook)) {
            inventoryStatusLabel.setTextFill(Color.GREEN);
            inventoryStatusLabel.setText("Book marked as " + (newAvailability ? "Available" : "Unavailable"));
            loadBooks();
        } else {
            inventoryStatusLabel.setTextFill(Color.RED);
            inventoryStatusLabel.setText("Failed to update availability.");
        }
    }

    private void handleDeleteBook(Book book) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Delete Book");
        alert.setHeaderText("Are you sure?");
        alert.setContentText("Delete \"" + book.getTitle() + "\" from inventory?");

        if (alert.showAndWait().isPresent() && alert.getResult() == javafx.scene.control.ButtonType.OK) {
            if (bookDAO.deleteBook(book.getIsbn())) {
                inventoryStatusLabel.setTextFill(Color.GREEN);
                inventoryStatusLabel.setText("Book deleted successfully!");
                loadBooks();
            } else {
                inventoryStatusLabel.setTextFill(Color.RED);
                inventoryStatusLabel.setText("Failed to delete book.");
            }
        }
    }

    // ── Sidebar button styles ──

    private void setActiveButton(Button btn) {
        btn.setStyle("-fx-background-color: rgba(0,0,0,0.2); " +
                "-fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 13px; " +
                "-fx-padding: 14 20; -fx-cursor: hand; -fx-background-radius: 0; " +
                "-fx-border-color: transparent transparent transparent white; " +
                "-fx-border-width: 0 0 0 3; -fx-alignment: CENTER_LEFT;");
    }

    private void setInactiveButton(Button btn) {
        btn.setStyle("-fx-background-color: transparent; -fx-text-fill: rgba(255,255,255,0.7); " +
                "-fx-font-size: 13px; -fx-padding: 14 20; -fx-cursor: hand; " +
                "-fx-background-radius: 0; -fx-alignment: CENTER_LEFT;");
    }
}
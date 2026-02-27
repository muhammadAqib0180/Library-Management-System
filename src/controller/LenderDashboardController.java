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
    @FXML private Label statusLabel;

    @FXML private Button btnAddBook;
    @FXML private Button btnInventory;

    @FXML private VBox addBookPanel;
    @FXML private VBox inventoryPanel;

    @FXML private TableView<Book> bookTable;
    @FXML private TableColumn<Book, String> isbnColumn;
    @FXML private TableColumn<Book, String> titleColumn;
    @FXML private TableColumn<Book, String> authorColumn;
    @FXML private TableColumn<Book, String> statusColumn;

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
        statusColumn.setCellValueFactory(data ->
                new javafx.beans.property.SimpleStringProperty("Available"));

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

        if (isbn.isEmpty() || title.isEmpty() || author.isEmpty()) {
            statusLabel.setTextFill(Color.RED);
            statusLabel.setText("Please fill in all fields.");
            return;
        }

        Book book = new Book(isbn, title, author);
        bookDAO.insert(book);
        bookList.add(book);

        isbnField.clear();
        titleField.clear();
        authorField.clear();

        statusLabel.setTextFill(Color.GREEN);
        statusLabel.setText("Book \"" + title + "\" added successfully!");
    }

    private void loadBooks() {
        bookList.clear();
        bookList.addAll(bookDAO.getAll());
    }

    // ── Sidebar button styles ──

    private void setActiveButton(Button btn) {
        btn.setStyle("-fx-background-color: linear-gradient(to right, #0f3460, #1a1a2e); " +
                "-fx-text-fill: #4fc3f7; -fx-font-weight: bold; -fx-font-size: 13px; " +
                "-fx-padding: 14 20; -fx-cursor: hand; -fx-background-radius: 0; " +
                "-fx-border-color: transparent transparent transparent #4fc3f7; " +
                "-fx-border-width: 0 0 0 3; -fx-alignment: CENTER_LEFT;");
    }

    private void setInactiveButton(Button btn) {
        btn.setStyle("-fx-background-color: transparent; -fx-text-fill: rgba(255,255,255,0.6); " +
                "-fx-font-size: 13px; -fx-padding: 14 20; -fx-cursor: hand; " +
                "-fx-background-radius: 0; -fx-alignment: CENTER_LEFT;");
    }
}
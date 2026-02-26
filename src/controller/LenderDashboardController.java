package controller;

import database.BookDAO;
import database.SQLiteBookDAO;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import java.io.IOException;
import model.Book;

public class LenderDashboardController {

    // Add Book panel fields
    @FXML private TextField isbnField;
    @FXML private TextField titleField;
    @FXML private TextField authorField;
    @FXML private Label statusLabel;

    // Top bar
    @FXML private Label welcomeLabel;

    // Sidebar buttons
    @FXML private Button btnAddBook;
    @FXML private Button btnInventory;

    // Panels
    @FXML private VBox addBookPanel;
    @FXML private VBox inventoryPanel;

    // Table
    @FXML private TableView<Book> bookTable;
    @FXML private TableColumn<Book, String> isbnColumn;
    @FXML private TableColumn<Book, String> titleColumn;
    @FXML private TableColumn<Book, String> authorColumn;
    @FXML private TableColumn<Book, String> statusColumn;

    private final BookDAO bookDAO = new SQLiteBookDAO();
    private final ObservableList<Book> bookList = FXCollections.observableArrayList();
    private String currentUsername;

    @FXML
    public void initialize() {
        // Bind table columns to Book properties
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

        // Start on the Add Book panel
        showAddBookPanel();
    }

    /** Called from outside to pass the logged-in username */
    public void setUsername(String username) {
        this.currentUsername = username;
        welcomeLabel.setText("Welcome, " + username);
    }

    @FXML
    private void switchToBorrower() {
        // Borrower dashboard coming soon — no action for now
    }

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

    private void setActiveButton(Button btn) {
        btn.setStyle("-fx-background-color: #2ecc71; -fx-text-fill: white; " +
                "-fx-font-weight: bold; -fx-font-size: 13; " +
                "-fx-padding: 12 20; -fx-cursor: hand; -fx-background-radius: 0;");
    }

    private void setInactiveButton(Button btn) {
        btn.setStyle("-fx-background-color: transparent; -fx-text-fill: #ecf0f1; " +
                "-fx-font-size: 13; -fx-padding: 12 20; -fx-cursor: hand; " +
                "-fx-background-radius: 0; -fx-alignment: CENTER_LEFT;");
    }
}
package controller;

import database.BookDAO;
import database.SQLiteBookDAO;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import model.Book;
import model.BorrowedBook;

import java.util.List;

public class BorrowerDashboardController extends BaseDashboardController {

    @FXML private TextField searchBar;

    // ✅ FIX 1: These now match the fx:id values added to the FXML
    @FXML private Button btnMyBooks;
    @FXML private Button btnBrowse;

    // Table View
    @FXML private TableView<BorrowedBook> borrowedTableView;
    @FXML private TableColumn<BorrowedBook, String> titleColumn;
    @FXML private TableColumn<BorrowedBook, String> authorColumn;
    @FXML private TableColumn<BorrowedBook, String> borrowDateColumn;
    @FXML private TableColumn<BorrowedBook, String> dueDateColumn;
    @FXML private TableColumn<BorrowedBook, String> statusColumn;

    // ✅ FIX 2: Kept fx:id but also declared here so renew/return can reference selected row
    @FXML private Button renewButton;
    @FXML private Button returnButton;

    private final BookDAO bookDAO = new SQLiteBookDAO();
    private final ObservableList<BorrowedBook> masterData = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        // 1. Setup Columns
        titleColumn.setCellValueFactory(data -> data.getValue().titleProperty());
        authorColumn.setCellValueFactory(data -> data.getValue().authorProperty());
        borrowDateColumn.setCellValueFactory(data -> data.getValue().borrowDateProperty());
        dueDateColumn.setCellValueFactory(data -> data.getValue().dueDateProperty());
        statusColumn.setCellValueFactory(data -> data.getValue().statusProperty());

        // 2. Initial Data Load
        loadBooks();

        // 3. Search Bar filtering
        FilteredList<BorrowedBook> filteredData = new FilteredList<>(masterData, p -> true);
        searchBar.textProperty().addListener((observable, oldValue, newValue) -> {
            filteredData.setPredicate(book -> {
                if (newValue == null || newValue.isEmpty()) return true;
                String lowerCaseFilter = newValue.toLowerCase();
                return book.titleProperty().get().toLowerCase().contains(lowerCaseFilter)
                        || book.authorProperty().get().toLowerCase().contains(lowerCaseFilter);
            });
        });

        borrowedTableView.setItems(filteredData);

        // ✅ FIX 3: btnMyBooks and btnBrowse are now injected from FXML — no more NullPointerException
        setActiveButton(btnMyBooks);
        setInactiveButton(btnBrowse);
    }

    /**
     * Loads borrowed book data into the master list.
     * Uses a placeholder loan date until a LoanDAO is implemented.
     */
    private void loadBooks() {
        masterData.clear();
        List<Book> books = bookDAO.getAll();
        for (Book b : books) {
            masterData.add(new BorrowedBook(
                    b.getIsbn(),
                    b.getTitle(),
                    b.getAuthor(),
                    "2026-02-27",   // Placeholder — replace with LoanDAO value
                    "2026-03-27",
                    "Active"
            ));
        }
    }

    // ── Sidebar switching ──

    @FXML
    public void showMyBooks() {
        setActiveButton(btnMyBooks);
        setInactiveButton(btnBrowse);
    }

    @FXML
    public void showBrowseCatalog() {
        setActiveButton(btnBrowse);
        setInactiveButton(btnMyBooks);
    }

    // ── Table action handlers ──

    // ✅ FIX 4: Stub handlers so FXML onAction references don't throw on click
    @FXML
    private void handleRenew() {
        BorrowedBook selected = borrowedTableView.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("No Selection", "Please select a book to renew.");
            return;
        }
        // TODO: call LoanDAO.renewLoan(selected.getIsbn(), currentUsername)
        showAlert("Renewed", "\"" + selected.titleProperty().get() + "\" has been renewed.");
    }

    @FXML
    private void handleReturn() {
        BorrowedBook selected = borrowedTableView.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("No Selection", "Please select a book to return.");
            return;
        }
        // TODO: call LoanDAO.returnLoan(selected.getIsbn(), currentUsername)
        masterData.remove(selected);
        showAlert("Returned", "\"" + selected.titleProperty().get() + "\" has been returned.");
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    // ── Sidebar button styles ──

    private void setActiveButton(Button btn) {
        btn.setStyle(
                "-fx-background-color: linear-gradient(to right, #0f3460, #1a1a2e); " +
                        "-fx-text-fill: #4fc3f7; -fx-font-weight: bold; -fx-font-size: 13px; " +
                        "-fx-padding: 14 20; -fx-cursor: hand; -fx-background-radius: 0; " +
                        "-fx-border-color: transparent transparent transparent #4fc3f7; " +
                        "-fx-border-width: 0 0 0 3; -fx-alignment: CENTER_LEFT;");
    }

    private void setInactiveButton(Button btn) {
        btn.setStyle(
                "-fx-background-color: transparent; -fx-text-fill: rgba(255,255,255,0.6); " +
                        "-fx-font-size: 13px; -fx-padding: 14 20; -fx-cursor: hand; " +
                        "-fx-background-radius: 0; -fx-alignment: CENTER_LEFT;");
    }
}
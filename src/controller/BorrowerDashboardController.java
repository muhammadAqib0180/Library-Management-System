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

    // Sidebar Buttons
    @FXML private Button btnMyBooks;
    @FXML private Button btnBrowse;

    // Table View
    @FXML private TableView<BorrowedBook> borrowedTableView;
    @FXML private TableColumn<BorrowedBook, String> titleColumn;
    @FXML private TableColumn<BorrowedBook, String> authorColumn;
    @FXML private TableColumn<BorrowedBook, String> borrowDateColumn;
    @FXML private TableColumn<BorrowedBook, String> dueDateColumn;
    @FXML private TableColumn<BorrowedBook, String> statusColumn;

    // Database access (Following Lender Pattern)
    private final BookDAO bookDAO = new SQLiteBookDAO();
    private final ObservableList<BorrowedBook> masterData = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        // 1. Setup Columns (Following Lender's lambda style)
        titleColumn.setCellValueFactory(data -> data.getValue().titleProperty());
        authorColumn.setCellValueFactory(data -> data.getValue().authorProperty());
        borrowDateColumn.setCellValueFactory(data -> data.getValue().borrowDateProperty());
        dueDateColumn.setCellValueFactory(data -> data.getValue().dueDateProperty());
        statusColumn.setCellValueFactory(data -> data.getValue().statusProperty());

        // 2. Initial Data Load (Following Lender's loadBooks style)
        loadBooks();

        // 3. Search Bar logic (Filtering the master list)
        FilteredList<BorrowedBook> filteredData = new FilteredList<>(masterData, p -> true);
        searchBar.textProperty().addListener((observable, oldValue, newValue) -> {
            filteredData.setPredicate(book -> {
                if (newValue == null || newValue.isEmpty()) return true;
                String lowerCaseFilter = newValue.toLowerCase();
                return book.titleProperty().get().toLowerCase().contains(lowerCaseFilter) ||
                        book.authorProperty().get().toLowerCase().contains(lowerCaseFilter);
            });
        });

        borrowedTableView.setItems(filteredData);

        // 4. Default Sidebar State
        setActiveButton(btnMyBooks);
        setInactiveButton(btnBrowse);
    }

    /**
     * Replicates the loadBooks() procedure from LenderDashboardController
     * but adapts it for BorrowedBook data.
     */
    private void loadBooks() {
        masterData.clear();

        /* NOTE: Since your team is using SQLiteBookDAO, we pull the books.
         In a full implementation, you would use a LoanDAO to filter by 'currentUsername'.
         For now, we fetch all books to populate the UI.
        */
        List<Book> books = bookDAO.getAll();

        for (Book b : books) {
            // Mapping existing Book data to the BorrowedBook view structure
            masterData.add(new BorrowedBook(
                    b.getIsbn(),
                    b.getTitle(),
                    b.getAuthor(),
                    "2026-02-27", // Placeholder date until LoanDAO is ready
                    "2026-03-27",
                    "Active"
            ));
        }
    }

    // ── Sidebar Styling (Copied from LenderDashboardController) ──

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

    @FXML public void showMyBooks() { setActiveButton(btnMyBooks); setInactiveButton(btnBrowse); }
    @FXML public void showBrowseCatalog() { setActiveButton(btnBrowse); setInactiveButton(btnMyBooks); }
}
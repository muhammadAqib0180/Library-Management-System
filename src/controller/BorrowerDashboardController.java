package controller;

import database.BookDAO;
import database.SupaBookDAO;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import model.Book;

public class BorrowerDashboardController extends BaseDashboardController {

    // Sidebar buttons
    @FXML
    private Button btnMyBooks;
    @FXML
    private Button btnBrowse;

    // Panels
    @FXML
    private VBox myBooksPanel;
    @FXML
    private VBox browseCatalogPanel;

    // My Borrowed table
    @FXML
    private TableView<Book> borrowedTableView;
    @FXML
    private TableColumn<Book, String> titleColumn;
    @FXML
    private TableColumn<Book, String> authorColumn;
    @FXML
    private TableColumn<Book, String> borrowDateColumn;
    @FXML
    private TableColumn<Book, String> dueDateColumn;
    @FXML
    private TableColumn<Book, String> statusColumn;
    @FXML
    private Button renewButton;
    @FXML
    private Button returnButton;

    // Browse catalog table
    @FXML
    private TableView<Book> catalogTableView;
    @FXML
    private TableColumn<Book, String> catIsbnColumn;
    @FXML
    private TableColumn<Book, String> catTitleColumn;
    @FXML
    private TableColumn<Book, String> catAuthorColumn;
    @FXML
    private TableColumn<Book, String> catStatusColumn;

    private final BookDAO bookDAO = new SupaBookDAO();
    private final ObservableList<Book> catalogList = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        // Browse catalog columns
        catIsbnColumn
                .setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().getIsbn()));
        catTitleColumn.setCellValueFactory(
                data -> new javafx.beans.property.SimpleStringProperty(data.getValue().getTitle()));
        catAuthorColumn.setCellValueFactory(
                data -> new javafx.beans.property.SimpleStringProperty(data.getValue().getAuthor()));
        catStatusColumn.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(
                data.getValue().isAvailable() ? "Available" : "Borrowed"));

        catalogTableView.setItems(catalogList);

        // Start on My Borrowed panel
        showMyBooks();
        startStatusCheck();
    }

    @FXML
    public void showMyBooks() {
        myBooksPanel.setVisible(true);
        myBooksPanel.setManaged(true);
        browseCatalogPanel.setVisible(false);
        browseCatalogPanel.setManaged(false);
        setActiveButton(btnMyBooks);
        setInactiveButton(btnBrowse);
    }

    @FXML
    public void showBrowseCatalog() {
        browseCatalogPanel.setVisible(true);
        browseCatalogPanel.setManaged(true);
        myBooksPanel.setVisible(false);
        myBooksPanel.setManaged(false);
        setActiveButton(btnBrowse);
        setInactiveButton(btnMyBooks);

        // Load only AVAILABLE books from the DB
        catalogList.clear();
        catalogList.addAll(
                bookDAO.getAll().stream()
                        .filter(model.Book::isAvailable)
                        .collect(java.util.stream.Collectors.toList()));
    }

    @FXML
    private void switchToLender() {
        navigateTo("/view/LenderDashboard.fxml", "Lender Dashboard",
                ctrl -> ((LenderDashboardController) ctrl).setUsername(currentUsername));
    }

    @FXML
    private void handleRenew() {
        // Coming soon
    }

    @FXML
    private void handleReturn() {
        // Coming soon
    }

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
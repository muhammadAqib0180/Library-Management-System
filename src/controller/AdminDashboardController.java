package controller;

import database.SupaUserDAO;
import database.SupaBookDAO;
import database.UserDAO;
import database.BookDAO;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.SimpleIntegerProperty;
import model.Book;
import model.BookWithBorrower;
import model.User;
import util.AlertHelper;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class AdminDashboardController extends BaseDashboardController {

    @FXML private Button btnManageUsers;
    @FXML private VBox   welcomePanel;
    @FXML private VBox   manageUsersPanel;

    @FXML private VBox auditLogPanel; // Make sure this matches the fx:id in FXML
    @FXML private Button btnAuditLog;

    @FXML private VBox reportsPanel;
    @FXML private Button btnReports;



    @FXML private TableView<User>          userTableView;
    @FXML private TableColumn<User, Integer> idColumn;
    @FXML private TableColumn<User, String>  usernameColumn;
    @FXML private TableColumn<User, String>  roleColumn;
    @FXML private TableColumn<User, String>  statusColumn;
    @FXML private TableColumn<User, String>  lastLoginColumn;

    @FXML private Button deactivateUserBtn;
    @FXML private Label  adminStatusLabel;

    @FXML private Button btnAllBooks;
    @FXML private Button btnUserActivity;
    @FXML private Button btnOverdue;

    @FXML private VBox   allBooksPanel;
    @FXML private TextField bookSearchField;

    @FXML private Label totalBooksLabel;
    @FXML private Label borrowedCountLabel;
    @FXML private Label availableCountLabel;

    @FXML private TableView<BookWithBorrower>          allBooksTable;
    @FXML private TableColumn<BookWithBorrower, String> abIsbnCol;
    @FXML private TableColumn<BookWithBorrower, String> abTitleCol;
    @FXML private TableColumn<BookWithBorrower, String> abAuthorCol;
    @FXML private TableColumn<BookWithBorrower, String> abGenreCol;
    @FXML private TableColumn<BookWithBorrower, String> abOwnerCol;
    @FXML private TableColumn<BookWithBorrower, String> abBorrowerCol;
    @FXML private TableColumn<BookWithBorrower, String> abBorrowDateCol;
    @FXML private TableColumn<BookWithBorrower, String> abDueDateCol;
    @FXML private TableColumn<BookWithBorrower, String> abStatusCol;

    @FXML private VBox               userActivityPanel;
    @FXML private TextField          userSearchField;
    @FXML private Label              activityUserLabel;
    @FXML private HBox               activityStatsBox;
    @FXML private Label              actTotalLabel;
    @FXML private Label              actActiveLabel;
    @FXML private Label              actOverdueLabel;

    @FXML private TableView<BookWithBorrower>          userActivityTable;
    @FXML private TableColumn<BookWithBorrower, String> uaIsbnCol;
    @FXML private TableColumn<BookWithBorrower, String> uaTitleCol;
    @FXML private TableColumn<BookWithBorrower, String> uaAuthorCol;
    @FXML private TableColumn<BookWithBorrower, String> uaBorrowDateCol;
    @FXML private TableColumn<BookWithBorrower, String> uaDueDateCol;
    @FXML private TableColumn<BookWithBorrower, String> uaStatusCol;

    @FXML private VBox  overduePanel;
    @FXML private Label overdueCountLabel;
    @FXML private Label overdueTodayLabel;

    @FXML private TableView<BookWithBorrower>           overdueTable;
    @FXML private TableColumn<BookWithBorrower, String> odIsbnCol;
    @FXML private TableColumn<BookWithBorrower, String> odTitleCol;
    @FXML private TableColumn<BookWithBorrower, String> odAuthorCol;
    @FXML private TableColumn<BookWithBorrower, String> odBorrowerCol;
    @FXML private TableColumn<BookWithBorrower, String> odOwnerCol;
    @FXML private TableColumn<BookWithBorrower, String> odBorrowDateCol;
    @FXML private TableColumn<BookWithBorrower, String> odDueDateCol;
    @FXML private TableColumn<BookWithBorrower, String> odDaysOverdueCol;

    private final UserDAO userDAO = new SupaUserDAO();
    private final BookDAO bookDAO = new SupaBookDAO();

    private final ObservableList<User>            userList        = FXCollections.observableArrayList();
    private final ObservableList<BookWithBorrower> allBooksMaster  = FXCollections.observableArrayList();
    private final ObservableList<BookWithBorrower> userActivityList = FXCollections.observableArrayList();
    private final ObservableList<BookWithBorrower> overdueList     = FXCollections.observableArrayList();

    private Map<Integer, String> userIdToName;

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    @FXML
    public void initialize() {
        idColumn.setCellValueFactory(data -> new SimpleIntegerProperty(data.getValue().getId()).asObject());
        usernameColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getUsername()));
        roleColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getRole()));
        statusColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().isActive() ? "Active" : "Suspended"));
        lastLoginColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getLastLogin()));

        deactivateUserBtn.setOnAction(e -> {
            User selected = userTableView.getSelectionModel().getSelectedItem();
            if (selected != null) handleToggleUserStatus(selected);
        });

        userTableView.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            boolean has = newVal != null;
            deactivateUserBtn.setDisable(!has);
            if (has) {
                if (newVal.isActive()) {
                    deactivateUserBtn.setText("Suspend Account");
                    deactivateUserBtn.setStyle("-fx-background-color: #EF4444; -fx-text-fill: #FFFFFF; -fx-font-weight: 700; -fx-padding: 10 20; -fx-background-radius: 6; -fx-font-size: 13px; -fx-cursor: hand;");
                } else {
                    deactivateUserBtn.setText("Restore Account");
                    deactivateUserBtn.setStyle("-fx-background-color: #10B981; -fx-text-fill: #FFFFFF; -fx-font-weight: 700; -fx-padding: 10 20; -fx-background-radius: 6; -fx-font-size: 13px; -fx-cursor: hand;");
                }
            }
        });

        userTableView.setItems(userList);

        abIsbnCol.setCellValueFactory(d -> d.getValue().isbnProperty());
        abTitleCol.setCellValueFactory(d -> d.getValue().titleProperty());
        abAuthorCol.setCellValueFactory(d -> d.getValue().authorProperty());
        abGenreCol.setCellValueFactory(d -> d.getValue().genreProperty());
        abOwnerCol.setCellValueFactory(d -> d.getValue().ownerUsernameProperty());
        abBorrowerCol.setCellValueFactory(d -> d.getValue().borrowerUsernameProperty());
        abBorrowDateCol.setCellValueFactory(d -> d.getValue().borrowDateProperty());
        abDueDateCol.setCellValueFactory(d -> d.getValue().dueDateProperty());
        abStatusCol.setCellValueFactory(d -> d.getValue().statusProperty());

        abStatusCol.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) { setText(null); setStyle(""); return; }
                setText(item);
                switch (item) {
                    case "Available" -> setStyle("-fx-text-fill: #10B981; -fx-font-weight: 700;");
                    case "Borrowed"  -> setStyle("-fx-text-fill: #F59E0B; -fx-font-weight: 700;");
                    case "Overdue"   -> setStyle("-fx-text-fill: #EF4444; -fx-font-weight: 700;");
                    default          -> setStyle("");
                }
            }
        });

        FilteredList<BookWithBorrower> filteredBooks = new FilteredList<>(allBooksMaster, p -> true);
        bookSearchField.textProperty().addListener((obs, o, n) ->
                filteredBooks.setPredicate(b -> {
                    if (n == null || n.isBlank()) return true;
                    String f = n.toLowerCase();
                    return b.getTitle().toLowerCase().contains(f)
                            || b.getAuthor().toLowerCase().contains(f)
                            || b.getIsbn().toLowerCase().contains(f);
                }));
        allBooksTable.setItems(filteredBooks);

        uaIsbnCol.setCellValueFactory(d -> d.getValue().isbnProperty());
        uaTitleCol.setCellValueFactory(d -> d.getValue().titleProperty());
        uaAuthorCol.setCellValueFactory(d -> d.getValue().authorProperty());
        uaBorrowDateCol.setCellValueFactory(d -> d.getValue().borrowDateProperty());
        uaDueDateCol.setCellValueFactory(d -> d.getValue().dueDateProperty());
        uaStatusCol.setCellValueFactory(d -> d.getValue().statusProperty());

        uaStatusCol.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) { setText(null); setStyle(""); return; }
                setText(item);
                switch (item) {
                    case "Active"  -> setStyle("-fx-text-fill: #10B981; -fx-font-weight: 700;");
                    case "Overdue" -> setStyle("-fx-text-fill: #EF4444; -fx-font-weight: 700;");
                    default        -> setStyle("");
                }
            }
        });
        userActivityTable.setItems(userActivityList);

        odIsbnCol.setCellValueFactory(d -> d.getValue().isbnProperty());
        odTitleCol.setCellValueFactory(d -> d.getValue().titleProperty());
        odAuthorCol.setCellValueFactory(d -> d.getValue().authorProperty());
        odBorrowerCol.setCellValueFactory(d -> d.getValue().borrowerUsernameProperty());
        odOwnerCol.setCellValueFactory(d -> d.getValue().ownerUsernameProperty());
        odBorrowDateCol.setCellValueFactory(d -> d.getValue().borrowDateProperty());
        odDueDateCol.setCellValueFactory(d -> d.getValue().dueDateProperty());
        odDaysOverdueCol.setCellValueFactory(d -> d.getValue().daysOverdueProperty());

        odDaysOverdueCol.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) { setText(null); setStyle(""); return; }
                setText(item);
                setStyle("-fx-text-fill: #EF4444; -fx-font-weight: 700;");
            }
        });
        overdueTable.setItems(overdueList);

        startStatusCheck();
    }

    private void showOnly(VBox target) {
        // Add auditLogPanel AND reportsPanel to this list
        for (VBox p : new VBox[]{welcomePanel, manageUsersPanel, allBooksPanel, userActivityPanel, overduePanel, auditLogPanel, reportsPanel}) {
            p.setVisible(false);
            p.setManaged(false);
        }
        target.setVisible(true);
        target.setManaged(true);


    }

    @FXML
    public void showManageUsers() {
        showOnly(manageUsersPanel);
        setSidebarActive(btnManageUsers);
        userList.clear();
        userList.addAll(userDAO.getAllUsers());
    }

    @FXML
    public void showAllBooks() {
        showOnly(allBooksPanel);
        setSidebarActive(btnAllBooks);
        loadAllBooksData();
    }

    @FXML
    public void refreshAllBooks() {
        loadAllBooksData();
    }

    @FXML
    public void showUserActivity() {
        showOnly(userActivityPanel);
        setSidebarActive(btnUserActivity);
        populateUserPicker();
    }

    @FXML
    public void showOverdue() {
        showOnly(overduePanel);
        setSidebarActive(btnOverdue);
        loadOverdueData();
    }

    @FXML
    public void refreshOverdue() {
        loadOverdueData();
    }

    private void loadAllBooksData() {
        allBooksMaster.clear();
        Map<Integer, String> nameMap = getUserIdToNameMap();
        List<Book> books = bookDAO.getAll();
        LocalDate today = LocalDate.now();

        int borrowed = 0, available = 0;

        for (Book b : books) {
            String borrowerName = null;
            String borrowDate   = null;
            String dueDate      = null;
            String status;

            if (b.getBorrowedBy() != null) {
                borrowerName = nameMap.getOrDefault(b.getBorrowedBy(), "User #" + b.getBorrowedBy());
                borrowDate   = b.getBorrowDate() != null ? b.getBorrowDate().format(DATE_FMT) : "—";
                dueDate      = b.getDueDate()    != null ? b.getDueDate().format(DATE_FMT)    : "—";
                status       = (b.getDueDate() != null && b.getDueDate().isBefore(today)) ? "Overdue" : "Borrowed";
                borrowed++;
            } else {
                status = "Available";
                available++;
            }

            String ownerName = nameMap.getOrDefault(b.getOwnerId(), "User #" + b.getOwnerId());

            allBooksMaster.add(new BookWithBorrower(
                    b.getIsbn(), b.getTitle(), b.getAuthor(), b.getGenre(),
                    ownerName, borrowerName, borrowDate, dueDate, status, null));
        }

        totalBooksLabel.setText("Total: " + books.size());
        borrowedCountLabel.setText("Borrowed: " + borrowed);
        availableCountLabel.setText("Available: " + available);
    }

    private void populateUserPicker() {
        userSearchField.clear();
        userActivityList.clear();
        activityStatsBox.setVisible(false);
        activityStatsBox.setManaged(false);
        activityUserLabel.setText("");
        userSearchField.setOnAction(e -> loadUserActivity());
    }

    @FXML
    public void loadUserActivity() {
        String selectedUsername = userSearchField.getText().trim();
        if (selectedUsername.isBlank()) {
            activityUserLabel.setText("System: Input required.");
            return;
        }

        int userId = userDAO.findUserIdByUsername(selectedUsername);
        if (userId == -1) {
            activityUserLabel.setText("System: Record not found.");
            return;
        }

        userActivityList.clear();
        Map<Integer, String> nameMap = getUserIdToNameMap();
        List<Book> borrowed = bookDAO.findBorrowedByUser(userId);
        LocalDate today = LocalDate.now();

        int active = 0, overdue = 0;

        for (Book b : borrowed) {
            String borrowDate = b.getBorrowDate() != null ? b.getBorrowDate().format(DATE_FMT) : "—";
            String dueDate    = b.getDueDate()    != null ? b.getDueDate().format(DATE_FMT)    : "—";
            boolean isOverdue = b.getDueDate() != null && b.getDueDate().isBefore(today);
            String status     = isOverdue ? "Overdue" : "Active";
            if (isOverdue) overdue++; else active++;

            String ownerName = nameMap.getOrDefault(b.getOwnerId(), "User #" + b.getOwnerId());

            userActivityList.add(new BookWithBorrower(
                    b.getIsbn(), b.getTitle(), b.getAuthor(), b.getGenre(),
                    ownerName, selectedUsername, borrowDate, dueDate, status, null));
        }

        actTotalLabel.setText("Total Borrowed: " + borrowed.size());
        actActiveLabel.setText("Active: " + active);
        actOverdueLabel.setText("Overdue: " + overdue);

        activityStatsBox.setVisible(true);
        activityStatsBox.setManaged(true);
        activityUserLabel.setText("Viewing: " + selectedUsername);
    }

    @FXML
    public void showAuditLog() {
        // 1. Clear other views and show this panel
        showOnly(auditLogPanel);
        setSidebarActive(btnAuditLog);

        // 2. Load the Audit Log interface into the container if it's empty
        if (auditLogPanel.getChildren().isEmpty()) {
            try {
                javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(getClass().getResource("/view/AuditLogPanel.fxml"));
                VBox content = loader.load();
                auditLogPanel.getChildren().add(content);
                VBox.setVgrow(content, javafx.scene.layout.Priority.ALWAYS);
            } catch (java.io.IOException e) {
                System.err.println("Error loading Audit Log: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    @FXML
    public void showReports() {
        showOnly(reportsPanel);
        setSidebarActive(btnReports);

        if (reportsPanel.getChildren().isEmpty()) {
            try {
                javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(getClass().getResource("/view/ReportsPanel.fxml"));
                reportsPanel.getChildren().add(loader.load());
            } catch (java.io.IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void loadOverdueData() {
        overdueList.clear();
        Map<Integer, String> nameMap = getUserIdToNameMap();
        List<Book> overdue = bookDAO.findOverdueBooks();
        LocalDate today = LocalDate.now();

        for (Book b : overdue) {
            String borrowerName = nameMap.getOrDefault(b.getBorrowedBy(), "User #" + b.getBorrowedBy());
            String ownerName    = nameMap.getOrDefault(b.getOwnerId(),      "User #" + b.getOwnerId());
            String borrowDate   = b.getBorrowDate() != null ? b.getBorrowDate().format(DATE_FMT) : "—";
            String dueDate      = b.getDueDate()    != null ? b.getDueDate().format(DATE_FMT)    : "—";

            long days = (b.getDueDate() != null) ? ChronoUnit.DAYS.between(b.getDueDate(), today) : 0;
            String daysOverdue = days + "d";

            overdueList.add(new BookWithBorrower(
                    b.getIsbn(), b.getTitle(), b.getAuthor(), b.getGenre(),
                    ownerName, borrowerName, borrowDate, dueDate, "Overdue", daysOverdue));
        }

        overdueCountLabel.setText(overdue.size() == 0 ? "No active compliance alerts." : overdue.size() + " active compliance alerts.");
        overdueTodayLabel.setText("Sync: " + today.format(DATE_FMT));
    }

    private void handleToggleUserStatus(User user) {
        boolean newStatus = !user.isActive();

        boolean ok = newStatus
                ? AlertHelper.confirm(userTableView.getScene().getWindow(),
                "Restore Account", "Confirm action?",
                "Restore access for: " + user.getUsername() + "?", "Confirm")
                : AlertHelper.danger(userTableView.getScene().getWindow(),
                "Suspend Account", "Confirm action?",
                "Suspend access for: " + user.getUsername() + "?", "Suspend");

        if (ok) {
            if (userDAO.updateUserStatus(user.getId(), newStatus)) {
                adminStatusLabel.setTextFill(javafx.scene.paint.Color.web(newStatus ? "#10B981" : "#EF4444"));
                adminStatusLabel.setText("Updated: " + user.getUsername() + " -> " + (newStatus ? "Active" : "Suspended"));
                userList.clear();
                userList.addAll(userDAO.getAllUsers());
                userTableView.refresh();
            } else {
                adminStatusLabel.setTextFill(javafx.scene.paint.Color.web("#EF4444"));
                adminStatusLabel.setText("System: Update failed.");
            }
        }
    }

    private Map<Integer, String> getUserIdToNameMap() {
        userIdToName = userDAO.getAllUsers().stream()
                .collect(Collectors.toMap(User::getId, User::getUsername, (a, b) -> a));
        return userIdToName;
    }

    private void setSidebarActive(Button btn) {
        // Deep Sapphire Active State - White text, dark transparent background, light blue left border
        btn.setStyle("-fx-background-color: rgba(0,0,0,0.2); -fx-text-fill: #FFFFFF; -fx-font-weight: 700; -fx-padding: 10 16; -fx-cursor: hand; -fx-background-radius: 6; -fx-border-color: transparent transparent transparent #60A5FA; -fx-border-width: 0 0 0 3;");
    }

    private void setSidebarInactive(Button btn) {
        // Muted Blue Inactive State
        btn.setStyle("-fx-background-color: transparent; -fx-text-fill: #93C5FD; -fx-font-weight: 600; -fx-padding: 10 16; -fx-cursor: hand; -fx-background-radius: 6; -fx-border-width: 0;");
    }
}
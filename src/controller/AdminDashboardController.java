package controller;

import database.SupaUserDAO;
import database.SupaBookDAO;
import database.UserDAO;
import database.BookDAO;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.SimpleIntegerProperty;
import model.Book;
import model.BookWithBorrower;
import model.User;
import util.AlertHelper;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class AdminDashboardController extends BaseDashboardController {

    // ── EXISTING FXML FIELDS ──────────────────────────────────────────────────

    @FXML private Button btnManageUsers;
    @FXML private VBox   welcomePanel;
    @FXML private VBox   manageUsersPanel;

    @FXML private TableView<User>          userTableView;
    @FXML private TableColumn<User, Integer> idColumn;
    @FXML private TableColumn<User, String>  usernameColumn;
    @FXML private TableColumn<User, String>  roleColumn;
    @FXML private TableColumn<User, String>  statusColumn;
    @FXML private TableColumn<User, String>  lastLoginColumn;

    @FXML private Button deactivateUserBtn;
    @FXML private Label  adminStatusLabel;

    // ── NEW SIDEBAR BUTTONS ───────────────────────────────────────────────────

    @FXML private Button btnAllBooks;
    @FXML private Button btnUserActivity;
    @FXML private Button btnOverdue;

    // ── PANEL 1: ALL BOOKS WITH BORROWER INFO ─────────────────────────────────

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

    // ── PANEL 2: PER-USER BORROW ACTIVITY ────────────────────────────────────

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

    // ── PANEL 3: OVERDUE BOOKS ────────────────────────────────────────────────

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

    // ── DAOs ──────────────────────────────────────────────────────────────────

    private final UserDAO userDAO = new SupaUserDAO();
    private final BookDAO bookDAO = new SupaBookDAO();

    // ── Observable lists ─────────────────────────────────────────────────────

    private final ObservableList<User>            userList        = FXCollections.observableArrayList();
    private final ObservableList<BookWithBorrower> allBooksMaster  = FXCollections.observableArrayList();
    private final ObservableList<BookWithBorrower> userActivityList = FXCollections.observableArrayList();
    private final ObservableList<BookWithBorrower> overdueList     = FXCollections.observableArrayList();

    /** username → User  cache, populated once per panel open */
    private Map<Integer, String> userIdToName;

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    // ══════════════════════════════════════════════════════════════════════════
    // INITIALIZE
    // ══════════════════════════════════════════════════════════════════════════

    @FXML
    public void initialize() {

        // ── Existing: Manage Users table ──
        idColumn.setCellValueFactory(
                data -> new SimpleIntegerProperty(data.getValue().getId()).asObject());
        usernameColumn.setCellValueFactory(
                data -> new SimpleStringProperty(data.getValue().getUsername()));
        roleColumn.setCellValueFactory(
                data -> new SimpleStringProperty(data.getValue().getRole()));
        statusColumn.setCellValueFactory(
                data -> new SimpleStringProperty(data.getValue().isActive() ? "Active" : "Inactive"));
        lastLoginColumn.setCellValueFactory(
                data -> new SimpleStringProperty(data.getValue().getLastLogin()));

        deactivateUserBtn.setOnAction(e -> {
            User selected = userTableView.getSelectionModel().getSelectedItem();
            if (selected != null) handleToggleUserStatus(selected);
        });

        userTableView.getSelectionModel().selectedItemProperty()
                .addListener((obs, oldVal, newVal) -> {
                    boolean has = newVal != null;
                    deactivateUserBtn.setDisable(!has);
                    if (has) {
                        if (newVal.isActive()) {
                            deactivateUserBtn.setText("🚫 Deactivate User");
                            deactivateUserBtn.setStyle(
                                    "-fx-background-color: #e74c3c; -fx-text-fill: white; " +
                                            "-fx-font-weight: bold; -fx-padding: 10 20; " +
                                            "-fx-background-radius: 8; -fx-font-size: 12px; -fx-cursor: hand;");
                        } else {
                            deactivateUserBtn.setText("✓ Activate User");
                            deactivateUserBtn.setStyle(
                                    "-fx-background-color: #27ae60; -fx-text-fill: white; " +
                                            "-fx-font-weight: bold; -fx-padding: 10 20; " +
                                            "-fx-background-radius: 8; -fx-font-size: 12px; -fx-cursor: hand;");
                        }
                    }
                });

        userTableView.setItems(userList);

        // ── New Panel 1: All Books table columns ──
        abIsbnCol.setCellValueFactory(d -> d.getValue().isbnProperty());
        abTitleCol.setCellValueFactory(d -> d.getValue().titleProperty());
        abAuthorCol.setCellValueFactory(d -> d.getValue().authorProperty());
        abGenreCol.setCellValueFactory(d -> d.getValue().genreProperty());
        abOwnerCol.setCellValueFactory(d -> d.getValue().ownerUsernameProperty());
        abBorrowerCol.setCellValueFactory(d -> d.getValue().borrowerUsernameProperty());
        abBorrowDateCol.setCellValueFactory(d -> d.getValue().borrowDateProperty());
        abDueDateCol.setCellValueFactory(d -> d.getValue().dueDateProperty());
        abStatusCol.setCellValueFactory(d -> d.getValue().statusProperty());

        // Colour-code the Status column: green = Available, orange = Borrowed, red = Overdue
        abStatusCol.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) { setText(null); setStyle(""); return; }
                setText(item);
                switch (item) {
                    case "Available" -> setStyle("-fx-text-fill: #1e8449; -fx-font-weight: bold;");
                    case "Borrowed"  -> setStyle("-fx-text-fill: #d68910; -fx-font-weight: bold;");
                    case "Overdue"   -> setStyle("-fx-text-fill: #c0392b; -fx-font-weight: bold;");
                    default          -> setStyle("");
                }
            }
        });

        // Live search filtering
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

        // ── New Panel 2: User Activity table columns ──
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
                    case "Active"  -> setStyle("-fx-text-fill: #1e8449; -fx-font-weight: bold;");
                    case "Overdue" -> setStyle("-fx-text-fill: #c0392b; -fx-font-weight: bold;");
                    default        -> setStyle("");
                }
            }
        });
        userActivityTable.setItems(userActivityList);

        // ── New Panel 3: Overdue table columns ──
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
                setStyle("-fx-text-fill: #c0392b; -fx-font-weight: bold;");
            }
        });
        overdueTable.setItems(overdueList);

        startStatusCheck();
    }

    // ══════════════════════════════════════════════════════════════════════════
    // SIDEBAR NAVIGATION
    // ══════════════════════════════════════════════════════════════════════════

    /** Hide all panels, then show the one requested. */
    private void showOnly(VBox target) {
        for (VBox p : new VBox[]{welcomePanel, manageUsersPanel, allBooksPanel, userActivityPanel, overduePanel}) {
            p.setVisible(false);
            p.setManaged(false);
        }
        target.setVisible(true);
        target.setManaged(true);
        // Reset all sidebar buttons to inactive, then mark the active one
        for (Button b : new Button[]{btnManageUsers, btnAllBooks, btnUserActivity, btnOverdue}) {
            setSidebarInactive(b);
        }
    }

    // ── EXISTING ──
    @FXML
    public void showManageUsers() {
        showOnly(manageUsersPanel);
        setSidebarActive(btnManageUsers);
        userList.clear();
        userList.addAll(userDAO.getAllUsers());
    }

    // ── NEW: Panel 1 ──
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

    // ── NEW: Panel 2 ──
    @FXML
    public void showUserActivity() {
        showOnly(userActivityPanel);
        setSidebarActive(btnUserActivity);
        populateUserPicker();
    }

    // ── NEW: Panel 3 ──
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

    // ══════════════════════════════════════════════════════════════════════════
    // DATA LOADING — PANEL 1: ALL BOOKS
    // ══════════════════════════════════════════════════════════════════════════

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

            if (b.getBorrowedById() != null) {
                borrowerName = nameMap.getOrDefault(b.getBorrowedById(), "User #" + b.getBorrowedById());
                borrowDate   = b.getBorrowDate() != null ? b.getBorrowDate().format(DATE_FMT) : "—";
                dueDate      = b.getDueDate()    != null ? b.getDueDate().format(DATE_FMT)    : "—";
                status       = (b.getDueDate() != null && b.getDueDate().isBefore(today))
                        ? "Overdue" : "Borrowed";
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

        int total = books.size();
        totalBooksLabel.setText("📚 Total: " + total);
        borrowedCountLabel.setText("📤 Borrowed: " + borrowed);
        availableCountLabel.setText("✅ Available: " + available);
    }

    // ══════════════════════════════════════════════════════════════════════════
    // DATA LOADING — PANEL 2: USER ACTIVITY
    // ══════════════════════════════════════════════════════════════════════════

    private void populateUserPicker() {
        userSearchField.clear();
        userActivityList.clear();
        activityStatsBox.setVisible(false);
        activityStatsBox.setManaged(false);
        activityUserLabel.setText("");
        // Allow pressing Enter in the search field to trigger the lookup
        userSearchField.setOnAction(e -> loadUserActivity());
    }

    @FXML
    public void loadUserActivity() {
        String selectedUsername = userSearchField.getText().trim();
        if (selectedUsername.isBlank()) {
            activityUserLabel.setText("⚠ Please enter a username first.");
            return;
        }

        // Resolve user ID
        int userId = userDAO.findUserIdByUsername(selectedUsername);
        if (userId == -1) {
            activityUserLabel.setText("User not found.");
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

        // Update stats chips
        actTotalLabel.setText("📚 Total Borrowed: " + borrowed.size());
        actActiveLabel.setText("✅ Active: " + active);
        actOverdueLabel.setText("🚨 Overdue: " + overdue);

        activityStatsBox.setVisible(true);
        activityStatsBox.setManaged(true);
        activityUserLabel.setText("Showing activity for: " + selectedUsername);
    }

    // ══════════════════════════════════════════════════════════════════════════
    // DATA LOADING — PANEL 3: OVERDUE BOOKS
    // ══════════════════════════════════════════════════════════════════════════

    private void loadOverdueData() {
        overdueList.clear();
        Map<Integer, String> nameMap = getUserIdToNameMap();
        List<Book> overdue = bookDAO.findOverdueBooks();
        LocalDate today = LocalDate.now();

        for (Book b : overdue) {
            String borrowerName = nameMap.getOrDefault(b.getBorrowedById(), "User #" + b.getBorrowedById());
            String ownerName    = nameMap.getOrDefault(b.getOwnerId(),      "User #" + b.getOwnerId());
            String borrowDate   = b.getBorrowDate() != null ? b.getBorrowDate().format(DATE_FMT) : "—";
            String dueDate      = b.getDueDate()    != null ? b.getDueDate().format(DATE_FMT)    : "—";

            long days = (b.getDueDate() != null)
                    ? ChronoUnit.DAYS.between(b.getDueDate(), today) : 0;
            String daysOverdue = days + (days == 1 ? " day" : " days");

            overdueList.add(new BookWithBorrower(
                    b.getIsbn(), b.getTitle(), b.getAuthor(), b.getGenre(),
                    ownerName, borrowerName, borrowDate, dueDate, "Overdue", daysOverdue));
        }

        overdueCountLabel.setText(overdue.size() == 0
                ? "No overdue books right now."
                : overdue.size() + " book" + (overdue.size() == 1 ? "" : "s") + " currently overdue.");
        overdueTodayLabel.setText("As of " + today.format(DATE_FMT));
    }

    // ══════════════════════════════════════════════════════════════════════════
    // EXISTING: Toggle User Active/Inactive
    // ══════════════════════════════════════════════════════════════════════════

    private void handleToggleUserStatus(User user) {
        boolean newStatus = !user.isActive();

        boolean ok = newStatus
                ? AlertHelper.confirm(userTableView.getScene().getWindow(),
                "Activate User", "Activate this user?",
                "Do you want to reactivate: " + user.getUsername() + "?", "Activate")
                : AlertHelper.danger(userTableView.getScene().getWindow(),
                "Deactivate User", "Deactivate this user?",
                "Do you want to deactivate: " + user.getUsername() + "?", "Deactivate");

        if (ok) {
            if (userDAO.updateUserStatus(user.getId(), newStatus)) {
                adminStatusLabel.setTextFill(javafx.scene.paint.Color.GREEN);
                adminStatusLabel.setText("User " + user.getUsername()
                        + " is now " + (newStatus ? "Active" : "Inactive"));
                userList.clear();
                userList.addAll(userDAO.getAllUsers());
                userTableView.refresh();
            } else {
                adminStatusLabel.setTextFill(javafx.scene.paint.Color.RED);
                adminStatusLabel.setText("Failed to update user status.");
            }
        }
    }

    // ══════════════════════════════════════════════════════════════════════════
    // HELPERS
    // ══════════════════════════════════════════════════════════════════════════

    /**
     * Builds / returns a cached id→username map from all users.
     * Re-fetched every time a panel opens so it stays fresh.
     */
    private Map<Integer, String> getUserIdToNameMap() {
        userIdToName = userDAO.getAllUsers().stream()
                .collect(Collectors.toMap(User::getId, User::getUsername, (a, b) -> a));
        return userIdToName;
    }

    private void setSidebarActive(Button btn) {
        btn.setStyle("-fx-background-color: rgba(0,0,0,0.2); -fx-text-fill: white; " +
                "-fx-font-weight: bold; -fx-font-size: 13px; -fx-padding: 14 20; " +
                "-fx-cursor: hand; -fx-background-radius: 0; " +
                "-fx-border-color: transparent transparent transparent #bdc3c7; " +
                "-fx-border-width: 0 0 0 3; -fx-alignment: CENTER_LEFT;");
    }

    private void setSidebarInactive(Button btn) {
        btn.setStyle("-fx-background-color: transparent; -fx-text-fill: #bdc3c7; " +
                "-fx-font-size: 13px; -fx-padding: 14 20; -fx-cursor: hand; " +
                "-fx-background-radius: 0; -fx-alignment: CENTER_LEFT;");
    }
}
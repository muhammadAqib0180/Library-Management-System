package controller;

import database.BookDAO;
import database.BorrowRequestDAO;
import database.NotificationDAO;
import database.SupaBookDAO;
import database.SupaBorrowRequestDAO;
import database.SupaNotificationDAO;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.scene.layout.HBox;
import javafx.scene.layout.BorderPane;
import javafx.scene.input.KeyCode;
import model.Book;
import model.BorrowRequest;
import model.Notification;
import java.time.LocalDate;
import java.time.LocalDateTime;

public class BorrowerDashboardController extends BaseDashboardController {

    // Sidebar buttons
    @FXML
    private Button btnMyBooks;
    @FXML
    private Button btnMyRequests;
    @FXML
    private Button btnBrowse;

    // Notification badge
    @FXML
    private Label notificationBell;
    @FXML
    private Label notificationBadge;
    @FXML
    private VBox notificationPanel;
    @FXML
    private VBox notificationListContainer;

    // Overdue Warning Banner
    @FXML
    private HBox overdueWarningBanner;
    @FXML
    private Label overdueWarningTitle;
    @FXML
    private Label overdueWarningMessage;

    // Root Dashboard
    @FXML
    private BorderPane dashboardRoot;

    // Red vignette overlay (defined in FXML)
    @FXML
    private javafx.scene.layout.StackPane vignetteOverlay;

    // Panels
    @FXML
    private VBox myBooksPanel;
    @FXML
    private VBox myRequestsPanel;
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
    @FXML
    private Button borrowButton;

    @FXML
    private TextField catalogSearchField;

    // My Requests table
    @FXML
    private TableView<BorrowRequest> requestsTableView;
    @FXML
    private TableColumn<BorrowRequest, String> reqBookTitleColumn;
    @FXML
    private TableColumn<BorrowRequest, String> reqAuthorColumn;
    @FXML
    private TableColumn<BorrowRequest, String> reqStatusColumn;
    @FXML
    private TableColumn<BorrowRequest, String> reqDueDateColumn;
    @FXML
    private TableColumn<BorrowRequest, String> reqDateRequestedColumn;
    @FXML
    private Button cancelRequestButton;

    private final BookDAO bookDAO = new SupaBookDAO();
    private final BorrowRequestDAO borrowRequestDAO = new SupaBorrowRequestDAO();
    private final NotificationDAO notificationDAO = new SupaNotificationDAO();
    private final ObservableList<Book> borrowedList = FXCollections.observableArrayList();
    private final ObservableList<Book> catalogList = FXCollections.observableArrayList();
    private final ObservableList<BorrowRequest> requestsList = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        // Setup My Borrowed Books table columns
        titleColumn.setCellValueFactory(data -> 
            new javafx.beans.property.SimpleStringProperty(data.getValue().getTitle()));
        authorColumn.setCellValueFactory(data -> 
            new javafx.beans.property.SimpleStringProperty(data.getValue().getAuthor()));
        borrowDateColumn.setCellValueFactory(data -> 
            new javafx.beans.property.SimpleStringProperty(
                data.getValue().getBorrowDate() != null ? data.getValue().getBorrowDate().toString() : "N/A"));
        dueDateColumn.setCellValueFactory(data -> 
            new javafx.beans.property.SimpleStringProperty(
                data.getValue().getDueDate() != null ? data.getValue().getDueDate().toString() : "N/A"));
        statusColumn.setCellValueFactory(data -> 
            new javafx.beans.property.SimpleStringProperty(getDueStatus(data.getValue().getDueDate())));

        borrowedTableView.setItems(borrowedList);
        
        // Add keyboard shortcut for testing overdue warning: Ctrl+Shift+O
        borrowedTableView.setOnKeyPressed(event -> {
            if (event.isControlDown() && event.isShiftDown()) {
                if (event.getCode() == javafx.scene.input.KeyCode.O) {
                    testOverdueWarning();
                    event.consume();
                } else if (event.getCode() == javafx.scene.input.KeyCode.R) {
                    removeTestOverdueBooks();
                    event.consume();
                }
            }
        });
        
        // Setup the vignette overlay with the red gradient
        setupVignetteOverlay();
        
        // Setup button handlers
        renewButton.setOnAction(e -> {
            Book selected = borrowedTableView.getSelectionModel().getSelectedItem();
            if (selected != null) handleRenew(selected);
        });
        returnButton.setOnAction(e -> {
            Book selected = borrowedTableView.getSelectionModel().getSelectedItem();
            if (selected != null) handleReturn(selected);
        });
        borrowButton.setOnAction(e -> {
            Book selected = catalogTableView.getSelectionModel().getSelectedItem();
            if (selected != null) handleBorrow(selected);
        });

        // Enable/disable buttons based on selection
        borrowedTableView.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            boolean hasSelection = newVal != null;
            renewButton.setDisable(!hasSelection);
            returnButton.setDisable(!hasSelection);
        });

        catalogTableView.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            borrowButton.setDisable(newVal == null);
        });

        // My Requests table columns
        reqBookTitleColumn.setCellValueFactory(data -> {
            Book book = cachedBooks.getOrDefault(data.getValue().getBookIsbn(), null);
            if (book == null) {
                book = bookDAO.findByIsbn(data.getValue().getBookIsbn());
                if (book != null) cachedBooks.put(data.getValue().getBookIsbn(), book);
            }
            return new javafx.beans.property.SimpleStringProperty(book != null ? book.getTitle() : "N/A");
        });
        reqAuthorColumn.setCellValueFactory(data -> {
            Book book = cachedBooks.getOrDefault(data.getValue().getBookIsbn(), null);
            if (book == null) {
                book = bookDAO.findByIsbn(data.getValue().getBookIsbn());
                if (book != null) cachedBooks.put(data.getValue().getBookIsbn(), book);
            }
            return new javafx.beans.property.SimpleStringProperty(book != null ? book.getAuthor() : "N/A");
        });
        reqStatusColumn.setCellValueFactory(data -> 
            new javafx.beans.property.SimpleStringProperty(data.getValue().getStatus()));
        reqDueDateColumn.setCellValueFactory(data ->
            new javafx.beans.property.SimpleStringProperty(
                data.getValue().getRequestedDueDate() != null ? data.getValue().getRequestedDueDate().toString() : "N/A"));
        reqDateRequestedColumn.setCellValueFactory(data ->
            new javafx.beans.property.SimpleStringProperty(
                data.getValue().getCreatedAt() != null ? data.getValue().getCreatedAt().toLocalDate().toString() : "N/A"));

        requestsTableView.setItems(requestsList);

        // Cancel request button handler
        cancelRequestButton.setOnAction(e -> {
            BorrowRequest selected = requestsTableView.getSelectionModel().getSelectedItem();
            if (selected != null) handleCancelRequest(selected);
        });

        // Enable/disable cancel button based on selection
        requestsTableView.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            boolean hasSelection = newVal != null && "PENDING".equals(newVal.getStatus());
            cancelRequestButton.setDisable(!hasSelection);
        });

        // Browse catalog columns
        catIsbnColumn.setCellValueFactory(data -> 
            new javafx.beans.property.SimpleStringProperty(data.getValue().getIsbn()));
        catTitleColumn.setCellValueFactory(data -> 
            new javafx.beans.property.SimpleStringProperty(data.getValue().getTitle()));
        catAuthorColumn.setCellValueFactory(data -> 
            new javafx.beans.property.SimpleStringProperty(data.getValue().getAuthor()));
        catStatusColumn.setCellValueFactory(data -> 
            new javafx.beans.property.SimpleStringProperty(
                data.getValue().isAvailable() ? "Available" : "Borrowed"));

        catalogTableView.setItems(catalogList);

            // Catalog search bar logic
            if (catalogSearchField != null) {
                catalogSearchField.textProperty().addListener((obs, oldVal, newVal) -> {
                    filterCatalogBooks(newVal);
                });
            }

        // Update notification badge
        updateNotificationBadge();

        // Start on My Borrowed panel
        showMyBooks();
    }

    private void updateNotificationBadge() {
        int unreadCount = notificationDAO.getUnreadCount(currentUserId);
        if (unreadCount > 0) {
            notificationBadge.setText(String.valueOf(unreadCount));
            notificationBadge.setStyle("-fx-text-fill: white; -fx-background-color: #e74c3c; " +
                    "-fx-font-size: 9px; -fx-font-weight: bold; " +
                    "-fx-padding: 0 3; -fx-background-radius: 8; " +
                    "-fx-alignment: CENTER; -fx-min-width: 16; -fx-min-height: 16;");
            notificationBadge.setVisible(true);
        } else {
            notificationBadge.setText("0");
            notificationBadge.setStyle("-fx-text-fill: white; -fx-background-color: #e74c3c; " +
                    "-fx-font-size: 9px; -fx-font-weight: bold; " +
                    "-fx-padding: 0 3; -fx-background-radius: 8; " +
                    "-fx-alignment: CENTER; -fx-min-width: 16; -fx-min-height: 16;");
            notificationBadge.setVisible(false);
        }
    }

    @FXML
    public void toggleNotificationPanel() {
        if (notificationPanel.isVisible()) {
            notificationPanel.setVisible(false);
            notificationPanel.setManaged(false);
        } else {
            notificationPanel.setVisible(true);
            notificationPanel.setManaged(true);
            // Load notifications in background to avoid UI freezing
            new Thread(() -> {
                java.util.List<Notification> notifications = notificationDAO.getUnreadNotifications(currentUserId);
                javafx.application.Platform.runLater(() -> {
                    notificationListContainer.getChildren().clear();
                    if (notifications.isEmpty()) {
                        Label emptyLabel = new Label("No new notifications");
                        emptyLabel.setStyle("-fx-text-fill: #999; -fx-font-size: 13px;");
                        notificationListContainer.getChildren().add(emptyLabel);
                        return;
                    }
                    for (Notification notif : notifications) {
                        VBox notificationItem = createNotificationItem(notif);
                        notificationListContainer.getChildren().add(notificationItem);
                    }
                });
            }).start();
        }
    }

    private void loadNotifications() {
        java.util.List<Notification> notifications = notificationDAO.getUnreadNotifications(currentUserId);
        notificationListContainer.getChildren().clear();

        if (notifications.isEmpty()) {
            Label emptyLabel = new Label("No new notifications");
            emptyLabel.setStyle("-fx-text-fill: #999; -fx-font-size: 13px;");
            notificationListContainer.getChildren().add(emptyLabel);
            return;
        }

        for (Notification notif : notifications) {
            VBox notificationItem = createNotificationItem(notif);
            notificationListContainer.getChildren().add(notificationItem);
        }
    }

    private VBox createNotificationItem(Notification notif) {
        VBox item = new VBox(6);
        item.setStyle("-fx-border-color: #e8e8e8; -fx-border-width: 0 0 1 0; -fx-padding: 12 8; -fx-cursor: hand;");
        item.setOnMouseEntered(e -> item.setStyle("-fx-background-color: #f5f5f5; -fx-border-color: #e8e8e8; -fx-border-width: 0 0 1 0; -fx-padding: 12 8; -fx-cursor: hand;"));
        item.setOnMouseExited(e -> item.setStyle("-fx-border-color: #e8e8e8; -fx-border-width: 0 0 1 0; -fx-padding: 12 8; -fx-cursor: hand;"));
        item.setOnMouseClicked(e -> handleNotificationClick(notif));

        Label titleLabel = new Label(notif.getTitle());
        titleLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 13px; -fx-text-fill: #1a1a2e;");
        titleLabel.setWrapText(true);

        Label messageLabel = new Label(notif.getMessage());
        messageLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #666; -fx-wrap-text: true;");
        messageLabel.setWrapText(true);

        Label timeLabel = new Label(getTimeAgo(notif.getCreatedAt()));
        timeLabel.setStyle("-fx-font-size: 10px; -fx-text-fill: #999;");

        item.getChildren().addAll(titleLabel, messageLabel, timeLabel);
        return item;
    }

    private void handleNotificationClick(Notification notif) {
        // Mark notification as read
        notificationDAO.markAsRead(notif.getNotificationId());

        // Navigate based on notification type
        if ("BORROW_REQUEST".equals(notif.getType())) {
            // Lender's view - not applicable for borrower
        } else if ("REQUEST_ACCEPTED".equals(notif.getType()) || "REQUEST_REJECTED".equals(notif.getType())) {
            // Show my requests
            showMyRequests();
        }

        // Close the panel
        notificationPanel.setVisible(false);
        notificationPanel.setManaged(false);

        // Update badge
        updateNotificationBadge();
    }

    private String getTimeAgo(LocalDateTime dateTime) {
        if (dateTime == null) return "";
        LocalDateTime now = LocalDateTime.now();
        long minutes = java.time.temporal.ChronoUnit.MINUTES.between(dateTime, now);
        long hours = java.time.temporal.ChronoUnit.HOURS.between(dateTime, now);
        long days = java.time.temporal.ChronoUnit.DAYS.between(dateTime, now);

        if (minutes < 1) return "Just now";
        if (minutes < 60) return minutes + "m ago";
        if (hours < 24) return hours + "h ago";
        if (days == 1) return "Yesterday";
        return days + "d ago";
    }

    private String getDueStatus(LocalDate dueDate) {
        if (dueDate == null) return "Unknown";
        LocalDate today = LocalDate.now();
        if (dueDate.isBefore(today)) return "OVERDUE";
        if (dueDate.equals(today)) return "Due Today";
        long daysLeft = java.time.temporal.ChronoUnit.DAYS.between(today, dueDate);
        return daysLeft + " days left";
    }

    @FXML
    public void showMyBooks() {
        myBooksPanel.setVisible(true);
        myBooksPanel.setManaged(true);
        myRequestsPanel.setVisible(false);
        myRequestsPanel.setManaged(false);
        browseCatalogPanel.setVisible(false);
        browseCatalogPanel.setManaged(false);
        setActiveButton(btnMyBooks);
        setInactiveButton(btnMyRequests);
        setInactiveButton(btnBrowse);
        loadMyBorrowedBooks();
    }

    @FXML
    public void showMyRequests() {
        myRequestsPanel.setVisible(true);
        myRequestsPanel.setManaged(true);
        myBooksPanel.setVisible(false);
        myBooksPanel.setManaged(false);
        browseCatalogPanel.setVisible(false);
        browseCatalogPanel.setManaged(false);
        setActiveButton(btnMyRequests);
        setInactiveButton(btnMyBooks);
        setInactiveButton(btnBrowse);
        loadMyRequests();
        // Still check for overdue books to keep warning visible
        checkForOverdueBooks();
    }

    @FXML
    public void showBrowseCatalog() {
        browseCatalogPanel.setVisible(true);
        browseCatalogPanel.setManaged(true);
        myBooksPanel.setVisible(false);
        myBooksPanel.setManaged(false);
        myRequestsPanel.setVisible(false);
        myRequestsPanel.setManaged(false);
        setActiveButton(btnBrowse);
        setInactiveButton(btnMyBooks);
        setInactiveButton(btnMyRequests);
        loadCatalog();
        // Still check for overdue books to keep warning visible
        checkForOverdueBooks();
    }

    private void loadMyBorrowedBooks() {
        borrowedList.clear();
        borrowedList.addAll(bookDAO.findBorrowedByUser(currentUserId));
        checkForOverdueBooks();
    }

    private void loadMyRequests() {
        // Load requests asynchronously to avoid blocking UI
        new Thread(() -> {
            java.util.List<BorrowRequest> requests = borrowRequestDAO.getMyRequests(currentUserId);
            
            // Pre-fetch all book details to avoid multiple DB queries
            java.util.Map<String, Book> bookCache = new java.util.HashMap<>();
            for (BorrowRequest req : requests) {
                if (!bookCache.containsKey(req.getBookIsbn())) {
                    Book book = bookDAO.findByIsbn(req.getBookIsbn());
                    if (book != null) {
                        bookCache.put(req.getBookIsbn(), book);
                    }
                }
            }
            
            // Store cache for cell value factories to use
            cachedBooks = bookCache;
            
            // Update UI on JavaFX thread
            javafx.application.Platform.runLater(() -> {
                requestsList.clear();
                requestsList.addAll(requests);
            });
        }).start();
    }
    
    // Cache for books to avoid repeated queries
    private java.util.Map<String, Book> cachedBooks = new java.util.HashMap<>();

    private void loadCatalog() {
            catalogList.clear();
            catalogList.addAll(
                bookDAO.getAll().stream()
                    .filter(book -> book.isAvailable() && book.isListed() && book.getOwnerId() != currentUserId)
                    .collect(java.util.stream.Collectors.toList()));
            // Re-apply filter if search bar is not empty
            if (catalogSearchField != null && !catalogSearchField.getText().isEmpty()) {
                filterCatalogBooks(catalogSearchField.getText());
            }
        }

        private void filterCatalogBooks(String filter) {
            if (filter == null || filter.isEmpty()) {
                catalogTableView.setItems(catalogList);
                return;
            }
            String lower = filter.toLowerCase();
            ObservableList<Book> filtered = catalogList.filtered(book ->
                (book.getTitle() != null && book.getTitle().toLowerCase().contains(lower)) ||
                (book.getAuthor() != null && book.getAuthor().toLowerCase().contains(lower)) ||
                (book.getIsbn() != null && book.getIsbn().toLowerCase().contains(lower)) ||
                (book.getGenre() != null && book.getGenre().toLowerCase().contains(lower))
            );
            catalogTableView.setItems(filtered);
    }

    @FXML
    private void switchToLender() {
        navigateTo("/view/LenderDashboard.fxml", "Lender Dashboard",
                ctrl -> ((LenderDashboardController) ctrl).setUsername(currentUsername));
    }

    @FXML
    private void handleBorrow(Book book) {
        Dialog<LocalDate> dialog = new Dialog<>();
        dialog.setTitle("Request to Borrow Book");
        dialog.setHeaderText("Request: " + book.getTitle());

        DatePicker dueDate = new DatePicker(LocalDate.now().plusDays(14));
        dueDate.setStyle("-fx-font-size: 13px; -fx-padding: 10;");
        
        javafx.scene.layout.GridPane grid = new javafx.scene.layout.GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new javafx.geometry.Insets(10));
        grid.add(new Label("Requested Due Date (default 14 days):"), 0, 0);
        grid.add(dueDate, 1, 0);
        
        dialog.getDialogPane().setContent(grid);
        dialog.getDialogPane().getButtonTypes().addAll(javafx.scene.control.ButtonType.OK, javafx.scene.control.ButtonType.CANCEL);

        dialog.setResultConverter(buttonType -> 
            buttonType == javafx.scene.control.ButtonType.OK ? dueDate.getValue() : null);

        if (dialog.showAndWait().isPresent()) {
            LocalDate selectedDueDate = dialog.getResult();
            if (selectedDueDate != null && selectedDueDate.isAfter(LocalDate.now())) {
                // Create a borrow request instead of immediately borrowing
                BorrowRequest request = new BorrowRequest(
                    book.getIsbn(),
                    currentUserId,
                    book.getOwnerId(),
                    selectedDueDate
                );
                
                int requestId = borrowRequestDAO.createBorrowRequest(request);
                
                if (requestId > 0) {
                    // Create notification for the lender
                    Notification lenderNotif = new Notification(
                        book.getOwnerId(),
                        "BORROW_REQUEST",
                        "New Request for " + book.getTitle(),
                        "User has requested to borrow \"" + book.getTitle() + "\" until " + selectedDueDate,
                        book.getIsbn(),
                        currentUserId,
                        "pending-requests"
                    );
                    notificationDAO.createNotification(lenderNotif);
                    updateNotificationBadge();
                    
                    Alert alert = new Alert(Alert.AlertType.INFORMATION);
                    alert.setTitle("Request Sent");
                    alert.setHeaderText("Borrow Request Submitted!");
                    alert.setContentText("Your request for \"" + book.getTitle() + "\" has been sent to the lender.\n\n" +
                                        "Requested Return Date: " + selectedDueDate + "\n\n" +
                                        "The lender will review your request and notify you of their decision.");
                    alert.showAndWait();
                    
                    loadCatalog();
                    showMyRequests();
                } else {
                    Alert alert = new Alert(Alert.AlertType.ERROR);
                    alert.setTitle("Error");
                    alert.setHeaderText("Failed to create request");
                    alert.setContentText("Could not submit your borrow request. Please try again.");
                    alert.showAndWait();
                }
            } else {
                Alert alert = new Alert(Alert.AlertType.WARNING);
                alert.setTitle("Invalid Date");
                alert.setHeaderText("Due date must be in the future!");
                alert.showAndWait();
            }
        }
    }

    @FXML
    private void handleRenew(Book book) {
        Dialog<LocalDate> dialog = new Dialog<>();
        dialog.setTitle("Renew Book");
        dialog.setHeaderText("Renew: " + book.getTitle());
        
        LocalDate currentDue = book.getDueDate();
        LocalDate suggestedDue = currentDue != null ? currentDue.plusDays(14) : LocalDate.now().plusDays(14);

        DatePicker newDueDate = new DatePicker(suggestedDue);
        newDueDate.setStyle("-fx-font-size: 13px; -fx-padding: 10;");
        
        javafx.scene.layout.GridPane grid = new javafx.scene.layout.GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new javafx.geometry.Insets(10));
        grid.add(new Label("Current Due Date:"), 0, 0);
        grid.add(new Label(currentDue != null ? currentDue.toString() : "N/A"), 1, 0);
        grid.add(new Label("New Due Date:"), 0, 1);
        grid.add(newDueDate, 1, 1);
        
        dialog.getDialogPane().setContent(grid);
        dialog.getDialogPane().getButtonTypes().addAll(javafx.scene.control.ButtonType.OK, javafx.scene.control.ButtonType.CANCEL);

        dialog.setResultConverter(buttonType -> 
            buttonType == javafx.scene.control.ButtonType.OK ? newDueDate.getValue() : null);

        if (dialog.showAndWait().isPresent()) {
            LocalDate selectedDueDate = dialog.getResult();
            if (selectedDueDate != null && selectedDueDate.isAfter(LocalDate.now())) {
                bookDAO.renewBook(book.getIsbn(), selectedDueDate);
                
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Success");
                alert.setHeaderText("Book Renewed!");
                alert.setContentText("New due date: " + selectedDueDate);
                alert.showAndWait();
                
                loadMyBorrowedBooks();
            } else {
                Alert alert = new Alert(Alert.AlertType.WARNING);
                alert.setTitle("Invalid Date");
                alert.setHeaderText("New due date must be in the future!");
                alert.showAndWait();
            }
        }
    }

    @FXML
    private void handleReturn(Book book) {
        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("Return Book");
        confirmAlert.setHeaderText("Return: " + book.getTitle());
        confirmAlert.setContentText("Are you sure you want to return this book?");

        if (confirmAlert.showAndWait().isPresent() && 
            confirmAlert.getResult() == javafx.scene.control.ButtonType.OK) {
            bookDAO.returnBook(book.getIsbn());
            
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Success");
            alert.setHeaderText("Book Returned!");
            alert.setContentText("Thank you for returning \"" + book.getTitle() + "\"");
            alert.showAndWait();
            
            loadMyBorrowedBooks();
            loadCatalog();
        }
    }

    private void handleCancelRequest(BorrowRequest request) {
        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("Cancel Request");
        confirmAlert.setHeaderText("Cancel Borrow Request");
        confirmAlert.setContentText("Are you sure you want to cancel this request?");

        if (confirmAlert.showAndWait().isPresent() && 
            confirmAlert.getResult() == javafx.scene.control.ButtonType.OK) {
            
            boolean cancelled = borrowRequestDAO.cancelBorrowRequest(request.getRequestId());
            
            if (cancelled) {
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Success");
                alert.setHeaderText("Request Cancelled");
                alert.setContentText("Your borrow request has been cancelled.");
                alert.showAndWait();
                
                loadMyRequests();
            } else {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Error");
                alert.setHeaderText("Could not cancel request");
                alert.setContentText("The request may have already been processed.");
                alert.showAndWait();
            }
        }
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

    /**
     * Checks for overdue books in the borrowed list and displays the warning banner if any are found.
     * If called from another panel, also loads borrowed books from database.
     */
    private void checkForOverdueBooks() {
        // Load borrowed books to check for overdue
        java.util.List<Book> booksToCheck = borrowedList;
        
        // If borrowedList is empty and we're not on the My Borrowed tab, load from DB
        if (booksToCheck.isEmpty() && !myBooksPanel.isVisible()) {
            booksToCheck = bookDAO.findBorrowedByUser(currentUserId);
        }
        
        java.util.List<Book> overdueBooks = new java.util.ArrayList<>();
        LocalDate today = LocalDate.now();
        
        for (Book book : booksToCheck) {
            if (book.getDueDate() != null && book.getDueDate().isBefore(today)) {
                overdueBooks.add(book);
            }
        }
        
        if (!overdueBooks.isEmpty()) {
            displayOverdueWarning(overdueBooks);
        } else {
            hideOverdueWarning();
        }
    }

    /**
     * Displays the overdue warning banner with information about overdue books.
     * Applies pulsing animation for visual "bleeding" effect on both banner and entire dashboard.
     */
    private void displayOverdueWarning(java.util.List<Book> overdueBooks) {
        overdueWarningBanner.setVisible(true);
        overdueWarningBanner.setManaged(true);
        
        // Update message with number of overdue books
        int count = overdueBooks.size();
        String title = count == 1 ? "1 Overdue Book" : count + " Overdue Books";
        String message = "⏰ Due " + (count == 1 ? "date" : "dates") + " passed. " + 
                        "Return " + (count == 1 ? "it" : "them") + " as soon as possible.";
        
        overdueWarningTitle.setText(title);
        overdueWarningMessage.setText(message);
        
        // Apply pulsing animation to both banner and dashboard
        applyOverdueAnimation();
        applyDashboardBleedingEffect();
    }

    /**
     * Hides the overdue warning banner and stops all animations.
     */
    private void hideOverdueWarning() {
        overdueWarningBanner.setVisible(false);
        overdueWarningBanner.setManaged(false);
        
        if (vignetteOverlay != null) {
            // Fade vignette back to baseline opacity instead of hiding completely
            javafx.animation.Timeline fadeOut = new javafx.animation.Timeline(
                new javafx.animation.KeyFrame(javafx.util.Duration.millis(300),
                    new javafx.animation.KeyValue(vignetteOverlay.opacityProperty(), 0.25))
            );
            fadeOut.play();
        }
        
        // Stop banner animation
        if (overdueWarningBanner.getUserData() instanceof javafx.animation.Timeline) {
            ((javafx.animation.Timeline) overdueWarningBanner.getUserData()).stop();
        }
        
        // Stop all dashboard animations
        Object userData = dashboardRoot.getUserData();
        if (userData instanceof Object[]) {
            Object[] timelines = (Object[]) userData;
            for (Object timeline : timelines) {
                if (timeline instanceof javafx.animation.Timeline) {
                    ((javafx.animation.Timeline) timeline).stop();
                }
            }
        }
        
        // Reset style
        dashboardRoot.setStyle("-fx-background-color: #f5f0fa;");
        dashboardRoot.setTranslateX(0);
        dashboardRoot.setTranslateY(0);
    }

    /**
     * Applies a pulsing animation to the warning banner to create a "bleeding" visual effect.
     * The animation continuously pulses with increased glow, scaling, and color changes.
     * Similar to the "HP low" warning effect in PUBG.
     */
    private void applyOverdueAnimation() {
        javafx.animation.Timeline timeline = new javafx.animation.Timeline(
            // Start: Normal state
            new javafx.animation.KeyFrame(
                javafx.util.Duration.millis(0),
                new javafx.animation.KeyValue(overdueWarningBanner.scaleXProperty(), 1.0),
                new javafx.animation.KeyValue(overdueWarningBanner.scaleYProperty(), 1.0),
                new javafx.animation.KeyValue(overdueWarningBanner.opacityProperty(), 1.0),
                new javafx.animation.KeyValue(overdueWarningBanner.translateXProperty(), 0)
            ),
            // 25%: Pulse - Scale up slightly with slight vibration
            new javafx.animation.KeyFrame(
                javafx.util.Duration.millis(600),
                new javafx.animation.KeyValue(overdueWarningBanner.scaleXProperty(), 1.02),
                new javafx.animation.KeyValue(overdueWarningBanner.scaleYProperty(), 1.05),
                new javafx.animation.KeyValue(overdueWarningBanner.opacityProperty(), 0.93),
                new javafx.animation.KeyValue(overdueWarningBanner.translateXProperty(), 3)
            ),
            // 50%: Peak glow - Maximum intensity
            new javafx.animation.KeyFrame(
                javafx.util.Duration.millis(1200),
                new javafx.animation.KeyValue(overdueWarningBanner.scaleXProperty(), 1.0),
                new javafx.animation.KeyValue(overdueWarningBanner.scaleYProperty(), 1.0),
                new javafx.animation.KeyValue(overdueWarningBanner.opacityProperty(), 1.0),
                new javafx.animation.KeyValue(overdueWarningBanner.translateXProperty(), -3)
            ),
            // 75%: Dim - Fade out slightly (like "pain" effect)
            new javafx.animation.KeyFrame(
                javafx.util.Duration.millis(1800),
                new javafx.animation.KeyValue(overdueWarningBanner.scaleXProperty(), 0.98),
                new javafx.animation.KeyValue(overdueWarningBanner.scaleYProperty(), 0.98),
                new javafx.animation.KeyValue(overdueWarningBanner.opacityProperty(), 0.86),
                new javafx.animation.KeyValue(overdueWarningBanner.translateXProperty(), 2)
            ),
            // Back to start for loop
            new javafx.animation.KeyFrame(
                javafx.util.Duration.millis(2400),
                new javafx.animation.KeyValue(overdueWarningBanner.scaleXProperty(), 1.0),
                new javafx.animation.KeyValue(overdueWarningBanner.scaleYProperty(), 1.0),
                new javafx.animation.KeyValue(overdueWarningBanner.opacityProperty(), 1.0),
                new javafx.animation.KeyValue(overdueWarningBanner.translateXProperty(), 0)
            )
        );
        timeline.setCycleCount(javafx.animation.Animation.INDEFINITE);
        timeline.play();
        
        // Store timeline for cleanup if needed
        overdueWarningBanner.setUserData(timeline);
    }

    /**
     * Sets up the vignette overlay with the red gradient effect.
     */
    private void setupVignetteOverlay() {
        if (vignetteOverlay == null) return;
        
        vignetteOverlay.getChildren().clear();
        vignetteOverlay.setMouseTransparent(true);
        vignetteOverlay.setPickOnBounds(false);
        
        // Create radial gradient for vignette effect (dark red on edges, transparent in center)
        javafx.scene.paint.RadialGradient gradient = new javafx.scene.paint.RadialGradient(
            0.5, 0.5,  // centerX, centerY
            0.5, 0.5,  // focusX, focusY
            0.8,       // radius
            false,     // proportional
            javafx.scene.paint.CycleMethod.NO_CYCLE,
            new javafx.scene.paint.Stop(0, javafx.scene.paint.Color.web("#00000000")),  // Transparent center
            new javafx.scene.paint.Stop(0.6, javafx.scene.paint.Color.web("#c0392bff")), // Red edges
            new javafx.scene.paint.Stop(1.0, javafx.scene.paint.Color.web("#990000ff"))  // Dark red corners
        );
        
        javafx.scene.shape.Rectangle vignetteRect = new javafx.scene.shape.Rectangle();
        vignetteRect.widthProperty().bind(vignetteOverlay.widthProperty());
        vignetteRect.heightProperty().bind(vignetteOverlay.heightProperty());
        vignetteRect.setFill(gradient);
        vignetteRect.setMouseTransparent(true);
        
        vignetteOverlay.getChildren().add(vignetteRect);
        vignetteOverlay.setOpacity(0.25);  // Subtle but visible red vignette
        vignetteOverlay.setVisible(true);  // Always visible on dashboard
    }
    
    /**
     * Creates the vignette overlay that will appear on top of content without blocking it.
     */
    private void createVignetteOverlay() {
        // This is now handled by FXML and setupVignetteOverlay()
    }

    /**
     * Applies an innovative "bleeding" effect to the entire dashboard.
     * Creates a red vignette overlay that pulsates with corner glow effects.
     * Multiple layers of animation create a sophisticated PUBG-like warning effect.
     */
    private void applyDashboardBleedingEffect() {
        // Show the vignette overlay if it exists
        if (vignetteOverlay != null) {
            vignetteOverlay.toFront();
            
            // === ANIMATION 1: Vignette Opacity Pulsing ===
            javafx.animation.Timeline vignetteTimeline = new javafx.animation.Timeline(
                new javafx.animation.KeyFrame(javafx.util.Duration.millis(0),
                    new javafx.animation.KeyValue(vignetteOverlay.opacityProperty(), 0.25)),
                new javafx.animation.KeyFrame(javafx.util.Duration.millis(500),
                    new javafx.animation.KeyValue(vignetteOverlay.opacityProperty(), 0.35)),
                new javafx.animation.KeyFrame(javafx.util.Duration.millis(1000),
                    new javafx.animation.KeyValue(vignetteOverlay.opacityProperty(), 0.55)),
                new javafx.animation.KeyFrame(javafx.util.Duration.millis(1500),
                    new javafx.animation.KeyValue(vignetteOverlay.opacityProperty(), 0.35)),
                new javafx.animation.KeyFrame(javafx.util.Duration.millis(2000),
                    new javafx.animation.KeyValue(vignetteOverlay.opacityProperty(), 0.25))
            );
            vignetteTimeline.setCycleCount(javafx.animation.Animation.INDEFINITE);
            vignetteTimeline.play();
            
            // Store for later cleanup
            if (dashboardRoot.getUserData() == null) {
                dashboardRoot.setUserData(new Object[]{vignetteTimeline, null});
            }
        }
        
        // === ANIMATION 2: Background color subtle shift ===
        javafx.animation.Timeline colorTimeline = new javafx.animation.Timeline(
            new javafx.animation.KeyFrame(javafx.util.Duration.millis(0),
                new javafx.animation.KeyValue(dashboardRoot.styleProperty(),
                    "-fx-background-color: #f5f0fa;")),
            new javafx.animation.KeyFrame(javafx.util.Duration.millis(600),
                new javafx.animation.KeyValue(dashboardRoot.styleProperty(),
                    "-fx-background-color: #f5eded;")),
            new javafx.animation.KeyFrame(javafx.util.Duration.millis(1200),
                new javafx.animation.KeyValue(dashboardRoot.styleProperty(),
                    "-fx-background-color: #fae6e0;")),
            new javafx.animation.KeyFrame(javafx.util.Duration.millis(1800),
                new javafx.animation.KeyValue(dashboardRoot.styleProperty(),
                    "-fx-background-color: #f5eded;")),
            new javafx.animation.KeyFrame(javafx.util.Duration.millis(2400),
                new javafx.animation.KeyValue(dashboardRoot.styleProperty(),
                    "-fx-background-color: #f5f0fa;"))
        );
        colorTimeline.setCycleCount(javafx.animation.Animation.INDEFINITE);
        colorTimeline.play();
        
        // Store timelines for cleanup
        Object[] timelines = (dashboardRoot.getUserData() instanceof Object[]) ? 
            (Object[]) dashboardRoot.getUserData() : new Object[2];
        timelines[0] = (timelines[0] == null) ? null : timelines[0];  // vignette timeline
        timelines[1] = colorTimeline;
        dashboardRoot.setUserData(timelines);
    }
    
    private javafx.scene.layout.Region region() {
        return dashboardRoot;
    }

    /**
     * Called when the dismiss button (✕) on the warning banner is clicked.
     * Hides the warning banner but keeps it available for re-display if overdue books still exist.
     */
    @FXML
    public void dismissOverdueWarning() {
        overdueWarningBanner.setVisible(false);
        overdueWarningBanner.setManaged(false);
    }

    /**
     * TEST METHOD - Simulates overdue books for testing the warning system.
     * Press Ctrl+Shift+O in the "My Borrowed" table to trigger this.
     * This adds temporary test books with overdue dates to demonstrate the warning.
     */
    private void testOverdueWarning() {
        // Create test books with overdue dates
        Book testBook1 = new Book("TEST-ISBN-001", "Test Overdue Book 1", "Test Author", "Fiction", 
                                  currentUserId, false, currentUserId, 
                                  LocalDate.now().minusDays(10), LocalDate.now().minusDays(5), true);
        
        Book testBook2 = new Book("TEST-ISBN-002", "Test Overdue Book 2", "Another Author", "Mystery", 
                                  currentUserId, false, currentUserId, 
                                  LocalDate.now().minusDays(8), LocalDate.now().minusDays(2), true);
        
        // Add test books to the display list
        borrowedList.add(testBook1);
        borrowedList.add(testBook2);
        
        // Trigger the overdue check
        checkForOverdueBooks();
        
        // Show a tooltip
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Test Mode");
        alert.setHeaderText("Overdue Warning Test Activated");
        alert.setContentText("Two test overdue books have been added. You should see the red warning banner above.\n\n" +
                           "Press Ctrl+Shift+R to remove test books.");
        alert.showAndWait();
    }

    /**
     * TEST METHOD - Removes test overdue books and reloads the real data.
     * Press Ctrl+Shift+R in the "My Borrowed" table to trigger this.
     */
    private void removeTestOverdueBooks() {
        // Reload the actual borrowed books from database
        loadMyBorrowedBooks();
        
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Test Mode");
        alert.setHeaderText("Test Books Removed");
        alert.setContentText("Test books have been removed and real data has been reloaded.");
        alert.showAndWait();
    }
}
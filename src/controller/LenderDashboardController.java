package controller;

import database.BookDAO;
import database.BorrowRequestDAO;
import database.NotificationDAO;
import database.SupaBookDAO;
import database.SupaBorrowRequestDAO;
import database.SupaNotificationDAO;
import database.SupaUserDAO;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import model.Book;
import model.BorrowRequest;
import model.Notification;
import model.User;

public class LenderDashboardController extends BaseDashboardController {

    @FXML private TextField isbnField;
    @FXML private TextField titleField;
    @FXML private TextField authorField;
    @FXML private TextField genreField;
    @FXML private Label statusLabel;

    @FXML private Button btnAddBook;
    @FXML private Button btnInventory;
    @FXML private Button btnPendingRequests;

    @FXML private Button btnLentBooks;

    // Notification badge
    @FXML private Label notificationBell;
    @FXML private Label notificationBadge;
    @FXML private VBox notificationPanel;
    @FXML private VBox notificationListContainer;

    @FXML private VBox addBookPanel;
    @FXML private VBox inventoryPanel;
    @FXML private VBox pendingRequestsPanel;

    @FXML private VBox lentBooksPanel;

    @FXML private TableView<Book> bookTable;
    @FXML private TableColumn<Book, String> isbnColumn;
    @FXML private TableColumn<Book, String> titleColumn;
    @FXML private TableColumn<Book, String> authorColumn;
    @FXML private TableColumn<Book, String> genreColumn;
    @FXML private TableColumn<Book, String> statusColumn;
    @FXML private Button editBookBtn;
    @FXML private Button toggleAvailBtn;
    @FXML private Button deleteBookBtn;
    @FXML private Button delistBookBtn;
    @FXML private Label inventoryStatusLabel;

    @FXML private TextField inventorySearchField;

    // Pending Requests
    @FXML private TableView<BorrowRequest> pendingRequestsTable;
    @FXML private TableColumn<BorrowRequest, String> reqBorrowerColumn;
    @FXML private TableColumn<BorrowRequest, String> reqBookColumn;
    @FXML private TableColumn<BorrowRequest, String> reqDueDateColumn;
    @FXML private TableColumn<BorrowRequest, String> reqDateColumn;
    @FXML private Button acceptRequestBtn;
    @FXML private Button rejectRequestBtn;

    // Lent Books Table
    @FXML private TableView<Book> lentBooksTable;
    @FXML private TableColumn<Book, String> lentIsbnColumn;
    @FXML private TableColumn<Book, String> lentTitleColumn;
    @FXML private TableColumn<Book, String> lentBorrowerColumn;
    @FXML private TableColumn<Book, String> lentBorrowDateColumn;
    @FXML private TableColumn<Book, String> lentDueDateColumn;
    @FXML private Label lentBooksStatusLabel;
    @FXML private Label pendingRequestsStatusLabel;

    private final BookDAO bookDAO = new SupaBookDAO();
    private final BorrowRequestDAO borrowRequestDAO = new SupaBorrowRequestDAO();
    private final NotificationDAO notificationDAO = new SupaNotificationDAO();
    private final ObservableList<Book> bookList = FXCollections.observableArrayList();
    private final ObservableList<BorrowRequest> pendingRequestsList = FXCollections.observableArrayList();

    private final ObservableList<Book> lentBooksList = FXCollections.observableArrayList();

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
        delistBookBtn.setOnAction(e -> {
            Book selected = bookTable.getSelectionModel().getSelectedItem();
            if (selected != null) handleDelistBook(selected);
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
            delistBookBtn.setDisable(!hasSelection);

            // Update toggle button text based on current status
            if (hasSelection) {
                toggleAvailBtn.setText(newVal.isAvailable() ? "⚠️ Mark Unavailable" : "✓ Mark Available");
                delistBookBtn.setText(newVal.isListed() ? "Hide from Catalog" : "Show in Catalog");
            }
        });

        bookTable.setItems(bookList);
        loadBooks();

            // Inventory search bar logic
            if (inventorySearchField != null) {
                inventorySearchField.textProperty().addListener((obs, oldVal, newVal) -> {
                    filterInventoryBooks(newVal);
                });
            }
        
        // Setup Pending Requests table columns
        reqBorrowerColumn.setCellValueFactory(data -> {
            // Get borrower name from userId
            return new javafx.beans.property.SimpleStringProperty("User #" + data.getValue().getBorrowerId());
        });
        reqBookColumn.setCellValueFactory(data -> {
            Book book = cachedBooks.getOrDefault(data.getValue().getBookIsbn(), null);
            if (book == null) {
                book = bookDAO.findByIsbn(data.getValue().getBookIsbn());
                if (book != null) cachedBooks.put(data.getValue().getBookIsbn(), book);
            }
            return new javafx.beans.property.SimpleStringProperty(book != null ? book.getTitle() : "N/A");
        });
        reqDueDateColumn.setCellValueFactory(data ->
            new javafx.beans.property.SimpleStringProperty(
                data.getValue().getRequestedDueDate() != null ? data.getValue().getRequestedDueDate().toString() : "N/A"));
        reqDateColumn.setCellValueFactory(data ->
            new javafx.beans.property.SimpleStringProperty(
                data.getValue().getCreatedAt() != null ? data.getValue().getCreatedAt().toLocalDate().toString() : "N/A"));

        pendingRequestsTable.setItems(pendingRequestsList);

        // Setup pending requests button handlers
        acceptRequestBtn.setOnAction(e -> {
            BorrowRequest selected = pendingRequestsTable.getSelectionModel().getSelectedItem();
            if (selected != null) handleAcceptRequest(selected);
        });
        rejectRequestBtn.setOnAction(e -> {
            BorrowRequest selected = pendingRequestsTable.getSelectionModel().getSelectedItem();
            if (selected != null) handleRejectRequest(selected);
        });

        // Enable/disable buttons based on table selection
        pendingRequestsTable.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            boolean hasSelection = newVal != null && "PENDING".equals(newVal.getStatus());
            acceptRequestBtn.setDisable(!hasSelection);
            rejectRequestBtn.setDisable(!hasSelection);
        });
        
        // Update notification badge
        updateNotificationBadge();
        
        showAddBookPanel();

        // Lent Books Table setup
        if (lentIsbnColumn != null) {
            lentIsbnColumn.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().getIsbn()));
            lentTitleColumn.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().getTitle()));
            lentBorrowerColumn.setCellValueFactory(data -> {
                Integer borrowerId = data.getValue().getBorrowedById();
                if (borrowerId == null) return new javafx.beans.property.SimpleStringProperty("");
                User borrower = new SupaUserDAO().getAllUsers().stream().filter(u -> u.getId() == borrowerId).findFirst().orElse(null);
                return new javafx.beans.property.SimpleStringProperty(borrower != null ? borrower.getUsername() : "User #" + borrowerId);
            });
            lentBorrowDateColumn.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(
                data.getValue().getBorrowDate() != null ? data.getValue().getBorrowDate().toString() : ""
            ));
            lentDueDateColumn.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(
                data.getValue().getDueDate() != null ? data.getValue().getDueDate().toString() : ""
            ));
            lentBooksTable.setItems(lentBooksList);
        }
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
            // Show pending requests
            showPendingRequestsPanel();
            btnPendingRequests.setStyle("-fx-background-color: rgba(0,0,0,0.2); " +
                    "-fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 13px; " +
                    "-fx-padding: 14 20; -fx-cursor: hand; -fx-background-radius: 0; " +
                    "-fx-border-color: transparent transparent transparent white; " +
                    "-fx-border-width: 0 0 0 3; -fx-alignment: CENTER_LEFT;");
            btnAddBook.setStyle("-fx-background-color: transparent; -fx-text-fill: rgba(255,255,255,0.7); " +
                    "-fx-font-size: 13px; -fx-padding: 14 20; -fx-cursor: hand; -fx-background-radius: 0; -fx-alignment: CENTER_LEFT;");
            btnInventory.setStyle("-fx-background-color: transparent; -fx-text-fill: rgba(255,255,255,0.7); " +
                    "-fx-font-size: 13px; -fx-padding: 14 20; -fx-cursor: hand; -fx-background-radius: 0; -fx-alignment: CENTER_LEFT;");
        }

        // Close the panel
        notificationPanel.setVisible(false);
        notificationPanel.setManaged(false);

        // Update badge
        updateNotificationBadge();
    }

    private String getTimeAgo(java.time.LocalDateTime dateTime) {
        if (dateTime == null) return "";
        java.time.LocalDateTime now = java.time.LocalDateTime.now();
        long minutes = java.time.temporal.ChronoUnit.MINUTES.between(dateTime, now);
        long hours = java.time.temporal.ChronoUnit.HOURS.between(dateTime, now);
        long days = java.time.temporal.ChronoUnit.DAYS.between(dateTime, now);

        if (minutes < 1) return "Just now";
        if (minutes < 60) return minutes + "m ago";
        if (hours < 24) return hours + "h ago";
        if (days == 1) return "Yesterday";
        return days + "d ago";
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
        pendingRequestsPanel.setVisible(false);
        pendingRequestsPanel.setManaged(false);
        if (lentBooksPanel != null) {
            lentBooksPanel.setVisible(false);
            lentBooksPanel.setManaged(false);
        }
        setActiveButton(btnAddBook);
        setInactiveButton(btnInventory);
        setInactiveButton(btnPendingRequests);
        setInactiveButton(btnLentBooks);
    }

    @FXML
    public void showInventoryPanel() {
        inventoryPanel.setVisible(true);
        inventoryPanel.setManaged(true);
        addBookPanel.setVisible(false);
        addBookPanel.setManaged(false);
        pendingRequestsPanel.setVisible(false);
        pendingRequestsPanel.setManaged(false);
        if (lentBooksPanel != null) {
            lentBooksPanel.setVisible(false);
            lentBooksPanel.setManaged(false);
        }
        setActiveButton(btnInventory);
        setInactiveButton(btnAddBook);
        setInactiveButton(btnPendingRequests);
        setInactiveButton(btnLentBooks);
        loadBooks();
    }

    @FXML
    public void showPendingRequestsPanel() {
        pendingRequestsPanel.setVisible(true);
        pendingRequestsPanel.setManaged(true);
        addBookPanel.setVisible(false);
        addBookPanel.setManaged(false);
        inventoryPanel.setVisible(false);
        inventoryPanel.setManaged(false);
        if (lentBooksPanel != null) {
            lentBooksPanel.setVisible(false);
            lentBooksPanel.setManaged(false);
        }
        setActiveButton(btnPendingRequests);
        setInactiveButton(btnAddBook);
        setInactiveButton(btnInventory);
        setInactiveButton(btnLentBooks);
        loadPendingRequests();
    }

    @FXML
    public void showLentBooksPanel() {
        if (lentBooksPanel == null) return;
        lentBooksPanel.setVisible(true);
        lentBooksPanel.setManaged(true);
        addBookPanel.setVisible(false);
        addBookPanel.setManaged(false);
        inventoryPanel.setVisible(false);
        inventoryPanel.setManaged(false);
        pendingRequestsPanel.setVisible(false);
        pendingRequestsPanel.setManaged(false);
        setActiveButton(btnLentBooks);
        setInactiveButton(btnAddBook);
        setInactiveButton(btnInventory);
        setInactiveButton(btnPendingRequests);
        loadLentBooks();
    }

    private void loadLentBooks() {
        lentBooksList.clear();
        // Only show books owned by this lender, currently lent out (borrowed_by not null)
        for (Book book : bookDAO.getByOwnerId(currentUserId)) {
            if (book.getBorrowedById() != null) {
                lentBooksList.add(book);
            }
        }
        if (lentBooksList.isEmpty()) {
            if (lentBooksStatusLabel != null) {
                lentBooksStatusLabel.setText("No books are currently lent out.");
                lentBooksStatusLabel.setTextFill(Color.GRAY);
            }
        } else {
            if (lentBooksStatusLabel != null) {
                lentBooksStatusLabel.setText("");
            }
        }
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
            // Re-apply filter if search bar is not empty
            if (inventorySearchField != null && !inventorySearchField.getText().isEmpty()) {
                filterInventoryBooks(inventorySearchField.getText());
            }
        }

        private void filterInventoryBooks(String filter) {
            if (filter == null || filter.isEmpty()) {
                bookTable.setItems(bookList);
                return;
            }
            String lower = filter.toLowerCase();
            ObservableList<Book> filtered = bookList.filtered(book ->
                (book.getTitle() != null && book.getTitle().toLowerCase().contains(lower)) ||
                (book.getAuthor() != null && book.getAuthor().toLowerCase().contains(lower)) ||
                (book.getIsbn() != null && book.getIsbn().toLowerCase().contains(lower)) ||
                (book.getGenre() != null && book.getGenre().toLowerCase().contains(lower))
            );
            bookTable.setItems(filtered);
    }

    private void loadPendingRequests() {
        // Load requests asynchronously to avoid blocking UI
        new Thread(() -> {
            java.util.List<BorrowRequest> requests = borrowRequestDAO.getPendingRequestsForLender(currentUserId);
            
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
                pendingRequestsList.clear();
                pendingRequestsList.addAll(requests);
            });
        }).start();
    }
    
    // Cache for books to avoid repeated queries
    private java.util.Map<String, Book> cachedBooks = new java.util.HashMap<>();

    private void handleAcceptRequest(BorrowRequest request) {
        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("Accept Request");
        confirmAlert.setHeaderText("Accept Borrow Request");
        
        Book book = bookDAO.findByIsbn(request.getBookIsbn());
        String bookTitle = book != null ? book.getTitle() : request.getBookIsbn();
        confirmAlert.setContentText("Accept borrow request for \"" + bookTitle + "\"?\n\n" +
                                    "Requested Return: " + request.getRequestedDueDate() + "\n\n" +
                                    "The book will be marked as borrowed and no longer available for other requests.");

        if (confirmAlert.showAndWait().isPresent() && 
            confirmAlert.getResult() == javafx.scene.control.ButtonType.OK) {
            
            boolean accepted = borrowRequestDAO.acceptBorrowRequest(request.getRequestId());
            
            if (accepted) {
                // Create notification for borrower
                Notification borrowerNotif = new Notification(
                    request.getBorrowerId(),
                    "REQUEST_ACCEPTED",
                    "Your Request Accepted: " + bookTitle,
                    "Your request to borrow \"" + bookTitle + "\" has been accepted! " +
                    "Return date: " + request.getRequestedDueDate(),
                    request.getBookIsbn(),
                    currentUserId,
                    "my-books"
                );
                notificationDAO.createNotification(borrowerNotif);
                updateNotificationBadge();
                
                pendingRequestsStatusLabel.setTextFill(Color.GREEN);
                pendingRequestsStatusLabel.setText("Request accepted! Borrower has been notified.");
                loadPendingRequests();
            } else {
                pendingRequestsStatusLabel.setTextFill(Color.RED);
                pendingRequestsStatusLabel.setText("Failed to accept request.");
            }
        }
    }

    private void handleRejectRequest(BorrowRequest request) {
        Dialog<String> dialog = new Dialog<>();
        dialog.setTitle("Reject Request");
        dialog.setHeaderText("Reject Borrow Request");

        TextArea reasonArea = new TextArea();
        reasonArea.setWrapText(true);
        reasonArea.setPrefRowCount(5);
        reasonArea.setStyle("-fx-font-size: 13px; -fx-padding: 10;");

        javafx.scene.layout.BorderPane pane = new javafx.scene.layout.BorderPane();
        pane.setTop(new Label("Reason (optional):"));
        pane.setCenter(reasonArea);

        dialog.getDialogPane().setContent(pane);
        dialog.getDialogPane().getButtonTypes().addAll(javafx.scene.control.ButtonType.OK, javafx.scene.control.ButtonType.CANCEL);

        dialog.setResultConverter(buttonType -> 
            buttonType == javafx.scene.control.ButtonType.OK ? reasonArea.getText().trim() : null);

        if (dialog.showAndWait().isPresent()) {
            String rejectionReason = dialog.getResult();
            if (rejectionReason != null) {
                boolean rejected = borrowRequestDAO.rejectBorrowRequest(request.getRequestId(), rejectionReason);
                
                if (rejected) {
                    Book book = bookDAO.findByIsbn(request.getBookIsbn());
                    String bookTitle = book != null ? book.getTitle() : request.getBookIsbn();
                    
                    // Create notification for borrower
                    Notification borrowerNotif = new Notification(
                        request.getBorrowerId(),
                        "REQUEST_REJECTED",
                        "Your Request Rejected: " + bookTitle,
                        "Your request to borrow \"" + bookTitle + "\" has been rejected.\n\n" +
                        "Reason: " + (rejectionReason.isEmpty() ? "No reason provided" : rejectionReason),
                        request.getBookIsbn(),
                        currentUserId,
                        "my-requests"
                    );
                    notificationDAO.createNotification(borrowerNotif);
                    updateNotificationBadge();
                    
                    pendingRequestsStatusLabel.setTextFill(Color.GREEN);
                    pendingRequestsStatusLabel.setText("Request rejected. Borrower has been notified.");
                    loadPendingRequests();
                } else {
                    pendingRequestsStatusLabel.setTextFill(Color.RED);
                    pendingRequestsStatusLabel.setText("Failed to reject request.");
                }
            }
        }
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
                book.getBorrowedById()
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
            newAvailability ? null : book.getBorrowedById()
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

    private void handleDelistBook(Book book) {
        boolean newListingStatus = !book.isListed();
        if (newListingStatus) {
            // Re-listing the book
            bookDAO.relistBook(book.getIsbn());
            inventoryStatusLabel.setTextFill(Color.GREEN);
            inventoryStatusLabel.setText("Book is now visible in the catalog!");
        } else {
            // Delisting the book
            bookDAO.delistBook(book.getIsbn());
            inventoryStatusLabel.setTextFill(Color.GREEN);
            inventoryStatusLabel.setText("Book hidden from catalog. Borrowers won't see it.");
        }
        loadBooks();
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
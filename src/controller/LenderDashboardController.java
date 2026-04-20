package controller;

import database.BookDAO;
import database.BorrowRequestDAO;
import database.NotificationDAO;
import database.SupaBookDAO;
import database.SupaBorrowRequestDAO;
import database.SupaNotificationDAO;
import database.SupaUserDAO;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import model.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import util.AuditLogger;
import util.OtpService;

import java.time.LocalDateTime;
import java.util.Optional;

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
    @FXML private TableColumn<Book, String> coverColumn;
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
    @FXML private TableColumn<BorrowRequest, Void> reqActionColumn;


    // Lent Books Table
    @FXML private TableView<Book> lentBooksTable;
    @FXML private TableColumn<Book, String> lentIsbnColumn;
    @FXML private TableColumn<Book, String> lentTitleColumn;
    @FXML private TableColumn<Book, String> lentBorrowerColumn;
    @FXML private TableColumn<Book, String> lentBorrowDateColumn;
    @FXML private TableColumn<Book, String> lentDueDateColumn;
    @FXML private Label lentBooksStatusLabel;
    @FXML private Label pendingRequestsStatusLabel;

    @FXML private ComboBox<String> conditionCombo;
    @FXML private TextField bookImageField;
    @FXML private Button bookImageBtn;
    private java.io.File selectedBookImage = null; // Temporarily stores the file before upload

    private final BookDAO bookDAO = new SupaBookDAO();
    private final BorrowRequestDAO borrowRequestDAO = new SupaBorrowRequestDAO();
    private final NotificationDAO notificationDAO = new SupaNotificationDAO();
    private final ObservableList<Book> bookList = FXCollections.observableArrayList();
    private final ObservableList<BorrowRequest> pendingRequestsList = FXCollections.observableArrayList();
    private final ObservableList<Book> lentBooksList = FXCollections.observableArrayList();
    private java.util.Map<String, Book> cachedBooks = new java.util.HashMap<>();

    @FXML
    public void initialize() {
        // --- 1. INVENTORY TABLE SETUP ---
        isbnColumn.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().getIsbn()));
        titleColumn.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().getTitle()));
        authorColumn.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().getAuthor()));
        genreColumn.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().getGenre()));
        statusColumn.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().isAvailable() ? "Available" : "Unavailable"));
        // Add this near the top of initialize()
        if (conditionCombo != null) {
            conditionCombo.getItems().addAll("Like New", "Very Good", "Good", "Acceptable");
            conditionCombo.setValue("Good"); // Set a safe default
        }
        if (bookImageBtn != null) {
            bookImageBtn.setOnAction(e -> openBookImageChooser());
        }
        statusColumn.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) { setText(null); setStyle(""); return; }
                setText(item);
                if (item.equals("Available")) {
                    setStyle("-fx-text-fill: #0D9488; -fx-font-weight: 700;"); // Teal
                } else {
                    setStyle("-fx-text-fill: #D97706; -fx-font-weight: 700;"); // Amber
                }
            }
        });

        coverColumn.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().getCoverUrl()));

        coverColumn.setCellFactory(col -> new TableCell<>() {
            private final ImageView imageView = new ImageView();
            @Override
            protected void updateItem(String url, boolean empty) {
                super.updateItem(url, empty);
                if (empty || url == null || url.isEmpty()) {
                    setGraphic(null);
                } else {
                    imageView.setImage(new Image(url, 40, 60, true, true));
                    setGraphic(imageView);
                    setStyle("-fx-alignment: CENTER;");
                }
            }
        });

        // Inventory Button Handlers
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

        bookTable.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            boolean hasSelection = newVal != null;
            editBookBtn.setDisable(!hasSelection);
            toggleAvailBtn.setDisable(!hasSelection);
            deleteBookBtn.setDisable(!hasSelection);
            delistBookBtn.setDisable(!hasSelection);
            if (hasSelection) {
                toggleAvailBtn.setText(newVal.isAvailable() ? "Mark Unavailable" : "Mark Available");
                delistBookBtn.setText(newVal.isListed() ? "Hide from Catalog" : "Show in Catalog");
            }
        });

        bookTable.setItems(bookList);
        loadBooks();

        if (inventorySearchField != null) {
            inventorySearchField.textProperty().addListener((obs, oldVal, newVal) -> filterInventoryBooks(newVal));
        }

        // --- 2. PENDING REQUESTS TABLE SETUP (US-1 Handover Integration) ---
        reqBorrowerColumn.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty("User #" + data.getValue().getBorrowerId()));
        reqBookColumn.setCellValueFactory(data -> {
            Book book = cachedBooks.getOrDefault(data.getValue().getBookIsbn(), null);
            if (book == null) {
                book = bookDAO.findByIsbn(data.getValue().getBookIsbn());
                if (book != null) cachedBooks.put(data.getValue().getBookIsbn(), book);
            }
            return new javafx.beans.property.SimpleStringProperty(book != null ? book.getTitle() : "N/A");
        });
        reqDueDateColumn.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().getRequestedDueDate() != null ? data.getValue().getRequestedDueDate().toString() : "N/A"));
        reqDateColumn.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().getCreatedAt() != null ? data.getValue().getCreatedAt().toLocalDate().toString() : "N/A"));

        // US-1: DYNAMIC ACTION COLUMN SETUP
        // This replaces your static accept/reject buttons with a "Smart Button"
        reqActionColumn.setCellFactory(col -> new TableCell<BorrowRequest, Void>() {
            private final Button actionBtn = new Button();
            private final Button rejectBtn = new Button("Decline");

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    BorrowRequest req = getTableView().getItems().get(getIndex());
                    String status = req.getStatus();

                    // Style standard for both
                    actionBtn.setMinWidth(110);
                    actionBtn.setStyle("-fx-font-weight: bold; -fx-text-fill: white; -fx-background-radius: 6; -fx-cursor: hand;");
                    rejectBtn.setStyle("-fx-background-color: transparent; -fx-text-fill: #EF4444; -fx-font-weight: bold; -fx-cursor: hand;");

                    HBox container = new HBox(10, actionBtn);
                    container.setAlignment(javafx.geometry.Pos.CENTER_LEFT);

                    switch (status) {
                        case "PENDING":
                            actionBtn.setText("Approve");
                            actionBtn.setStyle(actionBtn.getStyle() + "-fx-background-color: #3B82F6;");
                            actionBtn.setOnAction(e -> handleAcceptRequest(req));
                            container.getChildren().add(rejectBtn);
                            rejectBtn.setOnAction(e -> handleRejectRequest(req));
                            break;

                        case "ACCEPTED":
                            actionBtn.setText("Set Handover");
                            actionBtn.setStyle(actionBtn.getStyle() + "-fx-background-color: #8B5CF6;");
                            actionBtn.setOnAction(e -> initiateHandover(req));
                            break;

                        case "AWAITING_HANDOVER":
                            actionBtn.setText("Verify OTP");
                            actionBtn.setStyle(actionBtn.getStyle() + "-fx-background-color: #F59E0B;");
                            actionBtn.setOnAction(e -> verifyHandoverOtp(req));
                            break;

                        case "IN_TRANSIT":
                            actionBtn.setText("On Courier");
                            actionBtn.setStyle(actionBtn.getStyle() + "-fx-background-color: #64748B;");
                            actionBtn.setDisable(true);
                            break;

                        case "BORROWED":
                            actionBtn.setText("On Loan");
                            actionBtn.setStyle(actionBtn.getStyle() + "-fx-background-color: #10B981;");
                            actionBtn.setDisable(true);
                            break;

                        case "RETURN_IN_TRANSIT":
                            actionBtn.setText("Confirm Return");
                            actionBtn.setStyle(actionBtn.getStyle() + "-fx-background-color: #EC4899;"); // Pink
                            actionBtn.setDisable(false);
                            actionBtn.setOnAction(e -> handleConfirmReturn(req));
                            break;

                        default:
                            actionBtn.setText(status);
                            actionBtn.setDisable(true);
                    }
                    setGraphic(container);
                }
            }
        });

        pendingRequestsTable.setItems(pendingRequestsList);

        // --- 3. LENT BOOKS TABLE SETUP ---
        if (lentIsbnColumn != null) {
            lentIsbnColumn.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().getIsbn()));
            lentTitleColumn.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().getTitle()));
            lentBorrowerColumn.setCellValueFactory(data -> {
                Integer borrowerId = data.getValue().getBorrowedBy();
                if (borrowerId == null) return new javafx.beans.property.SimpleStringProperty("");
                User borrower = new SupaUserDAO().getAllUsers().stream().filter(u -> u.getId() == borrowerId).findFirst().orElse(null);
                return new javafx.beans.property.SimpleStringProperty(borrower != null ? borrower.getUsername() : "User #" + borrowerId);
            });
            lentBorrowDateColumn.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().getBorrowDate() != null ? data.getValue().getBorrowDate().toString() : ""));
            lentDueDateColumn.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().getDueDate() != null ? data.getValue().getDueDate().toString() : ""));
            lentBooksTable.setItems(lentBooksList);
        }

        updateNotificationBadge();
        showAddBookPanel();
    }

    private void updateNotificationBadge() {
        int unreadCount = notificationDAO.getUnreadCount(currentUserId);
        if (unreadCount > 0) {
            notificationBadge.setText(String.valueOf(unreadCount));
            notificationBadge.setVisible(true);
        } else {
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
            new Thread(() -> {
                java.util.List<Notification> notifications = notificationDAO.getUnreadNotifications(currentUserId);
                javafx.application.Platform.runLater(() -> {
                    notificationListContainer.getChildren().clear();
                    if (notifications.isEmpty()) {
                        Label emptyLabel = new Label("No new alerts.");
                        emptyLabel.setStyle("-fx-text-fill: #9CA3AF; -fx-font-size: 13px; -fx-padding: 16;");
                        notificationListContainer.getChildren().add(emptyLabel);
                        return;
                    }
                    for (Notification notif : notifications) {
                        notificationListContainer.getChildren().add(createNotificationItem(notif));
                    }
                });
            }).start();
        }
    }

    private VBox createNotificationItem(Notification notif) {
        VBox item = new VBox(6);
        item.setStyle("-fx-border-color: transparent transparent #F3F4F6 transparent; -fx-border-width: 1; -fx-padding: 16; -fx-cursor: hand;");
        item.setOnMouseEntered(e -> item.setStyle("-fx-background-color: #F9FAFB; -fx-border-color: transparent transparent #F3F4F6 transparent; -fx-border-width: 1; -fx-padding: 16; -fx-cursor: hand;"));
        item.setOnMouseExited(e -> item.setStyle("-fx-background-color: transparent; -fx-border-color: transparent transparent #F3F4F6 transparent; -fx-border-width: 1; -fx-padding: 16; -fx-cursor: hand;"));
        item.setOnMouseClicked(e -> handleNotificationClick(notif));

        Label titleLabel = new Label(notif.getTitle());
        titleLabel.setStyle("-fx-font-weight: 700; -fx-font-size: 13px; -fx-text-fill: #111827;");
        titleLabel.setWrapText(true);

        Label messageLabel = new Label(notif.getMessage());
        messageLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #6B7280; -fx-wrap-text: true;");
        messageLabel.setWrapText(true);

        Label timeLabel = new Label(getTimeAgo(notif.getCreatedAt()));
        timeLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #9CA3AF; -fx-font-weight: 600;");

        item.getChildren().addAll(titleLabel, messageLabel, timeLabel);
        return item;
    }

    private void handleNotificationClick(Notification notif) {
        notificationDAO.markAsRead(notif.getNotificationId());
        if ("BORROW_REQUEST".equals(notif.getType())) {
            showPendingRequestsPanel();
        }
        notificationPanel.setVisible(false);
        notificationPanel.setManaged(false);
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

    @FXML
    private void switchToBorrower() {
        navigateTo("/view/BorrowerDashboard.fxml", "Library Nexus - Member Portal", (Object controller) -> {
            ((BaseDashboardController) controller).setUsername(currentUsername, currentUserId);
        });
    }

    @FXML
    public void showAddBookPanel() {
        addBookPanel.setVisible(true); addBookPanel.setManaged(true);
        inventoryPanel.setVisible(false); inventoryPanel.setManaged(false);
        pendingRequestsPanel.setVisible(false); pendingRequestsPanel.setManaged(false);
        if (lentBooksPanel != null) { lentBooksPanel.setVisible(false); lentBooksPanel.setManaged(false); }
        setActiveButton(btnAddBook);
        setInactiveButton(btnInventory); setInactiveButton(btnPendingRequests); setInactiveButton(btnLentBooks);

    }

    @FXML
    public void showInventoryPanel() {
        inventoryPanel.setVisible(true); inventoryPanel.setManaged(true);
        addBookPanel.setVisible(false); addBookPanel.setManaged(false);
        pendingRequestsPanel.setVisible(false); pendingRequestsPanel.setManaged(false);
        if (lentBooksPanel != null) { lentBooksPanel.setVisible(false); lentBooksPanel.setManaged(false); }
        setActiveButton(btnInventory);
        setInactiveButton(btnAddBook); setInactiveButton(btnPendingRequests); setInactiveButton(btnLentBooks);
        loadBooks();
    }

    @FXML
    public void showPendingRequestsPanel() {
        pendingRequestsPanel.setVisible(true); pendingRequestsPanel.setManaged(true);
        addBookPanel.setVisible(false); addBookPanel.setManaged(false);
        inventoryPanel.setVisible(false); inventoryPanel.setManaged(false);
        if (lentBooksPanel != null) { lentBooksPanel.setVisible(false); lentBooksPanel.setManaged(false); }
        setActiveButton(btnPendingRequests);
        setInactiveButton(btnAddBook); setInactiveButton(btnInventory); setInactiveButton(btnLentBooks);
        loadPendingRequests();
    }

    @FXML
    public void showLentBooksPanel() {
        if (lentBooksPanel == null) return;
        lentBooksPanel.setVisible(true); lentBooksPanel.setManaged(true);
        addBookPanel.setVisible(false); addBookPanel.setManaged(false);
        inventoryPanel.setVisible(false); inventoryPanel.setManaged(false);
        pendingRequestsPanel.setVisible(false); pendingRequestsPanel.setManaged(false);
        setActiveButton(btnLentBooks);
        setInactiveButton(btnAddBook); setInactiveButton(btnInventory); setInactiveButton(btnPendingRequests);
        loadLentBooks();
    }

    private void loadLentBooks() {
        lentBooksList.clear();
        for (Book book : bookDAO.getByOwnerId(currentUserId)) {
            if (book.getBorrowedBy() != null) {
                lentBooksList.add(book);
            }
        }
        if (lentBooksList.isEmpty()) {
            if (lentBooksStatusLabel != null) {
                lentBooksStatusLabel.setText("No active loans at this time.");
                lentBooksStatusLabel.setTextFill(Color.web("#9CA3AF"));
            }
        } else {
            if (lentBooksStatusLabel != null) lentBooksStatusLabel.setText("");
        }
    }

    @FXML
    private void handleAddBook() {
        String isbn   = isbnField.getText().trim();
        String title  = titleField.getText().trim();
        String author = authorField.getText().trim();
        String genre  = genreField.getText().trim();
        String condition = (conditionCombo != null && conditionCombo.getValue() != null)
                ? conditionCombo.getValue() : "Good";

        if (isbn.isEmpty() || title.isEmpty() || author.isEmpty() || genre.isEmpty()) {
            statusLabel.setTextFill(Color.web("#EF4444")); // Red
            statusLabel.setText("ISBN, Title, Author, and Genre are required.");
            return;
        }

        // Change status to let user know it's working
        statusLabel.setTextFill(Color.web("#F59E0B")); // Amber/Warning color
        statusLabel.setText("Publishing asset... please wait.");

        // Disable button so they don't click it twice while uploading
        btnAddBook.setDisable(true);

        // Run network/DB tasks in the background to prevent freezing the UI
        new Thread(() -> {
            String imageUrl = null;

            // 1. Upload Image if they selected one
            if (selectedBookImage != null) {
                imageUrl = util.ImageUploadService.upload(selectedBookImage);
            }

            // 2. Create the Book Object
            // NOTE: Make sure your Book.java model has a constructor that accepts condition and coverUrl!
            Book book = new Book(isbn, title, author, genre, currentUserId, true, null, null, null, true, condition, imageUrl, 0);

            // 3. Save to Database
            boolean success = bookDAO.insert(book);

            // 4. Update the UI back on the main thread
            javafx.application.Platform.runLater(() -> {
                btnAddBook.setDisable(false);

                if (success) {
                    bookList.add(book);

                    // Clear fields
                    isbnField.clear(); titleField.clear(); authorField.clear(); genreField.clear();
                    if (conditionCombo != null) conditionCombo.setValue("Good");
                    bookImageField.clear();
                    selectedBookImage = null;

                    statusLabel.setTextFill(Color.web("#10B981")); // Green
                    statusLabel.setText("Asset published successfully!");
                } else {
                    statusLabel.setTextFill(Color.web("#EF4444")); // Red
                    statusLabel.setText("Database error: Could not publish asset.");
                }
            });
        }).start();
    }

    private void loadBooks() {
        bookList.clear();
        bookList.addAll(bookDAO.getByOwnerId(currentUserId));
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
        new Thread(() -> {
            // --- THE FIX: USE getActiveRequestsFor ---
            SupaBorrowRequestDAO dao = (SupaBorrowRequestDAO) borrowRequestDAO;
            java.util.List<BorrowRequest> requests = dao.getActiveRequestsFor(currentUserId);

            // Filter out requests where the current user is the borrower (we only want lender requests here)
            requests.removeIf(r -> r.getLenderId() != currentUserId);

            java.util.Map<String, Book> bookCache = new java.util.HashMap<>();
            for (BorrowRequest req : requests) {
                if (!bookCache.containsKey(req.getBookIsbn())) {
                    Book book = bookDAO.findByIsbn(req.getBookIsbn());
                    if (book != null) bookCache.put(req.getBookIsbn(), book);
                }
            }
            cachedBooks = bookCache;
            javafx.application.Platform.runLater(() -> {
                pendingRequestsList.clear();
                pendingRequestsList.addAll(requests);
            });
        }).start();
    }

    private void handleAcceptRequest(BorrowRequest request) {
        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("Approve Request");
        confirmAlert.setHeaderText("Approve Borrow Request");
        Book book = bookDAO.findByIsbn(request.getBookIsbn());
        String bookTitle = book != null ? book.getTitle() : request.getBookIsbn();
        confirmAlert.setContentText("Approve request for \"" + bookTitle + "\"?\n\nReturn Date: " + request.getRequestedDueDate());

        if (confirmAlert.showAndWait().isPresent() && confirmAlert.getResult() == javafx.scene.control.ButtonType.OK) {
            boolean accepted = borrowRequestDAO.acceptBorrowRequest(request.getRequestId());

            if (accepted) {
                // 1. Notify the Borrower
                Notification borrowerNotif = new Notification(
                        request.getBorrowerId(), "REQUEST_ACCEPTED", "Request Approved: " + bookTitle,
                        "Your request to borrow \"" + bookTitle + "\" was approved! Return date: " + request.getRequestedDueDate(),
                        request.getBookIsbn(), currentUserId, "my-requests"
                );
                notificationDAO.createNotification(borrowerNotif);
                updateNotificationBadge();

                if (pendingRequestsStatusLabel != null) {
                    pendingRequestsStatusLabel.setTextFill(Color.web("#10B981"));
                    pendingRequestsStatusLabel.setText("Approved. Member notified.");
                }

                // 2. STOP HERE and Refresh the Table
                // The Action Column button will automatically change from "Approve" to "Set Handover".
                loadPendingRequests();

            } else {
                Alert errorAlert = new Alert(Alert.AlertType.ERROR);
                errorAlert.setHeaderText("System Error");
                errorAlert.setContentText("Unable to process approval. Please try again.");
                errorAlert.showAndWait();
            }
        }
    }

    private void handleRejectRequest(BorrowRequest request) {
        Dialog<String> dialog = new Dialog<>();
        dialog.setTitle("Decline Request");
        dialog.setHeaderText("Decline Request");
        TextArea reasonArea = new TextArea();
        reasonArea.setWrapText(true); reasonArea.setPrefRowCount(4); reasonArea.setStyle("-fx-font-size: 13px; -fx-padding: 10;");
        javafx.scene.layout.BorderPane pane = new javafx.scene.layout.BorderPane();
        pane.setTop(new Label("Reason for declining (optional):"));
        pane.setCenter(reasonArea);
        dialog.getDialogPane().setContent(pane);
        dialog.getDialogPane().getButtonTypes().addAll(javafx.scene.control.ButtonType.OK, javafx.scene.control.ButtonType.CANCEL);
        dialog.setResultConverter(buttonType -> buttonType == javafx.scene.control.ButtonType.OK ? reasonArea.getText().trim() : null);

        if (dialog.showAndWait().isPresent()) {
            String rejectionReason = dialog.getResult();
            if (rejectionReason != null) {
                if (borrowRequestDAO.rejectBorrowRequest(request.getRequestId(), rejectionReason)) {
                    Book book = bookDAO.findByIsbn(request.getBookIsbn());
                    String bookTitle = book != null ? book.getTitle() : request.getBookIsbn();
                    Notification borrowerNotif = new Notification(
                            request.getBorrowerId(), "REQUEST_REJECTED", "Request Declined: " + bookTitle,
                            "Your request to borrow \"" + bookTitle + "\" was declined.\nReason: " + (rejectionReason.isEmpty() ? "N/A" : rejectionReason),
                            request.getBookIsbn(), currentUserId, "my-requests"
                    );
                    notificationDAO.createNotification(borrowerNotif);
                    pendingRequestsStatusLabel.setTextFill(Color.web("#EF4444"));
                    pendingRequestsStatusLabel.setText("Request declined.");
                    loadPendingRequests();
                }
            }
        }
    }

    private void handleEditBook(Book book) {
        Dialog<Object> dialog = new Dialog<>();
        dialog.setTitle("Edit Asset Details");
        dialog.setHeaderText("Updating: " + book.getTitle());

        TextField editIsbnField = new TextField(book.getIsbn());
        TextField editTitleField = new TextField(book.getTitle());
        TextField editAuthorField = new TextField(book.getAuthor());
        TextField editGenreField = new TextField(book.getGenre());
        editIsbnField.setDisable(true); editIsbnField.setStyle("-fx-opacity: 0.6;");

        javafx.scene.layout.GridPane grid = new javafx.scene.layout.GridPane();
        grid.setHgap(10); grid.setVgap(10); grid.setPadding(new javafx.geometry.Insets(16));
        grid.add(new Label("ISBN:"), 0, 0); grid.add(editIsbnField, 1, 0);
        grid.add(new Label("Title:"), 0, 1); grid.add(editTitleField, 1, 1);
        grid.add(new Label("Author:"), 0, 2); grid.add(editAuthorField, 1, 2);
        grid.add(new Label("Genre:"), 0, 3); grid.add(editGenreField, 1, 3);

        dialog.getDialogPane().setContent(grid);
        dialog.getDialogPane().getButtonTypes().addAll(javafx.scene.control.ButtonType.OK, javafx.scene.control.ButtonType.CANCEL);

        if (dialog.showAndWait().orElse(javafx.scene.control.ButtonType.CANCEL) == javafx.scene.control.ButtonType.OK) {
            Book updatedBook = new Book(book.getIsbn(), editTitleField.getText().trim(), editAuthorField.getText().trim(), editGenreField.getText().trim(), book.getOwnerId(), book.isAvailable(), book.getBorrowedBy());
            if (bookDAO.updateBook(updatedBook)) {
                inventoryStatusLabel.setTextFill(Color.web("#10B981"));
                inventoryStatusLabel.setText("Details updated successfully.");
                loadBooks();
            } else {
                inventoryStatusLabel.setTextFill(Color.web("#EF4444"));
                inventoryStatusLabel.setText("Update failed.");
            }
        }
    }

    private void handleToggleAvailability(Book book) {
        boolean newAvailability = !book.isAvailable();
        Book updatedBook = new Book(book.getIsbn(), book.getTitle(), book.getAuthor(), book.getGenre(), book.getOwnerId(), newAvailability, newAvailability ? null : book.getBorrowedBy());
        if (bookDAO.updateBook(updatedBook)) {
            inventoryStatusLabel.setTextFill(Color.web("#10B981"));
            inventoryStatusLabel.setText("Status updated to: " + (newAvailability ? "Available" : "Unavailable"));
            loadBooks();
        }
    }

    private void handleDelistBook(Book book) {
        if (!book.isListed()) {
            bookDAO.relistBook(book.getIsbn());
            inventoryStatusLabel.setTextFill(Color.web("#10B981"));
            inventoryStatusLabel.setText("Asset is now visible in the public catalog.");
        } else {
            bookDAO.delistBook(book.getIsbn());
            inventoryStatusLabel.setTextFill(Color.web("#D97706"));
            inventoryStatusLabel.setText("Asset hidden from public catalog.");
        }
        loadBooks();
    }

    private void handleDeleteBook(Book book) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setHeaderText("Delete Asset");
        alert.setContentText("Permanently remove \"" + book.getTitle() + "\" from your inventory?");
        if (alert.showAndWait().isPresent() && alert.getResult() == javafx.scene.control.ButtonType.OK) {
            if (bookDAO.deleteBook(book.getIsbn())) {
                inventoryStatusLabel.setTextFill(Color.web("#10B981"));
                inventoryStatusLabel.setText("Asset deleted.");
                loadBooks();
            }
        }
    }

    private void initiateHandover(BorrowRequest request) {
        Optional<HandoverMethodDialogController.HandoverMethod> methodOpt = HandoverMethodDialogController.show();

        if (methodOpt.isPresent()) {
            HandoverMethodDialogController.HandoverMethod method = methodOpt.get();
            SupaBorrowRequestDAO dao = (SupaBorrowRequestDAO) borrowRequestDAO;

            if (method == HandoverMethodDialogController.HandoverMethod.MEETUP) {
                Optional<MeetupDetailsDialogController.MeetupDetails> meetupOpt = MeetupDetailsDialogController.show();
                if (meetupOpt.isPresent()) {
                    MeetupDetailsDialogController.MeetupDetails details = meetupOpt.get();

                    // 1. Update the database with location/time. This generates the OTP in the backend.
                    if (dao.initiateMeetupHandover(request.getRequestId(), details.location, details.dateTime)) {
                        // 2. Refresh the table.
                        // DO NOT call the OTP dialog here! The button in the table will change to "Verify OTP"
                        loadPendingRequests();
                    }
                }
            } else {
                Optional<CourierFormDialogController.CourierDetails> courierOpt = CourierFormDialogController.show();
                if (courierOpt.isPresent()) {
                    CourierFormDialogController.CourierDetails details = courierOpt.get();

                    HandoverDetails handover = new HandoverDetails();
                    handover.setMethod(HandoverDetails.Method.COURIER);
                    handover.setCourierService(details.service);
                    handover.setCourierPerson(details.personName);
                    handover.setVehiclePlate(details.vehiclePlate);
                    handover.setVehicleType(details.vehicleType);
                    handover.setProofImageUrl(details.proofImagePath);

                    if (dao.initiateCourierHandover(request.getRequestId(), handover)) {
                        loadPendingRequests();
                    }
                }
            }
        }
    }

    private void verifyHandoverOtp(BorrowRequest request) {
        Optional<String> enteredOtp = OtpEntryDialogController.show(null);
        if (enteredOtp.isPresent()) {
            SupaBorrowRequestDAO supaBorrowDAO = (SupaBorrowRequestDAO) borrowRequestDAO;
            // CORRECTED: Use the actual verification method in your DAO
            if (supaBorrowDAO.verifyOtpAndMarkBorrowed(request.getRequestId(), enteredOtp.get())) {
                Alert successAlert = new Alert(Alert.AlertType.INFORMATION, "Handover complete! Asset marked as BORROWED.");
                successAlert.show();
                showPendingRequestsPanel();
            } else {
                Alert errorAlert = new Alert(Alert.AlertType.ERROR, "Invalid OTP. Handover failed.");
                errorAlert.show();
            }
        }
    }

    private void handleConfirmReturn(BorrowRequest request) {
        SupaBorrowRequestDAO dao = (SupaBorrowRequestDAO) borrowRequestDAO;

        // We can check if a return OTP exists to know if it was a meetup return
        String returnOtp = (request.getHandover() != null) ? request.getHandover().getReturnOtp() : null;

        if (returnOtp != null && !returnOtp.isEmpty()) {
            // It's a MEETUP return. Ask Lender for the OTP the Borrower has.
            Optional<String> enteredOtp = OtpEntryDialogController.show(null);
            if (enteredOtp.isPresent()) {
                if (dao.confirmReturn(request.getRequestId(), enteredOtp.get())) {
                    Alert success = new Alert(Alert.AlertType.INFORMATION, "Return Verified! Book is available again.");
                    success.show();

                    // Trigger Rating (US-6)
                    triggerLenderRating(request);

                    loadPendingRequests();
                    loadBooks(); // Refresh inventory
                } else {
                    Alert error = new Alert(Alert.AlertType.ERROR, "Invalid Return OTP.");
                    error.show();
                }
            }
        } else {
            // It's a COURIER return. Just ask if they got the package.
            Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
            confirm.setHeaderText("Confirm Courier Delivery");
            confirm.setContentText("Did you safely receive the book from the courier?");

            if (confirm.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK) {
                if (dao.confirmReturn(request.getRequestId(), null)) {
                    triggerLenderRating(request);
                    loadPendingRequests();
                    loadBooks();
                }
            }
        }
    }

    // Helper to trigger ratings after successful return
    private void triggerLenderRating(BorrowRequest request) {
        Book book = bookDAO.findByIsbn(request.getBookIsbn());
        new Thread(() -> javafx.application.Platform.runLater(() -> {
            RatingDialogController.showUserRating(
                    request.getRequestId(), currentUserId, request.getBorrowerId(), "Borrower"
            );
        })).start();
    }

    // Helper to fix the "cannot resolve refreshRequestsTable" error
    private void refreshRequestsTable() {
        loadPendingRequests();
    }

    private void openBookImageChooser() {
        javafx.stage.FileChooser chooser = new javafx.stage.FileChooser();
        chooser.setTitle("Select Book Photo");
        chooser.getExtensionFilters().add(
                new javafx.stage.FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg", "*.webp")
        );

        // Open the dialog
        java.io.File file = chooser.showOpenDialog(bookImageBtn.getScene().getWindow());

        if (file != null) {
            selectedBookImage = file;
            bookImageField.setText(file.getName());
        }
    }

    private void setActiveButton(Button btn) {
        // Deep Teal Active State - Bright cyan text with a thick left border
        btn.setStyle("-fx-background-color: rgba(0,0,0,0.2); -fx-text-fill: #FFFFFF; -fx-font-weight: 700; -fx-padding: 10 16; -fx-cursor: hand; -fx-background-radius: 6; -fx-border-color: transparent transparent transparent #2DD4BF; -fx-border-width: 0 0 0 3;");
    }

    private void setInactiveButton(Button btn) {
        // Muted Teal Inactive State
        btn.setStyle("-fx-background-color: transparent; -fx-text-fill: #99F6E4; -fx-font-weight: 600; -fx-padding: 10 16; -fx-cursor: hand; -fx-background-radius: 6; -fx-border-width: 0;");
    }
}
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
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;
import javafx.scene.layout.HBox;
import javafx.scene.layout.BorderPane;
import javafx.scene.input.KeyCode;
import javafx.scene.paint.Color;
import model.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.List;

public class BorrowerDashboardController extends BaseDashboardController {

    @FXML private Button btnMyBooks;
    @FXML private Button btnMyRequests;
    @FXML private Button btnBrowse;

    @FXML private Label notificationBell;
    @FXML private Label notificationBadge;
    @FXML private VBox notificationPanel;
    @FXML private VBox notificationListContainer;

    @FXML private HBox overdueWarningBanner;
    @FXML private Label overdueWarningTitle;
    @FXML private Label overdueWarningMessage;

    @FXML private BorderPane dashboardRoot;
    @FXML private javafx.scene.layout.StackPane vignetteOverlay;

    @FXML private VBox myBooksPanel;
    @FXML private VBox myRequestsPanel;
    @FXML private VBox browseCatalogPanel;

    @FXML private TableView<Book> borrowedTableView;
    @FXML private TableColumn<Book, String> titleColumn;
    @FXML private TableColumn<Book, String> authorColumn;
    @FXML private TableColumn<Book, String> borrowDateColumn;
    @FXML private TableColumn<Book, String> dueDateColumn;
    @FXML private TableColumn<Book, String> statusColumn;

    // --- SPRINT 3: GRID VIEW UPDATES ---
    @FXML private FlowPane bookGrid;
    @FXML private TextField searchField;

    @FXML private Button renewButton;
    @FXML private Button returnButton;

    private AvailabilityPanelController availabilityController;
    private String selectedBookIsbn = null;

    @FXML private TableView<BorrowRequest> requestsTableView;
    @FXML private TableColumn<BorrowRequest, String> reqBookTitleColumn;
    @FXML private TableColumn<BorrowRequest, String> reqAuthorColumn;
    @FXML private TableColumn<BorrowRequest, String> reqStatusColumn;
    @FXML private TableColumn<BorrowRequest, String> reqDueDateColumn;
    @FXML private TableColumn<BorrowRequest, String> reqDateRequestedColumn;
    @FXML private TableColumn<BorrowRequest, Void> reqActionColumn;
    @FXML private Button cancelRequestButton;

    @FXML private VBox searchFilterContainer;
    private SearchFilterPanelController searchFilterController;

    private final BookDAO bookDAO = new SupaBookDAO();
    private final BorrowRequestDAO borrowRequestDAO = new SupaBorrowRequestDAO();
    private final NotificationDAO notificationDAO = new SupaNotificationDAO();
    private final ObservableList<Book> borrowedList = FXCollections.observableArrayList();
    private final ObservableList<BorrowRequest> requestsList = FXCollections.observableArrayList();

    private java.util.Map<String, Book> cachedBooks = new java.util.HashMap<>();

    @FXML
    public void initialize() {
        // --- 1. MY BORROWED BOOKS TABLE (The books you currently have) ---
        titleColumn.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().getTitle()));
        authorColumn.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().getAuthor()));
        borrowDateColumn.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().getBorrowDate() != null ? data.getValue().getBorrowDate().toString() : "N/A"));
        dueDateColumn.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().getDueDate() != null ? data.getValue().getDueDate().toString() : "N/A"));
        statusColumn.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(getDueStatus(data.getValue().getDueDate())));

        statusColumn.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) { setText(null); setStyle(""); return; }
                setText(item);
                if (item.equals("OVERDUE")) {
                    setStyle("-fx-text-fill: #EF4444; -fx-font-weight: 800;");
                } else if (item.equals("Due Today")) {
                    setStyle("-fx-text-fill: #F59E0B; -fx-font-weight: 700;");
                } else {
                    setStyle("-fx-text-fill: #6366F1; -fx-font-weight: 700;");
                }
            }
        });

        borrowedTableView.setItems(borrowedList);

        // --- 2. MY REQUESTS TABLE (The status of your borrow requests) ---
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
        reqStatusColumn.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().getStatus()));
        reqDueDateColumn.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().getRequestedDueDate() != null ? data.getValue().getRequestedDueDate().toString() : "N/A"));
        reqDateRequestedColumn.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().getCreatedAt() != null ? data.getValue().getCreatedAt().toLocalDate().toString() : "N/A"));

        // US-1: DYNAMIC ACTION COLUMN FOR HANDOVER
        reqActionColumn.setCellFactory(col -> new TableCell<BorrowRequest, Void>() {
            private final Button actionBtn = new Button();

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    BorrowRequest req = getTableView().getItems().get(getIndex());
                    String status = req.getStatus();

                    actionBtn.setMaxWidth(Double.MAX_VALUE);
                    actionBtn.setStyle("-fx-font-weight: bold; -fx-text-fill: white; -fx-background-radius: 6; -fx-cursor: hand;");

                    if ("IN_TRANSIT".equals(status)) {
                        actionBtn.setText("Confirm Receipt");
                        actionBtn.setStyle(actionBtn.getStyle() + "-fx-background-color: #10B981;");
                        actionBtn.setOnAction(e -> handleConfirmReceipt(req));
                        setGraphic(actionBtn);
                    } else if ("AWAITING_HANDOVER".equals(status)) {
                        actionBtn.setText("Show OTP");
                        actionBtn.setStyle(actionBtn.getStyle() + "-fx-background-color: #6366F1;");
                        actionBtn.setOnAction(e -> showMeetupDetails(req));
                        setGraphic(actionBtn);
                    } else if ("PENDING".equals(status)) {
                        setGraphic(null);
                    } else {
                        setGraphic(null);
                    }
                }
            }
        });

        requestsTableView.setItems(requestsList);

        // --- 3. EVENT HANDLERS & NAVIGATION ---
        borrowedTableView.setOnKeyPressed(event -> {
            if (event.isControlDown() && event.isShiftDown()) {
                if (event.getCode() == KeyCode.O) { testOverdueWarning(); event.consume(); }
                else if (event.getCode() == KeyCode.R) { removeTestOverdueBooks(); event.consume(); }
            }
        });

        setupVignetteOverlay();

        renewButton.setOnAction(e -> {
            Book selected = borrowedTableView.getSelectionModel().getSelectedItem();
            if (selected != null) handleRenew(selected);
        });

        returnButton.setOnAction(e -> {
            Book selected = borrowedTableView.getSelectionModel().getSelectedItem();
            if (selected != null) handleReturn(selected);
        });

        borrowedTableView.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            boolean hasSelection = newVal != null;
            renewButton.setDisable(!hasSelection);
            returnButton.setDisable(!hasSelection);
        });

        cancelRequestButton.setOnAction(e -> {
            BorrowRequest selected = requestsTableView.getSelectionModel().getSelectedItem();
            if (selected != null) handleCancelRequest(selected);
        });

        requestsTableView.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            boolean hasSelection = newVal != null && "PENDING".equals(newVal.getStatus());
            cancelRequestButton.setDisable(!hasSelection);
        });

        // Search Bar Real-Time Listener
        if (searchField != null) {
            searchField.textProperty().addListener((obs, oldVal, newVal) -> {
                loadBrowseBooks();
            });
        }

        updateNotificationBadge();
        showMyBooks();
        initializeSearchFilterPanel();
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
                        Label emptyLabel = new Label("No new notifications.");
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
        if ("REQUEST_ACCEPTED".equals(notif.getType()) || "REQUEST_REJECTED".equals(notif.getType())) {
            showMyRequests();
        }
        notificationPanel.setVisible(false);
        notificationPanel.setManaged(false);
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
        myBooksPanel.setVisible(true); myBooksPanel.setManaged(true);
        myRequestsPanel.setVisible(false); myRequestsPanel.setManaged(false);
        browseCatalogPanel.setVisible(false); browseCatalogPanel.setManaged(false);
        setActiveButton(btnMyBooks); setInactiveButton(btnMyRequests); setInactiveButton(btnBrowse);
        loadMyBorrowedBooks();
    }

    @FXML
    public void showMyRequests() {
        myRequestsPanel.setVisible(true); myRequestsPanel.setManaged(true);
        myBooksPanel.setVisible(false); myBooksPanel.setManaged(false);
        browseCatalogPanel.setVisible(false); browseCatalogPanel.setManaged(false);
        setActiveButton(btnMyRequests); setInactiveButton(btnMyBooks); setInactiveButton(btnBrowse);
        loadMyRequests();
        checkForOverdueBooks();
    }

    @FXML
    public void showBrowseCatalog() {
        browseCatalogPanel.setVisible(true); browseCatalogPanel.setManaged(true);
        myBooksPanel.setVisible(false); myBooksPanel.setManaged(false);
        myRequestsPanel.setVisible(false); myRequestsPanel.setManaged(false);
        setActiveButton(btnBrowse); setInactiveButton(btnMyBooks); setInactiveButton(btnMyRequests);
        checkForOverdueBooks();
        loadBrowseBooks();
    }

    // --- SPRINT 3: GRID VIEW LOAD LOGIC ---
    private void loadBrowseBooks() {
        // 1. Grab Main Search Bar Text
        String query = searchField != null && searchField.getText() != null ? searchField.getText().trim() : null;

        // 2. Grab Advanced Filter Values (safely)
        String genre = searchFilterController != null ? searchFilterController.getGenre() : null;
        String condition = searchFilterController != null ? searchFilterController.getCondition() : null;
        boolean availableOnly = searchFilterController != null && searchFilterController.isAvailableOnly();
        String sortBy = searchFilterController != null ? searchFilterController.getSortBy() : "newest";

        new Thread(() -> {
            // 3. Use the US-3 Database Method!
            SupaBookDAO supaBookDAO = (SupaBookDAO) bookDAO;
            java.util.List<Book> searchResults = supaBookDAO.findByFilters(query, genre, condition, availableOnly, sortBy, 50, 0);

            // Filter out books the user owns themselves
            java.util.List<Book> finalBooks = searchResults.stream()
                    .filter(b -> b.getOwnerId() != currentUserId)
                    .toList();

            // 4. Update the Grid
            javafx.application.Platform.runLater(() -> {
                if (bookGrid != null) {
                    bookGrid.getChildren().clear();

                    if (finalBooks.isEmpty()) {
                        Label noResults = new Label("No assets found matching your criteria.");
                        noResults.setStyle("-fx-font-size: 14px; -fx-text-fill: #6B7280; -fx-padding: 20;");
                        bookGrid.getChildren().add(noResults);
                    } else {
                        for (Book book : finalBooks) {
                            bookGrid.getChildren().add(createBookCard(book));
                        }
                    }
                }
            });
        }).start();
    }

    private VBox createBookCard(Book book) {
        VBox card = new VBox(10);
        card.setStyle("-fx-background-color: white; -fx-background-radius: 10; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 10, 0, 0, 5); -fx-padding: 15; -fx-alignment: top-center;");
        card.setPrefWidth(200);
        card.setMaxWidth(200);

        javafx.scene.layout.StackPane imageContainer = new javafx.scene.layout.StackPane();
        imageContainer.setPrefSize(150, 200);

        if (book.getCoverUrl() != null && !book.getCoverUrl().isEmpty()) {
            ImageView cover = new ImageView();
            cover.setFitWidth(150);
            cover.setFitHeight(200);
            cover.setPreserveRatio(true);
            cover.setImage(new Image(book.getCoverUrl(), 150, 200, true, true));
            imageContainer.getChildren().add(cover);
        } else {
            javafx.scene.shape.Rectangle fallbackRect = new javafx.scene.shape.Rectangle(150, 200, Color.web("#E5E7EB"));
            fallbackRect.setArcWidth(10); fallbackRect.setArcHeight(10);
            Label fallbackText = new Label(book.getTitle());
            fallbackText.setWrapText(true);
            fallbackText.setStyle("-fx-text-fill: #6B7280; -fx-font-weight: bold; -fx-alignment: center; -fx-padding: 10;");
            fallbackText.setTextAlignment(javafx.scene.text.TextAlignment.CENTER);
            imageContainer.getChildren().addAll(fallbackRect, fallbackText);
        }

        Label title = new Label(book.getTitle());
        title.setStyle("-fx-font-weight: bold; -fx-font-size: 14px; -fx-text-fill: #111827;");
        title.setWrapText(true);
        title.setTextAlignment(javafx.scene.text.TextAlignment.CENTER);
        title.setMaxHeight(40);

        Label author = new Label(book.getAuthor());
        author.setStyle("-fx-text-fill: #6B7280; -fx-font-size: 12px;");

        String conditionText = book.getCondition() != null ? book.getCondition() : "Good";
        Label condition = new Label(conditionText);

        if ("Like New".equals(conditionText)) {
            condition.setStyle("-fx-background-color: #D1FAE5; -fx-text-fill: #065F46; -fx-padding: 3 8; -fx-background-radius: 12; -fx-font-size: 10px; -fx-font-weight: bold;");
        } else if ("Very Good".equals(conditionText) || "Good".equals(conditionText)) {
            condition.setStyle("-fx-background-color: #E0E7FF; -fx-text-fill: #4338CA; -fx-padding: 3 8; -fx-background-radius: 12; -fx-font-size: 10px; -fx-font-weight: bold;");
        } else {
            condition.setStyle("-fx-background-color: #FEF3C7; -fx-text-fill: #92400E; -fx-padding: 3 8; -fx-background-radius: 12; -fx-font-size: 10px; -fx-font-weight: bold;");
        }

        Button requestBtn = new Button("Request Asset");
        requestBtn.setMaxWidth(Double.MAX_VALUE);
        requestBtn.setStyle("-fx-background-color: #3B82F6; -fx-text-fill: white; -fx-font-weight: bold; -fx-cursor: hand; -fx-padding: 8; -fx-background-radius: 6;");

        requestBtn.setOnAction(e -> handleBorrowRequest(book));

        card.getChildren().addAll(imageContainer, title, author, condition, requestBtn);
        return card;
    }

    private void initializeSearchFilterPanel() {
        try {
            javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(
                    SearchFilterPanelController.class.getResource("/view/SearchFilterPanel.fxml")
            );
            VBox searchPanel = loader.load();
            searchFilterController = loader.getController();

            // WHEN "APPLY FILTERS" IS CLICKED -> REFRESH THE GRID!
            searchFilterController.setOnFilterAppliedListener(this::loadBrowseBooks);

            if (searchFilterContainer != null) {
                searchFilterContainer.getChildren().clear();
                searchFilterContainer.getChildren().add(searchPanel);
            }

        } catch (java.io.IOException e) {
            System.err.println("[BorrowerDashboard] Failed to load SearchFilterPanel: " + e.getMessage());
        }
    }

    private void handleBorrowRequest(Book book) {
        showAvailabilityForBook(book.getIsbn());
    }

    private void showAvailabilityForBook(String isbn) {
        selectedBookIsbn = isbn;
        Book book = bookDAO.findByIsbn(isbn);
        if (book == null) return;

        try {
            // Load the FXML
            javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(
                    getClass().getResource("/view/AvailabilityPanel.fxml")
            );
            VBox availabilityPanel = loader.load();
            availabilityController = loader.getController();

            // 1. Create the Dialog object FIRST
            // 1. Create the Dialog object
            // 1. Create the Dialog object
            Dialog<ButtonType> availabilityDialog = new Dialog<>();

            // ---> KILL THE WINDOWS BORDER <---
            availabilityDialog.initStyle(javafx.stage.StageStyle.TRANSPARENT);

            availabilityDialog.setHeaderText(null);
            availabilityDialog.setGraphic(null);

            // Force the background to be transparent so our rounded corners show!
            availabilityDialog.getDialogPane().setStyle("-fx-background-color: transparent; -fx-padding: 0;");
            availabilityDialog.getDialogPane().getScene().setFill(javafx.scene.paint.Color.TRANSPARENT);

            // 2. Wire up the buttons
            availabilityController.setOnBorrowClickedListener((lenderId) -> {
                availabilityDialog.setResult(ButtonType.CLOSE);
                availabilityDialog.close();
                this.onBorrowFromAvailability(isbn, lenderId, book.getTitle());
            });

            // Make our custom "X" button close the dialog
            availabilityController.setOnCloseClickedListener(() -> {
                availabilityDialog.setResult(ButtonType.CLOSE);
                availabilityDialog.close();
            });

            // 3. Load the data
            availabilityController.loadAvailability(isbn);

            // 4. Show the popup (We don't need to add a default ButtonType.CLOSE anymore!)
            availabilityDialog.getDialogPane().setContent(availabilityPanel);
            availabilityDialog.showAndWait();

        } catch (java.io.IOException e) {
            System.err.println("[BorrowerDashboard] FXML Load Crash!");
            e.printStackTrace(); // This will print the exact red error line if your file is missing!
        }
    }

    private void onBorrowFromAvailability(String isbn, int lenderId, String title) {
        Dialog<LocalDate> dialog = new Dialog<>();
        dialog.setTitle("Request to Borrow Book");
        dialog.setHeaderText("Request: " + title);

        DatePicker dueDate = new DatePicker(LocalDate.now().plusDays(14));
        dueDate.setStyle("-fx-font-size: 13px; -fx-padding: 10;");

        javafx.scene.layout.GridPane grid = new javafx.scene.layout.GridPane();
        grid.setHgap(10); grid.setVgap(10); grid.setPadding(new javafx.geometry.Insets(10));
        grid.add(new Label("Requested Due Date (default 14 days):"), 0, 0);
        grid.add(dueDate, 1, 0);

        dialog.getDialogPane().setContent(grid);
        dialog.getDialogPane().getButtonTypes().addAll(javafx.scene.control.ButtonType.OK, javafx.scene.control.ButtonType.CANCEL);

        dialog.setResultConverter(buttonType -> buttonType == javafx.scene.control.ButtonType.OK ? dueDate.getValue() : null);

        if (dialog.showAndWait().isPresent()) {
            LocalDate selectedDueDate = dialog.getResult();
            if (selectedDueDate != null && selectedDueDate.isAfter(LocalDate.now())) {
                BorrowRequest request = new BorrowRequest(isbn, currentUserId, lenderId, selectedDueDate);
                int requestId = borrowRequestDAO.createBorrowRequest(request);

                if (requestId > 0) {
                    Alert alert = new Alert(Alert.AlertType.INFORMATION);
                    alert.setHeaderText("Borrow Request Created!");
                    alert.setContentText("Your request has been sent to the lender. Check 'My Requests' for status.");
                    alert.showAndWait();

                    loadBrowseBooks(); // Refresh grid
                    loadMyRequests();
                    showMyRequests();
                } else {
                    Alert alert = new Alert(Alert.AlertType.ERROR);
                    alert.setHeaderText("Request Failed");
                    alert.setContentText("Unable to create borrow request. Please try again.");
                    alert.showAndWait();
                }
            }
        }
    }

    private void loadMyBorrowedBooks() {
        borrowedList.clear();
        borrowedList.addAll(bookDAO.findBorrowedByUser(currentUserId));
        checkForOverdueBooks();
    }

    private void loadMyRequests() {
        new Thread(() -> {
            java.util.List<BorrowRequest> requests = borrowRequestDAO.getMyRequests(currentUserId);
            java.util.Map<String, Book> bookCache = new java.util.HashMap<>();
            for (BorrowRequest req : requests) {
                if (!bookCache.containsKey(req.getBookIsbn())) {
                    Book book = bookDAO.findByIsbn(req.getBookIsbn());
                    if (book != null) bookCache.put(req.getBookIsbn(), book);
                }
            }
            cachedBooks = bookCache;
            javafx.application.Platform.runLater(() -> {
                requestsList.clear();
                requestsList.addAll(requests);
            });
        }).start();
    }

    @FXML
    private void switchToLender() {
        navigateTo("/view/LenderDashboard.fxml", "Lender Dashboard",
                ctrl -> ((BaseDashboardController) ctrl).setUsername(currentUsername, currentUserId));
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
        grid.setHgap(10); grid.setVgap(10); grid.setPadding(new javafx.geometry.Insets(10));
        grid.add(new Label("Current Due Date:"), 0, 0);
        grid.add(new Label(currentDue != null ? currentDue.toString() : "N/A"), 1, 0);
        grid.add(new Label("New Due Date:"), 0, 1);
        grid.add(newDueDate, 1, 1);

        dialog.getDialogPane().setContent(grid);
        dialog.getDialogPane().getButtonTypes().addAll(javafx.scene.control.ButtonType.OK, javafx.scene.control.ButtonType.CANCEL);
        dialog.setResultConverter(buttonType -> buttonType == javafx.scene.control.ButtonType.OK ? newDueDate.getValue() : null);

        if (dialog.showAndWait().isPresent()) {
            LocalDate selectedDueDate = dialog.getResult();
            if (selectedDueDate != null && selectedDueDate.isAfter(LocalDate.now())) {
                bookDAO.renewBook(book.getIsbn(), selectedDueDate);
                loadMyBorrowedBooks();
            }
        }
    }

    @FXML
    private void handleReturn(Book book) {
        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setHeaderText("Initiate Return: " + book.getTitle());
        confirmAlert.setContentText("This will start the return process. You will need to choose a handover method.");

        if (confirmAlert.showAndWait().orElse(ButtonType.CANCEL) != ButtonType.OK) return;

        SupaBorrowRequestDAO dao = (SupaBorrowRequestDAO) borrowRequestDAO;
        List<BorrowRequest> active = dao.getActiveRequestsFor(currentUserId);
        BorrowRequest currentReq = active.stream()
                .filter(r -> r.getBookIsbn().equals(book.getIsbn()) && "BORROWED".equals(r.getStatus()))
                .findFirst().orElse(null);

        if (currentReq == null) {
            Alert error = new Alert(Alert.AlertType.WARNING);
            error.setHeaderText("Action Not Allowed");
            error.setContentText("The system doesn't register this book as officially 'BORROWED' yet.\n\nMake sure the Lender has verified your OTP or you have confirmed the Courier receipt!");
            error.showAndWait();
            return;
        }

        Optional<HandoverMethodDialogController.HandoverMethod> methodOpt = HandoverMethodDialogController.show();
        if (methodOpt.isEmpty()) return;

        if (methodOpt.get() == HandoverMethodDialogController.HandoverMethod.MEETUP) {
            if (dao.initiateReturnMeetup(currentReq.getRequestId())) {
                BorrowRequest updated = dao.getRequestById(currentReq.getRequestId());
                String returnOtp = (updated.getHandover() != null) ? updated.getHandover().getReturnOtp() : "N/A";

                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setHeaderText("Return Meetup Initiated");
                alert.setContentText("Show this Return OTP to the lender when you meet: " + returnOtp);
                alert.showAndWait();
            }
        } else {
            Optional<CourierFormDialogController.CourierDetails> courierOpt = CourierFormDialogController.show();
            if (courierOpt.isPresent()) {
                CourierFormDialogController.CourierDetails d = courierOpt.get();
                HandoverDetails h = new HandoverDetails();
                h.setReturnCourierService(d.service);
                h.setReturnCourierPerson(d.personName);
                h.setReturnVehiclePlate(d.vehiclePlate);
                h.setReturnVehicleType(d.vehicleType);
                h.setReturnProofImageUrl(d.proofImagePath);

                if (dao.initiateReturnCourier(currentReq.getRequestId(), h)) {
                    Alert alert = new Alert(Alert.AlertType.INFORMATION, "Book is in transit back to the lender.");
                    alert.show();
                }
            }
        }

        loadMyBorrowedBooks();
        loadMyRequests();
    }

    private void handleCancelRequest(BorrowRequest request) {
        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setHeaderText("Cancel Borrow Request");
        confirmAlert.setContentText("Are you sure you want to cancel this request?");

        if (confirmAlert.showAndWait().isPresent() && confirmAlert.getResult() == javafx.scene.control.ButtonType.OK) {
            if (borrowRequestDAO.cancelBorrowRequest(request.getRequestId())) {
                loadMyRequests();
            }
        }
    }

    private void setActiveButton(Button btn) {
        btn.setStyle("-fx-background-color: rgba(0,0,0,0.2); -fx-text-fill: #FFFFFF; -fx-font-weight: 700; -fx-padding: 10 16; -fx-cursor: hand; -fx-background-radius: 6; -fx-border-color: transparent transparent transparent #8B5CF6; -fx-border-width: 0 0 0 3;");
    }

    private void setInactiveButton(Button btn) {
        btn.setStyle("-fx-background-color: transparent; -fx-text-fill: #C4B5FD; -fx-font-weight: 600; -fx-padding: 10 16; -fx-cursor: hand; -fx-background-radius: 6; -fx-border-width: 0;");
    }

    private void checkForOverdueBooks() {
        java.util.List<Book> booksToCheck = borrowedList;
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

    private void displayOverdueWarning(java.util.List<Book> overdueBooks) {
        overdueWarningBanner.setVisible(true);
        overdueWarningBanner.setManaged(true);
        int count = overdueBooks.size();
        String title = count == 1 ? "1 Overdue Book" : count + " Overdue Books";
        String message = "⏰ Due " + (count == 1 ? "date" : "dates") + " passed. Return " + (count == 1 ? "it" : "them") + " as soon as possible.";

        overdueWarningTitle.setText(title);
        overdueWarningMessage.setText(message);
        applyOverdueAnimation();
        applyDashboardBleedingEffect();
    }

    private void hideOverdueWarning() {
        overdueWarningBanner.setVisible(false);
        overdueWarningBanner.setManaged(false);

        if (vignetteOverlay != null) {
            javafx.animation.Timeline fadeOut = new javafx.animation.Timeline(
                    new javafx.animation.KeyFrame(javafx.util.Duration.millis(300),
                            new javafx.animation.KeyValue(vignetteOverlay.opacityProperty(), 0.25))
            );
            fadeOut.play();
        }

        if (dashboardRoot.getUserData() instanceof Object[]) {
            Object[] timelines = (Object[]) dashboardRoot.getUserData();
            for (Object timeline : timelines) {
                if (timeline instanceof javafx.animation.Timeline) {
                    ((javafx.animation.Timeline) timeline).stop();
                }
            }
        }

        dashboardRoot.setStyle("-fx-background-color: #F3F4F6;");
        dashboardRoot.setTranslateX(0);
        dashboardRoot.setTranslateY(0);
    }

    private void applyOverdueAnimation() {
        javafx.animation.Timeline timeline = new javafx.animation.Timeline(
                new javafx.animation.KeyFrame(javafx.util.Duration.millis(0), new javafx.animation.KeyValue(overdueWarningBanner.scaleXProperty(), 1.0), new javafx.animation.KeyValue(overdueWarningBanner.scaleYProperty(), 1.0), new javafx.animation.KeyValue(overdueWarningBanner.opacityProperty(), 1.0), new javafx.animation.KeyValue(overdueWarningBanner.translateXProperty(), 0)),
                new javafx.animation.KeyFrame(javafx.util.Duration.millis(600), new javafx.animation.KeyValue(overdueWarningBanner.scaleXProperty(), 1.02), new javafx.animation.KeyValue(overdueWarningBanner.scaleYProperty(), 1.05), new javafx.animation.KeyValue(overdueWarningBanner.opacityProperty(), 0.93), new javafx.animation.KeyValue(overdueWarningBanner.translateXProperty(), 3)),
                new javafx.animation.KeyFrame(javafx.util.Duration.millis(1200), new javafx.animation.KeyValue(overdueWarningBanner.scaleXProperty(), 1.0), new javafx.animation.KeyValue(overdueWarningBanner.scaleYProperty(), 1.0), new javafx.animation.KeyValue(overdueWarningBanner.opacityProperty(), 1.0), new javafx.animation.KeyValue(overdueWarningBanner.translateXProperty(), -3)),
                new javafx.animation.KeyFrame(javafx.util.Duration.millis(1800), new javafx.animation.KeyValue(overdueWarningBanner.scaleXProperty(), 0.98), new javafx.animation.KeyValue(overdueWarningBanner.scaleYProperty(), 0.98), new javafx.animation.KeyValue(overdueWarningBanner.opacityProperty(), 0.86), new javafx.animation.KeyValue(overdueWarningBanner.translateXProperty(), 2)),
                new javafx.animation.KeyFrame(javafx.util.Duration.millis(2400), new javafx.animation.KeyValue(overdueWarningBanner.scaleXProperty(), 1.0), new javafx.animation.KeyValue(overdueWarningBanner.scaleYProperty(), 1.0), new javafx.animation.KeyValue(overdueWarningBanner.opacityProperty(), 1.0), new javafx.animation.KeyValue(overdueWarningBanner.translateXProperty(), 0))
        );
        timeline.setCycleCount(javafx.animation.Animation.INDEFINITE);
        timeline.play();
    }

    private void setupVignetteOverlay() {
        if (vignetteOverlay == null) return;
        vignetteOverlay.getChildren().clear();
        vignetteOverlay.setMouseTransparent(true);
        vignetteOverlay.setPickOnBounds(false);

        javafx.scene.paint.RadialGradient gradient = new javafx.scene.paint.RadialGradient(
                0.5, 0.5, 0.5, 0.5, 0.8, false, javafx.scene.paint.CycleMethod.NO_CYCLE,
                new javafx.scene.paint.Stop(0, javafx.scene.paint.Color.web("#00000000")),
                new javafx.scene.paint.Stop(0.6, javafx.scene.paint.Color.web("#EF444433")),
                new javafx.scene.paint.Stop(1.0, javafx.scene.paint.Color.web("#B91C1C80"))
        );

        javafx.scene.shape.Rectangle vignetteRect = new javafx.scene.shape.Rectangle();
        vignetteRect.widthProperty().bind(vignetteOverlay.widthProperty());
        vignetteRect.heightProperty().bind(vignetteOverlay.heightProperty());
        vignetteRect.setFill(gradient);
        vignetteRect.setMouseTransparent(true);

        vignetteOverlay.getChildren().add(vignetteRect);
        vignetteOverlay.setOpacity(0.25);
        vignetteOverlay.setVisible(true);
    }

    private void applyDashboardBleedingEffect() {
        if (vignetteOverlay != null) {
            vignetteOverlay.toFront();
            javafx.animation.Timeline vignetteTimeline = new javafx.animation.Timeline(
                    new javafx.animation.KeyFrame(javafx.util.Duration.millis(0), new javafx.animation.KeyValue(vignetteOverlay.opacityProperty(), 0.25)),
                    new javafx.animation.KeyFrame(javafx.util.Duration.millis(500), new javafx.animation.KeyValue(vignetteOverlay.opacityProperty(), 0.35)),
                    new javafx.animation.KeyFrame(javafx.util.Duration.millis(1000), new javafx.animation.KeyValue(vignetteOverlay.opacityProperty(), 0.55)),
                    new javafx.animation.KeyFrame(javafx.util.Duration.millis(1500), new javafx.animation.KeyValue(vignetteOverlay.opacityProperty(), 0.35)),
                    new javafx.animation.KeyFrame(javafx.util.Duration.millis(2000), new javafx.animation.KeyValue(vignetteOverlay.opacityProperty(), 0.25))
            );
            vignetteTimeline.setCycleCount(javafx.animation.Animation.INDEFINITE);
            vignetteTimeline.play();
            if (dashboardRoot.getUserData() == null) {
                dashboardRoot.setUserData(new Object[]{vignetteTimeline, null});
            }
        }

        javafx.animation.Timeline colorTimeline = new javafx.animation.Timeline(
                new javafx.animation.KeyFrame(javafx.util.Duration.millis(0), new javafx.animation.KeyValue(dashboardRoot.styleProperty(), "-fx-background-color: #F3F4F6;")),
                new javafx.animation.KeyFrame(javafx.util.Duration.millis(600), new javafx.animation.KeyValue(dashboardRoot.styleProperty(), "-fx-background-color: #FEE2E2;")),
                new javafx.animation.KeyFrame(javafx.util.Duration.millis(1200), new javafx.animation.KeyValue(dashboardRoot.styleProperty(), "-fx-background-color: #FECACA;")),
                new javafx.animation.KeyFrame(javafx.util.Duration.millis(1800), new javafx.animation.KeyValue(dashboardRoot.styleProperty(), "-fx-background-color: #FEE2E2;")),
                new javafx.animation.KeyFrame(javafx.util.Duration.millis(2400), new javafx.animation.KeyValue(dashboardRoot.styleProperty(), "-fx-background-color: #F3F4F6;"))
        );
        colorTimeline.setCycleCount(javafx.animation.Animation.INDEFINITE);
        colorTimeline.play();

        Object[] timelines = (dashboardRoot.getUserData() instanceof Object[]) ? (Object[]) dashboardRoot.getUserData() : new Object[2];
        timelines[0] = (timelines[0] == null) ? null : timelines[0];
        timelines[1] = colorTimeline;
        dashboardRoot.setUserData(timelines);
    }

    @FXML
    public void dismissOverdueWarning() {
        overdueWarningBanner.setVisible(false);
        overdueWarningBanner.setManaged(false);
    }

    private void testOverdueWarning() {
        Book testBook1 = new Book("TEST-ISBN-001", "Test Overdue Book 1", "Test Author", "Fiction", currentUserId, false, currentUserId, LocalDate.now().minusDays(10), LocalDate.now().minusDays(5), true, "Good", null, 0);
        Book testBook2 = new Book("TEST-ISBN-002", "Test Overdue Book 2", "Another Author", "Mystery", currentUserId, false, currentUserId, LocalDate.now().minusDays(8), LocalDate.now().minusDays(2), true, "Good", null, 0);
        borrowedList.add(testBook1);
        borrowedList.add(testBook2);
        checkForOverdueBooks();
    }

    private void removeTestOverdueBooks() {
        loadMyBorrowedBooks();
    }

    private void handleConfirmReceipt(BorrowRequest request) {
        SupaBorrowRequestDAO dao = (SupaBorrowRequestDAO) borrowRequestDAO;
        if (dao.markReceivedByCourier(request.getRequestId())) {
            loadMyRequests();
            loadMyBorrowedBooks();
            showMyBooks();
        }
    }

    private void showMeetupDetails(BorrowRequest request) {
        String otp = request.getOtp();
        String location = request.getHandover().getMeetupLocation();

        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Meetup Details");
        alert.setHeaderText("Meetup at: " + location);

        Label l = new Label(otp);
        l.setStyle("-fx-font-size: 32px; -fx-font-weight: 900; -fx-text-fill: #4F46E5;");

        VBox v = new VBox(10, new Label("Show this code to the lender:"), l);
        v.setAlignment(javafx.geometry.Pos.CENTER);
        alert.getDialogPane().setContent(v);
        alert.showAndWait();
    }
}
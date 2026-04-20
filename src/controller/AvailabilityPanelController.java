package controller;

import database.DatabaseHandler;
import database.SupaBookDAO;
import database.SupaUserDAO;
import database.UserDAO;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import model.User;

import java.util.List;
import java.util.function.Consumer;

public class AvailabilityPanelController {

    @FXML private Label lenderCountLabel;
    @FXML private VBox lendersContainer;

    private Consumer<Integer> onBorrowClicked;
    private final SupaBookDAO bookDAO = new SupaBookDAO();
    private final UserDAO userDAO = new SupaUserDAO();

    public void setOnBorrowClickedListener(Consumer<Integer> listener) {
        this.onBorrowClicked = listener;
    }

    // Add these variables at the top
    @FXML private Button closeBtn;
    private Runnable onCloseClicked;

    // Add this setter method
    public void setOnCloseClickedListener(Runnable listener) {
        this.onCloseClicked = listener;
    }

    @FXML
    public void initialize() {
        // Wire up the close button
        if (closeBtn != null) {
            closeBtn.setOnAction(e -> {
                if (onCloseClicked != null) onCloseClicked.run();
            });
        }
    }

    public void loadAvailability(String isbn) {
        lendersContainer.getChildren().clear();
        lenderCountLabel.setText("Loading available copies...");

        new Thread(() -> {
            // Fetch grouped data using US-2 feature
            List<SupaBookDAO.BookWithLenderInfo> results = bookDAO.findGroupedByLender(isbn);

            Platform.runLater(() -> {
                lendersContainer.getChildren().clear();

                if (results.isEmpty()) {
                    lenderCountLabel.setText("0 lenders have this asset available.");
                    Label empty = new Label("No available copies found right now. Check back later!");
                    empty.setStyle("-fx-text-fill: #6B7280; -fx-font-size: 14px; -fx-padding: 20;");
                    lendersContainer.getChildren().add(empty);
                    return;
                }

                int totalAvailable = results.stream().mapToInt(r -> r.availableCopies).sum();
                lenderCountLabel.setText(results.size() + " lender(s) · " + totalAvailable + " copies available");

                for (SupaBookDAO.BookWithLenderInfo info : results) {
                    lendersContainer.getChildren().add(createLenderCard(info));
                }
            });
        }).start();
    }

    private HBox createLenderCard(SupaBookDAO.BookWithLenderInfo info) {
        User lender = userDAO.getAllUsers().stream().filter(u -> u.getId() == info.lenderId).findFirst().orElse(null);
        String lenderName = (lender != null && lender.getUsername() != null) ? lender.getUsername() : "User #" + info.lenderId;

        // Modern Card Container (White with Drop Shadow)
        HBox card = new HBox(16);
        card.setStyle("-fx-background-color: #FFFFFF; -fx-padding: 20; -fx-background-radius: 12; -fx-border-color: #F3F4F6; -fx-border-width: 1; -fx-border-radius: 12; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.06), 15, 0, 0, 4);");
        card.setAlignment(javafx.geometry.Pos.CENTER_LEFT);

        // Avatar (Violet Theme)
        javafx.scene.layout.StackPane avatar = new javafx.scene.layout.StackPane();
        Circle circle = new Circle(24, Color.web("#8B5CF6"));
        String initial = lenderName.substring(0, 1).toUpperCase();
        Label letter = new Label(initial);
        letter.setStyle("-fx-text-fill: white; -fx-font-weight: 900; -fx-font-size: 18px;");
        avatar.getChildren().addAll(circle, letter);

        // Text Details
        VBox textContainer = new VBox(6);
        Label nameLabel = new Label(lenderName);
        nameLabel.setStyle("-fx-font-weight: 800; -fx-font-size: 16px; -fx-text-fill: #111827;");

        Label copiesLabel = new Label(info.availableCopies + " of " + info.totalCopies + " available");
        copiesLabel.setStyle("-fx-text-fill: #10B981; -fx-font-size: 13px; -fx-font-weight: 700;");

        textContainer.getChildren().addAll(nameLabel, copiesLabel);

        // Spacer
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        // Primary Action Button (Matches the Request Button on the dashboard)
        Button requestBtn = new Button("Request Asset");
        requestBtn.setStyle("-fx-background-color: #8B5CF6; -fx-text-fill: white; -fx-font-weight: 800; -fx-font-size: 13px; -fx-padding: 10 20; -fx-background-radius: 8; -fx-cursor: hand;");

        if (info.availableCopies <= 0) {
            requestBtn.setDisable(true);
            requestBtn.setText("Unavailable");
            requestBtn.setStyle("-fx-background-color: #F3F4F6; -fx-text-fill: #9CA3AF; -fx-font-weight: 800; -fx-font-size: 13px; -fx-padding: 10 20; -fx-background-radius: 8;");
            copiesLabel.setStyle("-fx-text-fill: #EF4444; -fx-font-size: 13px; -fx-font-weight: 700;");
        } else {
            requestBtn.setOnAction(e -> {
                if (onBorrowClicked != null) {
                    onBorrowClicked.accept(info.lenderId);
                }
            });
        }

        card.getChildren().addAll(avatar, textContainer, spacer, requestBtn);
        return card;
    }
}
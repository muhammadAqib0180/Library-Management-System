package controller;

import database.SupaBookDAO;
import database.SupaUserDAO;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.effect.DropShadow;
import javafx.scene.paint.Color;
import model.User;

import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class AvailabilityPanelController {

    @FXML private VBox lenderListContainer;
    @FXML private VBox emptyStatePane;
    @FXML private Label resultCountLabel;
    @FXML private Button refreshButton;

    private String currentBookIsbn;
    private final SupaBookDAO bookDAO = new SupaBookDAO();
    private final SupaUserDAO userDAO = new SupaUserDAO();

    private java.util.function.Consumer<Integer> onBorrowClickedListener;

    public void setOnBorrowClickedListener(java.util.function.Consumer<Integer> listener) {
        this.onBorrowClickedListener = listener;
    }

    @FXML
    private void initialize() {
        refreshButton.setOnAction(e -> {
            if (currentBookIsbn != null) {
                loadAvailability(currentBookIsbn);
            }
        });
    }

    public void loadAvailability(String isbn) {
        this.currentBookIsbn = isbn;
        lenderListContainer.getChildren().clear();

        List<SupaBookDAO.BookWithLenderInfo> results = bookDAO.findGroupedByLender(isbn);

        if (results.isEmpty()) {
            emptyStatePane.setVisible(true);
            emptyStatePane.setManaged(true);
            resultCountLabel.setText("0 lenders found");
            return;
        }

        emptyStatePane.setVisible(false);
        emptyStatePane.setManaged(false);
        resultCountLabel.setText(results.size() + " lender" + (results.size() == 1 ? "" : "s") + " have this asset");

        for (SupaBookDAO.BookWithLenderInfo info : results) {
            lenderListContainer.getChildren().add(createLenderCard(info));
        }
    }

    private HBox createLenderCard(SupaBookDAO.BookWithLenderInfo info) {
        User lender = userDAO.getUserById(info.lenderId);
        String lenderName = lender != null ? lender.getUsername() : "Lender #" + info.lenderId;

        HBox card = new HBox(16);
        card.setStyle("-fx-background-color: #FFFFFF; -fx-border-color: #E5E7EB; -fx-border-radius: 8; -fx-background-radius: 8; -fx-padding: 16;");
        card.setPrefWidth(Double.MAX_VALUE);
        card.setAlignment(Pos.CENTER_LEFT);

        DropShadow shadow = new DropShadow();
        shadow.setColor(Color.web("#00000008"));
        shadow.setRadius(10);
        shadow.setOffsetY(2);
        card.setEffect(shadow);

        // Lender Info (Left Side)
        VBox lenderInfo = new VBox(4);
        Label nameLabel = new Label("Lender: " + lenderName);
        nameLabel.setStyle("-fx-font-weight: 800; -fx-font-size: 14px; -fx-text-fill: #111827;");

        Label copiesText = new Label("Inventory: " + info.availableCopies + " of " + info.totalCopies + " available");
        copiesText.setStyle("-fx-font-size: 12px; -fx-text-fill: #6B7280;");

        lenderInfo.getChildren().addAll(nameLabel, copiesText);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        // Status & Action (Right Side)
        VBox actionBox = new VBox(8);
        actionBox.setAlignment(Pos.CENTER_RIGHT);

        Label badge = new Label();
        if (info.availableCopies > 0) {
            badge.setText("Available");
            badge.setStyle("-fx-background-color: #D1FAE5; -fx-text-fill: #065F46; -fx-font-weight: 700; -fx-font-size: 11px; -fx-padding: 4 8; -fx-background-radius: 4;");
        } else {
            if (info.nextReturnDate != null) {
                String nextDate = info.nextReturnDate.format(DateTimeFormatter.ofPattern("MMM dd"));
                badge.setText("Returns " + nextDate);
            } else {
                badge.setText("Waitlist");
            }
            badge.setStyle("-fx-background-color: #FEF3C7; -fx-text-fill: #B45309; -fx-font-weight: 700; -fx-font-size: 11px; -fx-padding: 4 8; -fx-background-radius: 4;");
        }

        Button borrowButton = new Button("Request");
        if (info.availableCopies > 0) {
            borrowButton.setStyle("-fx-background-color: #8B5CF6; -fx-text-fill: #FFFFFF; -fx-font-weight: 700; -fx-font-size: 12px; -fx-padding: 8 16; -fx-background-radius: 6; -fx-cursor: hand;");
            borrowButton.setOnAction(e -> onBorrowClicked(info.lenderId));
        } else {
            borrowButton.setStyle("-fx-background-color: #F3F4F6; -fx-text-fill: #9CA3AF; -fx-font-weight: 700; -fx-font-size: 12px; -fx-padding: 8 16; -fx-background-radius: 6;");
            borrowButton.setDisable(true);
        }

        actionBox.getChildren().addAll(badge, borrowButton);

        card.getChildren().addAll(lenderInfo, spacer, actionBox);
        return card;
    }

    private void onBorrowClicked(int lenderId) {
        if (onBorrowClickedListener != null) {
            onBorrowClickedListener.accept(lenderId);
        }
    }

    public static VBox loadComponent(String isbn) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    AvailabilityPanelController.class.getResource("/view/AvailabilityPanel.fxml")
            );
            VBox component = loader.load();
            AvailabilityPanelController controller = loader.getController();
            controller.loadAvailability(isbn);
            return component;
        } catch (IOException e) {
            System.err.println("[AvailabilityPanel] Failed to load: " + e.getMessage());
            e.printStackTrace();
            return new VBox();
        }
    }
}
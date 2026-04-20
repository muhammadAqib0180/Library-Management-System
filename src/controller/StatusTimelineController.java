package controller;

import database.SupaBorrowRequestDAO;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import model.BorrowRequest;
import model.HandoverStatus;

import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Controller for StatusTimelineComponent (US-1).
 * Displays timestamped state history of a borrow request.
 */
public class StatusTimelineController {

    @FXML private VBox timelineContainer;

    private final SupaBorrowRequestDAO dao = new SupaBorrowRequestDAO();

    /**
     * Load and display the timeline for a specific request.
     * @param requestId the borrow request ID
     */
    public void loadTimeline(int requestId) {
        timelineContainer.getChildren().clear();

        List<SupaBorrowRequestDAO.StateTransition> history = dao.getStateHistory(requestId);

        if (history.isEmpty()) {
            Text empty = new Text("No state history available");
            empty.setStyle("-fx-fill: #999; -fx-font-size: 11;");
            timelineContainer.getChildren().add(empty);
            return;
        }

        for (int i = 0; i < history.size(); i++) {
            SupaBorrowRequestDAO.StateTransition transition = history.get(i);
            timelineContainer.getChildren().add(createTimelineEntry(transition, i == history.size() - 1));
        }
    }

    private HBox createTimelineEntry(SupaBorrowRequestDAO.StateTransition t, boolean isLatest) {
        HBox entry = new HBox(12);
        entry.setStyle(
            "-fx-border-color: #e0e0e0; -fx-border-width: 0 0 0 3; " +
            "-fx-border-color: " + getStatusColor(t.toState) + "; " +
            "-fx-padding: 10; -fx-border-radius: 4;"
        );
        entry.setPrefWidth(Double.MAX_VALUE);
        entry.setAlignment(Pos.CENTER_LEFT);

        // Status Badge
        Label badge = new Label(getStatusEmoji(t.toState) + " " + formatStatus(t.toState));
        badge.setStyle(
            "-fx-font-weight: bold; -fx-font-size: 11; " +
            "-fx-text-fill: " + getStatusTextColor(t.toState) + "; " +
            "-fx-min-width: 120;"
        );

        // Timeline Info
        VBox info = new VBox(3);
        info.setStyle("-fx-padding: 0;");

        // Timestamp
        String timeStr = t.timestamp != null
            ? t.timestamp.format(java.time.format.DateTimeFormatter.ofPattern("MMM dd, yyyy HH:mm"))
            : "Unknown time";
        Text timeText = new Text(timeStr);
        timeText.setStyle("-fx-fill: #666; -fx-font-size: 10;");

        // Note (if available)
        if (t.note != null && !t.note.isEmpty()) {
            Text noteText = new Text("Note: " + t.note);
            noteText.setStyle("-fx-fill: #333; -fx-font-size: 10;");
            info.getChildren().addAll(timeText, noteText);
        } else {
            info.getChildren().add(timeText);
        }

        entry.getChildren().addAll(badge, info);
        return entry;
    }

    private String getStatusColor(String status) {
        return switch (status) {
            case "PENDING" -> "#ffc107";       // Amber
            case "ACCEPTED" -> "#2196f3";     // Blue
            case "AWAITING_HANDOVER" -> "#ff9800"; // Orange
            case "IN_TRANSIT" -> "#ff9800";   // Orange
            case "BORROWED" -> "#4caf50";     // Green
            case "RETURN_IN_TRANSIT" -> "#ff9800"; // Orange
            case "RETURNED" -> "#4caf50";     // Green
            case "REJECTED" -> "#f44336";     // Red
            case "CANCELLED" -> "#9e9e9e";    // Gray
            default -> "#757575";
        };
    }

    private String getStatusTextColor(String status) {
        return switch (status) {
            case "PENDING", "AWAITING_HANDOVER", "IN_TRANSIT", "RETURN_IN_TRANSIT" -> "#ff6f00";
            case "BORROWED", "RETURNED" -> "#2e7d32";
            case "REJECTED", "CANCELLED" -> "#c62828";
            default -> "#424242";
        };
    }

    private String getStatusEmoji(String status) {
        return switch (status) {
            case "PENDING" -> "⏳";
            case "ACCEPTED" -> "✋";
            case "AWAITING_HANDOVER", "IN_TRANSIT", "RETURN_IN_TRANSIT" -> "🚚";
            case "BORROWED" -> "📖";
            case "RETURNED" -> "✅";
            case "REJECTED" -> "❌";
            case "CANCELLED" -> "⊘";
            default -> "•";
        };
    }

    private String formatStatus(String status) {
        return status.replace("_", " ");
    }

    /**
     * Load and display the timeline component for a borrow request FXML.
     * Call this from your controller's initialize() or when you need to display the timeline.
     * @param requestId the borrow request ID
     * @return the loaded StatusTimelineComponent node
     */
    public static VBox loadComponent(int requestId) {
        try {
            FXMLLoader loader = new FXMLLoader(
                StatusTimelineController.class.getResource("/view/StatusTimelineComponent.fxml")
            );
            VBox component = loader.load();
            StatusTimelineController controller = loader.getController();
            controller.loadTimeline(requestId);
            return component;
        } catch (IOException e) {
            System.err.println("[StatusTimelineComponent] Failed to load: " + e.getMessage());
            return new VBox();
        }
    }
}

package controller;

import database.SupaRatingDAO;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Button;
import javafx.scene.control.Dialog;
import javafx.scene.control.Spinner;
import javafx.scene.control.TextArea;
import javafx.scene.text.Text;
import model.Rating;
import model.Rating.TargetType;

import java.io.IOException;
import java.util.Optional;

/**
 * Controller for RatingDialog (US-6).
 * User rates the other party (1-5 stars + comment) after transaction completion.
 */
public class RatingDialogController {

    @FXML private Dialog<Rating> dialog;
    @FXML private Text headerText;
    @FXML private Spinner<Integer> starSpinner;
    @FXML private Text starEmoji;
    @FXML private TextArea commentField;
    @FXML private Button skipButton;
    @FXML private Button submitButton;

    private int requestId;
    private int currentUserId;
    private Integer targetUserId; // Nullable for book ratings
    private String targetBookIsbn; // Non-null for book ratings
    private TargetType targetType = TargetType.USER; // Default: rating a user
    private Rating result = null;
    private final SupaRatingDAO ratingDAO = new SupaRatingDAO();

    @FXML
    private void initialize() {
        // Update emoji when star rating changes
        starSpinner.valueProperty().addListener((obs, oldVal, newVal) -> updateStarEmoji(newVal));

        skipButton.setOnAction(e -> {
            result = null;
            dialog.setResult(null);
            dialog.close();
        });

        submitButton.setOnAction(e -> submitRating());

        dialog.setResultConverter(param -> result);
    }

    private void updateStarEmoji(int stars) {
        starEmoji.setText("⭐".repeat(stars));
    }

    private void submitRating() {
        int stars = starSpinner.getValue();
        String comment = commentField.getText().trim();

        // Create rating object
        Rating rating = new Rating();
        rating.setRequestId(requestId);
        rating.setRaterId(currentUserId);
        
        // Set target based on type (user or book)
        if (targetType == TargetType.USER) {
            rating.setTargetUserId(targetUserId);
            rating.setTargetBookIsbn(null);
        } else {
            rating.setTargetUserId(null);
            rating.setTargetBookIsbn(targetBookIsbn);
        }
        
        rating.setStars(stars);
        rating.setComment(comment.isEmpty() ? null : comment);
        rating.setTargetType(targetType);

        // Persist to database
        boolean ok = ratingDAO.insert(rating);
        if (ok) {
            this.result = rating;
            dialog.setResult(rating);
            dialog.close();
        } else {
            System.err.println("[RatingDialog] Failed to save rating");
        }
    }

    /**
     * Show rating dialog for rating a user (lender/borrower).
     * @param requestId the transaction ID
     * @param currentUserId the rater (current logged-in user)
     * @param targetUserId the person being rated
     * @param targetName the name of the person being rated (for display)
     * @return Optional containing the submitted Rating, or empty if skipped/cancelled
     */
    public static Optional<Rating> showUserRating(int requestId, int currentUserId, 
                                                    int targetUserId, String targetName) {
        try {
            FXMLLoader loader = new FXMLLoader(
                RatingDialogController.class.getResource("/view/RatingDialog.fxml")
            );
            Dialog<Rating> dialog = loader.load();
            RatingDialogController controller = loader.getController();
            
            controller.requestId = requestId;
            controller.currentUserId = currentUserId;
            controller.targetUserId = targetUserId;
            controller.targetType = TargetType.USER;
            controller.headerText.setText("Rate " + targetName);
            
            return dialog.showAndWait();
        } catch (IOException e) {
            System.err.println("[RatingDialog] Failed to load: " + e.getMessage());
            return Optional.empty();
        }
    }

    /**
     * Show rating dialog for rating a book.
     * @param requestId the transaction ID
     * @param currentUserId the rater
     * @param bookIsbn the book being rated
     * @param bookTitle the book title (for display)
     * @return Optional containing the submitted Rating, or empty if skipped/cancelled
     */
    public static Optional<Rating> showBookRating(int requestId, int currentUserId, 
                                                   String bookIsbn, String bookTitle) {
        try {
            FXMLLoader loader = new FXMLLoader(
                RatingDialogController.class.getResource("/view/RatingDialog.fxml")
            );
            Dialog<Rating> dialog = loader.load();
            RatingDialogController controller = loader.getController();
            
            controller.requestId = requestId;
            controller.currentUserId = currentUserId;
            controller.targetBookIsbn = bookIsbn;
            controller.targetUserId = null;
            controller.targetType = TargetType.BOOK;
            controller.headerText.setText("Rate Book: " + bookTitle);
            
            return dialog.showAndWait();
        } catch (IOException e) {
            System.err.println("[RatingDialog] Failed to load: " + e.getMessage());
            return Optional.empty();
        }
    }
}

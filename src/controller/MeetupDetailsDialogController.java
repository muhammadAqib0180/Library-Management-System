package controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Button;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Dialog;
import javafx.scene.control.Spinner;
import javafx.scene.control.TextArea;
import javafx.scene.text.Text;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Optional;

/**
 * Controller for MeetupDetailsDialog (US-1 Meetup Flow).
 * User enters meetup location and date/time.
 */
public class MeetupDetailsDialogController {

    public static class MeetupDetails {
        public String location;
        public LocalDateTime dateTime;

        public MeetupDetails(String location, LocalDateTime dateTime) {
            this.location = location;
            this.dateTime = dateTime;
        }

        @Override
        public String toString() {
            return "MeetupDetails{" +
                    "location='" + location + '\'' +
                    ", dateTime=" + dateTime +
                    '}';
        }
    }

    @FXML private Dialog<MeetupDetails> dialog;
    @FXML private TextArea locationField;
    @FXML private DatePicker dateField;
    @FXML private Spinner<Integer> hourSpinner;
    @FXML private Spinner<Integer> minuteSpinner;
    @FXML private Button cancelButton;
    @FXML private Button confirmButton;
    @FXML private Text locationError;
    @FXML private Text dateError;
    @FXML private Text timeError;

    private MeetupDetails result = null;

    @FXML
    private void initialize() {
        // Clear error messages when user types
        locationField.textProperty().addListener((o, oldV, newV) -> locationError.setText(""));
        dateField.valueProperty().addListener((o, oldV, newV) -> dateError.setText(""));
        hourSpinner.valueProperty().addListener((o, oldV, newV) -> timeError.setText(""));

        cancelButton.setOnAction(e -> dialog.setResult(null));
        confirmButton.setOnAction(e -> validateAndConfirm());

        dialog.setResultConverter(param -> result);
    }

    private void validateAndConfirm() {
        boolean valid = true;

        // Validate location
        String location = locationField.getText().trim();
        if (location.isEmpty()) {
            locationError.setText("Location is required");
            valid = false;
        }

        // Validate date
        LocalDate selectedDate = dateField.getValue();
        if (selectedDate == null) {
            dateError.setText("Please select a date");
            valid = false;
        } else if (selectedDate.isBefore(LocalDate.now())) {
            dateError.setText("Date must be in the future");
            valid = false;
        }

        // Validate time
        Integer hour = hourSpinner.getValue();
        Integer minute = minuteSpinner.getValue();
        if (hour == null || minute == null) {
            timeError.setText("Please select a time");
            valid = false;
        }

        if (!valid) return;

        // Build result
        LocalDateTime meetupDateTime = LocalDateTime.of(selectedDate, LocalTime.of(hour, minute));
        if (meetupDateTime.isBefore(LocalDateTime.now())) {
            timeError.setText("Meetup time must be in the future");
            return;
        }

        this.result = new MeetupDetails(location, meetupDateTime);
        dialog.setResult(result);
        dialog.close();
    }

    /**
     * Show the meetup details dialog and return the entered details.
     * @return Optional containing MeetupDetails, or empty if cancelled
     */
    public static Optional<MeetupDetails> show() {
        try {
            FXMLLoader loader = new FXMLLoader(
                MeetupDetailsDialogController.class.getResource("/view/MeetupDetailsDialog.fxml")
            );
            Dialog<MeetupDetails> dialog = loader.load();
            return dialog.showAndWait();
        } catch (IOException e) {
            System.err.println("[MeetupDetailsDialog] Failed to load FXML: " + e.getMessage());
            return Optional.empty();
        }
    }
}

package controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Button;
import javafx.scene.control.Dialog;
import javafx.scene.control.TextField;
import javafx.scene.text.Text;
import java.io.IOException;
import java.util.Optional;

/**
 * Controller for OtpEntryDialog (US-1 Meetup Flow).
 * User enters 4-digit OTP to confirm handover.
 */
public class OtpEntryDialogController {

    @FXML private Dialog<String> dialog;
    @FXML private TextField otpField;
    @FXML private Button cancelButton;
    @FXML private Button verifyButton;
    @FXML private Text errorMessage;

    private String expectedOtp;
    private String result = null;

    @FXML
    private void initialize() {
        // Only allow numeric input, max 4 digits
        otpField.textProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal.matches("\\d{0,4}")) {
                otpField.setText(oldVal);
            }
            errorMessage.setText("");
        });

        cancelButton.setOnAction(e -> dialog.setResult(null));
        verifyButton.setOnAction(e -> validateAndConfirm());

        dialog.setResultConverter(param -> result);
    }

    private void validateAndConfirm() {
        String entered = otpField.getText().trim();

        // Validate format
        if (entered.isEmpty()) {
            errorMessage.setText("Please enter the OTP");
            return;
        }

        if (!entered.matches("\\d{4}")) {
            errorMessage.setText("OTP must be exactly 4 digits");
            return;
        }

        // Note: Actual verification happens in the controller calling this dialog
        // This dialog just returns the entered OTP; the caller verifies against DB
        this.result = entered;
        dialog.setResult(result);
        dialog.close();
    }

    /**
     * Show the OTP entry dialog.
     * @param expectedOtp the OTP to verify against (optional; caller can verify)
     * @return Optional containing the entered OTP, or empty if cancelled
     */
    public static Optional<String> show(String expectedOtp) {
        try {
            FXMLLoader loader = new FXMLLoader(
                OtpEntryDialogController.class.getResource("/view/OtpEntryDialog.fxml")
            );
            Dialog<String> dialog = loader.load();
            OtpEntryDialogController controller = loader.getController();
            controller.expectedOtp = expectedOtp;
            return dialog.showAndWait();
        } catch (IOException e) {
            System.err.println("[OtpEntryDialog] Failed to load FXML: " + e.getMessage());
            return Optional.empty();
        }
    }
}

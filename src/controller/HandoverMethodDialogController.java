package controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Button;
import javafx.scene.control.Dialog;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.io.IOException;
import java.util.Optional;

/**
 * Controller for HandoverMethodDialog (US-1).
 * User selects between "Meetup" and "Courier" delivery methods.
 */
public class HandoverMethodDialogController {

    public enum HandoverMethod {
        MEETUP, COURIER
    }

    @FXML private Dialog<HandoverMethod> dialog;
    @FXML private Button cancelButton;
    @FXML private Button meetupButton;
    @FXML private Button courierButton;
    @FXML private VBox meetupBox;
    @FXML private VBox courierBox;

    private HandoverMethod selectedMethod = null;

    @FXML
    private void initialize() {
        meetupButton.setOnAction(e -> selectMethod(HandoverMethod.MEETUP));
        courierButton.setOnAction(e -> selectMethod(HandoverMethod.COURIER));
        cancelButton.setOnAction(e -> dialog.setResult(null));

        // Allow clicking the entire box to select the method
        meetupBox.setOnMouseClicked(e -> selectMethod(HandoverMethod.MEETUP));
        courierBox.setOnMouseClicked(e -> selectMethod(HandoverMethod.COURIER));

        dialog.setResultConverter(param -> selectedMethod);
    }

    private void selectMethod(HandoverMethod method) {
        this.selectedMethod = method;
        dialog.setResult(method);
        dialog.close();
    }

    /**
     * Show the handover method dialog and return the user's choice.
     * @return Optional containing the selected method, or empty if cancelled
     */
    public static Optional<HandoverMethod> show() {
        try {
            FXMLLoader loader = new FXMLLoader(
                HandoverMethodDialogController.class.getResource("/view/HandoverMethodDialog.fxml")
            );
            Dialog<HandoverMethod> dialog = loader.load();
            return dialog.showAndWait();
        } catch (IOException e) {
            System.err.println("[HandoverMethodDialog] Failed to load FXML: " + e.getMessage());
            return Optional.empty();
        }
    }
}

package controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.*;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;
import javafx.stage.Window;
import java.io.File;
import java.io.IOException;
import java.util.Optional;

public class CourierFormDialogController {

    public static class CourierDetails {
        public String service;
        public String personName;
        public String vehiclePlate;
        public String vehicleType;
        public String proofImagePath;

        public CourierDetails(String service, String personName, String vehiclePlate, String vehicleType, String proofImagePath) {
            this.service = service;
            this.personName = personName;
            this.vehiclePlate = vehiclePlate;
            this.vehicleType = vehicleType;
            this.proofImagePath = proofImagePath;
        }
    }

    @FXML private DialogPane dialogPane;
    @FXML private ComboBox<String> serviceCombo;
    @FXML private TextField otherServiceField;
    @FXML private TextField personField;
    @FXML private TextField plateField;
    @FXML private ComboBox<String> vehicleCombo;
    @FXML private TextField filePathField;
    @FXML private Button uploadButton;
    @FXML private Button cancelButton;
    @FXML private Button submitButton;
    @FXML private Text serviceError;
    @FXML private Text personError;
    @FXML private Text plateError;
    @FXML private Text vehicleError;
    @FXML private Text fileError;

    private String selectedFilePath = null;
    private CourierDetails result = null;
    private Dialog<CourierDetails> ownerDialog;

    // This is called right after the FXML loads
    public void setOwnerDialog(Dialog<CourierDetails> ownerDialog) {
        this.ownerDialog = ownerDialog;
        this.ownerDialog.setResultConverter(param -> result);
    }

    @FXML
    private void initialize() {
        // Safe way to populate ComboBoxes!
        serviceCombo.getItems().addAll("inDrive", "Yango", "Other");
        vehicleCombo.getItems().addAll("Bike", "Car", "Van");

        serviceCombo.valueProperty().addListener((obs, oldVal, newVal) -> {
            boolean showOther = "Other".equals(newVal);
            otherServiceField.setVisible(showOther);
            otherServiceField.setManaged(showOther);
            serviceError.setText("");
        });

        personField.textProperty().addListener((o, oldV, newV) -> personError.setText(""));
        plateField.textProperty().addListener((o, oldV, newV) -> plateError.setText(""));
        vehicleCombo.valueProperty().addListener((o, oldV, newV) -> vehicleError.setText(""));

        uploadButton.setOnAction(e -> openFileChooser());
        cancelButton.setOnAction(e -> handleCancel());
        submitButton.setOnAction(e -> validateAndSubmit());
    }

    private void openFileChooser() {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Select Proof Screenshot");
        chooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg", "*.gif")
        );
        // Safely get the window without relying on a null dialog variable
        Window window = dialogPane.getScene() != null ? dialogPane.getScene().getWindow() : null;
        File selected = chooser.showOpenDialog(window);

        if (selected != null) {
            selectedFilePath = selected.getAbsolutePath();
            filePathField.setText(selected.getName());
            fileError.setText("");
        }
    }

    @FXML
    private void validateAndSubmit() {
        boolean valid = true;

        String service = serviceCombo.getValue();
        if (service == null || service.isEmpty()) {
            serviceError.setText("Please select a courier service");
            valid = false;
        } else if ("Other".equals(service)) {
            String otherName = otherServiceField.getText().trim();
            if (otherName.isEmpty()) {
                serviceError.setText("Please enter the courier service name");
                valid = false;
            } else {
                service = otherName;
            }
        }

        String person = personField.getText().trim();
        if (person.isEmpty()) {
            personError.setText("Delivery person's name is required");
            valid = false;
        }

        String plate = plateField.getText().trim();
        if (plate.isEmpty()) {
            plateError.setText("Vehicle number plate is required");
            valid = false;
        }

        String vehicle = vehicleCombo.getValue();
        if (vehicle == null || vehicle.isEmpty()) {
            vehicleError.setText("Please select a vehicle type");
            valid = false;
        }

        if (!valid) return;

        this.result = new CourierDetails(service, person, plate, vehicle, selectedFilePath);

        if (ownerDialog != null) {
            ownerDialog.setResult(result);
            ownerDialog.close();
        }
    }

    @FXML
    private void handleCancel() {
        this.result = null;
        if (ownerDialog != null) {
            ownerDialog.setResult(null);
            ownerDialog.close();
        }
    }

    public static Optional<CourierDetails> show() {
        try {
            FXMLLoader loader = new FXMLLoader(
                    CourierFormDialogController.class.getResource("/view/CourierFormDialog.fxml")
            );
            DialogPane pane = loader.load();
            CourierFormDialogController controller = loader.getController();

            Dialog<CourierDetails> dialog = new Dialog<>();
            dialog.setDialogPane(pane);
            dialog.setTitle("Courier Delivery Details");

            controller.setOwnerDialog(dialog);

            return dialog.showAndWait();
        } catch (IOException e) {
            e.printStackTrace();
            return Optional.empty();
        }
    }
}
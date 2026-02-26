package controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import model.User;
import java.io.IOException;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.scene.control.PasswordField;
import javafx.scene.control.Label;
import javafx.scene.control.Button;
import javafx.scene.control.ToggleButton;
import database.UserDAO;
import database.SQLiteUserDAO;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;

public class LoginController {

    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private Label statusLabel;
    @FXML private ComboBox<String> roleComboBox;
    @FXML private Button submitButton;
    @FXML private ToggleButton loginTab;
    @FXML private ToggleButton signupTab;

    private UserDAO userDAO = new SQLiteUserDAO();

    private static final String ACTIVE_STYLE =
            "-fx-background-color: #3498db; -fx-text-fill: white; " +
            "-fx-font-weight: bold; -fx-font-size: 12px; -fx-cursor: hand;";

    private static final String INACTIVE_STYLE =
            "-fx-background-color: rgba(255,255,255,0.1); -fx-text-fill: rgba(255,255,255,0.7); " +
            "-fx-font-weight: bold; -fx-font-size: 12px; -fx-cursor: hand;";

    @FXML
    public void initialize() {
        roleComboBox.setItems(FXCollections.observableArrayList("Admin", "Member"));
        roleComboBox.setPromptText("Select Role");

        // Press Enter from either field to submit
        usernameField.setOnAction(e -> handleSubmit());
        passwordField.setOnAction(e -> handleSubmit());

        showLoginView();
    }
    public void showLoginView() {
        signupTab.setSelected(false);
        loginTab.setSelected(true);

        loginTab.setStyle(ACTIVE_STYLE   + " -fx-background-radius: 25 0 0 25; -fx-padding: 9 40;");
        signupTab.setStyle(INACTIVE_STYLE + " -fx-background-radius: 0 25 25 0; -fx-padding: 9 40;");

        roleComboBox.setVisible(false);
        roleComboBox.setManaged(false);

        submitButton.setText("SIGN IN");
        submitButton.setStyle("-fx-background-color: linear-gradient(to right, #3498db, #8e44ad); " +
                "-fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 14px; " +
                "-fx-background-radius: 10; -fx-padding: 13 0; -fx-cursor: hand;");
        statusLabel.setText("");
    }

    @FXML
    public void showSignupView() {
        loginTab.setSelected(false);
        signupTab.setSelected(true);

        signupTab.setStyle(ACTIVE_STYLE   + " -fx-background-radius: 0 25 25 0; -fx-padding: 9 40;");
        loginTab.setStyle(INACTIVE_STYLE  + " -fx-background-radius: 25 0 0 25; -fx-padding: 9 40;");

        roleComboBox.setVisible(true);
        roleComboBox.setManaged(true);

        submitButton.setText("CREATE ACCOUNT");
        submitButton.setStyle("-fx-background-color: linear-gradient(to right, #3498db, #8e44ad); " +
                "-fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 14px; " +
                "-fx-background-radius: 10; -fx-padding: 13 0; -fx-cursor: hand;");
        statusLabel.setText("");
    }
    /**
     * Determines which action to take based on the selected tab
     */
    @FXML
    public void handleSubmit() {
        if (loginTab.isSelected()) {
            handleLogin();
        } else {
            handleSignUp();
        }
    }

    private void handleLogin() {
        String user = usernameField.getText();
        String pass = passwordField.getText();

        if (user.isEmpty() || pass.isEmpty()) {
            statusLabel.setTextFill(javafx.scene.paint.Color.RED);
            statusLabel.setText("Please enter credentials.");
            return;
        }

        if (userDAO.validateLogin(user, pass)) {
            model.User loggedInUser = userDAO.getUserByUsername(user);
            if (loggedInUser == null) return;

            if ("Member".equals(loggedInUser.getRole())) {
                navigateTo("/view/LenderDashboard.fxml", "Lender Dashboard",
                        ctrl -> ((LenderDashboardController) ctrl).setUsername(loggedInUser.getUsername()));
            } else if ("Admin".equals(loggedInUser.getRole())) {
                navigateTo("/view/AdminDashboard.fxml", "Admin Dashboard",
                        ctrl -> ((AdminDashboardController) ctrl).setUsername(loggedInUser.getUsername()));
            }
        } else {
            statusLabel.setTextFill(javafx.scene.paint.Color.RED);
            statusLabel.setText("Account not found or invalid password.");
        }
    }

    private void navigateTo(String fxmlPath, String title, BaseDashboardController.ControllerCallback callback) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent root = loader.load();
            callback.accept(loader.getController());

            Stage stage = (Stage) submitButton.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle(title);
            stage.show();
        } catch (IOException e) {
            statusLabel.setTextFill(javafx.scene.paint.Color.RED);
            statusLabel.setText("Failed to load dashboard.");
            e.printStackTrace();
        }
    }
    private void handleSignUp() {
        String user = usernameField.getText();
        String pass = passwordField.getText();
        String role = roleComboBox.getValue();

        if (user.isEmpty() || pass.isEmpty() || role == null) {
            statusLabel.setTextFill(javafx.scene.paint.Color.RED);
            statusLabel.setText("Please fill all fields and select a role.");
            return;
        }

        model.User newUser = new model.User(user, pass, role);
        if (userDAO.addUser(newUser)) {
            statusLabel.setTextFill(javafx.scene.paint.Color.BLUE);
            statusLabel.setText("Account created as " + role + "!");
        } else {
            statusLabel.setTextFill(javafx.scene.paint.Color.RED);
            statusLabel.setText("Username already exists.");
        }
    }
}
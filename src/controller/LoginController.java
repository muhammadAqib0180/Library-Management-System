package controller;

import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.scene.control.PasswordField;
import javafx.scene.control.Label;
import javafx.scene.control.Button;
import javafx.scene.control.ToggleButton;
import database.UserDAO;
import database.SQLiteUserDAO;

public class LoginController {

    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private Label statusLabel;
    @FXML private ComboBox<String> roleComboBox;
    @FXML private Button submitButton;
    @FXML private ToggleButton loginTab;
    @FXML private ToggleButton signupTab;

    private UserDAO userDAO = new SQLiteUserDAO();

    /**
     * Switches the UI to Login Mode
     */
    @FXML
    public void showLoginView() {
        signupTab.setSelected(false);
        loginTab.setSelected(true);

        // Hide the role selection for login
        roleComboBox.setVisible(false);
        roleComboBox.setManaged(false);

        submitButton.setText("SIGN IN");
        submitButton.setStyle("-fx-background-color: #2ecc71; -fx-text-fill: white; -fx-background-radius: 25; -fx-font-weight: bold; -fx-padding: 12 60;");
        statusLabel.setText("");
    }

    /**
     * Switches the UI to Sign Up Mode
     */
    @FXML
    public void showSignupView() {
        loginTab.setSelected(false);
        signupTab.setSelected(true);

        // Show the role selection for registration
        roleComboBox.setVisible(true);
        roleComboBox.setManaged(true);

        submitButton.setText("CREATE ACCOUNT");
        submitButton.setStyle("-fx-background-color: #3498db; -fx-text-fill: white; -fx-background-radius: 25; -fx-font-weight: bold; -fx-padding: 12 60;");
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
            statusLabel.setTextFill(javafx.scene.paint.Color.GREEN);
            statusLabel.setText("Successfully Logged In!");
        } else {
            statusLabel.setTextFill(javafx.scene.paint.Color.RED);
            statusLabel.setText("Account not found or invalid password.");
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
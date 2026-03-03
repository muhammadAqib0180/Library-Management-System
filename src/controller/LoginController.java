package controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.scene.control.PasswordField;
import javafx.scene.control.Label;
import javafx.scene.control.Button;
import javafx.scene.control.ToggleButton;
import javafx.scene.shape.Circle;
import database.UserDAO;
import database.SupaUserDAO;
import javafx.collections.FXCollections;
import javafx.concurrent.Task;

public class LoginController {

    @FXML
    private TextField usernameField;
    @FXML
    private PasswordField passwordField;
    @FXML
    private TextField passwordVisibleField;
    @FXML
    private ToggleButton togglePasswordBtn;
    @FXML
    private Label statusLabel;
    @FXML
    private Circle systemStatusCircle;
    @FXML
    private ComboBox<String> roleComboBox;
    @FXML
    private Button submitButton;
    @FXML
    private ToggleButton loginTab;
    @FXML
    private ToggleButton signupTab;

    private UserDAO userDAO = new SupaUserDAO();

    private static final String ACTIVE_STYLE = "-fx-background-color: #3498db; -fx-text-fill: white; " +
            "-fx-font-weight: bold; -fx-font-size: 12px; -fx-cursor: hand;";

    private static final String INACTIVE_STYLE = "-fx-background-color: rgba(255,255,255,0.1); -fx-text-fill: rgba(255,255,255,0.7); "
            +
            "-fx-font-weight: bold; -fx-font-size: 12px; -fx-cursor: hand;";

    @FXML
    public void initialize() {
        roleComboBox.setItems(FXCollections.observableArrayList("Admin", "Member"));
        roleComboBox.setPromptText("Select Role");

        // Press Enter from either field to submit
        usernameField.setOnAction(e -> handleSubmit());
        passwordField.setOnAction(e -> handleSubmit());
        if (passwordVisibleField != null) {
            passwordVisibleField.setOnAction(e -> handleSubmit());
            passwordVisibleField.textProperty().bindBidirectional(passwordField.textProperty());
        }

        showLoginView();

        // Pre-warm DB connection in background so first login isn't slow
        new Thread(() -> {
            try {
                database.DatabaseConnection.getConnection();
            } catch (Exception ignored) {
            }
        }, "db-warmup").start();

        startStatusCheck();
    }

    @FXML
    public void togglePasswordVisibility() {
        if (togglePasswordBtn.isSelected()) {
            passwordVisibleField.setVisible(true);
            passwordVisibleField.setManaged(true);
            passwordField.setVisible(false);
            passwordField.setManaged(false);
        } else {
            passwordVisibleField.setVisible(false);
            passwordVisibleField.setManaged(false);
            passwordField.setVisible(true);
            passwordField.setManaged(true);
        }
    }

    private void startStatusCheck() {
        if (systemStatusCircle == null)
            return;
        javafx.animation.Timeline timeline = new javafx.animation.Timeline(
                new javafx.animation.KeyFrame(javafx.util.Duration.seconds(3), e -> checkSystemStatus()));
        timeline.setCycleCount(javafx.animation.Animation.INDEFINITE);
        timeline.play();
        checkSystemStatus();
    }

    private void checkSystemStatus() {
        javafx.concurrent.Task<Boolean> task = new javafx.concurrent.Task<>() {
            @Override
            protected Boolean call() {
                try {
                    java.sql.Connection conn = database.DatabaseConnection.getConnection();
                    boolean valid = conn.isValid(2);
                    conn.close();
                    return valid;
                } catch (Exception ex) {
                    return false;
                }
            }
        };
        task.setOnSucceeded(e -> {
            if (task.getValue()) {
                systemStatusCircle.setFill(javafx.scene.paint.Color.rgb(46, 204, 113));
            } else {
                systemStatusCircle.setFill(javafx.scene.paint.Color.rgb(231, 76, 60));
            }
        });
        task.setOnFailed(e -> {
            systemStatusCircle.setFill(javafx.scene.paint.Color.rgb(231, 76, 60));
        });
        new Thread(task).start();
    }

    public void showLoginView() {
        signupTab.setSelected(false);
        loginTab.setSelected(true);

        loginTab.setStyle(ACTIVE_STYLE + " -fx-background-radius: 25 0 0 25; -fx-padding: 9 40;");
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

        signupTab.setStyle(ACTIVE_STYLE + " -fx-background-radius: 0 25 25 0; -fx-padding: 9 40;");
        loginTab.setStyle(INACTIVE_STYLE + " -fx-background-radius: 25 0 0 25; -fx-padding: 9 40;");

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

        submitButton.setDisable(true);
        statusLabel.setTextFill(javafx.scene.paint.Color.WHITE);
        statusLabel.setText("Signing in...");

        Task<model.User> loginTask = new Task<>() {
            @Override
            protected model.User call() {
                // Single DB call — gets user and validates password AND active status
                model.User found = userDAO.getUserByUsername(user);
                if (found != null && found.getPassword().equals(pass) && found.isActive()) {
                    return found;
                }
                return null;
            }
        };

        loginTask.setOnSucceeded(e -> {
            model.User loggedInUser = loginTask.getValue();
            submitButton.setDisable(false);
            if (loggedInUser == null) {
                statusLabel.setTextFill(javafx.scene.paint.Color.RED);
                statusLabel.setText("Account not found or invalid password.");
                return;
            }

            // Register Last Login Audit
            new Thread(() -> userDAO.updateLastLogin(loggedInUser.getId())).start();

            if ("Member".equals(loggedInUser.getRole())) {
                navigateTo("/view/LenderDashboard.fxml", "Lender Dashboard",
                        ctrl -> ((LenderDashboardController) ctrl).setUsername(loggedInUser.getUsername(),
                                loggedInUser.getId()));
            } else if ("Admin".equals(loggedInUser.getRole())) {
                navigateTo("/view/AdminDashboard.fxml", "Admin Dashboard",
                        ctrl -> ((AdminDashboardController) ctrl).setUsername(loggedInUser.getUsername(),
                                loggedInUser.getId()));
            }
        });

        loginTask.setOnFailed(e -> {
            submitButton.setDisable(false);
            statusLabel.setTextFill(javafx.scene.paint.Color.RED);
            statusLabel.setText("Connection error. Try again.");
        });

        new Thread(loginTask).start();
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
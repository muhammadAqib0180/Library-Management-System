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

    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private TextField passwordVisibleField;
    @FXML private ToggleButton togglePasswordBtn;
    @FXML private Label statusLabel;
    @FXML private Circle systemStatusCircle;
    @FXML private ComboBox<String> roleComboBox;
    @FXML private Button submitButton;
    @FXML private ToggleButton loginTab;
    @FXML private ToggleButton signupTab;

    private UserDAO userDAO = new SupaUserDAO();

    // Clean Minimalist Styles
    // Active tab gets a stark black underline and bold text
    // High-Contrast Glassmorphism Styles
    private static final String ACTIVE_STYLE = "-fx-background-color: transparent; -fx-text-fill: #F8FAFC; " +
            "-fx-font-weight: 800; -fx-font-size: 14px; -fx-cursor: hand; -fx-border-color: transparent transparent #F8FAFC transparent; -fx-border-width: 0 0 2 0; -fx-padding: 0 4 4 4;";

    private static final String INACTIVE_STYLE = "-fx-background-color: transparent; -fx-text-fill: #64748B; " +
            "-fx-font-weight: normal; -fx-font-size: 14px; -fx-cursor: hand; -fx-border-color: transparent; -fx-padding: 0 4 4 4;";

    private static final String SUBMIT_BTN_STYLE = "-fx-background-color: #F8FAFC; -fx-text-fill: #0F172A; " +
            "-fx-font-weight: 800; -fx-font-size: 14px; -fx-padding: 14; -fx-background-radius: 8; -fx-cursor: hand;";

    @FXML
    public void initialize() {
        roleComboBox.setItems(FXCollections.observableArrayList("Admin", "Member"));
        roleComboBox.setPromptText("Select Role");

        usernameField.setOnAction(e -> handleSubmit());
        passwordField.setOnAction(e -> handleSubmit());
        if (passwordVisibleField != null) {
            passwordVisibleField.setOnAction(e -> handleSubmit());
            passwordVisibleField.textProperty().bindBidirectional(passwordField.textProperty());
        }

        showLoginView();

        new Thread(() -> {
            try {
                database.DatabaseConnection.getConnection();
            } catch (Exception ignored) {}
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
            togglePasswordBtn.setText("Hide");
        } else {
            passwordVisibleField.setVisible(false);
            passwordVisibleField.setManaged(false);
            passwordField.setVisible(true);
            passwordField.setManaged(true);
            togglePasswordBtn.setText("Show");
        }
    }

    private void startStatusCheck() {
        if (systemStatusCircle == null) return;
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
                systemStatusCircle.setFill(javafx.scene.paint.Color.web("#10B981")); // Emerald Green
            } else {
                systemStatusCircle.setFill(javafx.scene.paint.Color.web("#EF4444")); // Red
            }
        });
        task.setOnFailed(e -> {
            systemStatusCircle.setFill(javafx.scene.paint.Color.web("#EF4444"));
        });
        new Thread(task).start();
    }

    public void showLoginView() {
        signupTab.setSelected(false);
        loginTab.setSelected(true);

        loginTab.setStyle(ACTIVE_STYLE);
        signupTab.setStyle(INACTIVE_STYLE);

        roleComboBox.setVisible(false);
        roleComboBox.setManaged(false);

        submitButton.setText("Sign In");
        submitButton.setStyle(SUBMIT_BTN_STYLE);
        statusLabel.setText("");
    }

    @FXML
    public void showSignupView() {
        loginTab.setSelected(false);
        signupTab.setSelected(true);

        signupTab.setStyle(ACTIVE_STYLE);
        loginTab.setStyle(INACTIVE_STYLE);

        roleComboBox.setVisible(true);
        roleComboBox.setManaged(true);

        submitButton.setText("Create Account");
        submitButton.setStyle(SUBMIT_BTN_STYLE);
        statusLabel.setText("");
    }

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
            statusLabel.setTextFill(javafx.scene.paint.Color.web("#EF4444"));
            statusLabel.setText("Please enter both username and password.");
            return;
        }

        submitButton.setDisable(true);
        statusLabel.setTextFill(javafx.scene.paint.Color.web("#6B7280"));
        statusLabel.setText("Signing in...");

        Task<model.User> loginTask = new Task<>() {
            @Override
            protected model.User call() {
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
                statusLabel.setTextFill(javafx.scene.paint.Color.web("#EF4444"));
                statusLabel.setText("Incorrect username or password.");
                return;
            }

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
            statusLabel.setTextFill(javafx.scene.paint.Color.web("#EF4444"));
            statusLabel.setText("Network error. Please try again.");
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
            statusLabel.setTextFill(javafx.scene.paint.Color.web("#EF4444"));
            statusLabel.setText("Failed to load dashboard.");
            e.printStackTrace();
        }
    }

    private void handleSignUp() {
        String user = usernameField.getText();
        String pass = passwordField.getText();
        String role = roleComboBox.getValue();

        if (user.isEmpty() || pass.isEmpty() || role == null) {
            statusLabel.setTextFill(javafx.scene.paint.Color.web("#EF4444"));
            statusLabel.setText("Please fill all fields and select a role.");
            return;
        }

        model.User newUser = new model.User(user, pass, role);
        if (userDAO.addUser(newUser)) {
            statusLabel.setTextFill(javafx.scene.paint.Color.web("#10B981"));
            statusLabel.setText("Account created successfully. You can now sign in.");
        } else {
            statusLabel.setTextFill(javafx.scene.paint.Color.web("#EF4444"));
            statusLabel.setText("That username is already taken.");
        }
    }
}
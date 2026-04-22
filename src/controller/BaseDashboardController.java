package controller;

import database.SupaUserDAO;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.stage.Stage;
import util.AlertHelper;

import java.io.IOException;

public abstract class BaseDashboardController {

    @FXML
    protected javafx.scene.shape.Circle systemStatusCircle;

    @FXML
    protected Label welcomeLabel;

    protected String currentUsername;
    protected int currentUserId;

    /**
     * Called after login to pass the logged-in username and user ID.
     * Subclasses can override but should call super.setUsername(username, userId).
     */
    public void setUsername(String username, int userId) {
        this.currentUsername = username;
        this.currentUserId = userId;
        if (welcomeLabel != null) {
            welcomeLabel.setText("Welcome, " + username);
        }
    }

    /**
     * Backward compatibility method - fetch user ID from database
     */
    public void setUsername(String username) {
        this.currentUsername = username;
        try {
            SupaUserDAO userDAO = new SupaUserDAO();
            model.User user = userDAO.getUserByUsername(username);
            if (user != null) {
                this.currentUserId = user.getId();
            }
        } catch (Exception ignored) {
        }
        if (welcomeLabel != null) {
            welcomeLabel.setText("Welcome, " + username);
        }
    }

    protected void startStatusCheck() {
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

    /**
     * Logs out and returns to the Login screen.
     * Inherited by ALL dashboards.
     */
    @FXML
    protected void handleLogout() {
        Stage stage = (Stage) welcomeLabel.getScene().getWindow();

        boolean ok = AlertHelper.confirm(
                stage, "Logout",
                "Are you sure you want to logout?",
                "You will be returned to the login screen.",
                "Logout");
        if (!ok)
            return;

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/LoginScreen.fxml"));
            Parent root = loader.load();
            stage.setScene(new Scene(root));
            stage.setTitle("Library Nexus — Sign In");
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Shared navigation utility for subclasses.
     */
    protected void navigateTo(String fxmlPath, String title, ControllerCallback callback) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent root = loader.load();

            if (callback != null) {
                callback.accept(loader.getController());
            }

            Stage stage = (Stage) welcomeLabel.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle(title);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }



    @FunctionalInterface
    protected interface ControllerCallback {
        void accept(Object controller);
    }
}
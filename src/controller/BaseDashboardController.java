package controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.stage.Stage;

import java.io.IOException;

public abstract class BaseDashboardController {

    @FXML protected Label welcomeLabel;

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
            database.SQLiteUserDAO userDAO = new database.SQLiteUserDAO();
            model.User user = userDAO.getUserByUsername(username);
            if (user != null) {
                this.currentUserId = user.getId();
            }
        } catch (Exception ignored) {}
        if (welcomeLabel != null) {
            welcomeLabel.setText("Welcome, " + username);
        }
    }

    /**
     * Logs out and returns to the Login screen.
     * Inherited by ALL dashboards.
     */
    @FXML
    protected void handleLogout() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/LoginScreen.fxml"));
            Parent root = loader.load();

            Stage stage = (Stage) welcomeLabel.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Library Management System - Login");
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
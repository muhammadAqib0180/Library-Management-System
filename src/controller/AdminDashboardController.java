package controller;

import database.SupaUserDAO;
import database.UserDAO;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.SimpleIntegerProperty;
import model.User;
import util.AlertHelper;

public class AdminDashboardController extends BaseDashboardController {

    @FXML
    private Button btnManageUsers;
    @FXML
    private VBox welcomePanel;
    @FXML
    private VBox manageUsersPanel;
    @FXML
    private TableView<User> userTableView;
    @FXML
    private TableColumn<User, Integer> idColumn;
    @FXML
    private TableColumn<User, String> usernameColumn;
    @FXML
    private TableColumn<User, String> roleColumn;
    @FXML
    private TableColumn<User, String> statusColumn;
    @FXML
    private TableColumn<User, String> lastLoginColumn;
    @FXML
    private Button deactivateUserBtn;
    @FXML
    private Label adminStatusLabel;

    private final UserDAO userDAO = new SupaUserDAO();
    private final ObservableList<User> userList = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        idColumn.setCellValueFactory(data -> new SimpleIntegerProperty(data.getValue().getId()).asObject());
        usernameColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getUsername()));
        roleColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getRole()));
        statusColumn.setCellValueFactory(
                data -> new SimpleStringProperty(data.getValue().isActive() ? "Active" : "Inactive"));
        lastLoginColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getLastLogin()));

        deactivateUserBtn.setOnAction(e -> {
            User selected = userTableView.getSelectionModel().getSelectedItem();
            if (selected != null)
                handleToggleUserStatus(selected);
        });

        userTableView.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            boolean hasSelection = newVal != null;
            deactivateUserBtn.setDisable(!hasSelection);

            if (hasSelection) {
                if (newVal.isActive()) {
                    deactivateUserBtn.setText("\uD83D\uDEAB Deactivate User");
                    deactivateUserBtn
                            .setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white; -fx-font-weight: bold; " +
                                    "-fx-padding: 10 20; -fx-background-radius: 8; -fx-font-size: 12px; -fx-cursor: hand;");
                } else {
                    deactivateUserBtn.setText("\u2713 Activate User");
                    deactivateUserBtn
                            .setStyle("-fx-background-color: #27ae60; -fx-text-fill: white; -fx-font-weight: bold; " +
                                    "-fx-padding: 10 20; -fx-background-radius: 8; -fx-font-size: 12px; -fx-cursor: hand;");
                }
            }
        });

        userTableView.setItems(userList);
        startStatusCheck();
    }

    @FXML
    public void showManageUsers() {
        manageUsersPanel.setVisible(true);
        manageUsersPanel.setManaged(true);
        welcomePanel.setVisible(false);
        welcomePanel.setManaged(false);

        btnManageUsers.setStyle(
                "-fx-background-color: rgba(0,0,0,0.2); -fx-text-fill: white; " +
                        "-fx-font-weight: bold; -fx-font-size: 13px; -fx-padding: 14 20; " +
                        "-fx-cursor: hand; -fx-background-radius: 0; " +
                        "-fx-border-color: transparent transparent transparent #bdc3c7; " +
                        "-fx-border-width: 0 0 0 3; -fx-alignment: CENTER_LEFT;");

        userList.clear();
        userList.addAll(userDAO.getAllUsers());
    }

    private void handleToggleUserStatus(User user) {
        boolean newStatus = !user.isActive();

        // Red button for deactivate, blue/purple for reactivate
        boolean ok = newStatus
                ? AlertHelper.confirm(userTableView.getScene().getWindow(),
                        "Activate User", "Activate this user?",
                        "Do you want to reactivate: " + user.getUsername() + "?", "Activate")
                : AlertHelper.danger(userTableView.getScene().getWindow(),
                        "Deactivate User", "Deactivate this user?",
                        "Do you want to deactivate: " + user.getUsername() + "?", "Deactivate");

        if (ok) {
            if (userDAO.updateUserStatus(user.getId(), newStatus)) {
                adminStatusLabel.setTextFill(javafx.scene.paint.Color.GREEN);
                adminStatusLabel
                        .setText("User " + user.getUsername() + " is now " + (newStatus ? "Active" : "Inactive"));
                userList.clear();
                userList.addAll(userDAO.getAllUsers());
                userTableView.refresh();
            } else {
                adminStatusLabel.setTextFill(javafx.scene.paint.Color.RED);
                adminStatusLabel.setText("Failed to update user status.");
            }
        }
    }
}
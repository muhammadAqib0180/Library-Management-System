package controller;

import javafx.fxml.FXML;
import javafx.scene.control.TableView;
import javafx.scene.control.TableColumn;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import model.User;
import database.UserDAO;
import database.SQLiteUserDAO;

public class AdminDashboardController extends BaseDashboardController {
    @FXML private TableView<User> userTableView;
    @FXML private TableColumn<User, Integer> idColumn;
    @FXML private TableColumn<User, String> usernameColumn;
    @FXML private TableColumn<User, String> roleColumn;
    @FXML private TableColumn<User, String> statusColumn;

    private UserDAO userDAO = new SQLiteUserDAO();

    @FXML
    public void initialize() {
        System.out.println("[AdminDashboardController] Initializing TableView...");
        userTableView.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        usernameColumn.setCellValueFactory(new PropertyValueFactory<>("username"));
        roleColumn.setCellValueFactory(new PropertyValueFactory<>("role"));
        // For now, status is a placeholder (could be extended later)
        statusColumn.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty("Active"));

        java.util.List<User> userList = userDAO.getAllUsers();
        System.out.println("[AdminDashboardController] Users loaded from DB: " + userList.size());
        for (User u : userList) {
            System.out.println("[AdminDashboardController] User: id=" + u.getId() + ", username=" + u.getUsername() + ", role=" + u.getRole());
        }
        ObservableList<User> users = FXCollections.observableArrayList(userList);
        userTableView.setItems(users);
        userTableView.refresh();
    }
}
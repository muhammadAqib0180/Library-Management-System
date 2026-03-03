package model;

import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.SimpleBooleanProperty;

public class User {
    private final SimpleIntegerProperty id;
    private final SimpleStringProperty username;
    private final SimpleStringProperty password;
    private final SimpleStringProperty role;
    private final SimpleBooleanProperty active;
    private final SimpleStringProperty lastLogin;

    public User(int id, String username, String password, String role, boolean active, String lastLogin) {
        this.id = new SimpleIntegerProperty(id);
        this.username = new SimpleStringProperty(username);
        this.password = new SimpleStringProperty(password);
        this.role = new SimpleStringProperty(role);
        this.active = new SimpleBooleanProperty(active);
        this.lastLogin = new SimpleStringProperty(lastLogin == null ? "Never" : lastLogin);
    }

    public User(int id, String username, String password, String role, boolean active) {
        this(id, username, password, role, active, "Never");
    }

    public User(int id, String username, String password, String role) {
        this(id, username, password, role, true, "Never");
    }

    public User(String username, String password, String role) {
        this(-1, username, password, role, true, "Never");
    }

    // Getters for JavaFX TableView
    public int getId() {
        return id.get();
    }

    public SimpleIntegerProperty idProperty() {
        return id;
    }

    public String getUsername() {
        return username.get();
    }

    public SimpleStringProperty usernameProperty() {
        return username;
    }

    public String getPassword() {
        return password.get();
    }

    public SimpleStringProperty passwordProperty() {
        return password;
    }

    public String getRole() {
        return role.get();
    }

    public SimpleStringProperty roleProperty() {
        return role;
    }

    public boolean isActive() {
        return active.get();
    }

    public SimpleBooleanProperty activeProperty() {
        return active;
    }

    public String getLastLogin() {
        return lastLogin.get();
    }

    public SimpleStringProperty lastLoginProperty() {
        return lastLogin;
    }
}
package model;

import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;

public class User {
    private final SimpleIntegerProperty id;
    private final SimpleStringProperty username;
    private final SimpleStringProperty password;
    private final SimpleStringProperty role;

    public User(int id, String username, String password, String role) {
        this.id = new SimpleIntegerProperty(id);
        this.username = new SimpleStringProperty(username);
        this.password = new SimpleStringProperty(password);
        this.role = new SimpleStringProperty(role);
    }

    public User(String username, String password, String role) {
        this(-1, username, password, role);
    }

    // Getters for JavaFX TableView
    public int getId() { return id.get(); }
    public SimpleIntegerProperty idProperty() { return id; }
    public String getUsername() { return username.get(); }
    public SimpleStringProperty usernameProperty() { return username; }
    public String getPassword() { return password.get(); }
    public SimpleStringProperty passwordProperty() { return password; }
    public String getRole() { return role.get(); }
    public SimpleStringProperty roleProperty() { return role; }
}
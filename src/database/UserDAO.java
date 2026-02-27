package database;

import model.User;

public interface UserDAO {
    boolean addUser(User user);
    User getUserByUsername(String username);
    boolean validateLogin(String username, String password);
    java.util.List<model.User> getAllUsers();
}
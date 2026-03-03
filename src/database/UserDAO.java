package database;

import model.User;

public interface UserDAO {
    boolean addUser(User user);

    User getUserByUsername(String username);

    boolean validateLogin(String username, String password);

    java.util.List<model.User> getAllUsers();

    boolean updateUserStatus(int userId, boolean active);

    int findUserIdByUsername(String username);

    void updateLastLogin(int userId);
}
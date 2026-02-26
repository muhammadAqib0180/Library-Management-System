package database;

import model.User;

import java.sql.*;

public class SQLiteUserDAO implements UserDAO {
    private String url = "jdbc:sqlite:library.db";

    @Override
    public boolean addUser(User user) {
        String sql = "INSERT INTO Users(username, password, role) VALUES(?,?,?)";

        try (Connection conn = DriverManager.getConnection("jdbc:sqlite:library.db");
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, user.getUsername());
            pstmt.setString(2, user.getPassword());
            pstmt.setString(3, user.getRole()); // Usually "Member" by default

            pstmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            // This will fail if the username already exists (if username is a UNIQUE column)
            System.out.println("Error adding user: " + e.getMessage());
            return false;
        }
    }

    @Override
    public User getUserByUsername(String username) {
        String sql = "SELECT * FROM Users WHERE username = ?";
        try (Connection conn = DriverManager.getConnection(url);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, username);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return new User(rs.getString("username"), rs.getString("password"), rs.getString("role"));
            }
        } catch (SQLException e) { System.out.println(e.getMessage()); }
        return null;
    }

    @Override
    public boolean validateLogin(String username, String password) {
        User user = getUserByUsername(username);
        return user != null && user.getPassword().equals(password);
    }
}
package database;

import model.User;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class SupaUserDAO implements UserDAO {

    @Override
    public boolean addUser(User user) {
        String sql = "INSERT INTO users(username, password, role, active) VALUES(?,?,?,?)";
        try (Connection conn = DatabaseHandler.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, user.getUsername());
            pstmt.setString(2, user.getPassword());
            pstmt.setString(3, user.getRole());
            pstmt.setBoolean(4, true);
            pstmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            // If active column doesn't exist yet, try without it
            if (e.getMessage().contains("active")) {
                String sqlFallback = "INSERT INTO users(username, password, role) VALUES(?,?,?)";
                try (Connection conn = DatabaseHandler.getConnection();
                        PreparedStatement pstmt = conn.prepareStatement(sqlFallback)) {
                    pstmt.setString(1, user.getUsername());
                    pstmt.setString(2, user.getPassword());
                    pstmt.setString(3, user.getRole());
                    pstmt.executeUpdate();
                    System.out.println("User added (without active column - will be added in migration)");
                    return true;
                } catch (SQLException e2) {
                    System.out.println("Error adding user: " + e2.getMessage());
                    return false;
                }
            }
            System.out.println("Error adding user: " + e.getMessage());
            return false;
        }
    }

    @Override
    public User getUserByUsername(String username) {
        String sql = "SELECT * FROM Users WHERE username = ?";
        try (Connection conn = DatabaseHandler.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, username);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                boolean active = true;
                String lastLogin = "Never";
                try {
                    active = rs.getBoolean("active");
                    lastLogin = rs.getString("last_login");
                    if (lastLogin == null)
                        lastLogin = "Never";
                } catch (SQLException e) {
                    // Column doesn't exist yet, default to active and Never
                    System.out.println("Warning: 'active' or 'last_login' column not found, defaulting.");
                }
                return new User(rs.getInt("id"), rs.getString("username"),
                        rs.getString("password"), rs.getString("role"), active, lastLogin);
            }
        } catch (SQLException e) {
            System.out.println("Error fetching user: " + e.getMessage());
        }
        return null;
    }

    public User getUserById(int userId) {
        String sql = "SELECT * FROM Users WHERE id = ?";
        try (Connection conn = DatabaseHandler.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, userId);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                boolean active = true;
                String lastLogin = "Never";
                try {
                    active = rs.getBoolean("active");
                    lastLogin = rs.getString("last_login");
                    if (lastLogin == null)
                        lastLogin = "Never";
                } catch (SQLException e) {
                    System.out.println("Warning: 'active' or 'last_login' column not found, defaulting.");
                }
                return new User(rs.getInt("id"), rs.getString("username"),
                        rs.getString("password"), rs.getString("role"), active, lastLogin);
            }
        } catch (SQLException e) {
            System.out.println("Error fetching user by ID: " + e.getMessage());
        }
        return null;
    }

    @Override
    public boolean validateLogin(String username, String password) {
        User user = getUserByUsername(username);
        return user != null && user.getPassword().equals(password) && user.isActive();
    }

    @Override
    public List<User> getAllUsers() {
        List<User> users = new ArrayList<>();
        String sql = "SELECT * FROM users";
        System.out.println("[SQLiteUserDAO] Fetching all users from DB...");
        try (Connection conn = DatabaseHandler.getConnection();
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                boolean active = true;
                String lastLogin = "Never";
                try {
                    active = rs.getBoolean("active");
                    lastLogin = rs.getString("last_login");
                    if (lastLogin == null)
                        lastLogin = "Never";
                } catch (SQLException e) {
                    // Column doesn't exist yet, default to active
                    System.out.println("Warning: 'active' or 'last_login' column not found, defaulting.");
                }
                User user = new User(
                        rs.getInt("id"),
                        rs.getString("username"),
                        rs.getString("password"),
                        rs.getString("role"),
                        active,
                        lastLogin);
                System.out.println("[SQLiteUserDAO] Found user: id=" + user.getId()
                        + ", username=" + user.getUsername() + ", role=" + user.getRole()
                        + ", active=" + user.isActive());
                users.add(user);
            }
        } catch (SQLException e) {
            System.out.println("[SQLiteUserDAO] Error fetching users: " + e.getMessage());
        }
        System.out.println("[SQLiteUserDAO] Returning " + users.size() + " users.");
        return users;
    }

    @Override
    public boolean updateUserStatus(int userId, boolean active) {
        String sql = "UPDATE users SET active = ? WHERE id = ?";
        try (Connection conn = DatabaseHandler.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setBoolean(1, active);
            pstmt.setInt(2, userId);
            pstmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            if (e.getMessage().contains("active")) {
                System.out.println("Warning: 'active' column doesn't exist yet. Run schema migration.");
            }
            System.out.println("Error updating user status: " + e.getMessage());
            return false;
        }
    }

    @Override
    public int findUserIdByUsername(String username) {
        User user = getUserByUsername(username);
        return user != null ? user.getId() : -1;
    }

    @Override
    public void updateLastLogin(int userId) {
        String sql = "UPDATE users SET last_login = CURRENT_TIMESTAMP WHERE id = ?";
        try (Connection conn = DatabaseHandler.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, userId);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.out.println("Error updating last login (might need migration): " + e.getMessage());
        }
    }
}
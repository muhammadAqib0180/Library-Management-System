package database;

import model.Notification;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class SupaNotificationDAO implements NotificationDAO {

    @Override
    public int createNotification(Notification notification) {
        String sql = """
            INSERT INTO notifications 
            (user_id, type, title, message, related_book_isbn, related_user_id, is_read, action_url, created_at)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, NOW())
            RETURNING notification_id
        """;
        
        try (Connection conn = DatabaseHandler.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, notification.getUserId());
            pstmt.setString(2, notification.getType());
            pstmt.setString(3, notification.getTitle());
            pstmt.setString(4, notification.getMessage());
            pstmt.setString(5, notification.getRelatedBookIsbn());
            
            if (notification.getRelatedUserId() != null) {
                pstmt.setInt(6, notification.getRelatedUserId());
            } else {
                pstmt.setNull(6, Types.INTEGER);
            }
            
            pstmt.setBoolean(7, notification.isRead());
            pstmt.setString(8, notification.getActionUrl());
            
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("notification_id");
                }
            }
        } catch (SQLException e) {
            System.err.println("Error creating notification: " + e.getMessage());
        }
        return -1;
    }

    @Override
    public List<Notification> getUnreadNotifications(int userId) {
        List<Notification> notifications = new ArrayList<>();
        String sql = """
            SELECT notification_id, user_id, type, title, message, related_book_isbn, 
                   related_user_id, is_read, created_at, action_url
            FROM notifications
            WHERE user_id = ? AND is_read = FALSE
            ORDER BY created_at DESC
        """;
        
        try (Connection conn = DatabaseHandler.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, userId);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    Notification notif = mapResultSetToNotification(rs);
                    notifications.add(notif);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error fetching unread notifications: " + e.getMessage());
        }
        return notifications;
    }

    @Override
    public List<Notification> getAllNotifications(int userId) {
        List<Notification> notifications = new ArrayList<>();
        String sql = """
            SELECT notification_id, user_id, type, title, message, related_book_isbn, 
                   related_user_id, is_read, created_at, action_url
            FROM notifications
            WHERE user_id = ?
            ORDER BY created_at DESC
        """;
        
        try (Connection conn = DatabaseHandler.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, userId);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    Notification notif = mapResultSetToNotification(rs);
                    notifications.add(notif);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error fetching all notifications: " + e.getMessage());
        }
        return notifications;
    }

    @Override
    public List<Notification> getNotificationsPaginated(int userId, int limit, int offset) {
        List<Notification> notifications = new ArrayList<>();
        String sql = """
            SELECT notification_id, user_id, type, title, message, related_book_isbn, 
                   related_user_id, is_read, created_at, action_url
            FROM notifications
            WHERE user_id = ?
            ORDER BY created_at DESC
            LIMIT ? OFFSET ?
        """;
        
        try (Connection conn = DatabaseHandler.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, userId);
            pstmt.setInt(2, limit);
            pstmt.setInt(3, offset);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    Notification notif = mapResultSetToNotification(rs);
                    notifications.add(notif);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error fetching paginated notifications: " + e.getMessage());
        }
        return notifications;
    }

    @Override
    public boolean markAsRead(int notificationId) {
        String sql = """
            UPDATE notifications
            SET is_read = TRUE
            WHERE notification_id = ?
        """;
        
        try (Connection conn = DatabaseHandler.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, notificationId);
            int rowsAffected = pstmt.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            System.err.println("Error marking notification as read: " + e.getMessage());
            return false;
        }
    }

    @Override
    public boolean markAllAsRead(int userId) {
        String sql = """
            UPDATE notifications
            SET is_read = TRUE
            WHERE user_id = ? AND is_read = FALSE
        """;
        
        try (Connection conn = DatabaseHandler.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, userId);
            int rowsAffected = pstmt.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            System.err.println("Error marking all notifications as read: " + e.getMessage());
            return false;
        }
    }

    @Override
    public int getUnreadCount(int userId) {
        String sql = """
            SELECT COUNT(*) as count
            FROM notifications
            WHERE user_id = ? AND is_read = FALSE
        """;
        
        try (Connection conn = DatabaseHandler.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, userId);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("count");
                }
            }
        } catch (SQLException e) {
            System.err.println("Error getting unread count: " + e.getMessage());
        }
        return 0;
    }

    @Override
    public boolean deleteNotification(int notificationId) {
        String sql = """
            DELETE FROM notifications
            WHERE notification_id = ?
        """;
        
        try (Connection conn = DatabaseHandler.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, notificationId);
            int rowsAffected = pstmt.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            System.err.println("Error deleting notification: " + e.getMessage());
            return false;
        }
    }

    @Override
    public List<Notification> getNotificationsByType(int userId, String type) {
        List<Notification> notifications = new ArrayList<>();
        String sql = """
            SELECT notification_id, user_id, type, title, message, related_book_isbn, 
                   related_user_id, is_read, created_at, action_url
            FROM notifications
            WHERE user_id = ? AND type = ?
            ORDER BY created_at DESC
        """;
        
        try (Connection conn = DatabaseHandler.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, userId);
            pstmt.setString(2, type);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    Notification notif = mapResultSetToNotification(rs);
                    notifications.add(notif);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error fetching notifications by type: " + e.getMessage());
        }
        return notifications;
    }

    private Notification mapResultSetToNotification(ResultSet rs) throws SQLException {
        return new Notification(
            rs.getInt("notification_id"),
            rs.getInt("user_id"),
            rs.getString("type"),
            rs.getString("title"),
            rs.getString("message"),
            rs.getString("related_book_isbn"),
            rs.getObject("related_user_id") != null ? rs.getInt("related_user_id") : null,
            rs.getBoolean("is_read"),
            rs.getTimestamp("created_at").toLocalDateTime(),
            rs.getString("action_url")
        );
    }
}

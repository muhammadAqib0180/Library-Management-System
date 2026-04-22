package database;

import model.Notification;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class SupaNotificationDAO implements NotificationDAO {

    @Override
    public int createNotification(Notification notification) {
        String sql = "INSERT INTO notifications (user_id, type, title, message, related_book_isbn, " +
                "related_user_id, is_read, action_url, target_role, created_at) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, NOW()) RETURNING notification_id";

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
            pstmt.setString(9, notification.getTargetRole());

            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return -1;
    }

    @Override
    public List<Notification> getUnreadNotifications(int userId, String role) {
        List<Notification> list = new ArrayList<>();
        String sql = "SELECT * FROM notifications WHERE user_id = ? AND target_role = ? AND is_read = FALSE ORDER BY created_at DESC";

        try (Connection conn = DatabaseHandler.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, userId);
            pstmt.setString(2, role);

            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                list.add(mapResultSetToNotification(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    @Override
    public List<Notification> getAllNotifications(int userId, String role) {
        List<Notification> list = new ArrayList<>();
        String sql = "SELECT * FROM notifications WHERE user_id = ? AND target_role = ? ORDER BY created_at DESC";

        try (Connection conn = DatabaseHandler.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, userId);
            pstmt.setString(2, role);

            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                list.add(mapResultSetToNotification(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    @Override
    public int getUnreadCount(int userId, String role) {
        String sql = "SELECT COUNT(*) FROM notifications WHERE user_id = ? AND target_role = ? AND is_read = FALSE";

        try (Connection conn = DatabaseHandler.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, userId);
            pstmt.setString(2, role);

            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    @Override
    public boolean markAsRead(int notificationId) {
        String sql = "UPDATE notifications SET is_read = TRUE WHERE notification_id = ?";

        try (Connection conn = DatabaseHandler.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, notificationId);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public boolean markAllAsRead(int userId, String role) {
        String sql = "UPDATE notifications SET is_read = TRUE WHERE user_id = ? AND target_role = ? AND is_read = FALSE";

        try (Connection conn = DatabaseHandler.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, userId);
            pstmt.setString(2, role);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
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
                rs.getString("action_url"),
                rs.getString("target_role")
        );
    }
}
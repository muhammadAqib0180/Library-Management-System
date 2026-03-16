package database;

import model.Notification;
import java.util.List;

public interface NotificationDAO {
    /**
     * Create a new notification for a user
     * Supports all user types: borrowers, lenders, admins
     * @param notification Notification object
     * @return notification ID if successful, -1 if failed
     */
    int createNotification(Notification notification);

    /**
     * Get all unread notifications for a specific user
     * @param userId User ID (borrower, lender, or admin)
     * @return List of unread Notification objects, sorted by newest first
     */
    List<Notification> getUnreadNotifications(int userId);

    /**
     * Get all notifications for a specific user (read and unread)
     * @param userId User ID (borrower, lender, or admin)
     * @return List of all Notification objects, sorted by newest first
     */
    List<Notification> getAllNotifications(int userId);

    /**
     * Get paginated notifications for a user
     * @param userId User ID
     * @param limit Number of notifications to retrieve
     * @param offset Starting position
     * @return List of Notification objects
     */
    List<Notification> getNotificationsPaginated(int userId, int limit, int offset);

    /**
     * Mark a notification as read
     * @param notificationId ID of the notification
     * @return true if marked successfully
     */
    boolean markAsRead(int notificationId);

    /**
     * Mark all notifications as read for a user
     * @param userId User ID
     * @return true if all marked successfully
     */
    boolean markAllAsRead(int userId);

    /**
     * Get count of unread notifications for a user (for badge display)
     * @param userId User ID
     * @return Number of unread notifications
     */
    int getUnreadCount(int userId);

    /**
     * Delete a specific notification
     * @param notificationId ID of the notification
     * @return true if deleted successfully
     */
    boolean deleteNotification(int notificationId);

    /**
     * Get notifications by type for a specific user
     * @param userId User ID
     * @param type Notification type (e.g., "BORROW_REQUEST", "REQUEST_REJECTED", etc.)
     * @return List of Notification objects of that type
     */
    List<Notification> getNotificationsByType(int userId, String type);
}

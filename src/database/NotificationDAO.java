package database;

import model.Notification;
import java.util.List;

public interface NotificationDAO {
    int createNotification(Notification notification);

    // Added 'String role' to these to separate Lender/Borrower dashboards
    List<Notification> getUnreadNotifications(int userId, String role);
    List<Notification> getAllNotifications(int userId, String role);
    int getUnreadCount(int userId, String role);
    boolean markAllAsRead(int userId, String role);

    boolean markAsRead(int notificationId);
    //boolean deleteNotification(int notificationId);
}
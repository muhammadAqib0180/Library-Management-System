package model;

import java.time.LocalDateTime;

public class Notification {
    private Integer notificationId;
    private Integer userId;  // For any user: lender, borrower, admin
    private String type;     // BORROW_REQUEST, REQUEST_ACCEPTED, REQUEST_REJECTED, BOOK_RETURNED, BOOK_OVERDUE, ADMIN_ACTION, etc.
    private String title;
    private String message;
    private String relatedBookIsbn;  // Optional: link to related book
    private Integer relatedUserId;   // Optional: link to related user (who triggered notification)
    private Boolean isRead;
    private LocalDateTime createdAt;
    private String actionUrl;  // Optional: URL or identifier for action (e.g., open pending requests)

    // Constructors
    public Notification() {
        this.isRead = false;
    }

    public Notification(Integer userId, String type, String title, String message) {
        this.userId = userId;
        this.type = type;
        this.title = title;
        this.message = message;
        this.isRead = false;
        this.createdAt = LocalDateTime.now();
    }

    public Notification(Integer userId, String type, String title, String message, 
                       String relatedBookIsbn, Integer relatedUserId, String actionUrl) {
        this.userId = userId;
        this.type = type;
        this.title = title;
        this.message = message;
        this.relatedBookIsbn = relatedBookIsbn;
        this.relatedUserId = relatedUserId;
        this.actionUrl = actionUrl;
        this.isRead = false;
        this.createdAt = LocalDateTime.now();
    }

    public Notification(Integer notificationId, Integer userId, String type, String title, 
                       String message, String relatedBookIsbn, Integer relatedUserId, 
                       Boolean isRead, LocalDateTime createdAt, String actionUrl) {
        this.notificationId = notificationId;
        this.userId = userId;
        this.type = type;
        this.title = title;
        this.message = message;
        this.relatedBookIsbn = relatedBookIsbn;
        this.relatedUserId = relatedUserId;
        this.isRead = isRead;
        this.createdAt = createdAt;
        this.actionUrl = actionUrl;
    }

    // Getters and Setters
    public Integer getNotificationId() {
        return notificationId;
    }

    public void setNotificationId(Integer notificationId) {
        this.notificationId = notificationId;
    }

    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getRelatedBookIsbn() {
        return relatedBookIsbn;
    }

    public void setRelatedBookIsbn(String relatedBookIsbn) {
        this.relatedBookIsbn = relatedBookIsbn;
    }

    public Integer getRelatedUserId() {
        return relatedUserId;
    }

    public void setRelatedUserId(Integer relatedUserId) {
        this.relatedUserId = relatedUserId;
    }

    public Boolean isRead() {
        return isRead;
    }

    public void setRead(Boolean read) {
        isRead = read;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public String getActionUrl() {
        return actionUrl;
    }

    public void setActionUrl(String actionUrl) {
        this.actionUrl = actionUrl;
    }

    @Override
    public String toString() {
        return "Notification{" +
                "notificationId=" + notificationId +
                ", userId=" + userId +
                ", type='" + type + '\'' +
                ", title='" + title + '\'' +
                ", isRead=" + isRead +
                ", createdAt=" + createdAt +
                '}';
    }
}

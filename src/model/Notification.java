package model;

import java.time.LocalDateTime;

public class Notification {
    private Integer notificationId;
    private Integer userId;
    private String type;
    private String title;
    private String message;
    private String relatedBookIsbn;
    private Integer relatedUserId;
    private Boolean isRead;
    private LocalDateTime createdAt;
    private String actionUrl;
    private String targetRole; // "LENDER" or "BORROWER"

    public Notification() {
        this.isRead = false;
        this.createdAt = LocalDateTime.now();
    }

    // Full constructor for the DAO to use when reading from DB
    public Notification(Integer notificationId, Integer userId, String type, String title,
                        String message, String relatedBookIsbn, Integer relatedUserId,
                        Boolean isRead, LocalDateTime createdAt, String actionUrl, String targetRole) {
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
        this.targetRole = targetRole;
    }

    // Constructor for creating new notifications in controllers
    public Notification(Integer userId, String type, String title, String message,
                        String relatedBookIsbn, Integer relatedUserId, String actionUrl, String targetRole) {
        this.userId = userId;
        this.type = type;
        this.title = title;
        this.message = message;
        this.relatedBookIsbn = relatedBookIsbn;
        this.relatedUserId = relatedUserId;
        this.actionUrl = actionUrl;
        this.targetRole = targetRole;
        this.isRead = false;
        this.createdAt = LocalDateTime.now();
    }

    // --- Getters and Setters ---
    public String getTargetRole() { return targetRole; }
    public void setTargetRole(String targetRole) { this.targetRole = targetRole; }

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



// Update constructors to include targetRole

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

package model;

import java.time.LocalDateTime;

public class Rating {
    public enum TargetType { USER, BOOK }

    private int id;
    private Integer requestId;
    private int raterId;
    private TargetType targetType;
    private Integer targetUserId;
    private String targetBookIsbn;
    private int stars;
    private String comment;
    private LocalDateTime createdAt;

    public Rating() {}

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public Integer getRequestId() { return requestId; }
    public void setRequestId(Integer requestId) { this.requestId = requestId; }
    public int getRaterId() { return raterId; }
    public void setRaterId(int raterId) { this.raterId = raterId; }
    public TargetType getTargetType() { return targetType; }
    public void setTargetType(TargetType targetType) { this.targetType = targetType; }
    public Integer getTargetUserId() { return targetUserId; }
    public void setTargetUserId(Integer targetUserId) { this.targetUserId = targetUserId; }
    public String getTargetBookIsbn() { return targetBookIsbn; }
    public void setTargetBookIsbn(String targetBookIsbn) { this.targetBookIsbn = targetBookIsbn; }
    public int getStars() { return stars; }
    public void setStars(int stars) { this.stars = stars; }
    public String getComment() { return comment; }
    public void setComment(String comment) { this.comment = comment; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}

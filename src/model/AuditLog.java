package model;

import java.time.LocalDateTime;

public class AuditLog {
    private int id;
    private LocalDateTime timestamp;
    private Integer actorUserId;
    private String actorUsername;
    private String actionType;
    private String targetType;
    private String targetId;
    private String details;

    public AuditLog() {}

    public AuditLog(int id, LocalDateTime timestamp, Integer actorUserId, String actorUsername,
                    String actionType, String targetType, String targetId, String details) {
        this.id = id;
        this.timestamp = timestamp;
        this.actorUserId = actorUserId;
        this.actorUsername = actorUsername;
        this.actionType = actionType;
        this.targetType = targetType;
        this.targetId = targetId;
        this.details = details;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }
    public Integer getActorUserId() { return actorUserId; }
    public void setActorUserId(Integer actorUserId) { this.actorUserId = actorUserId; }
    public String getActorUsername() { return actorUsername; }
    public void setActorUsername(String actorUsername) { this.actorUsername = actorUsername; }
    public String getActionType() { return actionType; }
    public void setActionType(String actionType) { this.actionType = actionType; }
    public String getTargetType() { return targetType; }
    public void setTargetType(String targetType) { this.targetType = targetType; }
    public String getTargetId() { return targetId; }
    public void setTargetId(String targetId) { this.targetId = targetId; }
    public String getDetails() { return details; }
    public void setDetails(String details) { this.details = details; }

    /** Known action types — use these constants to keep logs consistent. */
    public static final class Action {
        public static final String LOGIN              = "LOGIN";
        public static final String LOGOUT             = "LOGOUT";
        public static final String BOOK_ADDED         = "BOOK_ADDED";
        public static final String BOOK_EDITED        = "BOOK_EDITED";
        public static final String BOOK_DELETED       = "BOOK_DELETED";
        public static final String REQUEST_CREATED    = "REQUEST_CREATED";
        public static final String REQUEST_ACCEPTED   = "REQUEST_ACCEPTED";
        public static final String REQUEST_REJECTED   = "REQUEST_REJECTED";
        public static final String REQUEST_CANCELLED  = "REQUEST_CANCELLED";
        public static final String HANDOVER_INITIATED = "HANDOVER_INITIATED";
        public static final String HANDOVER_IN_TRANSIT= "HANDOVER_IN_TRANSIT";
        public static final String OTP_VERIFIED       = "OTP_VERIFIED";
        public static final String BOOK_RECEIVED      = "BOOK_RECEIVED";
        public static final String RETURN_INITIATED   = "RETURN_INITIATED";
        public static final String BOOK_RETURNED      = "BOOK_RETURNED";
        public static final String RATING_SUBMITTED   = "RATING_SUBMITTED";
        public static final String USER_REGISTERED    = "USER_REGISTERED";
        public static final String USER_DEACTIVATED   = "USER_DEACTIVATED";
    }
}

package util;

import database.AuditLogDAO;
import database.SupaAuditLogDAO;
import model.AuditLog;
import model.User;

/**
 * Global audit-logging hook. Call from controllers/DAOs whenever a significant
 * event happens. Never throws — failures are swallowed & printed so they never
 * break the user-facing flow. Sprint 3 — US-4.
 */
public final class AuditLogger {
    private static final AuditLogDAO DAO = new SupaAuditLogDAO();
    private static volatile User currentUser;   // set by LoginController after login

    private AuditLogger() {}

    public static void setCurrentUser(User u) { currentUser = u; }
    public static User getCurrentUser() { return currentUser; }

    public static void log(String actionType, String targetType, Object targetId, String details) {
        try {
            AuditLog entry = new AuditLog();
            entry.setActionType(actionType);
            entry.setTargetType(targetType);
            entry.setTargetId(targetId == null ? null : String.valueOf(targetId));
            entry.setDetails(details);
            if (currentUser != null) {
                entry.setActorUserId(currentUser.getId());
                entry.setActorUsername(currentUser.getUsername());
            }
            DAO.insert(entry);
        } catch (Exception e) {
            System.err.println("[AuditLogger] failed to log " + actionType + ": " + e.getMessage());
        }
    }

    /** Convenience overloads. */
    public static void log(String actionType) { log(actionType, null, null, null); }
    public static void log(String actionType, String details) { log(actionType, null, null, details); }
}

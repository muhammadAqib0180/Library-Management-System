
package model;

/**
 * Full lifecycle of a borrow request — Sprint 3 (US-1).
 * Transitions:
 *   PENDING → ACCEPTED → AWAITING_HANDOVER → (IN_TRANSIT) → BORROWED
 *           → RETURN_IN_TRANSIT → RETURNED
 *   PENDING → REJECTED / CANCELLED   (terminal)
 */
public enum HandoverStatus {
    PENDING,
    ACCEPTED,
    AWAITING_HANDOVER,
    IN_TRANSIT,
    BORROWED,
    RETURN_IN_TRANSIT,
    RETURNED,
    REJECTED,
    CANCELLED;

    public static HandoverStatus fromString(String s) {
        if (s == null) return PENDING;
        try { return HandoverStatus.valueOf(s.trim().toUpperCase()); }
        catch (IllegalArgumentException e) { return PENDING; }
    }

    public String badgeEmoji() {
        return switch (this) {
            case PENDING           -> "⏳";
            case ACCEPTED          -> "👍";
            case AWAITING_HANDOVER -> "📦";
            case IN_TRANSIT        -> "🚚";
            case BORROWED          -> "📖";
            case RETURN_IN_TRANSIT -> "↩️";
            case RETURNED          -> "✅";
            case REJECTED          -> "❌";
            case CANCELLED         -> "🚫";
        };
    }

    public String badgeColor() {
        return switch (this) {
            case PENDING           -> "#F59E0B";
            case ACCEPTED          -> "#3B82F6";
            case AWAITING_HANDOVER -> "#F59E0B";
            case IN_TRANSIT        -> "#EAB308";
            case BORROWED          -> "#10B981";
            case RETURN_IN_TRANSIT -> "#06B6D4";
            case RETURNED          -> "#22C55E";
            case REJECTED          -> "#EF4444";
            case CANCELLED         -> "#6B7280";
        };
    }

    public boolean isTerminal() {
        return this == RETURNED || this == REJECTED || this == CANCELLED;
    }
}
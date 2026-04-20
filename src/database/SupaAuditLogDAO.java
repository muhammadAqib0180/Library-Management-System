package database;

import model.AuditLog;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class SupaAuditLogDAO implements AuditLogDAO {

    @Override
    public int insert(AuditLog e) {
        String sql = """
            INSERT INTO audit_log (actor_user_id, actor_username, action_type, target_type, target_id, details)
            VALUES (?, ?, ?, ?, ?, ?)
            RETURNING id
        """;
        try (Connection conn = DatabaseHandler.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            if (e.getActorUserId() != null) ps.setInt(1, e.getActorUserId()); else ps.setNull(1, Types.INTEGER);
            ps.setString(2, e.getActorUsername());
            ps.setString(3, e.getActionType());
            ps.setString(4, e.getTargetType());
            ps.setString(5, e.getTargetId());
            ps.setString(6, e.getDetails());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt(1);
            }
        } catch (SQLException ex) {
            System.err.println("[SupaAuditLogDAO.insert] " + ex.getMessage());
        }
        return -1;
    }

    @Override
    public List<AuditLog> findAll() {
        return search(null, null, null, null, null);
    }

    @Override
    public List<AuditLog> search(Integer actorUserId, String actionType,
                                 LocalDateTime from, LocalDateTime to, String freeText) {
        StringBuilder sql = new StringBuilder("""
            SELECT id, timestamp, actor_user_id, actor_username, action_type, target_type, target_id, details
            FROM audit_log
            WHERE 1=1
        """);
        List<Object> params = new ArrayList<>();
        if (actorUserId != null)  { sql.append(" AND actor_user_id = ?"); params.add(actorUserId); }
        if (actionType != null && !actionType.isBlank()) { sql.append(" AND action_type = ?"); params.add(actionType); }
        if (from != null) { sql.append(" AND timestamp >= ?"); params.add(Timestamp.valueOf(from)); }
        if (to   != null) { sql.append(" AND timestamp <= ?"); params.add(Timestamp.valueOf(to)); }
        if (freeText != null && !freeText.isBlank()) {
            sql.append(" AND (LOWER(details) LIKE ? OR LOWER(actor_username) LIKE ? OR LOWER(target_id) LIKE ?)");
            String like = "%" + freeText.toLowerCase() + "%";
            params.add(like); params.add(like); params.add(like);
        }
        sql.append(" ORDER BY timestamp DESC LIMIT 2000");

        List<AuditLog> out = new ArrayList<>();
        try (Connection conn = DatabaseHandler.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            for (int i = 0; i < params.size(); i++) ps.setObject(i + 1, params.get(i));
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) out.add(map(rs));
            }
        } catch (SQLException ex) {
            System.err.println("[SupaAuditLogDAO.search] " + ex.getMessage());
        }
        return out;
    }

    private AuditLog map(ResultSet rs) throws SQLException {
        int actorId = rs.getInt("actor_user_id");
        Integer actorIdBoxed = rs.wasNull() ? null : actorId;
        return new AuditLog(
            rs.getInt("id"),
            rs.getTimestamp("timestamp").toLocalDateTime(),
            actorIdBoxed,
            rs.getString("actor_username"),
            rs.getString("action_type"),
            rs.getString("target_type"),
            rs.getString("target_id"),
            rs.getString("details")
        );
    }
}

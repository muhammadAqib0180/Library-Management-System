package database;

import model.AuditLog;

import java.time.LocalDateTime;
import java.util.List;

public interface AuditLogDAO {
    int insert(AuditLog entry);

    /** All entries, newest first. */
    List<AuditLog> findAll();

    /** Filtered search. Any argument may be null to skip that filter. */
    List<AuditLog> search(Integer actorUserId,
                          String actionType,
                          LocalDateTime from,
                          LocalDateTime to,
                          String freeText);
}

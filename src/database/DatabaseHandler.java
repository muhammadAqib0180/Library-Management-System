package database;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

public class DatabaseHandler {

    public static Connection getConnection() throws SQLException {
        return DatabaseConnection.getConnection();
    }

    public static void initialize() {
        try (Connection conn = getConnection(); Statement stmt = conn.createStatement()) {
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS users (
                    id       SERIAL PRIMARY KEY,
                    username VARCHAR(255) UNIQUE NOT NULL,
                    password VARCHAR(255) NOT NULL,
                    role     VARCHAR(50)  NOT NULL,
                    active   BOOLEAN DEFAULT TRUE
                )
            """);
            safeExec(stmt, "ALTER TABLE users ADD COLUMN active BOOLEAN DEFAULT TRUE", "users.active");

            // ----------- BOOKS -----------
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS books (
                    isbn            VARCHAR(255) PRIMARY KEY UNIQUE NOT NULL,
                    title           VARCHAR(255) NOT NULL,
                    author          VARCHAR(255),
                    genre           VARCHAR(100),
                    owner_id        INTEGER REFERENCES users(id),
                    available       BOOLEAN DEFAULT TRUE,
                    borrowed_by     INTEGER REFERENCES users(id),
                    borrow_date     DATE,
                    due_date        DATE,
                    listed          BOOLEAN DEFAULT TRUE
                )
            """);
            safeExec(stmt, "ALTER TABLE books ADD COLUMN borrow_date DATE", "books.borrow_date");
            safeExec(stmt, "ALTER TABLE books ADD COLUMN due_date DATE", "books.due_date");
            safeExec(stmt, "ALTER TABLE books ADD COLUMN listed BOOLEAN DEFAULT TRUE", "books.listed");
            // Sprint 3 — US-3 & US-7 new columns
            safeExec(stmt, "ALTER TABLE books ADD COLUMN condition VARCHAR(20) DEFAULT 'Good'", "books.condition");
            safeExec(stmt, "ALTER TABLE books ADD COLUMN cover_url TEXT", "books.cover_url");
            safeExec(stmt, "ALTER TABLE books ADD COLUMN borrow_count INTEGER DEFAULT 0", "books.borrow_count");

            // ----------- BORROW_REQUESTS -----------
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS borrow_requests (
                    request_id       SERIAL PRIMARY KEY,
                    book_isbn        VARCHAR(255) NOT NULL REFERENCES books(isbn),
                    borrower_id      INTEGER NOT NULL REFERENCES users(id),
                    lender_id        INTEGER NOT NULL REFERENCES users(id),
                    status           VARCHAR(50) DEFAULT 'PENDING',
                    requested_due_date DATE,
                    created_at       TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                    responded_at     TIMESTAMP,
                    rejection_reason TEXT
                )
            """);
            // Sprint 3 — US-1 handover columns
            safeExec(stmt, "ALTER TABLE borrow_requests ADD COLUMN handover_method VARCHAR(20)", "br.handover_method");
            safeExec(stmt, "ALTER TABLE borrow_requests ADD COLUMN otp VARCHAR(10)", "br.otp");
            safeExec(stmt, "ALTER TABLE borrow_requests ADD COLUMN return_otp VARCHAR(10)", "br.return_otp");
            safeExec(stmt, "ALTER TABLE borrow_requests ADD COLUMN meetup_location TEXT", "br.meetup_location");
            safeExec(stmt, "ALTER TABLE borrow_requests ADD COLUMN meetup_time TIMESTAMP", "br.meetup_time");
            safeExec(stmt, "ALTER TABLE borrow_requests ADD COLUMN courier_service VARCHAR(100)", "br.courier_service");
            safeExec(stmt, "ALTER TABLE borrow_requests ADD COLUMN courier_person VARCHAR(255)", "br.courier_person");
            safeExec(stmt, "ALTER TABLE borrow_requests ADD COLUMN vehicle_plate VARCHAR(50)", "br.vehicle_plate");
            safeExec(stmt, "ALTER TABLE borrow_requests ADD COLUMN vehicle_type VARCHAR(20)", "br.vehicle_type");
            safeExec(stmt, "ALTER TABLE borrow_requests ADD COLUMN proof_image_url TEXT", "br.proof_image_url");
            safeExec(stmt, "ALTER TABLE borrow_requests ADD COLUMN return_courier_service VARCHAR(100)", "br.return_courier_service");
            safeExec(stmt, "ALTER TABLE borrow_requests ADD COLUMN return_courier_person VARCHAR(255)", "br.return_courier_person");
            safeExec(stmt, "ALTER TABLE borrow_requests ADD COLUMN return_vehicle_plate VARCHAR(50)", "br.return_vehicle_plate");
            safeExec(stmt, "ALTER TABLE borrow_requests ADD COLUMN return_vehicle_type VARCHAR(20)", "br.return_vehicle_type");
            safeExec(stmt, "ALTER TABLE borrow_requests ADD COLUMN return_proof_image_url TEXT", "br.return_proof_image_url");
            safeExec(stmt, "ALTER TABLE borrow_requests ADD COLUMN received_at TIMESTAMP", "br.received_at");
            safeExec(stmt, "ALTER TABLE borrow_requests ADD COLUMN returned_at TIMESTAMP", "br.returned_at");

            // State history (timeline) — one row per state transition
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS state_history (
                    id          SERIAL PRIMARY KEY,
                    request_id  INTEGER NOT NULL REFERENCES borrow_requests(request_id) ON DELETE CASCADE,
                    from_state  VARCHAR(50),
                    to_state    VARCHAR(50) NOT NULL,
                    actor_id    INTEGER REFERENCES users(id),
                    note        TEXT,
                    created_at  TIMESTAMP DEFAULT CURRENT_TIMESTAMP
                )
            """);

            // ----------- NOTIFICATIONS -----------
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS notifications (
                    notification_id  SERIAL PRIMARY KEY,
                    user_id          INTEGER NOT NULL REFERENCES users(id),
                    type             VARCHAR(100) NOT NULL,
                    title            VARCHAR(255) NOT NULL,
                    message          TEXT,
                    related_book_isbn VARCHAR(255) REFERENCES books(isbn),
                    related_user_id  INTEGER REFERENCES users(id),
                    is_read          BOOLEAN DEFAULT FALSE,
                    created_at       TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                    action_url       VARCHAR(255)
                )
            """);
            safeExec(stmt, "ALTER TABLE notifications ADD COLUMN related_request_id INTEGER", "notif.related_request_id");

            // ----------- AUDIT LOG (US-4) -----------
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS audit_log (
                    id              SERIAL PRIMARY KEY,
                    timestamp       TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                    actor_user_id   INTEGER REFERENCES users(id),
                    actor_username  VARCHAR(255),
                    action_type     VARCHAR(100) NOT NULL,
                    target_type     VARCHAR(100),
                    target_id       VARCHAR(255),
                    details         TEXT
                )
            """);
            safeExec(stmt, "CREATE INDEX IF NOT EXISTS idx_audit_timestamp ON audit_log(timestamp DESC)", "idx_audit_ts");
            safeExec(stmt, "CREATE INDEX IF NOT EXISTS idx_audit_actor ON audit_log(actor_user_id)", "idx_audit_actor");
            safeExec(stmt, "CREATE INDEX IF NOT EXISTS idx_audit_action ON audit_log(action_type)", "idx_audit_action");

            // ----------- RATINGS (US-6) -----------
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS ratings (
                    id              SERIAL PRIMARY KEY,
                    request_id      INTEGER REFERENCES borrow_requests(request_id) ON DELETE CASCADE,
                    rater_id        INTEGER NOT NULL REFERENCES users(id),
                    target_type     VARCHAR(20) NOT NULL, -- 'USER' or 'BOOK'
                    target_user_id  INTEGER REFERENCES users(id),
                    target_book_isbn VARCHAR(255) REFERENCES books(isbn),
                    stars           INTEGER NOT NULL CHECK (stars BETWEEN 1 AND 5),
                    comment         TEXT,
                    created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                    UNIQUE(request_id, rater_id, target_type, target_user_id, target_book_isbn)
                )
            """);
            safeExec(stmt, "CREATE INDEX IF NOT EXISTS idx_ratings_target_user ON ratings(target_user_id)", "idx_rate_user");
            safeExec(stmt, "CREATE INDEX IF NOT EXISTS idx_ratings_target_book ON ratings(target_book_isbn)", "idx_rate_book");

            System.out.println("Database initialized successfully (Sprint 3 schema).");
        } catch (SQLException e) {
            System.err.println("Failed to initialize database: " + e.getMessage());
        }
    }

    /** Helper — run DDL and ignore "already exists" / "duplicate column" errors. */
    private static void safeExec(Statement stmt, String sql, String label) {
        try {
            stmt.execute(sql);
        } catch (SQLException e) {
            String msg = e.getMessage() == null ? "" : e.getMessage().toLowerCase();
            if (msg.contains("already exists") || msg.contains("duplicate")) {
                // normal — column/index already present
            } else {
                System.err.println("Note [" + label + "]: " + e.getMessage());
            }
        }
    }
}
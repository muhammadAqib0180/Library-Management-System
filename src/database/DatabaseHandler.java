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
            
            // Add active column if it doesn't exist (for existing tables)
            try {
                stmt.execute("""
                    ALTER TABLE users
                    ADD COLUMN active BOOLEAN DEFAULT TRUE
                """);
                System.out.println("Added 'active' column to users table.");
            } catch (SQLException e) {
                if (e.getMessage().contains("already exists")) {
                    System.out.println("Column 'active' already exists in users table.");
                } else {
                    System.err.println("Error adding 'active' column: " + e.getMessage());
                }
            }
            
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
            
            // Add new columns if they don't exist (for existing tables)
            try {
                stmt.execute("""
                    ALTER TABLE books
                    ADD COLUMN borrow_date DATE
                """);
                System.out.println("Added 'borrow_date' column to books table.");
            } catch (SQLException e) {
                if (!e.getMessage().contains("already exists") && !e.getMessage().contains("duplicate")) {
                    System.err.println("Note: 'borrow_date' column status: " + e.getMessage());
                }
            }
            
            try {
                stmt.execute("""
                    ALTER TABLE books
                    ADD COLUMN due_date DATE
                """);
                System.out.println("Added 'due_date' column to books table.");
            } catch (SQLException e) {
                if (!e.getMessage().contains("already exists") && !e.getMessage().contains("duplicate")) {
                    System.err.println("Note: 'due_date' column status: " + e.getMessage());
                }
            }
            
            try {
                stmt.execute("""
                    ALTER TABLE books
                    ADD COLUMN listed BOOLEAN DEFAULT TRUE
                """);
                System.out.println("Added 'listed' column to books table.");
            } catch (SQLException e) {
                if (!e.getMessage().contains("already exists") && !e.getMessage().contains("duplicate")) {
                    System.err.println("Note: 'listed' column status: " + e.getMessage());
                }
            }
            
            // Convert borrowed_by from VARCHAR to INTEGER if needed
            try {
                stmt.execute("""
                    ALTER TABLE books
                    DROP CONSTRAINT IF EXISTS books_borrowed_by_fkey
                """);
                System.out.println("Dropped old foreign key constraint on borrowed_by.");
            } catch (SQLException e) {
                System.out.println("Note: Could not drop constraint (may not exist): " + e.getMessage());
            }
            
            try {
                // Try to convert the column type
                stmt.execute("""
                    ALTER TABLE books
                    ALTER COLUMN borrowed_by TYPE INTEGER USING borrowed_by::INTEGER
                """);
                System.out.println("Converted borrowed_by column to INTEGER.");
            } catch (SQLException e) {
                if (e.getMessage().contains("already exists") || e.getMessage().contains("cannot cast")) {
                    System.out.println("Note: borrowed_by column already INTEGER or contains non-integer values.");
                    // Try to just recreate with proper type
                    try {
                        stmt.execute("""
                            DROP TABLE IF EXISTS books_temp;
                            CREATE TABLE books_temp AS SELECT * FROM books;
                            DROP TABLE books;
                            CREATE TABLE books (
                                isbn VARCHAR(255) PRIMARY KEY UNIQUE NOT NULL,
                                title VARCHAR(255) NOT NULL,
                                author VARCHAR(255),
                                genre VARCHAR(100),
                                owner_id INTEGER REFERENCES users(id),
                                available BOOLEAN DEFAULT TRUE,
                                borrowed_by INTEGER REFERENCES users(id),
                                borrow_date DATE,
                                due_date DATE,
                                listed BOOLEAN DEFAULT TRUE
                            );
                            INSERT INTO books SELECT * FROM books_temp;
                            DROP TABLE books_temp;
                        """);
                        System.out.println("Recreated books table with correct schema.");
                    } catch (SQLException e2) {
                        System.out.println("Note: Could not migrate borrowed_by: " + e2.getMessage());
                    }
                } else {
                    System.err.println("Note: borrowed_by conversion: " + e.getMessage());
                }
            }
            
            // Add foreign key constraint for borrowed_by
            try {
                stmt.execute("""
                    ALTER TABLE books
                    ADD CONSTRAINT books_borrowed_by_fkey FOREIGN KEY (borrowed_by) REFERENCES users(id)
                """);
                System.out.println("Added foreign key constraint on borrowed_by.");
            } catch (SQLException e) {
                if (e.getMessage().contains("already exists")) {
                    System.out.println("Foreign key constraint already exists.");
                }
            }
            
            // Create borrow_requests table for request-based borrowing workflow
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
            System.out.println("Created borrow_requests table.");
            
            // Create notifications table for all users (lender, borrower, admin)
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
            System.out.println("Created notifications table.");
            
            System.out.println("Database initialized successfully.");
        } catch (SQLException e) {
            System.err.println("Failed to initialize database: " + e.getMessage());
        }
    }
}
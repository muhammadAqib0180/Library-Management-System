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
                            active   BOOLEAN DEFAULT TRUE,
                            last_login TIMESTAMP
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

            // Add last_login column if it doesn't exist
            try {
                stmt.execute("""
                            ALTER TABLE users
                            ADD COLUMN last_login TIMESTAMP
                        """);
                System.out.println("Added 'last_login' column to users table.");
            } catch (SQLException e) {
                if (e.getMessage().contains("already exists")) {
                    System.out.println("Column 'last_login' already exists in users table.");
                } else {
                    System.err.println("Error adding 'last_login' column: " + e.getMessage());
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
                            borrowed_by     VARCHAR(255)
                        )
                    """);
            System.out.println("Database initialized successfully.");
        } catch (SQLException e) {
            System.err.println("Failed to initialize database: " + e.getMessage());
        }
    }
}
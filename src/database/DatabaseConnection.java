package database;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import java.sql.Connection;
import java.sql.SQLException;

public class DatabaseConnection {
    private static final String URL =
        "jdbc:postgresql://aws-1-ap-northeast-1.pooler.supabase.com:6543/postgres";
    private static final String USER     = "postgres.tyzwmebiitvlvorurvip";
    private static final String PASSWORD = "Qxv6_b.KGGX_xUU";

    private static HikariDataSource dataSource;

    static {
        try {
            HikariConfig config = new HikariConfig();
            config.setJdbcUrl(URL);
            config.setUsername(USER);
            config.setPassword(PASSWORD);
            config.setMaximumPoolSize(10);  // Max 10 concurrent connections
            config.setMinimumIdle(2);       // Keep 2 connections idle
            config.setConnectionTimeout(10000);  // 10 seconds to get a connection
            config.setIdleTimeout(600000);  // 10 minutes idle timeout
            config.setMaxLifetime(1800000); // 30 minutes max lifetime
            config.setAutoCommit(true);
            config.setPoolName("LibraryPool");

            dataSource = new HikariDataSource(config);
            System.out.println("Connection pool initialized successfully.");
        } catch (Exception e) {
            System.err.println("Failed to initialize connection pool: " + e.getMessage());
            throw new RuntimeException(e);
        }
    }

    public static Connection getConnection() throws SQLException {
        return dataSource.getConnection();
    }

    public static void close() {
        if (dataSource != null && !dataSource.isClosed()) {
            dataSource.close();
        }
    }
}

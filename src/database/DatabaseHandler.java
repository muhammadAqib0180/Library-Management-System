package database;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class DatabaseHandler {
    // The name of the file that will be created in your project folder
    private static final String URL = "jdbc:sqlite:library.db";

    public static void initializeDatabase() {
        // SQL to create the business logic tables 
        String createBooksTable = "CREATE TABLE IF NOT EXISTS Books (" +
                "isbn TEXT PRIMARY KEY, " +
                "title TEXT NOT NULL, " +
                "author TEXT NOT NULL, " +
                "status TEXT DEFAULT 'Available');";

        String createUsersTable = "CREATE TABLE IF NOT EXISTS Users (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "username TEXT UNIQUE NOT NULL, " +
                "password TEXT NOT NULL, " +
                "role TEXT NOT NULL);";

        try (Connection conn = DriverManager.getConnection(URL);
             Statement stmt = conn.createStatement()) {

            // Execute the creation of tables
            stmt.execute(createBooksTable);
            stmt.execute(createUsersTable);

            System.out.println("Success: library.db created and tables initialized.");

        } catch (SQLException e) {
            System.out.println("Database Error: " + e.getMessage());
        }
    }
}
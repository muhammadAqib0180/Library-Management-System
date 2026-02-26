package database;
import model.Book;
import database.BookDAO;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class SQLiteBookDAO implements BookDAO {
    private String url = "jdbc:sqlite:library.db";

    @Override
    public void insert(Book book) {
        String sql = "INSERT INTO Books(isbn, title, author) VALUES(?,?,?)";
        try (Connection conn = DriverManager.getConnection(url);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, book.getIsbn());
            pstmt.setString(2, book.getTitle());
            pstmt.setString(3, book.getAuthor());
            pstmt.executeUpdate();
            System.out.println("Book saved to SQLite!");
        } catch (SQLException e) {
            System.out.println("Error saving book: " + e.getMessage());
        }
    }

    @Override
    public List<Book> getAll() {
        List<Book> books = new ArrayList<>();
        String sql = "SELECT * FROM Books";
        try (Connection conn = DriverManager.getConnection(url);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                books.add(new Book(rs.getString("isbn"), rs.getString("title"), rs.getString("author")));
            }
        } catch (SQLException e) {
            System.out.println("Error fetching books: " + e.getMessage());
        }
        return books;
    }

    @Override
    public Book findByIsbn(String isbn) {
        // Implementation for finding a specific book...
        return null;
    }
}
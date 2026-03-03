package database;
import model.Book;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class SupaBookDAO implements BookDAO {
    // Now connects to Supabase PostgreSQL via DatabaseHandler
    private UserDAO userDAO = new SupaUserDAO();
    private String url = "jdbc:sqlite:library.db";

    @Override
    public void insert(Book book) {
        String sql = "INSERT INTO books(isbn, title, author, genre, owner_id, available, borrowed_by) VALUES(?,?,?,?,?,?,?)";
        try (Connection conn = DatabaseHandler.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, book.getIsbn());
            pstmt.setString(2, book.getTitle());
            pstmt.setString(3, book.getAuthor());
            pstmt.setString(4, book.getGenre());
            pstmt.setInt(5, book.getOwnerId());
            pstmt.setBoolean(6, book.isAvailable());
            pstmt.setString(7, book.getBorrowedBy());
            pstmt.executeUpdate();
            System.out.println("Book saved to database!");
        } catch (SQLException e) {
            System.out.println("Error saving book: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @Override
    public List<Book> getAll() {
        List<Book> books = new ArrayList<>();
        String sql = "SELECT isbn, title, author, genre, owner_id, available, borrowed_by FROM books";
        try (Connection conn = DatabaseHandler.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                books.add(new Book(
                    rs.getString("isbn"),
                    rs.getString("title"),
                    rs.getString("author"),
                    rs.getString("genre"),
                    rs.getInt("owner_id"),
                    rs.getBoolean("available"),
                    rs.getString("borrowed_by")
                ));
            }
        } catch (SQLException e) {
            System.out.println("Error fetching books: " + e.getMessage());
            e.printStackTrace();
        }
        return books;
    }

    @Override
    public Book findByIsbn(String isbn) {
        String sql = "SELECT isbn, title, author, genre, owner_id, available, borrowed_by FROM books WHERE isbn = ?";
        try (Connection conn = DatabaseHandler.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, isbn);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return new Book(
                    rs.getString("isbn"),
                    rs.getString("title"),
                    rs.getString("author"),
                    rs.getString("genre"),
                    rs.getInt("owner_id"),
                    rs.getBoolean("available"),
                    rs.getString("borrowed_by")
                );
            }
        } catch (SQLException e) {
            System.out.println("Error finding book: " + e.getMessage());
        }
        return null;
    }

    @Override
    public boolean updateBook(Book book) {
        String sql = "UPDATE books SET title = ?, author = ?, genre = ?, available = ?, borrowed_by = ? WHERE isbn = ?";
        try (Connection conn = DatabaseHandler.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, book.getTitle());
            pstmt.setString(2, book.getAuthor());
            pstmt.setString(3, book.getGenre());
            pstmt.setBoolean(4, book.isAvailable());
            pstmt.setString(5, book.getBorrowedBy());
            pstmt.setString(6, book.getIsbn());
            pstmt.executeUpdate();
            System.out.println("Book updated successfully!");
            return true;
        } catch (SQLException e) {
            System.out.println("Error updating book: " + e.getMessage());
            return false;
        }
    }

    @Override
    public boolean deleteBook(String isbn) {
        String sql = "DELETE FROM books WHERE isbn = ?";
        try (Connection conn = DatabaseHandler.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, isbn);
            pstmt.executeUpdate();
            System.out.println("Book deleted successfully!");
            return true;
        } catch (SQLException e) {
            System.out.println("Error deleting book: " + e.getMessage());
            return false;
        }
    }

    @Override
    public List<Book> getByOwnerId(int ownerId) {
        List<Book> books = new ArrayList<>();
        String sql = "SELECT isbn, title, author, genre, owner_id, available, borrowed_by FROM books WHERE owner_id = ?";
        try (Connection conn = DatabaseHandler.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, ownerId);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                books.add(new Book(
                    rs.getString("isbn"),
                    rs.getString("title"),
                    rs.getString("author"),
                    rs.getString("genre"),
                    rs.getInt("owner_id"),
                    rs.getBoolean("available"),
                    rs.getString("borrowed_by")
                ));
            }
        } catch (SQLException e) {
            System.out.println("Error fetching books by owner: " + e.getMessage());
        }
        return books;
    }
}
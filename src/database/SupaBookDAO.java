package database;
import model.Book;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class SupaBookDAO implements BookDAO {
    // Now connects to Supabase PostgreSQL via DatabaseHandler
    private UserDAO userDAO = new SupaUserDAO();
    private String url = "jdbc:sqlite:library.db";

    @Override
    public void insert(Book book) {
        String sql = "INSERT INTO books(isbn, title, author, genre, owner_id, available, borrowed_by, borrow_date, due_date, listed) VALUES(?,?,?,?,?,?,?,?,?,?)";
        try (Connection conn = DatabaseHandler.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, book.getIsbn());
            pstmt.setString(2, book.getTitle());
            pstmt.setString(3, book.getAuthor());
            pstmt.setString(4, book.getGenre());
            pstmt.setInt(5, book.getOwnerId());
            pstmt.setBoolean(6, book.isAvailable());
            pstmt.setObject(7, book.getBorrowedById() != null ? book.getBorrowedById() : null);
            pstmt.setObject(8, book.getBorrowDate());
            pstmt.setObject(9, book.getDueDate());
            pstmt.setBoolean(10, book.isListed());
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
        String sql = "SELECT isbn, title, author, genre, owner_id, available, borrowed_by, borrow_date, due_date, listed FROM books";
        try (Connection conn = DatabaseHandler.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                Book book = new Book(
                    rs.getString("isbn"),
                    rs.getString("title"),
                    rs.getString("author"),
                    rs.getString("genre"),
                    rs.getInt("owner_id"),
                    rs.getBoolean("available"),
                    (Integer) rs.getObject("borrowed_by"),
                    rs.getObject("borrow_date", LocalDate.class),
                    rs.getObject("due_date", LocalDate.class),
                    rs.getBoolean("listed")
                );
                books.add(book);
            }
        } catch (SQLException e) {
            System.out.println("Error fetching books: " + e.getMessage());
            e.printStackTrace();
        }
        return books;
    }

    @Override
    public Book findByIsbn(String isbn) {
        String sql = "SELECT isbn, title, author, genre, owner_id, available, borrowed_by, borrow_date, due_date, listed FROM books WHERE isbn = ?";
        try (Connection conn = DatabaseHandler.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, isbn);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return new Book(
                        rs.getString("isbn"),
                        rs.getString("title"),
                        rs.getString("author"),
                        rs.getString("genre"),
                        rs.getInt("owner_id"),
                        rs.getBoolean("available"),
                        (Integer) rs.getObject("borrowed_by"),
                        rs.getObject("borrow_date", LocalDate.class),
                        rs.getObject("due_date", LocalDate.class),
                        rs.getBoolean("listed")
                    );
                }
            }
        } catch (SQLException e) {
            System.out.println("Error finding book: " + e.getMessage());
        }
        return null;
    }

    @Override
    public boolean updateBook(Book book) {
        String sql = "UPDATE books SET title = ?, author = ?, genre = ?, available = ?, borrowed_by = ?, borrow_date = ?, due_date = ?, listed = ? WHERE isbn = ?";
        try (Connection conn = DatabaseHandler.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, book.getTitle());
            pstmt.setString(2, book.getAuthor());
            pstmt.setString(3, book.getGenre());
            pstmt.setBoolean(4, book.isAvailable());
            pstmt.setObject(5, book.getBorrowedById() != null ? book.getBorrowedById() : null);
            pstmt.setObject(6, book.getBorrowDate());
            pstmt.setObject(7, book.getDueDate());
            pstmt.setBoolean(8, book.isListed());
            pstmt.setString(9, book.getIsbn());
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

    // ============ LOAN OPERATIONS ============

    @Override
    public void borrowBook(String isbn, int borrowerId, LocalDate dueDate) {
        String sql = "UPDATE books SET borrowed_by = ?, borrow_date = ?, due_date = ?, available = false WHERE isbn = ?";
        try (Connection conn = DatabaseHandler.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, borrowerId);
            pstmt.setObject(2, LocalDate.now());
            pstmt.setObject(3, dueDate);
            pstmt.setString(4, isbn);
            pstmt.executeUpdate();
            System.out.println("Book borrowed successfully! ISBN: " + isbn + ", Borrower ID: " + borrowerId + ", Due Date: " + dueDate);
        } catch (SQLException e) {
            System.out.println("Error borrowing book: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @Override
    public void returnBook(String isbn) {
        String sql = "UPDATE books SET borrowed_by = null, borrow_date = null, due_date = null, available = true WHERE isbn = ?";
        try (Connection conn = DatabaseHandler.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, isbn);
            pstmt.executeUpdate();
            System.out.println("Book returned successfully! ISBN: " + isbn);
        } catch (SQLException e) {
            System.out.println("Error returning book: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @Override
    public void renewBook(String isbn, LocalDate newDueDate) {
        String sql = "UPDATE books SET due_date = ? WHERE isbn = ?";
        try (Connection conn = DatabaseHandler.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setObject(1, newDueDate);
            pstmt.setString(2, isbn);
            pstmt.executeUpdate();
            System.out.println("Book renewed successfully! ISBN: " + isbn + ", New Due Date: " + newDueDate);
        } catch (SQLException e) {
            System.out.println("Error renewing book: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @Override
    public List<Book> findBorrowedByUser(int userId) {
        List<Book> books = new ArrayList<>();
        String sql = "SELECT isbn, title, author, genre, owner_id, available, borrowed_by, borrow_date, due_date, listed FROM books WHERE CAST(borrowed_by AS INTEGER) = ?";
        try (Connection conn = DatabaseHandler.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, userId);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                Book book = new Book(
                    rs.getString("isbn"),
                    rs.getString("title"),
                    rs.getString("author"),
                    rs.getString("genre"),
                    rs.getInt("owner_id"),
                    rs.getBoolean("available"),
                    (Integer) rs.getObject("borrowed_by"),
                    rs.getObject("borrow_date", LocalDate.class),
                    rs.getObject("due_date", LocalDate.class),
                    rs.getBoolean("listed")
                );
                books.add(book);
            }
        } catch (SQLException e) {
            System.out.println("Error fetching borrowed books: " + e.getMessage());
            e.printStackTrace();
        }
        return books;
    }

    @Override
    public List<Book> findOverdueBooks() {
        List<Book> books = new ArrayList<>();
        String sql = "SELECT isbn, title, author, genre, owner_id, available, borrowed_by, borrow_date, due_date, listed FROM books WHERE due_date < ? AND borrowed_by IS NOT NULL";
        try (Connection conn = DatabaseHandler.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setObject(1, LocalDate.now());
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                Book book = new Book(
                    rs.getString("isbn"),
                    rs.getString("title"),
                    rs.getString("author"),
                    rs.getString("genre"),
                    rs.getInt("owner_id"),
                    rs.getBoolean("available"),
                    (Integer) rs.getObject("borrowed_by"),
                    rs.getObject("borrow_date", LocalDate.class),
                    rs.getObject("due_date", LocalDate.class),
                    rs.getBoolean("listed")
                );
                books.add(book);
            }
        } catch (SQLException e) {
            System.out.println("Error fetching overdue books: " + e.getMessage());
            e.printStackTrace();
        }
        return books;
    }

    // ============ LISTING OPERATIONS ============

    @Override
    public void delistBook(String isbn) {
        String sql = "UPDATE books SET listed = false WHERE isbn = ?";
        try (Connection conn = DatabaseHandler.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, isbn);
            pstmt.executeUpdate();
            System.out.println("Book delisted successfully! ISBN: " + isbn);
        } catch (SQLException e) {
            System.out.println("Error delisting book: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @Override
    public void relistBook(String isbn) {
        String sql = "UPDATE books SET listed = true WHERE isbn = ?";
        try (Connection conn = DatabaseHandler.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, isbn);
            pstmt.executeUpdate();
            System.out.println("Book relisted successfully! ISBN: " + isbn);
        } catch (SQLException e) {
            System.out.println("Error relisting book: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @Override
    public List<Book> getByOwnerId(int ownerId) {
        List<Book> books = new ArrayList<>();
        String sql = "SELECT isbn, title, author, genre, owner_id, available, borrowed_by, borrow_date, due_date, listed FROM books WHERE owner_id = ?";
        try (Connection conn = DatabaseHandler.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, ownerId);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                Book book = new Book(
                    rs.getString("isbn"),
                    rs.getString("title"),
                    rs.getString("author"),
                    rs.getString("genre"),
                    rs.getInt("owner_id"),
                    rs.getBoolean("available"),
                    (Integer) rs.getObject("borrowed_by"),
                    rs.getObject("borrow_date", LocalDate.class),
                    rs.getObject("due_date", LocalDate.class),
                    rs.getBoolean("listed")
                );
                books.add(book);
            }
        } catch (SQLException e) {
            System.out.println("Error fetching books by owner: " + e.getMessage());
        }
        return books;
    }
}
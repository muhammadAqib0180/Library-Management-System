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
    public boolean insert(Book book) {
        String sql = "INSERT INTO books(isbn, title, author, genre, owner_id, available, borrowed_by, borrow_date, due_date, listed, condition, cover_url, borrow_count) VALUES(?,?,?,?,?,?,?,?,?,?,?,?,?)";
        try (Connection conn = DatabaseHandler.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, book.getIsbn());
            pstmt.setString(2, book.getTitle());
            pstmt.setString(3, book.getAuthor());
            pstmt.setString(4, book.getGenre());
            pstmt.setInt(5, book.getOwnerId());
            pstmt.setBoolean(6, book.isAvailable());
            pstmt.setObject(7, book.getBorrowedBy() != null ? book.getBorrowedBy() : null);
            pstmt.setObject(8, book.getBorrowDate());
            pstmt.setObject(9, book.getDueDate());
            pstmt.setBoolean(10, book.isListed());
            pstmt.setString(11, book.getCondition());
            pstmt.setString(12, book.getCoverUrl());
            pstmt.setInt(13, book.getBorrowCount());

            pstmt.executeUpdate();
            System.out.println("Book saved to database with image & condition!");
            return true; // <-- ADD THIS
        } catch (SQLException e) {
            System.out.println("Error saving book: " + e.getMessage());
            e.printStackTrace();
            return false; // <-- ADD THIS
        }
    }

    @Override
    public List<Book> getAll() {
        List<Book> books = new ArrayList<>();
        // ADDED the missing columns to the SELECT statement
        String sql = "SELECT isbn, title, author, genre, owner_id, available, borrowed_by, borrow_date, due_date, listed, condition, cover_url, borrow_count FROM books";
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
                // PULL THE NEW DATA
                book.setCondition(rs.getString("condition"));
                book.setCoverUrl(rs.getString("cover_url"));
                book.setBorrowCount(rs.getInt("borrow_count"));

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
        // ADDED the missing columns to the SELECT statement
        String sql = "SELECT isbn, title, author, genre, owner_id, available, borrowed_by, borrow_date, due_date, listed, condition, cover_url, borrow_count FROM books WHERE isbn = ?";
        try (Connection conn = DatabaseHandler.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, isbn);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
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
                    // PULL THE NEW DATA
                    book.setCondition(rs.getString("condition"));
                    book.setCoverUrl(rs.getString("cover_url"));
                    book.setBorrowCount(rs.getInt("borrow_count"));
                    return book;
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
            pstmt.setObject(5, book.getBorrowedBy() != null ? book.getBorrowedBy() : null);
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
        // ADDED the missing columns to the SELECT statement
        String sql = "SELECT isbn, title, author, genre, owner_id, available, borrowed_by, borrow_date, due_date, listed, condition, cover_url, borrow_count FROM books WHERE owner_id = ?";
        try (Connection conn = DatabaseHandler.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, ownerId);
            try (ResultSet rs = pstmt.executeQuery()) {
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
                    // PULL THE NEW DATA
                    book.setCondition(rs.getString("condition"));
                    book.setCoverUrl(rs.getString("cover_url"));
                    book.setBorrowCount(rs.getInt("borrow_count"));

                    books.add(book);
                }
            }
        } catch (SQLException e) {
            System.out.println("Error fetching books by owner: " + e.getMessage());
        }
        return books;
    }

    /* ==========================================================
       SPRINT 3 — US-2 DISTRIBUTED AVAILABILITY LOOKUP
       ========================================================== */

    /**
     * US-2: Find all copies of a book grouped by lender with availability info.
     * @param isbn the book ISBN
     * @return List of BookWithLenderInfo containing grouped results
     */
    public List<BookWithLenderInfo> findGroupedByLender(String isbn) {
        String sql = """
            SELECT owner_id, COUNT(*) AS total_copies,
                   SUM(CASE WHEN available = true THEN 1 ELSE 0 END) AS available_copies,
                   MIN(due_date) AS next_return_date
            FROM books
            WHERE isbn = ? AND listed = true
            GROUP BY owner_id
            ORDER BY available_copies DESC, owner_id ASC
        """;
        List<BookWithLenderInfo> results = new ArrayList<>();
        try (Connection conn = DatabaseHandler.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, isbn);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    BookWithLenderInfo info = new BookWithLenderInfo();
                    info.lenderId = rs.getInt("owner_id");
                    info.totalCopies = rs.getInt("total_copies");
                    info.availableCopies = rs.getInt("available_copies");
                    
                    LocalDate nextReturn = rs.getObject("next_return_date", LocalDate.class);
                    if (nextReturn != null) info.nextReturnDate = nextReturn;
                    
                    // Fetch lender name from users table
                    UserDAO userDAO = new SupaUserDAO();
                    // TODO: Implement lender name lookup if User model has name field
                    
                    results.add(info);
                }
            }
        } catch (SQLException e) {
            System.err.println("[findGroupedByLender] " + e.getMessage());
        }
        return results;
    }

    /**
     * US-3: Advanced search with filters and sorting.
     * @param title search title (partial match, can be null)
     * @param genre filter by genre (null for all)
     * @param condition filter by condition: "New", "Good", "Fair" (null for all)
     * @param availableOnly if true, only available books
     * @param sortBy sort key: "newest", "title", "most_borrowed", "highest_rated" (default: newest)
     * @param limit max results per page
     * @param offset pagination offset
     * @return filtered & sorted book list
     */
    public List<Book> findByFilters(String title, String genre, String condition, 
                                     boolean availableOnly, String sortBy, 
                                     int limit, int offset) {
        StringBuilder sql = new StringBuilder("""
            SELECT isbn, title, author, genre, owner_id, available, borrowed_by, 
                   borrow_date, due_date, listed, condition, cover_url, borrow_count
            FROM books
            WHERE listed = true
            """);
        
        List<Object> params = new ArrayList<>();
        
        // Title search (partial match)
        if (title != null && !title.isBlank()) {
            sql.append(" AND (title ILIKE ? OR author ILIKE ?)");
            String searchTerm = "%" + title + "%";
            params.add(searchTerm);
            params.add(searchTerm);
        }
        
        // Genre filter
        if (genre != null && !genre.isBlank()) {
            sql.append(" AND genre = ?");
            params.add(genre);
        }
        
        // Condition filter
        if (condition != null && !condition.isBlank()) {
            sql.append(" AND condition = ?");
            params.add(condition);
        }
        
        // Availability filter
        if (availableOnly) {
            sql.append(" AND available = true");
        }
        
        // Sorting
        switch (sortBy != null ? sortBy.toLowerCase() : "newest") {
            case "title" -> sql.append(" ORDER BY title ASC");
            case "most_borrowed" -> sql.append(" ORDER BY COALESCE(borrow_count, 0) DESC, title ASC");
            case "highest_rated" -> sql.append(" ORDER BY (SELECT AVG(stars) FROM ratings WHERE target_type='BOOK' AND target_book_isbn=books.isbn) DESC");
            default -> sql.append(" ORDER BY isbn DESC"); // newest (most recent added)
        }
        
        // Pagination
        sql.append(" LIMIT ? OFFSET ?");
        params.add(limit);
        params.add(offset);
        
        List<Book> books = new ArrayList<>();
        try (Connection conn = DatabaseHandler.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            for (int i = 0; i < params.size(); i++) {
                ps.setObject(i + 1, params.get(i));
            }
            try (ResultSet rs = ps.executeQuery()) {
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
                    book.setCondition(rs.getString("condition"));
                    book.setCoverUrl(rs.getString("cover_url"));
                    book.setBorrowCount(rs.getInt("borrow_count"));
                    books.add(book);
                }
            }
        } catch (SQLException e) {
            System.err.println("[findByFilters] " + e.getMessage());
        }
        return books;
    }

    /**
     * Helper class for US-2 grouped availability results.
     */
    public static class BookWithLenderInfo {
        public int lenderId;
        public String lenderName;
        public int totalCopies;
        public int availableCopies;
        public LocalDate nextReturnDate;

        @Override
        public String toString() {
            return lenderName + " (" + availableCopies + "/" + totalCopies + " available)";
        }
    }
}
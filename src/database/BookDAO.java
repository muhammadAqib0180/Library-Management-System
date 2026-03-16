package database;
import model.Book;

import java.util.List;

import java.time.LocalDate;

public interface BookDAO {
    void insert(Book book);
    List<Book> getAll();
    Book findByIsbn(String isbn);
    boolean updateBook(Book book);
    boolean deleteBook(String isbn);
    List<Book> getByOwnerId(int ownerId);
    
    // Loan operations
    void borrowBook(String isbn, int borrowerId, LocalDate dueDate);
    void returnBook(String isbn);
    void renewBook(String isbn, LocalDate newDueDate);
    List<Book> findBorrowedByUser(int userId);
    List<Book> findOverdueBooks();
    
    // Listing/Unlisting
    void delistBook(String isbn);
    void relistBook(String isbn);
}
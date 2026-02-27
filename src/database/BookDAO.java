package database;
import model.Book;

import java.util.List;

public interface BookDAO {
    void insert(Book book);
    List<Book> getAll();
    Book findByIsbn(String isbn);
    boolean updateBook(Book book);
    boolean deleteBook(String isbn);
    List<Book> getByOwnerId(int ownerId);
}
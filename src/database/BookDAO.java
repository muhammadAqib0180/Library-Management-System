package database;
import model.Book;

import java.util.List;

public interface BookDAO {
    void insert(Book book);
    List<Book> getAll();
    Book findByIsbn(String isbn);
}
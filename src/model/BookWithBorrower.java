package model;

import javafx.beans.property.SimpleStringProperty;

/**
 * View-model for the Admin "Books & Borrowers" and "Overdue" tables.
 * Joins Book data with borrower/owner usernames resolved from the users table.
 */
public class BookWithBorrower {

    private final SimpleStringProperty isbn;
    private final SimpleStringProperty title;
    private final SimpleStringProperty author;
    private final SimpleStringProperty genre;
    private final SimpleStringProperty ownerUsername;
    private final SimpleStringProperty borrowerUsername;  // null → "Available"
    private final SimpleStringProperty borrowDate;
    private final SimpleStringProperty dueDate;
    private final SimpleStringProperty status;            // "Available" | "Borrowed" | "Overdue"
    private final SimpleStringProperty daysOverdue;       // e.g. "3 days"  (overdue panel only)

    public BookWithBorrower(String isbn, String title, String author, String genre,
                            String ownerUsername, String borrowerUsername,
                            String borrowDate, String dueDate,
                            String status, String daysOverdue) {
        this.isbn             = new SimpleStringProperty(isbn);
        this.title            = new SimpleStringProperty(title);
        this.author           = new SimpleStringProperty(author);
        this.genre            = new SimpleStringProperty(genre != null ? genre : "—");
        this.ownerUsername    = new SimpleStringProperty(ownerUsername  != null ? ownerUsername  : "Unknown");
        this.borrowerUsername = new SimpleStringProperty(borrowerUsername != null ? borrowerUsername : "Available");
        this.borrowDate       = new SimpleStringProperty(borrowDate != null ? borrowDate : "—");
        this.dueDate          = new SimpleStringProperty(dueDate    != null ? dueDate    : "—");
        this.status           = new SimpleStringProperty(status);
        this.daysOverdue      = new SimpleStringProperty(daysOverdue != null ? daysOverdue : "—");
    }

    // Properties (for TableView cell factories)
    public SimpleStringProperty isbnProperty()             { return isbn; }
    public SimpleStringProperty titleProperty()            { return title; }
    public SimpleStringProperty authorProperty()           { return author; }
    public SimpleStringProperty genreProperty()            { return genre; }
    public SimpleStringProperty ownerUsernameProperty()    { return ownerUsername; }
    public SimpleStringProperty borrowerUsernameProperty() { return borrowerUsername; }
    public SimpleStringProperty borrowDateProperty()       { return borrowDate; }
    public SimpleStringProperty dueDateProperty()          { return dueDate; }
    public SimpleStringProperty statusProperty()           { return status; }
    public SimpleStringProperty daysOverdueProperty()      { return daysOverdue; }

    // Plain getters
    public String getIsbn()             { return isbn.get(); }
    public String getTitle()            { return title.get(); }
    public String getAuthor()           { return author.get(); }
    public String getGenre()            { return genre.get(); }
    public String getOwnerUsername()    { return ownerUsername.get(); }
    public String getBorrowerUsername() { return borrowerUsername.get(); }
    public String getBorrowDate()       { return borrowDate.get(); }
    public String getDueDate()          { return dueDate.get(); }
    public String getStatus()           { return status.get(); }
    public String getDaysOverdue()      { return daysOverdue.get(); }
}
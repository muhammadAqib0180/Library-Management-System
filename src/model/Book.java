package model;

public class Book {
    private String isbn;
    private String title;
    private String author;
    private String genre;
    private int ownerId;
    private boolean available;
    private String borrowedBy;

    // Constructor for new book (called from UI)
    public Book(String isbn, String title, String author, String genre, int ownerId) {
        this.isbn = isbn;
        this.title = title;
        this.author = author;
        this.genre = genre;
        this.ownerId = ownerId;
        this.available = true;
        this.borrowedBy = null;
    }

    // Constructor for retrieving from database
    public Book(String isbn, String title, String author, String genre, int ownerId, 
                boolean available, String borrowedBy) {
        this.isbn = isbn;
        this.title = title;
        this.author = author;
        this.genre = genre;
        this.ownerId = ownerId;
        this.available = available;
        this.borrowedBy = borrowedBy;
    }

    // Getters
    public String getIsbn() { return isbn; }
    public String getTitle() { return title; }
    public String getAuthor() { return author; }
    public String getGenre() { return genre; }
    public int getOwnerId() { return ownerId; }
    public boolean isAvailable() { return available; }
    public String getBorrowedBy() { return borrowedBy; }
}
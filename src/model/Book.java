package model;

import java.time.LocalDate;

public class Book {
    private String isbn;
    private String title;
    private String author;
    private String genre;
    private int ownerId;
    private boolean available;
    private Integer borrowedBy;
    private LocalDate borrowDate;
    private LocalDate dueDate;
    private boolean listed = true;

    // Sprint 3 additions
    private String condition = "Good";   // New / Good / Fair
    private String coverUrl;              // remote image url (optional)
    private int borrowCount;              // US-5 / US-3 sort

    public Book() {}

    public Book(String isbn, String title, String author, String genre, int ownerId) {
        this.isbn = isbn;
        this.title = title;
        this.author = author;
        this.genre = genre;
        this.ownerId = ownerId;
        this.available = true;
    }

    // Overloaded constructor for complete book data (Sprint 2 compatibility)
    public Book(String isbn, String title, String author, String genre, int ownerId,
                boolean available, Integer borrowedBy) {
        this.isbn = isbn;
        this.title = title;
        this.author = author;
        this.genre = genre;
        this.ownerId = ownerId;
        this.available = available;
        this.borrowedBy = borrowedBy;
    }

    // Overloaded constructor with all fields (Sprint 2 full compatibility)
    public Book(String isbn, String title, String author, String genre, int ownerId,
                boolean available, Integer borrowedBy, LocalDate borrowDate, 
                LocalDate dueDate, boolean listed) {
        this.isbn = isbn;
        this.title = title;
        this.author = author;
        this.genre = genre;
        this.ownerId = ownerId;
        this.available = available;
        this.borrowedBy = borrowedBy;
        this.borrowDate = borrowDate;
        this.dueDate = dueDate;
        this.listed = listed;
    }

    // Sprint 3: The "Everything" Constructor
    public Book(String isbn, String title, String author, String genre, int ownerId,
                boolean available, Integer borrowedBy, LocalDate borrowDate,
                LocalDate dueDate, boolean listed, String condition, String coverUrl, int borrowCount) {
        this.isbn = isbn;
        this.title = title;
        this.author = author;
        this.genre = genre;
        this.ownerId = ownerId;
        this.available = available;
        this.borrowedBy = borrowedBy;
        this.borrowDate = borrowDate;
        this.dueDate = dueDate;
        this.listed = listed;
        this.condition = condition;
        this.coverUrl = coverUrl;
        this.borrowCount = borrowCount;
    }

    public String getIsbn() { return isbn; }
    public void setIsbn(String isbn) { this.isbn = isbn; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getAuthor() { return author; }
    public void setAuthor(String author) { this.author = author; }
    public String getGenre() { return genre; }
    public void setGenre(String genre) { this.genre = genre; }
    public int getOwnerId() { return ownerId; }
    public void setOwnerId(int ownerId) { this.ownerId = ownerId; }
    public boolean isAvailable() { return available; }
    public void setAvailable(boolean available) { this.available = available; }
    public Integer getBorrowedBy() { return borrowedBy; }
    public void setBorrowedBy(Integer borrowedBy) { this.borrowedBy = borrowedBy; }
    public LocalDate getBorrowDate() { return borrowDate; }
    public void setBorrowDate(LocalDate borrowDate) { this.borrowDate = borrowDate; }
    public LocalDate getDueDate() { return dueDate; }
    public void setDueDate(LocalDate dueDate) { this.dueDate = dueDate; }
    public boolean isListed() { return listed; }
    public void setListed(boolean listed) { this.listed = listed; }

    public String getCondition() { return condition; }
    public void setCondition(String condition) { this.condition = condition; }
    public String getCoverUrl() { return coverUrl; }
    public void setCoverUrl(String coverUrl) { this.coverUrl = coverUrl; }
    public int getBorrowCount() { return borrowCount; }
    public void setBorrowCount(int borrowCount) { this.borrowCount = borrowCount; }
}
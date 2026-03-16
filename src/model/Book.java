package model;

import java.time.LocalDate;

public class Book {
    private String isbn;
    private String title;
    private String author;
    private String genre;
    private int ownerId;
    private boolean available;
    private Integer borrowedById;      // Changed from String to Integer (user ID)
    private LocalDate borrowDate;      // NEW: When the book was borrowed
    private LocalDate dueDate;         // NEW: When the book is due back
    private boolean listed;            // NEW: Whether the book is listed in catalog

    // Constructor for new book (called from UI)
    public Book(String isbn, String title, String author, String genre, int ownerId) {
        this.isbn = isbn;
        this.title = title;
        this.author = author;
        this.genre = genre;
        this.ownerId = ownerId;
        this.available = true;
        this.borrowedById = null;
        this.borrowDate = null;
        this.dueDate = null;
        this.listed = true;
    }

    // Constructor for retrieving from database (old format - for backward compatibility)
    public Book(String isbn, String title, String author, String genre, int ownerId, 
                boolean available, Integer borrowedById) {
        this.isbn = isbn;
        this.title = title;
        this.author = author;
        this.genre = genre;
        this.ownerId = ownerId;
        this.available = available;
        this.borrowedById = borrowedById;
        this.borrowDate = null;
        this.dueDate = null;
        this.listed = true;
    }

    // Constructor for retrieving from database (new format - with all fields)
    public Book(String isbn, String title, String author, String genre, int ownerId, 
                boolean available, Integer borrowedById, LocalDate borrowDate, 
                LocalDate dueDate, boolean listed) {
        this.isbn = isbn;
        this.title = title;
        this.author = author;
        this.genre = genre;
        this.ownerId = ownerId;
        this.available = available;
        this.borrowedById = borrowedById;
        this.borrowDate = borrowDate;
        this.dueDate = dueDate;
        this.listed = listed;
    }

    // Getters
    public String getIsbn() { return isbn; }
    public String getTitle() { return title; }
    public String getAuthor() { return author; }
    public String getGenre() { return genre; }
    public int getOwnerId() { return ownerId; }
    public boolean isAvailable() { return available; }
    public Integer getBorrowedById() { return borrowedById; }
    public LocalDate getBorrowDate() { return borrowDate; }
    public LocalDate getDueDate() { return dueDate; }
    public boolean isListed() { return listed; }
    
    // Legacy getter for backward compatibility
    public String getBorrowedBy() { 
        return borrowedById != null ? borrowedById.toString() : null;
    }

    // Setters
    public void setAvailable(boolean available) { this.available = available; }
    public void setBorrowedById(Integer borrowedById) { this.borrowedById = borrowedById; }
    public void setBorrowDate(LocalDate borrowDate) { this.borrowDate = borrowDate; }
    public void setDueDate(LocalDate dueDate) { this.dueDate = dueDate; }
    public void setListed(boolean listed) { this.listed = listed; }
}
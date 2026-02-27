package model;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

/**
 * Extends the existing Book model to include loan-specific details.
 * This follows the inheritance pattern to maintain project consistency.
 */
public class BorrowedBook extends Book {
    private final StringProperty borrowDate;
    private final StringProperty dueDate;
    private final StringProperty status;

    public BorrowedBook(String isbn, String title, String author, String borrowDate, String dueDate, String status) {
        // Calls the constructor of the existing Book class
        super(isbn, title, author);
        this.borrowDate = new SimpleStringProperty(borrowDate);
        this.dueDate = new SimpleStringProperty(dueDate);
        this.status = new SimpleStringProperty(status);
    }

    // --- Property Methods (Required for TableView Data Binding) ---

    public StringProperty borrowDateProperty() {
        return borrowDate;
    }

    public StringProperty dueDateProperty() {
        return dueDate;
    }

    public StringProperty statusProperty() {
        return status;
    }

    /**
     * Helper to wrap the parent title in a Property for the TableView.
     */
    public StringProperty titleProperty() {
        return new SimpleStringProperty(getTitle());
    }

    /**
     * Helper to wrap the parent author in a Property for the TableView.
     */
    public StringProperty authorProperty() {
        return new SimpleStringProperty(getAuthor());
    }

    // --- Standard Getters ---

    public String getBorrowDate() { return borrowDate.get(); }
    public String getDueDate() { return dueDate.get(); }
    public String getStatus() { return status.get(); }
}
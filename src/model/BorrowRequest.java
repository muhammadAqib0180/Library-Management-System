package model;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class BorrowRequest {
    private Integer requestId;
    private String bookIsbn;
    private Integer borrowerId;
    private Integer lenderId;  // Owner of the book
    private String status;     // PENDING, ACCEPTED, REJECTED, CANCELLED
    private LocalDate requestedDueDate;
    private LocalDateTime createdAt;
    private LocalDateTime respondedAt;  // When lender accepted/rejected
    private String rejectionReason;    // Optional: why lender rejected

    // Constructors
    public BorrowRequest() {
        this.status = "PENDING";
    }

    public BorrowRequest(String bookIsbn, Integer borrowerId, Integer lenderId, LocalDate requestedDueDate) {
        this.bookIsbn = bookIsbn;
        this.borrowerId = borrowerId;
        this.lenderId = lenderId;
        this.requestedDueDate = requestedDueDate;
        this.status = "PENDING";
        this.createdAt = LocalDateTime.now();
    }

    public BorrowRequest(Integer requestId, String bookIsbn, Integer borrowerId, Integer lenderId,
                        String status, LocalDate requestedDueDate, LocalDateTime createdAt,
                        LocalDateTime respondedAt, String rejectionReason) {
        this.requestId = requestId;
        this.bookIsbn = bookIsbn;
        this.borrowerId = borrowerId;
        this.lenderId = lenderId;
        this.status = status;
        this.requestedDueDate = requestedDueDate;
        this.createdAt = createdAt;
        this.respondedAt = respondedAt;
        this.rejectionReason = rejectionReason;
    }

    // Getters and Setters
    public Integer getRequestId() {
        return requestId;
    }

    public void setRequestId(Integer requestId) {
        this.requestId = requestId;
    }

    public String getBookIsbn() {
        return bookIsbn;
    }

    public void setBookIsbn(String bookIsbn) {
        this.bookIsbn = bookIsbn;
    }

    public Integer getBorrowerId() {
        return borrowerId;
    }

    public void setBorrowerId(Integer borrowerId) {
        this.borrowerId = borrowerId;
    }

    public Integer getLenderId() {
        return lenderId;
    }

    public void setLenderId(Integer lenderId) {
        this.lenderId = lenderId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public LocalDate getRequestedDueDate() {
        return requestedDueDate;
    }

    public void setRequestedDueDate(LocalDate requestedDueDate) {
        this.requestedDueDate = requestedDueDate;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getRespondedAt() {
        return respondedAt;
    }

    public void setRespondedAt(LocalDateTime respondedAt) {
        this.respondedAt = respondedAt;
    }

    public String getRejectionReason() {
        return rejectionReason;
    }

    public void setRejectionReason(String rejectionReason) {
        this.rejectionReason = rejectionReason;
    }

    @Override
    public String toString() {
        return "BorrowRequest{" +
                "requestId=" + requestId +
                ", bookIsbn='" + bookIsbn + '\'' +
                ", borrowerId=" + borrowerId +
                ", status='" + status + '\'' +
                ", requestedDueDate=" + requestedDueDate +
                ", createdAt=" + createdAt +
                '}';
    }
}

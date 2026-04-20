package model;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class BorrowRequest {
    private int requestId;
    private String bookIsbn;
    private int borrowerId;
    private int lenderId;
    private String status;
    private LocalDate requestedDueDate;
    private LocalDateTime createdAt;
    private LocalDateTime respondedAt;
    private String rejectionReason;

    // Sprint 3 — US-1
    private HandoverDetails handover = new HandoverDetails();
    private LocalDateTime receivedAt;   // when borrower confirmed receipt — T-2
    private LocalDateTime returnedAt;

    public BorrowRequest() {}

    // Constructor for creating new borrow requests (UI creation)
    public BorrowRequest(String bookIsbn, int borrowerId, int lenderId, LocalDate requestedDueDate) {
        this.bookIsbn = bookIsbn;
        this.borrowerId = borrowerId;
        this.lenderId = lenderId;
        this.requestedDueDate = requestedDueDate;
        this.status = "PENDING";
        this.createdAt = LocalDateTime.now();
    }

    // Constructor for loading from database (full object)
    public BorrowRequest(int requestId, String bookIsbn, int borrowerId, int lenderId,
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

    public int getRequestId() { return requestId; }
    public void setRequestId(int requestId) { this.requestId = requestId; }
    public String getBookIsbn() { return bookIsbn; }
    public void setBookIsbn(String bookIsbn) { this.bookIsbn = bookIsbn; }
    public int getBorrowerId() { return borrowerId; }
    public void setBorrowerId(int borrowerId) { this.borrowerId = borrowerId; }
    public int getLenderId() { return lenderId; }
    public void setLenderId(int lenderId) { this.lenderId = lenderId; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public HandoverStatus getHandoverStatus() { return HandoverStatus.fromString(status); }

    public LocalDate getRequestedDueDate() { return requestedDueDate; }
    public void setRequestedDueDate(LocalDate requestedDueDate) { this.requestedDueDate = requestedDueDate; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getRespondedAt() { return respondedAt; }
    public void setRespondedAt(LocalDateTime respondedAt) { this.respondedAt = respondedAt; }
    public String getRejectionReason() { return rejectionReason; }
    public void setRejectionReason(String rejectionReason) { this.rejectionReason = rejectionReason; }

    public HandoverDetails getHandover() { return handover; }
    public void setHandover(HandoverDetails handover) { this.handover = handover; }
    public LocalDateTime getReceivedAt() { return receivedAt; }
    public void setReceivedAt(LocalDateTime receivedAt) { this.receivedAt = receivedAt; }
    public LocalDateTime getReturnedAt() { return returnedAt; }
    public void setReturnedAt(LocalDateTime returnedAt) { this.returnedAt = returnedAt; }
    public String getOtp() {
        return (this.handover != null) ? this.handover.getOtp() : null;
    }
}

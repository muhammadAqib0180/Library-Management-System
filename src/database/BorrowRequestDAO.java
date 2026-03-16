package database;

import model.BorrowRequest;
import java.util.List;

public interface BorrowRequestDAO {
    /**
     * Create a new borrow request when a borrower requests a book
     * @param request BorrowRequest object with bookIsbn, borrowerId, lenderId, requestedDueDate
     * @return request ID if successful, -1 if failed
     */
    int createBorrowRequest(BorrowRequest request);

    /**
     * Get all pending requests for a lender's books
     * @param lenderId User ID of the lender
     * @return List of pending BorrowRequest objects
     */
    List<BorrowRequest> getPendingRequestsForLender(int lenderId);

    /**
     * Get all requests (any status) made by a borrower
     * @param borrowerId User ID of the borrower
     * @return List of BorrowRequest objects
     */
    List<BorrowRequest> getMyRequests(int borrowerId);

    /**
     * Lender accepts a borrow request
     * @param requestId ID of the request to accept
     * @return true if accepted successfully
     */
    boolean acceptBorrowRequest(int requestId);

    /**
     * Lender rejects a borrow request with optional reason
     * @param requestId ID of the request to reject
     * @param rejectionReason Optional reason for rejection
     * @return true if rejected successfully
     */
    boolean rejectBorrowRequest(int requestId, String rejectionReason);

    /**
     * Get a specific borrow request by ID
     * @param requestId ID of the request
     * @return BorrowRequest object or null if not found
     */
    BorrowRequest getRequestById(int requestId);

    /**
     * Cancel a borrow request (borrower cancels before lender responds)
     * @param requestId ID of the request to cancel
     * @return true if cancelled successfully
     */
    boolean cancelBorrowRequest(int requestId);
}

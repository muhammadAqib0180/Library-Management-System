package database;

import model.BorrowRequest;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class SupaBorrowRequestDAO implements BorrowRequestDAO {

    @Override
    public int createBorrowRequest(BorrowRequest request) {
        String sql = """
            INSERT INTO borrow_requests (book_isbn, borrower_id, lender_id, status, requested_due_date, created_at)
            VALUES (?, ?, ?, ?, ?, NOW())
            RETURNING request_id
        """;
        
        try (Connection conn = DatabaseHandler.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, request.getBookIsbn());
            pstmt.setInt(2, request.getBorrowerId());
            pstmt.setInt(3, request.getLenderId());
            pstmt.setString(4, "PENDING");
            pstmt.setDate(5, java.sql.Date.valueOf(request.getRequestedDueDate()));
            
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("request_id");
                }
            }
        } catch (SQLException e) {
            System.err.println("Error creating borrow request: " + e.getMessage());
        }
        return -1;
    }

    @Override
    public List<BorrowRequest> getPendingRequestsForLender(int lenderId) {
        List<BorrowRequest> requests = new ArrayList<>();
        String sql = """
            SELECT request_id, book_isbn, borrower_id, lender_id, status, requested_due_date, 
                   created_at, responded_at, rejection_reason
            FROM borrow_requests
            WHERE lender_id = ? AND status = 'PENDING'
            ORDER BY created_at DESC
        """;
        
        try (Connection conn = DatabaseHandler.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, lenderId);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    BorrowRequest req = mapResultSetToBorrowRequest(rs);
                    requests.add(req);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error fetching pending requests for lender: " + e.getMessage());
        }
        return requests;
    }

    @Override
    public List<BorrowRequest> getMyRequests(int borrowerId) {
        List<BorrowRequest> requests = new ArrayList<>();
        String sql = """
            SELECT request_id, book_isbn, borrower_id, lender_id, status, requested_due_date, 
                   created_at, responded_at, rejection_reason
            FROM borrow_requests
            WHERE borrower_id = ?
            ORDER BY created_at DESC
        """;
        
        try (Connection conn = DatabaseHandler.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, borrowerId);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    BorrowRequest req = mapResultSetToBorrowRequest(rs);
                    requests.add(req);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error fetching borrower requests: " + e.getMessage());
        }
        return requests;
    }

    @Override
    public boolean acceptBorrowRequest(int requestId) {
        // First, get the request details
        BorrowRequest request = getRequestById(requestId);
        if (request == null) {
            return false;
        }
        
        try (Connection conn = DatabaseHandler.getConnection()) {
            conn.setAutoCommit(false);
            
            try {
                // Update request status
                String updateRequestSql = """
                    UPDATE borrow_requests
                    SET status = 'ACCEPTED', responded_at = NOW()
                    WHERE request_id = ?
                """;
                try (PreparedStatement pstmt = conn.prepareStatement(updateRequestSql)) {
                    pstmt.setInt(1, requestId);
                    pstmt.executeUpdate();
                }
                
                // Actually borrow the book (update books table)
                String borrowBookSql = """
                    UPDATE books
                    SET borrowed_by = ?, borrow_date = NOW(), due_date = ?, available = false
                    WHERE isbn = ?
                """;
                try (PreparedStatement pstmt = conn.prepareStatement(borrowBookSql)) {
                    pstmt.setInt(1, request.getBorrowerId());
                    pstmt.setDate(2, java.sql.Date.valueOf(request.getRequestedDueDate()));
                    pstmt.setString(3, request.getBookIsbn());
                    pstmt.executeUpdate();
                }
                
                conn.commit();
                return true;
            } catch (SQLException e) {
                conn.rollback();
                System.err.println("Error accepting borrow request: " + e.getMessage());
                throw e;
            } finally {
                conn.setAutoCommit(true);
            }
        } catch (SQLException e) {
            System.err.println("Error in acceptBorrowRequest: " + e.getMessage());
            return false;
        }
    }

    @Override
    public boolean rejectBorrowRequest(int requestId, String rejectionReason) {
        String sql = """
            UPDATE borrow_requests
            SET status = 'REJECTED', responded_at = NOW(), rejection_reason = ?
            WHERE request_id = ?
        """;
        
        try (Connection conn = DatabaseHandler.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, rejectionReason);
            pstmt.setInt(2, requestId);
            
            int rowsAffected = pstmt.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            System.err.println("Error rejecting borrow request: " + e.getMessage());
            return false;
        }
    }

    @Override
    public BorrowRequest getRequestById(int requestId) {
        String sql = """
            SELECT request_id, book_isbn, borrower_id, lender_id, status, requested_due_date, 
                   created_at, responded_at, rejection_reason
            FROM borrow_requests
            WHERE request_id = ?
        """;
        
        try (Connection conn = DatabaseHandler.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, requestId);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToBorrowRequest(rs);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error fetching borrow request by ID: " + e.getMessage());
        }
        return null;
    }

    @Override
    public boolean cancelBorrowRequest(int requestId) {
        String sql = """
            UPDATE borrow_requests
            SET status = 'CANCELLED', responded_at = NOW()
            WHERE request_id = ? AND status = 'PENDING'
        """;
        
        try (Connection conn = DatabaseHandler.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, requestId);
            int rowsAffected = pstmt.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            System.err.println("Error cancelling borrow request: " + e.getMessage());
            return false;
        }
    }

    private BorrowRequest mapResultSetToBorrowRequest(ResultSet rs) throws SQLException {
        return new BorrowRequest(
            rs.getInt("request_id"),
            rs.getString("book_isbn"),
            rs.getInt("borrower_id"),
            rs.getInt("lender_id"),
            rs.getString("status"),
            rs.getDate("requested_due_date").toLocalDate(),
            rs.getTimestamp("created_at").toLocalDateTime(),
            rs.getTimestamp("responded_at") != null ? rs.getTimestamp("responded_at").toLocalDateTime() : null,
            rs.getString("rejection_reason")
        );
    }
}

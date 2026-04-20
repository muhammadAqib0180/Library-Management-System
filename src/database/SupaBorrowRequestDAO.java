package database;

import model.BorrowRequest;
import model.HandoverDetails;
import model.HandoverStatus;
import util.AuditLogger;
import util.OtpService;
import model.AuditLog;

import java.sql.*;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

public class SupaBorrowRequestDAO implements BorrowRequestDAO {

    /* ==========================================================
       SELECT column lists — keep in sync
       ========================================================== */
    private static final String ALL_COLS = """
        request_id, book_isbn, borrower_id, lender_id, status, requested_due_date,
        created_at, responded_at, rejection_reason,
        handover_method, otp, return_otp, meetup_location, meetup_time,
        courier_service, courier_person, vehicle_plate, vehicle_type, proof_image_url,
        return_courier_service, return_courier_person, return_vehicle_plate,
        return_vehicle_type, return_proof_image_url,
        received_at, returned_at
    """;

    /* ==========================================================
       CRUD
       ========================================================== */

    @Override
    public int createBorrowRequest(BorrowRequest request) {
        String sql = """
            INSERT INTO borrow_requests (book_isbn, borrower_id, lender_id, status, requested_due_date, created_at)
            VALUES (?, ?, ?, ?, ?, NOW())
            RETURNING request_id
        """;
        try (Connection conn = DatabaseHandler.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, request.getBookIsbn());
            ps.setInt(2, request.getBorrowerId());
            ps.setInt(3, request.getLenderId());
            ps.setString(4, HandoverStatus.PENDING.name());
            ps.setDate(5, request.getRequestedDueDate() == null ? null : Date.valueOf(request.getRequestedDueDate()));
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    int id = rs.getInt(1);
                    recordState(conn, id, null, HandoverStatus.PENDING, request.getBorrowerId(), "Request created");
                    AuditLogger.log(AuditLog.Action.REQUEST_CREATED, "BORROW_REQUEST", id,
                            "book=" + request.getBookIsbn());
                    return id;
                }
            }
        } catch (SQLException e) {
            System.err.println("[createBorrowRequest] " + e.getMessage());
        }
        return -1;
    }

    @Override
    public List<BorrowRequest> getPendingRequestsForLender(int lenderId) {
        return findByWhere("WHERE lender_id = ? AND status = 'PENDING' ORDER BY created_at DESC",
                ps -> ps.setInt(1, lenderId));
    }

    @Override
    public List<BorrowRequest> getMyRequests(int borrowerId) {
        return findByWhere("WHERE borrower_id = ? ORDER BY created_at DESC",
                ps -> ps.setInt(1, borrowerId));
    }

    /** All active (non-terminal) requests involving this user (borrower or lender). */
    public List<BorrowRequest> getActiveRequestsFor(int userId) {
        return findByWhere("""
                WHERE (borrower_id = ? OR lender_id = ?)
                  AND status NOT IN ('RETURNED','REJECTED','CANCELLED')
                ORDER BY created_at DESC
                """, ps -> { ps.setInt(1, userId); ps.setInt(2, userId); });
    }

    @Override
    public BorrowRequest getRequestById(int requestId) {
        List<BorrowRequest> list = findByWhere("WHERE request_id = ?", ps -> ps.setInt(1, requestId));
        return list.isEmpty() ? null : list.get(0);
    }

    @Override
    public boolean acceptBorrowRequest(int requestId) {
        // Sprint 3 — T-2: accepting NO LONGER activates the borrow.
        // It now moves the request to ACCEPTED (awaiting the lender to choose handover method).
        String sql = """
            UPDATE borrow_requests
            SET status = 'ACCEPTED', responded_at = NOW()
            WHERE request_id = ? AND status = 'PENDING'
        """;
        try (Connection conn = DatabaseHandler.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, requestId);
            if (ps.executeUpdate() <= 0) return false;
            recordState(conn, requestId, HandoverStatus.PENDING, HandoverStatus.ACCEPTED, null, "Request accepted");
            AuditLogger.log(AuditLog.Action.REQUEST_ACCEPTED, "BORROW_REQUEST", requestId, null);
            return true;
        } catch (SQLException e) {
            System.err.println("[acceptBorrowRequest] " + e.getMessage());
            return false;
        }
    }

    @Override
    public boolean rejectBorrowRequest(int requestId, String rejectionReason) {
        String sql = """
            UPDATE borrow_requests
            SET status = 'REJECTED', responded_at = NOW(), rejection_reason = ?
            WHERE request_id = ? AND status = 'PENDING'
        """;
        try (Connection conn = DatabaseHandler.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, rejectionReason);
            ps.setInt(2, requestId);
            if (ps.executeUpdate() <= 0) return false;
            recordState(conn, requestId, HandoverStatus.PENDING, HandoverStatus.REJECTED, null, rejectionReason);
            AuditLogger.log(AuditLog.Action.REQUEST_REJECTED, "BORROW_REQUEST", requestId, rejectionReason);
            return true;
        } catch (SQLException e) {
            System.err.println("[rejectBorrowRequest] " + e.getMessage());
            return false;
        }
    }

    @Override
    public boolean cancelBorrowRequest(int requestId) {
        String sql = """
            UPDATE borrow_requests
            SET status = 'CANCELLED', responded_at = NOW()
            WHERE request_id = ? AND status = 'PENDING'
        """;
        try (Connection conn = DatabaseHandler.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, requestId);
            if (ps.executeUpdate() <= 0) return false;
            recordState(conn, requestId, HandoverStatus.PENDING, HandoverStatus.CANCELLED, null, null);
            AuditLogger.log(AuditLog.Action.REQUEST_CANCELLED, "BORROW_REQUEST", requestId, null);
            return true;
        } catch (SQLException e) {
            System.err.println("[cancelBorrowRequest] " + e.getMessage());
            return false;
        }
    }

    /* ==========================================================
       SPRINT 3 — HANDOVER (US-1)
       ========================================================== */

    /** Lender chose MEETUP → set location, time, generate OTP. Status: AWAITING_HANDOVER. */
    public boolean initiateMeetupHandover(int requestId, String location, LocalDateTime time) {
        String otp = OtpService.generate();
        String sql = """
            UPDATE borrow_requests
            SET handover_method='MEETUP', status='AWAITING_HANDOVER',
                meetup_location=?, meetup_time=?, otp=?
            WHERE request_id=? AND status='ACCEPTED'
        """;
        try (Connection conn = DatabaseHandler.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, location);
            ps.setTimestamp(2, time == null ? null : Timestamp.valueOf(time));
            ps.setString(3, otp);
            ps.setInt(4, requestId);
            if (ps.executeUpdate() <= 0) return false;
            recordState(conn, requestId, HandoverStatus.ACCEPTED, HandoverStatus.AWAITING_HANDOVER, null,
                    "Meetup @ " + location);
            AuditLogger.log(AuditLog.Action.HANDOVER_INITIATED, "BORROW_REQUEST", requestId, "MEETUP");
            return true;
        } catch (SQLException e) {
            System.err.println("[initiateMeetupHandover] " + e.getMessage());
            return false;
        }
    }

    /** Lender chose COURIER → persist courier details. Status: IN_TRANSIT. */
    public boolean initiateCourierHandover(int requestId, HandoverDetails d) {
        String sql = """
            UPDATE borrow_requests
            SET handover_method='COURIER', status='IN_TRANSIT',
                courier_service=?, courier_person=?, vehicle_plate=?, vehicle_type=?, proof_image_url=?
            WHERE request_id=? AND status='ACCEPTED'
        """;
        try (Connection conn = DatabaseHandler.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, d.getCourierService());
            ps.setString(2, d.getCourierPerson());
            ps.setString(3, d.getVehiclePlate());
            ps.setString(4, d.getVehicleType());
            ps.setString(5, d.getProofImageUrl());
            ps.setInt(6, requestId);
            if (ps.executeUpdate() <= 0) return false;
            recordState(conn, requestId, HandoverStatus.ACCEPTED, HandoverStatus.IN_TRANSIT, null,
                    "Courier: " + d.getCourierService());
            AuditLogger.log(AuditLog.Action.HANDOVER_IN_TRANSIT, "BORROW_REQUEST", requestId, "COURIER");
            return true;
        } catch (SQLException e) {
            System.err.println("[initiateCourierHandover] " + e.getMessage());
            return false;
        }
    }

    /** Lender verifies the OTP provided by the borrower at meetup → BORROWED. Due date starts NOW. */
    public boolean verifyOtpAndMarkBorrowed(int requestId, String enteredOtp) {
        BorrowRequest r = getRequestById(requestId);
        if (r == null) return false;
        if (!OtpService.verify(r.getHandover().getOtp(), enteredOtp)) return false;
        AuditLogger.log(AuditLog.Action.OTP_VERIFIED, "BORROW_REQUEST", requestId, null);
        return markReceivedInternal(requestId, r, "OTP verified at meetup");
    }

    /** Borrower clicks "I Received the Book" after courier delivery. Due date starts NOW. */
    public boolean markReceivedByCourier(int requestId) {
        BorrowRequest r = getRequestById(requestId);
        if (r == null) return false;
        return markReceivedInternal(requestId, r, "Borrower confirmed courier delivery");
    }

    /**
     * T-2 — due date starts on RECEIVED.
     * We preserve the intended lease length: (requested_due_date − created_at) days from NOW.
     * If no requested date existed, default to 14 days.
     */
    private boolean markReceivedInternal(int requestId, BorrowRequest r, String note) {
        LocalDateTime now = LocalDateTime.now();
        long leaseDays = 14;
        if (r.getRequestedDueDate() != null && r.getCreatedAt() != null) {
            long days = ChronoUnit.DAYS.between(r.getCreatedAt().toLocalDate(), r.getRequestedDueDate());
            if (days > 0) leaseDays = days;
        }
        java.sql.Date newDueDate = Date.valueOf(now.toLocalDate().plusDays(leaseDays));

        String updReq = """
            UPDATE borrow_requests
            SET status='BORROWED', received_at=NOW(), requested_due_date=?
            WHERE request_id=?
              AND status IN ('AWAITING_HANDOVER','IN_TRANSIT')
        """;
        String updBook = """
            UPDATE books
            SET borrowed_by=?, borrow_date=?, due_date=?, available=false
            WHERE isbn=?
        """;
        String incCount = "UPDATE books SET borrow_count = COALESCE(borrow_count,0) + 1 WHERE isbn=?";

        try (Connection conn = DatabaseHandler.getConnection()) {
            conn.setAutoCommit(false);
            try (PreparedStatement ps = conn.prepareStatement(updReq)) {
                ps.setDate(1, newDueDate);
                ps.setInt(2, requestId);
                if (ps.executeUpdate() <= 0) { conn.rollback(); return false; }
            }
            try (PreparedStatement ps = conn.prepareStatement(updBook)) {
                ps.setInt(1, r.getBorrowerId());
                ps.setDate(2, Date.valueOf(now.toLocalDate()));
                ps.setDate(3, newDueDate);
                ps.setString(4, r.getBookIsbn());
                ps.executeUpdate();
            }
            try (PreparedStatement ps = conn.prepareStatement(incCount)) {
                ps.setString(1, r.getBookIsbn());
                ps.executeUpdate();
            }
            recordState(conn, requestId, r.getHandoverStatus(), HandoverStatus.BORROWED, null, note);
            conn.commit();
            AuditLogger.log(AuditLog.Action.BOOK_RECEIVED, "BORROW_REQUEST", requestId, note);
            return true;
        } catch (SQLException e) {
            System.err.println("[markReceivedInternal] " + e.getMessage());
            return false;
        }
    }

    /* ---------- RETURN FLOW ---------- */

    public boolean initiateReturnMeetup(int requestId) {
        String otp = OtpService.generate();
        String sql = """
            UPDATE borrow_requests
            SET status='RETURN_IN_TRANSIT', return_otp=?
            WHERE request_id=? AND status='BORROWED'
        """;
        try (Connection conn = DatabaseHandler.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, otp);
            ps.setInt(2, requestId);
            if (ps.executeUpdate() <= 0) return false;
            recordState(conn, requestId, HandoverStatus.BORROWED, HandoverStatus.RETURN_IN_TRANSIT, null, "Return via meetup");
            AuditLogger.log(AuditLog.Action.RETURN_INITIATED, "BORROW_REQUEST", requestId, "MEETUP");
            return true;
        } catch (SQLException e) {
            System.err.println("[initiateReturnMeetup] " + e.getMessage());
            return false;
        }
    }

    public boolean initiateReturnCourier(int requestId, HandoverDetails d) {
        String sql = """
            UPDATE borrow_requests
            SET status='RETURN_IN_TRANSIT',
                return_courier_service=?, return_courier_person=?, return_vehicle_plate=?,
                return_vehicle_type=?, return_proof_image_url=?
            WHERE request_id=? AND status='BORROWED'
        """;
        try (Connection conn = DatabaseHandler.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, d.getReturnCourierService());
            ps.setString(2, d.getReturnCourierPerson());
            ps.setString(3, d.getReturnVehiclePlate());
            ps.setString(4, d.getReturnVehicleType());
            ps.setString(5, d.getReturnProofImageUrl());
            ps.setInt(6, requestId);
            if (ps.executeUpdate() <= 0) return false;
            recordState(conn, requestId, HandoverStatus.BORROWED, HandoverStatus.RETURN_IN_TRANSIT, null,
                    "Return courier: " + d.getReturnCourierService());
            AuditLogger.log(AuditLog.Action.RETURN_INITIATED, "BORROW_REQUEST", requestId, "COURIER");
            return true;
        } catch (SQLException e) {
            System.err.println("[initiateReturnCourier] " + e.getMessage());
            return false;
        }
    }

    /** Lender confirms: either by OTP (meetup) or by clicking "I Received My Book" (courier). */
    public boolean confirmReturn(int requestId, String enteredOtpOrNull) {
        BorrowRequest r = getRequestById(requestId);
        if (r == null) return false;
        if (enteredOtpOrNull != null && !OtpService.verify(r.getHandover().getReturnOtp(), enteredOtpOrNull)) {
            return false;
        }

        String updReq = """
            UPDATE borrow_requests
            SET status='RETURNED', returned_at=NOW()
            WHERE request_id=? AND status='RETURN_IN_TRANSIT'
        """;
        String updBook = """
            UPDATE books
            SET borrowed_by=NULL, borrow_date=NULL, due_date=NULL, available=true
            WHERE isbn=?
        """;
        try (Connection conn = DatabaseHandler.getConnection()) {
            conn.setAutoCommit(false);
            try (PreparedStatement ps = conn.prepareStatement(updReq)) {
                ps.setInt(1, requestId);
                if (ps.executeUpdate() <= 0) { conn.rollback(); return false; }
            }
            try (PreparedStatement ps = conn.prepareStatement(updBook)) {
                ps.setString(1, r.getBookIsbn());
                ps.executeUpdate();
            }
            recordState(conn, requestId, HandoverStatus.RETURN_IN_TRANSIT, HandoverStatus.RETURNED, null,
                    enteredOtpOrNull != null ? "Return OTP verified" : "Lender confirmed receipt");
            conn.commit();
            AuditLogger.log(AuditLog.Action.BOOK_RETURNED, "BORROW_REQUEST", requestId, null);
            return true;
        } catch (SQLException e) {
            System.err.println("[confirmReturn] " + e.getMessage());
            return false;
        }
    }

    /* ==========================================================
       STATE HISTORY
       ========================================================== */

    public List<StateTransition> getStateHistory(int requestId) {
        String sql = """
            SELECT id, from_state, to_state, actor_id, note, created_at
            FROM state_history WHERE request_id=? ORDER BY created_at ASC
        """;
        List<StateTransition> out = new ArrayList<>();
        try (Connection conn = DatabaseHandler.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, requestId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    StateTransition t = new StateTransition();
                    t.id = rs.getInt("id");
                    t.fromState = rs.getString("from_state");
                    t.toState = rs.getString("to_state");
                    int a = rs.getInt("actor_id"); t.actorId = rs.wasNull() ? null : a;
                    t.note = rs.getString("note");
                    Timestamp ts = rs.getTimestamp("created_at");
                    if (ts != null) t.timestamp = ts.toLocalDateTime();
                    out.add(t);
                }
            }
        } catch (SQLException e) {
            System.err.println("[getStateHistory] " + e.getMessage());
        }
        return out;
    }

    private void recordState(Connection conn, int requestId, HandoverStatus from, HandoverStatus to,
                             Integer actorId, String note) {
        String sql = "INSERT INTO state_history (request_id, from_state, to_state, actor_id, note) VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, requestId);
            ps.setString(2, from == null ? null : from.name());
            ps.setString(3, to.name());
            if (actorId != null) ps.setInt(4, actorId); else ps.setNull(4, Types.INTEGER);
            ps.setString(5, note);
            ps.executeUpdate();
        } catch (SQLException ignored) {}
    }

    public static class StateTransition {
        public int id;
        public String fromState;
        public String toState;
        public Integer actorId;
        public String note;
        public LocalDateTime timestamp;
    }

    /* ==========================================================
       INTERNAL — query helper + mapping
       ========================================================== */

    @FunctionalInterface private interface PSSetter { void set(PreparedStatement ps) throws SQLException; }

    private List<BorrowRequest> findByWhere(String whereAndOrder, PSSetter setter) {
        String sql = "SELECT " + ALL_COLS + " FROM borrow_requests " + whereAndOrder;
        List<BorrowRequest> out = new ArrayList<>();
        try (Connection conn = DatabaseHandler.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            setter.set(ps);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) out.add(mapResultSetToBorrowRequest(rs));
            }
        } catch (SQLException e) {
            System.err.println("[findByWhere] " + e.getMessage());
        }
        return out;
    }

    private BorrowRequest mapResultSetToBorrowRequest(ResultSet rs) throws SQLException {
        BorrowRequest r = new BorrowRequest(
                rs.getInt("request_id"),
                rs.getString("book_isbn"),
                rs.getInt("borrower_id"),
                rs.getInt("lender_id"),
                rs.getString("status"),
                rs.getDate("requested_due_date") == null ? null : rs.getDate("requested_due_date").toLocalDate(),
                rs.getTimestamp("created_at") == null ? null : rs.getTimestamp("created_at").toLocalDateTime(),
                rs.getTimestamp("responded_at") == null ? null : rs.getTimestamp("responded_at").toLocalDateTime(),
                rs.getString("rejection_reason")
        );
        HandoverDetails h = r.getHandover();
        h.setOtp(rs.getString("otp"));
        h.setReturnOtp(rs.getString("return_otp"));
        h.setMeetupLocation(rs.getString("meetup_location"));
        Timestamp mt = rs.getTimestamp("meetup_time");
        if (mt != null) h.setMeetupTime(mt.toLocalDateTime());
        h.setCourierService(rs.getString("courier_service"));
        h.setCourierPerson(rs.getString("courier_person"));
        h.setVehiclePlate(rs.getString("vehicle_plate"));
        h.setVehicleType(rs.getString("vehicle_type"));
        h.setProofImageUrl(rs.getString("proof_image_url"));
        h.setReturnCourierService(rs.getString("return_courier_service"));
        h.setReturnCourierPerson(rs.getString("return_courier_person"));
        h.setReturnVehiclePlate(rs.getString("return_vehicle_plate"));
        h.setReturnVehicleType(rs.getString("return_vehicle_type"));
        h.setReturnProofImageUrl(rs.getString("return_proof_image_url"));
        String method = rs.getString("handover_method");
        if (method != null) {
            try { h.setMethod(HandoverDetails.Method.valueOf(method)); } catch (Exception ignored) {}
        }
        Timestamp recv = rs.getTimestamp("received_at");
        if (recv != null) r.setReceivedAt(recv.toLocalDateTime());
        Timestamp ret = rs.getTimestamp("returned_at");
        if (ret != null) r.setReturnedAt(ret.toLocalDateTime());
        return r;
    }
}

package database;

import model.Rating;
import model.RatingStats;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class SupaRatingDAO implements RatingDAO {

    @Override
    public boolean insert(Rating r) {
        String sql = """
            INSERT INTO ratings (request_id, rater_id, target_type, target_user_id, target_book_isbn, stars, comment)
            VALUES (?, ?, ?, ?, ?, ?, ?)
            ON CONFLICT DO NOTHING
        """;
        try (Connection c = DatabaseHandler.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            if (r.getRequestId() != null) ps.setInt(1, r.getRequestId()); else ps.setNull(1, Types.INTEGER);
            ps.setInt(2, r.getRaterId());
            ps.setString(3, r.getTargetType().name());
            if (r.getTargetUserId() != null) ps.setInt(4, r.getTargetUserId()); else ps.setNull(4, Types.INTEGER);
            ps.setString(5, r.getTargetBookIsbn());
            ps.setInt(6, r.getStars());
            ps.setString(7, r.getComment());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("[SupaRatingDAO.insert] " + e.getMessage());
            return false;
        }
    }

    @Override
    public RatingStats getUserStats(int userId) {
        String sql = "SELECT AVG(stars)::FLOAT AS avg, COUNT(*) AS cnt FROM ratings WHERE target_type='USER' AND target_user_id=?";
        return fetchStats(sql, ps -> ps.setInt(1, userId));
    }

    @Override
    public RatingStats getBookStats(String isbn) {
        String sql = "SELECT AVG(stars)::FLOAT AS avg, COUNT(*) AS cnt FROM ratings WHERE target_type='BOOK' AND target_book_isbn=?";
        return fetchStats(sql, ps -> ps.setString(1, isbn));
    }

    @Override
    public boolean hasRated(int requestId, int raterId, Rating.TargetType type) {
        String sql = "SELECT 1 FROM ratings WHERE request_id=? AND rater_id=? AND target_type=? LIMIT 1";
        try (Connection c = DatabaseHandler.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, requestId);
            ps.setInt(2, raterId);
            ps.setString(3, type.name());
            try (ResultSet rs = ps.executeQuery()) { return rs.next(); }
        } catch (SQLException e) {
            return false;
        }
    }

    @Override
    public List<Rating> getRatingsForUser(int userId) {
        return list("WHERE target_type='USER' AND target_user_id=? ORDER BY created_at DESC", ps -> ps.setInt(1, userId));
    }

    @Override
    public List<Rating> getRatingsForBook(String isbn) {
        return list("WHERE target_type='BOOK' AND target_book_isbn=? ORDER BY created_at DESC", ps -> ps.setString(1, isbn));
    }

    @FunctionalInterface private interface PSSetter { void set(PreparedStatement ps) throws SQLException; }

    private RatingStats fetchStats(String sql, PSSetter setter) {
        try (Connection c = DatabaseHandler.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            setter.set(ps);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return new RatingStats(rs.getDouble("avg"), rs.getInt("cnt"));
            }
        } catch (SQLException e) {
            System.err.println("[SupaRatingDAO.fetchStats] " + e.getMessage());
        }
        return new RatingStats(0.0, 0);
    }

    private List<Rating> list(String whereAndOrder, PSSetter setter) {
        String sql = """
            SELECT id, request_id, rater_id, target_type, target_user_id, target_book_isbn, stars, comment, created_at
            FROM ratings
        """ + " " + whereAndOrder;
        List<Rating> out = new ArrayList<>();
        try (Connection c = DatabaseHandler.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            setter.set(ps);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Rating r = new Rating();
                    r.setId(rs.getInt("id"));
                    int rid = rs.getInt("request_id"); r.setRequestId(rs.wasNull() ? null : rid);
                    r.setRaterId(rs.getInt("rater_id"));
                    r.setTargetType(Rating.TargetType.valueOf(rs.getString("target_type")));
                    int tu = rs.getInt("target_user_id"); r.setTargetUserId(rs.wasNull() ? null : tu);
                    r.setTargetBookIsbn(rs.getString("target_book_isbn"));
                    r.setStars(rs.getInt("stars"));
                    r.setComment(rs.getString("comment"));
                    Timestamp ts = rs.getTimestamp("created_at");
                    if (ts != null) r.setCreatedAt(ts.toLocalDateTime());
                    out.add(r);
                }
            }
        } catch (SQLException e) {
            System.err.println("[SupaRatingDAO.list] " + e.getMessage());
        }
        return out;
    }
}

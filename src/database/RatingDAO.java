package database;

import model.Rating;
import model.RatingStats;

import java.util.List;

public interface RatingDAO {
    boolean insert(Rating r);
    RatingStats getUserStats(int userId);
    RatingStats getBookStats(String isbn);
    boolean hasRated(int requestId, int raterId, Rating.TargetType type);
    List<Rating> getRatingsForUser(int userId);
    List<Rating> getRatingsForBook(String isbn);
}

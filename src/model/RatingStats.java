package model;

public class RatingStats {
    private final double average;
    private final int count;

    public RatingStats(double average, int count) {
        this.average = average;
        this.count = count;
    }

    public double getAverage() { return average; }
    public int getCount() { return count; }

    public String formatted() {
        if (count == 0) return "No ratings yet";
        return String.format("★ %.1f (%d)", average, count);
    }
}

package controller;

import javafx.fxml.FXML;
import javafx.scene.control.*;

public class SearchFilterPanelController {

    @FXML private ComboBox<String> genreCombo;
    @FXML private ComboBox<String> conditionCombo;
    @FXML private CheckBox availableOnlyCheck;
    @FXML private ComboBox<String> sortByCombo;
    @FXML private Button applyFiltersBtn;
    @FXML private Button clearFiltersBtn;

    // This is the trigger that talks to the main dashboard
    private Runnable onFilterApplied;

    @FXML
    public void initialize() {
        genreCombo.getItems().addAll("All", "Fiction", "Non-Fiction", "Mystery", "Sci-Fi", "Biography", "Textbook");
        conditionCombo.getItems().addAll("All", "Like New", "Very Good", "Good", "Acceptable");
        sortByCombo.getItems().addAll("Newest", "Title (A-Z)", "Most Borrowed", "Highest Rated");

        // Set defaults
        genreCombo.setValue("All");
        conditionCombo.setValue("All");
        sortByCombo.setValue("Newest");

        applyFiltersBtn.setOnAction(e -> {
            if (onFilterApplied != null) onFilterApplied.run();
        });

        clearFiltersBtn.setOnAction(e -> {
            genreCombo.setValue("All");
            conditionCombo.setValue("All");
            availableOnlyCheck.setSelected(false);
            sortByCombo.setValue("Newest");
            if (onFilterApplied != null) onFilterApplied.run();
        });
    }

    public void setOnFilterAppliedListener(Runnable listener) {
        this.onFilterApplied = listener;
    }

    // --- GETTERS SO THE DASHBOARD CAN READ THE VALUES ---
    public String getGenre() {
        String val = genreCombo.getValue();
        return "All".equals(val) ? null : val;
    }

    public String getCondition() {
        String val = conditionCombo.getValue();
        return "All".equals(val) ? null : val;
    }

    public boolean isAvailableOnly() {
        return availableOnlyCheck.isSelected();
    }

    public String getSortBy() {
        String val = sortByCombo.getValue();
        if ("Title (A-Z)".equals(val)) return "title";
        if ("Most Borrowed".equals(val)) return "most_borrowed";
        if ("Highest Rated".equals(val)) return "highest_rated";
        return "newest";
    }
}
package controller;

import database.BookDAO;
import database.SupaBookDAO;
import database.SupaUserDAO;
import database.UserDAO;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import model.Book;
import model.User;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ReportsController {
    @FXML private Label lblTotalBooks, lblActiveBorrows, lblOverdueCount, lblTotalUsers;
    @FXML private TableView<LenderStat> lenderTable;
    @FXML private TableColumn<LenderStat, String> colLenderName;
    @FXML private TableColumn<LenderStat, Integer> colBooksOwned;
    @FXML private TableColumn<LenderStat, Integer> colActiveLends;

    private final BookDAO bookDAO = new SupaBookDAO();
    private final UserDAO userDAO = new SupaUserDAO();

    @FXML
    public void initialize() {
        colLenderName.setCellValueFactory(d -> d.getValue().name);
        colBooksOwned.setCellValueFactory(d -> d.getValue().owned.asObject());
        colActiveLends.setCellValueFactory(d -> d.getValue().active.asObject());

        refreshStats();
    }

    @FXML
    private void refreshStats() {
        List<Book> allBooks = bookDAO.getAll();
        List<User> allUsers = userDAO.getAllUsers();
        LocalDate today = LocalDate.now();

        // 1. Calculate KPI Numbers
        long totalBooks = allBooks.size();
        long activeBorrows = allBooks.stream().filter(b -> b.getBorrowedBy() != null).count();
        long overdue = allBooks.stream()
                .filter(b -> b.getDueDate() != null && b.getDueDate().isBefore(today))
                .count();

        lblTotalBooks.setText(String.valueOf(totalBooks));
        lblActiveBorrows.setText(String.valueOf(activeBorrows));
        lblOverdueCount.setText(String.valueOf(overdue));
        lblTotalUsers.setText(String.valueOf(allUsers.size()));

        // 2. Calculate Lender Table Stats
        Map<Integer, String> userMap = allUsers.stream()
                .collect(Collectors.toMap(User::getId, User::getUsername));

        Map<Integer, List<Book>> booksByOwner = allBooks.stream()
                .collect(Collectors.groupingBy(Book::getOwnerId));

        ObservableList<LenderStat> stats = FXCollections.observableArrayList();
        booksByOwner.forEach((ownerId, books) -> {
            String name = userMap.getOrDefault(ownerId, "Unknown");
            int owned = books.size();
            int active = (int) books.stream().filter(b -> b.getBorrowedBy() != null).count();
            stats.add(new LenderStat(name, owned, active));
        });

        lenderTable.setItems(stats);
    }

    // Small inner class for table mapping
    public static class LenderStat {
        SimpleStringProperty name;
        SimpleIntegerProperty owned;
        SimpleIntegerProperty active;

        LenderStat(String n, int o, int a) {
            this.name = new SimpleStringProperty(n);
            this.owned = new SimpleIntegerProperty(o);
            this.active = new SimpleIntegerProperty(a);
        }
    }
}
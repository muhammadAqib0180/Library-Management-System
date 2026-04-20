package controller;

import database.AuditLogDAO;
import database.SupaAuditLogDAO;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import model.AuditLog;
import java.time.format.DateTimeFormatter;

public class AuditLogController {
    @FXML private TableView<AuditLog> auditTableView;
    @FXML private TableColumn<AuditLog, String> colTimestamp;
    @FXML private TableColumn<AuditLog, String> colActor;
    @FXML private TableColumn<AuditLog, String> colAction;
    @FXML private TableColumn<AuditLog, String> colTarget;
    @FXML private TableColumn<AuditLog, String> colDetails;
    @FXML private TextField auditSearchField;

    private final AuditLogDAO auditDAO = new SupaAuditLogDAO();
    private final ObservableList<AuditLog> masterData = FXCollections.observableArrayList();
    private static final DateTimeFormatter TIME_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @FXML
    public void initialize() {
        // Mapping columns to AuditLog properties
        colTimestamp.setCellValueFactory(d -> new javafx.beans.property.SimpleStringProperty(
                d.getValue().getTimestamp() != null ? d.getValue().getTimestamp().format(TIME_FMT) : "—"));

        colActor.setCellValueFactory(d -> new javafx.beans.property.SimpleStringProperty(
                d.getValue().getActorUsername() != null ? d.getValue().getActorUsername() : "System"));

        colAction.setCellValueFactory(d -> new javafx.beans.property.SimpleStringProperty(d.getValue().getActionType()));
        colTarget.setCellValueFactory(d -> new javafx.beans.property.SimpleStringProperty(
                d.getValue().getTargetType() != null ? d.getValue().getTargetType() : "—"));

        colDetails.setCellValueFactory(d -> new javafx.beans.property.SimpleStringProperty(d.getValue().getDetails()));

        // Make the "Action" column look like premium tags
        colAction.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) { setText(null); setStyle(""); return; }
                setText(item);
                setStyle("-fx-text-fill: #1E3A8A; -fx-font-weight: 800; -fx-font-size: 11px;");
            }
        });

        loadLogs();

        // High-performance search filtering
        FilteredList<AuditLog> filteredData = new FilteredList<>(masterData, p -> true);
        auditSearchField.textProperty().addListener((obs, oldVal, newVal) -> {
            filteredData.setPredicate(log -> {
                if (newVal == null || newVal.isEmpty()) return true;
                String f = newVal.toLowerCase();
                return (log.getDetails() != null && log.getDetails().toLowerCase().contains(f)) ||
                        (log.getActionType() != null && log.getActionType().toLowerCase().contains(f)) ||
                        (log.getActorUsername() != null && log.getActorUsername().toLowerCase().contains(f));
            });
        });
        auditTableView.setItems(filteredData);
    }

    private void loadLogs() {
        masterData.clear();
        // Uses your DAO's findAll() which calls search() with null filters
        masterData.addAll(auditDAO.findAll());
    }

    @FXML
    private void handleExport() {
        // Logic for CSV export could go here (US-4 nice-to-have)
        System.out.println("[System] Audit log export initiated...");
    }
}
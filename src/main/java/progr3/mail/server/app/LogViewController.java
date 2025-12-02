package progr3.mail.server.app;

import progr3.mail.server.io.JsonFileHandler;
import progr3.mail.server.model.Log;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;

import java.io.File;
import java.io.IOException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class LogViewController {

    @FXML
    private TextField searchField;

    @FXML
    private TableView<Log> logTableView;

    @FXML
    private TableColumn<Log, String> logLevelColumn;

    @FXML
    private TableColumn<Log, String> timestampColumn;

    @FXML
    private TableColumn<Log, String> requestIdColumn;

    @FXML
    private TableColumn<Log, String> messageColumn;

    @FXML
    private Label statusLabel;

    @FXML
    private Label logCountLabel;

    private final JsonFileHandler jsonFileHandler;
    private ObservableList<Log> logEntries = FXCollections.observableArrayList();
    private ObservableList<Log> filteredLogEntries = FXCollections.observableArrayList();

    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final String LOG_FILE_PATH = "data/prod/logs.json";

    public LogViewController() {
        this.jsonFileHandler = new JsonFileHandler();
    }

    @FXML
    public void initialize() {
        setupTableColumns();
        setupSearchFilter();
        loadLogData();
    }

    private void setupTableColumns() {
        // LogLevel column with color coding
        logLevelColumn.setCellValueFactory(
                cellData -> new SimpleStringProperty(cellData.getValue().getLogLevel().toString()));

        logLevelColumn.setCellFactory(column -> new TableCell<Log, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(item);
                    switch (item) {
                        case "DEBUG":
                            setStyle("-fx-text-fill: #2196F3; -fx-font-weight: bold;");
                            break;
                        case "INFO":
                            setStyle("-fx-text-fill: #4CAF50; -fx-font-weight: bold;");
                            break;
                        case "WARN":
                            setStyle("-fx-text-fill: #FF9800; -fx-font-weight: bold;");
                            break;
                        case "ERROR":
                            setStyle("-fx-text-fill: #F44336; -fx-font-weight: bold;");
                            break;
                        default:
                            setStyle("");
                    }
                }
            }
        });

        timestampColumn.setCellValueFactory(cellData -> {
            String timestamp = cellData.getValue().getTimestamp().toString();
            String formattedTime = formatTimestamp(timestamp);
            return new SimpleStringProperty(formattedTime);
        });

        timestampColumn.comparatorProperty().set((s1, s2) -> {
            Log l1 = logTableView.getItems().stream()
                    .filter(log -> formatTimestamp(log.getTimestamp().toString()).equals(s1))
                    .findFirst().orElse(null);
            Log l2 = logTableView.getItems().stream()
                    .filter(log -> formatTimestamp(log.getTimestamp().toString()).equals(s2))
                    .findFirst().orElse(null);

            return l1.getTimestamp().compareTo(l2.getTimestamp());
        });

        timestampColumn.setSortType(TableColumn.SortType.DESCENDING);

        // Other columns
        requestIdColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getRequestId()));

        messageColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getMessage()));

        // Make message column wrap text
        messageColumn.setCellFactory(tc -> {
            TableCell<Log, String> cell = new TableCell<>();
            Label label = new Label();
            label.setWrapText(true);
            label.prefWidthProperty().bind(messageColumn.widthProperty().subtract(10));
            cell.setGraphic(label);
            cell.setPrefHeight(Control.USE_COMPUTED_SIZE);
            label.textProperty().bind(cell.itemProperty());
            return cell;
        });
    }

    private String formatTimestamp(String timestamp) {
        try {
            Instant instant = Instant.parse(timestamp);
            LocalDateTime localDateTime = LocalDateTime.ofInstant(instant, ZoneId.systemDefault());
            return localDateTime.format(TIME_FORMATTER);
        } catch (Exception e) {
            return timestamp;
        }
    }

    private void setupSearchFilter() {
        searchField.textProperty().addListener((observable, oldValue, newValue) -> {
            filterLogs(newValue);
        });
    }

    private void filterLogs(String searchTerm) {
        if (searchTerm == null || searchTerm.trim().isEmpty()) {
            filteredLogEntries.setAll(logEntries);
        } else {
            String lowerCaseFilter = searchTerm.toLowerCase();
            filteredLogEntries.clear();
            for (Log entry : logEntries) {
                if (entry.getMessage().toLowerCase().contains(lowerCaseFilter) ||
                        entry.getLogLevel().toString().toLowerCase().contains(lowerCaseFilter) ||
                        entry.getRequestId().toLowerCase().contains(lowerCaseFilter)) {
                    filteredLogEntries.add(entry);
                }
            }
        }
        logTableView.setItems(filteredLogEntries);
        updateLogCount();
    }

    private void loadLogData() {
        try {
            List<Log> entries = jsonFileHandler.loadFromFile(LOG_FILE_PATH, Log.class);

            if (entries != null) {
                logEntries.setAll(entries);
                filteredLogEntries.setAll(entries);
                logTableView.setItems(filteredLogEntries);
                updateLogCount();
                logTableView.getSortOrder().clear();
                logTableView.getSortOrder().add(timestampColumn);
                logTableView.sort();
                statusLabel.setText("Logs loaded successfully");
            }
        } catch (IOException e) {
            statusLabel.setText("Error loading logs: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void updateLogCount() {
        logCountLabel.setText("Total logs: " + filteredLogEntries.size() +
                (filteredLogEntries.size() != logEntries.size() ? " (filtered from " + logEntries.size() + ")" : ""));
    }

    @FXML
    private void onRefreshClick() {
        loadLogData();
    }

    @FXML
    private void onClearClick() {
        logEntries.clear();
        filteredLogEntries.clear();
        logTableView.setItems(filteredLogEntries);
        updateLogCount();
        statusLabel.setText("Logs cleared");

        File logFile = new File(LOG_FILE_PATH);
        if (!logFile.exists()) {
            statusLabel.setText("Log file does not exist");
            return;
        }

        if (logFile.delete()) {
            statusLabel.setText("Log file cleared");
        } else {
            statusLabel.setText("Failed to clear log file");
        }
    }
}
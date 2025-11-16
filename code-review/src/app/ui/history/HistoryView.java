package app.ui.history;

import app.application.dto.FindingSummaryDTO;
import app.domain.entity.AnalysisRun;
import app.domain.value.RunStatus;
import app.domain.value.Severity;
import app.ui.common.LoadingIndicator;
import app.ui.common.SeverityBadge;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * Vista de historial de análisis.
 * Tabla superior: runs anteriores.
 * Tabla inferior: findings del run seleccionado.
 */
public class HistoryView extends BorderPane {
    
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
    
    private final HistoryController controller;
    
    // Controls
    private ComboBox<String> periodCombo;
    private Button refreshButton;
    private Button exportButton;
    private LoadingIndicator loadingIndicator;
    
    private TableView<AnalysisRun> runsTable;
    private TableView<FindingSummaryDTO> findingsTable;
    private ComboBox<String> severityFilterCombo;
    private Label findingsCountLabel;
    
    private Long currentSelectedRunId;
    private List<FindingSummaryDTO> allFindings = new ArrayList<>();
    
    public HistoryView(HistoryController controller) {
        this.controller = controller;
        initializeUI();
        loadInitialData();
    }
    
    private void initializeUI() {
        setPadding(new Insets(20));
        
        // Header
        Label titleLabel = new Label("Analysis History");
        titleLabel.setStyle("-fx-font-size: 24px; -fx-font-weight: bold;");
        
        // Period selector
        Label periodLabel = new Label("Period:");
        periodCombo = new ComboBox<>();
        periodCombo.getItems().addAll("Last 7 days", "Last 30 days", "Last 90 days");
        periodCombo.setValue("Last 30 days");
        periodCombo.setOnAction(e -> loadRuns());
        
        refreshButton = new Button("Refresh");
        refreshButton.setOnAction(e -> loadRuns());
        
        exportButton = new Button("Export CSV");
        exportButton.setOnAction(e -> exportCurrentRun());
        exportButton.setDisable(true); // Disabled until a run is selected
        
        HBox toolbar = new HBox(10, periodLabel, periodCombo, refreshButton, exportButton);
        toolbar.setAlignment(Pos.CENTER_LEFT);
        
        VBox header = new VBox(10, titleLabel, toolbar);
        header.setPadding(new Insets(0, 0, 20, 0));
        
        // Loading
        loadingIndicator = new LoadingIndicator();
        
        // Runs table
        Label runsLabel = new Label("Analysis Runs:");
        runsLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");
        runsTable = createRunsTable();
        
        VBox runsContainer = new VBox(10, runsLabel, runsTable);
        
        // Findings section
        Label findingsLabel = new Label("Findings:");
        findingsLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");
        
        Label filterLabel = new Label("Filter:");
        severityFilterCombo = new ComboBox<>();
        severityFilterCombo.getItems().addAll("ALL", "CRITICAL", "HIGH", "MEDIUM", "LOW", "INFO");
        severityFilterCombo.setValue("ALL");
        severityFilterCombo.setOnAction(e -> applyFilter());
        
        findingsCountLabel = new Label("0 findings");
        
        HBox findingsToolbar = new HBox(10, filterLabel, severityFilterCombo, findingsCountLabel);
        findingsToolbar.setAlignment(Pos.CENTER_LEFT);
        
        findingsTable = createFindingsTable();
        
        VBox findingsContainer = new VBox(10, findingsLabel, findingsToolbar, findingsTable);
        
        // Layout
        SplitPane splitPane = new SplitPane(runsContainer, findingsContainer);
        splitPane.setOrientation(javafx.geometry.Orientation.VERTICAL);
        splitPane.setDividerPositions(0.4);
        
        VBox content = new VBox(20, header, loadingIndicator, splitPane);
        VBox.setVgrow(splitPane, Priority.ALWAYS);
        
        setCenter(content);
    }
    
    @SuppressWarnings("unchecked")
    private TableView<AnalysisRun> createRunsTable() {
        TableView<AnalysisRun> table = new TableView<>();
        
        TableColumn<AnalysisRun, Long> idCol = new TableColumn<>("Run ID");
        idCol.setCellValueFactory(new PropertyValueFactory<>("id"));
        idCol.setPrefWidth(80);
        
        TableColumn<AnalysisRun, String> repoCol = new TableColumn<>("Repository");
        repoCol.setCellValueFactory(cellData -> 
            new javafx.beans.property.SimpleStringProperty(
                "Repo #" + cellData.getValue().getRepositoryId()
            )
        );
        repoCol.setPrefWidth(250);
        
        TableColumn<AnalysisRun, String> branchesCol = new TableColumn<>("Branches");
        branchesCol.setCellValueFactory(cellData -> {
            AnalysisRun run = cellData.getValue();
            String branches = run.getBaseBranch() + " → " + run.getTargetBranch();
            return new javafx.beans.property.SimpleStringProperty(branches);
        });
        branchesCol.setPrefWidth(200);
        
        TableColumn<AnalysisRun, RunStatus> statusCol = new TableColumn<>("Status");
        statusCol.setCellValueFactory(new PropertyValueFactory<>("status"));
        statusCol.setPrefWidth(100);
        
        TableColumn<AnalysisRun, String> dateCol = new TableColumn<>("Started At");
        dateCol.setCellValueFactory(cellData -> 
            new javafx.beans.property.SimpleStringProperty(
                cellData.getValue().getStartedAt().format(DATE_FORMATTER)
            )
        );
        dateCol.setPrefWidth(150);
        
        TableColumn<AnalysisRun, String> userCol = new TableColumn<>("User");
        userCol.setCellValueFactory(cellData -> 
            new javafx.beans.property.SimpleStringProperty(
                "User #" + cellData.getValue().getUserId()
            )
        );
        userCol.setPrefWidth(120);
        
        table.getColumns().addAll(idCol, repoCol, branchesCol, statusCol, dateCol, userCol);
        table.setPrefHeight(250);
        
        // Selection listener
        table.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                loadFindingsForRun(newVal.getId());
            }
        });
        
        return table;
    }
    
    @SuppressWarnings("unchecked")
    private TableView<FindingSummaryDTO> createFindingsTable() {
        TableView<FindingSummaryDTO> table = new TableView<>();
        
        TableColumn<FindingSummaryDTO, String> fileCol = new TableColumn<>("File");
        fileCol.setCellValueFactory(new PropertyValueFactory<>("filePath"));
        fileCol.setPrefWidth(300);
        
        TableColumn<FindingSummaryDTO, String> lineCol = new TableColumn<>("Line");
        lineCol.setCellValueFactory(new PropertyValueFactory<>("lineNumber"));
        lineCol.setPrefWidth(60);
        
        TableColumn<FindingSummaryDTO, String> ruleCol = new TableColumn<>("Rule");
        ruleCol.setCellValueFactory(new PropertyValueFactory<>("ruleId"));
        ruleCol.setPrefWidth(100);
        
        TableColumn<FindingSummaryDTO, Severity> severityCol = new TableColumn<>("Severity");
        severityCol.setCellValueFactory(new PropertyValueFactory<>("severity"));
        severityCol.setPrefWidth(100);
        severityCol.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(Severity severity, boolean empty) {
                super.updateItem(severity, empty);
                if (empty || severity == null) {
                    setGraphic(null);
                } else {
                    setGraphic(new SeverityBadge(severity));
                }
            }
        });
        
        TableColumn<FindingSummaryDTO, String> messageCol = new TableColumn<>("Message");
        messageCol.setCellValueFactory(new PropertyValueFactory<>("message"));
        messageCol.setPrefWidth(350);
        
        table.getColumns().addAll(fileCol, lineCol, ruleCol, severityCol, messageCol);
        table.setPrefHeight(300);
        
        return table;
    }
    
    private void loadInitialData() {
        loadRuns();
    }
    
    private void loadRuns() {
        int days = switch (periodCombo.getValue()) {
            case "Last 7 days" -> 7;
            case "Last 90 days" -> 90;
            default -> 30;
        };
        
        loadingIndicator.show("Loading runs...");
        refreshButton.setDisable(true);
        
        controller.loadRecentRuns(days, runs -> {
            runsTable.getItems().clear();
            runsTable.getItems().addAll(runs);
            loadingIndicator.hide();
            refreshButton.setDisable(false);
        });
    }
    
    private void loadFindingsForRun(Long runId) {
        currentSelectedRunId = runId;
        exportButton.setDisable(false); // Enable export button
        loadingIndicator.show("Loading findings...");
        
        controller.loadFindingsForRun(runId, findings -> {
            allFindings = findings;
            findingsTable.getItems().clear();
            findingsTable.getItems().addAll(findings);
            findingsCountLabel.setText(findings.size() + " findings");
            loadingIndicator.hide();
        });
    }
    
    private void applyFilter() {
        String filterValue = severityFilterCombo.getValue();
        Severity severity = filterValue.equals("ALL") ? null : Severity.valueOf(filterValue);
        
        controller.filterBySeverity(allFindings, severity, filtered -> {
            findingsTable.getItems().clear();
            findingsTable.getItems().addAll(filtered);
            findingsCountLabel.setText(filtered.size() + " findings");
        });
    }
    
    private void exportCurrentRun() {
        if (currentSelectedRunId == null) {
            app.ui.common.ConfirmDialog.showWarning("No Selection", "Please select an analysis run to export.");
            return;
        }
        
        exportButton.setDisable(true);
        loadingIndicator.show("Exporting to CSV...");
        
        controller.exportToCSV(currentSelectedRunId, success -> {
            loadingIndicator.hide();
            exportButton.setDisable(false);
            
            if (success) {
                app.ui.common.ConfirmDialog.showInfo("Export Complete", 
                    "Analysis results exported successfully to CSV.");
            } else {
                app.ui.common.ConfirmDialog.showWarning("Export Failed", 
                    "Failed to export analysis results.");
            }
        });
    }
}

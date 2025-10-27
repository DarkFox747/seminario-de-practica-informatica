package app.ui.analysis;

import app.application.dto.AnalysisResultDTO;
import app.application.dto.FindingSummaryDTO;
import app.domain.value.Severity;
import app.ui.common.LoadingIndicator;
import app.ui.common.SeverityBadge;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import javafx.stage.DirectoryChooser;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Vista principal de análisis (UC-01).
 * Formulario: repo path, base/target branches, botón analizar.
 * Resultados: tabla de findings con filtros por severidad.
 */
public class AnalysisView extends BorderPane {
    
    private final AnalysisController controller;
    
    // Form controls
    private TextField repoPathField;
    private Button browseButton;
    private ComboBox<String> baseBranchCombo;
    private ComboBox<String> targetBranchCombo;
    private Button analyzeButton;
    private Button refreshBranchesButton;
    
    // Results controls
    private LoadingIndicator loadingIndicator;
    private VBox resultsContainer;
    private Label summaryLabel;
    private ComboBox<String> severityFilterCombo;
    private TableView<FindingSummaryDTO> findingsTable;
    
    private List<FindingSummaryDTO> allFindings = new ArrayList<>();
    
    public AnalysisView(AnalysisController controller) {
        this.controller = controller;
        initializeUI();
    }
    
    private void initializeUI() {
        setPadding(new Insets(20));
        
        // Header
        Label titleLabel = new Label("Code Review Analysis");
        titleLabel.setStyle("-fx-font-size: 24px; -fx-font-weight: bold;");
        
        VBox header = new VBox(10, titleLabel);
        header.setPadding(new Insets(0, 0, 20, 0));
        
        // Form
        VBox formContainer = createForm();
        
        // Loading indicator
        loadingIndicator = new LoadingIndicator();
        
        // Results
        resultsContainer = createResultsContainer();
        resultsContainer.setVisible(false);
        resultsContainer.setManaged(false);
        
        // Layout
        VBox content = new VBox(20, header, formContainer, loadingIndicator, resultsContainer);
        setCenter(content);
    }
    
    private VBox createForm() {
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(10));
        
        // Repository Path
        Label repoLabel = new Label("Repository Path:");
        repoPathField = new TextField();
        repoPathField.setPrefWidth(400);
        repoPathField.setPromptText("/path/to/git/repository");
        
        browseButton = new Button("Browse...");
        browseButton.setOnAction(e -> browseRepository());
        
        HBox repoBox = new HBox(5, repoPathField, browseButton);
        HBox.setHgrow(repoPathField, Priority.ALWAYS);
        
        grid.add(repoLabel, 0, 0);
        grid.add(repoBox, 1, 0);
        
        // Base Branch
        Label baseLabel = new Label("Base Branch:");
        baseBranchCombo = new ComboBox<>();
        baseBranchCombo.setPrefWidth(200);
        baseBranchCombo.setEditable(true);
        baseBranchCombo.setPromptText("Select or type branch");
        
        grid.add(baseLabel, 0, 1);
        grid.add(baseBranchCombo, 1, 1);
        
        // Target Branch
        Label targetLabel = new Label("Target Branch:");
        targetBranchCombo = new ComboBox<>();
        targetBranchCombo.setPrefWidth(200);
        targetBranchCombo.setEditable(true);
        targetBranchCombo.setPromptText("Select or type branch");
        
        grid.add(targetLabel, 0, 2);
        grid.add(targetBranchCombo, 1, 2);
        
        // Refresh branches button
        refreshBranchesButton = new Button("Refresh Branches");
        refreshBranchesButton.setOnAction(e -> refreshBranches());
        
        grid.add(refreshBranchesButton, 1, 3);
        
        // Analyze button
        analyzeButton = new Button("Analyze");
        analyzeButton.setStyle("-fx-font-size: 14px; -fx-padding: 10 30;");
        analyzeButton.setDefaultButton(true);
        analyzeButton.setOnAction(e -> onAnalyze());
        
        HBox buttonBox = new HBox(analyzeButton);
        buttonBox.setAlignment(Pos.CENTER_RIGHT);
        buttonBox.setPadding(new Insets(10, 0, 0, 0));
        
        VBox formContainer = new VBox(10, grid, buttonBox);
        formContainer.setStyle("-fx-border-color: #ccc; -fx-border-width: 1; -fx-border-radius: 5; -fx-background-color: #f9f9f9; -fx-background-radius: 5;");
        formContainer.setPadding(new Insets(15));
        
        return formContainer;
    }
    
    private VBox createResultsContainer() {
        // Summary
        summaryLabel = new Label();
        summaryLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");
        
        // Filter
        Label filterLabel = new Label("Filter by Severity:");
        severityFilterCombo = new ComboBox<>();
        severityFilterCombo.getItems().addAll("ALL", "CRITICAL", "HIGH", "MEDIUM", "LOW", "INFO");
        severityFilterCombo.setValue("ALL");
        severityFilterCombo.setOnAction(e -> applyFilter());
        
        HBox filterBox = new HBox(10, filterLabel, severityFilterCombo);
        filterBox.setAlignment(Pos.CENTER_LEFT);
        
        // Table
        findingsTable = createFindingsTable();
        
        VBox container = new VBox(10, summaryLabel, filterBox, findingsTable);
        container.setPadding(new Insets(20, 0, 0, 0));
        
        return container;
    }
    
    @SuppressWarnings("unchecked")
    private TableView<FindingSummaryDTO> createFindingsTable() {
        TableView<FindingSummaryDTO> table = new TableView<>();
        
        // Columns
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
        messageCol.setPrefWidth(400);
        
        table.getColumns().addAll(fileCol, lineCol, ruleCol, severityCol, messageCol);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        table.setPrefHeight(400);
        
        return table;
    }
    
    private void browseRepository() {
        DirectoryChooser chooser = new DirectoryChooser();
        chooser.setTitle("Select Git Repository");
        
        File selectedDir = chooser.showDialog(getScene().getWindow());
        if (selectedDir != null) {
            repoPathField.setText(selectedDir.getAbsolutePath());
            refreshBranches();
        }
    }
    
    private void refreshBranches() {
        String repoPath = repoPathField.getText();
        controller.loadBranches(repoPath, branches -> {
            baseBranchCombo.getItems().clear();
            targetBranchCombo.getItems().clear();
            
            baseBranchCombo.getItems().addAll(branches);
            targetBranchCombo.getItems().addAll(branches);
            
            if (!branches.isEmpty()) {
                baseBranchCombo.setValue(branches.get(0));
                if (branches.size() > 1) {
                    targetBranchCombo.setValue(branches.get(1));
                }
            }
        });
    }
    
    private void onAnalyze() {
        String repoPath = repoPathField.getText();
        String baseBranch = baseBranchCombo.getValue();
        String targetBranch = targetBranchCombo.getValue();
        
        controller.performAnalysis(repoPath, baseBranch, targetBranch);
    }
    
    private void applyFilter() {
        String filterValue = severityFilterCombo.getValue();
        Severity severity = filterValue.equals("ALL") ? null : Severity.valueOf(filterValue);
        
        controller.filterBySeverity(allFindings, severity, filtered -> {
            findingsTable.getItems().clear();
            findingsTable.getItems().addAll(filtered);
        });
    }
    
    public void showLoading(String message) {
        loadingIndicator.show(message);
        analyzeButton.setDisable(true);
        resultsContainer.setVisible(false);
        resultsContainer.setManaged(false);
    }
    
    public void hideLoading() {
        loadingIndicator.hide();
        analyzeButton.setDisable(false);
    }
    
    public void displayResults(AnalysisResultDTO result) {
        // Convert findings to DTOs
        allFindings = result.getFindings().stream()
            .map(f -> {
                FindingSummaryDTO dto = new FindingSummaryDTO();
                dto.setFindingId(f.getId());
                dto.setRuleId(f.getRuleId() != null ? f.getRuleId() : "UNKNOWN");
                dto.setMessage(f.getMessage() != null ? f.getMessage() : "No description");
                dto.setSeverity(f.getSeverityFinal() != null ? f.getSeverityFinal() : Severity.INFO);
                dto.setFilePath("file.java"); // TODO: resolve from DiffFile
                dto.setLineNumber(f.getLineNumber() != null ? f.getLineNumber() : 0);
                dto.setCategory(f.getCategory());
                dto.setSuggestion(f.getSuggestion());
                return dto;
            })
            .toList();
        
        summaryLabel.setText(String.format(
            "Analysis Results: %d findings in %d files (Run ID: %d)",
            result.getTotalFindings(),
            result.getDiffFiles().size(),
            result.getRunId()
        ));
        
        findingsTable.getItems().clear();
        findingsTable.getItems().addAll(allFindings);
        
        resultsContainer.setVisible(true);
        resultsContainer.setManaged(true);
    }
}

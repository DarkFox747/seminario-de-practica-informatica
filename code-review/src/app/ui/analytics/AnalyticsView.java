package app.ui.analytics;

import app.application.dto.MetricsDTO;
import app.domain.value.Severity;
import app.ui.common.ConfirmDialog;
import app.ui.common.LoadingIndicator;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * Vista de analytics para Team Lead.
 * Muestra métricas: total findings, breakdown por severidad, stats por usuario.
 */
public class AnalyticsView extends BorderPane {
    
    private final AnalyticsController controller;
    
    // Controls
    private ComboBox<String> periodCombo;
    private Button refreshButton;
    private Button exportButton;
    private LoadingIndicator loadingIndicator;
    
    // Metrics display
    private Label totalRunsLabel;
    private Label totalFilesLabel;
    private Label totalFindingsLabel;
    private Label avgFindingsLabel;
    
    private BarChart<String, Number> severityChart;
    
    private MetricsDTO currentMetrics;
    
    public AnalyticsView(AnalyticsController controller) {
        this.controller = controller;
        initializeUI();
        loadInitialData();
    }
    
    private void initializeUI() {
        setPadding(new Insets(20));
        
        // Header
        Label titleLabel = new Label("Analytics Dashboard");
        titleLabel.setStyle("-fx-font-size: 24px; -fx-font-weight: bold;");
        
        // Period selector
        Label periodLabel = new Label("Period:");
        periodCombo = new ComboBox<>();
        periodCombo.getItems().addAll("Last 7 days", "Last 30 days", "Last 90 days", "All time");
        periodCombo.setValue("Last 30 days");
        periodCombo.setOnAction(e -> loadMetrics());
        
        refreshButton = new Button("Refresh");
        refreshButton.setOnAction(e -> loadMetrics());
        
        exportButton = new Button("Export CSV");
        exportButton.setOnAction(e -> exportMetrics());
        
        HBox toolbar = new HBox(10, periodLabel, periodCombo, refreshButton, exportButton);
        toolbar.setAlignment(Pos.CENTER_LEFT);
        
        VBox header = new VBox(10, titleLabel, toolbar);
        header.setPadding(new Insets(0, 0, 20, 0));
        
        // Loading
        loadingIndicator = new LoadingIndicator();
        
        // Summary cards
        HBox summaryCards = createSummaryCards();
        
        // Charts
        severityChart = createSeverityChart();
        
        VBox chartsContainer = new VBox(20, severityChart);
        
        // Layout
        VBox content = new VBox(20, header, loadingIndicator, summaryCards, chartsContainer);
        setCenter(content);
    }
    
    private HBox createSummaryCards() {
        totalRunsLabel = new Label("0");
        totalFilesLabel = new Label("0");
        totalFindingsLabel = new Label("0");
        avgFindingsLabel = new Label("0.0");
        
        VBox card1 = createMetricCard("Total Runs", totalRunsLabel, "#4CAF50");
        VBox card2 = createMetricCard("Files Analyzed", totalFilesLabel, "#2196F3");
        VBox card3 = createMetricCard("Total Findings", totalFindingsLabel, "#FF9800");
        VBox card4 = createMetricCard("Avg Findings/Run", avgFindingsLabel, "#9C27B0");
        
        HBox container = new HBox(20, card1, card2, card3, card4);
        container.setAlignment(Pos.CENTER);
        HBox.setHgrow(card1, Priority.ALWAYS);
        HBox.setHgrow(card2, Priority.ALWAYS);
        HBox.setHgrow(card3, Priority.ALWAYS);
        HBox.setHgrow(card4, Priority.ALWAYS);
        
        return container;
    }
    
    private VBox createMetricCard(String title, Label valueLabel, String color) {
        Label titleLabel = new Label(title);
        titleLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #666;");
        
        valueLabel.setStyle("-fx-font-size: 32px; -fx-font-weight: bold; -fx-text-fill: " + color + ";");
        
        VBox card = new VBox(10, titleLabel, valueLabel);
        card.setAlignment(Pos.CENTER);
        card.setPadding(new Insets(20));
        card.setStyle("-fx-border-color: #ddd; -fx-border-width: 1; -fx-border-radius: 8; " +
                     "-fx-background-color: white; -fx-background-radius: 8; " +
                     "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 5, 0, 0, 2);");
        
        return card;
    }
    
    private BarChart<String, Number> createSeverityChart() {
        CategoryAxis xAxis = new CategoryAxis();
        xAxis.setLabel("Severity");
        
        NumberAxis yAxis = new NumberAxis();
        yAxis.setLabel("Count");
        
        BarChart<String, Number> chart = new BarChart<>(xAxis, yAxis);
        chart.setTitle("Findings by Severity");
        chart.setLegendVisible(false);
        chart.setPrefHeight(300);
        
        return chart;
    }
    
    private void loadInitialData() {
        loadMetrics();
    }
    
    private void loadMetrics() {
        LocalDateTime since = switch (periodCombo.getValue()) {
            case "Last 7 days" -> LocalDateTime.now().minusDays(7);
            case "Last 90 days" -> LocalDateTime.now().minusDays(90);
            case "All time" -> LocalDateTime.of(2000, 1, 1, 0, 0);
            default -> LocalDateTime.now().minusDays(30);
        };
        
        loadingIndicator.show("Loading metrics...");
        refreshButton.setDisable(true);
        
        controller.loadMetrics(since, metrics -> {
            if (metrics != null) {
                currentMetrics = metrics;
                displayMetrics(metrics);
            }
            loadingIndicator.hide();
            refreshButton.setDisable(false);
        });
    }
    
    private void displayMetrics(MetricsDTO metrics) {
        // Update summary cards
        totalRunsLabel.setText(String.valueOf(metrics.getTotalRuns()));
        totalFilesLabel.setText(String.valueOf(metrics.getTotalFiles()));
        totalFindingsLabel.setText(String.valueOf(metrics.getTotalFindings()));
        avgFindingsLabel.setText(String.format("%.1f", metrics.getAvgFindingsPerRun()));
        
        // Update chart
        updateSeverityChart(metrics.getSeverityBreakdown());
    }
    
    private void updateSeverityChart(Map<String, Integer> breakdown) {
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Findings");
        
        for (Map.Entry<String, Integer> entry : breakdown.entrySet()) {
            series.getData().add(new XYChart.Data<>(entry.getKey(), entry.getValue()));
        }
        
        severityChart.getData().clear();
        severityChart.getData().add(series);
    }
    
    private void exportMetrics() {
        if (currentMetrics == null) {
            ConfirmDialog.showWarning("No Data", "No metrics to export. Please load data first.");
            return;
        }
        
        controller.exportMetrics(currentMetrics, success -> {
            if (success) {
                ConfirmDialog.showInfo("Export Complete", "Metrics exported successfully.");
            } else {
                ConfirmDialog.showWarning("Export Failed", "Failed to export metrics.");
            }
        });
    }
}

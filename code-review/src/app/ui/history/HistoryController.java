package app.ui.history;

import app.application.dto.FindingSummaryDTO;
import app.application.service.ExportService;
import app.application.service.HistoryQueryService;
import app.domain.entity.AnalysisRun;
import app.domain.value.Severity;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.stage.FileChooser;

import java.io.File;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * Controlador para la vista de historial.
 * Muestra análisis anteriores con filtros por fecha, usuario, etc.
 */
public class HistoryController {
    
    private final HistoryQueryService historyQueryService;
    private final ExportService exportService;
    private final HistoryView view;
    
    public HistoryController(HistoryQueryService historyQueryService, ExportService exportService) {
        this.historyQueryService = historyQueryService;
        this.exportService = exportService;
        this.view = new HistoryView(this);
    }
    
    public HistoryView getView() {
        return view;
    }
    
    /**
     * Carga runs recientes (últimos N días).
     */
    public void loadRecentRuns(int lastDays, Consumer<List<AnalysisRun>> callback) {
        Task<List<AnalysisRun>> task = new Task<>() {
            @Override
            protected List<AnalysisRun> call() throws Exception {
                // Use the limit as number of runs instead of days
                return historyQueryService.getRecentRunEntities(lastDays);
            }
        };
        
        task.setOnSucceeded(event -> Platform.runLater(() -> callback.accept(task.getValue())));
        task.setOnFailed(event -> {
            event.getSource().getException().printStackTrace();
            Platform.runLater(() -> callback.accept(List.of()));
        });
        
        Thread thread = new Thread(task);
        thread.setDaemon(true);
        thread.start();
    }
    
    /**
     * Carga findings de un run específico.
     */
    public void loadFindingsForRun(Long runId, Consumer<List<FindingSummaryDTO>> callback) {
        Task<List<FindingSummaryDTO>> task = new Task<>() {
            @Override
            protected List<FindingSummaryDTO> call() throws Exception {
                return historyQueryService.getFindings(runId);
            }
        };
        
        task.setOnSucceeded(event -> Platform.runLater(() -> callback.accept(task.getValue())));
        task.setOnFailed(event -> {
            event.getSource().getException().printStackTrace();
            Platform.runLater(() -> callback.accept(List.of()));
        });
        
        Thread thread = new Thread(task);
        thread.setDaemon(true);
        thread.start();
    }
    
    /**
     * Filtra findings por severidad.
     */
    public void filterBySeverity(List<FindingSummaryDTO> allFindings, Severity severity, Consumer<List<FindingSummaryDTO>> callback) {
        List<FindingSummaryDTO> filtered;
        
        if (severity == null) {
            filtered = allFindings;
        } else {
            filtered = allFindings.stream()
                .filter(f -> f.getSeverity() == severity)
                .toList();
        }
        
        Platform.runLater(() -> callback.accept(filtered));
    }
    
    /**
     * Export findings to CSV.
     */
    public void exportToCSV(Long analysisRunId, Consumer<Boolean> callback) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Export Analysis to CSV");
        fileChooser.setInitialFileName("analysis_" + analysisRunId + ".csv");
        fileChooser.getExtensionFilters().add(
            new FileChooser.ExtensionFilter("CSV Files", "*.csv")
        );
        
        File selectedFile = fileChooser.showSaveDialog(view.getScene().getWindow());
        
        if (selectedFile == null) {
            callback.accept(false);
            return;
        }
        
        // Execute export synchronously (TxManager uses ThreadLocal)
        try {
            exportService.exportToCSV(analysisRunId, selectedFile.getAbsolutePath());
            Platform.runLater(() -> callback.accept(true));
        } catch (Exception e) {
            e.printStackTrace();
            Platform.runLater(() -> callback.accept(false));
        }
    }
}

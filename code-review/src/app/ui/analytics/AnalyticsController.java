package app.ui.analytics;

import app.application.dto.MetricsDTO;
import app.application.service.AnalyticsService;
import javafx.application.Platform;
import javafx.concurrent.Task;

import java.time.LocalDateTime;
import java.util.function.Consumer;

/**
 * Controlador para vista de analytics (panel TL).
 * Carga métricas, tendencias, stats por usuario.
 */
public class AnalyticsController {
    
    private final AnalyticsService analyticsService;
    private final AnalyticsView view;
    
    public AnalyticsController(AnalyticsService analyticsService) {
        this.analyticsService = analyticsService;
        this.view = new AnalyticsView(this);
    }
    
    public AnalyticsView getView() {
        return view;
    }
    
    /**
     * Carga métricas del período especificado.
     */
    public void loadMetrics(LocalDateTime since, Consumer<MetricsDTO> callback) {
        Task<MetricsDTO> task = new Task<>() {
            @Override
            protected MetricsDTO call() throws Exception {
                LocalDateTime now = LocalDateTime.now();
                return analyticsService.getMetrics(since, now);
            }
        };
        
        task.setOnSucceeded(event -> Platform.runLater(() -> callback.accept(task.getValue())));
        task.setOnFailed(event -> {
            event.getSource().getException().printStackTrace();
            Platform.runLater(() -> callback.accept(null));
        });
        
        Thread thread = new Thread(task);
        thread.setDaemon(true);
        thread.start();
    }
    
    /**
     * Exporta métricas actuales a CSV.
     */
    public void exportMetrics(MetricsDTO metrics, Consumer<Boolean> callback) {
        Task<Boolean> task = new Task<>() {
            @Override
            protected Boolean call() throws Exception {
                // TODO: implementar export con ExportService
                // Por ahora simplemente retornamos true
                return true;
            }
        };
        
        task.setOnSucceeded(event -> Platform.runLater(() -> callback.accept(task.getValue())));
        task.setOnFailed(event -> Platform.runLater(() -> callback.accept(false)));
        
        Thread thread = new Thread(task);
        thread.setDaemon(true);
        thread.start();
    }
}

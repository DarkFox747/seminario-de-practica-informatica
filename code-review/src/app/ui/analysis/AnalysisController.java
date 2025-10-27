package app.ui.analysis;

import app.application.dto.AnalysisRequestDTO;
import app.application.dto.AnalysisResultDTO;
import app.application.dto.FindingSummaryDTO;
import app.application.service.AnalyzeBranchService;
import app.domain.entity.User;
import app.domain.port.DiffEngine;
import app.domain.value.Severity;
import app.ui.common.ConfirmDialog;
import app.ui.common.ErrorDialog;
import javafx.application.Platform;
import javafx.concurrent.Task;

import java.util.List;
import java.util.function.Consumer;

/**
 * Controlador para la vista de análisis (UC-01).
 * Orquesta el flujo: selección repo/ramas → análisis → mostrar resultados.
 */
public class AnalysisController {
    
    private final AnalyzeBranchService analyzeBranchService;
    private final DiffEngine diffEngine;
    private final AnalysisView view;
    private final User currentUser;
    
    public AnalysisController(AnalyzeBranchService analyzeBranchService, DiffEngine diffEngine, User currentUser) {
        this.analyzeBranchService = analyzeBranchService;
        this.diffEngine = diffEngine;
        this.currentUser = currentUser;
        this.view = new AnalysisView(this);
    }
    
    public AnalysisView getView() {
        return view;
    }
    
    /**
     * Inicia análisis en background thread.
     */
    public void performAnalysis(String repoPath, String baseBranch, String targetBranch) {
        // Validaciones
        if (repoPath == null || repoPath.trim().isEmpty()) {
            ErrorDialog.show("Validation Error", "Repository path is required");
            return;
        }
        if (baseBranch == null || baseBranch.trim().isEmpty()) {
            ErrorDialog.show("Validation Error", "Base branch is required");
            return;
        }
        if (targetBranch == null || targetBranch.trim().isEmpty()) {
            ErrorDialog.show("Validation Error", "Target branch is required");
            return;
        }
        if (baseBranch.equals(targetBranch)) {
            ErrorDialog.show("Validation Error", "Base and target branches must be different");
            return;
        }
        
        // Confirmación
        boolean confirmed = ConfirmDialog.show(
            "Confirm Analysis",
            String.format("Analyze diff between '%s' and '%s' in repository:\n%s", 
                baseBranch, targetBranch, repoPath)
        );
        
        if (!confirmed) {
            return;
        }
        
        // Crear DTO request con path temporal (será convertido a ID en el servicio)
        AnalysisRequestDTO request = new AnalysisRequestDTO(
            currentUser.getId(),
            1L, // Temporary repository ID - will be resolved by service
            baseBranch,
            targetBranch
        );
        
        // Set repository path for service to resolve
        request.setRepositoryPath(repoPath);
        
        // Ejecutar en background
        Task<AnalysisResultDTO> task = new Task<>() {
            @Override
            protected AnalysisResultDTO call() throws Exception {
                updateMessage("Calculating diff...");
                updateProgress(0, 100);
                
                AnalysisResultDTO result = analyzeBranchService.analyze(request);
                
                updateProgress(100, 100);
                return result;
            }
        };
        
        task.setOnSucceeded(event -> {
            AnalysisResultDTO result = task.getValue();
            view.hideLoading();
            view.displayResults(result);
            
            ConfirmDialog.showInfo(
                "Analysis Complete",
                String.format("Found %d issues across %d files", 
                    result.getTotalFindings(), 
                    result.getDiffFiles().size())
            );
        });
        
        task.setOnFailed(event -> {
            view.hideLoading();
            Throwable ex = task.getException();
            ErrorDialog.show("Analysis Failed", "Failed to complete analysis", ex);
        });
        
        // Bind loading indicator
        view.showLoading("Analyzing repository...");
        
        Thread thread = new Thread(task);
        thread.setDaemon(true);
        thread.start();
    }
    
    /**
     * Carga lista de branches disponibles desde el repo usando GitDiffEngine.
     */
    public void loadBranches(String repoPath, Consumer<List<String>> callback) {
        if (repoPath == null || repoPath.trim().isEmpty()) {
            Platform.runLater(() -> callback.accept(List.of()));
            return;
        }
        
        Task<List<String>> task = new Task<>() {
            @Override
            protected List<String> call() throws Exception {
                // Validar que sea un repo git válido
                if (!diffEngine.isValidRepository(repoPath)) {
                    throw new IllegalArgumentException("Invalid Git repository: " + repoPath);
                }
                
                // Obtener branches reales del repositorio
                return diffEngine.getBranches(repoPath);
            }
        };
        
        task.setOnSucceeded(event -> callback.accept(task.getValue()));
        task.setOnFailed(event -> {
            ErrorDialog.show("Load Branches Failed", 
                "Could not load branches from repository.\nMake sure the path points to a valid Git repository.", 
                task.getException());
            callback.accept(List.of());
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
}

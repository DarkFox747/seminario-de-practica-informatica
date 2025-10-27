package app.ui.policy;

import app.application.service.PolicyAdminService;
import app.domain.entity.SeverityPolicy;
import app.ui.common.ConfirmDialog;
import app.ui.common.ErrorDialog;
import javafx.application.Platform;
import javafx.concurrent.Task;

import java.util.List;
import java.util.function.Consumer;

/**
 * Controlador para vista de gestión de políticas.
 * CRUD sobre SeverityPolicy con versionado.
 */
public class PolicyController {
    
    private final PolicyAdminService policyAdminService;
    private final PolicyView view;
    
    public PolicyController(PolicyAdminService policyAdminService) {
        this.policyAdminService = policyAdminService;
        this.view = new PolicyView(this);
    }
    
    public PolicyView getView() {
        return view;
    }
    
    /**
     * Carga lista de políticas.
     */
    public void loadPolicies(Consumer<List<SeverityPolicy>> callback) {
        Task<List<SeverityPolicy>> task = new Task<>() {
            @Override
            protected List<SeverityPolicy> call() throws Exception {
                return policyAdminService.getAllPolicies();
            }
        };
        
        task.setOnSucceeded(event -> Platform.runLater(() -> callback.accept(task.getValue())));
        task.setOnFailed(event -> {
            ErrorDialog.show("Load Failed", "Could not load policies", task.getException());
            Platform.runLater(() -> callback.accept(List.of()));
        });
        
        Thread thread = new Thread(task);
        thread.setDaemon(true);
        thread.start();
    }
    
    /**
     * Crea nueva política.
     */
    public void createPolicy(String name, String rulesJson, Consumer<Boolean> callback) {
        if (name == null || name.trim().isEmpty()) {
            ErrorDialog.show("Validation Error", "Policy name is required");
            callback.accept(false);
            return;
        }
        
        Task<SeverityPolicy> task = new Task<>() {
            @Override
            protected SeverityPolicy call() throws Exception {
                // Create policy with defaults
                String description = "Policy: " + name;
                Long createdBy = 1L; // Default admin user
                return policyAdminService.createPolicy(name, description, rulesJson, createdBy);
            }
        };
        
        task.setOnSucceeded(event -> {
            ConfirmDialog.showInfo("Success", "Policy created successfully");
            Platform.runLater(() -> callback.accept(true));
        });
        
        task.setOnFailed(event -> {
            ErrorDialog.show("Create Failed", "Could not create policy", task.getException());
            Platform.runLater(() -> callback.accept(false));
        });
        
        Thread thread = new Thread(task);
        thread.setDaemon(true);
        thread.start();
    }
    
    /**
     * Actualiza política existente (crea nueva versión).
     */
    public void updatePolicy(Long policyId, String rulesJson, Consumer<Boolean> callback) {
        Task<SeverityPolicy> task = new Task<>() {
            @Override
            protected SeverityPolicy call() throws Exception {
                return policyAdminService.updatePolicy(policyId, rulesJson);
            }
        };
        
        task.setOnSucceeded(event -> {
            ConfirmDialog.showInfo("Success", "Policy updated (new version created)");
            Platform.runLater(() -> callback.accept(true));
        });
        
        task.setOnFailed(event -> {
            ErrorDialog.show("Update Failed", "Could not update policy", task.getException());
            Platform.runLater(() -> callback.accept(false));
        });
        
        Thread thread = new Thread(task);
        thread.setDaemon(true);
        thread.start();
    }
    
    /**
     * Activa una política (desactiva las demás del mismo nombre).
     */
    public void activatePolicy(Long policyId, Consumer<Boolean> callback) {
        boolean confirmed = ConfirmDialog.show(
            "Activate Policy",
            "Are you sure you want to activate this policy?\nOther versions will be deactivated."
        );
        
        if (!confirmed) {
            callback.accept(false);
            return;
        }
        
        Task<Void> task = new Task<>() {
            @Override
            protected Void call() throws Exception {
                policyAdminService.activatePolicy(policyId);
                return null;
            }
        };
        
        task.setOnSucceeded(event -> {
            ConfirmDialog.showInfo("Success", "Policy activated");
            Platform.runLater(() -> callback.accept(true));
        });
        
        task.setOnFailed(event -> {
            ErrorDialog.show("Activation Failed", "Could not activate policy", task.getException());
            Platform.runLater(() -> callback.accept(false));
        });
        
        Thread thread = new Thread(task);
        thread.setDaemon(true);
        thread.start();
    }
}

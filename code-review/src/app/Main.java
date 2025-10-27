package app;

import app.config.AppConfig;
import app.config.AppFactory;
import app.domain.entity.User;
import app.ui.MainWindow;
import app.ui.analysis.AnalysisController;
import app.ui.analytics.AnalyticsController;
import app.ui.history.HistoryController;
import app.ui.policy.PolicyController;
import javafx.application.Application;
import javafx.stage.Stage;

/**
 * Main entry point for the Code Review application.
 */
public class Main extends Application {
    
    private AppFactory factory;
    
    @Override
    public void init() throws Exception {
        super.init();
        
        System.out.println("Initializing Code Review Assistant...");
        
        // Load configuration
        AppConfig.getInstance();
        
        // Initialize factory
        factory = new AppFactory();
        
        System.out.println("Configuration loaded successfully");
    }
    
    @Override
    public void start(Stage primaryStage) throws Exception {
        System.out.println("Starting application UI...");
        
        // Get demo user (or create if doesn't exist)
        User currentUser = factory.getDemoUser();
        System.out.println("Current user: " + currentUser.getUsername());
        
        // Create controllers with services
        AnalysisController analysisController = new AnalysisController(
            factory.getAnalyzeBranchService(),
            factory.getDiffEngine(),
            currentUser
        );
        
        HistoryController historyController = new HistoryController(
            factory.getHistoryQueryService()
        );
        
        AnalyticsController analyticsController = new AnalyticsController(
            factory.getAnalyticsService()
        );
        
        PolicyController policyController = new PolicyController(
            factory.getPolicyAdminService()
        );
        
        // Create and show main window
        MainWindow mainWindow = new MainWindow(
            primaryStage,
            analysisController,
            historyController,
            analyticsController,
            policyController
        );
        
        mainWindow.show();
        
        System.out.println("Application started successfully");
    }
    
    @Override
    public void stop() throws Exception {
        super.stop();
        
        System.out.println("Application shutting down...");
        
        // Cleanup resources
        if (factory != null && factory.getTxManager() != null) {
            try {
                // Any cleanup needed
                System.out.println("Resources cleaned up");
            } catch (Exception e) {
                System.err.println("Error during cleanup: " + e.getMessage());
            }
        }
    }
    
    public static void main(String[] args) {
        launch(args);
    }
}


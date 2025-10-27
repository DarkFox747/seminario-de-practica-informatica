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
 * Main entry point for Code Review Assistant application.
 */
public class App extends Application {
    
    private AppFactory factory;
    
    @Override
    public void init() throws Exception {
        super.init();
        
        // Load configuration
        AppConfig.getInstance();
        
        // Initialize factory
        factory = new AppFactory();
        
        System.out.println("Code Review Assistant initialized");
    }
    
    @Override
    public void start(Stage primaryStage) throws Exception {
        // Get demo user (or create if doesn't exist)
        User currentUser = factory.getDemoUser();
        
        // Create controllers with services
        AnalysisController analysisController = new AnalysisController(
            factory.getAnalyzeBranchService(),
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
    }
    
    @Override
    public void stop() throws Exception {
        super.stop();
        
        // Cleanup resources
        if (factory != null && factory.getTxManager() != null) {
            // Close any open connections
            System.out.println("Cleaning up resources...");
        }
    }
    
    public static void main(String[] args) {
        launch(args);
    }
}

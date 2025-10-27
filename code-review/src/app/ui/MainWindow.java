package app.ui;

import app.ui.analysis.AnalysisController;
import app.ui.analytics.AnalyticsController;
import app.ui.history.HistoryController;
import app.ui.policy.PolicyController;
import javafx.scene.Scene;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.stage.Stage;

/**
 * Ventana principal de la aplicación.
 * TabPane con 4 vistas: Analysis, History, Analytics, Policy.
 */
public class MainWindow {
    
    private final Stage primaryStage;
    
    private final AnalysisController analysisController;
    private final HistoryController historyController;
    private final AnalyticsController analyticsController;
    private final PolicyController policyController;
    
    public MainWindow(
        Stage primaryStage,
        AnalysisController analysisController,
        HistoryController historyController,
        AnalyticsController analyticsController,
        PolicyController policyController
    ) {
        this.primaryStage = primaryStage;
        this.analysisController = analysisController;
        this.historyController = historyController;
        this.analyticsController = analyticsController;
        this.policyController = policyController;
    }
    
    public void show() {
        TabPane tabPane = new TabPane();
        tabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);
        
        // Tab 1: Analysis (UC-01)
        Tab analysisTab = new Tab("Analysis");
        analysisTab.setContent(analysisController.getView());
        
        // Tab 2: History
        Tab historyTab = new Tab("History");
        historyTab.setContent(historyController.getView());
        
        // Tab 3: Analytics (TL Dashboard)
        Tab analyticsTab = new Tab("Analytics");
        analyticsTab.setContent(analyticsController.getView());
        
        // Tab 4: Policies
        Tab policyTab = new Tab("Policies");
        policyTab.setContent(policyController.getView());
        
        tabPane.getTabs().addAll(analysisTab, historyTab, analyticsTab, policyTab);
        
        Scene scene = new Scene(tabPane, 1200, 800);
        primaryStage.setScene(scene);
        primaryStage.setTitle("Code Review Assistant");
        primaryStage.show();
    }
}

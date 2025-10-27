package app.config;

import app.application.service.*;
import app.domain.entity.User;
import app.domain.port.*;
import app.domain.value.UserRole;
import app.infra.integration.EndpointMockClient;
import app.infra.integration.GitDiffEngine;
import app.infra.integration.PolicyEngineImpl;
import app.infra.persistence.*;
import app.infra.tx.JdbcTxManager;

/**
 * Factory para crear e inyectar dependencias.
 * Wiring manual de todos los componentes.
 */
public class AppFactory {
    
    private final JdbcTxManager txManager;
    
    // Repositories
    private final UserRepository userRepository;
    private final RepositoryRefRepository repositoryRefRepository;
    private final AnalysisRunRepository analysisRunRepository;
    private final DiffFileRepository diffFileRepository;
    private final FindingRepository findingRepository;
    private final SeverityPolicyRepository severityPolicyRepository;
    
    // Engines
    private final DiffEngine diffEngine;
    private final EndpointClient endpointClient;
    private final PolicyEngine policyEngine;
    
    // Services
    private final AnalyzeBranchService analyzeBranchService;
    private final HistoryQueryService historyQueryService;
    private final AnalyticsService analyticsService;
    private final PolicyAdminService policyAdminService;
    private final ExportService exportService;
    
    public AppFactory() {
        // Load config
        AppConfig.getInstance();
        
        this.txManager = JdbcTxManager.getInstance();
        
        // Initialize repositories
        this.userRepository = new JdbcUserRepository(txManager);
        this.repositoryRefRepository = new JdbcRepositoryRefRepository(txManager);
        this.analysisRunRepository = new JdbcAnalysisRunRepository(txManager);
        this.diffFileRepository = new JdbcDiffFileRepository(txManager);
        this.findingRepository = new JdbcFindingRepository(txManager);
        this.severityPolicyRepository = new JdbcSeverityPolicyRepository(txManager);
        
        // Initialize engines
        this.diffEngine = new GitDiffEngine();
        this.endpointClient = new EndpointMockClient();
        this.policyEngine = new PolicyEngineImpl();
        
        // Initialize services
        this.analyzeBranchService = new AnalyzeBranchService(
            diffEngine,
            endpointClient,
            policyEngine,
            analysisRunRepository,
            diffFileRepository,
            findingRepository,
            severityPolicyRepository,
            repositoryRefRepository,
            txManager
        );
        
        this.historyQueryService = new HistoryQueryService(
            analysisRunRepository,
            findingRepository,
            diffFileRepository,
            txManager
        );
        
        this.analyticsService = new AnalyticsService(
            analysisRunRepository,
            txManager
        );
        
        this.policyAdminService = new PolicyAdminService(
            severityPolicyRepository,
            policyEngine,
            txManager
        );
        
        this.exportService = new ExportService(historyQueryService);
    }
    
    public JdbcTxManager getTxManager() {
        return txManager;
    }
    
    public AnalyzeBranchService getAnalyzeBranchService() {
        return analyzeBranchService;
    }
    
    public HistoryQueryService getHistoryQueryService() {
        return historyQueryService;
    }
    
    public AnalyticsService getAnalyticsService() {
        return analyticsService;
    }
    
    public PolicyAdminService getPolicyAdminService() {
        return policyAdminService;
    }
    
    public ExportService getExportService() {
        return exportService;
    }
    
    public UserRepository getUserRepository() {
        return userRepository;
    }
    
    /**
     * Crea o obtiene usuario demo para testing.
     */
    public User getDemoUser() {
        try {
            txManager.begin();
            
            // Intentar obtener usuario existente
            var optUser = userRepository.findByUsername("demo");
            if (optUser.isPresent()) {
                txManager.commit();
                return optUser.get();
            }
            
            // Crear usuario demo
            User user = new User(null, "demo", "demo@example.com", UserRole.DEV);
            
            user = userRepository.save(user);
            txManager.commit();
            return user;
            
        } catch (Exception e) {
            try {
                txManager.rollback();
            } catch (TxException rollbackEx) {
                // Ignorar errores de rollback
            }
            // Retornar usuario temporal sin persistir
            return new User(1L, "demo", "demo@example.com", UserRole.DEV);
        }
    }
}

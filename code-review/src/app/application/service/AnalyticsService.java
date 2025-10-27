package app.application.service;

import app.application.dto.MetricsDTO;
import app.domain.entity.AnalysisRun;
import app.domain.port.AnalysisRunRepository;
import app.domain.port.TxManager;
import app.domain.value.RunStatus;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Service for generating analytics and metrics (TL dashboard).
 */
public class AnalyticsService {
    
    private final AnalysisRunRepository analysisRunRepo;
    private final TxManager txManager;
    
    public AnalyticsService(AnalysisRunRepository analysisRunRepo, TxManager txManager) {
        this.analysisRunRepo = analysisRunRepo;
        this.txManager = txManager;
    }
    
    /**
     * Get metrics for a date range.
     */
    public MetricsDTO getMetrics(LocalDateTime from, LocalDateTime to) throws Exception {
        try {
            txManager.begin();
            List<AnalysisRun> runs = analysisRunRepo.findByDateRange(from, to);
            txManager.commit();
            
            return calculateMetrics(runs);
        } catch (Exception e) {
            try {
                txManager.rollback();
            } catch (Exception rollbackEx) {
                // Ignore rollback errors
            }
            throw e;
        }
    }
    
    /**
     * Get metrics for recent runs.
     */
    public MetricsDTO getRecentMetrics(int limit) throws Exception {
        try {
            txManager.begin();
            List<AnalysisRun> runs = analysisRunRepo.findRecent(limit);
            txManager.commit();
            
            return calculateMetrics(runs);
        } catch (Exception e) {
            try {
                txManager.rollback();
            } catch (Exception rollbackEx) {
                // Ignore rollback errors
            }
            throw e;
        }
    }
    
    /**
     * Get metrics for a specific user.
     */
    public MetricsDTO getUserMetrics(Long userId) throws Exception {
        try {
            txManager.begin();
            List<AnalysisRun> runs = analysisRunRepo.findByUserId(userId);
            txManager.commit();
            
            return calculateMetrics(runs);
        } catch (Exception e) {
            try {
                txManager.rollback();
            } catch (Exception rollbackEx) {
                // Ignore rollback errors
            }
            throw e;
        }
    }
    
    /**
     * Get metrics for a specific repository.
     */
    public MetricsDTO getRepositoryMetrics(Long repositoryId) throws Exception {
        try {
            txManager.begin();
            List<AnalysisRun> runs = analysisRunRepo.findByRepositoryId(repositoryId);
            txManager.commit();
            
            return calculateMetrics(runs);
        } catch (Exception e) {
            try {
                txManager.rollback();
            } catch (Exception rollbackEx) {
                // Ignore rollback errors
            }
            throw e;
        }
    }
    
    private MetricsDTO calculateMetrics(List<AnalysisRun> runs) {
        MetricsDTO metrics = new MetricsDTO();
        
        if (runs.isEmpty()) {
            return metrics;
        }
        
        int totalRuns = runs.size();
        int completedRuns = 0;
        int failedRuns = 0;
        int totalFindings = 0;
        int criticalFindings = 0;
        int highFindings = 0;
        int mediumFindings = 0;
        int lowFindings = 0;
        int infoFindings = 0;
        long totalDuration = 0;
        int completedWithDuration = 0;
        
        for (AnalysisRun run : runs) {
            // Count by status
            if (run.getStatus() == RunStatus.COMPLETED) {
                completedRuns++;
            } else if (run.getStatus() == RunStatus.FAILED) {
                failedRuns++;
            }
            
            // Sum findings
            if (run.getTotalFindings() != null) {
                totalFindings += run.getTotalFindings();
            }
            
            if (run.getCriticalCount() != null) {
                criticalFindings += run.getCriticalCount();
            }
            if (run.getHighCount() != null) {
                highFindings += run.getHighCount();
            }
            if (run.getMediumCount() != null) {
                mediumFindings += run.getMediumCount();
            }
            if (run.getLowCount() != null) {
                lowFindings += run.getLowCount();
            }
            if (run.getInfoCount() != null) {
                infoFindings += run.getInfoCount();
            }
            
            // Sum duration
            if (run.getDurationMs() != null) {
                totalDuration += run.getDurationMs();
                completedWithDuration++;
            }
        }
        
        // Set metrics
        metrics.setTotalAnalysisRuns(totalRuns);
        metrics.setCompletedRuns(completedRuns);
        metrics.setFailedRuns(failedRuns);
        metrics.setTotalFindings(totalFindings);
        metrics.setCriticalFindings(criticalFindings);
        metrics.setHighFindings(highFindings);
        metrics.setMediumFindings(mediumFindings);
        metrics.setLowFindings(lowFindings);
        metrics.setInfoFindings(infoFindings);
        
        // Calculate averages
        if (completedWithDuration > 0) {
            metrics.setAverageDurationMs((double) totalDuration / completedWithDuration);
        }
        
        if (completedRuns > 0) {
            metrics.setAverageFindingsPerRun((double) totalFindings / completedRuns);
        }
        
        return metrics;
    }
}

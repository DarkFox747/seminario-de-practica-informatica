package app.application.dto;

import app.domain.value.Severity;
import java.util.HashMap;
import java.util.Map;

/**
 * DTO for dashboard metrics (TL view).
 */
public class MetricsDTO {
    private int totalAnalysisRuns;
    private int completedRuns;
    private int failedRuns;
    private int totalFindings;
    private int totalFiles;
    private int criticalFindings;
    private int highFindings;
    private int mediumFindings;
    private int lowFindings;
    private int infoFindings;
    private double averageDurationMs;
    private double averageFindingsPerRun;
    
    public MetricsDTO() {
    }
    
    public int getTotalAnalysisRuns() {
        return totalAnalysisRuns;
    }
    
    public void setTotalAnalysisRuns(int totalAnalysisRuns) {
        this.totalAnalysisRuns = totalAnalysisRuns;
    }
    
    public int getCompletedRuns() {
        return completedRuns;
    }
    
    public void setCompletedRuns(int completedRuns) {
        this.completedRuns = completedRuns;
    }
    
    public int getFailedRuns() {
        return failedRuns;
    }
    
    public void setFailedRuns(int failedRuns) {
        this.failedRuns = failedRuns;
    }
    
    public int getTotalFindings() {
        return totalFindings;
    }
    
    public void setTotalFindings(int totalFindings) {
        this.totalFindings = totalFindings;
    }
    
    public int getCriticalFindings() {
        return criticalFindings;
    }
    
    public void setCriticalFindings(int criticalFindings) {
        this.criticalFindings = criticalFindings;
    }
    
    public int getHighFindings() {
        return highFindings;
    }
    
    public void setHighFindings(int highFindings) {
        this.highFindings = highFindings;
    }
    
    public int getMediumFindings() {
        return mediumFindings;
    }
    
    public void setMediumFindings(int mediumFindings) {
        this.mediumFindings = mediumFindings;
    }
    
    public int getLowFindings() {
        return lowFindings;
    }
    
    public void setLowFindings(int lowFindings) {
        this.lowFindings = lowFindings;
    }
    
    public int getInfoFindings() {
        return infoFindings;
    }
    
    public void setInfoFindings(int infoFindings) {
        this.infoFindings = infoFindings;
    }
    
    public double getAverageDurationMs() {
        return averageDurationMs;
    }
    
    public void setAverageDurationMs(double averageDurationMs) {
        this.averageDurationMs = averageDurationMs;
    }
    
    public double getAverageFindingsPerRun() {
        return averageFindingsPerRun;
    }
    
    public void setAverageFindingsPerRun(double averageFindingsPerRun) {
        this.averageFindingsPerRun = averageFindingsPerRun;
    }
    
    public int getTotalRuns() {
        return totalAnalysisRuns;
    }
    
    public int getTotalFiles() {
        return totalFiles;
    }
    
    public void setTotalFiles(int totalFiles) {
        this.totalFiles = totalFiles;
    }
    
    public double getAvgFindingsPerRun() {
        return averageFindingsPerRun;
    }
    
    public Map<String, Integer> getSeverityBreakdown() {
        Map<String, Integer> breakdown = new HashMap<>();
        breakdown.put("CRITICAL", criticalFindings);
        breakdown.put("HIGH", highFindings);
        breakdown.put("MEDIUM", mediumFindings);
        breakdown.put("LOW", lowFindings);
        breakdown.put("INFO", infoFindings);
        return breakdown;
    }
}

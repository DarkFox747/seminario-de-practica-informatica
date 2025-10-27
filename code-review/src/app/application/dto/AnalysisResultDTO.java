package app.application.dto;

import app.domain.entity.DiffFile;
import app.domain.entity.Finding;
import app.domain.value.RunStatus;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * DTO for analysis result summary.
 */
public class AnalysisResultDTO {
    private Long analysisRunId;
    private RunStatus status;
    private LocalDateTime startedAt;
    private LocalDateTime completedAt;
    private Long durationMs;
    private Integer totalFiles;
    private Integer totalFindings;
    private Integer criticalCount;
    private Integer highCount;
    private Integer mediumCount;
    private Integer lowCount;
    private Integer infoCount;
    private String errorMessage;
    private List<DiffFile> diffFiles = new ArrayList<>();
    private List<Finding> findings = new ArrayList<>();
    
    public AnalysisResultDTO() {
    }
    
    public Long getAnalysisRunId() {
        return analysisRunId;
    }
    
    public void setAnalysisRunId(Long analysisRunId) {
        this.analysisRunId = analysisRunId;
    }
    
    public RunStatus getStatus() {
        return status;
    }
    
    public void setStatus(RunStatus status) {
        this.status = status;
    }
    
    public LocalDateTime getStartedAt() {
        return startedAt;
    }
    
    public void setStartedAt(LocalDateTime startedAt) {
        this.startedAt = startedAt;
    }
    
    public LocalDateTime getCompletedAt() {
        return completedAt;
    }
    
    public void setCompletedAt(LocalDateTime completedAt) {
        this.completedAt = completedAt;
    }
    
    public Long getDurationMs() {
        return durationMs;
    }
    
    public void setDurationMs(Long durationMs) {
        this.durationMs = durationMs;
    }
    
    public Integer getTotalFiles() {
        return totalFiles;
    }
    
    public void setTotalFiles(Integer totalFiles) {
        this.totalFiles = totalFiles;
    }
    
    public Integer getTotalFindings() {
        return totalFindings;
    }
    
    public void setTotalFindings(Integer totalFindings) {
        this.totalFindings = totalFindings;
    }
    
    public Integer getCriticalCount() {
        return criticalCount;
    }
    
    public void setCriticalCount(Integer criticalCount) {
        this.criticalCount = criticalCount;
    }
    
    public Integer getHighCount() {
        return highCount;
    }
    
    public void setHighCount(Integer highCount) {
        this.highCount = highCount;
    }
    
    public Integer getMediumCount() {
        return mediumCount;
    }
    
    public void setMediumCount(Integer mediumCount) {
        this.mediumCount = mediumCount;
    }
    
    public Integer getLowCount() {
        return lowCount;
    }
    
    public void setLowCount(Integer lowCount) {
        this.lowCount = lowCount;
    }
    
    public Integer getInfoCount() {
        return infoCount;
    }
    
    public void setInfoCount(Integer infoCount) {
        this.infoCount = infoCount;
    }
    
    public String getErrorMessage() {
        return errorMessage;
    }
    
    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }
    
    public List<DiffFile> getDiffFiles() {
        return diffFiles;
    }
    
    public void setDiffFiles(List<DiffFile> diffFiles) {
        this.diffFiles = diffFiles;
    }
    
    public List<Finding> getFindings() {
        return findings;
    }
    
    public void setFindings(List<Finding> findings) {
        this.findings = findings;
    }
    
    public Long getRunId() {
        return analysisRunId;
    }
    
    public boolean hasErrors() {
        return status == RunStatus.ERROR && errorMessage != null;
    }
    
    public boolean isSuccess() {
        return status == RunStatus.SUCCESS;
    }
}

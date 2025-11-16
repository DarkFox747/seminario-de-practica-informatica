package app.application.service;

import app.application.dto.AnalysisResultDTO;
import app.application.dto.FindingSummaryDTO;
import app.domain.entity.AnalysisRun;
import app.domain.entity.Finding;
import app.domain.port.*;
import app.domain.value.Severity;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Service for querying analysis history and findings.
 */
public class HistoryQueryService {
    
    private final AnalysisRunRepository analysisRunRepo;
    private final FindingRepository findingRepo;
    private final DiffFileRepository diffFileRepo;
    private final TxManager txManager;
    
    public HistoryQueryService(
            AnalysisRunRepository analysisRunRepo,
            FindingRepository findingRepo,
            DiffFileRepository diffFileRepo,
            TxManager txManager) {
        this.analysisRunRepo = analysisRunRepo;
        this.findingRepo = findingRepo;
        this.diffFileRepo = diffFileRepo;
        this.txManager = txManager;
    }
    
    /**
     * Get recent analysis runs.
     */
    public List<AnalysisResultDTO> getRecentRuns(int limit) throws Exception {
        try {
            txManager.begin();
            List<AnalysisRun> runs = analysisRunRepo.findRecent(limit);
            txManager.commit();
            
            return runs.stream()
                .map(this::mapToResultDTO)
                .collect(Collectors.toList());
        } catch (Exception e) {
            try {
                txManager.rollback();
            } catch (Exception rollbackEx) {
                // Ignore
            }
            throw e;
        }
    }
    
    /**
     * Get recent analysis run entities (for UI tables).
     */
    public List<AnalysisRun> getRecentRunEntities(int limit) throws Exception {
        try {
            txManager.begin();
            List<AnalysisRun> runs = analysisRunRepo.findRecent(limit);
            txManager.commit();
            return runs;
        } catch (Exception e) {
            try {
                txManager.rollback();
            } catch (Exception rollbackEx) {
                // Ignore
            }
            throw e;
        }
    }
    
    /**
     * Get analysis runs by user.
     */
    public List<AnalysisResultDTO> getRunsByUser(Long userId) throws Exception {
        try {
            txManager.begin();
            List<AnalysisRun> runs = analysisRunRepo.findByUserId(userId);
            txManager.commit();
            
            return runs.stream()
                .map(this::mapToResultDTO)
                .collect(Collectors.toList());
        } catch (Exception e) {
            try {
                txManager.rollback();
            } catch (Exception rollbackEx) {
                // Ignore
            }
            throw e;
        }
    }
    
    /**
     * Get analysis runs by repository.
     */
    public List<AnalysisResultDTO> getRunsByRepository(Long repositoryId) throws Exception {
        try {
            txManager.begin();
            List<AnalysisRun> runs = analysisRunRepo.findByRepositoryId(repositoryId);
            txManager.commit();
            
            return runs.stream()
                .map(this::mapToResultDTO)
                .collect(Collectors.toList());
        } catch (Exception e) {
            txManager.rollback();
            throw e;
        }
    }
    
    /**
     * Get analysis runs by date range.
     */
    public List<AnalysisResultDTO> getRunsByDateRange(LocalDateTime from, LocalDateTime to) 
            throws Exception {
        try {
            txManager.begin();
            List<AnalysisRun> runs = analysisRunRepo.findByDateRange(from, to);
            txManager.commit();
            
            return runs.stream()
                .map(this::mapToResultDTO)
                .collect(Collectors.toList());
        } catch (Exception e) {
            txManager.rollback();
            throw e;
        }
    }
    
    /**
     * Get all findings for an analysis run.
     */
    public List<FindingSummaryDTO> getFindings(Long analysisRunId) throws Exception {
        try {
            txManager.begin();
            List<Finding> findings = findingRepo.findByAnalysisRunId(analysisRunId);
            txManager.commit();
            
            return findings.stream()
                .map(this::mapToFindingSummary)
                .collect(Collectors.toList());
        } catch (Exception e) {
            txManager.rollback();
            throw e;
        }
    }
    
    /**
     * Get findings filtered by severity.
     */
    public List<FindingSummaryDTO> getFindingsBySeverity(Long analysisRunId, Severity severity) 
            throws Exception {
        try {
            txManager.begin();
            List<Finding> findings = findingRepo.findBySeverity(analysisRunId, severity);
            txManager.commit();
            
            return findings.stream()
                .map(this::mapToFindingSummary)
                .collect(Collectors.toList());
        } catch (Exception e) {
            txManager.rollback();
            throw e;
        }
    }
    
    /**
     * Get single analysis run details.
     */
    public AnalysisResultDTO getRunDetails(Long analysisRunId) throws Exception {
        try {
            txManager.begin();
            AnalysisRun run = analysisRunRepo.findById(analysisRunId)
                .orElseThrow(() -> new Exception("Analysis run not found: " + analysisRunId));
            AnalysisResultDTO result = mapToResultDTO(run);
            txManager.commit();
            return result;
        } catch (Exception e) {
            try {
                txManager.rollback();
            } catch (Exception rollbackEx) {
                // Ignore
            }
            throw e;
        }
    }
    
    /**
     * Get run and findings together (for exports).
     * Uses a single transaction for both operations.
     */
    public AnalysisResultDTO getRunWithFindings(Long analysisRunId, 
                                                java.util.function.Consumer<List<FindingSummaryDTO>> findingsConsumer) 
            throws Exception {
        try {
            txManager.begin();
            
            // Get run
            AnalysisRun run = analysisRunRepo.findById(analysisRunId)
                .orElseThrow(() -> new Exception("Analysis run not found: " + analysisRunId));
            AnalysisResultDTO result = mapToResultDTO(run);
            
            // Get findings
            List<Finding> findings = findingRepo.findByAnalysisRunId(analysisRunId);
            List<FindingSummaryDTO> findingDTOs = findings.stream()
                .map(this::mapToFindingSummary)
                .collect(java.util.stream.Collectors.toList());
            
            txManager.commit();
            
            // Pass findings to consumer
            findingsConsumer.accept(findingDTOs);
            
            return result;
        } catch (Exception e) {
            try {
                txManager.rollback();
            } catch (Exception rollbackEx) {
                // Ignore
            }
            throw e;
        }
    }
    
    private AnalysisResultDTO mapToResultDTO(AnalysisRun run) {
        AnalysisResultDTO dto = new AnalysisResultDTO();
        dto.setAnalysisRunId(run.getId());
        dto.setStatus(run.getStatus());
        dto.setStartedAt(run.getStartedAt());
        dto.setCompletedAt(run.getCompletedAt());
        dto.setDurationMs(run.getDurationMs());
        dto.setTotalFiles(run.getTotalFiles());
        dto.setTotalFindings(run.getTotalFindings());
        dto.setCriticalCount(run.getCriticalCount());
        dto.setHighCount(run.getHighCount());
        dto.setMediumCount(run.getMediumCount());
        dto.setLowCount(run.getLowCount());
        dto.setInfoCount(run.getInfoCount());
        dto.setErrorMessage(run.getErrorMessage());
        return dto;
    }
    
    private FindingSummaryDTO mapToFindingSummary(Finding finding) {
        FindingSummaryDTO dto = new FindingSummaryDTO();
        dto.setFindingId(finding.getId());
        dto.setFilePath(finding.getFilePath() != null ? finding.getFilePath() : "Unknown");
        dto.setLineNumber(finding.getLineNumber());
        dto.setRuleId(finding.getRuleId());
        dto.setCategory(finding.getCategory());
        dto.setMessage(finding.getMessage());
        dto.setSeverity(finding.getSeverityFinal());
        dto.setSuggestion(finding.getSuggestion());
        return dto;
    }
}

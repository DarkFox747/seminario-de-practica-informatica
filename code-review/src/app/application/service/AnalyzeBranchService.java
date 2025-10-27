package app.application.service;

import app.application.dto.AnalysisRequestDTO;
import app.application.dto.AnalysisResultDTO;
import app.domain.entity.*;
import app.domain.port.*;
import app.domain.value.Severity;

import java.io.File;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Service for analyzing code between branches (UC-01).
 * Orchestrates: Diff -> Endpoint -> Policy -> Persistence
 */
public class AnalyzeBranchService {
    
    private final DiffEngine diffEngine;
    private final EndpointClient endpointClient;
    private final PolicyEngine policyEngine;
    private final AnalysisRunRepository analysisRunRepo;
    private final DiffFileRepository diffFileRepo;
    private final FindingRepository findingRepo;
    private final SeverityPolicyRepository policyRepo;
    private final RepositoryRefRepository repositoryRepo;
    private final TxManager txManager;
    
    public AnalyzeBranchService(
            DiffEngine diffEngine,
            EndpointClient endpointClient,
            PolicyEngine policyEngine,
            AnalysisRunRepository analysisRunRepo,
            DiffFileRepository diffFileRepo,
            FindingRepository findingRepo,
            SeverityPolicyRepository policyRepo,
            RepositoryRefRepository repositoryRepo,
            TxManager txManager) {
        this.diffEngine = diffEngine;
        this.endpointClient = endpointClient;
        this.policyEngine = policyEngine;
        this.analysisRunRepo = analysisRunRepo;
        this.diffFileRepo = diffFileRepo;
        this.findingRepo = findingRepo;
        this.policyRepo = policyRepo;
        this.repositoryRepo = repositoryRepo;
        this.txManager = txManager;
    }
    
    /**
     * Execute branch analysis (UC-01 main flow).
     */
    public AnalysisResultDTO analyze(AnalysisRequestDTO request) {
        AnalysisRun run = null;
        
        try {
            // Step 1: Create and persist analysis run
            txManager.begin();
            run = createAnalysisRun(request);
            run.markAsRunning();
            run = analysisRunRepo.save(run);
            txManager.commit();
            
            // Step 2: Get repository path
            RepositoryRef repository = getRepository(request.getRepositoryId());
            String repoPath = repository.getLocalPath();
            
            // Step 3: Calculate diff
            List<DiffFile> diffFiles = diffEngine.calculateDiff(
                repoPath, 
                request.getBaseBranch(), 
                request.getTargetBranch()
            );
            
            if (diffFiles.isEmpty()) {
                return handleEmptyDiff(run);
            }
            
            // Step 4: Load active policy
            SeverityPolicy activePolicy = policyRepo.findActivePolicy()
                .orElse(null);
            
            // Step 5: Process each file
            txManager.begin();
            Map<Severity, Integer> severityCounts = new HashMap<>();
            initializeSeverityCounts(severityCounts);
            
            int totalFindings = 0;
            
            for (DiffFile diffFile : diffFiles) {
                // Persist diff file
                diffFile.setAnalysisRunId(run.getId());
                DiffFile savedFile = diffFileRepo.save(diffFile);
                
                // Analyze file with endpoint
                String fileContent = readFileContent(repoPath, diffFile.getFilePath());
                List<Finding> findings = endpointClient.analyzeFile(
                    diffFile.getFilePath(), 
                    fileContent
                );
                
                // Apply policy and persist findings
                for (Finding finding : findings) {
                    finding.setAnalysisRunId(run.getId());
                    finding.setDiffFileId(savedFile.getId());
                    
                    // Apply policy classification
                    if (activePolicy != null) {
                        policyEngine.applyPolicy(finding, activePolicy);
                    }
                    
                    findingRepo.save(finding);
                    
                    // Update counts
                    Severity severity = finding.getSeverityFinal();
                    severityCounts.put(severity, severityCounts.get(severity) + 1);
                    totalFindings++;
                }
            }
            
            // Step 6: Update run with results
            run.setTotalFiles(diffFiles.size());
            run.setTotalFindings(totalFindings);
            run.setCriticalCount(severityCounts.get(Severity.CRITICAL));
            run.setHighCount(severityCounts.get(Severity.HIGH));
            run.setMediumCount(severityCounts.get(Severity.MEDIUM));
            run.setLowCount(severityCounts.get(Severity.LOW));
            run.setInfoCount(severityCounts.get(Severity.INFO));
            run.markAsCompleted();
            
            analysisRunRepo.save(run);
            txManager.commit();
            
            // Step 7: Update repository last analyzed timestamp
            updateRepositoryTimestamp(repository);
            
            return mapToResultDTO(run);
            
        } catch (Exception e) {
            handleError(run, e);
            return mapToResultDTO(run);
        }
    }
    
    /**
     * Get available branches for a repository.
     */
    public List<String> getBranches(Long repositoryId) throws Exception {
        RepositoryRef repository = getRepository(repositoryId);
        return diffEngine.getBranches(repository.getLocalPath());
    }
    
    private AnalysisRun createAnalysisRun(AnalysisRequestDTO request) {
        return new AnalysisRun(
            request.getUserId(),
            request.getRepositoryId(),
            request.getBaseBranch(),
            request.getTargetBranch()
        );
    }
    
    private RepositoryRef getRepository(Long repositoryId) throws Exception {
        txManager.begin();
        try {
            RepositoryRef repo = repositoryRepo.findById(repositoryId)
                .orElseThrow(() -> new Exception("Repository not found: " + repositoryId));
            txManager.commit();
            return repo;
        } catch (Exception e) {
            txManager.rollback();
            throw e;
        }
    }
    
    private AnalysisResultDTO handleEmptyDiff(AnalysisRun run) {
        try {
            txManager.begin();
            run.markAsFailed("No changes detected between branches");
            run.setTotalFiles(0);
            run.setTotalFindings(0);
            analysisRunRepo.save(run);
            txManager.commit();
        } catch (Exception e) {
            try {
                txManager.rollback();
            } catch (TxException rollbackEx) {
                // Ignore rollback errors
            }
        }
        return mapToResultDTO(run);
    }
    
    private void handleError(AnalysisRun run, Exception e) {
        try {
            try {
                txManager.rollback();
            } catch (TxException rollbackEx) {
                // Ignore
            }
            
            txManager.begin();
            if (run != null && run.getId() != null) {
                run.markAsFailed(e.getMessage());
                analysisRunRepo.save(run);
            }
            txManager.commit();
        } catch (Exception ex) {
            try {
                txManager.rollback();
            } catch (TxException txEx) {
                // Ignore
            }
            System.err.println("Error handling failure: " + ex.getMessage());
        }
    }
    
    private void updateRepositoryTimestamp(RepositoryRef repository) {
        try {
            txManager.begin();
            repository.setLastAnalyzedAt(java.time.LocalDateTime.now());
            repositoryRepo.save(repository);
            txManager.commit();
        } catch (Exception e) {
            try {
                txManager.rollback();
            } catch (TxException txEx) {
                // Ignore
            }
            System.err.println("Failed to update repository timestamp: " + e.getMessage());
        }
    }
    
    private String readFileContent(String repoPath, String filePath) {
        try {
            File file = new File(repoPath, filePath);
            if (!file.exists() || !file.isFile()) {
                return "";
            }
            return Files.readString(file.toPath());
        } catch (Exception e) {
            System.err.println("Failed to read file content: " + filePath);
            return "";
        }
    }
    
    private void initializeSeverityCounts(Map<Severity, Integer> counts) {
        counts.put(Severity.CRITICAL, 0);
        counts.put(Severity.HIGH, 0);
        counts.put(Severity.MEDIUM, 0);
        counts.put(Severity.LOW, 0);
        counts.put(Severity.INFO, 0);
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
}

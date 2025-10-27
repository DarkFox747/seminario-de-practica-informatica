package app.application.service;

import app.application.dto.AnalysisRequestDTO;
import app.application.dto.AnalysisResultDTO;
import app.domain.entity.*;
import app.domain.port.*;
import app.domain.value.Severity;
import app.infra.integration.EndpointMockClient;

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
            System.out.println("[AnalyzeBranchService] Starting analysis for: " + request.getRepositoryPath());
            
            // Step 1: Get or create repository
            System.out.println("[AnalyzeBranchService] Getting/creating repository...");
            RepositoryRef repository = getOrCreateRepository(request);
            System.out.println("[AnalyzeBranchService] Repository ready: " + repository.getName());
            
            // Step 2: Create and persist analysis run (with SUCCESS status, will change to ERROR if fails)
            System.out.println("[AnalyzeBranchService] Creating analysis run...");
            run = createAnalysisRun(request);
            run.setRepositoryId(repository.getId());
            run.setPolicyId(1L); // Use default policy
            run.setEndpointId(1L); // Use default endpoint
            
            txManager.begin();
            run = analysisRunRepo.save(run);
            txManager.commit();
            
            System.out.println("[AnalyzeBranchService] Analysis run created with ID: " + run.getId());
            
            // Step 3: Calculate diff
            String repoPath = repository.getLocalPath();
            List<DiffFile> diffFiles = diffEngine.calculateDiff(
                repoPath, 
                request.getBaseBranch(), 
                request.getTargetBranch()
            );
            
            System.out.println("[AnalyzeBranchService] Diff calculated: " + diffFiles.size() + " files changed");
            
            if (diffFiles.isEmpty()) {
                return handleEmptyDiff(run);
            }
            
            // Step 4: Process each file
            txManager.begin();
            
            // Load active policy (inside transaction)
            SeverityPolicy activePolicy = policyRepo.findActivePolicy()
                .orElse(null);
            
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
                
                System.out.println("[AnalyzeBranchService] File " + diffFile.getFilePath() + " returned " + findings.size() + " findings");
                
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
            
            System.out.println("[AnalyzeBranchService] Total findings: " + totalFindings);
            
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
            
            // Step 8: Rotate to next mock scenario for next analysis
            rotateMockScenario();
            
            return mapToResultDTO(run);
            
        } catch (Exception e) {
            System.err.println("[AnalyzeBranchService] ERROR: " + e.getMessage());
            e.printStackTrace();
            
            if (run != null) {
                handleError(run, e);
                return mapToResultDTO(run);
            } else {
                // Run was never created, return error DTO
                throw new RuntimeException("Analysis failed: " + e.getMessage(), e);
            }
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
    
    /**
     * Get or create repository based on local path from request.
     */
    private RepositoryRef getOrCreateRepository(AnalysisRequestDTO request) throws Exception {
        String repoPath = request.getRepositoryPath();
        if (repoPath == null || repoPath.trim().isEmpty()) {
            throw new Exception("Repository path is required");
        }
        
        txManager.begin();
        try {
            // For now, just create a temporary repository object
            // In production, you'd search by path and reuse existing ones
            String repoName = extractRepoName(repoPath);
            RepositoryRef newRepo = new RepositoryRef(null, repoName, repoPath);
            newRepo = repositoryRepo.save(newRepo);
            txManager.commit();
            
            System.out.println("[AnalyzeBranchService] Created repository: " + repoName + " (ID: " + newRepo.getId() + ")");
            return newRepo;
            
        } catch (Exception e) {
            txManager.rollback();
            throw e;
        }
    }
    
    /**
     * Extract repository name from path.
     */
    private String extractRepoName(String path) {
        String normalized = path.replace("\\", "/");
        String[] parts = normalized.split("/");
        return parts[parts.length - 1];
    }
    
    private AnalysisResultDTO handleEmptyDiff(AnalysisRun run) {
        try {
            txManager.begin();
            run.markAsEmptyDiff();
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
    
    /**
     * Rotate mock scenario if using EndpointMockClient.
     */
    private void rotateMockScenario() {
        if (endpointClient instanceof EndpointMockClient) {
            ((EndpointMockClient) endpointClient).rotateScenario();
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
        
        // Load findings and diff files from database
        try {
            txManager.begin();
            List<Finding> findings = findingRepo.findByAnalysisRunId(run.getId());
            List<DiffFile> diffFiles = diffFileRepo.findByAnalysisRunId(run.getId());
            dto.setFindings(findings);
            dto.setDiffFiles(diffFiles);
            txManager.commit();
        } catch (Exception e) {
            System.err.println("[AnalyzeBranchService] Warning: Could not load findings/files: " + e.getMessage());
            try {
                txManager.rollback();
            } catch (TxException rollbackEx) {
                // Ignore
            }
        }
        
        return dto;
    }
}

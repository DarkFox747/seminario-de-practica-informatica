package app.application.dto;

/**
 * DTO for analysis request.
 */
public class AnalysisRequestDTO {
    private Long userId;
    private Long repositoryId;
    private String baseBranch;
    private String targetBranch;
    private String repositoryPath; // Temporary field for UI, resolved to ID by service
    
    public AnalysisRequestDTO() {
    }
    
    public AnalysisRequestDTO(Long userId, Long repositoryId, String baseBranch, String targetBranch) {
        this.userId = userId;
        this.repositoryId = repositoryId;
        this.baseBranch = baseBranch;
        this.targetBranch = targetBranch;
    }
    
    public Long getUserId() {
        return userId;
    }
    
    public void setUserId(Long userId) {
        this.userId = userId;
    }
    
    public Long getRepositoryId() {
        return repositoryId;
    }
    
    public void setRepositoryId(Long repositoryId) {
        this.repositoryId = repositoryId;
    }
    
    public String getBaseBranch() {
        return baseBranch;
    }
    
    public void setBaseBranch(String baseBranch) {
        this.baseBranch = baseBranch;
    }
    
    public String getTargetBranch() {
        return targetBranch;
    }
    
    public void setTargetBranch(String targetBranch) {
        this.targetBranch = targetBranch;
    }
    
    public String getRepositoryPath() {
        return repositoryPath;
    }
    
    public void setRepositoryPath(String repositoryPath) {
        this.repositoryPath = repositoryPath;
    }
}

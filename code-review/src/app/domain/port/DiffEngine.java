package app.domain.port;

import app.domain.entity.DiffFile;
import java.util.List;

/**
 * Port for Git diff operations.
 * Implementations will execute git diff commands and parse results.
 */
public interface DiffEngine {
    
    /**
     * Calculate diff between two branches.
     * 
     * @param repositoryPath Local path to the git repository
     * @param baseBranch Base branch name
     * @param targetBranch Target branch name
     * @return List of files with changes
     * @throws DiffException if git command fails or repository is invalid
     */
    List<DiffFile> calculateDiff(String repositoryPath, String baseBranch, String targetBranch) 
            throws DiffException;
    
    /**
     * Validate that a repository path is a valid git repository.
     * 
     * @param repositoryPath Path to validate
     * @return true if valid git repository
     */
    boolean isValidRepository(String repositoryPath);
    
    /**
     * Get list of branches in the repository.
     * 
     * @param repositoryPath Local path to the git repository
     * @return List of branch names
     * @throws DiffException if repository is invalid
     */
    List<String> getBranches(String repositoryPath) throws DiffException;
}

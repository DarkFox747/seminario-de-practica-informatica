package app.domain.port;

import app.domain.entity.Finding;
import app.domain.value.Severity;
import java.util.List;
import java.util.Optional;

/**
 * Repository for Finding persistence.
 */
public interface FindingRepository {
    
    Finding save(Finding finding) throws RepositoryException;
    
    Optional<Finding> findById(Long id) throws RepositoryException;
    
    List<Finding> findByAnalysisRunId(Long analysisRunId) throws RepositoryException;
    
    List<Finding> findByDiffFileId(Long diffFileId) throws RepositoryException;
    
    List<Finding> findBySeverity(Long analysisRunId, Severity severity) throws RepositoryException;
    
    void deleteByAnalysisRunId(Long analysisRunId) throws RepositoryException;
}

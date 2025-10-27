package app.domain.port;

import app.domain.entity.AnalysisRun;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repository for AnalysisRun persistence.
 */
public interface AnalysisRunRepository {
    
    AnalysisRun save(AnalysisRun analysisRun) throws RepositoryException;
    
    Optional<AnalysisRun> findById(Long id) throws RepositoryException;
    
    List<AnalysisRun> findByUserId(Long userId) throws RepositoryException;
    
    List<AnalysisRun> findByRepositoryId(Long repositoryId) throws RepositoryException;
    
    List<AnalysisRun> findByDateRange(LocalDateTime from, LocalDateTime to) throws RepositoryException;
    
    List<AnalysisRun> findRecent(int limit) throws RepositoryException;
}

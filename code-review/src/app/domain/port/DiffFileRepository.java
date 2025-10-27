package app.domain.port;

import app.domain.entity.DiffFile;
import java.util.List;
import java.util.Optional;

/**
 * Repository for DiffFile persistence.
 */
public interface DiffFileRepository {
    
    DiffFile save(DiffFile diffFile) throws RepositoryException;
    
    Optional<DiffFile> findById(Long id) throws RepositoryException;
    
    List<DiffFile> findByAnalysisRunId(Long analysisRunId) throws RepositoryException;
    
    void deleteByAnalysisRunId(Long analysisRunId) throws RepositoryException;
}

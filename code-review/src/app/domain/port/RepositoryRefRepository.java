package app.domain.port;

import app.domain.entity.RepositoryRef;
import java.util.List;
import java.util.Optional;

/**
 * Repository for RepositoryRef persistence.
 */
public interface RepositoryRefRepository {
    
    RepositoryRef save(RepositoryRef repository) throws RepositoryException;
    
    Optional<RepositoryRef> findById(Long id) throws RepositoryException;
    
    Optional<RepositoryRef> findByName(String name) throws RepositoryException;
    
    List<RepositoryRef> findAllActive() throws RepositoryException;
    
    void deleteById(Long id) throws RepositoryException;
}

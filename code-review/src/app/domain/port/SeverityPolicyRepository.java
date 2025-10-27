package app.domain.port;

import app.domain.entity.SeverityPolicy;
import java.util.List;
import java.util.Optional;

/**
 * Repository for SeverityPolicy persistence.
 */
public interface SeverityPolicyRepository {
    
    SeverityPolicy save(SeverityPolicy policy) throws RepositoryException;
    
    Optional<SeverityPolicy> findById(Long id) throws RepositoryException;
    
    Optional<SeverityPolicy> findActivePolicy() throws RepositoryException;
    
    List<SeverityPolicy> findAll() throws RepositoryException;
    
    List<SeverityPolicy> findByName(String name) throws RepositoryException;
}

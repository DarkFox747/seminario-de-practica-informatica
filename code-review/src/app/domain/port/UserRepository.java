package app.domain.port;

import app.domain.entity.User;
import java.util.List;
import java.util.Optional;

/**
 * Repository for User persistence.
 */
public interface UserRepository {
    
    /**
     * Save a new user or update existing one.
     */
    User save(User user) throws RepositoryException;
    
    /**
     * Find user by ID.
     */
    Optional<User> findById(Long id) throws RepositoryException;
    
    /**
     * Find user by username.
     */
    Optional<User> findByUsername(String username) throws RepositoryException;
    
    /**
     * Find all active users.
     */
    List<User> findAllActive() throws RepositoryException;
    
    /**
     * Delete user by ID.
     */
    void deleteById(Long id) throws RepositoryException;
}

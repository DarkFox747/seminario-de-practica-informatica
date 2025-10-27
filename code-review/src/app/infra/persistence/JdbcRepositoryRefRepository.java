package app.infra.persistence;

import app.domain.entity.RepositoryRef;
import app.domain.port.RepositoryException;
import app.domain.port.RepositoryRefRepository;
import app.domain.port.TxManager;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * JDBC implementation of RepositoryRefRepository.
 */
public class JdbcRepositoryRefRepository implements RepositoryRefRepository {
    
    private final TxManager txManager;
    
    public JdbcRepositoryRefRepository(TxManager txManager) {
        this.txManager = txManager;
    }
    
    @Override
    public RepositoryRef save(RepositoryRef repository) throws RepositoryException {
        if (repository.getId() == null) {
            return insert(repository);
        } else {
            return update(repository);
        }
    }
    
    private RepositoryRef insert(RepositoryRef repo) throws RepositoryException {
        String sql = "INSERT INTO repositories (local_path, vcs, created_at) VALUES (?, ?, ?)";
        
        try {
            Connection conn = txManager.getConnection();
            PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            
            stmt.setString(1, repo.getLocalPath());
            stmt.setString(2, "git"); // Always git for now
            stmt.setTimestamp(3, Timestamp.valueOf(repo.getCreatedAt()));
            
            stmt.executeUpdate();
            
            ResultSet rs = stmt.getGeneratedKeys();
            if (rs.next()) {
                repo.setId(rs.getLong(1));
            }
            rs.close();
            stmt.close();
            
            return repo;
        } catch (Exception e) {
            throw new RepositoryException("Failed to insert repository", e);
        }
    }
    
    private RepositoryRef update(RepositoryRef repo) throws RepositoryException {
        // Note: Current DB schema only has (id, local_path, vcs, created_at)
        // No updatable fields, so just return the repo unchanged
        return repo;
    }
    
    @Override
    public Optional<RepositoryRef> findById(Long id) throws RepositoryException {
        String sql = "SELECT * FROM repositories WHERE id = ?";
        
        try (Connection conn = txManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setLong(1, id);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRow(rs));
                }
                return Optional.empty();
            }
        } catch (Exception e) {
            throw new RepositoryException("Failed to find repository by id", e);
        }
    }
    
    @Override
    public Optional<RepositoryRef> findByName(String name) throws RepositoryException {
        String sql = "SELECT * FROM repositories WHERE name = ?";
        
        try (Connection conn = txManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, name);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRow(rs));
                }
                return Optional.empty();
            }
        } catch (Exception e) {
            throw new RepositoryException("Failed to find repository by name", e);
        }
    }
    
    @Override
    public List<RepositoryRef> findAllActive() throws RepositoryException {
        String sql = "SELECT * FROM repositories WHERE active = true ORDER BY name";
        List<RepositoryRef> repos = new ArrayList<>();
        
        try (Connection conn = txManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            
            while (rs.next()) {
                repos.add(mapRow(rs));
            }
            return repos;
        } catch (Exception e) {
            throw new RepositoryException("Failed to find active repositories", e);
        }
    }
    
    @Override
    public void deleteById(Long id) throws RepositoryException {
        String sql = "UPDATE repositories SET active = false WHERE id = ?";
        
        try (Connection conn = txManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setLong(1, id);
            stmt.executeUpdate();
        } catch (Exception e) {
            throw new RepositoryException("Failed to delete repository", e);
        }
    }
    
    private RepositoryRef mapRow(ResultSet rs) throws SQLException {
        RepositoryRef repo = new RepositoryRef();
        repo.setId(rs.getLong("id"));
        repo.setName(rs.getString("name"));
        repo.setLocalPath(rs.getString("local_path"));
        repo.setDefaultBranch(rs.getString("default_branch"));
        repo.setDescription(rs.getString("description"));
        repo.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
        
        Timestamp lastAnalyzed = rs.getTimestamp("last_analyzed_at");
        if (lastAnalyzed != null) {
            repo.setLastAnalyzedAt(lastAnalyzed.toLocalDateTime());
        }
        
        repo.setActive(rs.getBoolean("active"));
        return repo;
    }
}

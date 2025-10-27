package app.infra.persistence;

import app.domain.entity.SeverityPolicy;
import app.domain.port.RepositoryException;
import app.domain.port.SeverityPolicyRepository;
import app.domain.port.TxManager;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * JDBC implementation of SeverityPolicyRepository.
 */
public class JdbcSeverityPolicyRepository implements SeverityPolicyRepository {
    
    private final TxManager txManager;
    
    public JdbcSeverityPolicyRepository(TxManager txManager) {
        this.txManager = txManager;
    }
    
    @Override
    public SeverityPolicy save(SeverityPolicy policy) throws RepositoryException {
        if (policy.getId() == null) {
            return insert(policy);
        } else {
            return update(policy);
        }
    }
    
    private SeverityPolicy insert(SeverityPolicy policy) throws RepositoryException {
        String sql = "INSERT INTO severity_policies (name, description, rules_json, version, " +
                     "active, created_at, updated_at, created_by) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        
        try (Connection conn = txManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            
            stmt.setString(1, policy.getName());
            stmt.setString(2, policy.getDescription());
            stmt.setString(3, policy.getRulesJson());
            stmt.setInt(4, policy.getVersion());
            stmt.setBoolean(5, policy.isActive());
            stmt.setTimestamp(6, Timestamp.valueOf(policy.getCreatedAt()));
            stmt.setTimestamp(7, policy.getUpdatedAt() != null ? 
                Timestamp.valueOf(policy.getUpdatedAt()) : null);
            setLong(stmt, 8, policy.getCreatedBy());
            
            stmt.executeUpdate();
            
            try (ResultSet rs = stmt.getGeneratedKeys()) {
                if (rs.next()) {
                    policy.setId(rs.getLong(1));
                }
            }
            
            return policy;
        } catch (Exception e) {
            throw new RepositoryException("Failed to insert severity policy", e);
        }
    }
    
    private SeverityPolicy update(SeverityPolicy policy) throws RepositoryException {
        String sql = "UPDATE severity_policies SET name = ?, description = ?, rules_json = ?, " +
                     "version = ?, active = ?, updated_at = ? WHERE id = ?";
        
        try (Connection conn = txManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, policy.getName());
            stmt.setString(2, policy.getDescription());
            stmt.setString(3, policy.getRulesJson());
            stmt.setInt(4, policy.getVersion());
            stmt.setBoolean(5, policy.isActive());
            stmt.setTimestamp(6, policy.getUpdatedAt() != null ? 
                Timestamp.valueOf(policy.getUpdatedAt()) : null);
            stmt.setLong(7, policy.getId());
            
            stmt.executeUpdate();
            return policy;
        } catch (Exception e) {
            throw new RepositoryException("Failed to update severity policy", e);
        }
    }
    
    @Override
    public Optional<SeverityPolicy> findById(Long id) throws RepositoryException {
        String sql = "SELECT * FROM severity_policies WHERE id = ?";
        
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
            throw new RepositoryException("Failed to find policy by id", e);
        }
    }
    
    @Override
    public Optional<SeverityPolicy> findActivePolicy() throws RepositoryException {
        String sql = "SELECT * FROM severity_policies WHERE active = true ORDER BY created_at DESC LIMIT 1";
        
        try {
            Connection conn = txManager.getConnection();
            PreparedStatement stmt = conn.prepareStatement(sql);
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                SeverityPolicy policy = mapRow(rs);
                rs.close();
                stmt.close();
                return Optional.of(policy);
            }
            rs.close();
            stmt.close();
            return Optional.empty();
        } catch (Exception e) {
            throw new RepositoryException("Failed to find active policy", e);
        }
    }
    
    @Override
    public List<SeverityPolicy> findAll() throws RepositoryException {
        String sql = "SELECT * FROM severity_policies ORDER BY created_at DESC";
        List<SeverityPolicy> policies = new ArrayList<>();
        
        try {
            Connection conn = txManager.getConnection();
            PreparedStatement stmt = conn.prepareStatement(sql);
            ResultSet rs = stmt.executeQuery();
            
            while (rs.next()) {
                policies.add(mapRow(rs));
            }
            rs.close();
            stmt.close();
            return policies;
        } catch (Exception e) {
            throw new RepositoryException("Failed to find all policies", e);
        }
    }
    
    @Override
    public List<SeverityPolicy> findByName(String name) throws RepositoryException {
        String sql = "SELECT * FROM severity_policies WHERE name = ? ORDER BY version DESC";
        List<SeverityPolicy> policies = new ArrayList<>();
        
        try (Connection conn = txManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, name);
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    policies.add(mapRow(rs));
                }
            }
            return policies;
        } catch (Exception e) {
            throw new RepositoryException("Failed to find policies by name", e);
        }
    }
    
    private SeverityPolicy mapRow(ResultSet rs) throws SQLException {
        SeverityPolicy policy = new SeverityPolicy();
        policy.setId(rs.getLong("id"));
        policy.setName(rs.getString("name"));
        policy.setDescription(rs.getString("description"));
        policy.setRulesJson(rs.getString("rules_json"));
        policy.setVersion(rs.getInt("version"));
        policy.setActive(rs.getBoolean("active"));
        policy.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
        
        Timestamp updated = rs.getTimestamp("updated_at");
        if (updated != null) {
            policy.setUpdatedAt(updated.toLocalDateTime());
        }
        
        policy.setCreatedBy(getLong(rs, "created_by"));
        return policy;
    }
    
    private void setLong(PreparedStatement stmt, int index, Long value) throws SQLException {
        if (value != null) {
            stmt.setLong(index, value);
        } else {
            stmt.setNull(index, Types.BIGINT);
        }
    }
    
    private Long getLong(ResultSet rs, String column) throws SQLException {
        long value = rs.getLong(column);
        return rs.wasNull() ? null : value;
    }
}

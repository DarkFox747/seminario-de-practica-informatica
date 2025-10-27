package app.infra.persistence;

import app.domain.entity.User;
import app.domain.port.RepositoryException;
import app.domain.port.TxManager;
import app.domain.port.UserRepository;
import app.domain.value.UserRole;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * JDBC implementation of UserRepository.
 */
public class JdbcUserRepository implements UserRepository {
    
    private final TxManager txManager;
    
    public JdbcUserRepository(TxManager txManager) {
        this.txManager = txManager;
    }
    
    @Override
    public User save(User user) throws RepositoryException {
        if (user.getId() == null) {
            return insert(user);
        } else {
            return update(user);
        }
    }
    
    private User insert(User user) throws RepositoryException {
        String sql = "INSERT INTO users (username, email, role, created_at, last_login_at, active) " +
                     "VALUES (?, ?, ?, ?, ?, ?)";
        
        try (Connection conn = txManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            
            stmt.setString(1, user.getUsername());
            stmt.setString(2, user.getEmail());
            stmt.setString(3, user.getRole().name());
            stmt.setTimestamp(4, Timestamp.valueOf(user.getCreatedAt()));
            stmt.setTimestamp(5, user.getLastLoginAt() != null ? 
                Timestamp.valueOf(user.getLastLoginAt()) : null);
            stmt.setBoolean(6, user.isActive());
            
            stmt.executeUpdate();
            
            try (ResultSet rs = stmt.getGeneratedKeys()) {
                if (rs.next()) {
                    user.setId(rs.getLong(1));
                }
            }
            
            return user;
        } catch (Exception e) {
            throw new RepositoryException("Failed to insert user", e);
        }
    }
    
    private User update(User user) throws RepositoryException {
        String sql = "UPDATE users SET username = ?, email = ?, role = ?, " +
                     "last_login_at = ?, active = ? WHERE id = ?";
        
        try (Connection conn = txManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, user.getUsername());
            stmt.setString(2, user.getEmail());
            stmt.setString(3, user.getRole().name());
            stmt.setTimestamp(4, user.getLastLoginAt() != null ? 
                Timestamp.valueOf(user.getLastLoginAt()) : null);
            stmt.setBoolean(5, user.isActive());
            stmt.setLong(6, user.getId());
            
            stmt.executeUpdate();
            return user;
        } catch (Exception e) {
            throw new RepositoryException("Failed to update user", e);
        }
    }
    
    @Override
    public Optional<User> findById(Long id) throws RepositoryException {
        String sql = "SELECT * FROM users WHERE id = ?";
        
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
            throw new RepositoryException("Failed to find user by id", e);
        }
    }
    
    @Override
    public Optional<User> findByUsername(String username) throws RepositoryException {
        String sql = "SELECT * FROM users WHERE username = ?";
        
        try (Connection conn = txManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, username);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRow(rs));
                }
                return Optional.empty();
            }
        } catch (Exception e) {
            throw new RepositoryException("Failed to find user by username", e);
        }
    }
    
    @Override
    public List<User> findAllActive() throws RepositoryException {
        String sql = "SELECT * FROM users WHERE active = true ORDER BY username";
        List<User> users = new ArrayList<>();
        
        try (Connection conn = txManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            
            while (rs.next()) {
                users.add(mapRow(rs));
            }
            return users;
        } catch (Exception e) {
            throw new RepositoryException("Failed to find active users", e);
        }
    }
    
    @Override
    public void deleteById(Long id) throws RepositoryException {
        String sql = "UPDATE users SET active = false WHERE id = ?";
        
        try (Connection conn = txManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setLong(1, id);
            stmt.executeUpdate();
        } catch (Exception e) {
            throw new RepositoryException("Failed to delete user", e);
        }
    }
    
    private User mapRow(ResultSet rs) throws SQLException {
        User user = new User();
        user.setId(rs.getLong("id"));
        user.setUsername(rs.getString("username"));
        user.setEmail(rs.getString("email"));
        user.setRole(UserRole.valueOf(rs.getString("role")));
        user.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
        
        Timestamp lastLogin = rs.getTimestamp("last_login_at");
        if (lastLogin != null) {
            user.setLastLoginAt(lastLogin.toLocalDateTime());
        }
        
        user.setActive(rs.getBoolean("active"));
        return user;
    }
}

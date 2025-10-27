package app.infra.persistence;

import app.domain.entity.AnalysisRun;
import app.domain.port.AnalysisRunRepository;
import app.domain.port.RepositoryException;
import app.domain.port.TxManager;
import app.domain.value.RunStatus;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * JDBC implementation of AnalysisRunRepository.
 */
public class JdbcAnalysisRunRepository implements AnalysisRunRepository {
    
    private final TxManager txManager;
    
    public JdbcAnalysisRunRepository(TxManager txManager) {
        this.txManager = txManager;
    }
    
    @Override
    public AnalysisRun save(AnalysisRun run) throws RepositoryException {
        if (run.getId() == null) {
            return insert(run);
        } else {
            return update(run);
        }
    }
    
    private AnalysisRun insert(AnalysisRun run) throws RepositoryException {
        String sql = "INSERT INTO analysis_runs (user_id, repository_id, base_branch, target_branch, " +
                     "status, started_at, completed_at, total_files, total_findings, " +
                     "critical_count, high_count, medium_count, low_count, info_count, " +
                     "error_message, duration_ms) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        
        try (Connection conn = txManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            
            stmt.setLong(1, run.getUserId());
            stmt.setLong(2, run.getRepositoryId());
            stmt.setString(3, run.getBaseBranch());
            stmt.setString(4, run.getTargetBranch());
            stmt.setString(5, run.getStatus().name());
            stmt.setTimestamp(6, Timestamp.valueOf(run.getStartedAt()));
            stmt.setTimestamp(7, run.getCompletedAt() != null ? 
                Timestamp.valueOf(run.getCompletedAt()) : null);
            setInteger(stmt, 8, run.getTotalFiles());
            setInteger(stmt, 9, run.getTotalFindings());
            setInteger(stmt, 10, run.getCriticalCount());
            setInteger(stmt, 11, run.getHighCount());
            setInteger(stmt, 12, run.getMediumCount());
            setInteger(stmt, 13, run.getLowCount());
            setInteger(stmt, 14, run.getInfoCount());
            stmt.setString(15, run.getErrorMessage());
            setLong(stmt, 16, run.getDurationMs());
            
            stmt.executeUpdate();
            
            try (ResultSet rs = stmt.getGeneratedKeys()) {
                if (rs.next()) {
                    run.setId(rs.getLong(1));
                }
            }
            
            return run;
        } catch (Exception e) {
            throw new RepositoryException("Failed to insert analysis run", e);
        }
    }
    
    private AnalysisRun update(AnalysisRun run) throws RepositoryException {
        String sql = "UPDATE analysis_runs SET status = ?, completed_at = ?, total_files = ?, " +
                     "total_findings = ?, critical_count = ?, high_count = ?, medium_count = ?, " +
                     "low_count = ?, info_count = ?, error_message = ?, duration_ms = ? WHERE id = ?";
        
        try (Connection conn = txManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, run.getStatus().name());
            stmt.setTimestamp(2, run.getCompletedAt() != null ? 
                Timestamp.valueOf(run.getCompletedAt()) : null);
            setInteger(stmt, 3, run.getTotalFiles());
            setInteger(stmt, 4, run.getTotalFindings());
            setInteger(stmt, 5, run.getCriticalCount());
            setInteger(stmt, 6, run.getHighCount());
            setInteger(stmt, 7, run.getMediumCount());
            setInteger(stmt, 8, run.getLowCount());
            setInteger(stmt, 9, run.getInfoCount());
            stmt.setString(10, run.getErrorMessage());
            setLong(stmt, 11, run.getDurationMs());
            stmt.setLong(12, run.getId());
            
            stmt.executeUpdate();
            return run;
        } catch (Exception e) {
            throw new RepositoryException("Failed to update analysis run", e);
        }
    }
    
    @Override
    public Optional<AnalysisRun> findById(Long id) throws RepositoryException {
        String sql = "SELECT * FROM analysis_runs WHERE id = ?";
        
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
            throw new RepositoryException("Failed to find analysis run by id", e);
        }
    }
    
    @Override
    public List<AnalysisRun> findByUserId(Long userId) throws RepositoryException {
        String sql = "SELECT * FROM analysis_runs WHERE user_id = ? ORDER BY started_at DESC";
        List<AnalysisRun> runs = new ArrayList<>();
        
        try (Connection conn = txManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setLong(1, userId);
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    runs.add(mapRow(rs));
                }
            }
            return runs;
        } catch (Exception e) {
            throw new RepositoryException("Failed to find runs by user", e);
        }
    }
    
    @Override
    public List<AnalysisRun> findByRepositoryId(Long repositoryId) throws RepositoryException {
        String sql = "SELECT * FROM analysis_runs WHERE repository_id = ? ORDER BY started_at DESC";
        List<AnalysisRun> runs = new ArrayList<>();
        
        try (Connection conn = txManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setLong(1, repositoryId);
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    runs.add(mapRow(rs));
                }
            }
            return runs;
        } catch (Exception e) {
            throw new RepositoryException("Failed to find runs by repository", e);
        }
    }
    
    @Override
    public List<AnalysisRun> findByDateRange(LocalDateTime from, LocalDateTime to) 
            throws RepositoryException {
        String sql = "SELECT * FROM analysis_runs WHERE started_at BETWEEN ? AND ? " +
                     "ORDER BY started_at DESC";
        List<AnalysisRun> runs = new ArrayList<>();
        
        try (Connection conn = txManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setTimestamp(1, Timestamp.valueOf(from));
            stmt.setTimestamp(2, Timestamp.valueOf(to));
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    runs.add(mapRow(rs));
                }
            }
            return runs;
        } catch (Exception e) {
            throw new RepositoryException("Failed to find runs by date range", e);
        }
    }
    
    @Override
    public List<AnalysisRun> findRecent(int limit) throws RepositoryException {
        String sql = "SELECT * FROM analysis_runs ORDER BY started_at DESC LIMIT ?";
        List<AnalysisRun> runs = new ArrayList<>();
        
        try (Connection conn = txManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, limit);
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    runs.add(mapRow(rs));
                }
            }
            return runs;
        } catch (Exception e) {
            throw new RepositoryException("Failed to find recent runs", e);
        }
    }
    
    private AnalysisRun mapRow(ResultSet rs) throws SQLException {
        AnalysisRun run = new AnalysisRun();
        run.setId(rs.getLong("id"));
        run.setUserId(rs.getLong("user_id"));
        run.setRepositoryId(rs.getLong("repository_id"));
        run.setBaseBranch(rs.getString("base_branch"));
        run.setTargetBranch(rs.getString("target_branch"));
        run.setStatus(RunStatus.valueOf(rs.getString("status")));
        run.setStartedAt(rs.getTimestamp("started_at").toLocalDateTime());
        
        Timestamp completed = rs.getTimestamp("completed_at");
        if (completed != null) {
            run.setCompletedAt(completed.toLocalDateTime());
        }
        
        run.setTotalFiles(getInteger(rs, "total_files"));
        run.setTotalFindings(getInteger(rs, "total_findings"));
        run.setCriticalCount(getInteger(rs, "critical_count"));
        run.setHighCount(getInteger(rs, "high_count"));
        run.setMediumCount(getInteger(rs, "medium_count"));
        run.setLowCount(getInteger(rs, "low_count"));
        run.setInfoCount(getInteger(rs, "info_count"));
        run.setErrorMessage(rs.getString("error_message"));
        run.setDurationMs(getLong(rs, "duration_ms"));
        
        return run;
    }
    
    private void setInteger(PreparedStatement stmt, int index, Integer value) throws SQLException {
        if (value != null) {
            stmt.setInt(index, value);
        } else {
            stmt.setNull(index, Types.INTEGER);
        }
    }
    
    private void setLong(PreparedStatement stmt, int index, Long value) throws SQLException {
        if (value != null) {
            stmt.setLong(index, value);
        } else {
            stmt.setNull(index, Types.BIGINT);
        }
    }
    
    private Integer getInteger(ResultSet rs, String column) throws SQLException {
        int value = rs.getInt(column);
        return rs.wasNull() ? null : value;
    }
    
    private Long getLong(ResultSet rs, String column) throws SQLException {
        long value = rs.getLong(column);
        return rs.wasNull() ? null : value;
    }
}

package app.infra.persistence;

import app.domain.entity.Finding;
import app.domain.port.FindingRepository;
import app.domain.port.RepositoryException;
import app.domain.port.TxManager;
import app.domain.value.Severity;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * JDBC implementation of FindingRepository.
 */
public class JdbcFindingRepository implements FindingRepository {
    
    private final TxManager txManager;
    
    public JdbcFindingRepository(TxManager txManager) {
        this.txManager = txManager;
    }
    
    @Override
    public Finding save(Finding finding) throws RepositoryException {
        if (finding.getId() == null) {
            return insert(finding);
        } else {
            return update(finding);
        }
    }
    
    private Finding insert(Finding finding) throws RepositoryException {
        String sql = "INSERT INTO findings (analysis_run_id, diff_file_id, rule_id, category, " +
                     "message, severity_raw, severity_final, line_number, code_snippet, suggestion) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        
        try (Connection conn = txManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            
            stmt.setLong(1, finding.getAnalysisRunId());
            setLong(stmt, 2, finding.getDiffFileId());
            stmt.setString(3, finding.getRuleId());
            stmt.setString(4, finding.getCategory());
            stmt.setString(5, finding.getMessage());
            stmt.setString(6, finding.getSeverityRaw().name());
            stmt.setString(7, finding.getSeverityFinal().name());
            setInteger(stmt, 8, finding.getLineNumber());
            stmt.setString(9, finding.getCodeSnippet());
            stmt.setString(10, finding.getSuggestion());
            
            stmt.executeUpdate();
            
            try (ResultSet rs = stmt.getGeneratedKeys()) {
                if (rs.next()) {
                    finding.setId(rs.getLong(1));
                }
            }
            
            return finding;
        } catch (Exception e) {
            throw new RepositoryException("Failed to insert finding", e);
        }
    }
    
    private Finding update(Finding finding) throws RepositoryException {
        String sql = "UPDATE findings SET rule_id = ?, category = ?, message = ?, " +
                     "severity_raw = ?, severity_final = ?, line_number = ?, " +
                     "code_snippet = ?, suggestion = ? WHERE id = ?";
        
        try (Connection conn = txManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, finding.getRuleId());
            stmt.setString(2, finding.getCategory());
            stmt.setString(3, finding.getMessage());
            stmt.setString(4, finding.getSeverityRaw().name());
            stmt.setString(5, finding.getSeverityFinal().name());
            setInteger(stmt, 6, finding.getLineNumber());
            stmt.setString(7, finding.getCodeSnippet());
            stmt.setString(8, finding.getSuggestion());
            stmt.setLong(9, finding.getId());
            
            stmt.executeUpdate();
            return finding;
        } catch (Exception e) {
            throw new RepositoryException("Failed to update finding", e);
        }
    }
    
    @Override
    public Optional<Finding> findById(Long id) throws RepositoryException {
        String sql = "SELECT * FROM findings WHERE id = ?";
        
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
            throw new RepositoryException("Failed to find finding by id", e);
        }
    }
    
    @Override
    public List<Finding> findByAnalysisRunId(Long analysisRunId) throws RepositoryException {
        String sql = "SELECT * FROM findings WHERE analysis_run_id = ? " +
                     "ORDER BY severity_final, file_path, line_number";
        List<Finding> findings = new ArrayList<>();
        
        try (Connection conn = txManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setLong(1, analysisRunId);
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    findings.add(mapRow(rs));
                }
            }
            return findings;
        } catch (Exception e) {
            throw new RepositoryException("Failed to find findings by run", e);
        }
    }
    
    @Override
    public List<Finding> findByDiffFileId(Long diffFileId) throws RepositoryException {
        String sql = "SELECT * FROM findings WHERE diff_file_id = ? ORDER BY line_number";
        List<Finding> findings = new ArrayList<>();
        
        try (Connection conn = txManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setLong(1, diffFileId);
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    findings.add(mapRow(rs));
                }
            }
            return findings;
        } catch (Exception e) {
            throw new RepositoryException("Failed to find findings by file", e);
        }
    }
    
    @Override
    public List<Finding> findBySeverity(Long analysisRunId, Severity severity) 
            throws RepositoryException {
        String sql = "SELECT * FROM findings WHERE analysis_run_id = ? AND severity_final = ? " +
                     "ORDER BY line_number";
        List<Finding> findings = new ArrayList<>();
        
        try (Connection conn = txManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setLong(1, analysisRunId);
            stmt.setString(2, severity.name());
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    findings.add(mapRow(rs));
                }
            }
            return findings;
        } catch (Exception e) {
            throw new RepositoryException("Failed to find findings by severity", e);
        }
    }
    
    @Override
    public void deleteByAnalysisRunId(Long analysisRunId) throws RepositoryException {
        String sql = "DELETE FROM findings WHERE analysis_run_id = ?";
        
        try (Connection conn = txManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setLong(1, analysisRunId);
            stmt.executeUpdate();
        } catch (Exception e) {
            throw new RepositoryException("Failed to delete findings", e);
        }
    }
    
    private Finding mapRow(ResultSet rs) throws SQLException {
        Finding finding = new Finding();
        finding.setId(rs.getLong("id"));
        finding.setAnalysisRunId(rs.getLong("analysis_run_id"));
        finding.setDiffFileId(getLong(rs, "diff_file_id"));
        finding.setRuleId(rs.getString("rule_id"));
        finding.setCategory(rs.getString("category"));
        finding.setMessage(rs.getString("message"));
        finding.setSeverityRaw(Severity.valueOf(rs.getString("severity_raw")));
        finding.setSeverityFinal(Severity.valueOf(rs.getString("severity_final")));
        finding.setLineNumber(getInteger(rs, "line_number"));
        finding.setCodeSnippet(rs.getString("code_snippet"));
        finding.setSuggestion(rs.getString("suggestion"));
        return finding;
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

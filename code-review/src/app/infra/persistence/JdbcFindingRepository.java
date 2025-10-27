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
        String sql = "INSERT INTO findings (run_id, code, title, description, " +
                     "severity_code, file_path, line_start, line_end, category, created_at) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, NOW())";
        
        try {
            Connection conn = txManager.getConnection();
            PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            
            // Get file_path from diff_file using diff_file_id
            String filePath = getFilePathFromDiffFile(finding.getDiffFileId());
            
            // Map severity to DB-compatible value (INFO -> LOW since INFO not in severity_type)
            String severityCode = mapSeverityToDbCode(finding.getSeverityFinal());
            
            stmt.setLong(1, finding.getAnalysisRunId());
            stmt.setString(2, finding.getRuleId()); // code
            stmt.setString(3, finding.getMessage()); // title
            stmt.setString(4, finding.getMessage()); // description (same as title for now)
            stmt.setString(5, severityCode); // severity_code
            stmt.setString(6, filePath); // file_path
            setInteger(stmt, 7, finding.getLineNumber()); // line_start
            setInteger(stmt, 8, finding.getLineNumber()); // line_end
            stmt.setString(9, finding.getCategory()); // category
            
            stmt.executeUpdate();
            
            ResultSet rs = stmt.getGeneratedKeys();
            if (rs.next()) {
                finding.setId(rs.getLong(1));
            }
            rs.close();
            stmt.close();
            
            return finding;
        } catch (Exception e) {
            throw new RepositoryException("Failed to insert finding", e);
        }
    }
    
    private String getFilePathFromDiffFile(Long diffFileId) throws RepositoryException {
        if (diffFileId == null) {
            return "unknown";
        }
        String sql = "SELECT path FROM diff_files WHERE id = ?";
        try {
            Connection conn = txManager.getConnection();
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setLong(1, diffFileId);
            ResultSet rs = stmt.executeQuery();
            String path = rs.next() ? rs.getString("path") : "unknown";
            rs.close();
            stmt.close();
            return path;
        } catch (Exception e) {
            throw new RepositoryException("Failed to get file path from diff_file", e);
        }
    }
    
    private Finding update(Finding finding) throws RepositoryException {
        String sql = "UPDATE findings SET code = ?, category = ?, title = ?, " +
                     "description = ?, severity_code = ?, line_start = ?, " +
                     "line_end = ? WHERE id = ?";
        
        try {
            Connection conn = txManager.getConnection();
            PreparedStatement stmt = conn.prepareStatement(sql);
            
            // Map severity to DB-compatible value
            String severityCode = mapSeverityToDbCode(finding.getSeverityFinal());
            
            stmt.setString(1, finding.getRuleId()); // code
            stmt.setString(2, finding.getCategory());
            stmt.setString(3, finding.getMessage()); // title
            stmt.setString(4, finding.getMessage()); // description
            stmt.setString(5, severityCode); // severity_code
            setInteger(stmt, 6, finding.getLineNumber()); // line_start
            setInteger(stmt, 7, finding.getLineNumber()); // line_end
            stmt.setLong(8, finding.getId());
            
            stmt.executeUpdate();
            stmt.close();
            return finding;
        } catch (Exception e) {
            throw new RepositoryException("Failed to update finding", e);
        }
    }
    
    @Override
    public Optional<Finding> findById(Long id) throws RepositoryException {
        String sql = "SELECT * FROM findings WHERE id = ?";
        
        try {
            Connection conn = txManager.getConnection();
            PreparedStatement stmt = conn.prepareStatement(sql);
            
            stmt.setLong(1, id);
            
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                Finding finding = mapRow(rs);
                rs.close();
                stmt.close();
                return Optional.of(finding);
            }
            rs.close();
            stmt.close();
            return Optional.empty();
        } catch (Exception e) {
            throw new RepositoryException("Failed to find finding by id", e);
        }
    }
    
    @Override
    public List<Finding> findByAnalysisRunId(Long analysisRunId) throws RepositoryException {
        String sql = "SELECT * FROM findings WHERE run_id = ? " +
                     "ORDER BY severity_code, file_path, line_start";
        List<Finding> findings = new ArrayList<>();
        
        try {
            Connection conn = txManager.getConnection();
            PreparedStatement stmt = conn.prepareStatement(sql);
            
            stmt.setLong(1, analysisRunId);
            
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                findings.add(mapRow(rs));
            }
            rs.close();
            stmt.close();
            return findings;
        } catch (Exception e) {
            throw new RepositoryException("Failed to find findings by run", e);
        }
    }
    
    @Override
    public List<Finding> findByDiffFileId(Long diffFileId) throws RepositoryException {
        // NOTE: DB schema doesn't have diff_file_id, only file_path
        // This method needs to query by file_path instead
        throw new UnsupportedOperationException("findByDiffFileId not supported with current schema");
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
        finding.setAnalysisRunId(rs.getLong("run_id"));
        finding.setRuleId(rs.getString("code"));
        finding.setCategory(rs.getString("category"));
        finding.setMessage(rs.getString("title")); // or "description"
        finding.setSeverityRaw(Severity.valueOf(rs.getString("severity_code")));
        finding.setSeverityFinal(Severity.valueOf(rs.getString("severity_code")));
        finding.setLineNumber(getInteger(rs, "line_start"));
        // Note: DB doesn't store diff_file_id, code_snippet, suggestion
        return finding;
    }
    
    /**
     * Maps Severity enum to DB severity_type code.
     * INFO is mapped to LOW since it doesn't exist in severity_type table.
     */
    private String mapSeverityToDbCode(Severity severity) {
        if (severity == Severity.INFO) {
            return "LOW";
        }
        return severity.name();
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

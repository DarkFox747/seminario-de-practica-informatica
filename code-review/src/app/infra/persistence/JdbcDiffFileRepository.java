package app.infra.persistence;

import app.domain.entity.DiffFile;
import app.domain.port.DiffFileRepository;
import app.domain.port.RepositoryException;
import app.domain.port.TxManager;
import app.domain.value.FileChangeType;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * JDBC implementation of DiffFileRepository.
 */
public class JdbcDiffFileRepository implements DiffFileRepository {
    
    private final TxManager txManager;
    
    public JdbcDiffFileRepository(TxManager txManager) {
        this.txManager = txManager;
    }
    
    @Override
    public DiffFile save(DiffFile diffFile) throws RepositoryException {
        if (diffFile.getId() == null) {
            return insert(diffFile);
        } else {
            return update(diffFile);
        }
    }
    
    private DiffFile insert(DiffFile file) throws RepositoryException {
        String sql = "INSERT INTO diff_files (analysis_run_id, file_path, change_type, " +
                     "lines_added, lines_removed, old_path) VALUES (?, ?, ?, ?, ?, ?)";
        
        try (Connection conn = txManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            
            stmt.setLong(1, file.getAnalysisRunId());
            stmt.setString(2, file.getFilePath());
            stmt.setString(3, file.getChangeType().name());
            stmt.setInt(4, file.getLinesAdded());
            stmt.setInt(5, file.getLinesRemoved());
            stmt.setString(6, file.getOldPath());
            
            stmt.executeUpdate();
            
            try (ResultSet rs = stmt.getGeneratedKeys()) {
                if (rs.next()) {
                    file.setId(rs.getLong(1));
                }
            }
            
            return file;
        } catch (Exception e) {
            throw new RepositoryException("Failed to insert diff file", e);
        }
    }
    
    private DiffFile update(DiffFile file) throws RepositoryException {
        String sql = "UPDATE diff_files SET file_path = ?, change_type = ?, " +
                     "lines_added = ?, lines_removed = ?, old_path = ? WHERE id = ?";
        
        try (Connection conn = txManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, file.getFilePath());
            stmt.setString(2, file.getChangeType().name());
            stmt.setInt(3, file.getLinesAdded());
            stmt.setInt(4, file.getLinesRemoved());
            stmt.setString(5, file.getOldPath());
            stmt.setLong(6, file.getId());
            
            stmt.executeUpdate();
            return file;
        } catch (Exception e) {
            throw new RepositoryException("Failed to update diff file", e);
        }
    }
    
    @Override
    public Optional<DiffFile> findById(Long id) throws RepositoryException {
        String sql = "SELECT * FROM diff_files WHERE id = ?";
        
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
            throw new RepositoryException("Failed to find diff file by id", e);
        }
    }
    
    @Override
    public List<DiffFile> findByAnalysisRunId(Long analysisRunId) throws RepositoryException {
        String sql = "SELECT * FROM diff_files WHERE analysis_run_id = ? ORDER BY file_path";
        List<DiffFile> files = new ArrayList<>();
        
        try (Connection conn = txManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setLong(1, analysisRunId);
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    files.add(mapRow(rs));
                }
            }
            return files;
        } catch (Exception e) {
            throw new RepositoryException("Failed to find diff files by run", e);
        }
    }
    
    @Override
    public void deleteByAnalysisRunId(Long analysisRunId) throws RepositoryException {
        String sql = "DELETE FROM diff_files WHERE analysis_run_id = ?";
        
        try (Connection conn = txManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setLong(1, analysisRunId);
            stmt.executeUpdate();
        } catch (Exception e) {
            throw new RepositoryException("Failed to delete diff files", e);
        }
    }
    
    private DiffFile mapRow(ResultSet rs) throws SQLException {
        DiffFile file = new DiffFile();
        file.setId(rs.getLong("id"));
        file.setAnalysisRunId(rs.getLong("analysis_run_id"));
        file.setFilePath(rs.getString("file_path"));
        file.setChangeType(FileChangeType.valueOf(rs.getString("change_type")));
        file.setLinesAdded(rs.getInt("lines_added"));
        file.setLinesRemoved(rs.getInt("lines_removed"));
        file.setOldPath(rs.getString("old_path"));
        return file;
    }
}

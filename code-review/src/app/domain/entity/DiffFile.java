package app.domain.entity;

import app.domain.value.FileChangeType;

/**
 * Represents a file detected in the Git diff.
 */
public class DiffFile {
    private Long id;
    private Long analysisRunId;
    private String filePath;
    private FileChangeType changeType;
    private Integer linesAdded;
    private Integer linesRemoved;
    private String oldPath; // For renamed files

    public DiffFile() {
        this.linesAdded = 0;
        this.linesRemoved = 0;
    }

    public DiffFile(String filePath, FileChangeType changeType) {
        this();
        this.filePath = filePath;
        this.changeType = changeType;
    }

    // Getters and setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getAnalysisRunId() {
        return analysisRunId;
    }

    public void setAnalysisRunId(Long analysisRunId) {
        this.analysisRunId = analysisRunId;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public FileChangeType getChangeType() {
        return changeType;
    }

    public void setChangeType(FileChangeType changeType) {
        this.changeType = changeType;
    }

    public Integer getLinesAdded() {
        return linesAdded;
    }

    public void setLinesAdded(Integer linesAdded) {
        this.linesAdded = linesAdded;
    }

    public Integer getLinesRemoved() {
        return linesRemoved;
    }

    public void setLinesRemoved(Integer linesRemoved) {
        this.linesRemoved = linesRemoved;
    }

    public String getOldPath() {
        return oldPath;
    }

    public void setOldPath(String oldPath) {
        this.oldPath = oldPath;
    }

    @Override
    public String toString() {
        return "DiffFile{" +
                "filePath='" + filePath + '\'' +
                ", changeType=" + changeType +
                ", +lines=" + linesAdded +
                ", -lines=" + linesRemoved +
                '}';
    }
}

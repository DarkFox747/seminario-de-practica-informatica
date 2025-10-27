package app.domain.entity;

import java.time.LocalDateTime;

/**
 * Reference to a Git repository for analysis.
 */
public class RepositoryRef {
    private Long id;
    private String name;
    private String localPath;
    private String defaultBranch;
    private String description;
    private LocalDateTime createdAt;
    private LocalDateTime lastAnalyzedAt;
    private boolean active;

    public RepositoryRef() {
        this.active = true;
        this.createdAt = LocalDateTime.now();
        this.defaultBranch = "main";
    }

    public RepositoryRef(Long id, String name, String localPath) {
        this();
        this.id = id;
        this.name = name;
        this.localPath = localPath;
    }

    // Getters and setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getLocalPath() {
        return localPath;
    }

    public void setLocalPath(String localPath) {
        this.localPath = localPath;
    }

    public String getDefaultBranch() {
        return defaultBranch;
    }

    public void setDefaultBranch(String defaultBranch) {
        this.defaultBranch = defaultBranch;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getLastAnalyzedAt() {
        return lastAnalyzedAt;
    }

    public void setLastAnalyzedAt(LocalDateTime lastAnalyzedAt) {
        this.lastAnalyzedAt = lastAnalyzedAt;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    @Override
    public String toString() {
        return "RepositoryRef{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", localPath='" + localPath + '\'' +
                '}';
    }
}

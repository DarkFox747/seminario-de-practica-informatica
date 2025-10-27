package app.domain.entity;

import java.time.LocalDateTime;

/**
 * Policy for severity classification rules.
 */
public class SeverityPolicy {
    private Long id;
    private String name;
    private String description;
    private String rulesJson; // JSON with classification rules
    private Integer version;
    private boolean active;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Long createdBy;

    public SeverityPolicy() {
        this.version = 1;
        this.active = true;
        this.createdAt = LocalDateTime.now();
    }

    public SeverityPolicy(String name, String rulesJson) {
        this();
        this.name = name;
        this.rulesJson = rulesJson;
    }

    public void incrementVersion() {
        this.version++;
        this.updatedAt = LocalDateTime.now();
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

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getRulesJson() {
        return rulesJson;
    }

    public void setRulesJson(String rulesJson) {
        this.rulesJson = rulesJson;
    }

    public Integer getVersion() {
        return version;
    }

    public void setVersion(Integer version) {
        this.version = version;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public Long getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(Long createdBy) {
        this.createdBy = createdBy;
    }

    @Override
    public String toString() {
        return "SeverityPolicy{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", version=" + version +
                ", active=" + active +
                '}';
    }
}

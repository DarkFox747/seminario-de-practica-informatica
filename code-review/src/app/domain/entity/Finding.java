package app.domain.entity;

import app.domain.value.Severity;

/**
 * Represents a code review finding from the endpoint analysis.
 */
public class Finding {
    private Long id;
    private Long analysisRunId;
    private Long diffFileId;
    private String ruleId;
    private String category;
    private String message;
    private Severity severityRaw; // Original severity from endpoint
    private Severity severityFinal; // After policy classification
    private Integer lineNumber;
    private String codeSnippet;
    private String suggestion;

    public Finding() {
    }

    public Finding(String ruleId, String message, Severity severity) {
        this.ruleId = ruleId;
        this.message = message;
        this.severityRaw = severity;
        this.severityFinal = severity;
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

    public Long getDiffFileId() {
        return diffFileId;
    }

    public void setDiffFileId(Long diffFileId) {
        this.diffFileId = diffFileId;
    }

    public String getRuleId() {
        return ruleId;
    }

    public void setRuleId(String ruleId) {
        this.ruleId = ruleId;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Severity getSeverityRaw() {
        return severityRaw;
    }

    public void setSeverityRaw(Severity severityRaw) {
        this.severityRaw = severityRaw;
    }

    public Severity getSeverityFinal() {
        return severityFinal;
    }

    public void setSeverityFinal(Severity severityFinal) {
        this.severityFinal = severityFinal;
    }

    public Integer getLineNumber() {
        return lineNumber;
    }

    public void setLineNumber(Integer lineNumber) {
        this.lineNumber = lineNumber;
    }

    public String getCodeSnippet() {
        return codeSnippet;
    }

    public void setCodeSnippet(String codeSnippet) {
        this.codeSnippet = codeSnippet;
    }

    public String getSuggestion() {
        return suggestion;
    }

    public void setSuggestion(String suggestion) {
        this.suggestion = suggestion;
    }

    @Override
    public String toString() {
        return "Finding{" +
                "ruleId='" + ruleId + '\'' +
                ", category='" + category + '\'' +
                ", severityFinal=" + severityFinal +
                ", line=" + lineNumber +
                '}';
    }
}

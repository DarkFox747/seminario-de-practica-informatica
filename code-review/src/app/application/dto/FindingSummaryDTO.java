package app.application.dto;

import app.domain.value.Severity;

/**
 * DTO for finding summary in UI.
 */
public class FindingSummaryDTO {
    private Long findingId;
    private String filePath;
    private Integer lineNumber;
    private String ruleId;
    private String category;
    private String message;
    private Severity severity;
    private String suggestion;
    
    public FindingSummaryDTO() {
    }
    
    public Long getFindingId() {
        return findingId;
    }
    
    public void setFindingId(Long findingId) {
        this.findingId = findingId;
    }
    
    public String getFilePath() {
        return filePath;
    }
    
    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }
    
    public Integer getLineNumber() {
        return lineNumber;
    }
    
    public void setLineNumber(Integer lineNumber) {
        this.lineNumber = lineNumber;
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
    
    public Severity getSeverity() {
        return severity;
    }
    
    public void setSeverity(Severity severity) {
        this.severity = severity;
    }
    
    public String getSuggestion() {
        return suggestion;
    }
    
    public void setSuggestion(String suggestion) {
        this.suggestion = suggestion;
    }
}

package app.infra.integration;

import app.domain.entity.Finding;
import app.domain.port.EndpointClient;
import app.domain.port.EndpointException;
import app.domain.value.Severity;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Mock endpoint client that simulates code analysis.
 * Returns randomized findings based on file type and content.
 */
public class EndpointMockClient implements EndpointClient {
    
    private final Random random;
    private final List<MockFinding> mockData;
    
    public EndpointMockClient() {
        this.random = new Random();
        this.mockData = loadMockData();
    }
    
    @Override
    public List<Finding> analyzeFile(String filePath, String fileContent) throws EndpointException {
        List<Finding> findings = new ArrayList<>();
        
        // Randomly select 0-3 findings
        int numFindings = random.nextInt(4);
        
        for (int i = 0; i < numFindings && i < mockData.size(); i++) {
            MockFinding mock = mockData.get(random.nextInt(mockData.size()));
            
            Finding finding = new Finding();
            finding.setRuleId(mock.ruleId);
            finding.setCategory(mock.category);
            finding.setMessage(mock.message);
            finding.setSeverityRaw(mock.severity);
            finding.setSeverityFinal(mock.severity);
            finding.setLineNumber(mock.lineNumber + random.nextInt(50)); // Randomize line
            finding.setCodeSnippet(mock.codeSnippet);
            finding.setSuggestion(mock.suggestion);
            
            findings.add(finding);
        }
        
        return findings;
    }
    
    @Override
    public boolean isAvailable() {
        return true; // Mock is always available
    }
    
    /**
     * Load mock findings from JSON file or use hardcoded data.
     */
    private List<MockFinding> loadMockData() {
        List<MockFinding> findings = new ArrayList<>();
        
        try {
            // Try to load from JSON file
            InputStream is = getClass().getClassLoader()
                .getResourceAsStream("resources/mock-findings.json");
            
            if (is != null) {
                String json = new String(is.readAllBytes(), StandardCharsets.UTF_8);
                findings = parseSimpleJson(json);
                is.close();
            }
        } catch (Exception e) {
            System.err.println("Failed to load mock data from JSON: " + e.getMessage());
        }
        
        // If loading failed or file not found, use hardcoded data
        if (findings.isEmpty()) {
            findings.add(new MockFinding(
                "SEC001", "Security", 
                "Potential SQL injection vulnerability detected",
                Severity.CRITICAL, 42,
                "String sql = \"SELECT * FROM users WHERE id = \" + userId;",
                "Use PreparedStatement instead of string concatenation"
            ));
            
            findings.add(new MockFinding(
                "CODE002", "Code Quality",
                "Method complexity exceeds threshold",
                Severity.HIGH, 78,
                "public void processData() { ... }",
                "Consider breaking down this method into smaller functions"
            ));
            
            findings.add(new MockFinding(
                "PERF003", "Performance",
                "Inefficient loop detected",
                Severity.MEDIUM, 105,
                "for (int i = 0; i < list.size(); i++) { ... }",
                "Cache list.size() or use enhanced for loop"
            ));
            
            findings.add(new MockFinding(
                "STYLE004", "Style",
                "Variable naming does not follow conventions",
                Severity.LOW, 15,
                "String MyVariable = \"test\";",
                "Use camelCase for variable names: myVariable"
            ));
            
            findings.add(new MockFinding(
                "INFO005", "Documentation",
                "Missing Javadoc for public method",
                Severity.INFO, 32,
                "public void calculate() { ... }",
                "Add Javadoc to document method purpose and parameters"
            ));
        }
        
        return findings;
    }
    
    /**
     * Simple JSON parser for mock findings (without external libraries).
     */
    private List<MockFinding> parseSimpleJson(String json) {
        List<MockFinding> findings = new ArrayList<>();
        
        // Very basic JSON parsing - extract objects between { }
        String[] objects = json.split("\\},\\s*\\{");
        
        for (String obj : objects) {
            obj = obj.replace("[", "").replace("]", "")
                     .replace("{", "").replace("}", "").trim();
            
            try {
                String ruleId = extractValue(obj, "ruleId");
                String category = extractValue(obj, "category");
                String message = extractValue(obj, "message");
                String severityStr = extractValue(obj, "severity");
                int lineNumber = Integer.parseInt(extractValue(obj, "lineNumber"));
                String codeSnippet = extractValue(obj, "codeSnippet");
                String suggestion = extractValue(obj, "suggestion");
                
                Severity severity = Severity.valueOf(severityStr);
                
                findings.add(new MockFinding(
                    ruleId, category, message, severity, 
                    lineNumber, codeSnippet, suggestion
                ));
            } catch (Exception e) {
                // Skip malformed entries
                System.err.println("Failed to parse mock finding: " + e.getMessage());
            }
        }
        
        return findings;
    }
    
    private String extractValue(String obj, String key) {
        String pattern = "\"" + key + "\"\\s*:\\s*";
        int start = obj.indexOf(pattern);
        if (start < 0) return "";
        
        start += pattern.length();
        
        if (obj.charAt(start) == '"') {
            // String value
            start++;
            int end = obj.indexOf('"', start);
            if (end < 0) return "";
            return obj.substring(start, end);
        } else {
            // Number value
            int end = obj.indexOf(',', start);
            if (end < 0) end = obj.length();
            return obj.substring(start, end).trim();
        }
    }
    
    /**
     * Internal class to hold mock finding data.
     */
    private static class MockFinding {
        String ruleId;
        String category;
        String message;
        Severity severity;
        int lineNumber;
        String codeSnippet;
        String suggestion;
        
        MockFinding(String ruleId, String category, String message, 
                   Severity severity, int lineNumber, 
                   String codeSnippet, String suggestion) {
            this.ruleId = ruleId;
            this.category = category;
            this.message = message;
            this.severity = severity;
            this.lineNumber = lineNumber;
            this.codeSnippet = codeSnippet;
            this.suggestion = suggestion;
        }
    }
}

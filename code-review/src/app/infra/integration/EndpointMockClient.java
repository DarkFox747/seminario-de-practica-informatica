package app.infra.integration;

import app.domain.entity.Finding;
import app.domain.port.EndpointClient;
import app.domain.port.EndpointException;
import app.domain.value.Severity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Mock endpoint client with 3 different scenarios.
 * Rotates between scenarios to generate varied analysis results.
 */
public class EndpointMockClient implements EndpointClient {
    
    private final Map<Integer, List<MockFinding>> scenarios;
    private int currentScenario = 0;
    
    public EndpointMockClient() {
        this.scenarios = loadAllScenarios();
    }
    
    @Override
    public List<Finding> analyzeFile(String filePath, String fileContent) throws EndpointException {
        // Select scenario (rotate or random)
        List<MockFinding> scenarioData = scenarios.get(currentScenario);
        
        System.out.println("[EndpointMock] Analyzing file: " + filePath + " with scenario " + (currentScenario + 1));
        
        List<Finding> findings = new ArrayList<>();
        
        // Return ALL findings from the selected scenario (not random subset)
        for (MockFinding mock : scenarioData) {
            Finding finding = new Finding();
            finding.setRuleId(mock.ruleId);
            finding.setCategory(mock.category);
            finding.setMessage(mock.message);
            finding.setSeverityRaw(mock.severity);
            finding.setSeverityFinal(mock.severity);
            finding.setLineNumber(mock.lineNumber);
            finding.setCodeSnippet(mock.codeSnippet);
            finding.setSuggestion(mock.suggestion);
            
            findings.add(finding);
        }
        
        System.out.println("[EndpointMock] Returning " + findings.size() + " findings for " + filePath);
        return findings;
    }
    
    /**
     * Rotate to next scenario for the next analysis.
     */
    public void rotateScenario() {
        currentScenario = (currentScenario + 1) % 3;
        System.out.println("[EndpointMock] Rotated to scenario " + (currentScenario + 1));
    }
    
    /**
     * Select a specific scenario (0, 1, or 2).
     */
    public void setScenario(int scenario) {
        if (scenario >= 0 && scenario < 3) {
            currentScenario = scenario;
            System.out.println("[EndpointMock] Set to scenario " + (scenario + 1));
        }
    }
    
    /**
     * Get current scenario number (0-2).
     */
    public int getCurrentScenario() {
        return currentScenario;
    }
    
    @Override
    public boolean isAvailable() {
        return true; // Mock is always available
    }
    
    /**
     * Load all 3 scenarios from JSON files.
     */
    private Map<Integer, List<MockFinding>> loadAllScenarios() {
        Map<Integer, List<MockFinding>> allScenarios = new HashMap<>();
        
        // For now, use hardcoded scenarios (JSON parser too simple)
        // In production, use proper JSON library like Jackson or Gson
        
        allScenarios.put(0, getHardcodedScenario(1));
        allScenarios.put(1, getHardcodedScenario(2));
        allScenarios.put(2, getHardcodedScenario(3));
        
        System.out.println("[EndpointMock] Loaded 3 scenarios with hardcoded data");
        
        return allScenarios;
    }
    
    /**
     * Get hardcoded scenario data (3 different scenarios).
     */
    private List<MockFinding> getHardcodedScenario(int scenarioNum) {
        List<MockFinding> findings = new ArrayList<>();
        
        if (scenarioNum == 1) {
            // Scenario 1: Security-heavy (2 CRITICAL, 1 HIGH, 1 MEDIUM, 1 LOW, 2 INFO)
            findings.add(new MockFinding(
                "SEC001", "Security", 
                "Potential SQL injection vulnerability detected",
                Severity.CRITICAL, 42,
                "String sql = \"SELECT * FROM users WHERE id = \" + userId;",
                "Use PreparedStatement instead of string concatenation"
            ));
            findings.add(new MockFinding(
                "SEC002", "Security",
                "Hardcoded password found",
                Severity.CRITICAL, 18,
                "String password = \"admin123\";",
                "Store sensitive credentials in environment variables or secure vault"
            ));
            findings.add(new MockFinding(
                "CODE002", "Code Quality",
                "Method complexity exceeds threshold",
                Severity.HIGH, 78,
                "public void processData() { ... }",
                "Consider breaking down this method into smaller functions"
            ));
            findings.add(new MockFinding(
                "CODE005", "Code Quality",
                "Duplicated code block detected",
                Severity.MEDIUM, 120,
                "if (user != null) { validate(user); }",
                "Extract common logic into a reusable method"
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
            findings.add(new MockFinding(
                "INFO006", "Documentation",
                "TODO comment found",
                Severity.INFO, 88,
                "// TODO: implement error handling",
                "Address pending TODO items before production release"
            ));
            
        } else if (scenarioNum == 2) {
            // Scenario 2: Performance & Code Quality focused (1 HIGH, 3 MEDIUM, 2 LOW)
            findings.add(new MockFinding(
                "SEC010", "Security",
                "Insecure random number generation",
                Severity.HIGH, 55,
                "Random rand = new Random();",
                "Use SecureRandom for cryptographic operations"
            ));
            findings.add(new MockFinding(
                "PERF003", "Performance",
                "Inefficient loop detected",
                Severity.MEDIUM, 105,
                "for (int i = 0; i < list.size(); i++) { ... }",
                "Cache list.size() or use enhanced for loop"
            ));
            findings.add(new MockFinding(
                "PERF007", "Performance",
                "String concatenation in loop",
                Severity.MEDIUM, 134,
                "result += item;",
                "Use StringBuilder for string concatenation in loops"
            ));
            findings.add(new MockFinding(
                "CODE011", "Code Quality",
                "Empty catch block",
                Severity.MEDIUM, 92,
                "catch (Exception e) { }",
                "Handle exceptions properly or at least log them"
            ));
            findings.add(new MockFinding(
                "CODE008", "Code Quality",
                "Unused import statement",
                Severity.LOW, 5,
                "import java.util.ArrayList;",
                "Remove unused imports to keep code clean"
            ));
            findings.add(new MockFinding(
                "STYLE012", "Style",
                "Magic number detected",
                Severity.LOW, 67,
                "if (count > 100) { ... }",
                "Extract magic numbers to named constants"
            ));
            
        } else {
            // Scenario 3: Critical issues (1 CRITICAL, 3 HIGH, 2 MEDIUM, 1 LOW, 1 INFO)
            findings.add(new MockFinding(
                "SEC015", "Security",
                "Deserialization of untrusted data",
                Severity.CRITICAL, 201,
                "ObjectInputStream ois = new ObjectInputStream(input);",
                "Validate and sanitize input before deserializing"
            ));
            findings.add(new MockFinding(
                "SEC016", "Security",
                "Missing input validation",
                Severity.HIGH, 73,
                "String userInput = request.getParameter(\"data\");",
                "Always validate and sanitize user input"
            ));
            findings.add(new MockFinding(
                "PERF022", "Performance",
                "Potential memory leak - unclosed resource",
                Severity.HIGH, 88,
                "FileInputStream fis = new FileInputStream(file);",
                "Use try-with-resources to ensure proper resource cleanup"
            ));
            findings.add(new MockFinding(
                "CODE025", "Code Quality",
                "Null pointer dereference risk",
                Severity.HIGH, 178,
                "user.getName().toLowerCase();",
                "Add null checks before dereferencing objects"
            ));
            findings.add(new MockFinding(
                "CODE020", "Code Quality",
                "Method has too many parameters",
                Severity.MEDIUM, 45,
                "public void process(String a, int b, boolean c, List d, Map e, Object f) { ... }",
                "Consider using a parameter object or builder pattern"
            ));
            findings.add(new MockFinding(
                "CODE021", "Code Quality",
                "Deep nesting detected",
                Severity.MEDIUM, 112,
                "if (...) { if (...) { if (...) { if (...) { ... } } } }",
                "Reduce nesting depth by extracting methods or using guard clauses"
            ));
            findings.add(new MockFinding(
                "STYLE023", "Style",
                "Inconsistent indentation",
                Severity.LOW, 156,
                "  if (valid) {\n      process();\n  }",
                "Use consistent indentation throughout the file"
            ));
            findings.add(new MockFinding(
                "INFO024", "Documentation",
                "Deprecated API usage",
                Severity.INFO, 34,
                "@Deprecated public void oldMethod() { ... }",
                "Consider migrating to newer API alternatives"
            ));
        }
        
        System.out.println("[EndpointMock] Created scenario " + scenarioNum + " with " + findings.size() + " findings");
        return findings;
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

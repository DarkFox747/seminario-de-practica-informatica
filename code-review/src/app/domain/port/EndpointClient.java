package app.domain.port;

import app.domain.entity.Finding;
import java.util.List;

/**
 * Port for calling the code analysis endpoint (mock or real).
 */
public interface EndpointClient {
    
    /**
     * Analyze a file and return findings.
     * 
     * @param filePath Path of the file to analyze
     * @param fileContent Content of the file
     * @return List of findings detected
     * @throws EndpointException if endpoint call fails
     */
    List<Finding> analyzeFile(String filePath, String fileContent) throws EndpointException;
    
    /**
     * Check if endpoint is available.
     * 
     * @return true if endpoint is reachable
     */
    boolean isAvailable();
}

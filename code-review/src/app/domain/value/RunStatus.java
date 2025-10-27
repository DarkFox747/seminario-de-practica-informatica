package app.domain.value;

/**
 * Status of an analysis run.
 */
public enum RunStatus {
    /**
     * Analysis is pending execution
     */
    PENDING,
    
    /**
     * Analysis is currently running
     */
    RUNNING,
    
    /**
     * Analysis completed successfully
     */
    COMPLETED,
    
    /**
     * Analysis failed due to error
     */
    FAILED,
    
    /**
     * Analysis was cancelled by user
     */
    CANCELLED
}

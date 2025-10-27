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
     * Analysis completed successfully (alias for COMPLETED)
     */
    SUCCESS,
    
    /**
     * Analysis failed due to error
     */
    FAILED,
    
    /**
     * Analysis had an error
     */
    ERROR,
    
    /**
     * Analysis completed with empty diff
     */
    EMPTY_DIFF,
    
    /**
     * Analysis was cancelled by user
     */
    CANCELLED
}

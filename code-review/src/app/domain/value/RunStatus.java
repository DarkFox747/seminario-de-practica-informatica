package app.domain.value;

/**
 * Status of an analysis run.
 * Must match values in run_status_type catalog table.
 */
public enum RunStatus {
    /**
     * Analysis completed successfully
     */
    SUCCESS,
    
    /**
     * Analysis failed due to error
     */
    ERROR,
    
    /**
     * No changes found to analyze
     */
    EMPTY_DIFF
}

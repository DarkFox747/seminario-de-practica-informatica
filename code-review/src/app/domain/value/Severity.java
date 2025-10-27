package app.domain.value;

/**
 * Severity level of a finding.
 */
public enum Severity {
    /**
     * Critical severity - requires immediate attention
     */
    CRITICAL,
    
    /**
     * High severity - should be fixed soon
     */
    HIGH,
    
    /**
     * Medium severity - moderate priority
     */
    MEDIUM,
    
    /**
     * Low severity - minor issue
     */
    LOW,
    
    /**
     * Informational - not an issue, just information
     */
    INFO
}

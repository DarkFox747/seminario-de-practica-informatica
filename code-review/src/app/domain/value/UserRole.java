package app.domain.value;

/**
 * User roles in the code review system.
 */
public enum UserRole {
    /**
     * Developer - can run analysis and view their own reports
     */
    DEV,
    
    /**
     * Tech Lead - can view analytics, dashboards and manage policies
     */
    TL,
    
    /**
     * Admin - full system access
     */
    ADMIN
}

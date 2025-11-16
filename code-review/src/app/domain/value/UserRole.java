package app.domain.value;

/**
 * User roles in the code review system.
 * Must match values in user_role_type catalog table.
 */
public enum UserRole {
    /**
     * Developer - can run analysis and view their own reports
     */
    DEVELOPER,
    
    /**
     * Tech Lead - can view analytics, dashboards and manage policies
     */
    TECH_LEAD,
    
    /**
     * QA - Quality Assurance
     */
    QA,
    
    /**
     * Admin - full system access
     */
    ADMIN
}

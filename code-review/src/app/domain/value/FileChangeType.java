package app.domain.value;

/**
 * Type of file change detected in Git diff.
 */
public enum FileChangeType {
    /**
     * New file added
     */
    ADDED,
    
    /**
     * Existing file modified
     */
    MODIFIED,
    
    /**
     * File deleted
     */
    DELETED,
    
    /**
     * File renamed
     */
    RENAMED,
    
    /**
     * File copied
     */
    COPIED
}

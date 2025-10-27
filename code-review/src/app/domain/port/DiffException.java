package app.domain.port;

/**
 * Exception thrown when diff operations fail.
 */
public class DiffException extends Exception {
    
    public DiffException(String message) {
        super(message);
    }
    
    public DiffException(String message, Throwable cause) {
        super(message, cause);
    }
}

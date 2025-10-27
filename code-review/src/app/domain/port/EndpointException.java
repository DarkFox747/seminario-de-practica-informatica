package app.domain.port;

/**
 * Exception thrown when endpoint operations fail.
 */
public class EndpointException extends Exception {
    
    public EndpointException(String message) {
        super(message);
    }
    
    public EndpointException(String message, Throwable cause) {
        super(message, cause);
    }
}

package app.domain.port;

/**
 * Exception thrown during transaction operations.
 */
public class TxException extends Exception {
    
    public TxException(String message) {
        super(message);
    }
    
    public TxException(String message, Throwable cause) {
        super(message, cause);
    }
}

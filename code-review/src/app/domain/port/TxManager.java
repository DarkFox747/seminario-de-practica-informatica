package app.domain.port;

import java.sql.Connection;

/**
 * Transaction manager for coordinating database transactions.
 */
public interface TxManager {
    
    /**
     * Get current connection for the transaction.
     * If no transaction is active, creates a new one.
     * 
     * @return Active database connection
     * @throws TxException if connection cannot be obtained
     */
    Connection getConnection() throws TxException;
    
    /**
     * Begin a new transaction.
     * 
     * @throws TxException if transaction cannot be started
     */
    void begin() throws TxException;
    
    /**
     * Commit current transaction.
     * 
     * @throws TxException if commit fails
     */
    void commit() throws TxException;
    
    /**
     * Rollback current transaction.
     * 
     * @throws TxException if rollback fails
     */
    void rollback() throws TxException;
    
    /**
     * Close current connection.
     */
    void close();
    
    /**
     * Check if there is an active transaction.
     * 
     * @return true if a transaction is active, false otherwise
     */
    boolean isActive();
    
    /**
     * Force cleanup of any active transaction.
     * Closes connection without commit or rollback.
     */
    void forceCleanup();
}

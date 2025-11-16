package app.infra.tx;

import app.config.AppConfig;
import app.domain.port.TxException;
import app.domain.port.TxManager;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * JDBC-based transaction manager implementation.
 * Uses ThreadLocal to maintain one connection per thread.
 */
public class JdbcTxManager implements TxManager {
    
    private static JdbcTxManager instance;
    private final ThreadLocal<Connection> connectionHolder = new ThreadLocal<>();
    private final AppConfig config;
    
    private JdbcTxManager() {
        this.config = AppConfig.getInstance();
        loadDriver();
    }
    
    public static JdbcTxManager getInstance() {
        if (instance == null) {
            synchronized (JdbcTxManager.class) {
                if (instance == null) {
                    instance = new JdbcTxManager();
                }
            }
        }
        return instance;
    }
    
    private void loadDriver() {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            System.err.println("MySQL JDBC Driver not found: " + e.getMessage());
        }
    }
    
    @Override
    public Connection getConnection() throws TxException {
        Connection conn = connectionHolder.get();
        
        // Check if connection exists and is still valid
        try {
            if (conn != null && conn.isClosed()) {
                // Connection is closed, clean up and create new one
                connectionHolder.remove();
                conn = null;
            }
        } catch (SQLException e) {
            // If we can't check, assume it's bad and clean up
            connectionHolder.remove();
            conn = null;
        }
        
        if (conn == null) {
            begin();
            conn = connectionHolder.get();
        }
        return conn;
    }
    
    @Override
    public void begin() throws TxException {
        if (connectionHolder.get() != null) {
            throw new TxException("Transaction already active");
        }
        
        try {
            Connection conn = DriverManager.getConnection(
                config.getDbUrl(),
                config.getDbUsername(),
                config.getDbPassword()
            );
            conn.setAutoCommit(false);
            connectionHolder.set(conn);
        } catch (SQLException e) {
            throw new TxException("Failed to start transaction", e);
        }
    }
    
    @Override
    public void commit() throws TxException {
        Connection conn = connectionHolder.get();
        if (conn == null) {
            throw new TxException("No active transaction");
        }
        
        try {
            // Check if connection is still valid
            if (conn.isClosed()) {
                throw new TxException("Connection is already closed");
            }
            conn.commit();
        } catch (SQLException e) {
            // Try to rollback if commit fails
            try {
                if (!conn.isClosed()) {
                    conn.rollback();
                }
            } catch (SQLException rollbackEx) {
                // Ignore rollback errors
            }
            throw new TxException("Failed to commit transaction", e);
        } finally {
            close();
        }
    }
    
    @Override
    public void rollback() throws TxException {
        Connection conn = connectionHolder.get();
        if (conn == null) {
            return; // Nothing to rollback
        }
        
        try {
            conn.rollback();
        } catch (SQLException e) {
            throw new TxException("Failed to rollback transaction", e);
        } finally {
            close();
        }
    }
    
    @Override
    public void close() {
        Connection conn = connectionHolder.get();
        if (conn != null) {
            try {
                conn.close();
            } catch (SQLException e) {
                System.err.println("Error closing connection: " + e.getMessage());
            } finally {
                connectionHolder.remove();
            }
        }
    }
    
    @Override
    public boolean isActive() {
        return connectionHolder.get() != null;
    }
    
    @Override
    public void forceCleanup() {
        Connection conn = connectionHolder.get();
        if (conn != null) {
            try {
                if (!conn.isClosed()) {
                    conn.rollback();  // Rollback any uncommitted changes
                }
            } catch (SQLException e) {
                System.err.println("Error during force cleanup rollback: " + e.getMessage());
            }
            close();  // Close and remove from ThreadLocal
        }
    }
}

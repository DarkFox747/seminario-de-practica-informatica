import app.config.AppConfig;
import app.infra.tx.JdbcTxManager;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

/**
 * Quick test to verify database connection.
 * Run this before starting the full application.
 */
public class TestConnection {
    
    public static void main(String[] args) {
        System.out.println("===========================================");
        System.out.println("   DATABASE CONNECTION TEST");
        System.out.println("===========================================\n");
        
        try {
            // Load configuration
            System.out.println("1. Loading configuration...");
            AppConfig config = AppConfig.getInstance();
            System.out.println("   ✓ Config loaded");
            System.out.println("   DB URL: " + config.getDbUrl());
            System.out.println("   DB User: " + config.getDbUsername());
            System.out.println();
            
            // Get transaction manager
            System.out.println("2. Initializing transaction manager...");
            JdbcTxManager txManager = JdbcTxManager.getInstance();
            System.out.println("   ✓ TxManager initialized");
            System.out.println();
            
            // Test connection
            System.out.println("3. Testing database connection...");
            txManager.begin();
            Connection conn = txManager.getConnection();
            System.out.println("   ✓ Connection established");
            System.out.println();
            
            // Test query
            System.out.println("4. Running test query...");
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT DATABASE() as db, VERSION() as version, NOW() as now");
            
            if (rs.next()) {
                System.out.println("   ✓ Query successful");
                System.out.println("   Database: " + rs.getString("db"));
                System.out.println("   MySQL Version: " + rs.getString("version"));
                System.out.println("   Server Time: " + rs.getString("now"));
            }
            rs.close();
            stmt.close();
            System.out.println();
            
            // Check tables
            System.out.println("5. Checking database tables...");
            stmt = conn.createStatement();
            rs = stmt.executeQuery("SHOW TABLES");
            
            int tableCount = 0;
            while (rs.next()) {
                tableCount++;
                System.out.println("   - " + rs.getString(1));
            }
            
            if (tableCount == 0) {
                System.out.println("   ⚠ No tables found. Run db-creation.sql first!");
            } else {
                System.out.println("   ✓ Found " + tableCount + " tables");
            }
            
            rs.close();
            stmt.close();
            txManager.commit();
            System.out.println();
            
            // Success
            System.out.println("===========================================");
            System.out.println("   ✅ ALL TESTS PASSED!");
            System.out.println("   Database connection is working correctly.");
            System.out.println("===========================================");
            
        } catch (Exception e) {
            System.err.println("\n===========================================");
            System.err.println("   ❌ CONNECTION FAILED!");
            System.err.println("===========================================");
            System.err.println("Error: " + e.getMessage());
            System.err.println("\nCommon solutions:");
            System.err.println("1. Verify MySQL is running: services.msc (Windows)");
            System.err.println("2. Check credentials in .env file");
            System.err.println("3. Verify database exists: CREATE DATABASE code_review_db;");
            System.err.println("4. Check connection URL in .env");
            System.err.println("\nStack trace:");
            e.printStackTrace();
            
            System.exit(1);
        }
    }
}

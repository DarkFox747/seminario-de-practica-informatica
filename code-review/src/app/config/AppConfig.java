package app.config;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;

/**
 * Application configuration loader.
 * Priority: .env file > app.properties > system properties
 */
public class AppConfig {
    
    private static AppConfig instance;
    private Properties properties;
    
    private AppConfig() {
        properties = new Properties();
        loadProperties();
    }
    
    public static AppConfig getInstance() {
        if (instance == null) {
            instance = new AppConfig();
        }
        return instance;
    }
    
    private void loadProperties() {
        // 1. Load app.properties first (defaults)
        loadAppProperties();
        
        // 2. Load .env file (overrides defaults with sensitive data)
        loadEnvFile();
        
        System.out.println("[AppConfig] Configuration loaded successfully");
        System.out.println("[AppConfig] DB URL: " + getDbUrl());
        System.out.println("[AppConfig] DB User: " + getDbUsername());
    }
    
    private void loadAppProperties() {
        try (InputStream input = getClass().getClassLoader()
                .getResourceAsStream("resources/app.properties")) {
            if (input == null) {
                System.err.println("[AppConfig] Unable to find app.properties");
                return;
            }
            properties.load(input);
            System.out.println("[AppConfig] Loaded app.properties");
        } catch (IOException e) {
            System.err.println("[AppConfig] Error loading app.properties: " + e.getMessage());
        }
    }
    
    private void loadEnvFile() {
        Path envPath = Paths.get(".env");
        
        if (!Files.exists(envPath)) {
            System.out.println("[AppConfig] .env file not found, using app.properties defaults");
            return;
        }
        
        try (FileInputStream fis = new FileInputStream(envPath.toFile())) {
            Properties envProps = new Properties();
            envProps.load(fis);
            
            // Override with .env values
            envProps.forEach((key, value) -> {
                String keyStr = key.toString();
                String valueStr = value.toString().trim();
                
                // Map .env keys to app.properties format
                switch (keyStr) {
                    case "DB_URL":
                        properties.setProperty("db.url", valueStr);
                        break;
                    case "DB_USERNAME":
                        properties.setProperty("db.username", valueStr);
                        break;
                    case "DB_PASSWORD":
                        properties.setProperty("db.password", valueStr);
                        break;
                    default:
                        // Keep other .env variables as-is
                        properties.setProperty(keyStr.toLowerCase().replace('_', '.'), valueStr);
                }
            });
            
            System.out.println("[AppConfig] Loaded .env file with " + envProps.size() + " properties");
        } catch (IOException e) {
            System.err.println("[AppConfig] Error loading .env: " + e.getMessage());
        }
    }
    
    public String getProperty(String key) {
        return properties.getProperty(key);
    }
    
    public String getProperty(String key, String defaultValue) {
        return properties.getProperty(key, defaultValue);
    }
    
    // Database configuration
    public String getDbUrl() {
        return getProperty("db.url");
    }
    
    public String getDbUsername() {
        return getProperty("db.username");
    }
    
    public String getDbPassword() {
        return getProperty("db.password");
    }
    
    // Git configuration
    public String getGitExecutable() {
        return getProperty("git.executable", "git");
    }
    
    // Endpoint configuration
    public boolean isEndpointMockEnabled() {
        return Boolean.parseBoolean(getProperty("endpoint.mock.enabled", "true"));
    }
    
    public String getEndpointMockDataPath() {
        return getProperty("endpoint.mock.dataPath");
    }
    
    // Policy configuration
    public String getPolicyDefaultName() {
        return getProperty("policy.default.name");
    }
    
    public String getPolicyRulesPath() {
        return getProperty("policy.rules.path");
    }
    
    // Application info
    public String getAppName() {
        return getProperty("app.name", "Code Review Analyzer");
    }
    
    public String getAppVersion() {
        return getProperty("app.version", "1.0.0");
    }
}

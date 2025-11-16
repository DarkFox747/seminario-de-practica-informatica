package app.application.service;

import app.domain.entity.User;
import app.domain.port.RepositoryException;
import app.domain.port.UserRepository;

import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Service for user authentication.
 * Handles login verification against the database.
 */
public class LoginService {
    
    private static final Logger LOGGER = Logger.getLogger(LoginService.class.getName());
    
    private final UserRepository userRepository;
    
    public LoginService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }
    
    /**
     * Authenticates a user by email and password.
     * 
     * @param email the user's email
     * @param password the plaintext password
     * @return Optional containing the authenticated User if successful, empty otherwise
     */
    public Optional<User> authenticate(String email, String password) {
        if (email == null || email.trim().isEmpty() || password == null || password.isEmpty()) {
            LOGGER.warning("Login attempt with empty credentials");
            return Optional.empty();
        }
        
        try {
            Optional<User> userOpt = userRepository.findByEmail(email.trim().toLowerCase());
            
            if (userOpt.isEmpty()) {
                LOGGER.info("Login failed: User not found for email: " + email);
                return Optional.empty();
            }
            
            User user = userOpt.get();
            
            // Check if user is active
            if (!user.isActive()) {
                LOGGER.info("Login failed: User is inactive: " + email);
                return Optional.empty();
            }
            
            // Verify password
            String storedHash = user.getPasswordHash();
            if (storedHash == null || !PasswordHasher.verify(password, storedHash)) {
                LOGGER.info("Login failed: Invalid password for email: " + email);
                return Optional.empty();
            }
            
            // NOTE: La tabla users no tiene last_login_at, omitir actualización
            // Si se necesita en el futuro, agregar columna a la BD
            
            LOGGER.info("Login successful for user: " + email);
            return Optional.of(user);
            
        } catch (RepositoryException e) {
            LOGGER.log(Level.SEVERE, "Error during authentication for email: " + email, e);
            return Optional.empty();
        }
    }
}

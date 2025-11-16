package app.ui;

import app.domain.entity.User;

/**
 * Singleton to store the currently logged-in user.
 * Provides global access to the authenticated user throughout the application.
 */
public class UserSession {
    
    private static User currentUser;
    
    private UserSession() {
        // Private constructor to prevent instantiation
    }
    
    /**
     * Sets the currently logged-in user.
     * 
     * @param user the authenticated user
     */
    public static void setCurrentUser(User user) {
        currentUser = user;
    }
    
    /**
     * Gets the currently logged-in user.
     * 
     * @return the current user, or null if no user is logged in
     */
    public static User getCurrentUser() {
        return currentUser;
    }
    
    /**
     * Clears the current user session (logout).
     */
    public static void clearSession() {
        currentUser = null;
    }
    
    /**
     * Checks if a user is currently logged in.
     * 
     * @return true if a user is logged in, false otherwise
     */
    public static boolean isLoggedIn() {
        return currentUser != null;
    }
}

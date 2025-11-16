package app.application.service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Utility class for password hashing using SHA-256.
 * Provides methods to hash plaintext passwords and verify them against stored hashes.
 */
public class PasswordHasher {
    
    private static final String ALGORITHM = "SHA-256";
    
    /**
     * Hashes a plaintext password using SHA-256.
     * 
     * @param plaintext the plaintext password
     * @return the hexadecimal representation of the hash
     * @throws RuntimeException if SHA-256 algorithm is not available
     */
    public static String hash(String plaintext) {
        try {
            MessageDigest digest = MessageDigest.getInstance(ALGORITHM);
            byte[] hashBytes = digest.digest(plaintext.getBytes(StandardCharsets.UTF_8));
            return bytesToHex(hashBytes);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 algorithm not available", e);
        }
    }
    
    /**
     * Verifies if a plaintext password matches a stored hash.
     * 
     * @param plaintext the plaintext password to verify
     * @param storedHash the stored hash to compare against
     * @return true if the password matches the hash, false otherwise
     */
    public static boolean verify(String plaintext, String storedHash) {
        String computedHash = hash(plaintext);
        return computedHash.equals(storedHash);
    }
    
    /**
     * Converts byte array to hexadecimal string.
     * 
     * @param bytes the byte array
     * @return the hexadecimal string representation
     */
    private static String bytesToHex(byte[] bytes) {
        StringBuilder hexString = new StringBuilder();
        for (byte b : bytes) {
            String hex = Integer.toHexString(0xff & b);
            if (hex.length() == 1) {
                hexString.append('0');
            }
            hexString.append(hex);
        }
        return hexString.toString();
    }
}

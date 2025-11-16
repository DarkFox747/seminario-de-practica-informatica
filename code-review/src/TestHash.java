import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

public class TestHash {
    public static void main(String[] args) throws Exception {
        String password = "demo123";
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] hashBytes = digest.digest(password.getBytes(StandardCharsets.UTF_8));
        
        StringBuilder hexString = new StringBuilder();
        for (byte b : hashBytes) {
            String hex = Integer.toHexString(0xff & b);
            if (hex.length() == 1) {
                hexString.append('0');
            }
            hexString.append(hex);
        }
        
        System.out.println("Password: " + password);
        System.out.println("SHA-256 Hash: " + hexString.toString());
    }
}

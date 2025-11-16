import app.application.service.PasswordHasher;

public class TestPasswordHash {
    public static void main(String[] args) {
        String password = "demo123";
        String hash = PasswordHasher.hash(password);
        
        System.out.println("Password: " + password);
        System.out.println("Hash SHA-256: " + hash);
        
        // Verificar contra el hash en la BD
        String dbHash = "9f735e0df9a1ddc702bf0a1a7b83033f9f894a07d1434d5a4c91b49fb6f86fde";
        boolean matches = PasswordHasher.verify(password, dbHash);
        
        System.out.println("\nDB Hash: " + dbHash);
        System.out.println("Matches: " + matches);
    }
}

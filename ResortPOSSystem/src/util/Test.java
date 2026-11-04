package util;

import config.DatabaseConnection;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import model.Cottage;
import model.User;

public class Test {

    public static void main(String[] args) {
        // 1. Hash test
        String hash = Password.hash("admin123");
        System.out.println("Hash of admin123: " + hash);

        // 2. Compare with database hash
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(
                     "SELECT password_hash FROM users WHERE username = ?")) {
            ps.setString(1, "admin");
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    String dbHash = rs.getString("password_hash");
                    System.out.println("Matches database? "
                            + Password.verify("admin123", dbHash));
                    System.out.println("Wrong password matches? "
                            + Password.verify("wrong123", dbHash));
                } else {
                    System.out.println("User 'admin' not found in the database.");
                }
            }
        } catch (Exception e) {
            System.out.println("FAILED: " + e.getMessage());
        }

        // 3. Session test
        SessionManager.login(new User(1, "admin", "Resort Administrator", "ADMIN", "ACTIVE"));
        System.out.println("Logged in? " + SessionManager.isLoggedIn());
        System.out.println("Is admin? " + SessionManager.isAdmin());
        System.out.println("Is cashier? " + SessionManager.isCashier());
        SessionManager.logout();
        System.out.println("Logged in after logout? " + SessionManager.isLoggedIn());

        // 4. Validation test
        System.out.println("Valid phone 09171234567: " + ValidationUtil.isValidPhone("09171234567"));
        System.out.println("Valid phone 12345: " + ValidationUtil.isValidPhone("12345"));
        System.out.println("Valid email a@b.com: " + ValidationUtil.isValidEmail("a@b.com"));
        System.out.println("Valid money 150.50: " + ValidationUtil.isValidMoney("150.50"));
        System.out.println("Valid money abc: " + ValidationUtil.isValidMoney("abc"));

        // 5. Model test
        Cottage c = new Cottage(1, "Cottage 1", "Small Cottage", 6,
                new java.math.BigDecimal("500.00"), "AVAILABLE");
        System.out.println("Cottage: " + c);
    }
}
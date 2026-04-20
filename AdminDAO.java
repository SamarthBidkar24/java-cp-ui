import java.sql.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * AdminDAO class for administrative authentication and registration.
 * Product and Order operations are moved to ProductDAO and OrderDAO.
 */
public class AdminDAO {

    public AdminDAO() {
        checkAndCreateTables();
        seedDefaultAdmin();
    }

    private void seedDefaultAdmin() {
        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement()) {
            
            // Check if ANY admin exists
            ResultSet rsCount = stmt.executeQuery("SELECT COUNT(*) FROM admins");
            if (rsCount.next() && rsCount.getInt(1) == 0) {
                System.out.println("[SECURITY] No admins found. Seeding default admin...");
                registerAdmin("System Admin", "admin@supermart.com", "0000000000", "admin123");
                return;
            }

            // Check if 'admin@supermart.com' exists but is still plaintext
            String checkSql = "SELECT password_salt FROM admins WHERE email = 'admin@supermart.com'";
            ResultSet rsCheck = stmt.executeQuery(checkSql);
            if (rsCheck.next()) {
                String salt = rsCheck.getString("password_salt");
                if (salt == null || salt.trim().isEmpty()) {
                    System.out.println("[SECURITY] Found legacy admin. Migrating to hashed password...");
                    String newSalt = SecurityUtils.generateSalt();
                    String newHash = SecurityUtils.hashPassword("admin123", newSalt);
                    String updateSql = "UPDATE admins SET password = ?, password_salt = ?, role = 'ADMIN', is_active = TRUE WHERE email = 'admin@supermart.com'";
                    try (PreparedStatement pstmt = conn.prepareStatement(updateSql)) {
                        pstmt.setString(1, newHash);
                        pstmt.setString(2, newSalt);
                        pstmt.executeUpdate();
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void checkAndCreateTables() {
        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement()) {
            
            // Helper to add column if not exists
            addColIfMissing(stmt, "admins", "password_salt", "VARCHAR(100)");
            addColIfMissing(stmt, "admins", "role", "VARCHAR(20) DEFAULT 'ADMIN'");
            addColIfMissing(stmt, "admins", "is_active", "BOOLEAN DEFAULT TRUE");
            
            // Ensure existing admins are active
            stmt.executeUpdate("UPDATE admins SET is_active = TRUE WHERE is_active IS NULL");
            
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void addColIfMissing(Statement stmt, String table, String col, String type) {
        try {
            stmt.executeUpdate("ALTER TABLE " + table + " ADD COLUMN " + col + " " + type);
            System.out.println("[DB] Added column " + col + " to " + table);
        } catch (SQLException e) {
            if (e.getErrorCode() != 1060) { // 1060 = Duplicate column name
                System.err.println("[DB] Error adding column " + col + ": " + e.getMessage());
            }
        }
    }

    /**
     * Admin login validation.
     * 
     * @param email The admin's email.
     * @param password The admin's password.
     * @return Admin object if successful, null otherwise.
     */
    public Admin adminLogin(String email, String password) {
        String query = "SELECT * FROM admins WHERE email = ? AND is_active = TRUE";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            
            pstmt.setString(1, email);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    String storedHash = rs.getString("password");
                    String storedSalt = rs.getString("password_salt");
                    
                    if (storedSalt == null || storedSalt.isEmpty()) {
                        // LEGACY FALLBACK: Plaintext check
                        if (password.equals(storedHash)) {
                            System.out.println("[SECURITY] Upgrading legacy plaintext account for: " + email);
                            String newSalt = SecurityUtils.generateSalt();
                            String newHash = SecurityUtils.hashPassword(password, newSalt);
                            String updateSql = "UPDATE admins SET password = ?, password_salt = ? WHERE email = ?";
                            try (PreparedStatement upstmt = conn.prepareStatement(updateSql)) {
                                upstmt.setString(1, newHash);
                                upstmt.setString(2, newSalt);
                                upstmt.setString(3, email);
                                upstmt.executeUpdate();
                            }
                            return new Admin(rs.getInt("id"), rs.getString("name"), rs.getString("email"), rs.getString("phone"), newHash);
                        }
                    } else {
                        // SECURE HASHED check
                        if (SecurityUtils.verifyPassword(password, storedHash, storedSalt)) {
                            return new Admin(
                                rs.getInt("id"),
                                rs.getString("name"),
                                rs.getString("email"),
                                rs.getString("phone"),
                                rs.getString("password")
                            );
                        }
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Checks if an admin email already exists.
     */
    public boolean emailExists(String email) {
        String query = "SELECT COUNT(*) FROM admins WHERE email = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            
            pstmt.setString(1, email);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) return rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean registerAdmin(String name, String email, String phone, String password) {
        String salt = SecurityUtils.generateSalt();
        String hash = SecurityUtils.hashPassword(password, salt);
        String query = "INSERT INTO admins (name, email, phone, password, password_salt, role, is_active) VALUES (?, ?, ?, ?, ?, 'ADMIN', TRUE)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            
            pstmt.setString(1, name);
            pstmt.setString(2, email);
            pstmt.setString(3, phone);
            pstmt.setString(4, hash);
            pstmt.setString(5, salt);
            return pstmt.executeUpdate() == 1;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
}

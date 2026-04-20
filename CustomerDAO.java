import java.sql.*;

/**
 * CustomerDAO class for database operations on the 'customers' table.
 */
public class CustomerDAO {

    public CustomerDAO() {
        checkAndCreateTables();
    }

    private void checkAndCreateTables() {
        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement()) {
            addColIfMissing(stmt, "customers", "password_salt", "VARCHAR(100)");
            addColIfMissing(stmt, "customers", "role", "VARCHAR(20) DEFAULT 'CUSTOMER'");
            addColIfMissing(stmt, "customers", "is_verified", "BOOLEAN DEFAULT FALSE");
            addColIfMissing(stmt, "customers", "status", "VARCHAR(20) DEFAULT 'ACTIVE'");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void addColIfMissing(Statement stmt, String table, String col, String type) {
        try {
            stmt.executeUpdate("ALTER TABLE " + table + " ADD COLUMN " + col + " " + type);
            System.out.println("[DB] Added column " + col + " to " + table);
        } catch (SQLException e) {
            if (e.getErrorCode() != 1060) {
                System.err.println("[DB] Error adding column " + col + ": " + e.getMessage());
            }
        }
    }

    /**
     * Checks if an email already exists in the 'customers' table.
     */
    public boolean emailExists(String email) {
        String query = "SELECT COUNT(*) FROM customers WHERE email = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setString(1, email);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) return rs.getInt(1) > 0;
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return false;
    }

    /**
     * Retrieves a customer by their unique ID.
     */
    public Customer getCustomerById(int id) {
        String query = "SELECT * FROM customers WHERE id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setInt(1, id);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) return mapResultSetToCustomer(rs);
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return null;
    }

    /**
     * Retrieves a customer by their email address.
     */
    public Customer getCustomerByEmail(String email) {
        String query = "SELECT * FROM customers WHERE email = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setString(1, email);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) return mapResultSetToCustomer(rs);
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return null;
    }

    /**
     * Updates a customer's password after verifying the current one.
     */
    public boolean updatePassword(int id, String currentPwd, String newPwd) {
        String verifyQuery = "SELECT password FROM customers WHERE id = ?";
        String updateQuery = "UPDATE customers SET password = ? WHERE id = ?";
        
        try (Connection conn = DBConnection.getConnection()) {
            try (PreparedStatement vpstmt = conn.prepareStatement(verifyQuery)) {
                vpstmt.setInt(1, id);
                try (ResultSet rs = vpstmt.executeQuery()) {
                    if (rs.next() && rs.getString("password").equals(currentPwd)) {
                        try (PreparedStatement upstmt = conn.prepareStatement(updateQuery)) {
                            upstmt.setString(1, newPwd);
                            upstmt.setInt(2, id);
                            return upstmt.executeUpdate() == 1;
                        }
                    }
                }
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return false;
    }

    public boolean registerCustomer(String name, String email, String phone, String password) {
        String salt = SecurityUtils.generateSalt();
        String hash = SecurityUtils.hashPassword(password, salt);
        String query = "INSERT INTO customers (name, email, phone, password, password_salt, role, is_verified, status) VALUES (?, ?, ?, ?, ?, 'CUSTOMER', FALSE, 'ACTIVE')";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setString(1, name);
            pstmt.setString(2, email);
            pstmt.setString(3, phone);
            pstmt.setString(4, hash);
            pstmt.setString(5, salt);
            return pstmt.executeUpdate() == 1;
        } catch (SQLException e) { e.printStackTrace(); }
        return false;
    }

    public Customer customerLogin(String email, String password) {
        String query = "SELECT * FROM customers WHERE email = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setString(1, email);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    String storedHash = rs.getString("password");
                    String storedSalt = rs.getString("password_salt");
                    
                    if (storedSalt == null || storedSalt.isEmpty()) {
                        if (password.equals(storedHash)) {
                            System.out.println("[SECURITY] Upgrading legacy plaintext account for customer: " + email);
                            String newSalt = SecurityUtils.generateSalt();
                            String newHash = SecurityUtils.hashPassword(password, newSalt);
                            try (PreparedStatement upstmt = conn.prepareStatement("UPDATE customers SET password = ?, password_salt = ? WHERE email = ?")) {
                                upstmt.setString(1, newHash);
                                upstmt.setString(2, newSalt);
                                upstmt.setString(3, email);
                                upstmt.executeUpdate();
                            }
                            return mapResultSetToCustomer(rs);
                        }
                    } else if (SecurityUtils.verifyPassword(password, storedHash, storedSalt)) {
                        return mapResultSetToCustomer(rs);
                    }
                }
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return null;
    }

    /**
     * Maps a result set row to a Customer object.
     */
    private Customer mapResultSetToCustomer(ResultSet rs) throws SQLException {
        return new Customer(
            rs.getInt("id"),
            rs.getString("name"),
            rs.getString("email"),
            rs.getString("phone"),
            rs.getString("password")
        );
    }
}

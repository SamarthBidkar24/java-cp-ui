import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * DeliveryDAO for managing delivery personnel.
 */
public class DeliveryDAO {

    public DeliveryDAO() {
        checkAndCreateTables();
    }

    private void checkAndCreateTables() {
        String sql = "CREATE TABLE IF NOT EXISTS delivery_agents (" +
                     "agent_id INT AUTO_INCREMENT PRIMARY KEY, " +
                     "full_name VARCHAR(100), " +
                     "email VARCHAR(100) UNIQUE, " +
                     "phone VARCHAR(20), " +
                     "password VARCHAR(100), " +
                     "vehicle_type VARCHAR(50), " +
                     "assigned_area VARCHAR(100), " +
                     "is_active BOOLEAN DEFAULT TRUE, " +
                     "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP)";
        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.executeUpdate(sql);
            
            addColIfMissing(stmt, "delivery_agents", "password_salt", "VARCHAR(100)");
            addColIfMissing(stmt, "delivery_agents", "role", "VARCHAR(20) DEFAULT 'DELIVERY'");
            addColIfMissing(stmt, "delivery_agents", "approval_status", "VARCHAR(20) DEFAULT 'PENDING'");
            
            System.out.println("[DIAGNOSTIC] Verified/Created 'delivery_agents' table with security columns.");
        } catch (SQLException e) {
            System.err.println("[DIAGNOSTIC] Table creation failed: " + e.getMessage());
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

    public boolean registerAgent(String name, String email, String phone, String pwd, String vehicle, String area, String status) {
        System.out.println("[DEBUG] Registering Delivery Agent (" + status + "): " + email);
        String salt = SecurityUtils.generateSalt();
        String hash = SecurityUtils.hashPassword(pwd, salt);
        String sql = "INSERT INTO delivery_agents (full_name, email, phone, password, password_salt, vehicle_type, assigned_area, approval_status) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, name);
            pstmt.setString(2, email);
            pstmt.setString(3, phone);
            pstmt.setString(4, hash);
            pstmt.setString(5, salt);
            pstmt.setString(6, vehicle);
            pstmt.setString(7, area);
            pstmt.setString(8, status);
            boolean success = pstmt.executeUpdate() == 1;
            System.out.println("[DEBUG] Registration " + (success ? "SUCCESS" : "FAILED") + " for: " + email);
            return success;
        } catch (SQLException e) {
            System.err.println("[DEBUG] Registration ERROR: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    public DeliveryAgent agentLogin(String email, String pwd) {
        System.out.println("[DEBUG] Attempting Delivery Login for: " + email);
        String sql = "SELECT * FROM delivery_agents WHERE email = ? AND is_active = TRUE";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, email);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    String storedHash = rs.getString("password");
                    String storedSalt = rs.getString("password_salt");
                    String status = rs.getString("approval_status");
                    
                    if (!"APPROVED".equalsIgnoreCase(status)) {
                        System.out.println("[DEBUG] Login DENIED for: " + email + " (Status: " + status + ")");
                        return null; 
                    }
                    
                    if (storedSalt == null || storedSalt.isEmpty()) {
                        if (pwd.equals(storedHash)) {
                            System.out.println("[SECURITY] Upgrading legacy plaintext account for agent: " + email);
                            String newSalt = SecurityUtils.generateSalt();
                            String newHash = SecurityUtils.hashPassword(pwd, newSalt);
                            try (PreparedStatement upstmt = conn.prepareStatement("UPDATE delivery_agents SET password = ?, password_salt = ? WHERE email = ?")) {
                                upstmt.setString(1, newHash);
                                upstmt.setString(2, newSalt);
                                upstmt.setString(3, email);
                                upstmt.executeUpdate();
                            }
                            return mapRow(rs);
                        }
                    } else if (SecurityUtils.verifyPassword(pwd, storedHash, storedSalt)) {
                        System.out.println("[DEBUG] Login SUCCESS for: " + email);
                        return mapRow(rs);
                    }
                }
                System.out.println("[DEBUG] Login FAILED for: " + email + " (No match or inactive)");
            }
        } catch (SQLException e) {
            System.err.println("[DEBUG] SQL Error during login: " + e.getMessage());
            e.printStackTrace();
        }
        return null;
    }

    public List<DeliveryAgent> getAllAgents() {
        List<DeliveryAgent> agents = new ArrayList<>();
        String sql = "SELECT * FROM delivery_agents";
        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                agents.add(mapRow(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return agents;
    }

    public boolean updateAgentStatus(int agentId, boolean active) {
        String sql = "UPDATE delivery_agents SET is_active = ? WHERE agent_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setBoolean(1, active);
            pstmt.setInt(2, agentId);
            return pstmt.executeUpdate() == 1;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public List<DeliveryAgent> getPendingAgents() {
        List<DeliveryAgent> agents = new ArrayList<>();
        String sql = "SELECT * FROM delivery_agents WHERE approval_status = 'PENDING'";
        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                agents.add(mapRow(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return agents;
    }

    public boolean updateAgentApproval(int agentId, String status) {
        String sql = "UPDATE delivery_agents SET approval_status = ? WHERE agent_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, status);
            pstmt.setInt(2, agentId);
            return pstmt.executeUpdate() == 1;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    private DeliveryAgent mapRow(ResultSet rs) throws SQLException {
        DeliveryAgent a = new DeliveryAgent();
        a.setAgentId(rs.getInt("agent_id"));
        a.setFullName(rs.getString("full_name"));
        a.setEmail(rs.getString("email"));
        a.setPhone(rs.getString("phone"));
        a.setPassword(rs.getString("password"));
        a.setVehicleType(rs.getString("vehicle_type"));
        a.setAssignedArea(rs.getString("assigned_area"));
        a.setActive(rs.getBoolean("is_active"));
        
        try { a.setApprovalStatus(rs.getString("approval_status")); } catch (Exception e) {}
        
        a.setCreatedAt(rs.getString("created_at"));
        return a;
    }

    public boolean emailExists(String email) {
        String sql = "SELECT 1 FROM delivery_agents WHERE email = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, email);
            try (ResultSet rs = pstmt.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            return false;
        }
    }
}

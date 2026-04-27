import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * DeliveryAssignmentDAO for managing order assignments to delivery agents.
 */
public class DeliveryAssignmentDAO {

    public DeliveryAssignmentDAO() {
        checkAndCreateTables();
    }

    private void checkAndCreateTables() {
        String sql = "CREATE TABLE IF NOT EXISTS delivery_assignments (" +
                     "assignment_id INT AUTO_INCREMENT PRIMARY KEY, " +
                     "order_id VARCHAR(50), " +
                     "agent_id INT, " +
                     "delivery_type VARCHAR(50), " +
                     "route_sequence INT, " +
                     "eta_minutes INT, " +
                     "assigned_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
                     "status VARCHAR(50), " +
                     "FOREIGN KEY (agent_id) REFERENCES delivery_agents(agent_id))";
        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.executeUpdate(sql);
            System.out.println("[DIAGNOSTIC] Verified/Created 'delivery_assignments' table.");
        } catch (SQLException e) {
            System.err.println("[DIAGNOSTIC] Assignment table creation failed: " + e.getMessage());
        }
    }

    public boolean assignOrder(String orderId, int agentId, String deliveryType, int sequence, int eta) {
        String sql = "INSERT INTO delivery_assignments (order_id, agent_id, delivery_type, route_sequence, eta_minutes, status) VALUES (?, ?, ?, ?, ?, 'Assigned')";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, orderId);
            pstmt.setInt(2, agentId);
            pstmt.setString(3, deliveryType);
            pstmt.setInt(4, sequence);
            pstmt.setInt(5, eta);
            return pstmt.executeUpdate() == 1;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public List<DeliveryAssignment> getAssignmentsByAgentId(int agentId) {
        List<DeliveryAssignment> list = new ArrayList<>();
        String sql = "SELECT da.*, o.customer_email, o.address, o.pincode, o.status as order_status, c.name as customer_name " +
                     "FROM delivery_assignments da " +
                     "JOIN orders o ON da.order_id = o.order_id " +
                     "JOIN customers c ON o.customer_email = c.email " +
                     "WHERE da.agent_id = ? AND da.status != 'Delivered' " +
                     "ORDER BY da.delivery_type, da.route_sequence";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, agentId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    DeliveryAssignment a = mapRow(rs);
                    a.setCustomerName(rs.getString("customer_name"));
                    a.setCustomerAddress(rs.getString("address"));
                    a.setPincode(rs.getString("pincode"));
                    a.setOrderStatus(rs.getString("order_status"));
                    list.add(a);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public boolean updateAssignmentStatus(int assignmentId, String orderId, String status) {
        String sql1 = "UPDATE delivery_assignments SET status = ? WHERE assignment_id = ?";
        String sql2 = "UPDATE orders SET status = ? WHERE order_id = ?";
        Connection conn = null;
        try {
            conn = DBConnection.getConnection();
            conn.setAutoCommit(false);
            
            try (PreparedStatement pstmt1 = conn.prepareStatement(sql1)) {
                pstmt1.setString(1, status);
                pstmt1.setInt(2, assignmentId);
                pstmt1.executeUpdate();
            }
            
            try (PreparedStatement pstmt2 = conn.prepareStatement(sql2)) {
                pstmt2.setString(1, status);
                pstmt2.setString(2, orderId);
                pstmt2.executeUpdate();
            }
            
            conn.commit();
            return true;
        } catch (SQLException e) {
            if (conn != null) try { conn.rollback(); } catch (SQLException ex) {}
            e.printStackTrace();
            return false;
        } finally {
            if (conn != null) try { conn.close(); } catch (SQLException e) {}
        }
    }

    private DeliveryAssignment mapRow(ResultSet rs) throws SQLException {
        DeliveryAssignment a = new DeliveryAssignment();
        a.setAssignmentId(rs.getInt("assignment_id"));
        a.setOrderId(rs.getString("order_id"));
        a.setAgentId(rs.getInt("agent_id"));
        a.setDeliveryType(rs.getString("delivery_type"));
        a.setRouteSequence(rs.getInt("route_sequence"));
        a.setEtaMinutes(rs.getInt("eta_minutes"));
        a.setAssignedAt(rs.getString("assigned_at"));
        a.setStatus(rs.getString("status"));
        return a;
    }

    public int getNextSequenceForAgent(int agentId, String deliveryType) {
        String sql = "SELECT MAX(route_sequence) FROM delivery_assignments WHERE agent_id = ? AND delivery_type = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, agentId);
            pstmt.setString(2, deliveryType);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) return rs.getInt(1) + 1;
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return 1;
    }

    public DeliveryAssignment getAssignmentByOrderId(String orderId) {
        String sql = "SELECT da.*, a.full_name as agent_name FROM delivery_assignments da " +
                     "JOIN delivery_agents a ON da.agent_id = a.agent_id " +
                     "WHERE da.order_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, orderId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    DeliveryAssignment a = mapRow(rs);
                    // could add agent name to model if needed
                    return a;
                }
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return null;
    }
}

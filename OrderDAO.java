import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * OrderDAO class for managing orders in the database.
 */
public class OrderDAO {

    /**
     * Places a new order in the database.
     * 
     * @param order The Order object to insert.
     * @return true if insertion was successful.
     */
    public boolean placeOrder(Order order) {
        String query = "INSERT INTO orders (order_id, customer_email, order_date, status, total, delivery_type, address, payment_method, items_summary) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        Connection conn = null;
        PreparedStatement pstmt = null;
        boolean success = false;

        try {
            conn = DBConnection.getConnection();
            if (conn != null) {
                pstmt = conn.prepareStatement(query);
                pstmt.setString(1, order.getOrderId());
                pstmt.setString(2, order.getCustomerEmail());
                pstmt.setString(3, order.getDate());
                pstmt.setString(4, order.getStatus());
                pstmt.setDouble(5, order.getTotal());
                pstmt.setString(6, order.getDeliveryType());
                pstmt.setString(7, order.getAddress());
                pstmt.setString(8, order.getPaymentMethod());
                pstmt.setString(9, order.getItemsSummary());

                int rows = pstmt.executeUpdate();
                success = (rows == 1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            closeResources(conn, pstmt, null);
        }
        return success;
    }

    /**
     * Retrieves all orders placed by a specific customer.
     * 
     * @param email The customer's email.
     * @return List of Order objects.
     */
    public List<Order> getOrdersByCustomer(String email) {
        List<Order> orders = new ArrayList<>();
        String query = "SELECT * FROM orders WHERE customer_email = ?";
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            conn = DBConnection.getConnection();
            if (conn != null) {
                pstmt = conn.prepareStatement(query);
                pstmt.setString(1, email);
                rs = pstmt.executeQuery();
                while (rs.next()) {
                    orders.add(mapResultSetToOrder(rs));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            closeResources(conn, pstmt, rs);
        }
        return orders;
    }

    /**
     * Retrieves all orders from the database.
     * 
     * @return List of all Order objects.
     */
    public List<Order> getAllOrders() {
        List<Order> orders = new ArrayList<>();
        String query = "SELECT * FROM orders";
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            conn = DBConnection.getConnection();
            if (conn != null) {
                pstmt = conn.prepareStatement(query);
                rs = pstmt.executeQuery();
                while (rs.next()) {
                    orders.add(mapResultSetToOrder(rs));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            closeResources(conn, pstmt, rs);
        }
        return orders;
    }

    /**
     * Updates the status of an existing order.
     * 
     * @param orderId The unique order ID.
     * @param newStatus The new status to set.
     * @return true if update was successful.
     */
    public boolean updateOrderStatus(String orderId, String newStatus) {
        String query = "UPDATE orders SET status = ? WHERE order_id = ?";
        Connection conn = null;
        PreparedStatement pstmt = null;
        boolean success = false;

        try {
            conn = DBConnection.getConnection();
            if (conn != null) {
                pstmt = conn.prepareStatement(query);
                pstmt.setString(1, newStatus);
                pstmt.setString(2, orderId);
                int rows = pstmt.executeUpdate();
                success = (rows == 1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            closeResources(conn, pstmt, null);
        }
        return success;
    }

    /**
     * Helper method to map a ResultSet row to an Order object.
     */
    private Order mapResultSetToOrder(ResultSet rs) throws SQLException {
        return new Order(
            rs.getString("order_id"),
            rs.getString("order_date"),
            rs.getString("status"),
            rs.getDouble("total"),
            rs.getString("delivery_type"),
            rs.getString("address"),
            rs.getString("payment_method"),
            rs.getString("items_summary"),
            rs.getString("customer_email")
        );
    }

    private void closeResources(Connection conn, PreparedStatement pstmt, ResultSet rs) {
        try { if (rs != null) rs.close(); } catch (SQLException e) { e.printStackTrace(); }
        try { if (pstmt != null) pstmt.close(); } catch (SQLException e) { e.printStackTrace(); }
        try { if (conn != null) conn.close(); } catch (SQLException e) { e.printStackTrace(); }
    }
}

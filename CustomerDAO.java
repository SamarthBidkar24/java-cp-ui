import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * CustomerDAO class for database operations on the 'customers' table.
 */
public class CustomerDAO {

    /**
     * Checks if an email already exists in the 'customers' table.
     * 
     * @param email The email to check.
     * @return true if count > 0, false otherwise.
     */
    public boolean emailExists(String email) {
        String query = "SELECT COUNT(*) FROM customers WHERE email = ?";
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        boolean exists = false;

        try {
            conn = DBConnection.getConnection();
            if (conn != null) {
                pstmt = conn.prepareStatement(query);
                pstmt.setString(1, email);
                rs = pstmt.executeQuery();
                if (rs.next()) {
                    exists = rs.getInt(1) > 0;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            closeResources(conn, pstmt, rs);
        }

        return exists;
    }

    /**
     * Registers a new customer into the 'customers' table.
     * 
     * @param name Customer name
     * @param email Customer email
     * @param phone Customer phone
     * @param password Customer password
     * @return true if 1 row was successfully inserted.
     */
    public boolean registerCustomer(String name, String email, String phone, String password) {
        String query = "INSERT INTO customers (name, email, phone, password) VALUES (?, ?, ?, ?)";
        Connection conn = null;
        PreparedStatement pstmt = null;
        boolean success = false;

        try {
            conn = DBConnection.getConnection();
            if (conn != null) {
                pstmt = conn.prepareStatement(query);
                pstmt.setString(1, name);
                pstmt.setString(2, email);
                pstmt.setString(3, phone);
                pstmt.setString(4, password);

                int rowsAffected = pstmt.executeUpdate();
                success = rowsAffected == 1;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            closeResources(conn, pstmt, null);
        }

        return success;
    }

    /**
     * Validates a customer login.
     * 
     * @param email Customer email
     * @param password Customer password
     * @return Customer object if successful, null otherwise.
     */
    public Customer login(String email, String password) {
        String query = "SELECT * FROM customers WHERE email = ? AND password = ?";
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        Customer customer = null;

        try {
            conn = DBConnection.getConnection();
            if (conn != null) {
                pstmt = conn.prepareStatement(query);
                pstmt.setString(1, email);
                pstmt.setString(2, password);
                rs = pstmt.executeQuery();

                if (rs.next()) {
                    customer = new Customer();
                    customer.setId(rs.getInt("id"));
                    customer.setName(rs.getString("name"));
                    customer.setEmail(rs.getString("email"));
                    customer.setPhone(rs.getString("phone"));
                    customer.setPassword(rs.getString("password"));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            closeResources(conn, pstmt, rs);
        }

        return customer;
    }

    /**
     * Helper method to close database resources in finally blocks.
     */
    private void closeResources(Connection conn, PreparedStatement pstmt, ResultSet rs) {
        try {
            if (rs != null) rs.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        try {
            if (pstmt != null) pstmt.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        try {
            if (conn != null) conn.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}

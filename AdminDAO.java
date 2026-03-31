import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * AdminDAO class for administrative operations on products and orders.
 */
public class AdminDAO {

    /**
     * Retrieves all products from the 'products' table.
     * 
     * @return List of Product objects.
     */
    public List<Product> getAllProducts() {
        List<Product> products = new ArrayList<>();
        String query = "SELECT * FROM products";
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            conn = DBConnection.getConnection();
            if (conn != null) {
                pstmt = conn.prepareStatement(query);
                rs = pstmt.executeQuery();
                while (rs.next()) {
                    Product product = new Product(
                        rs.getInt("id"),
                        rs.getString("name"),
                        rs.getInt("quantity"),
                        rs.getDouble("price"),
                        rs.getString("category"),
                        rs.getString("image_path")
                    );
                    products.add(product);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            closeResources(conn, pstmt, rs);
        }
        return products;
    }

    /**
     * Adds a new product to the database.
     * 
     * @return true if insertion was successful.
     */
    public boolean addProduct(String name, int quantity, double price, String category, String imagePath) {
        String query = "INSERT INTO products (name, quantity, price, category, image_path) VALUES (?, ?, ?, ?, ?)";
        Connection conn = null;
        PreparedStatement pstmt = null;
        boolean success = false;

        try {
            conn = DBConnection.getConnection();
            if (conn != null) {
                pstmt = conn.prepareStatement(query);
                pstmt.setString(1, name);
                pstmt.setInt(2, quantity);
                pstmt.setDouble(3, price);
                pstmt.setString(4, category);
                pstmt.setString(5, imagePath);
                
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
     * Updates an existing product in the database.
     * 
     * @return true if update was successful.
     */
    public boolean updateProduct(int id, String name, int quantity, double price, String category, String imagePath) {
        String query = "UPDATE products SET name=?, quantity=?, price=?, category=?, image_path=? WHERE id=?";
        Connection conn = null;
        PreparedStatement pstmt = null;
        boolean success = false;

        try {
            conn = DBConnection.getConnection();
            if (conn != null) {
                pstmt = conn.prepareStatement(query);
                pstmt.setString(1, name);
                pstmt.setInt(2, quantity);
                pstmt.setDouble(3, price);
                pstmt.setString(4, category);
                pstmt.setString(5, imagePath);
                pstmt.setInt(6, id);

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
     * Deletes a product from the database.
     * 
     * @param id The product ID to delete.
     * @return true if deletion was successful.
     */
    public boolean deleteProduct(int id) {
        String query = "DELETE FROM products WHERE id=?";
        Connection conn = null;
        PreparedStatement pstmt = null;
        boolean success = false;

        try {
            conn = DBConnection.getConnection();
            if (conn != null) {
                pstmt = conn.prepareStatement(query);
                pstmt.setInt(1, id);
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
     * Retrieves all orders from the 'orders' table.
     * 
     * @return List of Order objects.
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
                    Order order = new Order(
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
                    orders.add(order);
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
     * @return true if update was successful.
     */
    public boolean updateOrderStatus(String orderId, String newStatus) {
        String query = "UPDATE orders SET status=? WHERE order_id=?";
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
     * Admin login validation.
     * 
     * @param email The admin's email.
     * @param password The admin's password.
     * @return Admin object if successful, null otherwise.
     */
    public Admin loginAdmin(String email, String password) {
        String query = "SELECT * FROM admins WHERE email = ? AND password = ?";
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        Admin admin = null;

        try {
            conn = DBConnection.getConnection();
            if (conn != null) {
                pstmt = conn.prepareStatement(query);
                pstmt.setString(1, email);
                pstmt.setString(2, password);
                rs = pstmt.executeQuery();
                if (rs.next()) {
                    admin = new Admin(
                        rs.getInt("id"),
                        rs.getString("name"),
                        rs.getString("email"),
                        rs.getString("phone"),
                        rs.getString("password")
                    );
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            closeResources(conn, pstmt, rs);
        }
        return admin;
    }

    /**
     * Checks if an admin email already exists.
     * 
     * @param email The email to check.
     * @return true if it exists.
     */
    public boolean emailExists(String email) {
        String query = "SELECT COUNT(*) FROM admins WHERE email = ?";
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
     * Registers a new admin.
     * 
     * @return true if successful.
     */
    public boolean registerAdmin(String name, String email, String phone, String password) {
        String query = "INSERT INTO admins (name, email, phone, password) VALUES (?, ?, ?, ?)";
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
                success = pstmt.executeUpdate() == 1;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            closeResources(conn, pstmt, null);
        }
        return success;
    }

    private void closeResources(Connection conn, PreparedStatement pstmt, ResultSet rs) {
        try { if (rs != null) rs.close(); } catch (SQLException e) { e.printStackTrace(); }
        try { if (pstmt != null) pstmt.close(); } catch (SQLException e) { e.printStackTrace(); }
        try { if (conn != null) conn.close(); } catch (SQLException e) { e.printStackTrace(); }
    }
}

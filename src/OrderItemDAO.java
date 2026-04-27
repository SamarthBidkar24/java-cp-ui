import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * OrderItemDAO for managing individual items in an order.
 */
public class OrderItemDAO {

    /**
     * Retrieves all items for a specific order.
     */
    public List<OrderItem> getItemsByOrder(String orderId) {
        List<OrderItem> items = new ArrayList<>();
        // Note: Joins with products table to get product names if needed
        String query = "SELECT oi.*, p.name as product_name, p.category as product_cat FROM order_items oi " +
                       "JOIN products p ON oi.product_id = p.id WHERE oi.order_id = ?";
        
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            
            pstmt.setString(1, orderId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    Product p = new Product();
                    p.setId(rs.getInt("product_id"));
                    p.setName(rs.getString("product_name"));
                    p.setCategory(rs.getString("product_cat"));
                    
                    items.add(new OrderItem(
                        rs.getInt("id"),
                        rs.getString("order_id"),
                        p,
                        rs.getInt("quantity"),
                        rs.getDouble("price_at_order")
                    ));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return items;
    }
}

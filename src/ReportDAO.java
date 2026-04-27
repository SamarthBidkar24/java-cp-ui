import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * ReportDAO class for generating sales reports using MySQL.
 */
public class ReportDAO {

    /**
     * Retrieves comprehensive metrics for a specific date.
     */
    public Map<String, Object> getMetricsForDate(String date) {
        Map<String, Object> metrics = new HashMap<>();
        String query = "SELECT " +
                       "COUNT(*) as total_orders, " +
                       "SUM(total) as total_revenue, " +
                       "SUM(CASE WHEN delivery_type = 'Same Day' THEN 1 ELSE 0 END) as same_day_count, " +
                       "SUM(CASE WHEN delivery_type = 'Next Day' THEN 1 ELSE 0 END) as next_day_count, " +
                       "SUM(CASE WHEN status = 'Delivered' THEN 1 ELSE 0 END) as delivered_count, " +
                       "SUM(CASE WHEN status = 'Processing' THEN 1 ELSE 0 END) as processing_count, " +
                       "SUM(CASE WHEN status = 'Pending' THEN 1 ELSE 0 END) as pending_count, " +
                       "AVG(total) as avg_order_value, " +
                       "MAX(total) as max_order_value, " +
                       "MIN(total) as min_order_value " +
                       "FROM orders WHERE DATE(order_date) = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            
            pstmt.setString(1, date);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    metrics.put("total_orders", rs.getInt("total_orders"));
                    metrics.put("total_revenue", rs.getDouble("total_revenue"));
                    metrics.put("same_day_count", rs.getInt("same_day_count"));
                    metrics.put("next_day_count", rs.getInt("next_day_count"));
                    metrics.put("delivered_count", rs.getInt("delivered_count"));
                    metrics.put("processing_count", rs.getInt("processing_count"));
                    metrics.put("pending_count", rs.getInt("pending_count"));
                    metrics.put("avg_order_value", rs.getDouble("avg_order_value"));
                    metrics.put("max_order_value", rs.getDouble("max_order_value"));
                    metrics.put("min_order_value", rs.getDouble("min_order_value"));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return metrics;
    }

    /**
     * Retrieves top selling products for a specific date.
     */
    public List<TopProduct> getTopSellingProducts(String date) {
        List<TopProduct> topProducts = new ArrayList<>();
        String query = "SELECT p.name, SUM(oi.quantity) as total_qty, SUM(oi.quantity * oi.price_at_order) as total_revenue " +
                       "FROM order_items oi " +
                       "JOIN products p ON oi.product_id = p.id " +
                       "JOIN orders o ON oi.order_id = o.order_id " +
                       "WHERE DATE(o.order_date) = ? " +
                       "GROUP BY p.id, p.name " +
                       "ORDER BY total_qty DESC";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setString(1, date);
            try (ResultSet rs = pstmt.executeQuery()) {
                int rank = 1;
                while (rs.next()) {
                    topProducts.add(new TopProduct(
                        rank++,
                        rs.getString("name"),
                        rs.getInt("total_qty"),
                        rs.getDouble("total_revenue")
                    ));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return topProducts;
    }

    public static class TopProduct {
        private final int rank;
        private final String name;
        private final int quantity;
        private final double revenue;

        public TopProduct(int rank, String name, int quantity, double revenue) {
            this.rank = rank;
            this.name = name;
            this.quantity = quantity;
            this.revenue = revenue;
        }

        public int getRank() { return rank; }
        public String getName() { return name; }
        public int getQuantity() { return quantity; }
        public double getRevenue() { return revenue; }
    }
}

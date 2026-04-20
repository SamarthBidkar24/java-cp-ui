import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * OrderDAO class for managing orders and order items using JDBC transactions.
 */
public class OrderDAO {
    private static String lastErrorMessage = "";
    public static String getLastErrorMessage() { return lastErrorMessage; }

    public OrderDAO() {
        checkAndCreateTables();
    }

    private void checkAndCreateTables() {
        String itemsSql = "CREATE TABLE IF NOT EXISTS order_items (" +
                     "id INT AUTO_INCREMENT PRIMARY KEY, " +
                     "order_id VARCHAR(50), " +
                     "product_id INT, " +
                     "quantity INT, " +
                     "price_at_order DOUBLE)";
        
        String ordersSql = "CREATE TABLE IF NOT EXISTS orders (" +
                     "id INT AUTO_INCREMENT PRIMARY KEY, " +
                     "customer_email VARCHAR(100), " +
                     "order_date DATETIME, " +
                     "delivery_type VARCHAR(50), " +
                     "status VARCHAR(50), " +
                     "address TEXT, " +
                     "total DOUBLE, " +
                     "payment_method VARCHAR(50), " +
                     "order_id VARCHAR(50) UNIQUE, " +
                     "items_summary TEXT, " +
                     "latitude DOUBLE, " +
                     "longitude DOUBLE, " +
                     "scheduled_delivery_date DATE)";

        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.executeUpdate(itemsSql);
            stmt.executeUpdate(ordersSql);
            
            // Ensure scheduled_delivery_date exists if table was already created
            try {
                stmt.executeUpdate("ALTER TABLE orders ADD COLUMN scheduled_delivery_date DATE");
                System.out.println("[DIAGNOSTIC] Added 'scheduled_delivery_date' column to 'orders' table.");
            } catch (SQLException e) {
                // Column likely already exists
            }
            
            System.out.println("[DIAGNOSTIC] Verified/Created order tables.");
        } catch (SQLException e) {
            System.err.println("[DIAGNOSTIC] Table creation failed: " + e.getMessage());
        }
    }

    public boolean placeOrder(Order order, List<OrderItem> items) {
        String insertOrderSQL = "INSERT INTO orders (customer_email, order_date, delivery_type, status, address, total, payment_method, order_id, items_summary, latitude, longitude, house_flat_no, street_area, landmark, city, state, pincode, full_address, geocoded_display_name, geocode_status, scheduled_delivery_date) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        String insertOrderItemSQL = "INSERT INTO order_items (order_id, product_id, quantity, price_at_order) VALUES (?, ?, ?, ?)";
        String updateProductSQL = "UPDATE products SET quantity = quantity - ? WHERE id = ? AND quantity >= ?";

        Connection conn = null;
        try {
            lastErrorMessage = "";
            System.out.println("[DIAGNOSTIC] --- Starting Order Placement ---");
            System.out.println("[DIAGNOSTIC] Customer: " + order.getCustomerEmail());
            System.out.println("[DIAGNOSTIC] Items Count: " + items.size());
            System.out.println("[DIAGNOSTIC] Address: " + order.getAddress());
            System.out.println("[DIAGNOSTIC] Total: ₹" + order.getTotal());

            conn = DBConnection.getConnection();
            if (conn == null) {
                lastErrorMessage = "Database connection failed.";
                System.err.println("[DIAGNOSTIC] DB Connection Success: FALSE");
                return false;
            }
            System.out.println("[DIAGNOSTIC] DB Connection Success: TRUE");
            conn.setAutoCommit(false);

            // 1. Validation: Check stock for all items
            for (OrderItem item : items) {
                String checkStockSQL = "SELECT name, quantity FROM products WHERE id = ?";
                try (PreparedStatement checkStmt = conn.prepareStatement(checkStockSQL)) {
                    checkStmt.setInt(1, item.getProduct().getId());
                    try (ResultSet rs = checkStmt.executeQuery()) {
                        if (rs.next()) {
                            int available = rs.getInt("quantity");
                            String pName = rs.getString("name");
                            System.out.println("[DIAGNOSTIC] Stock Check - Product: " + pName + " | Req: " + item.getQty() + " | Avail: " + available);
                            if (available < item.getQty()) {
                                lastErrorMessage = "Insufficient stock for: " + pName + " (Available: " + available + ")";
                                conn.rollback();
                                return false; 
                            }
                        } else {
                            lastErrorMessage = "Product not found: " + item.getProduct().getName();
                            conn.rollback();
                            return false;
                        }
                    }
                }
            }

            // 2. Insert into orders table
            System.out.println("[DIAGNOSTIC] Inserting Order Header (ID: " + order.getOrderId() + ")...");
            try (PreparedStatement pstmt = conn.prepareStatement(insertOrderSQL)) {
                pstmt.setString(1, order.getCustomerEmail());
                pstmt.setString(2, new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new java.util.Date()));
                pstmt.setString(3, order.getDeliveryType());
                pstmt.setString(4, "Pending");
                pstmt.setString(5, order.getAddress());
                pstmt.setDouble(6, order.getTotal());
                pstmt.setString(7, order.getPaymentMethod());
                pstmt.setString(8, order.getOrderId());

                StringBuilder summary = new StringBuilder();
                for (OrderItem item : items) {
                    if (summary.length() > 0) summary.append(", ");
                    summary.append(item.getProduct().getName()).append(" x").append(item.getQty());
                }
                pstmt.setString(9, summary.toString());
                pstmt.setDouble(10, order.getLatitude());
                pstmt.setDouble(11, order.getLongitude());
                
                pstmt.setString(12, order.getHouseFlatNo());
                pstmt.setString(13, order.getStreetArea());
                pstmt.setString(14, order.getLandmark());
                pstmt.setString(15, order.getCity());
                pstmt.setString(16, order.getState());
                pstmt.setString(17, order.getPincode());
                pstmt.setString(18, order.getFullAddress());
                pstmt.setString(19, order.getGeocodedDisplayName());
                pstmt.setString(20, order.getGeocodeStatus());
                
                // Logic for scheduled_delivery_date
                java.util.Calendar cal = java.util.Calendar.getInstance();
                java.sql.Date sqlOrderDate = new java.sql.Date(cal.getTimeInMillis());
                if ("Next Day".equalsIgnoreCase(order.getDeliveryType())) {
                    cal.add(java.util.Calendar.DAY_OF_YEAR, 1);
                }
                java.sql.Date scheduledDate = new java.sql.Date(cal.getTimeInMillis());
                pstmt.setDate(21, scheduledDate);
                order.setScheduledDeliveryDate(scheduledDate.toString());
                
                int affectedRows = pstmt.executeUpdate();
                System.out.println("[DIAGNOSTIC] Order Header Inserted. Rows: " + affectedRows);
            }

            // 3. Insert into order_items table
            System.out.println("[DIAGNOSTIC] Inserting Order Items using String ID: " + order.getOrderId());
            try (PreparedStatement itemStmt = conn.prepareStatement(insertOrderItemSQL)) {
                for (OrderItem item : items) {
                    itemStmt.setString(1, order.getOrderId()); // Link using alphanumeric String
                    itemStmt.setInt(2, item.getProduct().getId());
                    itemStmt.setInt(3, item.getQty());
                    itemStmt.setDouble(4, item.getProduct().getPrice());
                    itemStmt.addBatch();
                }
                int[] results = itemStmt.executeBatch();
                System.out.println("[DIAGNOSTIC] Items Batch Inserted. Count: " + results.length);
            }

            // 4. Update Product Inventory
            System.out.println("[DIAGNOSTIC] Decrementing Stock...");
            try (PreparedStatement stockPstmt = conn.prepareStatement(updateProductSQL)) {
                for (OrderItem item : items) {
                    stockPstmt.setInt(1, item.getQty());
                    stockPstmt.setInt(2, item.getProduct().getId());
                    stockPstmt.setInt(3, item.getQty()); 
                    int rowsUpdated = stockPstmt.executeUpdate();
                    if (rowsUpdated == 0) {
                        lastErrorMessage = "Inventory update failed for: " + item.getProduct().getName();
                        throw new SQLException("Race condition: stock became insufficient for " + item.getProduct().getName());
                    }
                }
            }

            conn.commit();
            System.out.println("[DIAGNOSTIC] Transaction COMMITTED successfully.");
            System.out.println("[SUCCESS] Order " + order.getOrderId() + " placed.");
            
            // NEW: Send notification to admins
            NotificationService.getInstance().sendToAdmins(
                "New Order Received",
                "Order " + order.getOrderId() + " has been placed by " + order.getCustomerEmail() + ". Total ₹" + order.getTotal() + ".",
                order.getOrderId()
            );
            
            return true;
        } catch (SQLException e) {
            System.err.println("[DIAGNOSTIC] TRANSACTION FAILED: " + e.getMessage());
            if (lastErrorMessage.isEmpty()) lastErrorMessage = "SQL Error: " + e.getMessage();
            if (conn != null) try { 
                conn.rollback(); 
                System.err.println("[DIAGNOSTIC] Transaction ROLLED BACK.");
            } catch (SQLException ex) {}
            e.printStackTrace();
            return false;
        } finally {
            if (conn != null) try { conn.close(); } catch (SQLException e) {}
        }
    }
    public Order getOrderById(String orderId) {
        String sql = "SELECT o.*, c.name as customer_name, c.phone as customer_phone FROM orders o JOIN customers c ON o.customer_email = c.email WHERE o.order_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, orderId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) return mapRow(rs);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public List<Order> getOrdersByCustomerEmail(String email) {
        System.out.println("[DEBUG] Fetching All Orders for customer: " + email);
        return getOrdersByQuery("SELECT o.*, c.name as customer_name, c.phone as customer_phone FROM orders o JOIN customers c ON o.customer_email = c.email WHERE o.customer_email = ? ORDER BY o.order_date DESC", email);
    }

    public List<Order> getTodaysOrdersForCustomer(String email) {
        System.out.println("[DEBUG] Fetching Today's Orders for customer: " + email);
        return getOrdersByQuery(
                "SELECT o.*, c.name as customer_name, c.phone as customer_phone FROM orders o JOIN customers c ON o.customer_email = c.email WHERE o.customer_email = ? AND DATE(o.order_date) = CURDATE() ORDER BY o.order_date DESC",
                email);
    }

    private List<Order> getOrdersByQuery(String query, String email) {
        List<Order> orders = new ArrayList<>();
        try (Connection conn = DBConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setString(1, email);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    orders.add(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("[ERROR] Failed to fetch orders for customer " + email);
            e.printStackTrace();
        }
        System.out.println("[DEBUG] Total rows fetched: " + orders.size());
        return orders;
    }

    public List<Order> getSameDayOrdersForToday() {
        System.out.println("[DEBUG] Fetching Admin Same Day Orders for Today");
        return getOrdersByCriteria("Same Day", "CURDATE()");
    }

    public List<Order> getNextDayOrdersForTomorrow() {
        System.out.println("[DEBUG] Fetching Admin Next Day Orders");
        return getOrdersByCriteria("Next Day", null);
    }

    public List<Order> getNextDayOrders() {
        // Simple query for all Next Day orders that are not yet delivered
        return getOrdersByQueryNoEmail("SELECT o.*, c.name as customer_name, c.phone as customer_phone FROM orders o JOIN customers c ON o.customer_email = c.email WHERE o.delivery_type = 'Next Day' AND o.status != 'Delivered'");
    }

    private List<Order> getOrdersByQueryNoEmail(String query) {
        List<Order> orders = new ArrayList<>();
        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            while (rs.next()) {
                orders.add(mapRow(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return orders;
    }

    public List<Order> getOrdersByCriteria(String deliveryType, String dateCondition) {
        // This is the old way, but keeping it for compatibility if needed.
        // However, we should prioritize the new batch logic.
        List<Order> orders = new ArrayList<>();
        String sql = "SELECT o.*, c.name as customer_name, c.phone as customer_phone FROM orders o JOIN customers c ON o.customer_email = c.email WHERE o.delivery_type = ?";
        if (dateCondition != null && !dateCondition.isEmpty())
            sql += " AND DATE(o.scheduled_delivery_date) = " + dateCondition;
        sql += " ORDER BY o.scheduled_delivery_date ASC, o.order_date ASC";

        try (Connection conn = DBConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, deliveryType);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    orders.add(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("[ERROR] Failed to fetch admin orders for: " + deliveryType);
            e.printStackTrace();
        }
        return orders;
    }

    public String getEarliestPendingDeliveryDate(String deliveryType) {
        String sql = "SELECT MIN(scheduled_delivery_date) FROM orders WHERE delivery_type = ? AND status != 'Delivered'";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, deliveryType);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next() && rs.getDate(1) != null) {
                    return rs.getDate(1).toString();
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public String getEarliestPendingDeliveryDateForAgent(int agentId, String deliveryType) {
        String sql = "SELECT MIN(o.scheduled_delivery_date) FROM orders o " +
                     "JOIN delivery_assignments da ON o.order_id = da.order_id " +
                     "WHERE da.agent_id = ? AND o.delivery_type = ? AND o.status != 'Delivered'";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, agentId);
            pstmt.setString(2, deliveryType);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next() && rs.getDate(1) != null) {
                    return rs.getDate(1).toString();
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public List<Order> getOrdersForRouteBatch(String deliveryType, String scheduledDate) {
        String sql = "SELECT o.*, c.name as customer_name, c.phone as customer_phone FROM orders o " +
                     "JOIN customers c ON o.customer_email = c.email " +
                     "WHERE o.delivery_type = ? AND o.scheduled_delivery_date = ? AND o.status != 'Delivered' " +
                     "ORDER BY o.order_date ASC";
        List<Order> orders = new ArrayList<>();
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, deliveryType);
            pstmt.setString(2, scheduledDate);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    orders.add(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return orders;
    }

    public List<Order> getOrdersForRouteBatchByAgent(int agentId, String deliveryType, String scheduledDate) {
        String sql = "SELECT o.*, c.name as customer_name, c.phone as customer_phone FROM orders o " +
                     "JOIN customers c ON o.customer_email = c.email " +
                     "JOIN delivery_assignments da ON o.order_id = da.order_id " +
                     "WHERE da.agent_id = ? AND o.delivery_type = ? AND o.scheduled_delivery_date = ? AND o.status != 'Delivered' " +
                     "ORDER BY da.route_sequence ASC";
        List<Order> orders = new ArrayList<>();
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, agentId);
            pstmt.setString(2, deliveryType);
            pstmt.setString(3, scheduledDate);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    orders.add(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return orders;
    }

    public boolean updateOrderStatus(String orderId, String newStatus) {
        String customerEmail = getCustomerEmailByOrderId(orderId);
        String query = "UPDATE orders SET status = ? WHERE order_id = ?";
        try (Connection conn = DBConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setString(1, newStatus);
            pstmt.setString(2, orderId);
            boolean success = pstmt.executeUpdate() == 1;
            
            if (success && customerEmail != null) {
                NotificationService.getInstance().sendToCustomer(
                    customerEmail,
                    "Order Status Updated",
                    "Your order " + orderId + " status is now " + newStatus + ".",
                    orderId
                );
            }
            return success;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public String getCustomerEmailByOrderId(String orderId) {
        String sql = "SELECT customer_email FROM orders WHERE order_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, orderId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) return rs.getString("customer_email");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public List<OrderItem> getOrderItemsByOrderId(String orderId) {
        System.out.println("[DEBUG_DAO] Fetching items for Order ID: " + orderId);
        List<OrderItem> items = new ArrayList<>();
        // Simplify: Search directly in order_items using the alphanumeric order_id
        String query = "SELECT p.*, oi.quantity, oi.price_at_order FROM order_items oi " +
                       "JOIN products p ON oi.product_id = p.id " +
                       "WHERE oi.order_id = ?";
        try (Connection conn = DBConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setString(1, orderId);
            try (ResultSet rs = pstmt.executeQuery()) {
                int count = 0;
                while (rs.next()) {
                    count++;
                    Product p = new Product();
                    p.setId(rs.getInt("id"));
                    p.setName(rs.getString("name"));
                    p.setPrice(rs.getDouble("price"));
                    p.setCategory(rs.getString("category"));
                    
                    double priceAtOrder = rs.getDouble("price_at_order");
                    int qty = rs.getInt("quantity");
                    
                    if (count == 1) {
                        System.out.println("[DEBUG_DAO] First item found: " + p.getName() + ", Qty: " + qty);
                    }
                    
                    items.add(new OrderItem(0, orderId, p, qty, priceAtOrder));
                }
                System.out.println("[DEBUG_DAO] Total items fetched from DB: " + count);
            }
        } catch (SQLException e) {
            System.err.println("[DEBUG_DAO] SQL Error fetching items: " + e.getMessage());
            e.printStackTrace();
        }
        return items;
    }

    private Order mapRow(ResultSet rs) throws SQLException {
        String oid = rs.getString("order_id");
        Order o = new Order();
        o.setOrderId(oid);
        o.setCustomerEmail(rs.getString("customer_email"));
        o.setDate(rs.getString("order_date"));
        o.setStatus(rs.getString("status"));
        o.setTotal(rs.getDouble("total"));
        o.setDeliveryType(rs.getString("delivery_type"));
        o.setAddress(rs.getString("address"));
        o.setPaymentMethod(rs.getString("payment_method"));

        // New Requirement: Fetch Name and Phone from Join
        try {
            o.setCustomerName(rs.getString("customer_name"));
            o.setCustomerPhone(rs.getString("customer_phone"));
        } catch (SQLException e) {
            // Some queries might not have the join
        }

        // Pre-fetch items summary for table display efficiency
        o.setItemsSummary(rs.getString("items_summary"));
        o.setLatitude(rs.getDouble("latitude"));
        o.setLongitude(rs.getDouble("longitude"));
        
        // Load new fields if they exist (using column labels)
        try {
            o.setHouseFlatNo(rs.getString("house_flat_no"));
            o.setStreetArea(rs.getString("street_area"));
            o.setLandmark(rs.getString("landmark"));
            o.setCity(rs.getString("city"));
            o.setState(rs.getString("state"));
            o.setPincode(rs.getString("pincode"));
            o.setFullAddress(rs.getString("full_address"));
            o.setGeocodedDisplayName(rs.getString("geocoded_display_name"));
            o.setGeocodeStatus(rs.getString("geocode_status"));
        } catch (SQLException e) {}

        try {
            Date sDate = rs.getDate("scheduled_delivery_date");
            if (sDate != null) o.setScheduledDeliveryDate(sDate.toString());
        } catch (SQLException e) {}
        
        return o;
    }

}

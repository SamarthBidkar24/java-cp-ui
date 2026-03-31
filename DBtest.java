import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

public class DBtest {
    public static void main(String[] args) {

        // --- Connection Test ---
        try {
            Connection con = DBConnection.getConnection();
            if (con != null) {
                System.out.println("✅ DB Connection successful!");
                con.close();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        // --- CustomerDAO Test ---
        System.out.println("\n--- Testing CustomerDAO ---");
        CustomerDAO customerDAO = new CustomerDAO();
        if (customerDAO.emailExists("customer@test.com")) {
            System.out.println("ℹ️  Email already exists, skipping registration.");
        } else {
            boolean registered = customerDAO.registerCustomer("Test User", "customer@test.com", "9876543210", "123456");
            System.out.println(registered ? "✅ Registration successful!" : "❌ Registration failed.");
        }
        Customer loggedIn = customerDAO.login("customer@test.com", "123456");
        System.out.println(loggedIn != null ? "✅ Login successful! Welcome: " + loggedIn.getName() : "❌ Login failed.");

        // --- AdminDAO Test ---
        System.out.println("\n--- Testing AdminDAO ---");
        AdminDAO adminDAO = new AdminDAO();
        List<Product> products = adminDAO.getAllProducts();
        System.out.println("✅ Total products in DB: " + products.size());
        for (Product p : products) {
            System.out.println("   - " + p.getId() + " | " + p.getName() + " | ₹" + p.getPrice());
        }

        // --- OrderDAO Test ---
        System.out.println("\n--- Testing OrderDAO ---");
        OrderDAO orderDAO = new OrderDAO();

        Order testOrder = new Order(
                "ORD-TEST001",
                "2026-03-31",
                "Pending",
                259.99,
                "Same Day",
                "123 Test Street, Pune",
                "UPI",
                "Test Apple (x2)",
                "customer@test.com");

        boolean placed = orderDAO.placeOrder(testOrder);
        System.out.println(placed ? "✅ Order placed!" : "❌ Order placement failed.");

        List<Order> myOrders = orderDAO.getOrdersByCustomer("customer@test.com");
        System.out.println("✅ Orders for customer: " + myOrders.size());
        for (Order o : myOrders) {
            System.out.println("   - " + o.getOrderId() + " | " + o.getStatus() + " | ₹" + o.getTotal());
        }

        boolean statusUpdated = orderDAO.updateOrderStatus("ORD-TEST001", "Delivered");
        System.out.println(statusUpdated ? "✅ Status updated to Delivered!" : "❌ Status update failed.");
    }
}
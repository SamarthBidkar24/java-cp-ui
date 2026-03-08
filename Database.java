import java.util.ArrayList;
import java.util.List;

public class Database {
    public static ArrayList<Product> products = new ArrayList<>();
    public static ArrayList<Customer> customers = new ArrayList<>();
    public static ArrayList<Order> orders = new ArrayList<>();

    public static int nextProductId = 1;
    public static int nextOrderId = 1;

    public static class Product {
        private int id;
        private String name, category;
        private double price;
        private int quantity;
        private String imagePath = "";

        public Product(int id, String name, int quantity, double price, String category, String imagePath) {
            this.id = id;
            this.name = name;
            this.quantity = quantity;
            this.price = price;
            this.category = category;
            this.imagePath = imagePath;
        }

        public int getId() { return id; }
        public void setId(int id) { this.id = id; }
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public String getCategory() { return category; }
        public void setCategory(String category) { this.category = category; }
        public double getPrice() { return price; }
        public void setPrice(double price) { this.price = price; }
        public int getQuantity() { return quantity; }
        public void setQuantity(int quantity) { this.quantity = quantity; }
        public String getImagePath() { return imagePath; }
        public void setImagePath(String imagePath) { this.imagePath = imagePath; }
    }

    public static class Customer {
        private String name, email, phone, password;

        public Customer(String name, String email, String phone, String password) {
            this.name = name;
            this.email = email;
            this.phone = phone;
            this.password = password;
        }

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }
        public String getPhone() { return phone; }
        public void setPhone(String phone) { this.phone = phone; }
        public String getPassword() { return password; }
        public void setPassword(String password) { this.password = password; }
    }

    public static class OrderItem {
        public Product product;
        public int qty;
        public OrderItem(Product product, int qty) { 
            this.product = product; 
            this.qty = qty; 
        }
    }

    public static class Order {
        private String orderId;
        private String customerEmail, date, deliveryType, status, address;
        private double total;
        private List<OrderItem> items;
        private String paymentMethod;
        private String itemsSummary;
        
        public Order(String orderId, String date, String status, double total, 
                     String deliveryType, String address, String paymentMethod, String itemsSummary, String customerEmail) {
            this.orderId = orderId;
            this.date = date;
            this.status = status;
            this.total = total;
            this.deliveryType = deliveryType;
            this.address = address;
            this.paymentMethod = paymentMethod;
            this.itemsSummary = itemsSummary;
            this.customerEmail = customerEmail;
        }

        public String getOrderId() { return orderId; }
        public String getCustomerEmail() { return customerEmail; }
        public String getDate() { return date; }
        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
        public double getTotal() { return total; }
        public String getDeliveryType() { return deliveryType; }
        public String getAddress() { return address; }
        public String getPaymentMethod() { return paymentMethod; }
        
        public String getItemsSummary() { return itemsSummary; }
    }
}

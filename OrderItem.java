/**
 * OrderItem model representing an item in an order.
 */
public class OrderItem {
    private int id;
    private String orderId;
    public Product product;
    public int qty;
    private double priceAtOrder;

    public OrderItem(Product product, int qty) {
        this.product = product;
        this.qty = qty;
        this.priceAtOrder = product != null ? product.getPrice() : 0.0;
    }

    public OrderItem(int id, String orderId, Product product, int qty, double priceAtOrder) {
        this.id = id;
        this.orderId = orderId;
        this.product = product;
        this.qty = qty;
        this.priceAtOrder = priceAtOrder;
    }

    public Product getProduct() { return product; }
    public int getQty() { return qty; }
    public double getPriceAtOrder() { return priceAtOrder; }
    public String getOrderId() { return orderId; }
    public void setOrderId(String orderId) { this.orderId = orderId; }
}

/**
 * OrderItem model representing an item in an order.
 */
public class OrderItem {
    public Product product;
    public int qty;

    public OrderItem(Product product, int qty) {
        this.product = product;
        this.qty = qty;
    }
}

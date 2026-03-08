public class Order {
    private String orderId;
    private String date;
    private String status;
    private double total;
    private String deliveryType;
    private String address;
    private String paymentMethod;
    private String itemsSummary;
    private String customerEmail;

    public Order(String orderId, String date, String status, double total, String deliveryType, String address,
            String paymentMethod, String itemsSummary, String customerEmail) {
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

    public String getCustomerEmail() {
        return customerEmail;
    }

    public String getOrderId() {
        return orderId;
    }

    public String getDate() {
        return date;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public double getTotal() {
        return total;
    }

    public String getDeliveryType() {
        return deliveryType;
    }

    public String getAddress() {
        return address;
    }

    public String getPaymentMethod() {
        return paymentMethod;
    }

    public String getItemsSummary() {
        return itemsSummary;
    }
}

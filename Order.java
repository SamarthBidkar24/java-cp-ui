/**
 * Order model representing an order in the database.
 */
public class Order {
    private String id; // Changed to String to handle "ORD-..."
    private String orderId; // Display ID
    private int customerId;
    private String customerEmail;
    private String date;
    private String status;
    private double total;
    private String deliveryType;
    private String address;
    private String houseFlatNo;
    private String streetArea;
    private String landmark;
    private String city;
    private String state;
    private String pincode;
    private String fullAddress;
    private String geocodedDisplayName;
    private String geocodeStatus;
    private String paymentMethod;
    private String itemsSummary;
    private double latitude;
    private double longitude;
    private String customerPhone;
    private String customerName;
    private String scheduledDeliveryDate;

    // Default constructor for UI
    public Order() {
    }

    // Constructor for Checkout
    public Order(int customerId, String orderId, String date, String status, double total, String deliveryType,
            String address, String paymentMethod) {
        this.customerId = customerId;
        this.orderId = orderId;
        this.date = date;
        this.status = status;
        this.total = total;
        this.deliveryType = deliveryType;
        this.address = address;
        this.paymentMethod = paymentMethod;
    }

    // Getters/Setters
    public String getId() {
        return id != null ? id : orderId;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    public int getCustomerId() {
        return customerId;
    }

    public void setCustomerId(int customerId) {
        this.customerId = customerId;
    }

    public String getCustomerEmail() {
        return customerEmail;
    }

    public void setCustomerEmail(String customerEmail) {
        this.customerEmail = customerEmail;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
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

    public void setTotal(double total) {
        this.total = total;
    }

    public String getDeliveryType() {
        return deliveryType;
    }

    public void setDeliveryType(String deliveryType) {
        this.deliveryType = deliveryType;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getHouseFlatNo() { return houseFlatNo; }
    public void setHouseFlatNo(String houseFlatNo) { this.houseFlatNo = houseFlatNo; }

    public String getStreetArea() { return streetArea; }
    public void setStreetArea(String streetArea) { this.streetArea = streetArea; }

    public String getLandmark() { return landmark; }
    public void setLandmark(String landmark) { this.landmark = landmark; }

    public String getCity() { return city; }
    public void setCity(String city) { this.city = city; }

    public String getState() { return state; }
    public void setState(String state) { this.state = state; }

    public String getPincode() { return pincode; }
    public void setPincode(String pincode) { this.pincode = pincode; }

    public String getFullAddress() { return fullAddress; }
    public void setFullAddress(String fullAddress) { this.fullAddress = fullAddress; }

    public String getGeocodedDisplayName() { return geocodedDisplayName; }
    public void setGeocodedDisplayName(String geocodedDisplayName) { this.geocodedDisplayName = geocodedDisplayName; }

    public String getGeocodeStatus() { return geocodeStatus; }
    public void setGeocodeStatus(String geocodeStatus) { this.geocodeStatus = geocodeStatus; }

    public String getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(String paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    public String getItemsSummary() { return itemsSummary; }
    public void setItemsSummary(String itemsSummary) { this.itemsSummary = itemsSummary; }

    public double getLatitude() { return latitude; }
    public void setLatitude(double latitude) { this.latitude = latitude; }

    public double getLongitude() { return longitude; }
    public void setLongitude(double longitude) { this.longitude = longitude; }

    public String getCustomerPhone() { return customerPhone; }
    public void setCustomerPhone(String customerPhone) { this.customerPhone = customerPhone; }

    public String getCustomerName() { return customerName; }
    public void setCustomerName(String customerName) { this.customerName = customerName; }

    public String getScheduledDeliveryDate() { return scheduledDeliveryDate; }
    public void setScheduledDeliveryDate(String scheduledDeliveryDate) { this.scheduledDeliveryDate = scheduledDeliveryDate; }
}

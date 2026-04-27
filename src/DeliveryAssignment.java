/**
 * DeliveryAssignment model representing an order assigned to a delivery agent.
 */
public class DeliveryAssignment {
    private int assignmentId;
    private String orderId;
    private int agentId;
    private String deliveryType; // SAME_DAY / NEXT_DAY
    private int routeSequence;
    private int etaMinutes;
    private String assignedAt;
    private String status; // Pending / Out for Delivery / Delivered

    // Extra fields for UI convenience
    private String customerName;
    private String customerAddress;
    private String pincode;
    private String orderStatus;

    public DeliveryAssignment() {}

    public DeliveryAssignment(int assignmentId, String orderId, int agentId, String deliveryType, int routeSequence, int etaMinutes, String assignedAt, String status) {
        this.assignmentId = assignmentId;
        this.orderId = orderId;
        this.agentId = agentId;
        this.deliveryType = deliveryType;
        this.routeSequence = routeSequence;
        this.etaMinutes = etaMinutes;
        this.assignedAt = assignedAt;
        this.status = status;
    }

    // Getters and Setters
    public int getAssignmentId() { return assignmentId; }
    public void setAssignmentId(int assignmentId) { this.assignmentId = assignmentId; }

    public String getOrderId() { return orderId; }
    public void setOrderId(String orderId) { this.orderId = orderId; }

    public int getAgentId() { return agentId; }
    public void setAgentId(int agentId) { this.agentId = agentId; }

    public String getDeliveryType() { return deliveryType; }
    public void setDeliveryType(String deliveryType) { this.deliveryType = deliveryType; }

    public int getRouteSequence() { return routeSequence; }
    public void setRouteSequence(int routeSequence) { this.routeSequence = routeSequence; }

    public int getEtaMinutes() { return etaMinutes; }
    public void setEtaMinutes(int etaMinutes) { this.etaMinutes = etaMinutes; }

    public String getAssignedAt() { return assignedAt; }
    public void setAssignedAt(String assignedAt) { this.assignedAt = assignedAt; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getCustomerName() { return customerName; }
    public void setCustomerName(String customerName) { this.customerName = customerName; }

    public String getCustomerAddress() { return customerAddress; }
    public void setCustomerAddress(String customerAddress) { this.customerAddress = customerAddress; }

    public String getPincode() { return pincode; }
    public void setPincode(String pincode) { this.pincode = pincode; }

    public String getOrderStatus() { return orderStatus; }
    public void setOrderStatus(String orderStatus) { this.orderStatus = orderStatus; }
}

/**
 * DeliveryAgent model representing a delivery personnel.
 */
public class DeliveryAgent {
    private int agentId;
    private String fullName;
    private String email;
    private String phone;
    private String password;
    private String vehicleType;
    private String assignedArea;
    private boolean isActive;
    private String createdAt;
    private String approvalStatus;

    public DeliveryAgent() {}

    public DeliveryAgent(int agentId, String fullName, String email, String phone, String password, String vehicleType, String assignedArea, boolean isActive, String createdAt) {
        this.agentId = agentId;
        this.fullName = fullName;
        this.email = email;
        this.phone = phone;
        this.password = password;
        this.vehicleType = vehicleType;
        this.assignedArea = assignedArea;
        this.isActive = isActive;
        this.createdAt = createdAt;
    }

    // Getters and Setters
    public int getAgentId() { return agentId; }
    public void setAgentId(int agentId) { this.agentId = agentId; }

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getVehicleType() { return vehicleType; }
    public void setVehicleType(String vehicleType) { this.vehicleType = vehicleType; }

    public String getAssignedArea() { return assignedArea; }
    public void setAssignedArea(String assignedArea) { this.assignedArea = assignedArea; }

    public boolean isActive() { return isActive; }
    public void setActive(boolean active) { isActive = active; }

    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }

    public String getApprovalStatus() { return approvalStatus; }
    public void setApprovalStatus(String approvalStatus) { this.approvalStatus = approvalStatus; }

    @Override
    public String toString() {
        return fullName + " (" + assignedArea + ")";
    }
}

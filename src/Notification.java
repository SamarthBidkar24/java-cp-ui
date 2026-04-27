import java.time.LocalDateTime;

/**
 * Notification model / DTO.
 */
public class Notification {
    public enum UserType { ADMIN, CUSTOMER, DELIVERY_GUY }

    private int notificationId;
    private UserType userType;
    private String targetUser; // email for customer, "admin" or null for admin
    private String title;
    private String message;
    private String relatedOrderId;
    private String createdAt;
    private boolean isRead;

    public Notification() {}

    public Notification(int notificationId, UserType userType, String targetUser, String title, String message, String relatedOrderId, String createdAt, boolean isRead) {
        this.notificationId = notificationId;
        this.userType = userType;
        this.targetUser = targetUser;
        this.title = title;
        this.message = message;
        this.relatedOrderId = relatedOrderId;
        this.createdAt = createdAt;
        this.isRead = isRead;
    }

    // Getters and Setters
    public int getNotificationId() { return notificationId; }
    public void setNotificationId(int notificationId) { this.notificationId = notificationId; }

    public UserType getUserType() { return userType; }
    public void setUserType(UserType userType) { this.userType = userType; }

    public String getTargetUser() { return targetUser; }
    public void setTargetUser(String targetUser) { this.targetUser = targetUser; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public String getRelatedOrderId() { return relatedOrderId; }
    public void setRelatedOrderId(String relatedOrderId) { this.relatedOrderId = relatedOrderId; }

    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }

    public boolean isRead() { return isRead; }
    public void setRead(boolean read) { isRead = read; }
}

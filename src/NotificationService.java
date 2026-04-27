import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * Central NotificationService / EventBus for JavaFX loosely coupled communication.
 */
public class NotificationService {
    private static NotificationService instance;
    private final NotificationDAO dao = new NotificationDAO();
    private final List<Consumer<Notification>> listeners = new ArrayList<>();

    private NotificationService() {}

    public static synchronized NotificationService getInstance() {
        if (instance == null) {
            instance = new NotificationService();
        }
        return instance;
    }

    public void sendToAdmins(String title, String message, String orderId) {
        Notification n = new Notification(0, Notification.UserType.ADMIN, "admin", title, message, orderId, null, false);
        if (dao.addNotification(n)) {
            publish(n);
        }
    }

    public void sendToCustomer(String customerEmail, String title, String message, String orderId) {
        Notification n = new Notification(0, Notification.UserType.CUSTOMER, customerEmail, title, message, orderId, null, false);
        if (dao.addNotification(n)) {
            publish(n);
        }
    }

    public void sendToDeliveryGuy(String agentEmail, String title, String message, String orderId) {
        Notification n = new Notification(0, Notification.UserType.DELIVERY_GUY, agentEmail, title, message, orderId, null, false);
        if (dao.addNotification(n)) {
            publish(n);
        }
    }

    public List<Notification> getAdminNotifications() {
        return dao.getNotificationsForUser(Notification.UserType.ADMIN, "admin");
    }

    public List<Notification> getCustomerNotifications(String customerEmail) {
        return dao.getNotificationsForUser(Notification.UserType.CUSTOMER, customerEmail);
    }

    public List<Notification> getNotificationsForUser(Notification.UserType type, String targetUser) {
        return dao.getNotificationsForUser(type, targetUser);
    }

    public void markAsRead(int notificationId) {
        dao.markAsRead(notificationId);
    }

    public void markAllAsRead(Notification.UserType type, String targetUser) {
        dao.markAllAsRead(type, targetUser);
    }

    public void subscribe(Consumer<Notification> listener) {
        listeners.add(listener);
    }

    public void unsubscribe(Consumer<Notification> listener) {
        listeners.remove(listener);
    }

    private void publish(Notification n) {
        Platform.runLater(() -> {
            for (Consumer<Notification> listener : listeners) {
                listener.accept(n);
            }
        });
    }
}

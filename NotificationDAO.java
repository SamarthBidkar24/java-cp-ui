import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * NotificationDAO for managing notifications in the database.
 */
public class NotificationDAO {

    public NotificationDAO() {
        checkAndCreateTables();
    }

    private void checkAndCreateTables() {
        String sql = "CREATE TABLE IF NOT EXISTS notifications (" +
                     "notification_id INT AUTO_INCREMENT PRIMARY KEY, " +
                     "target_type ENUM('ADMIN', 'CUSTOMER'), " +
                     "target_user VARCHAR(100), " +
                     "title VARCHAR(200), " +
                     "message TEXT, " +
                     "order_id VARCHAR(50), " +
                     "is_read BOOLEAN DEFAULT FALSE, " +
                     "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP)";
        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.executeUpdate(sql);
            System.out.println("[DIAGNOSTIC] Verified/Created 'notifications' table.");
        } catch (SQLException e) {
            System.err.println("[DIAGNOSTIC] Notification table creation failed: " + e.getMessage());
        }
    }

    public boolean addNotification(Notification n) {
        String sql = "INSERT INTO notifications (target_type, target_user, title, message, order_id) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setString(1, n.getUserType().name());
            pstmt.setString(2, n.getTargetUser());
            pstmt.setString(3, n.getTitle());
            pstmt.setString(4, n.getMessage());
            pstmt.setString(5, n.getRelatedOrderId());
            
            int affectedRows = pstmt.executeUpdate();
            if (affectedRows > 0) {
                try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        n.setNotificationId(generatedKeys.getInt(1));
                    }
                }
                return true;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public List<Notification> getNotificationsForUser(Notification.UserType type, String targetUser) {
        List<Notification> list = new ArrayList<>();
        String sql = "SELECT * FROM notifications WHERE target_type = ? " +
                     (targetUser != null ? "AND target_user = ? " : "") +
                     "ORDER BY created_at DESC";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, type.name());
            if (targetUser != null) {
                pstmt.setString(2, targetUser);
            }
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public boolean markAsRead(int notificationId) {
        String sql = "UPDATE notifications SET is_read = TRUE WHERE notification_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, notificationId);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean markAllAsRead(Notification.UserType type, String targetUser) {
        String sql = "UPDATE notifications SET is_read = TRUE WHERE target_type = ? " +
                     (targetUser != null ? "AND target_user = ? " : "");
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, type.name());
            if (targetUser != null) {
                pstmt.setString(2, targetUser);
            }
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    private Notification mapRow(ResultSet rs) throws SQLException {
        Notification n = new Notification();
        n.setNotificationId(rs.getInt("notification_id"));
        n.setUserType(Notification.UserType.valueOf(rs.getString("target_type")));
        n.setTargetUser(rs.getString("target_user"));
        n.setTitle(rs.getString("title"));
        n.setMessage(rs.getString("message"));
        n.setRelatedOrderId(rs.getString("order_id"));
        n.setRead(rs.getBoolean("is_read"));
        n.setCreatedAt(rs.getString("created_at"));
        return n;
    }
}

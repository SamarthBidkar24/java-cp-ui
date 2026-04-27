import java.sql.*;

public class ForceMigration {
    public static void main(String[] args) {
        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement()) {
            
            System.out.println("Migrating ADMINS...");
            addCol(stmt, "admins", "password_salt", "VARCHAR(100)");
            addCol(stmt, "admins", "role", "VARCHAR(20) DEFAULT 'ADMIN'");
            addCol(stmt, "admins", "is_active", "BOOLEAN DEFAULT TRUE");
            
            System.out.println("Migrating CUSTOMERS...");
            addCol(stmt, "customers", "password_salt", "VARCHAR(100)");
            addCol(stmt, "customers", "role", "VARCHAR(20) DEFAULT 'CUSTOMER'");
            addCol(stmt, "customers", "is_verified", "BOOLEAN DEFAULT FALSE");
            addCol(stmt, "customers", "status", "VARCHAR(20) DEFAULT 'ACTIVE'");
            
            System.out.println("Migrating DELIVERY_AGENTS...");
            addCol(stmt, "delivery_agents", "password_salt", "VARCHAR(100)");
            addCol(stmt, "delivery_agents", "role", "VARCHAR(20) DEFAULT 'DELIVERY'");
            addCol(stmt, "delivery_agents", "approval_status", "VARCHAR(20) DEFAULT 'PENDING'");
            
            System.out.println("Updating legacy admin...");
            String salt = SecurityUtils.generateSalt();
            String hash = SecurityUtils.hashPassword("admin123", salt);
            stmt.executeUpdate("UPDATE admins SET password='" + hash + "', password_salt='" + salt + "', role='ADMIN', is_active=TRUE WHERE email='admin@supermart.com'");
            
            System.out.println("Migration Complete.");
            
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void addCol(Statement stmt, String table, String col, String type) {
        try {
            stmt.executeUpdate("ALTER TABLE " + table + " ADD COLUMN " + col + " " + type);
            System.out.println("Added " + col + " to " + table);
        } catch (SQLException e) {
            if (e.getErrorCode() == 1060) {
                System.out.println(col + " already exists in " + table);
            } else {
                System.err.println("Error adding " + col + ": " + e.getMessage());
            }
        }
    }
}

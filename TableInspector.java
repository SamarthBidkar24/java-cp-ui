import java.sql.*;

public class TableInspector {
    public static void main(String[] args) {
        try (Connection conn = DBConnection.getConnection()) {
            System.out.println("--- ADMINS SCHEMA ---");
            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery("DESCRIBE admins")) {
                while (rs.next()) {
                    System.out.println("Field: " + rs.getString("Field") + " | Type: " + rs.getString("Type"));
                }
            }
            
            System.out.println("\n--- ADMINS DATA ---");
            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery("SELECT * FROM admins")) {
                ResultSetMetaData md = rs.getMetaData();
                int cols = md.getColumnCount();
                while (rs.next()) {
                    for (int i = 1; i <= cols; i++) {
                        System.out.print(md.getColumnName(i) + ": " + rs.getString(i) + " | ");
                    }
                    System.out.println();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

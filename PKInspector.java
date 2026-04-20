import java.sql.*;

public class PKInspector {
    public static void main(String[] args) {
        try (Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/supermart_oop", "root", "S@m242005")) {
            DatabaseMetaData md = conn.getMetaData();
            ResultSet rs = md.getPrimaryKeys(null, null, "orders");
            System.out.println("--- PRIMARY KEYS IN orders ---");
            while (rs.next()) {
                System.out.println("Column: " + rs.getString("COLUMN_NAME"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}

import java.sql.*;

public class ColumnInspector {
    public static void main(String[] args) {
        try (Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/supermart_oop", "root", "S@m242005")) {
            DatabaseMetaData md = conn.getMetaData();
            ResultSet rs = md.getColumns(null, null, "%", "%");
            System.out.println("--- SEARCHING FOR TABLES WITH order_id ---");
            while (rs.next()) {
                String tableName = rs.getString("TABLE_NAME");
                String columnName = rs.getString("COLUMN_NAME");
                if (columnName.toLowerCase().contains("order_id") || columnName.toLowerCase().contains("product_id")) {
                    System.out.println("Table: " + tableName + " | Column: " + columnName);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}

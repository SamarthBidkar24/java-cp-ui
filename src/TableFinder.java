import java.sql.*;

public class TableFinder {
    public static void main(String[] args) {
        try (Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/supermart_oop", "root", "S@m242005")) {
            DatabaseMetaData md = conn.getMetaData();
            ResultSet rs = md.getColumns(null, null, "%", "%price%");
            System.out.println("--- TABLES WITH price COLUMN ---");
            while (rs.next()) {
                System.out.println("Table: " + rs.getString("TABLE_NAME") + " | Column: " + rs.getString("COLUMN_NAME"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}

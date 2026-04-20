import java.sql.*;

public class TableDescriptor {
    public static void main(String[] args) {
        try (Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/supermart_oop", "root", "S@m242005")) {
            DatabaseMetaData md = conn.getMetaData();
            ResultSet rs = md.getColumns(null, null, "payment", "%");
            System.out.println("--- COLUMNS IN payment TABLE ---");
            while (rs.next()) {
                System.out.println(rs.getString("COLUMN_NAME") + " (" + rs.getString("TYPE_NAME") + ")");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}

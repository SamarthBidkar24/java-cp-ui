import java.sql.*;

public class TableLister {
    public static void main(String[] args) {
        try (Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/supermart_oop", "root", "S@m242005")) {
            DatabaseMetaData md = conn.getMetaData();
            ResultSet rs = md.getTables(null, null, "%", new String[]{"TABLE"});
            System.out.println("--- TABLES IN DATABASE ---");
            while (rs.next()) {
                System.out.println(rs.getString(3));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}

import java.sql.*;

public class GlobalTableFinder {
    public static void main(String[] args) {
        try (Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/", "root", "S@m242005")) {
            DatabaseMetaData md = conn.getMetaData();
            ResultSet rs = md.getTables(null, null, "%order%", null);
            System.out.println("--- GLOBAL TABLE SEARCH FOR 'order' ---");
            while (rs.next()) {
                System.out.println("DB: " + rs.getString(1) + " | Table: " + rs.getString(3));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}

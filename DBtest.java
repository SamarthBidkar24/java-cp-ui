import java.sql.Connection;
import java.sql.SQLException;

public class DBtest {
    public static void main(String[] args) {
        try {
            Connection con = DBConnection.getConnection();
            if (con != null) {
                System.out.println("Connection successful!");
                con.close();
            } else {
                System.out.println("Connection is null.");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
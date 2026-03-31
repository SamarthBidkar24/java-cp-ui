import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import javax.swing.JOptionPane;

/**
 * DBConnection utility class to handle MySQL database connectivity.
 */
public class DBConnection {
    // Database connection details
    private static final String URL = "jdbc:mysql://localhost:3306/supermart_oop";
    private static final String USER = "root";
    private static final String PASSWORD = "S@m242005";
    private static final String DRIVER = "com.mysql.cj.jdbc.Driver";

    private static Connection connection = null;

    /**
     * Establishes and returns a connection to the MySQL database.
     * 
     * @return java.sql.Connection object
     */
    public static Connection getConnection() {
        try {
            // Load the MySQL JDBC driver
            Class.forName(DRIVER);

            // Establish the connection
            connection = DriverManager.getConnection(URL, USER, PASSWORD);

        } catch (ClassNotFoundException e) {
            JOptionPane.showMessageDialog(null,
                    "MySQL JDBC Driver not found: " + e.getMessage(),
                    "Database Connection Error",
                    JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null,
                    "Failed to connect to the database: " + e.getMessage(),
                    "Database Connection Error",
                    JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }

        return connection;
    }
}

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * DBConnection utility class to handle MySQL database connectivity.
 */
public class DBConnection {
    // Database connection details
    private static final String URL = "jdbc:mysql://localhost:3306/supermart_oop";
    private static final String USER = "root";
    private static final String PASSWORD = "S@m242005"; // Your MySQL password
    private static final String DRIVER = "com.mysql.cj.jdbc.Driver";

    /**
     * Establishes and returns a connection to the MySQL database.
     * 
     * @return java.sql.Connection object or null if connection fails
     */
    public static Connection getConnection() {
        Connection connection = null;
        try {
            // Load the MySQL JDBC driver
            Class.forName(DRIVER);

            // Establish the connection
            connection = DriverManager.getConnection(URL, USER, PASSWORD);
            
        } catch (ClassNotFoundException e) {
            System.err.println("JDBC Driver not found: " + e.getMessage());
        } catch (SQLException e) {
            System.err.println("Failed to connect to the database: " + e.getMessage());
        }
        return connection;
    }
}



import java.sql.*;

/**
 * Database Configuration and Connection Manager for EduTrack
 */
public class DatabaseConfig {
    
    // Database credentials - Update these according to your PostgreSQL setup
    private static final String DB_URL = "jdbc:postgresql://localhost:5432/postgres";
    private static final String DB_USER = "postgres";
    private static final String DB_PASSWORD = "1234";
    
    // Connection pool
    private static Connection connection = null;
    
    /**
     * Get database connection
     */
    public static Connection getConnection() throws SQLException {
        if (connection == null || connection.isClosed()) {
            try {
                // Load PostgreSQL JDBC Driver
                Class.forName("org.postgresql.Driver");
                connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
                System.out.println("Database connected successfully!");
            } catch (ClassNotFoundException e) {
                System.err.println("PostgreSQL JDBC Driver not found!");
                e.printStackTrace();
                throw new SQLException("Driver not found", e);
            }
        }
        return connection;
    }
    
    /**
     * Close database connection
     */
    public static void closeConnection() {
        if (connection != null) {
            try {
                connection.close();
                System.out.println("Database connection closed.");
            } catch (SQLException e) {
                System.err.println("Error closing database connection!");
                e.printStackTrace();
            }
        }
    }
    
    /**
     * Test database connection
     */
    public static boolean testConnection() {
        try {
            Connection conn = getConnection();
            return conn != null && !conn.isClosed();
        } catch (SQLException e) {
            System.err.println("Database connection test failed!");
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * Execute a query and return ResultSet
     */
    public static ResultSet executeQuery(String query) throws SQLException {
        Connection conn = getConnection();
        Statement stmt = conn.createStatement();
        return stmt.executeQuery(query);
    }
    
    /**
     * Execute an update query (INSERT, UPDATE, DELETE)
     */
    public static int executeUpdate(String query) throws SQLException {
        Connection conn = getConnection();
        Statement stmt = conn.createStatement();
        return stmt.executeUpdate(query);
    }
    
    /**
     * Create prepared statement
     */
    public static PreparedStatement prepareStatement(String query) throws SQLException {
        Connection conn = getConnection();
        return conn.prepareStatement(query);
    }
    
    /**
     * Close resources safely
     */
    public static void closeResources(ResultSet rs, Statement stmt) {
        try {
            if (rs != null) rs.close();
            if (stmt != null) stmt.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
        // --- TEMP TEST MAIN ---
    public static void main(String[] args) {
        if (DatabaseConfig.testConnection()) {
            System.out.println("✅ Connection test successful!");
        } else {
            System.out.println("❌ Connection test failed!");
        }
        DatabaseConfig.closeConnection();
    }

}
package database;

import java.sql.Connection;
import java.sql.Statement;

public class DBManager {

    private static final String DB_URL = System.getenv("DB_URL");
    private static final String DB_USER = System.getenv("DB_USER");
    private static final String DB_PASSWORD = System.getenv("DB_PASSWORD");

    private static Connection connection = null;

    public static void initializeConnection() throws Exception {
        if (connection != null && !connection.isClosed()) {
            return;
        }
        try {
            // Load the JDBC driver
            Class.forName("com.mysql.cj.jdbc.Driver");

            // Establish the connection
            connection = java.sql.DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);

            createTables();
            System.out.println("Database connection established");

        } catch (Exception e) {
            System.err.println("Error establishing database connection: " + e.getMessage());
        }
    }

    public static Connection getConnection() throws Exception{
        if (connection == null || connection.isClosed()) {
            throw new Exception("Database connection is not initialized or closed.");
        }
        return connection;
    }

    public static void closeConnection() throws Exception {
        if (connection != null && !connection.isClosed())
            try {
                connection.close();
                System.out.println("Database connection closed");
            } catch (Exception e) {
                System.err.println("Error closing database connection: " + e.getMessage());
            }
    }

    private static void createTables() {
        try {
            Statement st = connection.createStatement();

            st.executeUpdate("""
                CREATE TABLE IF NOT EXISTS users (
                    user_id VARCHAR(36) PRIMARY KEY,
                    username VARCHAR(50) UNIQUE NOT NULL,
                    balance DECIMAL(19,2) NOT NULL DEFAULT 0.00,
                    currency VARCHAR(3) NOT NULL DEFAULT 'USD',
                    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
                )
            """);

            st.executeUpdate("""
                CREATE TABLE IF NOT EXISTS transactions (
                    transaction_id VARCHAR(36) PRIMARY KEY,
                    account_id VARCHAR(36) NOT NULL,
                    amount DECIMAL(19,2) NOT NULL,
                    type ENUM('DEPOSIT', 'WITHDRAW') NOT NULL,
                    status ENUM('PENDING', 'PROCESSING', 'COMPLETED', 'FAILED', 'RETRYING') NOT NULL DEFAULT 'PENDING',
                    prev_balance DECIMAL(19,2),
                    new_balance DECIMAL(19,2),
                    creation_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                    processed_time TIMESTAMP NULL,
                    FOREIGN KEY (account_id) REFERENCES users(user_id)
                )
            """);

        }
        catch (Exception e) {
            System.err.println("Error creating tables: " + e.getMessage());
        }
    }
}

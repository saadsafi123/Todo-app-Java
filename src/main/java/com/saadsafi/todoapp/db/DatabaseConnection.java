package com.saadsafi.todoapp.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConnection {

    // --- !! IMPORTANT !! ---
    // Update these 3 variables to match your MySQL setup
    private static final String DB_URL = "jdbc:mysql://localhost:3306/todo_app";
    private static final String USER = "root";
    private static final String PASSWORD = "saad123"; 
    // -----------------------

    // This method will be called from all over our app to get a connection
    public static Connection getConnection() throws SQLException {
        try {
            // 1. Load the database driver
            Class.forName("com.mysql.cj.jdbc.Driver");
            
            // 2. Return the connection
            return DriverManager.getConnection(DB_URL, USER, PASSWORD);
            
        } catch (ClassNotFoundException e) {
            System.out.println("MySQL JDBC Driver not found!");
            throw new SQLException("JDBC Driver not found", e);
        }
    }

    /* * --- This is a temporary main method for TESTING ONLY ---
     * We will delete this later.
     */
    public static void main(String[] args) {
        try {
            Connection conn = DatabaseConnection.getConnection();
            if (conn != null && !conn.isClosed()) {
                System.out.println("Connection Successful! :)");
                conn.close();
            } else {
                System.out.println("Connection Failed! :(");
            }
        } catch (SQLException e) {
            System.out.println("Connection Failed! See error below:");
            e.printStackTrace();
        }
    }
}
package com.saadsafi.todoapp.dao;

import com.saadsafi.todoapp.db.DatabaseConnection; // Our database connector
import com.saadsafi.todoapp.model.User;            // Our User model
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import org.mindrot.jbcrypt.BCrypt; // The password hasher

/**
 * Data Access Object for all User-related database operations.
 */
public class UserDAO {

    /**
     * Registers a new user in the database.
     * Hashes the password using jBCrypt.
     *
     * @param username The username to register.
     * @param password The plain-text password to hash and store.
     * @return true if registration was successful, false otherwise.
     */
    public boolean registerUser(String username, String password) {
        // 1. Hash the password
        String hashedPassword = BCrypt.hashpw(password, BCrypt.gensalt());

        // 2. Create the SQL query
        // We use '?' as placeholders to prevent SQL injection
        String sql = "INSERT INTO users (username, password_hash) VALUES (?, ?)";

        // 3. Use try-with-resources to auto-close the connection
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, username);
            pstmt.setString(2, hashedPassword);
            
            // 4. Execute the query
            int rowsAffected = pstmt.executeUpdate();
            
            // 5. Return true if one row (the new user) was inserted
            return rowsAffected > 0;

        } catch (SQLException e) {
            // Check for duplicate username (SQL error code 1062)
            if (e.getErrorCode() == 1062) {
                System.err.println("Error: Username already exists.");
            } else {
                System.err.println("SQL Error during registration: " + e.getMessage());
            }
            return false;
        }
    }

    /**
     * Attempts to log in a user.
     *
     * @param username The username to check.
     * @param password The plain-text password to verify.
     * @return A User object if login is successful, null otherwise.
     */
    public User loginUser(String username, String password) {
        String sql = "SELECT user_id, password_hash FROM users WHERE username = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, username);
            
            // 1. Execute the query
            try (ResultSet rs = pstmt.executeQuery()) {
                
                // 2. Check if a user with that username was found
                if (rs.next()) {
                    int userId = rs.getInt("user_id");
                    String storedHash = rs.getString("password_hash");

                    // 3. Verify the plain-text password against the stored hash
                    if (BCrypt.checkpw(password, storedHash)) {
                        // Password matches! Return a new User object.
                        return new User(userId, username);
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("SQL Error during login: " + e.getMessage());
        }
        
        // 4. If anything fails (user not found, password mismatch, SQL error), return null.
        return null; 
    }
}

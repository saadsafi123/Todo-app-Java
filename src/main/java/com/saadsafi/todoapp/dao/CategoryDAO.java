package com.saadsafi.todoapp.dao;

import com.saadsafi.todoapp.db.DatabaseConnection;
import com.saadsafi.todoapp.model.Category;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.sql.Statement;

/**
 * Data Access Object for all Category-related database operations.
 */
public class CategoryDAO {

    /**
     * Fetches all categories for a specific user.
     *
     * @param userId The ID of the logged-in user.
     * @return A List of Category objects.
     */
    public List<Category> getCategoriesByUserId(int userId) {
        List<Category> categories = new ArrayList<>();
        String sql = "SELECT category_id, category_name FROM categories WHERE user_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, userId);

            try (ResultSet rs = pstmt.executeQuery()) {
                // Loop through all results and create Category objects
                while (rs.next()) {
                    int categoryId = rs.getInt("category_id");
                    String categoryName = rs.getString("category_name");
                    categories.add(new Category(categoryId, categoryName));
                }
            }
        } catch (SQLException e) {
            System.err.println("SQL Error getting categories: " + e.getMessage());
        }
        
        return categories; // Return the list (will be empty if no categories are found)
    }

    /**
     * Creates a new category in the database for a specific user.
     *
     * @param categoryName The name for the new category.
     * @param userId The ID of the user creating it.
     * @return The complete new Category object (with its new ID),
     * or null if creation failed.
     */
    public Category createCategory(String categoryName, int userId) {
        String sql = "INSERT INTO categories (user_id, category_name) VALUES (?, ?)";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            pstmt.setInt(1, userId);
            pstmt.setString(2, categoryName);

            int rowsAffected = pstmt.executeUpdate();

            if (rowsAffected > 0) {
                // Get the generated category_id
                try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        int newCategoryId = generatedKeys.getInt(1);
                        // Return a new Category object with the new ID
                        return new Category(newCategoryId, categoryName);
                    }
                }
            }
            return null; // Creation failed

        } catch (SQLException e) {
            System.err.println("SQL Error creating category: " + e.getMessage());
            return null;
        }
    }
    
    
        /**
     * Deletes a category from the database.
     * The database schema (ON DELETE SET NULL) will handle
     * un-linking any tasks associated with it.
     *
     * @param categoryId The ID of the category to delete.
     * @return true if the deletion was successful, false otherwise.
     */
    public boolean deleteCategory(int categoryId) {
        String sql = "DELETE FROM categories WHERE category_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, categoryId);
            int rowsAffected = pstmt.executeUpdate();

            return rowsAffected > 0;

        } catch (SQLException e) {
            System.err.println("SQL Error deleting category: " + e.getMessage());
            return false;
        }
    }
}
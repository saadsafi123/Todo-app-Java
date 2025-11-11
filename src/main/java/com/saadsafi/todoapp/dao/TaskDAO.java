package com.saadsafi.todoapp.dao;

import com.saadsafi.todoapp.db.DatabaseConnection;
import com.saadsafi.todoapp.model.Category;
import com.saadsafi.todoapp.model.Task;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.sql.Statement;

/**
 * Data Access Object for all Task-related database operations.
 */
public class TaskDAO {

    /**
     * Fetches all tasks for a specific user AND a specific category.
     *
     * @param userId The ID of the logged-in user.
     * @param categoryId The ID of the category to filter by.
     * @return A List of Task objects.
     */
    public List<Task> getTasksByUserAndCategory(int userId, int categoryId) {
        List<Task> tasks = new ArrayList<>();
        
        // This SQL query joins tasks and categories
        // t.* means "all columns from the tasks table"
        // c.category_name is the only column we need from categories
        String sql = "SELECT t.*, c.category_name " +
                     "FROM tasks t " +
                     "LEFT JOIN categories c ON t.category_id = c.category_id " +
                     "WHERE t.user_id = ? AND t.category_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, userId);
            pstmt.setInt(2, categoryId);

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    // --- Create Category Object (if it exists) ---
                    Category category = null;
                    String categoryName = rs.getString("category_name");
                    if (categoryName != null) {
                        // We already know the categoryId
                        category = new Category(categoryId, categoryName);
                    }

                    // --- Get other task data ---
                    int taskId = rs.getInt("task_id");
                    String title = rs.getString("title");
                    String description = rs.getString("description");
                    String priority = rs.getString("priority");
                    
                    // Handle NULL dates from the database
                    LocalDate dueDate = null;
                    if (rs.getDate("due_date") != null) {
                        dueDate = rs.getDate("due_date").toLocalDate();
                    }
                    
                    String status = rs.getString("status");

                    // --- Create Task Object ---
                    tasks.add(new Task(taskId, userId, title, description,
                                      priority, dueDate, status, category));
                }
            }
        } catch (SQLException e) {
            System.err.println("SQL Error getting tasks: " + e.getMessage());
        }
        
        return tasks; // Return the list
    }
    
    // We will add more methods here later:
    /**
     * Creates a new task in the database.
     *
     * @param task The Task object to create (note: task_id will be ignored)
     * @return The complete Task object including the new auto-generated task_id,
     * or null if the creation failed.
     */
    public Task createTask(Task task) {
        String sql = "INSERT INTO tasks (user_id, category_id, title, description, priority, due_date, status) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?)";

        // Use Statement.RETURN_GENERATED_KEYS to get the new task_id
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            pstmt.setInt(1, task.getUserId());

            if (task.getCategory() != null) {
                pstmt.setInt(2, task.getCategory().getCategoryId());
            } else {
                pstmt.setNull(2, java.sql.Types.INTEGER);
            }

            pstmt.setString(3, task.getTitle());
            pstmt.setString(4, task.getDescription());
            pstmt.setString(5, task.getPriority().name());

            if (task.getDueDate() != null) {
                pstmt.setDate(6, java.sql.Date.valueOf(task.getDueDate()));
            } else {
                pstmt.setNull(6, java.sql.Types.DATE);
            }

            pstmt.setString(7, task.getStatus().name());

            int rowsAffected = pstmt.executeUpdate();

            if (rowsAffected > 0) {
                // Get the generated task_id
                try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        int newTaskId = generatedKeys.getInt(1);

                        // Return a new Task object with the correct ID
                        return new Task(
                                newTaskId,
                                task.getUserId(),
                                task.getTitle(),
                                task.getDescription(),
                                task.getPriority().name(),
                                task.getDueDate(),
                                task.getStatus().name(),
                                task.getCategory()
                        );
                    }
                }
            }

            return null; // Creation failed

        } catch (SQLException e) {
            System.err.println("SQL Error creating task: " + e.getMessage());
            return null;
        }
    }
    // --- ADD THIS NEW METHOD TO TaskDAO.java ---

    /**
     * Updates an existing task in the database.
     *
     * @param task The Task object containing the updated information.
     * @return true if the update was successful, false otherwise.
     */
    public boolean updateTask(Task task) {
        String sql = "UPDATE tasks SET " +
                     "title = ?, " +
                     "description = ?, " +
                     "priority = ?, " +
                     "due_date = ?, " +
                     "status = ?, " +
                     "category_id = ? " +
                     "WHERE task_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            // Set the parameters for the UPDATE statement
            pstmt.setString(1, task.getTitle());
            pstmt.setString(2, task.getDescription());

            // Convert Enums to String for the database
            pstmt.setString(3, task.getPriority().name()); 

            // Convert LocalDate to java.sql.Date
            if (task.getDueDate() != null) {
                pstmt.setDate(4, java.sql.Date.valueOf(task.getDueDate()));
            } else {
                pstmt.setNull(4, java.sql.Types.DATE);
            }

            pstmt.setString(5, task.getStatus().name());

            // Handle possible null category
            if (task.getCategory() != null) {
                pstmt.setInt(6, task.getCategory().getCategoryId());
            } else {
                pstmt.setNull(6, java.sql.Types.INTEGER);
            }

            // Set the WHERE clause parameter
            pstmt.setInt(7, task.getTaskId());

            // Execute the update
            int rowsAffected = pstmt.executeUpdate();

            // Return true if exactly one row was updated
            return rowsAffected > 0;

        } catch (SQLException e) {
            System.err.println("SQL Error updating task: " + e.getMessage());
            return false;
        }
    }
    // --- ADD THIS NEW METHOD TO TaskDAO.java ---

    /**
     * Deletes a task from the database based on its ID.
     *
     * @param taskId The ID of the task to delete.
     * @return true if the deletion was successful, false otherwise.
     */
    public boolean deleteTask(int taskId) {
        String sql = "DELETE FROM tasks WHERE task_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, taskId);

            int rowsAffected = pstmt.executeUpdate();

            // Return true if exactly one row was deleted
            return rowsAffected > 0;

        } catch (SQLException e) {
            System.err.println("SQL Error deleting task: " + e.getMessage());
            return false;
        }
    }
}
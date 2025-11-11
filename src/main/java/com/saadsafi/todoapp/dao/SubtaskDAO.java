package com.saadsafi.todoapp.dao;

import com.saadsafi.todoapp.db.DatabaseConnection;
import com.saadsafi.todoapp.model.Status;
import com.saadsafi.todoapp.model.Subtask;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement; // <-- Required import
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object for all Subtask-related database operations.
 */
public class SubtaskDAO {

    /**
     * Fetches all subtasks for a specific parent task.
     * @param taskId The ID of the parent task.
     * @return A List of Subtask objects.
     */
    public List<Subtask> getSubtasksByTaskId(int taskId) {
        List<Subtask> subtasks = new ArrayList<>();
        String sql = "SELECT * FROM subtasks WHERE task_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, taskId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    subtasks.add(new Subtask(
                            rs.getInt("subtask_id"),
                            rs.getInt("task_id"),
                            rs.getString("title"),
                            rs.getString("status")
                    ));
                }
            }
        } catch (SQLException e) {
            System.err.println("SQL Error getting subtasks: " + e.getMessage());
        }
        return subtasks;
    }

    /**
     * Creates a new subtask for a parent task.
     * @param title The text of the subtask.
     * @param taskId The ID of the parent task.
     * @return The new Subtask object with its generated ID, or null.
     */
    public Subtask createSubtask(String title, int taskId) {
        String sql = "INSERT INTO subtasks (task_id, title, status) VALUES (?, ?, ?)";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            pstmt.setInt(1, taskId);
            pstmt.setString(2, title);
            pstmt.setString(3, Status.PENDING.name());

            int rowsAffected = pstmt.executeUpdate();
            if (rowsAffected > 0) {
                try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        int newSubtaskId = generatedKeys.getInt(1);
                        return new Subtask(newSubtaskId, taskId, title, Status.PENDING.name());
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("SQL Error creating subtask: " + e.getMessage());
        }
        return null;
    }

    /**
     * Updates an existing subtask (e.g., to mark as completed).
     * @param subtask The Subtask object with updated info.
     * @return true if successful, false otherwise.
     */
    public boolean updateSubtask(Subtask subtask) {
        String sql = "UPDATE subtasks SET title = ?, status = ? WHERE subtask_id = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, subtask.getTitle());
            pstmt.setString(2, subtask.getStatus().name());
            pstmt.setInt(3, subtask.getSubtaskId());
            
            int rowsAffected = pstmt.executeUpdate();
            return rowsAffected > 0;
            
        } catch (SQLException e) {
            System.err.println("SQL Error updating subtask: " + e.getMessage());
        }
        return false;
    }

    /**
     * Deletes a subtask from the database.
     * @param subtaskId The ID of the subtask to delete.
     * @return true if successful, false otherwise.
     */
    public boolean deleteSubtask(int subtaskId) {
        String sql = "DELETE FROM subtasks WHERE subtask_id = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, subtaskId);
            int rowsAffected = pstmt.executeUpdate();
            return rowsAffected > 0;

        } catch (SQLException e) {
            System.err.println("SQL Error deleting subtask: " + e.getMessage());
        }
        return false;
    }
}
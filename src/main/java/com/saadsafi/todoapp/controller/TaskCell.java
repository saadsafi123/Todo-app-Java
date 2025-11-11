package com.saadsafi.todoapp.controller;

import com.saadsafi.todoapp.dao.TaskDAO;
import com.saadsafi.todoapp.model.Status;
import com.saadsafi.todoapp.model.Task;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;

public class TaskCell extends ListCell<Task> {
    private HBox hbox = new HBox(10);
    private CheckBox checkBox = new CheckBox();
    private Label titleLabel = new Label();
    private TaskDAO taskDAO;
    private Task currentTask;

    public TaskCell(TaskDAO taskDAO) {
        super();
        this.taskDAO = taskDAO;

        HBox.setHgrow(titleLabel, Priority.ALWAYS);
        
        // Add style classes
        hbox.getStyleClass().add("task-cell-hbox");
        titleLabel.getStyleClass().add("task-cell-label");
        
        hbox.getChildren().addAll(checkBox, titleLabel);

        checkBox.setOnAction(event -> {
            if (currentTask != null) {
                if (checkBox.isSelected()) {
                    currentTask.setStatus(Status.COMPLETED);
                } else {
                    currentTask.setStatus(Status.PENDING);
                }
                
                boolean success = taskDAO.updateTask(currentTask);
                
                if (success) {
                    updateStyle(currentTask);
                }
            }
        });
    }

    /**
     * Updates the style CLASS based on the task's status.
     */
    private void updateStyle(Task task) {
        if (task.getStatus() == Status.COMPLETED) {
            // Apply strikethrough and a lighter gray text
            titleLabel.setStyle("-fx-strikethrough: true; -fx-text-fill: #9CA3AF;");
        } else {
            // Remove strikethrough and use standard text color
            titleLabel.setStyle("-fx-strikethrough: false; -fx-text-fill: #1F2937;");
        }
    }

    @Override
    protected void updateItem(Task task, boolean empty) {
        super.updateItem(task, empty);
        
        this.currentTask = task; 

        if (empty || task == null) {
            setText(null);
            setGraphic(null);
        } else {
            titleLabel.setText(task.getTitle());
            checkBox.setSelected(task.getStatus() == Status.COMPLETED);
            updateStyle(task); // Apply the style
            setGraphic(hbox);
        }
    }
}
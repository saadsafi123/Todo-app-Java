package com.saadsafi.todoapp.model;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class Subtask {

    private final IntegerProperty subtaskId;
    private final IntegerProperty taskId; // To link back to the parent task
    private final StringProperty title;
    private final ObjectProperty<Status> status; // Re-uses the Status enum

    /**
     * Constructor
     */
    public Subtask(int subtaskId, int taskId, String title, String status) {
        this.subtaskId = new SimpleIntegerProperty(subtaskId);
        this.taskId = new SimpleIntegerProperty(taskId);
        this.title = new SimpleStringProperty(title);
        
        // Use the safe helper method from the Status enum
        this.status = new SimpleObjectProperty<>(Status.fromString(status));
    }

    // --- Getters ---
    
    public int getSubtaskId() {
        return subtaskId.get();
    }

    public int getTaskId() {
        return taskId.get();
    }

    public String getTitle() {
        return title.get();
    }

    public Status getStatus() {
        return status.get();
    }

    // --- Setters ---
    
    public void setTitle(String title) {
        this.title.set(title);
    }

    public void setStatus(Status status) {
        this.status.set(status);
    }

    // --- JavaFX Properties ---
    
    public IntegerProperty subtaskIdProperty() {
        return subtaskId;
    }

    public IntegerProperty taskIdProperty() {
        return taskId;
    }

    public StringProperty titleProperty() {
        return title;
    }

    public ObjectProperty<Status> statusProperty() {
        return status;
    }
}
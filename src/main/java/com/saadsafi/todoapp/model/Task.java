package com.saadsafi.todoapp.model;

import java.time.LocalDate;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class Task {

    // These are the JavaFX properties matching our database columns
    private final IntegerProperty taskId;
    private final IntegerProperty userId;
    private final StringProperty title;
    private final StringProperty description;
    private final ObjectProperty<Priority> priority;
    private final ObjectProperty<LocalDate> dueDate;
    private final ObjectProperty<Status> status;
    
    // This is the "linked" Category object. Can be null (for "Inbox")
    private final ObjectProperty<Category> category;

    /**
     * Full constructor for creating a Task object from database data
     */
    public Task(int taskId, int userId, String title, String description,
                String priority, LocalDate dueDate, String status, Category category) {
        
        this.taskId = new SimpleIntegerProperty(taskId);
        this.userId = new SimpleIntegerProperty(userId);
        this.title = new SimpleStringProperty(title);
        this.description = new SimpleStringProperty(description);
        
        // Use our safe helper methods from the enums
        this.priority = new SimpleObjectProperty<>(Priority.fromString(priority));
        this.status = new SimpleObjectProperty<>(Status.fromString(status));
        
        // These can be null
        this.dueDate = new SimpleObjectProperty<>(dueDate);
        this.category = new SimpleObjectProperty<>(category);
    }

    // --- Getters for the values ---
    
    public int getTaskId() { return taskId.get(); }
    public int getUserId() { return userId.get(); }
    public String getTitle() { return title.get(); }
    public String getDescription() { return description.get(); }
    public Priority getPriority() { return priority.get(); }
    public LocalDate getDueDate() { return dueDate.get(); }
    public Status getStatus() { return status.get(); }
    public Category getCategory() { return category.get(); }

    // --- Setters for the values ---
    // We need these to update the task
    
    public void setTitle(String title) { this.title.set(title); }
    public void setDescription(String description) { this.description.set(description); }
    public void setPriority(Priority priority) { this.priority.set(priority); }
    public void setDueDate(LocalDate dueDate) { this.dueDate.set(dueDate); }
    public void setStatus(Status status) { this.status.set(status); }
    public void setCategory(Category category) { this.category.set(category); }

    // --- Getters for the *Properties* ---
    // These are used by JavaFX to bind to UI components
    
    public IntegerProperty taskIdProperty() { return taskId; }
    public IntegerProperty userIdProperty() { return userId; }
    public StringProperty titleProperty() { return title; }
    public StringProperty descriptionProperty() { return description; }
    public ObjectProperty<Priority> priorityProperty() { return priority; }
    public ObjectProperty<LocalDate> dueDateProperty() { return dueDate; }
    public ObjectProperty<Status> statusProperty() { return status; }
    public ObjectProperty<Category> categoryProperty() { return category; }
}
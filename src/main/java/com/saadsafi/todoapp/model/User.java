package com.saadsafi.todoapp.model;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class User {

    private final IntegerProperty userId;
    private final StringProperty username;

    /**
     * Constructor for the logged-in user
     * @param userId - The user's ID from the database
     * @param username - The user's username
     */
    public User(int userId, String username) {
        this.userId = new SimpleIntegerProperty(userId);
        this.username = new SimpleStringProperty(username);
    }

    // --- Getters ---
    
    public int getUserId() {
        return userId.get();
    }

    public String getUsername() {
        return username.get();
    }

    // --- JavaFX Properties ---
    
    public IntegerProperty userIdProperty() {
        return userId;
    }

    public StringProperty usernameProperty() {
        return username;
    }
}
package com.saadsafi.todoapp.model;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class Category {

    // These are JavaFX properties, which match our database columns
    private final IntegerProperty categoryId;
    private final StringProperty categoryName;

    /**
     * Constructor
     * @param categoryId - The ID from the database
     * @param categoryName - The name of the category
     */
    public Category(int categoryId, String categoryName) {
        this.categoryId = new SimpleIntegerProperty(categoryId);
        this.categoryName = new SimpleStringProperty(categoryName);
    }

    // --- Getters for the values ---
    
    public int getCategoryId() {
        return categoryId.get();
    }

    public String getCategoryName() {
        return categoryName.get();
    }

    // --- Getters for the *Properties* ---
    // These are used by JavaFX to bind to the UI

    public IntegerProperty categoryIdProperty() {
        return categoryId;
    }

    public StringProperty categoryNameProperty() {
        return categoryName;
    }

    /**
     * This method is crucial for displaying the category in a ComboBox or ListView.
     * JavaFX will automatically call this toString() method.
     */
    @Override
    public String toString() {
        return getCategoryName();
    }
}

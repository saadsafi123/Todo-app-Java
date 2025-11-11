package com.saadsafi.todoapp.model;

public enum Priority {
    HIGH,
    MEDIUM,
    LOW;

    /**
     * Helper method to safely convert a String from the database
     * into our Priority enum.
     */
    public static Priority fromString(String priority) {
        if (priority == null) {
            return MEDIUM; // Default value
        }
        try {
            return Priority.valueOf(priority.toUpperCase());
        } catch (IllegalArgumentException e) {
            return MEDIUM; // Default if the string is invalid
        }
    }
}
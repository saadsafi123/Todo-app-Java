package com.saadsafi.todoapp.model;

public enum Status {
    PENDING,
    COMPLETED;

    /**
     * Helper method to safely convert a String from the database
     * into our Status enum.
     */
    public static Status fromString(String status) {
        if (status == null) {
            return PENDING; // Default value
        }
        try {
            return Status.valueOf(status.toUpperCase());
        } catch (IllegalArgumentException e) {
            return PENDING; // Default if the string is invalid
        }
    }
}
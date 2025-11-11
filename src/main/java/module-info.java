module com.saadsafi.todoapp {
   requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;
    requires jbcrypt;  // <-- ADD THIS LINE
    requires java.base;

    opens com.saadsafi.todoapp to javafx.fxml;
    opens com.saadsafi.todoapp.controller to javafx.fxml;

    // We might need this for our DAO, let's add it just in case
    opens com.saadsafi.todoapp.dao to javafx.fxml; 
    
    // --- ADD THIS LINE ---
    // This allows JavaFX to access your Model classes (Task, Category, Priority)
    opens com.saadsafi.todoapp.model to javafx.fxml;

    exports com.saadsafi.todoapp;
}

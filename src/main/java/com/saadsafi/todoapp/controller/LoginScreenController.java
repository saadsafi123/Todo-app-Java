package com.saadsafi.todoapp.controller;

import com.saadsafi.todoapp.dao.UserDAO;
import com.saadsafi.todoapp.model.User;

// --- NEW IMPORTS ---
import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage; 

public class LoginScreenController implements Initializable {
    
    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private Button loginButton;
    @FXML private Button registerButton;
    @FXML private Label messageLabel;

    private UserDAO userDAO;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        messageLabel.setText("");
        this.userDAO = new UserDAO(); 
    }    

    @FXML
    private void handleLoginButtonAction(ActionEvent event) {
        String username = usernameField.getText();
        String password = passwordField.getText();

        if (username.isEmpty() || password.isEmpty()) {
            messageLabel.setText("Username and password cannot be empty.");
            return;
        }

        User user = userDAO.loginUser(username, password);

        if (user != null) {
            // --- SUCCESS! ---
            messageLabel.setText("Login Successful! Welcome, " + user.getUsername());
            System.out.println("Successful login for user_id: " + user.getUserId());
            
            // --- NEW: Call the method to switch scenes ---
            loadMainApp(user, event);
            
        } else {
            // --- FAILURE ---
            messageLabel.setText("Error: Invalid username or password.");
        }
    }

    @FXML
    private void handleRegisterButtonAction(ActionEvent event) {
        String username = usernameField.getText();
        String password = passwordField.getText();

        if (username.isEmpty() || password.isEmpty()) {
            messageLabel.setText("Username and password cannot be empty.");
            return;
        }

        boolean success = userDAO.registerUser(username, password);

        if (success) {
            messageLabel.setText("Registration Successful! Please log in.");
        } else {
            messageLabel.setText("Error: Registration failed. Username may be taken.");
        }
    }
    
    
    // --- THIS IS THE NEW HELPER METHOD ---
    /**
     * Closes the login window and opens the main application window.
     * @param user The user who successfully logged in.
     * @param event The ActionEvent from the button click, used to get the stage.
     */
    private void loadMainApp(User user, ActionEvent event) {
        try {
            // 1. Load the FXML file
            // Make sure the path is correct!
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/MainApp.fxml"));
            Parent root = loader.load();

            // 2. Get the MainAppController
            MainAppController mainAppController = loader.getController();
            
            // 3. Pass the logged-in user's data to the controller
            mainAppController.initData(user);

            // 4. Create a new stage (window) for the main app
            Stage mainStage = new Stage();
            mainStage.setTitle("Todo App - Dashboard");
            Scene scene = new Scene(root);
            
            // 5. Load the main app's CSS
            try {
                String cssPath = "/styles/mainapp.css";
                scene.getStylesheets().add(getClass().getResource(cssPath).toExternalForm());
            } catch (Exception e) {
                System.err.println("Could not load mainapp.css: " + e.getMessage());
            }

            mainStage.setScene(scene);
            mainStage.show();

            // 6. Close the current (login) window
            Stage loginStage = (Stage) loginButton.getScene().getWindow();
            loginStage.close();

        } catch (IOException e) {
            e.printStackTrace();
            messageLabel.setText("Error: Could not load main application window.");
        }
    }
}
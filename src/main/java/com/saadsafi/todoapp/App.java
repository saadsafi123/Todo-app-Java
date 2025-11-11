package com.saadsafi.todoapp;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.stage.StageStyle; // <-- NEW IMPORT
import java.io.IOException;

public class App extends Application {

    private Stage splashStage;

    /**
     * This is the main entry point, it now shows the splash screen first.
     */
    @Override
    public void start(Stage primaryStage) throws IOException {
        // 1. Show the Splash Screen
        showSplashScreen();

        // 2. Create a background task to simulate loading
        Task<Void> loadingTask = new Task<>() {
            @Override
            protected Void call() throws Exception {
                // Simulate loading time (e.g., connecting to DB)
                // We can also load the login FXML here to speed things up
                FXMLLoader loader = new FXMLLoader(App.class.getResource("/fxml/LoginScreen.fxml"));
                Parent loginRoot = loader.load();

                // Simulate a 2-second load time
                Thread.sleep(2000); 

                // When done, switch to the login screen on the main UI thread
                Platform.runLater(() -> {
                    showLoginScreen(loginRoot);
                    splashStage.close(); // Close the splash screen
                });
                
                return null;
            }
        };

        // 3. Start the background task
        new Thread(loadingTask).start();
    }

    /**
     * Creates and shows the borderless Splash Screen
     */
    private void showSplashScreen() {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(App.class.getResource("/fxml/SplashScreen.fxml"));
            Parent splashRoot = fxmlLoader.load();
            
            Scene scene = new Scene(splashRoot);
            
            // Use an UNDECORATED stage for a modern splash screen
            splashStage = new Stage();
            splashStage.initStyle(StageStyle.UNDECORATED);
            splashStage.setScene(scene);
            splashStage.show();
            
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Creates and shows the main Login Screen
     */
    private void showLoginScreen(Parent loginRoot) {
        Stage loginStage = new Stage();
        loginStage.setTitle("Login"); // Title will be set based on brand
        Scene scene = new Scene(loginRoot);

        try {
            String cssPath = "/styles/loginscreen.css";
            scene.getStylesheets().add(getClass().getResource(cssPath).toExternalForm());
        } catch (Exception e) {
            System.err.println("Could not load loginscreen.css: " + e.getMessage());
        }

        loginStage.setScene(scene);
        loginStage.setResizable(false);
        loginStage.show();
    }

    /**
     * The main(String[] args) method that launches the app.
     */
    public static void main(String[] args) {
        launch();
    }
}
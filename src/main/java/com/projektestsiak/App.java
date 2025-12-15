package com.projektestsiak;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import com.projektestsiak.models.DatabaseConnection;

import java.sql.SQLException;

public class App extends Application {

    @Override
    public void start(Stage primaryStage) {
        try {
            // Initialize database connection
            new DatabaseConnection();
            
            // Load login page
            Parent root = FXMLLoader.load(getClass().getResource("/fxml/auth/login.fxml"));
            Scene scene = new Scene(root, 800, 600);
            
            // Load CSS
            scene.getStylesheets().add(getClass().getResource("/css/style.css").toExternalForm());
            
            primaryStage.setTitle("SIAK - Sistem Akademik");
            primaryStage.setScene(scene);
            primaryStage.show();
            
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
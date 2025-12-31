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
            
            // Load CSS - PASTIKAN PATH INI BENAR
            try {
                String cssPath = getClass().getResource("/css/style.css").toExternalForm();
                scene.getStylesheets().add(cssPath);
                System.out.println("CSS loaded successfully from: " + cssPath);
            } catch (NullPointerException e) {
                System.err.println("CSS file not found! Check the path: /css/style.css");
                // Continue without CSS
            }
            
            primaryStage.setTitle("SIAK - Sistem Akademik Sekolah");
            primaryStage.setScene(scene);
            primaryStage.show();
            
        } catch (Exception e) {
            e.printStackTrace();
            // Show error message jika gagal
            javafx.scene.control.Alert alert = new javafx.scene.control.Alert(
                javafx.scene.control.Alert.AlertType.ERROR);
            alert.setTitle("Application Error");
            alert.setHeaderText("Failed to start application");
            alert.setContentText("Error: " + e.getMessage());
            alert.showAndWait();
        }
    }
    
    public static void main(String[] args) {
        // Create uploads directory jika belum ada
        java.io.File uploadsDir = new java.io.File("uploads");
        if (!uploadsDir.exists()) {
            if (uploadsDir.mkdirs()) {
                System.out.println("Created uploads directory: " + uploadsDir.getAbsolutePath());
            }
        }
        
        launch(args);
    }
}
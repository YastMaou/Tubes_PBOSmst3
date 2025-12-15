package com.projektestsiak.controllers.auth;

import com.projektestsiak.models.LoginModel;
import com.projektestsiak.models.SessionManager;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.SQLException;

public class LoginController {

    @FXML
    private TextField usernameField;

    @FXML
    private PasswordField passwordField;

    private LoginModel loginModel;

    public LoginController() {
        try {
            this.loginModel = new LoginModel();
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("Error", "Gagal terhubung ke database: " + e.getMessage());
        }
    }

    @FXML
    private void handleLogin() {
        String username = usernameField.getText();
        String password = passwordField.getText();

        if (username.isEmpty() || password.isEmpty()) {
            showAlert("Error", "Username dan password harus diisi!");
            return;
        }

        try {
            if (loginModel.verifyLogin(username, password)) {
                showAlert("Success", "Login berhasil! Selamat datang " + SessionManager.getCurrentNama());
                
                // Redirect based on role
                if (SessionManager.isAdmin()) {
                    loadScene("/fxml/admin/admin_dashboard.fxml");
                } else {
                    loadScene("/fxml/akademik/dashboard.fxml");
                }
            } else {
                showAlert("Error", "Username atau password salah!");
            }
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Error", "Terjadi kesalahan: " + e.getMessage());
        }
    }

    private void loadScene(String fxmlPath) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent root = loader.load();
            Stage stage = (Stage) usernameField.getScene().getWindow();
            stage.setScene(new Scene(root, 1000, 700));
            stage.setTitle("SIAK - Sistem Akademik");
        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Error", "Gagal memuat halaman: " + e.getMessage());
        }
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
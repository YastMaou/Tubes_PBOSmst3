package com.projektestsiak.controllers.auth;

import com.projektestsiak.models.LoginModel;
import com.projektestsiak.models.SessionManager;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.SQLException;

public class LoginController {

    @FXML
    private TextField usernameField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private Button loginButton;

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
    public void initialize() {
        // Set focus ke username field saat pertama kali load
        usernameField.requestFocus();
        
        // Setup enter key listener untuk kedua field
        setupEnterKeyListener();
    }

    private void setupEnterKeyListener() {
        // Enter pada username field akan pindah ke password field
        usernameField.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ENTER) {
                passwordField.requestFocus();
            }
        });

        // Enter pada password field akan trigger login
        passwordField.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ENTER) {
                handleLogin();
            }
        });
    }

    @FXML
    private void handleKeyPress(KeyEvent event) {
        if (event.getCode() == KeyCode.ENTER) {
            // Jika focus ada di password field, langsung login
            if (passwordField.isFocused()) {
                handleLogin();
            }
            // Jika focus ada di username field, pindah ke password field
            else if (usernameField.isFocused()) {
                passwordField.requestFocus();
            }
        }
    }

    @FXML
    private void handleLogin() {
        String username = usernameField.getText().trim();
        String password = passwordField.getText().trim();

        // Validasi input
        if (validateInput(username, password)) {
            // Nonaktifkan button sementara
            loginButton.setDisable(true);
            loginButton.setText("MEMPROSES...");

            try {
                if (loginModel.verifyLogin(username, password)) {
                    // Login berhasil
                    showSuccessAlert("Selamat datang " + SessionManager.getCurrentNama());
                    redirectBasedOnRole();
                } else {
                    // Login gagal
                    showAlert("Error", "Username atau password salah!");
                    resetLoginButton();
                    // Kembalikan focus ke password field
                    passwordField.requestFocus();
                    passwordField.selectAll();
                }
            } catch (Exception e) {
                e.printStackTrace();
                showAlert("Error", "Terjadi kesalahan: " + e.getMessage());
                resetLoginButton();
            }
        }
    }

    private boolean validateInput(String username, String password) {
        if (username.isEmpty() || password.isEmpty()) {
            showAlert("Error", "Username dan password harus diisi!");
            return false;
        }

        if (username.length() < 3) {
            showAlert("Error", "Username minimal 3 karakter!");
            usernameField.requestFocus();
            usernameField.selectAll();
            return false;
        }

        if (password.length() < 6) {
            showAlert("Error", "Password minimal 6 karakter!");
            passwordField.requestFocus();
            passwordField.selectAll();
            return false;
        }

        return true;
    }

    private void redirectBasedOnRole() {
        try {
            String fxmlPath;
            String title;
            
            if (SessionManager.isAdmin()) {
                fxmlPath = "/fxml/admin/admin_dashboard.fxml";
                title = "Dashboard Administrator";
            } else {
                fxmlPath = "/fxml/akademik/dashboard.fxml";
                title = "Dashboard Akademik";
            }
            
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent root = loader.load();
            Stage stage = (Stage) usernameField.getScene().getWindow();
            stage.setScene(new Scene(root, 1000, 700));
            stage.setTitle("SIAK - " + title);
            stage.centerOnScreen();
            
        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Error", "Gagal memuat halaman: " + e.getMessage());
            resetLoginButton();
        }
    }

    private void resetLoginButton() {
        loginButton.setDisable(false);
        loginButton.setText("MASUK");
    }

    private void showSuccessAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Login Berhasil");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    // Method untuk quick login dari demo account (opsional)
    public void quickLogin(String username, String password) {
        usernameField.setText(username);
        passwordField.setText(password);
        handleLogin();
    }
}
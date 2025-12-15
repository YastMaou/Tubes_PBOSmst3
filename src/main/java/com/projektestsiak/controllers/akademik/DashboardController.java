package com.projektestsiak.controllers.akademik;

import com.projektestsiak.models.SessionManager;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.stage.Stage;

import java.io.IOException;

public class DashboardController {

    @FXML
    private Label welcomeLabel;

    @FXML
    private Label userInfoLabel;

    @FXML
    public void initialize() {
        welcomeLabel.setText("Selamat Datang, " + SessionManager.getCurrentNama() + "!");
        userInfoLabel.setText("Username: " + SessionManager.getCurrentUsername() + 
                            " | Role: " + SessionManager.getCurrentRole());
    }

    @FXML
    private void handleJadwal() {
        loadScene("/fxml/akademik/jadwal.fxml");
    }

    @FXML
    private void handleTugas() {
        loadScene("/fxml/akademik/tugas.fxml");
    }

    @FXML
    private void handlePelajaranTambahan() {
        loadScene("/fxml/akademik/pelajaran_tambahan.fxml");
    }

    @FXML
    private void handleLogout() {
        SessionManager.logout();
        loadScene("/fxml/auth/login.fxml");
    }

    private void loadScene(String fxmlPath) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent root = loader.load();
            Stage stage = (Stage) welcomeLabel.getScene().getWindow();
            stage.setScene(new Scene(root, 1000, 700));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
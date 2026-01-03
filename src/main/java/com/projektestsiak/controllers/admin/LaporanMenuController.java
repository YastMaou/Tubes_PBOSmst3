package com.projektestsiak.controllers.admin;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.scene.control.Button;

public class LaporanMenuController {

    @FXML private Button btnBack;

    private void load(String fxml, String title) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource(fxml));
            Stage stage = (Stage) btnBack.getScene().getWindow();
            stage.setScene(new Scene(root, 1200, 700));
            stage.setTitle("Sistem Akademik Sekolah - " + title);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML private void openNilai() {
        load("/fxml/admin/laporan_nilai.fxml", "Laporan Nilai");
    }

    @FXML private void openKehadiran() {
        load("/fxml/admin/laporan_kehadiran.fxml", "Laporan Kehadiran");
    }

    @FXML private void openKeuangan() {
        load("/fxml/admin/laporan_keuangan.fxml", "Laporan Keuangan");
    }

    @FXML private void openAktivitas() {
        load("/fxml/admin/laporan_aktivitas.fxml", "Laporan Aktivitas");
    }

    @FXML private void handleBack() {
        load("/fxml/admin/admin_dashboard.fxml", "Dashboard Admin");
    }
}

package com.projektestsiak.controllers.admin;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.ListView;
import javafx.stage.Stage;

public class LaporanAktivitasController {

    @FXML private ListView<String> list;

    @FXML
    public void initialize() {
        list.setItems(FXCollections.observableArrayList(
            "Admin login - 01/01/2026 08:00",
            "Input nilai siswa - 01/01/2026 09:30",
            "Validasi pendaftaran - 01/01/2026 10:15"
        ));
    }

    @FXML
    private void handleBack() throws Exception {
        Parent root = FXMLLoader.load(getClass().getResource("/fxml/admin/laporan_menu.fxml"));
        Stage stage = (Stage) list.getScene().getWindow();
        stage.setScene(new Scene(root, 1200, 700));
    }
}

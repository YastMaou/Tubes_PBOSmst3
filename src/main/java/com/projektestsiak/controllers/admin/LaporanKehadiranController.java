package com.projektestsiak.controllers.admin;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;

public class LaporanKehadiranController {

    @FXML private TableView<KehadiranDummy> table;
    @FXML private TableColumn<KehadiranDummy, String> colNama;
    @FXML private TableColumn<KehadiranDummy, String> colTanggal;
    @FXML private TableColumn<KehadiranDummy, String> colStatus;

    @FXML
    public void initialize() {
        colNama.setCellValueFactory(new PropertyValueFactory<>("nama"));
        colTanggal.setCellValueFactory(new PropertyValueFactory<>("tanggal"));
        colStatus.setCellValueFactory(new PropertyValueFactory<>("status"));

        table.setItems(FXCollections.observableArrayList(
            new KehadiranDummy("Andi", "01-01-2026", "Hadir"),
            new KehadiranDummy("Budi", "01-01-2026", "Izin")
        ));
    }

    @FXML
    private void handleBack() throws Exception {
        Parent root = FXMLLoader.load(getClass().getResource("/fxml/admin/laporan_menu.fxml"));
        Stage stage = (Stage) table.getScene().getWindow();
        stage.setScene(new Scene(root, 1200, 700));
    }

    public static class KehadiranDummy {
        private String nama, tanggal, status;
        public KehadiranDummy(String n, String t, String s) {
            nama = n; tanggal = t; status = s;
        }
        public String getNama() { return nama; }
        public String getTanggal() { return tanggal; }
        public String getStatus() { return status; }
    }
}

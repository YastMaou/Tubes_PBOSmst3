package com.projektestsiak.controllers.akademik;

import com.projektestsiak.models.Jadwal;
import com.projektestsiak.models.JadwalModel;
import com.projektestsiak.models.SessionManager;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

public class JadwalController {

    @FXML
    private ComboBox<String> hariComboBox;

    @FXML
    private TableView<Jadwal> jadwalTable;

    @FXML
    private TableColumn<Jadwal, String> colHari;

    @FXML
    private TableColumn<Jadwal, String> colJam;

    @FXML
    private TableColumn<Jadwal, String> colMataKuliah;

    @FXML
    private TableColumn<Jadwal, String> colRuangan;

    private JadwalModel jadwalModel;
    private ObservableList<Jadwal> jadwalList;

    @FXML
    public void initialize() {
        try {
            jadwalModel = new JadwalModel();
            setupTable();
            setupComboBox();
            loadAllJadwal();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void setupTable() {
        colHari.setCellValueFactory(new PropertyValueFactory<>("hari"));
        colJam.setCellValueFactory(new PropertyValueFactory<>("jamFormat"));
        colMataKuliah.setCellValueFactory(new PropertyValueFactory<>("mataKuliahNama"));
        colRuangan.setCellValueFactory(new PropertyValueFactory<>("ruangan"));
        
        jadwalList = FXCollections.observableArrayList();
        jadwalTable.setItems(jadwalList);
    }

    private void setupComboBox() {
        hariComboBox.getItems().addAll("Semua", "Senin", "Selasa", "Rabu", "Kamis", "Jumat", "Sabtu");
        hariComboBox.setValue("Semua");
        
        hariComboBox.setOnAction(e -> filterJadwal());
    }

    private void loadAllJadwal() {
        List<Jadwal> allJadwal = jadwalModel.getJadwalForStudent(SessionManager.getCurrentUserId());
        jadwalList.setAll(allJadwal);
    }

    private void filterJadwal() {
        String selectedHari = hariComboBox.getValue();
        
        if ("Semua".equals(selectedHari)) {
            loadAllJadwal();
        } else {
            List<Jadwal> filteredJadwal = jadwalModel.getJadwalByHari(selectedHari);
            jadwalList.setAll(filteredJadwal);
        }
    }

    @FXML
    private void handleBack() {
        loadScene("/fxml/akademik/dashboard.fxml");
    }

    @FXML
    private void handleRefresh() {
        loadAllJadwal();
    }

    private void loadScene(String fxmlPath) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent root = loader.load();
            Stage stage = (Stage) jadwalTable.getScene().getWindow();
            stage.setScene(new Scene(root, 1000, 700));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
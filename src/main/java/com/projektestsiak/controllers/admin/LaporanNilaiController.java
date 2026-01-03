package com.projektestsiak.controllers.admin;

import com.projektestsiak.models.NilaiModel;
import com.projektestsiak.models.LaporanNilai;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;

import java.sql.SQLException;

public class LaporanNilaiController {

    @FXML private TableView<LaporanNilai> tableLaporan;
    @FXML private TableColumn<LaporanNilai, String> colNama;
    @FXML private TableColumn<LaporanNilai, String> colTugas;
    @FXML private TableColumn<LaporanNilai, Double> colNilai;
    @FXML private TableColumn<LaporanNilai, String> colKomentar;

    @FXML private Button btnBack;

    @FXML
    public void initialize() {
        colNama.setCellValueFactory(new PropertyValueFactory<>("namaSiswa"));
        colTugas.setCellValueFactory(new PropertyValueFactory<>("judulTugas"));
        colNilai.setCellValueFactory(new PropertyValueFactory<>("nilai"));
        colKomentar.setCellValueFactory(new PropertyValueFactory<>("komentar"));

        loadData();
    }

    private void loadData() {
        try {
            NilaiModel model = new NilaiModel();
            tableLaporan.setItems(
                FXCollections.observableArrayList(model.getLaporanNilai())
            );
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleBack() {
        try {
            Parent root = FXMLLoader.load(
                getClass().getResource("/fxml/admin/laporan_menu.fxml")
            );

            Stage stage = (Stage) btnBack.getScene().getWindow();
            stage.setScene(new Scene(root, 1200, 700));
            stage.setTitle("Sistem Akademik Sekolah - Laporan Akademik");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

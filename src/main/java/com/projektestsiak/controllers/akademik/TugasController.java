package com.projektestsiak.controllers.akademik;

import com.projektestsiak.models.Tugas;
import com.projektestsiak.models.TugasModel;
import com.projektestsiak.models.SessionManager;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

public class TugasController {

    @FXML
    private TableView<Tugas> tugasTable;

    @FXML
    private TableColumn<Tugas, String> colMataKuliah;

    @FXML
    private TableColumn<Tugas, String> colJudul;

    @FXML
    private TableColumn<Tugas, String> colDeskripsi;

    @FXML
    private TableColumn<Tugas, String> colDeadline;

    @FXML
    private TableColumn<Tugas, String> colStatus;

    private TugasModel tugasModel;
    private ObservableList<Tugas> tugasList;

    @FXML
    public void initialize() {
        try {
            tugasModel = new TugasModel();
            setupTable();
            loadTugas();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void setupTable() {
        colMataKuliah.setCellValueFactory(new PropertyValueFactory<>("mataKuliahNama"));
        colJudul.setCellValueFactory(new PropertyValueFactory<>("judul"));
        colDeskripsi.setCellValueFactory(new PropertyValueFactory<>("deskripsi"));
        colDeadline.setCellValueFactory(new PropertyValueFactory<>("deadlineFormat"));
        colStatus.setCellValueFactory(new PropertyValueFactory<>("status"));
        
        tugasList = FXCollections.observableArrayList();
        tugasTable.setItems(tugasList);
    }

    private void loadTugas() {
        List<Tugas> allTugas = tugasModel.getTugasWithStatus(SessionManager.getCurrentUserId());
        tugasList.setAll(allTugas);
    }

    @FXML
    private void handleKumpulkanTugas() {
        Tugas selectedTugas = tugasTable.getSelectionModel().getSelectedItem();
        
        if (selectedTugas == null) {
            showAlert("Peringatan", "Pilih tugas yang akan dikumpulkan!");
            return;
        }

        if (selectedTugas.isSubmitted()) {
            showAlert("Info", "Tugas ini sudah dikumpulkan!");
            return;
        }

        // Navigate to pengumpulan page
        loadPengumpulanTugasScene(selectedTugas.getId());
    }

    @FXML
    private void handleBack() {
        loadScene("/fxml/akademik/dashboard.fxml");
    }

    @FXML
    private void handleRefresh() {
        loadTugas();
    }

    private void loadPengumpulanTugasScene(int tugasId) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/akademik/pengumpulan_tugas.fxml"));
            Parent root = loader.load();
            
            PengumpulanTugasController controller = loader.getController();
            controller.setTugasId(tugasId);
            
            Stage stage = (Stage) tugasTable.getScene().getWindow();
            stage.setScene(new Scene(root, 1000, 700));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void loadScene(String fxmlPath) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent root = loader.load();
            Stage stage = (Stage) tugasTable.getScene().getWindow();
            stage.setScene(new Scene(root, 1000, 700));
        } catch (IOException e) {
            e.printStackTrace();
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
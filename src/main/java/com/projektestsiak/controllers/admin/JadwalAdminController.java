package com.projektestsiak.controllers.admin;

import com.projektestsiak.models.JadwalAdminModel;
import com.projektestsiak.models.MataKuliah;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

public class JadwalAdminController {

    @FXML private TableView<JadwalAdminModel.JadwalDetail> jadwalTable;
    @FXML private TableColumn<JadwalAdminModel.JadwalDetail, String> colKode;
    @FXML private TableColumn<JadwalAdminModel.JadwalDetail, String> colMataKuliah;
    @FXML private TableColumn<JadwalAdminModel.JadwalDetail, String> colDosen;
    @FXML private TableColumn<JadwalAdminModel.JadwalDetail, String> colHari;
    @FXML private TableColumn<JadwalAdminModel.JadwalDetail, String> colJam;
    @FXML private TableColumn<JadwalAdminModel.JadwalDetail, String> colRuangan;
    
    @FXML private ComboBox<MataKuliah> mataKuliahComboBox;
    @FXML private ComboBox<String> hariComboBox;
    @FXML private TextField jamMulaiField;
    @FXML private TextField jamSelesaiField;
    @FXML private TextField ruanganField;
    
    @FXML private Button btnTambah;
    @FXML private Button btnUpdate;
    @FXML private Button btnHapus;
    @FXML private Button btnClear;
    
    private JadwalAdminModel jadwalModel;
    private ObservableList<JadwalAdminModel.JadwalDetail> jadwalList;
    private ObservableList<MataKuliah> mataKuliahList;
    private int selectedJadwalId = -1;

    @FXML
    public void initialize() {
        try {
            jadwalModel = new JadwalAdminModel();
            setupTable();
            setupComboBoxes();
            loadData();
            setupSelectionListener();
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("Error", "Gagal terhubung ke database: " + e.getMessage());
        }
    }

    private void setupTable() {
        colKode.setCellValueFactory(new PropertyValueFactory<>("kodeMataKuliah"));
        colMataKuliah.setCellValueFactory(new PropertyValueFactory<>("namaMataKuliah"));
        colDosen.setCellValueFactory(new PropertyValueFactory<>("dosen"));
        colHari.setCellValueFactory(new PropertyValueFactory<>("hari"));
        colJam.setCellValueFactory(new PropertyValueFactory<>("jamFormat"));
        colRuangan.setCellValueFactory(new PropertyValueFactory<>("ruangan"));
        
        jadwalList = FXCollections.observableArrayList();
        jadwalTable.setItems(jadwalList);
    }

    private void setupComboBoxes() {
        // Setup hari combo box
        hariComboBox.getItems().addAll("Senin", "Selasa", "Rabu", "Kamis", "Jumat", "Sabtu");
        
        // Setup mata kuliah combo box
        mataKuliahList = FXCollections.observableArrayList();
        List<MataKuliah> allMataKuliah = jadwalModel.getAllMataKuliah();
        mataKuliahList.setAll(allMataKuliah);
        mataKuliahComboBox.setItems(mataKuliahList);
        
        // Setup cell factory untuk menampilkan nama mata kuliah
        mataKuliahComboBox.setCellFactory(param -> new ListCell<MataKuliah>() {
            @Override
            protected void updateItem(MataKuliah item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item.getKode() + " - " + item.getNama());
                }
            }
        });
        
        mataKuliahComboBox.setButtonCell(new ListCell<MataKuliah>() {
            @Override
            protected void updateItem(MataKuliah item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item.getKode() + " - " + item.getNama());
                }
            }
        });
    }

    private void setupSelectionListener() {
        jadwalTable.getSelectionModel().selectedItemProperty().addListener(
            (observable, oldValue, newValue) -> {
                if (newValue != null) {
                    fillFormWithSelected(newValue);
                }
            }
        );
    }

    private void fillFormWithSelected(JadwalAdminModel.JadwalDetail jadwal) {
        selectedJadwalId = jadwal.getId();
        
        // Cari mata kuliah yang sesuai
        for (MataKuliah mk : mataKuliahList) {
            if (mk.getId() == jadwal.getMataKuliahId()) {
                mataKuliahComboBox.setValue(mk);
                break;
            }
        }
        
        hariComboBox.setValue(jadwal.getHari());
        jamMulaiField.setText(jadwal.getJamMulai().toString());
        jamSelesaiField.setText(jadwal.getJamSelesai().toString());
        ruanganField.setText(jadwal.getRuangan());
        
        btnTambah.setDisable(true);
        btnUpdate.setDisable(false);
        btnHapus.setDisable(false);
    }

    @FXML
    private void handleTambah() {
        if (!validateForm()) return;
        
        try {
            MataKuliah selectedMk = mataKuliahComboBox.getValue();
            String hari = hariComboBox.getValue();
            String jamMulai = jamMulaiField.getText();
            String jamSelesai = jamSelesaiField.getText();
            String ruangan = ruanganField.getText();
            
            boolean success = jadwalModel.tambahJadwal(
                selectedMk.getId(), hari, jamMulai, jamSelesai, ruangan
            );
            
            if (success) {
                showAlert("Sukses", "Jadwal berhasil ditambahkan!");
                clearForm();
                loadData();
            } else {
                showAlert("Error", "Gagal menambahkan jadwal!");
            }
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Error", "Terjadi kesalahan: " + e.getMessage());
        }
    }

    @FXML
    private void handleUpdate() {
        if (!validateForm() || selectedJadwalId == -1) return;
        
        try {
            MataKuliah selectedMk = mataKuliahComboBox.getValue();
            String hari = hariComboBox.getValue();
            String jamMulai = jamMulaiField.getText();
            String jamSelesai = jamSelesaiField.getText();
            String ruangan = ruanganField.getText();
            
            boolean success = jadwalModel.updateJadwal(
                selectedJadwalId, selectedMk.getId(), hari, jamMulai, jamSelesai, ruangan
            );
            
            if (success) {
                showAlert("Sukses", "Jadwal berhasil diperbarui!");
                clearForm();
                loadData();
            } else {
                showAlert("Error", "Gagal memperbarui jadwal!");
            }
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Error", "Terjadi kesalahan: " + e.getMessage());
        }
    }

    @FXML
    private void handleHapus() {
        if (selectedJadwalId == -1) {
            showAlert("Peringatan", "Pilih jadwal yang akan dihapus!");
            return;
        }
        
        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
        confirmation.setTitle("Konfirmasi Hapus");
        confirmation.setHeaderText(null);
        confirmation.setContentText("Apakah Anda yakin ingin menghapus jadwal ini?");
        
        if (confirmation.showAndWait().get() == ButtonType.OK) {
            boolean success = jadwalModel.hapusJadwal(selectedJadwalId);
            
            if (success) {
                showAlert("Sukses", "Jadwal berhasil dihapus!");
                clearForm();
                loadData();
            } else {
                showAlert("Error", "Gagal menghapus jadwal!");
            }
        }
    }

    @FXML
    private void handleClear() {
        clearForm();
    }

    @FXML
    private void handleBack() {
        loadScene("/fxml/admin/admin_dashboard.fxml");
    }

    @FXML
    private void handleRefresh() {
        loadData();
    }

    private void loadData() {
        List<JadwalAdminModel.JadwalDetail> allJadwal = jadwalModel.getAllJadwalWithDetails();
        jadwalList.setAll(allJadwal);
    }

    private boolean validateForm() {
        if (mataKuliahComboBox.getValue() == null) {
            showAlert("Validasi Error", "Pilih mata kuliah!");
            return false;
        }
        
        if (hariComboBox.getValue() == null || hariComboBox.getValue().isEmpty()) {
            showAlert("Validasi Error", "Pilih hari!");
            return false;
        }
        
        if (jamMulaiField.getText().isEmpty()) {
            showAlert("Validasi Error", "Masukkan jam mulai!");
            return false;
        }
        
        if (jamSelesaiField.getText().isEmpty()) {
            showAlert("Validasi Error", "Masukkan jam selesai!");
            return false;
        }
        
        if (ruanganField.getText().isEmpty()) {
            showAlert("Validasi Error", "Masukkan ruangan!");
            return false;
        }
        
        // Validasi format jam
        if (!isValidTimeFormat(jamMulaiField.getText())) {
            showAlert("Validasi Error", "Format jam mulai tidak valid! Gunakan format HH:mm");
            return false;
        }
        
        if (!isValidTimeFormat(jamSelesaiField.getText())) {
            showAlert("Validasi Error", "Format jam selesai tidak valid! Gunakan format HH:mm");
            return false;
        }
        
        return true;
    }
    
    private boolean isValidTimeFormat(String time) {
        return time.matches("^([0-1]?[0-9]|2[0-3]):[0-5][0-9]$");
    }

    private void clearForm() {
        selectedJadwalId = -1;
        mataKuliahComboBox.setValue(null);
        hariComboBox.setValue(null);
        jamMulaiField.clear();
        jamSelesaiField.clear();
        ruanganField.clear();
        
        jadwalTable.getSelectionModel().clearSelection();
        
        btnTambah.setDisable(false);
        btnUpdate.setDisable(true);
        btnHapus.setDisable(true);
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

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
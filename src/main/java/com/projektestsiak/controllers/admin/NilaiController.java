package com.projektestsiak.controllers.admin;

import com.projektestsiak.models.NilaiModel;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

public class NilaiController {

    @FXML private TabPane tabPane;
    
    // Tab Belum Dinilai
    @FXML private TableView<NilaiModel.PengumpulanDetail> tableBelumDinilai;
    @FXML private TableColumn<NilaiModel.PengumpulanDetail, String> colSiswaBelum;
    @FXML private TableColumn<NilaiModel.PengumpulanDetail, String> colMataKuliahBelum;
    @FXML private TableColumn<NilaiModel.PengumpulanDetail, String> colTugasBelum;
    @FXML private TableColumn<NilaiModel.PengumpulanDetail, String> colTanggalBelum;
    
    // Tab Sudah Dinilai
    @FXML private TableView<NilaiModel.PengumpulanDetail> tableSudahDinilai;
    @FXML private TableColumn<NilaiModel.PengumpulanDetail, String> colSiswaSudah;
    @FXML private TableColumn<NilaiModel.PengumpulanDetail, String> colMataKuliahSudah;
    @FXML private TableColumn<NilaiModel.PengumpulanDetail, String> colTugasSudah;
    @FXML private TableColumn<NilaiModel.PengumpulanDetail, Double> colNilaiSudah;
    @FXML private TableColumn<NilaiModel.PengumpulanDetail, String> colTanggalSudah;
    
    // Form Input Nilai
    @FXML private Label lblDetailSiswa;
    @FXML private Label lblDetailMataKuliah;
    @FXML private Label lblDetailTugas;
    @FXML private Label lblDetailDeskripsi;
    @FXML private Label lblDetailTanggal;
    
    @FXML private TextField nilaiField;
    @FXML private TextArea komentarArea;
    
    @FXML private Button btnBeriNilai;
    @FXML private Button btnUpdateNilai;
    @FXML private Button btnHapusNilai;
    @FXML private Button btnLihatFile;
    
    private NilaiModel nilaiModel;
    private ObservableList<NilaiModel.PengumpulanDetail> belumDinilaiList;
    private ObservableList<NilaiModel.PengumpulanDetail> sudahDinilaiList;
    private NilaiModel.PengumpulanDetail selectedPengumpulan;

    @FXML
    public void initialize() {
        try {
            nilaiModel = new NilaiModel();
            
            setupTables();
            loadData();
            setupSelectionListeners();
            clearForm();
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("Error", "Gagal terhubung ke database: " + e.getMessage());
        }
    }

    private void setupTables() {
        // Setup table belum dinilai
        colSiswaBelum.setCellValueFactory(new PropertyValueFactory<>("namaSiswa"));
        colMataKuliahBelum.setCellValueFactory(new PropertyValueFactory<>("mataKuliahNama"));
        colTugasBelum.setCellValueFactory(new PropertyValueFactory<>("judulTugas"));
        colTanggalBelum.setCellValueFactory(new PropertyValueFactory<>("submittedAtFormat"));
        
        belumDinilaiList = FXCollections.observableArrayList();
        tableBelumDinilai.setItems(belumDinilaiList);
        
        // Setup table sudah dinilai
        colSiswaSudah.setCellValueFactory(new PropertyValueFactory<>("namaSiswa"));
        colMataKuliahSudah.setCellValueFactory(new PropertyValueFactory<>("mataKuliahNama"));
        colTugasSudah.setCellValueFactory(new PropertyValueFactory<>("judulTugas"));
        colNilaiSudah.setCellValueFactory(new PropertyValueFactory<>("nilai"));
        colTanggalSudah.setCellValueFactory(new PropertyValueFactory<>("submittedAtFormat"));
        
        sudahDinilaiList = FXCollections.observableArrayList();
        tableSudahDinilai.setItems(sudahDinilaiList);
    }

    private void setupSelectionListeners() {
        // Listener untuk table belum dinilai
        tableBelumDinilai.getSelectionModel().selectedItemProperty().addListener(
            (observable, oldValue, newValue) -> {
                if (newValue != null) {
                    selectedPengumpulan = newValue;
                    fillFormWithSelected(newValue);
                    tableSudahDinilai.getSelectionModel().clearSelection();
                }
            }
        );
        
        // Listener untuk table sudah dinilai
        tableSudahDinilai.getSelectionModel().selectedItemProperty().addListener(
            (observable, oldValue, newValue) -> {
                if (newValue != null) {
                    selectedPengumpulan = newValue;
                    fillFormWithSelected(newValue);
                    tableBelumDinilai.getSelectionModel().clearSelection();
                }
            }
        );
    }

    private void fillFormWithSelected(NilaiModel.PengumpulanDetail pengumpulan) {
        lblDetailSiswa.setText(pengumpulan.getNamaSiswa() + " (" + pengumpulan.getUsername() + ")");
        lblDetailMataKuliah.setText(pengumpulan.getMataKuliahNama());
        lblDetailTugas.setText(pengumpulan.getJudulTugas());
        lblDetailDeskripsi.setText(pengumpulan.getDeskripsiTugas());
        lblDetailTanggal.setText(pengumpulan.getSubmittedAtFormat());
        
        if (pengumpulan.getNilai() != null) {
            nilaiField.setText(String.valueOf(pengumpulan.getNilai()));
            komentarArea.setText(pengumpulan.getKomentar());
            
            btnBeriNilai.setDisable(true);
            btnUpdateNilai.setDisable(false);
            btnHapusNilai.setDisable(false);
        } else {
            nilaiField.clear();
            komentarArea.clear();
            
            btnBeriNilai.setDisable(false);
            btnUpdateNilai.setDisable(true);
            btnHapusNilai.setDisable(true);
        }
        
        if (pengumpulan.getFilePath() != null && !pengumpulan.getFilePath().isEmpty()) {
            btnLihatFile.setDisable(false);
        } else {
            btnLihatFile.setDisable(true);
        }
    }

    @FXML
    private void handleBeriNilai() {
        if (selectedPengumpulan == null) {
            showAlert("Peringatan", "Pilih pengumpulan tugas yang akan dinilai!");
            return;
        }
        
        if (!validateNilaiForm()) return;
        
        try {
            double nilai = Double.parseDouble(nilaiField.getText());
            String komentar = komentarArea.getText();
            
            boolean success = nilaiModel.beriNilai(
                selectedPengumpulan.getId(), nilai, komentar
            );
            
            if (success) {
                showAlert("Sukses", "Nilai berhasil diberikan!");
                clearForm();
                loadData();
            } else {
                showAlert("Error", "Gagal memberikan nilai!");
            }
        } catch (NumberFormatException e) {
            showAlert("Error", "Format nilai tidak valid!");
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Error", "Terjadi kesalahan: " + e.getMessage());
        }
    }

    @FXML
    private void handleUpdateNilai() {
        if (selectedPengumpulan == null) {
            showAlert("Peringatan", "Pilih nilai yang akan diperbarui!");
            return;
        }
        
        if (!validateNilaiForm()) return;
        
        try {
            double nilai = Double.parseDouble(nilaiField.getText());
            String komentar = komentarArea.getText();
            
            boolean success = nilaiModel.updateNilai(
                selectedPengumpulan.getId(), nilai, komentar
            );
            
            if (success) {
                showAlert("Sukses", "Nilai berhasil diperbarui!");
                clearForm();
                loadData();
            } else {
                showAlert("Error", "Gagal memperbarui nilai!");
            }
        } catch (NumberFormatException e) {
            showAlert("Error", "Format nilai tidak valid!");
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Error", "Terjadi kesalahan: " + e.getMessage());
        }
    }

    @FXML
    private void handleHapusNilai() {
        if (selectedPengumpulan == null) {
            showAlert("Peringatan", "Pilih nilai yang akan dihapus!");
            return;
        }
        
        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
        confirmation.setTitle("Konfirmasi Hapus Nilai");
        confirmation.setHeaderText(null);
        confirmation.setContentText("Hapus nilai untuk tugas " + selectedPengumpulan.getJudulTugas() + 
                                  " dari " + selectedPengumpulan.getNamaSiswa() + "?");
        
        if (confirmation.showAndWait().get() == ButtonType.OK) {
            boolean success = nilaiModel.hapusNilai(selectedPengumpulan.getId());
            
            if (success) {
                showAlert("Sukses", "Nilai berhasil dihapus!");
                clearForm();
                loadData();
            } else {
                showAlert("Error", "Gagal menghapus nilai!");
            }
        }
    }

    @FXML
    private void handleLihatFile() {
        if (selectedPengumpulan == null || selectedPengumpulan.getFilePath() == null) {
            showAlert("Peringatan", "Tidak ada file tugas!");
            return;
        }
        
        String filePath = selectedPengumpulan.getFilePath();
        File file = new File(filePath);
        
        if (!file.exists()) {
            showAlert("Error", "File tugas tidak ditemukan!");
            return;
        }
        
        try {
            // Buka file dengan aplikasi default sistem
            java.awt.Desktop.getDesktop().open(file);
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Error", "Gagal membuka file: " + e.getMessage());
        }
    }

    @FXML
    private void handleClear() {
        clearForm();
        tableBelumDinilai.getSelectionModel().clearSelection();
        tableSudahDinilai.getSelectionModel().clearSelection();
    }

    @FXML
    private void handleBack() {
        loadScene("/fxml/admin/admin_dashboard.fxml");
    }

    @FXML
    private void handleRefresh() {
        loadData();
        clearForm();
    }

    private void loadData() {
        // Load data belum dinilai
        List<NilaiModel.PengumpulanDetail> belumDinilai = nilaiModel.getPengumpulanBelumDinilai();
        belumDinilaiList.setAll(belumDinilai);
        
        // Load data sudah dinilai
        List<NilaiModel.PengumpulanDetail> sudahDinilai = nilaiModel.getPengumpulanSudahDinilai();
        sudahDinilaiList.setAll(sudahDinilai);
    }

    private boolean validateNilaiForm() {
        if (nilaiField.getText().isEmpty()) {
            showAlert("Validasi Error", "Masukkan nilai!");
            return false;
        }
        
        try {
            double nilai = Double.parseDouble(nilaiField.getText());
            if (nilai < 0 || nilai > 100) {
                showAlert("Validasi Error", "Nilai harus antara 0-100!");
                return false;
            }
        } catch (NumberFormatException e) {
            showAlert("Validasi Error", "Format nilai tidak valid!");
            return false;
        }
        
        if (komentarArea.getText().isEmpty()) {
            showAlert("Validasi Error", "Masukkan komentar!");
            return false;
        }
        
        return true;
    }

    private void clearForm() {
        selectedPengumpulan = null;
        
        lblDetailSiswa.setText("-");
        lblDetailMataKuliah.setText("-");
        lblDetailTugas.setText("-");
        lblDetailDeskripsi.setText("-");
        lblDetailTanggal.setText("-");
        
        nilaiField.clear();
        komentarArea.clear();
        
        btnBeriNilai.setDisable(true);
        btnUpdateNilai.setDisable(true);
        btnHapusNilai.setDisable(true);
        btnLihatFile.setDisable(true);
    }

    private void loadScene(String fxmlPath) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent root = loader.load();
            Stage stage = (Stage) tabPane.getScene().getWindow();
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
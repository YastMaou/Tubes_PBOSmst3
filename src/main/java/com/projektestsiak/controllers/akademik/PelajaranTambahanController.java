package com.projektestsiak.controllers.akademik;

import com.projektestsiak.models.MataKuliah;
import com.projektestsiak.models.MataKuliahModel;
import com.projektestsiak.models.PelajaranTambahanModel;
import com.projektestsiak.models.SessionManager;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

public class PelajaranTambahanController {

    @FXML
    private ComboBox<MataKuliah> mataKuliahComboBox;

    @FXML
    private Label sksLabel;

    @FXML
    private Label biayaLabel;

    @FXML
    private TextField buktiPembayaranField;

    @FXML
    private TableView<MataKuliah> registeredTable;

    @FXML
    private TableColumn<MataKuliah, String> colKode;

    @FXML
    private TableColumn<MataKuliah, String> colNama;

    @FXML
    private TableColumn<MataKuliah, Integer> colSKS;

    @FXML
    private TableColumn<MataKuliah, String> colDosen;

    @FXML
    private Label totalSKSLabel;

    private MataKuliahModel mataKuliahModel;
    private PelajaranTambahanModel pelajaranTambahanModel;
    private ObservableList<MataKuliah> availableMataKuliah;
    private ObservableList<MataKuliah> registeredMataKuliah;

    @FXML
    public void initialize() {
        try {
            mataKuliahModel = new MataKuliahModel();
            pelajaranTambahanModel = new PelajaranTambahanModel();
            setupComboBox();
            setupTable();
            loadRegisteredMataKuliah();
            updateTotalSKS();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void setupComboBox() {
        availableMataKuliah = FXCollections.observableArrayList();
        List<MataKuliah> availableList = mataKuliahModel.getAvailableMataKuliahForStudent(
            SessionManager.getCurrentUserId()
        );
        availableMataKuliah.setAll(availableList);
        
        mataKuliahComboBox.setItems(availableMataKuliah);
        
        mataKuliahComboBox.setOnAction(e -> updateBiayaInfo());
    }

    private void setupTable() {
        colKode.setCellValueFactory(new PropertyValueFactory<>("kode"));
        colNama.setCellValueFactory(new PropertyValueFactory<>("nama"));
        colSKS.setCellValueFactory(new PropertyValueFactory<>("sks"));
        colDosen.setCellValueFactory(new PropertyValueFactory<>("dosen"));
        
        registeredMataKuliah = FXCollections.observableArrayList();
        registeredTable.setItems(registeredMataKuliah);
    }

    private void loadRegisteredMataKuliah() {
        List<MataKuliah> registeredList = pelajaranTambahanModel.getPelajaranTambahanByUser(
            SessionManager.getCurrentUserId()
        );
        registeredMataKuliah.setAll(registeredList);
    }

    private void updateBiayaInfo() {
        MataKuliah selected = mataKuliahComboBox.getValue();
        if (selected != null) {
            sksLabel.setText(selected.getSks() + " SKS");
            biayaLabel.setText("Rp " + String.format("%,.0f", selected.getTotalBiaya()));
        } else {
            sksLabel.setText("0 SKS");
            biayaLabel.setText("Rp 0");
        }
    }

    private void updateTotalSKS() {
        int totalSKS = pelajaranTambahanModel.getTotalSKSRegistered(SessionManager.getCurrentUserId());
        totalSKSLabel.setText("Total SKS Terdaftar: " + totalSKS + " SKS");
    }

    @FXML
    private void handleBrowseBukti() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Pilih Bukti Pembayaran");
        
        FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter(
            "Image Files", "*.png", "*.jpg", "*.jpeg", "*.pdf");
        fileChooser.getExtensionFilters().add(extFilter);
        
        File file = fileChooser.showOpenDialog(buktiPembayaranField.getScene().getWindow());
        if (file != null) {
            buktiPembayaranField.setText(file.getAbsolutePath());
        }
    }

    @FXML
    private void handleDaftar() {
        MataKuliah selectedMataKuliah = mataKuliahComboBox.getValue();
        String buktiPembayaran = buktiPembayaranField.getText();
        
        if (selectedMataKuliah == null) {
            showAlert("Error", "Pilih mata kuliah terlebih dahulu!");
            return;
        }
        
        if (buktiPembayaran.isEmpty()) {
            showAlert("Error", "Pilih bukti pembayaran terlebih dahulu!");
            return;
        }

        try {
            boolean success = pelajaranTambahanModel.pilihMataKuliah(
                SessionManager.getCurrentUserId(),
                selectedMataKuliah.getId(),
                buktiPembayaran
            );
            
            if (success) {
                showAlert("Success", "Pendaftaran berhasil! Menunggu persetujuan admin.");
                resetForm();
                loadRegisteredMataKuliah();
                updateTotalSKS();
            } else {
                showAlert("Error", "Gagal mendaftar mata kuliah!");
            }
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Error", "Terjadi kesalahan: " + e.getMessage());
        }
    }

    @FXML
    private void handleBack() {
        loadScene("/fxml/akademik/dashboard.fxml");
    }

    @FXML
    private void handleRefresh() {
        loadRegisteredMataKuliah();
        updateTotalSKS();
    }

    private void resetForm() {
        mataKuliahComboBox.setValue(null);
        buktiPembayaranField.clear();
        sksLabel.setText("0 SKS");
        biayaLabel.setText("Rp 0");
    }

    private void loadScene(String fxmlPath) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent root = loader.load();
            Stage stage = (Stage) mataKuliahComboBox.getScene().getWindow();
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
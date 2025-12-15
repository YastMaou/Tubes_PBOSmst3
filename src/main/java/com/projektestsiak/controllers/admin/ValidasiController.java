package com.projektestsiak.controllers.admin;

import com.projektestsiak.models.ValidasiModel;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

public class ValidasiController {

    @FXML private TableView<ValidasiModel.PendaftaranDetail> pendaftaranTable;
    @FXML private TableColumn<ValidasiModel.PendaftaranDetail, String> colSiswa;
    @FXML private TableColumn<ValidasiModel.PendaftaranDetail, String> colMataKuliah;
    @FXML private TableColumn<ValidasiModel.PendaftaranDetail, Integer> colSKS;
    @FXML private TableColumn<ValidasiModel.PendaftaranDetail, String> colBiaya;
    @FXML private TableColumn<ValidasiModel.PendaftaranDetail, String> colStatus;
    
    @FXML private Label lblDetailSiswa;
    @FXML private Label lblDetailMataKuliah;
    @FXML private Label lblDetailSKS;
    @FXML private Label lblDetailBiaya;
    @FXML private ImageView imageViewBukti;
    
    @FXML private Button btnApprove;
    @FXML private Button btnReject;
    @FXML private Button btnLihatBukti;
    
    private ValidasiModel validasiModel;
    private ObservableList<ValidasiModel.PendaftaranDetail> pendaftaranList;
    private ValidasiModel.PendaftaranDetail selectedPendaftaran;

    @FXML
    public void initialize() {
        try {
            validasiModel = new ValidasiModel();
            setupTable();
            loadData();
            setupSelectionListener();
            updateDetailPanel(null);
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("Error", "Gagal terhubung ke database: " + e.getMessage());
        }
    }

    private void setupTable() {
        colSiswa.setCellValueFactory(new PropertyValueFactory<>("namaSiswa"));
        colMataKuliah.setCellValueFactory(new PropertyValueFactory<>("namaMataKuliah"));
        colSKS.setCellValueFactory(new PropertyValueFactory<>("sks"));
        colBiaya.setCellValueFactory(cellData -> 
            new javafx.beans.property.SimpleStringProperty(
                String.format("Rp %,.0f", cellData.getValue().getTotalBiaya())
            )
        );
        colStatus.setCellValueFactory(new PropertyValueFactory<>("status"));
        
        pendaftaranList = FXCollections.observableArrayList();
        pendaftaranTable.setItems(pendaftaranList);
    }

    private void setupSelectionListener() {
        pendaftaranTable.getSelectionModel().selectedItemProperty().addListener(
            (observable, oldValue, newValue) -> {
                selectedPendaftaran = newValue;
                updateDetailPanel(newValue);
            }
        );
    }

    private void updateDetailPanel(ValidasiModel.PendaftaranDetail pendaftaran) {
        if (pendaftaran == null) {
            lblDetailSiswa.setText("-");
            lblDetailMataKuliah.setText("-");
            lblDetailSKS.setText("-");
            lblDetailBiaya.setText("-");
            imageViewBukti.setImage(null);
            
            btnApprove.setDisable(true);
            btnReject.setDisable(true);
            btnLihatBukti.setDisable(true);
        } else {
            lblDetailSiswa.setText(pendaftaran.getNamaSiswa() + " (" + pendaftaran.getUsername() + ")");
            lblDetailMataKuliah.setText(pendaftaran.getKodeMataKuliah() + " - " + pendaftaran.getNamaMataKuliah());
            lblDetailSKS.setText(String.valueOf(pendaftaran.getSks()) + " SKS");
            lblDetailBiaya.setText(String.format("Rp %,.0f", pendaftaran.getTotalBiaya()));
            
            // Load bukti pembayaran jika ada
            String buktiPath = pendaftaran.getBuktiPembayaran();
            if (buktiPath != null && !buktiPath.isEmpty()) {
                btnLihatBukti.setDisable(false);
            } else {
                btnLihatBukti.setDisable(true);
            }
            
            btnApprove.setDisable(false);
            btnReject.setDisable(false);
        }
    }

    @FXML
    private void handleApprove() {
        if (selectedPendaftaran == null) {
            showAlert("Peringatan", "Pilih pendaftaran yang akan disetujui!");
            return;
        }
        
        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
        confirmation.setTitle("Konfirmasi Persetujuan");
        confirmation.setHeaderText(null);
        confirmation.setContentText("Setujui pendaftaran " + selectedPendaftaran.getNamaSiswa() + 
                                  " untuk mata kuliah " + selectedPendaftaran.getNamaMataKuliah() + "?");
        
        if (confirmation.showAndWait().get() == ButtonType.OK) {
            boolean success = validasiModel.approvePendaftaran(selectedPendaftaran.getId());
            
            if (success) {
                showAlert("Sukses", "Pendaftaran berhasil disetujui!");
                loadData();
                updateDetailPanel(null);
            } else {
                showAlert("Error", "Gagal menyetujui pendaftaran!");
            }
        }
    }

    @FXML
    private void handleReject() {
        if (selectedPendaftaran == null) {
            showAlert("Peringatan", "Pilih pendaftaran yang akan ditolak!");
            return;
        }
        
        // Dialog untuk input alasan penolakan
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Penolakan Pendaftaran");
        dialog.setHeaderText("Alasan Penolakan");
        dialog.setContentText("Masukkan alasan penolakan:");
        
        dialog.showAndWait().ifPresent(alasan -> {
            if (!alasan.trim().isEmpty()) {
                Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
                confirmation.setTitle("Konfirmasi Penolakan");
                confirmation.setHeaderText(null);
                confirmation.setContentText("Tolak pendaftaran " + selectedPendaftaran.getNamaSiswa() + 
                                          " untuk mata kuliah " + selectedPendaftaran.getNamaMataKuliah() + "?");
                
                if (confirmation.showAndWait().get() == ButtonType.OK) {
                    boolean success = validasiModel.rejectPendaftaran(selectedPendaftaran.getId(), alasan);
                    
                    if (success) {
                        showAlert("Sukses", "Pendaftaran berhasil ditolak!");
                        loadData();
                        updateDetailPanel(null);
                    } else {
                        showAlert("Error", "Gagal menolak pendaftaran!");
                    }
                }
            }
        });
    }

    @FXML
    private void handleLihatBukti() {
        if (selectedPendaftaran == null || selectedPendaftaran.getBuktiPembayaran() == null) {
            showAlert("Peringatan", "Tidak ada bukti pembayaran!");
            return;
        }
        
        String buktiPath = selectedPendaftaran.getBuktiPembayaran();
        File file = new File(buktiPath);
        
        if (!file.exists()) {
            showAlert("Error", "File bukti pembayaran tidak ditemukan!");
            return;
        }
        
        try {
            // Tampilkan gambar dalam dialog
            Stage imageStage = new Stage();
            ImageView imageView = new ImageView(new Image(file.toURI().toString()));
            imageView.setPreserveRatio(true);
            imageView.setFitWidth(600);
            
            ScrollPane scrollPane = new ScrollPane(imageView);
            scrollPane.setFitToWidth(true);
            scrollPane.setFitToHeight(true);
            
            Scene scene = new Scene(scrollPane, 600, 400);
            imageStage.setTitle("Bukti Pembayaran - " + selectedPendaftaran.getNamaSiswa());
            imageStage.setScene(scene);
            imageStage.show();
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Error", "Gagal membuka bukti pembayaran: " + e.getMessage());
        }
    }

    @FXML
    private void handleBack() {
        loadScene("/fxml/admin/admin_dashboard.fxml");
    }

    @FXML
    private void handleRefresh() {
        loadData();
        updateDetailPanel(null);
    }

    private void loadData() {
        List<ValidasiModel.PendaftaranDetail> allPendaftaran = validasiModel.getPendaftaranPending();
        pendaftaranList.setAll(allPendaftaran);
    }

    private void loadScene(String fxmlPath) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent root = loader.load();
            Stage stage = (Stage) pendaftaranTable.getScene().getWindow();
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
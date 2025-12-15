package com.projektestsiak.controllers.akademik;

import com.projektestsiak.models.Tugas;
import com.projektestsiak.models.TugasModel;
import com.projektestsiak.models.PengumpulanTugasModel;
import com.projektestsiak.models.SessionManager;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.net.URL;
import java.sql.SQLException;
import java.util.ResourceBundle;

public class PengumpulanTugasController implements Initializable {

    @FXML
    private Label mataKuliahLabel;

    @FXML
    private Label judulLabel;

    @FXML
    private Label deadlineLabel;

    @FXML
    private TextArea deskripsiArea;

    @FXML
    private TextField filePathField;

    private int tugasId;
    private TugasModel tugasModel;
    private PengumpulanTugasModel pengumpulanModel;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        try {
            tugasModel = new TugasModel();
            pengumpulanModel = new PengumpulanTugasModel();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void setTugasId(int tugasId) {
        this.tugasId = tugasId;
        loadTugasData();
    }

    private void loadTugasData() {
        try {
            Tugas tugas = tugasModel.getTugasById(tugasId);
            if (tugas != null) {
                mataKuliahLabel.setText(tugas.getMataKuliahNama());
                judulLabel.setText(tugas.getJudul());
                deadlineLabel.setText(tugas.getDeadlineFormat());
                deskripsiArea.setText(tugas.getDeskripsi());
                deskripsiArea.setEditable(false);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleBrowseFile() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Pilih File Tugas");
        
        // Set extension filters
        FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter(
            "All Files (*.*)", "*.*");
        fileChooser.getExtensionFilters().add(extFilter);
        
        File file = fileChooser.showOpenDialog(filePathField.getScene().getWindow());
        if (file != null) {
            filePathField.setText(file.getAbsolutePath());
        }
    }

    @FXML
    private void handleSubmit() {
        String filePath = filePathField.getText();
        
        if (filePath.isEmpty()) {
            showAlert("Error", "Pilih file tugas terlebih dahulu!");
            return;
        }

        try {
            boolean success = pengumpulanModel.kumpulkanTugas(
                tugasId, 
                SessionManager.getCurrentUserId(), 
                filePath
            );
            
            if (success) {
                showAlert("Success", "Tugas berhasil dikumpulkan!");
                handleBack();
            } else {
                showAlert("Error", "Gagal mengumpulkan tugas!");
            }
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Error", "Terjadi kesalahan: " + e.getMessage());
        }
    }

    @FXML
    private void handleBack() {
        try {
            Stage stage = (Stage) filePathField.getScene().getWindow();
            stage.close();
        } catch (Exception e) {
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
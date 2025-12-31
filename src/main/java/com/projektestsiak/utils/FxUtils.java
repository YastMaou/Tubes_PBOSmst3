package com.projektestsiak.utils;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.image.Image;
import javafx.stage.FileChooser;
import javafx.stage.Modality;

import java.io.File;
import java.io.IOException;
import java.util.Optional;

public class FxUtils {
    
    // Method untuk load scene dengan ukuran standar
    public static void loadScene(Stage stage, String fxmlPath, String title) throws IOException {
        loadScene(stage, fxmlPath, title, 1200, 700);
    }
    
    // Method untuk load scene dengan ukuran custom
    public static void loadScene(Stage stage, String fxmlPath, String title, int width, int height) throws IOException {
        FXMLLoader loader = new FXMLLoader(FxUtils.class.getResource(fxmlPath));
        Parent root = loader.load();
        stage.setScene(new Scene(root, width, height));
        stage.setTitle("Sistem Akademik Sekolah - " + title);
        stage.centerOnScreen();
    }
    
    // Method untuk load scene dari class tertentu
    public static void loadSceneFromClass(Class<?> clazz, Stage stage, String fxmlPath, String title) throws IOException {
        FXMLLoader loader = new FXMLLoader(clazz.getResource(fxmlPath));
        Parent root = loader.load();
        stage.setScene(new Scene(root, 1200, 700));
        stage.setTitle("Sistem Akademik Sekolah - " + title);
        stage.centerOnScreen();
    }
    
    // Method untuk show alert
    public static void showAlert(String title, String message, Alert.AlertType alertType) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        
        // Tambahkan icon berdasarkan tipe alert
        switch (alertType) {
            case ERROR -> alert.setGraphic(new javafx.scene.control.Label("⛔"));
            case WARNING -> alert.setGraphic(new javafx.scene.control.Label("⚠️"));
            case INFORMATION -> alert.setGraphic(new javafx.scene.control.Label("ℹ️"));
            case CONFIRMATION -> alert.setGraphic(new javafx.scene.control.Label("❓"));
        }
        
        alert.showAndWait();
    }
    
    // Helper methods untuk tipe alert spesifik
    public static void showInfoAlert(String title, String message) {
        showAlert(title, message, Alert.AlertType.INFORMATION);
    }
    
    public static void showErrorAlert(String title, String message) {
        showAlert(title, message, Alert.AlertType.ERROR);
    }
    
    public static void showWarningAlert(String title, String message) {
        showAlert(title, message, Alert.AlertType.WARNING);
    }
    
    public static Optional<ButtonType> showConfirmationAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.setGraphic(new javafx.scene.control.Label("❓"));
        return alert.showAndWait();
    }
    
    // File chooser untuk bukti pembayaran (images dan PDF)
    public static File showFileChooser(Stage stage, String title, String... extensions) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle(title);
        
        if (extensions.length > 0) {
            FileChooser.ExtensionFilter extFilter;
            if (extensions[0].equals("*")) {
                extFilter = new FileChooser.ExtensionFilter("All Files", "*.*");
            } else {
                StringBuilder description = new StringBuilder();
                for (String ext : extensions) {
                    description.append("*.").append(ext).append(", ");
                }
                description.setLength(description.length() - 2); // Remove last comma
                extFilter = new FileChooser.ExtensionFilter("Supported Files", extensions);
            }
            fileChooser.getExtensionFilters().add(extFilter);
        }
        
        return fileChooser.showOpenDialog(stage);
    }
    
    // File chooser khusus untuk gambar
    public static File showImageChooser(Stage stage) {
        return showFileChooser(stage, "Pilih Gambar", 
            "*.png", "*.jpg", "*.jpeg", "*.gif", "*.bmp");
    }
    
    // File chooser khusus untuk PDF
    public static File showPdfChooser(Stage stage) {
        return showFileChooser(stage, "Pilih PDF", "*.pdf");
    }
    
    // File chooser untuk semua file
    public static File showAnyFileChooser(Stage stage) {
        return showFileChooser(stage, "Pilih File", "*");
    }
    
    // Validasi input tidak kosong
    public static boolean validateNotEmpty(String value, String fieldName) {
        if (value == null || value.trim().isEmpty()) {
            showErrorAlert("Validasi Error", fieldName + " tidak boleh kosong!");
            return false;
        }
        return true;
    }
    
    // Validasi angka
    public static boolean validateNumber(String value, String fieldName) {
        if (!validateNotEmpty(value, fieldName)) {
            return false;
        }
        
        try {
            Double.parseDouble(value);
            return true;
        } catch (NumberFormatException e) {
            showErrorAlert("Validasi Error", fieldName + " harus berupa angka!");
            return false;
        }
    }
    
    // Validasi angka dalam range
    public static boolean validateNumberInRange(String value, String fieldName, double min, double max) {
        if (!validateNumber(value, fieldName)) {
            return false;
        }
        
        double num = Double.parseDouble(value);
        if (num < min || num > max) {
            showErrorAlert("Validasi Error", 
                fieldName + " harus antara " + min + " dan " + max + "!");
            return false;
        }
        return true;
    }
    
    // Validasi format waktu (HH:mm)
    public static boolean validateTimeFormat(String time, String fieldName) {
        if (!validateNotEmpty(time, fieldName)) {
            return false;
        }
        
        if (!time.matches("^([0-1]?[0-9]|2[0-3]):[0-5][0-9]$")) {
            showErrorAlert("Validasi Error", 
                fieldName + " format tidak valid! Gunakan format HH:mm (contoh: 08:30)");
            return false;
        }
        return true;
    }
    
    // Format currency (Rp)
    public static String formatCurrency(double amount) {
        return String.format("Rp %,d", (int) amount);
    }
    
    // Format persentase
    public static String formatPercentage(double value) {
        return String.format("%.1f%%", value * 100);
    }
    
    // Buat stage modal (popup)
    public static Stage createModalStage(String title, int width, int height) {
        Stage modalStage = new Stage();
        modalStage.setTitle(title);
        modalStage.initModality(Modality.APPLICATION_MODAL);
        modalStage.setResizable(false);
        modalStage.setWidth(width);
        modalStage.setHeight(height);
        return modalStage;
    }
    
    // Set icon untuk stage
    public static void setStageIcon(Stage stage) {
        try {
            Image icon = new Image(FxUtils.class.getResourceAsStream("/images/app-icon.png"));
            stage.getIcons().add(icon);
        } catch (Exception e) {
            // Icon tidak ditemukan, skip saja
            System.out.println("App icon not found, using default.");
        }
    }
    
    // Generate filename yang unik untuk upload
    public static String generateUniqueFilename(String originalFilename) {
        String timestamp = String.valueOf(System.currentTimeMillis());
        String extension = "";
        
        int dotIndex = originalFilename.lastIndexOf('.');
        if (dotIndex > 0) {
            extension = originalFilename.substring(dotIndex);
        }
        
        return timestamp + "_" + originalFilename.replace(extension, "") + extension;
    }
    
    // Truncate text jika terlalu panjang
    public static String truncateText(String text, int maxLength) {
        if (text == null) return "";
        if (text.length() <= maxLength) return text;
        return text.substring(0, maxLength - 3) + "...";
    }
    
    // Capitalize first letter
    public static String capitalize(String text) {
        if (text == null || text.isEmpty()) return text;
        return text.substring(0, 1).toUpperCase() + text.substring(1).toLowerCase();
    }
    
    // Clean filename (remove illegal characters)
    public static String cleanFilename(String filename) {
        return filename.replaceAll("[^a-zA-Z0-9.-]", "_");
    }
}
package com.projektestsiak.controllers.admin;

import com.projektestsiak.models.SessionManager;
import com.projektestsiak.models.DatabaseConnection;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.Alert;
import javafx.scene.control.ProgressBar;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class AdminDashboardController {

    @FXML private Label welcomeLabel;
    @FXML private Label currentTimeLabel;
    @FXML private Label lastLoginLabel;
    
    // Statistik Cards
    @FXML private Label statsUsersLabel;
    @FXML private Label statsCoursesLabel;
    @FXML private Label statsPendingLabel;
    @FXML private Label statsUngradedLabel;
    @FXML private Label statsActiveLabel;
    @FXML private Label statsInactiveLabel;
    
    // Progress Bars
    @FXML private ProgressBar progressUsers;
    @FXML private ProgressBar progressCourses;
    @FXML private ProgressBar progressPending;
    @FXML private ProgressBar progressUngraded;
    
    // Quick Stats
    @FXML private Label quickTotalUsers;
    @FXML private Label quickTotalCourses;
    @FXML private Label quickTodayLogin;
    @FXML private Label quickTodaySubmissions;

    @FXML
    public void initialize() {
        welcomeLabel.setText("Selamat Datang, " + SessionManager.getCurrentNama());
        updateCurrentTime();
        loadStatistics();
        loadQuickStats();
        startClock();
    }
    
    private void updateCurrentTime() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("EEEE, dd MMMM yyyy HH:mm:ss");
        currentTimeLabel.setText(LocalDateTime.now().format(formatter));
        
        // Set last login (simulasi - bisa diambil dari database)
        lastLoginLabel.setText("Terakhir login: " + 
            LocalDateTime.now().minusHours(2).format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")));
    }
    
    private void startClock() {
        Thread clockThread = new Thread(() -> {
            try {
                while (true) {
                    Thread.sleep(1000);
                    javafx.application.Platform.runLater(() -> updateCurrentTime());
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        });
        clockThread.setDaemon(true);
        clockThread.start();
    }

    private void loadStatistics() {
        try {
            DatabaseConnection db = new DatabaseConnection();
            Connection conn = db.getConnection();
            
            // 1. Hitung jumlah user total dan aktif
            String sqlUsers = "SELECT " +
                "COUNT(*) as total, " +
                "SUM(CASE WHEN created_at >= DATE_SUB(NOW(), INTERVAL 30 DAY) THEN 1 ELSE 0 END) as active " +
                "FROM users WHERE role = 'student'";
            
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(sqlUsers);
            if (rs.next()) {
                int totalUsers = rs.getInt("total");
                int activeUsers = rs.getInt("active");
                int inactiveUsers = totalUsers - activeUsers;
                
                statsUsersLabel.setText(totalUsers + " Siswa");
                statsActiveLabel.setText(activeUsers + " Aktif");
                statsInactiveLabel.setText(inactiveUsers + " Tidak Aktif");
                
                // Update progress bar (maksimum 100 siswa sebagai baseline)
                progressUsers.setProgress(Math.min(totalUsers / 100.0, 1.0));
            }
            
            // 2. Hitung jumlah mata pelajaran
            String sqlCourses = "SELECT COUNT(*) as total FROM mata_kuliah";
            rs = stmt.executeQuery(sqlCourses);
            if (rs.next()) {
                int totalCourses = rs.getInt("total");
                statsCoursesLabel.setText(totalCourses + " Mata Pelajaran");
                
                // Update progress bar (maksimum 50 mata pelajaran)
                progressCourses.setProgress(Math.min(totalCourses / 50.0, 1.0));
            }
            
            // 3. Hitung pendaftaran pending
            String sqlPending = "SELECT COUNT(*) as total FROM pelajaran_tambahan WHERE status = 'pending'";
            rs = stmt.executeQuery(sqlPending);
            if (rs.next()) {
                int totalPending = rs.getInt("total");
                statsPendingLabel.setText(totalPending + " Pending");
                
                // Update progress bar (maksimum 20 pending)
                progressPending.setProgress(Math.min(totalPending / 20.0, 1.0));
            }
            
            // 4. Hitung tugas belum dinilai
            String sqlUngraded = "SELECT COUNT(DISTINCT pt.id) as total FROM pengumpulan_tugas pt WHERE pt.nilai IS NULL";
            rs = stmt.executeQuery(sqlUngraded);
            if (rs.next()) {
                int totalUngraded = rs.getInt("total");
                statsUngradedLabel.setText(totalUngraded + " Belum Dinilai");
                
                // Update progress bar (maksimum 30 tugas)
                progressUngraded.setProgress(Math.min(totalUngraded / 30.0, 1.0));
            }
            
            stmt.close();
            conn.close();
            
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("Error", "Gagal memuat statistik: " + e.getMessage());
        }
    }
    
    private void loadQuickStats() {
        try {
            DatabaseConnection db = new DatabaseConnection();
            Connection conn = db.getConnection();
            
            // 1. Total user
            String sqlTotalUsers = "SELECT COUNT(*) as total FROM users";
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(sqlTotalUsers);
            if (rs.next()) {
                quickTotalUsers.setText(String.valueOf(rs.getInt("total")));
            }
            
            // 2. Total courses
            String sqlTotalCourses = "SELECT COUNT(*) as total FROM mata_kuliah";
            rs = stmt.executeQuery(sqlTotalCourses);
            if (rs.next()) {
                quickTotalCourses.setText(String.valueOf(rs.getInt("total")));
            }
            
            // 3. Login hari ini (simulasi)
            String sqlTodayLogin = "SELECT COUNT(*) as total FROM users WHERE DATE(created_at) = CURDATE()";
            rs = stmt.executeQuery(sqlTodayLogin);
            if (rs.next()) {
                quickTodayLogin.setText(String.valueOf(rs.getInt("total")));
            }
            
            // 4. Submission hari ini
            String sqlTodaySubmissions = "SELECT COUNT(*) as total FROM pengumpulan_tugas WHERE DATE(submitted_at) = CURDATE()";
            rs = stmt.executeQuery(sqlTodaySubmissions);
            if (rs.next()) {
                quickTodaySubmissions.setText(String.valueOf(rs.getInt("total")));
            }
            
            stmt.close();
            conn.close();
            
        } catch (SQLException e) {
            e.printStackTrace();
            quickTotalUsers.setText("0");
            quickTotalCourses.setText("0");
            quickTodayLogin.setText("0");
            quickTodaySubmissions.setText("0");
        }
    }

    @FXML
    private void handleLogout() {
        SessionManager.logout();
        loadScene("/fxml/auth/login.fxml");
    }

    @FXML
    private void handleManageUsers() {
        showAlert("Info", "Fitur Manajemen User akan segera hadir!");
    }

    @FXML
    private void handleManageCourses() {
        showAlert("Info", "Fitur Manajemen Mata Pelajaran akan segera hadir!");
    }

    @FXML
    private void handleManageJadwal() {
        loadScene("/fxml/admin/jadwal_admin.fxml");
    }

    @FXML
    private void handleValidasiPendaftaran() {
        loadScene("/fxml/admin/validasi_pendaftaran.fxml");
    }

    @FXML
    private void handlePenilaianTugas() {
        loadScene("/fxml/admin/nilai_tugas.fxml");
    }

    @FXML
    private void handleReports() {
        showAlert("Info", "Fitur Laporan akan segera hadir!");
    }

    @FXML
    private void handleSettings() {
        showAlert("Info", "Fitur Pengaturan akan segera hadir!");
    }

    @FXML
    private void handleRefreshStats() {
        loadStatistics();
        loadQuickStats();
        showAlert("Info", "Statistik berhasil diperbarui!");
    }

    private void loadScene(String fxmlPath) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent root = loader.load();
            Stage stage = (Stage) welcomeLabel.getScene().getWindow();
            stage.setScene(new Scene(root, 1200, 700));
            stage.setTitle("Sistem Akademik Sekolah - " + getPageTitle(fxmlPath));
        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Error", "Gagal memuat halaman: " + e.getMessage());
        }
    }

    private String getPageTitle(String fxmlPath) {
        switch (fxmlPath) {
            case "/fxml/admin/jadwal_admin.fxml":
                return "Manajemen Jadwal";
            case "/fxml/admin/validasi_pendaftaran.fxml":
                return "Validasi Pendaftaran";
            case "/fxml/admin/nilai_tugas.fxml":
                return "Penilaian Tugas";
            case "/fxml/auth/login.fxml":
                return "Login";
            default:
                return "Dashboard Admin";
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
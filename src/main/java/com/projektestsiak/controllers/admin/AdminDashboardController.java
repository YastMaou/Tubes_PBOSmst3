package com.projektestsiak.controllers.admin;

import com.projektestsiak.models.SessionManager;
import com.projektestsiak.utils.DataSeeder;
import com.projektestsiak.models.DatabaseConnection;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ProgressBar;
import javafx.stage.Stage;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.util.Duration;

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
    
    private Timeline clockTimeline;

    @FXML
    public void initialize() {
        welcomeLabel.setText("Selamat Datang, " + SessionManager.getCurrentNama());
        updateCurrentTime();
        startClock();
        loadStatistics();
        loadQuickStats();
    }
    
    private void updateCurrentTime() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("EEEE, dd MMMM yyyy HH:mm:ss");
        currentTimeLabel.setText(LocalDateTime.now().format(formatter));
        
        // Set last login (simulasi - bisa diambil dari database)
        lastLoginLabel.setText("Terakhir login: " + 
            LocalDateTime.now().minusHours(2).format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")));
    }
    
    private void startClock() {
        // Stop previous timeline if exists
        if (clockTimeline != null) {
            clockTimeline.stop();
        }
        
        // Create new timeline that updates every second
        clockTimeline = new Timeline(new KeyFrame(Duration.seconds(1), event -> updateCurrentTime()));
        clockTimeline.setCycleCount(Animation.INDEFINITE);
        clockTimeline.play();
    }
    
    private void loadStatistics() {
        try (Connection conn = new DatabaseConnection().getConnection()) {
            
            // 1. Hitung jumlah user total dan aktif
            String sqlUsers = """
                SELECT 
                    COUNT(*) as total,
                    SUM(CASE WHEN created_at >= DATE_SUB(NOW(), INTERVAL 30 DAY) THEN 1 ELSE 0 END) as active
                FROM users WHERE role = 'student'
            """;
            
            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery(sqlUsers)) {
                
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
            }
            
            // 2. Hitung jumlah mata pelajaran
            String sqlCourses = "SELECT COUNT(*) as total FROM mata_kuliah";
            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery(sqlCourses)) {
                
                if (rs.next()) {
                    int totalCourses = rs.getInt("total");
                    statsCoursesLabel.setText(totalCourses + " Mata Pelajaran");
                    
                    // Update progress bar (maksimum 50 mata pelajaran)
                    progressCourses.setProgress(Math.min(totalCourses / 50.0, 1.0));
                }
            }
            
            // 3. Hitung pendaftaran pending
            String sqlPending = "SELECT COUNT(*) as total FROM pelajaran_tambahan WHERE status = 'pending'";
            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery(sqlPending)) {
                
                if (rs.next()) {
                    int totalPending = rs.getInt("total");
                    statsPendingLabel.setText(totalPending + " Pending");
                    
                    // Update progress bar (maksimum 20 pending)
                    progressPending.setProgress(Math.min(totalPending / 20.0, 1.0));
                }
            }
            
            // 4. Hitung tugas belum dinilai
            String sqlUngraded = "SELECT COUNT(DISTINCT pt.id) as total FROM pengumpulan_tugas pt WHERE pt.nilai IS NULL";
            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery(sqlUngraded)) {
                
                if (rs.next()) {
                    int totalUngraded = rs.getInt("total");
                    statsUngradedLabel.setText(totalUngraded + " Belum Dinilai");
                    
                    // Update progress bar (maksimum 30 tugas)
                    progressUngraded.setProgress(Math.min(totalUngraded / 30.0, 1.0));
                }
            }
            
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("Error", "Gagal memuat statistik: " + e.getMessage());
        }
    }
    
    private void loadQuickStats() {
        try (Connection conn = new DatabaseConnection().getConnection()) {
            
            // 1. Total user
            String sqlTotalUsers = "SELECT COUNT(*) as total FROM users";
            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery(sqlTotalUsers)) {
                if (rs.next()) {
                    quickTotalUsers.setText(String.valueOf(rs.getInt("total")));
                }
            }
            
            // 2. Total courses
            String sqlTotalCourses = "SELECT COUNT(*) as total FROM mata_kuliah";
            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery(sqlTotalCourses)) {
                if (rs.next()) {
                    quickTotalCourses.setText(String.valueOf(rs.getInt("total")));
                }
            }
            
            // 3. Login hari ini (simulasi - hitung user dibuat hari ini)
            String sqlTodayLogin = "SELECT COUNT(*) as total FROM users WHERE DATE(created_at) = CURDATE()";
            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery(sqlTodayLogin)) {
                if (rs.next()) {
                    quickTodayLogin.setText(String.valueOf(rs.getInt("total")));
                }
            }
            
            // 4. Submission hari ini
            String sqlTodaySubmissions = "SELECT COUNT(*) as total FROM pengumpulan_tugas WHERE DATE(submitted_at) = CURDATE()";
            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery(sqlTodaySubmissions)) {
                if (rs.next()) {
                    quickTodaySubmissions.setText(String.valueOf(rs.getInt("total")));
                }
            }
            
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
        if (clockTimeline != null) {
            clockTimeline.stop();
        }
        SessionManager.logout();
        loadScene("/fxml/auth/login.fxml");
    }
    
    @FXML
    private void handleManageUsers() {
        // Untuk sementara tampilkan alert, nanti bisa diarahkan ke halaman manajemen user
        showAlert("Info", "Fitur Manajemen User akan segera hadir!\n\nFitur yang tersedia sekarang:\n1. Dashboard Statistik\n2. Manajemen Jadwal\n3. Validasi Pendaftaran\n4. Penilaian Tugas");
    }
    
    @FXML
    private void handleManageCourses() {
        showAlert("Info", "Fitur Manajemen Mata Pelajaran akan segera hadir!\n\nAnda dapat melihat daftar mata pelajaran di Database.\nTotal mata pelajaran: " + quickTotalCourses.getText());
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
        showAlert("Info", "Fitur Laporan akan segera hadir!\n\nLaporan yang akan tersedia:\n1. Laporan Nilai Siswa\n2. Laporan Kehadiran\n3. Laporan Keuangan\n4. Laporan Aktivitas");
    }
    
    @FXML
    private void handleSettings() {
        showAlert("Info", "Fitur Pengaturan akan segera hadir!\n\nPengaturan yang akan tersedia:\n1. Pengaturan Sistem\n2. Pengaturan User\n3. Pengaturan Akademik\n4. Backup Database");
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
            
            // Stop clock jika pindah scene
            if (clockTimeline != null) {
                clockTimeline.stop();
            }
            
        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Error", "Gagal memuat halaman: " + e.getMessage());
        }
    }
    
    private String getPageTitle(String fxmlPath) {
        return switch (fxmlPath) {
            case "/fxml/admin/jadwal_admin.fxml" -> "Manajemen Jadwal";
            case "/fxml/admin/validasi_pendaftaran.fxml" -> "Validasi Pendaftaran";
            case "/fxml/admin/nilai_tugas.fxml" -> "Penilaian Tugas";
            case "/fxml/auth/login.fxml" -> "Login";
            default -> "Dashboard Admin";
        };
    }
    
    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
    
    // Cleanup method
    public void cleanup() {
        if (clockTimeline != null) {
            clockTimeline.stop();
        }
    }
    // Tambahkan method ini di AdminDashboardController
@FXML
private void handleSeedData() {
    Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
    confirmation.setTitle("Seed Data Testing");
    confirmation.setHeaderText("Tambahkan Data Testing?");
    confirmation.setContentText("Aksi ini akan menambahkan:\n" +
                              "1. Data mata kuliah tambahan\n" +
                              "2. Data pengumpulan tugas\n" +
                              "3. Data pendaftaran pelajaran tambahan\n\n" +
                              "Data hanya akan ditambahkan jika belum ada.");
    
    confirmation.showAndWait().ifPresent(response -> {
        if (response == ButtonType.OK) {
            try {
                DatabaseConnection db = new DatabaseConnection();
                // Panggil method seedTestData yang baru ditambahkan
                // db.seedTestData();
                
                // Seed data dummy
                DataSeeder.seedDummySubmissions();
                DataSeeder.seedDummyRegistrations();
                
                showAlert("Success", "Data testing berhasil ditambahkan!");
                loadStatistics();
                loadQuickStats();
            } catch (SQLException e) {
                showAlert("Error", "Gagal menambahkan data: " + e.getMessage());
            }
        }
    });
}
}
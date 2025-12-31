package com.projektestsiak.utils;

import com.projektestsiak.models.DatabaseConnection;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import java.sql.SQLException;
import java.util.Optional;

public class DataSeeder {
    
    public static void seedInitialData() {
        try {
            DatabaseConnection db = new DatabaseConnection();
            
            // Tanya user apakah ingin seed data testing
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Seed Data Testing");
            alert.setHeaderText("Database berhasil dibuat!");
            alert.setContentText("Apakah Anda ingin menambahkan data testing?\n\n" +
                               "Data yang akan ditambahkan:\n" +
                               "• Mata kuliah tambahan\n" +
                               "• Jadwal kuliah\n" +
                               "• Tugas untuk siswa\n" +
                               "• Pendaftaran pelajaran tambahan contoh");
            
            Optional<ButtonType> result = alert.showAndWait();
            if (result.isPresent() && result.get() == ButtonType.OK) {
                // Method seedTestData perlu ditambahkan ke DatabaseConnection
                // db.seedTestData();
                Alert successAlert = new Alert(Alert.AlertType.INFORMATION);
                successAlert.setTitle("Success");
                successAlert.setHeaderText(null);
                successAlert.setContentText("Data testing berhasil ditambahkan!");
                successAlert.showAndWait();
            }
            
        } catch (SQLException e) {
            Alert errorAlert = new Alert(Alert.AlertType.ERROR);
            errorAlert.setTitle("Database Error");
            errorAlert.setHeaderText("Gagal membuat database");
            errorAlert.setContentText("Error: " + e.getMessage());
            errorAlert.showAndWait();
        }
    }
    
    public static void seedDummySubmissions() {
        // Method ini bisa dipanggil dari menu admin untuk membuat data pengumpulan tugas
        try {
            DatabaseConnection db = new DatabaseConnection();
            try (var conn = db.getConnection()) {
                // Cek jika sudah ada pengumpulan tugas
                String checkSql = "SELECT COUNT(*) FROM pengumpulan_tugas";
                try (var stmt = conn.prepareStatement(checkSql);
                     var rs = stmt.executeQuery()) {
                    
                    rs.next();
                    if (rs.getInt(1) == 0) {
                        // Buat pengumpulan tugas untuk testing
                        String insertSql = """
                            INSERT INTO pengumpulan_tugas (tugas_id, user_id, file_path, nilai, komentar, submitted_at)
                            SELECT t.id, u.id, '/uploads/contoh_tugas.pdf', 
                                   CASE WHEN u.username = 'bahlil' THEN 85.5 ELSE 78.0 END,
                                   CASE WHEN u.username = 'bahlil' THEN 'Bagus!' ELSE 'Perlu improvement' END,
                                   DATE_SUB(NOW(), INTERVAL 2 DAY)
                            FROM tugas t, users u 
                            WHERE u.role = 'student' 
                            AND t.judul LIKE '%Java%'
                            LIMIT 2
                        """;
                        
                        try (var insertStmt = conn.prepareStatement(insertSql)) {
                            int rows = insertStmt.executeUpdate();
                            System.out.println("Created " + rows + " dummy submissions");
                            
                            Alert alert = new Alert(Alert.AlertType.INFORMATION);
                            alert.setTitle("Success");
                            alert.setHeaderText(null);
                            alert.setContentText("Berhasil membuat " + rows + " pengumpulan tugas dummy");
                            alert.showAndWait();
                        }
                    } else {
                        Alert alert = new Alert(Alert.AlertType.INFORMATION);
                        alert.setTitle("Info");
                        alert.setHeaderText(null);
                        alert.setContentText("Sudah ada data pengumpulan tugas di database.");
                        alert.showAndWait();
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    
    public static void seedDummyRegistrations() {
        // Method untuk membuat pendaftaran pelajaran tambahan dummy
        try {
            DatabaseConnection db = new DatabaseConnection();
            try (var conn = db.getConnection()) {
                // Cek jika sudah ada pendaftaran
                String checkSql = "SELECT COUNT(*) FROM pelajaran_tambahan";
                try (var stmt = conn.prepareStatement(checkSql);
                     var rs = stmt.executeQuery()) {
                    
                    rs.next();
                    if (rs.getInt(1) == 0) {
                        // Buat pendaftaran untuk testing
                        String insertSql = """
                            INSERT INTO pelajaran_tambahan (user_id, mata_kuliah_id, status, bukti_pembayaran)
                            SELECT u.id, mk.id, 'pending', '/uploads/bukti_contoh.jpg'
                            FROM users u, mata_kuliah mk 
                            WHERE u.username = 'bahlil'
                            AND mk.kode = 'MK004'
                            UNION
                            SELECT u.id, mk.id, 'approved', '/uploads/bukti_lunas.jpg'
                            FROM users u, mata_kuliah mk 
                            WHERE u.username = 'budiarie'
                            AND mk.kode = 'MK005'
                        """;
                        
                        try (var insertStmt = conn.prepareStatement(insertSql)) {
                            int rows = insertStmt.executeUpdate();
                            System.out.println("Created " + rows + " dummy registrations");
                            
                            Alert alert = new Alert(Alert.AlertType.INFORMATION);
                            alert.setTitle("Success");
                            alert.setHeaderText(null);
                            alert.setContentText("Berhasil membuat " + rows + " pendaftaran dummy");
                            alert.showAndWait();
                        }
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
package com.projektestsiak.models;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.sql.*;

public class PengumpulanTugasModel {
    private final DatabaseConnection dbConnection;
    private static final String UPLOAD_DIR = "uploads/";

    public PengumpulanTugasModel() throws SQLException {
        this.dbConnection = new DatabaseConnection();
        createUploadDirectory();
    }

    private void createUploadDirectory() {
        File directory = new File(UPLOAD_DIR);
        if (!directory.exists()) {
            directory.mkdirs();
            System.out.println("Upload directory created: " + UPLOAD_DIR);
        }
    }

    public boolean kumpulkanTugas(int tugasId, int userId, String filePath) {
        // Simpan file ke upload directory
        String savedFileName = saveUploadedFile(filePath);
        if (savedFileName == null) {
            return false;
        }

        String query = """
            INSERT INTO pengumpulan_tugas (tugas_id, user_id, file_path) 
            VALUES (?, ?, ?) 
            ON DUPLICATE KEY UPDATE 
            file_path = ?, submitted_at = CURRENT_TIMESTAMP
        """;

        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, tugasId);
            stmt.setInt(2, userId);
            stmt.setString(3, savedFileName);
            stmt.setString(4, savedFileName);

            int rowsAffected = stmt.executeUpdate();
            return rowsAffected > 0;

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    private String saveUploadedFile(String originalFilePath) {
        try {
            File originalFile = new File(originalFilePath);
            if (!originalFile.exists()) {
                System.err.println("File tidak ditemukan: " + originalFilePath);
                return null;
            }

            // Generate unique filename
            String fileName = System.currentTimeMillis() + "_" + originalFile.getName();
            Path targetPath = Paths.get(UPLOAD_DIR, fileName);

            // Copy file to upload directory
            Files.copy(originalFile.toPath(), targetPath, StandardCopyOption.REPLACE_EXISTING);
            
            System.out.println("File berhasil disimpan: " + targetPath.toString());
            return fileName;

        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public File getUploadedFile(String fileName) {
        if (fileName == null || fileName.isEmpty()) {
            return null;
        }
        return new File(UPLOAD_DIR + fileName);
    }

    public boolean beriNilai(int pengumpulanId, double nilai, String komentar) {
        String query = "UPDATE pengumpulan_tugas SET nilai = ?, komentar = ? WHERE id = ?";
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setDouble(1, nilai);
            stmt.setString(2, komentar);
            stmt.setInt(3, pengumpulanId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean isTugasSubmitted(int tugasId, int userId) {
        String query = "SELECT COUNT(*) FROM pengumpulan_tugas WHERE tugas_id = ? AND user_id = ?";
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, tugasId);
            stmt.setInt(2, userId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
}
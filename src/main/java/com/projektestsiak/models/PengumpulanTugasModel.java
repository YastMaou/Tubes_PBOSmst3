package com.projektestsiak.models;

import java.sql.*;

public class PengumpulanTugasModel {
    private final DatabaseConnection dbConnection;
    
    public PengumpulanTugasModel() throws SQLException {
        this.dbConnection = new DatabaseConnection();
    }
    
    public boolean kumpulkanTugas(int tugasId, int userId, String filePath) {
        String query = "INSERT INTO pengumpulan_tugas (tugas_id, user_id, file_path) VALUES (?, ?, ?) " +
                      "ON DUPLICATE KEY UPDATE file_path = ?, submitted_at = CURRENT_TIMESTAMP";
        
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            
            stmt.setInt(1, tugasId);
            stmt.setInt(2, userId);
            stmt.setString(3, filePath);
            stmt.setString(4, filePath);
            
            int rowsAffected = stmt.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
    
    public boolean beriNilai(int pengumpulanId, double nilai, String komentar) {
        String query = "UPDATE pengumpulan_tugas SET nilai = ?, komentar = ? WHERE id = ?";
        
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            
            stmt.setDouble(1, nilai);
            stmt.setString(2, komentar);
            stmt.setInt(3, pengumpulanId);
            
            int rowsAffected = stmt.executeUpdate();
            return rowsAffected > 0;
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
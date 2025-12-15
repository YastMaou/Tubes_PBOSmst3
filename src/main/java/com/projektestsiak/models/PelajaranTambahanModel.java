package com.projektestsiak.models;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class PelajaranTambahanModel {
    private final DatabaseConnection dbConnection;
    
    public PelajaranTambahanModel() throws SQLException {
        this.dbConnection = new DatabaseConnection();
    }
    
    public boolean pilihMataKuliah(int userId, int mataKuliahId, String buktiPembayaran) {
        String query = "INSERT INTO pelajaran_tambahan (user_id, mata_kuliah_id, status, bukti_pembayaran) VALUES (?, ?, 'pending', ?)";
        
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            
            stmt.setInt(1, userId);
            stmt.setInt(2, mataKuliahId);
            stmt.setString(3, buktiPembayaran);
            
            int rowsAffected = stmt.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
    
    public List<MataKuliah> getPelajaranTambahanByUser(int userId) {
        List<MataKuliah> list = new ArrayList<>();
        String query = "SELECT mk.*, pt.status " +
                      "FROM pelajaran_tambahan pt " +
                      "JOIN mata_kuliah mk ON pt.mata_kuliah_id = mk.id " +
                      "WHERE pt.user_id = ?";
        
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            
            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();
            
            while (rs.next()) {
                MataKuliah mk = new MataKuliah(
                    rs.getInt("id"),
                    rs.getString("kode"),
                    rs.getString("nama"),
                    rs.getInt("sks"),
                    rs.getInt("semester"),
                    rs.getString("dosen"),
                    rs.getDouble("harga_sks")
                );
                list.add(mk);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }
    
    public boolean isAlreadyRegistered(int userId, int mataKuliahId) {
        String query = "SELECT COUNT(*) FROM pelajaran_tambahan WHERE user_id = ? AND mata_kuliah_id = ?";
        
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            
            stmt.setInt(1, userId);
            stmt.setInt(2, mataKuliahId);
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
    
    public int getTotalSKSRegistered(int userId) {
        String query = "SELECT SUM(mk.sks) FROM pelajaran_tambahan pt " +
                      "JOIN mata_kuliah mk ON pt.mata_kuliah_id = mk.id " +
                      "WHERE pt.user_id = ? AND pt.status IN ('pending', 'approved')";
        
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            
            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }
}
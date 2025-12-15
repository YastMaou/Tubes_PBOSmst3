package com.projektestsiak.models;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class MataKuliahModel {
    private final DatabaseConnection dbConnection;
    
    public MataKuliahModel() throws SQLException {
        this.dbConnection = new DatabaseConnection();
    }
    
    public List<MataKuliah> getAllMataKuliah() {
        List<MataKuliah> list = new ArrayList<>();
        String query = "SELECT * FROM mata_kuliah ORDER BY semester, kode";
        
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {
            
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
    
    public List<MataKuliah> getMataKuliahBySemester(int semester) {
        List<MataKuliah> list = new ArrayList<>();
        String query = "SELECT * FROM mata_kuliah WHERE semester = ? ORDER BY kode";
        
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            
            stmt.setInt(1, semester);
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
    
    public MataKuliah getMataKuliahById(int id) {
        String query = "SELECT * FROM mata_kuliah WHERE id = ?";
        
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            
            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                return new MataKuliah(
                    rs.getInt("id"),
                    rs.getString("kode"),
                    rs.getString("nama"),
                    rs.getInt("sks"),
                    rs.getInt("semester"),
                    rs.getString("dosen"),
                    rs.getDouble("harga_sks")
                );
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }
    
    public List<MataKuliah> getAvailableMataKuliahForStudent(int studentId) {
        List<MataKuliah> list = new ArrayList<>();
        String query = "SELECT mk.* FROM mata_kuliah mk " +
                      "WHERE mk.id NOT IN (" +
                      "    SELECT pt.mata_kuliah_id FROM pelajaran_tambahan pt " +
                      "    WHERE pt.user_id = ? AND pt.status IN ('pending', 'approved')" +
                      ") ORDER BY mk.semester, mk.kode";
        
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            
            stmt.setInt(1, studentId);
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
}
package com.projektestsiak.models;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class TugasModel {
    private final DatabaseConnection dbConnection;
    
    public TugasModel() throws SQLException {
        this.dbConnection = new DatabaseConnection();
    }
    
    public List<Tugas> getAllTugas() {
        List<Tugas> list = new ArrayList<>();
        String query = "SELECT t.*, mk.nama as mata_kuliah_nama " +
                      "FROM tugas t " +
                      "JOIN mata_kuliah mk ON t.mata_kuliah_id = mk.id " +
                      "ORDER BY t.deadline";
        
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {
            
            while (rs.next()) {
                Tugas tugas = new Tugas(
                    rs.getInt("id"),
                    rs.getInt("mata_kuliah_id"),
                    rs.getString("mata_kuliah_nama"),
                    rs.getString("judul"),
                    rs.getString("deskripsi"),
                    rs.getTimestamp("deadline").toLocalDateTime(),
                    rs.getString("file_path")
                );
                list.add(tugas);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }
    
    public List<Tugas> getTugasWithStatus(int userId) {
        List<Tugas> list = new ArrayList<>();
        String query = "SELECT t.*, mk.nama as mata_kuliah_nama, " +
                      "pt.nilai, pt.komentar, CASE WHEN pt.id IS NOT NULL THEN true ELSE false END as submitted " +
                      "FROM tugas t " +
                      "JOIN mata_kuliah mk ON t.mata_kuliah_id = mk.id " +
                      "LEFT JOIN pengumpulan_tugas pt ON t.id = pt.tugas_id AND pt.user_id = ? " +
                      "ORDER BY t.deadline";
        
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            
            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();
            
            while (rs.next()) {
                Tugas tugas = new Tugas(
                    rs.getInt("id"),
                    rs.getInt("mata_kuliah_id"),
                    rs.getString("mata_kuliah_nama"),
                    rs.getString("judul"),
                    rs.getString("deskripsi"),
                    rs.getTimestamp("deadline").toLocalDateTime(),
                    rs.getString("file_path")
                );
                tugas.setSubmitted(rs.getBoolean("submitted"));
                tugas.setNilai(rs.getDouble("nilai"));
                if (rs.wasNull()) {
                    tugas.setNilai(null);
                }
                tugas.setKomentar(rs.getString("komentar"));
                list.add(tugas);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }
    
    public Tugas getTugasById(int id) {
        String query = "SELECT t.*, mk.nama as mata_kuliah_nama FROM tugas t " +
                      "JOIN mata_kuliah mk ON t.mata_kuliah_id = mk.id " +
                      "WHERE t.id = ?";
        
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            
            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                return new Tugas(
                    rs.getInt("id"),
                    rs.getInt("mata_kuliah_id"),
                    rs.getString("mata_kuliah_nama"),
                    rs.getString("judul"),
                    rs.getString("deskripsi"),
                    rs.getTimestamp("deadline").toLocalDateTime(),
                    rs.getString("file_path")
                );
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }
}
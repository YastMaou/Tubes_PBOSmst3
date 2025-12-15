package com.projektestsiak.models;

import java.sql.*;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

public class JadwalModel {
    private final DatabaseConnection dbConnection;
    
    public JadwalModel() throws SQLException {
        this.dbConnection = new DatabaseConnection();
    }
    
    public List<Jadwal> getAllJadwal() {
        List<Jadwal> list = new ArrayList<>();
        String query = "SELECT j.*, mk.nama as mata_kuliah_nama " +
                      "FROM jadwal j " +
                      "JOIN mata_kuliah mk ON j.mata_kuliah_id = mk.id " +
                      "ORDER BY FIELD(hari, 'Senin','Selasa','Rabu','Kamis','Jumat','Sabtu'), jam_mulai";
        
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {
            
            while (rs.next()) {
                Jadwal jadwal = new Jadwal(
                    rs.getInt("id"),
                    rs.getInt("mata_kuliah_id"),
                    rs.getString("mata_kuliah_nama"),
                    rs.getString("hari"),
                    rs.getTime("jam_mulai").toLocalTime(),
                    rs.getTime("jam_selesai").toLocalTime(),
                    rs.getString("ruangan")
                );
                list.add(jadwal);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }
    
    public List<Jadwal> getJadwalByHari(String hari) {
        List<Jadwal> list = new ArrayList<>();
        String query = "SELECT j.*, mk.nama as mata_kuliah_nama " +
                      "FROM jadwal j " +
                      "JOIN mata_kuliah mk ON j.mata_kuliah_id = mk.id " +
                      "WHERE j.hari = ? " +
                      "ORDER BY jam_mulai";
        
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            
            stmt.setString(1, hari);
            ResultSet rs = stmt.executeQuery();
            
            while (rs.next()) {
                Jadwal jadwal = new Jadwal(
                    rs.getInt("id"),
                    rs.getInt("mata_kuliah_id"),
                    rs.getString("mata_kuliah_nama"),
                    rs.getString("hari"),
                    rs.getTime("jam_mulai").toLocalTime(),
                    rs.getTime("jam_selesai").toLocalTime(),
                    rs.getString("ruangan")
                );
                list.add(jadwal);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }
    
    public List<Jadwal> getJadwalForStudent(int studentId) {
        // This could be enhanced to show only relevant courses for the student
        return getAllJadwal();
    }
}
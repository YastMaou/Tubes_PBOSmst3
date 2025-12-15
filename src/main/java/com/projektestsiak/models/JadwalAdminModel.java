package com.projektestsiak.models;

import java.sql.*;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

public class JadwalAdminModel {
    private final DatabaseConnection dbConnection;
    
    public JadwalAdminModel() throws SQLException {
        this.dbConnection = new DatabaseConnection();
    }
    
    // ========== CRUD JADWAL ==========
    
    // Create
    public boolean tambahJadwal(int mataPelajaranId, String hari, String jamMulai, 
                                String jamSelesai, String ruangan) {
        String query = "INSERT INTO jadwal (mata_kuliah_id, hari, jam_mulai, jam_selesai, ruangan) " +
                      "VALUES (?, ?, ?, ?, ?)";
        
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            
            stmt.setInt(1, mataPelajaranId);
            stmt.setString(2, hari);
            stmt.setString(3, jamMulai);
            stmt.setString(4, jamSelesai);
            stmt.setString(5, ruangan);
            
            int rowsAffected = stmt.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
    
    // Read semua jadwal dengan detail
    public List<JadwalDetail> getAllJadwalWithDetails() {
        List<JadwalDetail> list = new ArrayList<>();
        String query = "SELECT j.*, mk.kode, mk.nama as mata_kuliah_nama, mk.dosen " +
                      "FROM jadwal j " +
                      "JOIN mata_kuliah mk ON j.mata_kuliah_id = mk.id " +
                      "ORDER BY FIELD(j.hari, 'Senin','Selasa','Rabu','Kamis','Jumat','Sabtu'), j.jam_mulai";
        
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {
            
            while (rs.next()) {
                JadwalDetail jd = new JadwalDetail(
                    rs.getInt("id"),
                    rs.getInt("mata_kuliah_id"),
                    rs.getString("kode"),
                    rs.getString("mata_kuliah_nama"),
                    rs.getString("dosen"),
                    rs.getString("hari"),
                    rs.getTime("jam_mulai").toLocalTime(),
                    rs.getTime("jam_selesai").toLocalTime(),
                    rs.getString("ruangan")
                );
                list.add(jd);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }
    
    // Update
    public boolean updateJadwal(int id, int mataPelajaranId, String hari, 
                                String jamMulai, String jamSelesai, String ruangan) {
        String query = "UPDATE jadwal SET mata_kuliah_id = ?, hari = ?, jam_mulai = ?, " +
                      "jam_selesai = ?, ruangan = ? WHERE id = ?";
        
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            
            stmt.setInt(1, mataPelajaranId);
            stmt.setString(2, hari);
            stmt.setString(3, jamMulai);
            stmt.setString(4, jamSelesai);
            stmt.setString(5, ruangan);
            stmt.setInt(6, id);
            
            int rowsAffected = stmt.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
    
    // Delete
    public boolean hapusJadwal(int id) {
        String query = "DELETE FROM jadwal WHERE id = ?";
        
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            
            stmt.setInt(1, id);
            int rowsAffected = stmt.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
    
    // Get semua mata pelajaran untuk dropdown
    public List<MataKuliah> getAllMataKuliah() {
        List<MataKuliah> list = new ArrayList<>();
        String query = "SELECT * FROM mata_kuliah ORDER BY kode";
        
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
    
    // Inner class untuk jadwal detail
    public static class JadwalDetail {
        private int id;
        private int mataKuliahId;
        private String kodeMataKuliah;
        private String namaMataKuliah;
        private String dosen;
        private String hari;
        private LocalTime jamMulai;
        private LocalTime jamSelesai;
        private String ruangan;
        
        public JadwalDetail(int id, int mataKuliahId, String kodeMataKuliah, 
                           String namaMataKuliah, String dosen, String hari,
                           LocalTime jamMulai, LocalTime jamSelesai, String ruangan) {
            this.id = id;
            this.mataKuliahId = mataKuliahId;
            this.kodeMataKuliah = kodeMataKuliah;
            this.namaMataKuliah = namaMataKuliah;
            this.dosen = dosen;
            this.hari = hari;
            this.jamMulai = jamMulai;
            this.jamSelesai = jamSelesai;
            this.ruangan = ruangan;
        }
        
        // Getters
        public int getId() { return id; }
        public int getMataKuliahId() { return mataKuliahId; }
        public String getKodeMataKuliah() { return kodeMataKuliah; }
        public String getNamaMataKuliah() { return namaMataKuliah; }
        public String getDosen() { return dosen; }
        public String getHari() { return hari; }
        public LocalTime getJamMulai() { return jamMulai; }
        public LocalTime getJamSelesai() { return jamSelesai; }
        public String getRuangan() { return ruangan; }
        public String getJamFormat() { return jamMulai + " - " + jamSelesai; }
        public String getDetail() { return kodeMataKuliah + " - " + namaMataKuliah + " (" + dosen + ")"; }
    }
}
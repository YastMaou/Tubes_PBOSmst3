package com.projektestsiak.models;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class NilaiModel {
    private final DatabaseConnection dbConnection;
    
    public NilaiModel() throws SQLException {
        this.dbConnection = new DatabaseConnection();
    }
    
    // Get semua pengumpulan tugas yang belum dinilai
    public List<PengumpulanDetail> getPengumpulanBelumDinilai() {
        List<PengumpulanDetail> list = new ArrayList<>();
        String query = "SELECT pt.*, u.nama as nama_siswa, u.username, " +
                      "t.judul, t.deskripsi, mk.nama as mata_kuliah_nama " +
                      "FROM pengumpulan_tugas pt " +
                      "JOIN users u ON pt.user_id = u.id " +
                      "JOIN tugas t ON pt.tugas_id = t.id " +
                      "JOIN mata_kuliah mk ON t.mata_kuliah_id = mk.id " +
                      "WHERE pt.nilai IS NULL " +
                      "ORDER BY pt.submitted_at";
        
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {
            
            while (rs.next()) {
                PengumpulanDetail pd = new PengumpulanDetail(
                    rs.getInt("id"),
                    rs.getInt("tugas_id"),
                    rs.getInt("user_id"),
                    rs.getString("nama_siswa"),
                    rs.getString("username"),
                    rs.getString("judul"),
                    rs.getString("deskripsi"),
                    rs.getString("mata_kuliah_nama"),
                    rs.getString("file_path"),
                    rs.getTimestamp("submitted_at").toLocalDateTime(),
                    rs.getDouble("nilai"),
                    rs.getString("komentar")
                );
                list.add(pd);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }
    
    // Get semua pengumpulan yang sudah dinilai
    public List<PengumpulanDetail> getPengumpulanSudahDinilai() {
        List<PengumpulanDetail> list = new ArrayList<>();
        String query = "SELECT pt.*, u.nama as nama_siswa, u.username, " +
                      "t.judul, t.deskripsi, mk.nama as mata_kuliah_nama " +
                      "FROM pengumpulan_tugas pt " +
                      "JOIN users u ON pt.user_id = u.id " +
                      "JOIN tugas t ON pt.tugas_id = t.id " +
                      "JOIN mata_kuliah mk ON t.mata_kuliah_id = mk.id " +
                      "WHERE pt.nilai IS NOT NULL " +
                      "ORDER BY pt.submitted_at DESC";
        
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {
            
            while (rs.next()) {
                PengumpulanDetail pd = new PengumpulanDetail(
                    rs.getInt("id"),
                    rs.getInt("tugas_id"),
                    rs.getInt("user_id"),
                    rs.getString("nama_siswa"),
                    rs.getString("username"),
                    rs.getString("judul"),
                    rs.getString("deskripsi"),
                    rs.getString("mata_kuliah_nama"),
                    rs.getString("file_path"),
                    rs.getTimestamp("submitted_at").toLocalDateTime(),
                    rs.getDouble("nilai"),
                    rs.getString("komentar")
                );
                list.add(pd);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }
    
    // Beri nilai pada pengumpulan tugas
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
    
    // Update nilai
    public boolean updateNilai(int pengumpulanId, double nilai, String komentar) {
        return beriNilai(pengumpulanId, nilai, komentar);
    }
    
    // Hapus nilai (reset)
    public boolean hapusNilai(int pengumpulanId) {
        String query = "UPDATE pengumpulan_tugas SET nilai = NULL, komentar = NULL WHERE id = ?";
        
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            
            stmt.setInt(1, pengumpulanId);
            int rowsAffected = stmt.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
    
    // Inner class untuk detail pengumpulan
    public static class PengumpulanDetail {
        private int id;
        private int tugasId;
        private int userId;
        private String namaSiswa;
        private String username;
        private String judulTugas;
        private String deskripsiTugas;
        private String mataKuliahNama;
        private String filePath;
        private java.time.LocalDateTime submittedAt;
        private Double nilai;
        private String komentar;
        
        public PengumpulanDetail(int id, int tugasId, int userId, String namaSiswa,
                                String username, String judulTugas, String deskripsiTugas,
                                String mataKuliahNama, String filePath, 
                                java.time.LocalDateTime submittedAt, Double nilai, String komentar) {
            this.id = id;
            this.tugasId = tugasId;
            this.userId = userId;
            this.namaSiswa = namaSiswa;
            this.username = username;
            this.judulTugas = judulTugas;
            this.deskripsiTugas = deskripsiTugas;
            this.mataKuliahNama = mataKuliahNama;
            this.filePath = filePath;
            this.submittedAt = submittedAt;
            this.nilai = nilai;
            this.komentar = komentar;
        }
        
        // Getters
        public int getId() { return id; }
        public int getTugasId() { return tugasId; }
        public int getUserId() { return userId; }
        public String getNamaSiswa() { return namaSiswa; }
        public String getUsername() { return username; }
        public String getJudulTugas() { return judulTugas; }
        public String getDeskripsiTugas() { return deskripsiTugas; }
        public String getMataKuliahNama() { return mataKuliahNama; }
        public String getFilePath() { return filePath; }
        public java.time.LocalDateTime getSubmittedAt() { return submittedAt; }
        public Double getNilai() { return nilai; }
        public String getKomentar() { return komentar; }
        public String getStatus() { 
            return nilai != null ? "Sudah Dinilai (" + nilai + ")" : "Belum Dinilai"; 
        }
        public String getDetail() { 
            return namaSiswa + " - " + mataKuliahNama + " - " + judulTugas; 
        }
        public String getSubmittedAtFormat() {
            return submittedAt.toString().replace("T", " ");
        }
    }
}
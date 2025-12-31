package com.projektestsiak.models;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ValidasiModel {
    private final DatabaseConnection dbConnection;

    public ValidasiModel() throws SQLException {
        this.dbConnection = new DatabaseConnection();
    }

    // Get semua pendaftaran yang pending
    public List<PendaftaranDetail> getPendaftaranPending() {
        List<PendaftaranDetail> list = new ArrayList<>();
        String query = """
            SELECT pt.*, u.nama as nama_siswa, u.username,
                   mk.kode, mk.nama as mata_kuliah_nama, mk.sks, mk.harga_sks
            FROM pelajaran_tambahan pt
            JOIN users u ON pt.user_id = u.id
            JOIN mata_kuliah mk ON pt.mata_kuliah_id = mk.id
            WHERE pt.status = 'pending'
            ORDER BY pt.tanggal_daftar DESC
        """;

        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                PendaftaranDetail pd = new PendaftaranDetail(
                    rs.getInt("id"),
                    rs.getInt("user_id"),
                    rs.getString("nama_siswa"),
                    rs.getString("username"),
                    rs.getInt("mata_kuliah_id"),
                    rs.getString("kode"),
                    rs.getString("mata_kuliah_nama"),
                    rs.getInt("sks"),
                    rs.getDouble("harga_sks"),
                    rs.getString("bukti_pembayaran"),
                    rs.getString("status"),
                    rs.getTimestamp("tanggal_daftar").toLocalDateTime()
                );
                list.add(pd);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return list;
    }

    // Get semua pendaftaran (semua status) untuk monitoring
    public List<PendaftaranDetail> getAllPendaftaran() {
        List<PendaftaranDetail> list = new ArrayList<>();
        String query = """
            SELECT pt.*, u.nama as nama_siswa, u.username,
                   mk.kode, mk.nama as mata_kuliah_nama, mk.sks, mk.harga_sks
            FROM pelajaran_tambahan pt
            JOIN users u ON pt.user_id = u.id
            JOIN mata_kuliah mk ON pt.mata_kuliah_id = mk.id
            ORDER BY 
                CASE pt.status 
                    WHEN 'pending' THEN 1
                    WHEN 'approved' THEN 2
                    WHEN 'rejected' THEN 3
                END,
                pt.tanggal_daftar DESC
        """;

        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                PendaftaranDetail pd = new PendaftaranDetail(
                    rs.getInt("id"),
                    rs.getInt("user_id"),
                    rs.getString("nama_siswa"),
                    rs.getString("username"),
                    rs.getInt("mata_kuliah_id"),
                    rs.getString("kode"),
                    rs.getString("mata_kuliah_nama"),
                    rs.getInt("sks"),
                    rs.getDouble("harga_sks"),
                    rs.getString("bukti_pembayaran"),
                    rs.getString("status"),
                    rs.getTimestamp("tanggal_daftar").toLocalDateTime()
                );
                list.add(pd);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return list;
    }

    // Approve pendaftaran
    public boolean approvePendaftaran(int pendaftaranId) {
        String query = "UPDATE pelajaran_tambahan SET status = 'approved' WHERE id = ?";
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, pendaftaranId);
            int rowsAffected = stmt.executeUpdate();
            
            // Juga update jadwal untuk siswa yang disetujui
            if (rowsAffected > 0) {
                updateJadwalUntukSiswa(pendaftaranId);
            }
            
            return rowsAffected > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    private void updateJadwalUntukSiswa(int pendaftaranId) {
        // Method untuk menambahkan jadwal otomatis untuk mata kuliah yang disetujui
        // Ini opsional, bisa diimplementasikan jika perlu
    }

    // Reject pendaftaran
    public boolean rejectPendaftaran(int pendaftaranId, String alasan) {
        String query = "UPDATE pelajaran_tambahan SET status = 'rejected' WHERE id = ?";
        // Catatan: Bisa tambahkan field 'alasan_reject' di tabel untuk menyimpan alasan
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, pendaftaranId);
            int rowsAffected = stmt.executeUpdate();
            
            // Bisa tambahkan log atau notifikasi ke siswa
            if (rowsAffected > 0 && alasan != null && !alasan.trim().isEmpty()) {
                System.out.println("Pendaftaran ID " + pendaftaranId + " ditolak. Alasan: " + alasan);
            }
            
            return rowsAffected > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    // Inner class untuk detail pendaftaran
    public static class PendaftaranDetail {
        private int id;
        private int userId;
        private String namaSiswa;
        private String username;
        private int mataKuliahId;
        private String kodeMataKuliah;
        private String namaMataKuliah;
        private int sks;
        private double hargaSks;
        private String buktiPembayaran;
        private String status;

        public PendaftaranDetail(int id, int userId, String namaSiswa, String username,
                               int mataKuliahId, String kodeMataKuliah, String namaMataKuliah,
                               int sks, double hargaSks, String buktiPembayaran, String status,
                               java.time.LocalDateTime tanggalDaftar) {
            this.id = id;
            this.userId = userId;
            this.namaSiswa = namaSiswa;
            this.username = username;
            this.mataKuliahId = mataKuliahId;
            this.kodeMataKuliah = kodeMataKuliah;
            this.namaMataKuliah = namaMataKuliah;
            this.sks = sks;
            this.hargaSks = hargaSks;
            this.buktiPembayaran = buktiPembayaran;
            this.status = status;
        }

        // Getters
        public int getId() { return id; }
        public int getUserId() { return userId; }
        public String getNamaSiswa() { return namaSiswa; }
        public String getUsername() { return username; }
        public int getMataKuliahId() { return mataKuliahId; }
        public String getKodeMataKuliah() { return kodeMataKuliah; }
        public String getNamaMataKuliah() { return namaMataKuliah; }
        public int getSks() { return sks; }
        public double getHargaSks() { return hargaSks; }
        public String getBuktiPembayaran() { return buktiPembayaran; }
        public String getStatus() { return status; }
        public double getTotalBiaya() { return sks * hargaSks; }
        
        public String getStatusColor() {
            return switch (status.toLowerCase()) {
                case "pending" -> "orange";
                case "approved" -> "green";
                case "rejected" -> "red";
                default -> "gray";
            };
        }
        
        public String getDetail() {
            return namaSiswa + " (" + username + ") - " +
                   kodeMataKuliah + " - " + namaMataKuliah + " (" + sks + " SKS) - " +
                   "Status: " + status;
        }
    }
}
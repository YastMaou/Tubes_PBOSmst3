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
        // Cek apakah sudah mendaftar mata kuliah ini
        if (isAlreadyRegistered(userId, mataKuliahId)) {
            System.out.println("User sudah terdaftar di mata kuliah ini");
            return false;
        }

        // Cek total SKS yang sudah diambil
        if (getTotalSKSRegistered(userId) >= 24) { // Batas maksimal 24 SKS
            System.out.println("Batas SKS telah tercapai (24 SKS)");
            return false;
        }

        String query = """
            INSERT INTO pelajaran_tambahan (user_id, mata_kuliah_id, status, bukti_pembayaran) 
            VALUES (?, ?, 'pending', ?)
        """;

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
        String query = """
            SELECT mk.*, pt.status 
            FROM pelajaran_tambahan pt 
            JOIN mata_kuliah mk ON pt.mata_kuliah_id = mk.id 
            WHERE pt.user_id = ? 
            ORDER BY pt.tanggal_daftar DESC
        """;

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
        String query = """
            SELECT COUNT(*) FROM pelajaran_tambahan 
            WHERE user_id = ? AND mata_kuliah_id = ? 
            AND status IN ('pending', 'approved')
        """;

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
        String query = """
            SELECT COALESCE(SUM(mk.sks), 0) 
            FROM pelajaran_tambahan pt 
            JOIN mata_kuliah mk ON pt.mata_kuliah_id = mk.id 
            WHERE pt.user_id = ? AND pt.status IN ('pending', 'approved')
        """;

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

    public List<PelajaranTambahanDetail> getPelajaranTambahanDetailByUser(int userId) {
        List<PelajaranTambahanDetail> list = new ArrayList<>();
        String query = """
            SELECT pt.*, mk.kode, mk.nama, mk.sks, mk.dosen, mk.harga_sks
            FROM pelajaran_tambahan pt 
            JOIN mata_kuliah mk ON pt.mata_kuliah_id = mk.id 
            WHERE pt.user_id = ? 
            ORDER BY pt.tanggal_daftar DESC
        """;

        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                PelajaranTambahanDetail detail = new PelajaranTambahanDetail(
                    rs.getInt("id"),
                    rs.getString("kode"),
                    rs.getString("nama"),
                    rs.getInt("sks"),
                    rs.getString("dosen"),
                    rs.getDouble("harga_sks"),
                    rs.getString("status"),
                    rs.getString("bukti_pembayaran"),
                    rs.getTimestamp("tanggal_daftar").toLocalDateTime()
                );
                list.add(detail);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return list;
    }

    // Inner class untuk detail lengkap
    public static class PelajaranTambahanDetail {
        private int id;
        private String kode;
        private String nama;
        private int sks;
        private String dosen;
        private double hargaSks;
        private String status;
        private String buktiPembayaran;
        private java.time.LocalDateTime tanggalDaftar;

        public PelajaranTambahanDetail(int id, String kode, String nama, int sks, 
                                     String dosen, double hargaSks, String status,
                                     String buktiPembayaran, java.time.LocalDateTime tanggalDaftar) {
            this.id = id;
            this.kode = kode;
            this.nama = nama;
            this.sks = sks;
            this.dosen = dosen;
            this.hargaSks = hargaSks;
            this.status = status;
            this.buktiPembayaran = buktiPembayaran;
            this.tanggalDaftar = tanggalDaftar;
        }

        // Getters
        public int getId() { return id; }
        public String getKode() { return kode; }
        public String getNama() { return nama; }
        public int getSks() { return sks; }
        public String getDosen() { return dosen; }
        public double getHargaSks() { return hargaSks; }
        public String getStatus() { return status; }
        public String getBuktiPembayaran() { return buktiPembayaran; }
        public java.time.LocalDateTime getTanggalDaftar() { return tanggalDaftar; }
        public double getTotalBiaya() { return sks * hargaSks; }
        
        public String getStatusColor() {
            return switch (status.toLowerCase()) {
                case "pending" -> "orange";
                case "approved" -> "green";
                case "rejected" -> "red";
                default -> "gray";
            };
        }
    }
}
package com.projektestsiak.models;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class NilaiModel {
    private final DatabaseConnection dbConnection;

    public NilaiModel() throws SQLException {
        this.dbConnection = new DatabaseConnection();
    }

    
    //AUTO SEED PENGUMPULAN TUGAS (DUMMY)
    
    public void seedPengumpulanJikaKosong() {
        String checkSql = "SELECT COUNT(*) FROM pengumpulan_tugas";
        String insertSql = """
            INSERT INTO pengumpulan_tugas (tugas_id, user_id, file_path)
            VALUES (?, ?, ?)
        """;

        try (Connection conn = dbConnection.getConnection();
             PreparedStatement checkStmt = conn.prepareStatement(checkSql);
             ResultSet rs = checkStmt.executeQuery()) {

            rs.next();
            if (rs.getInt(1) > 0) return; // sudah ada data → stop

            try (PreparedStatement insertStmt = conn.prepareStatement(insertSql)) {
                // Dummy student (id = 2), tugas id 1 & 2
                insertStmt.setInt(1, 1);
                insertStmt.setInt(2, 2);
                insertStmt.setString(3, "uploads/tugas_aljabar.pdf");
                insertStmt.executeUpdate();

                insertStmt.setInt(1, 2);
                insertStmt.setInt(2, 2);
                insertStmt.setString(3, "uploads/kalkulator.zip");
                insertStmt.executeUpdate();
            }

            System.out.println("✔ Dummy pengumpulan tugas berhasil dibuat");

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    
    // GET PENGUMPULAN BELUM DINILAI
    
    public List<PengumpulanDetail> getPengumpulanBelumDinilai() {
        List<PengumpulanDetail> list = new ArrayList<>();
        String query = """
            SELECT pt.*, u.nama AS nama_siswa, u.username,
                   t.judul, t.deskripsi, mk.nama AS mata_kuliah_nama
            FROM pengumpulan_tugas pt
            JOIN users u ON pt.user_id = u.id
            JOIN tugas t ON pt.tugas_id = t.id
            JOIN mata_kuliah mk ON t.mata_kuliah_id = mk.id
            WHERE pt.nilai IS NULL
            ORDER BY pt.submitted_at
        """;

        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                Double nilai = rs.getDouble("nilai");
                if (rs.wasNull()) nilai = null; // FIX NULL/BUG(Reno)

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
                    nilai,
                    rs.getString("komentar")
                );
                list.add(pd);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    
    // GET PENGUMPULAN SUDAH DINILAI
    
    public List<PengumpulanDetail> getPengumpulanSudahDinilai() {
        List<PengumpulanDetail> list = new ArrayList<>();
        String query = """
            SELECT pt.*, u.nama AS nama_siswa, u.username,
                   t.judul, t.deskripsi, mk.nama AS mata_kuliah_nama
            FROM pengumpulan_tugas pt
            JOIN users u ON pt.user_id = u.id
            JOIN tugas t ON pt.tugas_id = t.id
            JOIN mata_kuliah mk ON t.mata_kuliah_id = mk.id
            WHERE pt.nilai IS NOT NULL
            ORDER BY pt.submitted_at DESC
        """;

        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                Double nilai = rs.getDouble("nilai");
                if (rs.wasNull()) nilai = null; //FIX NULL/BUG(RENO)

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
                    nilai,
                    rs.getString("komentar")
                );
                list.add(pd);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    
    // CRUD NILAI
    
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

    public boolean updateNilai(int pengumpulanId, double nilai, String komentar) {
        return beriNilai(pengumpulanId, nilai, komentar);
    }

    public boolean hapusNilai(int pengumpulanId) {
        String query = "UPDATE pengumpulan_tugas SET nilai = NULL, komentar = NULL WHERE id = ?";

        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, pengumpulanId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    
    // INNER CLASS
    
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

        public int getId() { return id; }
        public String getNamaSiswa() { return namaSiswa; }
        public String getUsername() { return username; }
        public String getJudulTugas() { return judulTugas; }
        public String getDeskripsiTugas() { return deskripsiTugas; }
        public String getMataKuliahNama() { return mataKuliahNama; }
        public String getFilePath() { return filePath; }
        public Double getNilai() { return nilai; }
        public String getKomentar() { return komentar; }

        public String getSubmittedAtFormat() {
            return submittedAt.toString().replace("T", " ");
        }
    }
}

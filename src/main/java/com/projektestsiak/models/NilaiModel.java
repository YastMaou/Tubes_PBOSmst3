package com.projektestsiak.models;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class NilaiModel {
    private final DatabaseConnection dbConnection;

    public NilaiModel() throws SQLException {
        this.dbConnection = new DatabaseConnection();
        // Auto seed pengumpulan tugas saat model dibuat
        seedPengumpulanJikaKosong();
    }

    // AUTO SEED PENGUMPULAN TUGAS (DUMMY)
    public void seedPengumpulanJikaKosong() {
        String checkSql = "SELECT COUNT(*) FROM pengumpulan_tugas";
        String insertSql = """
            INSERT INTO pengumpulan_tugas (tugas_id, user_id, file_path, submitted_at)
            VALUES (?, ?, ?, DATE_SUB(NOW(), INTERVAL ? DAY))
        """;

        try (Connection conn = dbConnection.getConnection();
             PreparedStatement checkStmt = conn.prepareStatement(checkSql);
             ResultSet rs = checkStmt.executeQuery()) {

            rs.next();
            if (rs.getInt(1) > 0) return; // sudah ada data → stop

            try (PreparedStatement insertStmt = conn.prepareStatement(insertSql)) {
                // Seed data untuk 5 tugas berbeda dan 5 siswa berbeda
                int[][] data = {
                    {1, 2, 0},  // tugas 1, siswa 2, 0 hari lalu
                    {2, 3, 1},  // tugas 2, siswa 3, 1 hari lalu
                    {3, 4, 2},  // tugas 3, siswa 4, 2 hari lalu
                    {4, 5, 3},  // tugas 4, siswa 5, 3 hari lalu
                    {5, 6, 4},  // tugas 5, siswa 6, 4 hari lalu
                    {1, 7, 5},  // tugas 1, siswa 7, 5 hari lalu
                    {2, 8, 6},  // tugas 2, siswa 8, 6 hari lalu
                    {3, 9, 7},  // tugas 3, siswa 9, 7 hari lalu
                    {4, 10, 8}, // tugas 4, siswa 10, 8 hari lalu
                    {5, 11, 9}  // tugas 5, siswa 11, 9 hari lalu
                };

                String[] fileNames = {
                    "tugas_aljabar.pdf", "kalkulator.zip", "erd_design.pdf",
                    "website_portfolio.zip", "ai_paper.docx", "network_design.pdf",
                    "os_guide.pdf", "algoritma_tugas.zip", "statistika_report.pdf",
                    "mobile_app.apk"
                };

                for (int i = 0; i < data.length; i++) {
                    insertStmt.setInt(1, data[i][0]); // tugas_id
                    insertStmt.setInt(2, data[i][1]); // user_id
                    insertStmt.setString(3, "/uploads/" + fileNames[i]); // file_path
                    insertStmt.setInt(4, data[i][2]); // days ago
                    insertStmt.addBatch();
                }
                insertStmt.executeBatch();

                // Beri nilai pada beberapa pengumpulan
                String updateNilaiSql = """
                    UPDATE pengumpulan_tugas 
                    SET nilai = ?, komentar = ?
                    WHERE tugas_id = ? AND user_id = ?
                """;
                
                try (PreparedStatement updateStmt = conn.prepareStatement(updateNilaiSql)) {
                    // Nilai untuk 5 pengumpulan pertama
                    double[] nilai = {85.5, 92.0, 78.0, 88.5, 95.0};
                    String[] komentar = {
                        "Bagus, tapi kurang detail",
                        "Implementasi sangat baik",
                        "Cukup baik, perlu improvement",
                        "Desain sangat baik",
                        "Excellent work!"
                    };
                    
                    for (int i = 0; i < 5; i++) {
                        updateStmt.setDouble(1, nilai[i]);
                        updateStmt.setString(2, komentar[i]);
                        updateStmt.setInt(3, data[i][0]);
                        updateStmt.setInt(4, data[i][1]);
                        updateStmt.addBatch();
                    }
                    updateStmt.executeBatch();
                }

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
                if (rs.wasNull()) nilai = null;

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
                if (rs.wasNull()) nilai = null;

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


// public static class LaporanNilai {
//         private final String namaSiswa;
//         private final String judulTugas;
//         private final Double nilai;
//         private final String komentar;

//         public LaporanNilai(String namaSiswa, String judulTugas, Double nilai, String komentar) {
//             this.namaSiswa = namaSiswa;
//             this.judulTugas = judulTugas;
//             this.nilai = nilai;
//             this.komentar = komentar;
//         }

//         public String getNamaSiswa() { return namaSiswa; }
//         public String getJudulTugas() { return judulTugas; }
//         public Double getNilai() { return nilai; }
//         public String getKomentar() { return komentar; }


   public List<LaporanNilai> getLaporanNilai() {
    List<LaporanNilai> list = new ArrayList<>();

    String sql = """
        SELECT u.nama, t.judul, pt.nilai, pt.komentar
        FROM pengumpulan_tugas pt
        JOIN users u ON pt.user_id = u.id
        JOIN tugas t ON pt.tugas_id = t.id
        WHERE pt.nilai IS NOT NULL
        ORDER BY u.nama
    """;

    try (Connection conn = DatabaseConnection.getConnection();
         PreparedStatement stmt = conn.prepareStatement(sql);
         ResultSet rs = stmt.executeQuery()) {

        while (rs.next()) {
            list.add(new LaporanNilai(
                rs.getString("nama"),
                rs.getString("judul"),
                rs.getDouble("nilai"),
                rs.getString("komentar")
            ));
        }

    } catch (SQLException e) {
        e.printStackTrace();
    }

    return list;
}
}


//     public static class PengumpulanDetail {
//         private final String namaSiswa, username, judulTugas, deskripsiTugas, mataKuliahNama, filePath;
//         private final java.time.LocalDateTime submittedAt;
//         private final Double nilai;
//         private final String komentar;

//         public PengumpulanDetail(int id, String namaSiswa, String username, String judulTugas,
//                                  String deskripsiTugas, String mataKuliahNama,
//                                  String filePath, java.time.LocalDateTime submittedAt,
//                                  Double nilai, String komentar) {
//             this.namaSiswa = namaSiswa;
//             this.username = username;
//             this.judulTugas = judulTugas;
//             this.deskripsiTugas = deskripsiTugas;
//             this.mataKuliahNama = mataKuliahNama;
//             this.filePath = filePath;
//             this.submittedAt = submittedAt;
//             this.nilai = nilai;
//             this.komentar = komentar;
//         }
//     }
// }

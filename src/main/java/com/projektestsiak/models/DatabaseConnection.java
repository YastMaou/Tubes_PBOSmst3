package com.projektestsiak.models;

import java.sql.*;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.mindrot.jbcrypt.BCrypt;

public class DatabaseConnection {
    private static HikariDataSource dataSource;
    private final String DB_NAME = "siak_db";

    public DatabaseConnection() throws SQLException {
        // Create database if not exists
        createDatabaseIfNotExists(DB_NAME);

        // Configure HikariCP
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl("jdbc:mysql://localhost:3306/" + DB_NAME);
        config.setUsername("root");
        config.setPassword("");
        config.addDataSourceProperty("cachePrepStmts", "true");
        config.addDataSourceProperty("prepStmtCacheSize", "250");
        config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");
        config.setMaximumPoolSize(20);
        config.setMinimumIdle(5);
        config.setConnectionTimeout(30000);
        config.setMaxLifetime(1800000);

        dataSource = new HikariDataSource(config);
        System.out.println("Connected to database '" + DB_NAME + "' with HikariCP!");

        // Create tables and sample data
        createTablesAndSampleData();
    }

    public static Connection getConnection() throws SQLException {
        return dataSource.getConnection();
    }

    private void createDatabaseIfNotExists(String dbName) {
        String sql = "CREATE DATABASE IF NOT EXISTS " + dbName;
        try (Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/", "root", "");
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.executeUpdate();
            System.out.println("Database '" + dbName + "' ready.");
        } catch (SQLException e) {
            System.err.println("Failed to create database: " + e.getMessage());
        }
    }

    private void createTablesAndSampleData() throws SQLException {
        try (Connection connection = getConnection()) {
            // Create users table
            String createUserTable = "CREATE TABLE IF NOT EXISTS users (" +
                "id INT AUTO_INCREMENT PRIMARY KEY, " +
                "nama VARCHAR(255) NOT NULL, " +
                "username VARCHAR(50) NOT NULL UNIQUE, " +
                "email VARCHAR(255) NOT NULL, " +
                "telepon VARCHAR(15) NOT NULL, " +
                "password VARCHAR(255) NOT NULL, " +
                "role VARCHAR(50) NOT NULL, " +
                "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP)";
            
            try (PreparedStatement stmt = connection.prepareStatement(createUserTable)) {
                stmt.executeUpdate();
            }

            // Create default users if not exist
            createDefaultUsers(connection);

            // Create academic tables
            createAcademicTables(connection);
        }
    }

    private void createDefaultUsers(Connection connection) throws SQLException {
        String checkUserSql = "SELECT COUNT(*) FROM users WHERE username = ?";
        String insertUserSql = "INSERT INTO users (nama, username, email, telepon, password, role) VALUES (?, ?, ?, ?, ?, ?)";

        // Check and create admin user
        try (PreparedStatement checkStmt = connection.prepareStatement(checkUserSql)) {
            checkStmt.setString(1, "admin");
            ResultSet rs = checkStmt.executeQuery();
            rs.next();
            if (rs.getInt(1) == 0) {
                try (PreparedStatement insertStmt = connection.prepareStatement(insertUserSql)) {
                    insertStmt.setString(1, "Administrator");
                    insertStmt.setString(2, "admin");
                    insertStmt.setString(3, "admin@siak.ac.id");
                    insertStmt.setString(4, "081111111111");
                    insertStmt.setString(5, BCrypt.hashpw("admin123", BCrypt.gensalt()));
                    insertStmt.setString(6, "admin");
                    insertStmt.executeUpdate();
                    System.out.println("Admin user created.");
                }
            }

            // Check and create student user
                        String upsertStudent = """
            INSERT INTO users (nama, username, email, telepon, password, role)
            VALUES (?, ?, ?, ?, ?, ?)
            ON DUPLICATE KEY UPDATE
                nama = VALUES(nama),
                email = VALUES(email),
                telepon = VALUES(telepon),
                role = VALUES(role)
            """;

            try (PreparedStatement stmt = connection.prepareStatement(upsertStudent)) {
                stmt.setString(1, "Bahlil Lahadalia");
                stmt.setString(2, "student");
                stmt.setString(3, "student@siak.ac.id");
                stmt.setString(4, "082222222222");
                stmt.setString(5, BCrypt.hashpw("student123", BCrypt.gensalt()));
                stmt.setString(6, "student");
                stmt.executeUpdate();
            }

        }
    }

    private void createAcademicTables(Connection connection) throws SQLException {
        // Mata Kuliah table
        String createMataKuliahTable = "CREATE TABLE IF NOT EXISTS mata_kuliah (" +
            "id INT AUTO_INCREMENT PRIMARY KEY, " +
            "kode VARCHAR(20) UNIQUE, " +
            "nama VARCHAR(100), " +
            "sks INT, " +
            "semester INT, " +
            "dosen VARCHAR(100), " +
            "harga_sks DECIMAL(10,2) DEFAULT 250000.00)";
        
        // Jadwal table
        String createJadwalTable = "CREATE TABLE IF NOT EXISTS jadwal (" +
            "id INT AUTO_INCREMENT PRIMARY KEY, " +
            "mata_kuliah_id INT, " +
            "hari ENUM('Senin','Selasa','Rabu','Kamis','Jumat','Sabtu'), " +
            "jam_mulai TIME, " +
            "jam_selesai TIME, " +
            "ruangan VARCHAR(50), " +
            "FOREIGN KEY (mata_kuliah_id) REFERENCES mata_kuliah(id))";
        
        // Tugas table
        String createTugasTable = "CREATE TABLE IF NOT EXISTS tugas (" +
            "id INT AUTO_INCREMENT PRIMARY KEY, " +
            "mata_kuliah_id INT, " +
            "judul VARCHAR(200), " +
            "deskripsi TEXT, " +
            "deadline DATETIME, " +
            "file_path VARCHAR(255), " +
            "FOREIGN KEY (mata_kuliah_id) REFERENCES mata_kuliah(id))";
        
        // Pengumpulan Tugas table
        String createPengumpulanTable = "CREATE TABLE IF NOT EXISTS pengumpulan_tugas (" +
            "id INT AUTO_INCREMENT PRIMARY KEY, " +
            "tugas_id INT, " +
            "user_id INT, " +
            "file_path VARCHAR(255), " +
            "submitted_at DATETIME DEFAULT CURRENT_TIMESTAMP, " +
            "nilai DECIMAL(5,2), " +
            "komentar TEXT, " +
            "FOREIGN KEY (tugas_id) REFERENCES tugas(id), " +
            "FOREIGN KEY (user_id) REFERENCES users(id))";
        
        // Pelajaran Tambahan table
        String createPelajaranTambahanTable = "CREATE TABLE IF NOT EXISTS pelajaran_tambahan (" +
            "id INT AUTO_INCREMENT PRIMARY KEY, " +
            "user_id INT, " +
            "mata_kuliah_id INT, " +
            "status ENUM('pending','approved','rejected'), " +
            "bukti_pembayaran VARCHAR(255), " +
            "tanggal_daftar DATETIME DEFAULT CURRENT_TIMESTAMP, " +
            "FOREIGN KEY (user_id) REFERENCES users(id), " +
            "FOREIGN KEY (mata_kuliah_id) REFERENCES mata_kuliah(id))";

        // Execute table creation
        try (PreparedStatement stmt = connection.prepareStatement(createMataKuliahTable)) {
            stmt.executeUpdate();
        }
        try (PreparedStatement stmt = connection.prepareStatement(createJadwalTable)) {
            stmt.executeUpdate();
        }
        try (PreparedStatement stmt = connection.prepareStatement(createTugasTable)) {
            stmt.executeUpdate();
        }
        try (PreparedStatement stmt = connection.prepareStatement(createPengumpulanTable)) {
            stmt.executeUpdate();
        }
        try (PreparedStatement stmt = connection.prepareStatement(createPelajaranTambahanTable)) {
            stmt.executeUpdate();
        }

        // Insert sample data
        insertSampleData(connection);
    }

    private void insertSampleData(Connection connection) throws SQLException {
        // Insert sample mata kuliah
        String[] mataKuliahData = {
            "INSERT IGNORE INTO mata_kuliah (kode, nama, sks, semester, dosen, harga_sks) VALUES ('MK001', 'Matematika Dasar', 3, 1, 'Dr. Ahmad', 250000)",
            "INSERT IGNORE INTO mata_kuliah (kode, nama, sks, semester, dosen, harga_sks) VALUES ('MK002', 'Pemrograman Java', 4, 1, 'Prof. Budi', 250000)",
            "INSERT IGNORE INTO mata_kuliah (kode, nama, sks, semester, dosen, harga_sks) VALUES ('MK003', 'Basis Data', 3, 2, 'Dr. Citra', 250000)",
            "INSERT IGNORE INTO mata_kuliah (kode, nama, sks, semester, dosen, harga_sks) VALUES ('MK004', 'Pemrograman Web', 3, 2, 'Dr. Dian', 250000)",
            "INSERT IGNORE INTO mata_kuliah (kode, nama, sks, semester, dosen, harga_sks) VALUES ('MK005', 'AI Fundamentals', 2, 3, 'Prof. Eko', 300000)"
        };
        
        for (String sql : mataKuliahData) {
            try (PreparedStatement stmt = connection.prepareStatement(sql)) {
                stmt.executeUpdate();
            }
        }
        
        // Insert sample jadwal
        String[] jadwalData = {
            "INSERT IGNORE INTO jadwal (mata_kuliah_id, hari, jam_mulai, jam_selesai, ruangan) VALUES (1, 'Senin', '08:00', '10:00', 'A101')",
            "INSERT IGNORE INTO jadwal (mata_kuliah_id, hari, jam_mulai, jam_selesai, ruangan) VALUES (2, 'Senin', '10:00', '12:00', 'Lab Komputer 1')",
            "INSERT IGNORE INTO jadwal (mata_kuliah_id, hari, jam_mulai, jam_selesai, ruangan) VALUES (3, 'Selasa', '08:00', '10:00', 'B201')",
            "INSERT IGNORE INTO jadwal (mata_kuliah_id, hari, jam_mulai, jam_selesai, ruangan) VALUES (4, 'Rabu', '13:00', '15:00', 'Lab Komputer 2')"
        };
        
        for (String sql : jadwalData) {
            try (PreparedStatement stmt = connection.prepareStatement(sql)) {
                stmt.executeUpdate();
            }
        }
        
        // Insert sample tugas
        String[] tugasData = {
            "INSERT IGNORE INTO tugas (mata_kuliah_id, judul, deskripsi, deadline) VALUES (1, 'Tugas Aljabar', 'Selesaikan soal halaman 45-50', '2024-12-20 23:59:00')",
            "INSERT IGNORE INTO tugas (mata_kuliah_id, judul, deskripsi, deadline) VALUES (2, 'Program Kalkulator', 'Buat kalkulator sederhana dengan Java', '2024-12-25 23:59:00')",
            "INSERT IGNORE INTO tugas (mata_kuliah_id, judul, deskripsi, deadline) VALUES (3, 'Design Database', 'Buat ERD untuk sistem perpustakaan', '2024-12-18 23:59:00')"
        };
        
        for (String sql : tugasData) {
            try (PreparedStatement stmt = connection.prepareStatement(sql)) {
                stmt.executeUpdate();
            }
        }
        
        System.out.println("Sample data inserted successfully.");
    }
}
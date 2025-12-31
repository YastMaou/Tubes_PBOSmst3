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

        // Create tables (tanpa data dummy)
        createTables();
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

    private void createTables() throws SQLException {
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

            // Create mata_kuliah table
            String createMataKuliahTable = "CREATE TABLE IF NOT EXISTS mata_kuliah (" +
                    "id INT AUTO_INCREMENT PRIMARY KEY, " +
                    "kode VARCHAR(20) UNIQUE, " +
                    "nama VARCHAR(100), " +
                    "sks INT, " +
                    "semester INT, " +
                    "dosen VARCHAR(100), " +
                    "harga_sks DECIMAL(10,2) DEFAULT 250000.00)";

            // Create jadwal table
            String createJadwalTable = "CREATE TABLE IF NOT EXISTS jadwal (" +
                    "id INT AUTO_INCREMENT PRIMARY KEY, " +
                    "mata_kuliah_id INT, " +
                    "hari ENUM('Senin','Selasa','Rabu','Kamis','Jumat','Sabtu'), " +
                    "jam_mulai TIME, " +
                    "jam_selesai TIME, " +
                    "ruangan VARCHAR(50), " +
                    "FOREIGN KEY (mata_kuliah_id) REFERENCES mata_kuliah(id))";

            // Create tugas table
            String createTugasTable = "CREATE TABLE IF NOT EXISTS tugas (" +
                    "id INT AUTO_INCREMENT PRIMARY KEY, " +
                    "mata_kuliah_id INT, " +
                    "judul VARCHAR(200), " +
                    "deskripsi TEXT, " +
                    "deadline DATETIME, " +
                    "file_path VARCHAR(255), " +
                    "FOREIGN KEY (mata_kuliah_id) REFERENCES mata_kuliah(id))";

            // Create pengumpulan_tugas table
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

            // Create pelajaran_tambahan table
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
            try (PreparedStatement stmt = connection.prepareStatement(createUserTable)) {
                stmt.executeUpdate();
            }
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

            System.out.println("All tables created successfully.");

            // Insert essential data only if tables are empty
            insertEssentialData(connection);

        }
    }

    private void insertEssentialData(Connection connection) throws SQLException {
        // Cek jika tabel users sudah ada data
        String checkUsersSql = "SELECT COUNT(*) FROM users";
        try (PreparedStatement stmt = connection.prepareStatement(checkUsersSql);
             ResultSet rs = stmt.executeQuery()) {
            
            rs.next();
            int userCount = rs.getInt(1);
            
            // Jika tabel kosong, masukkan data essential
            if (userCount == 0) {
                System.out.println("Inserting essential data...");
                insertAdminUser(connection);
                insertStudentUsers(connection);
                insertEssentialCourses(connection);
                insertEssentialSchedules(connection);
                insertEssentialAssignments(connection);
                System.out.println("Essential data inserted.");
            } else {
                System.out.println("Database already has data (" + userCount + " users). Skipping data insertion.");
                
                // Cek dan tambahkan user Budi Arie jika belum ada
                checkAndAddBudiArie(connection);
            }
        }
    }

    private void insertAdminUser(Connection connection) throws SQLException {
        String insertUserSql = "INSERT INTO users (nama, username, email, telepon, password, role) " +
                "VALUES (?, ?, ?, ?, ?, ?)";
        
        try (PreparedStatement stmt = connection.prepareStatement(insertUserSql)) {
            stmt.setString(1, "Administrator");
            stmt.setString(2, "admin");
            stmt.setString(3, "admin@siak.ac.id");
            stmt.setString(4, "081111111111");
            stmt.setString(5, BCrypt.hashpw("admin123", BCrypt.gensalt()));
            stmt.setString(6, "admin");
            stmt.executeUpdate();
            System.out.println("Admin user created.");
        }
    }

    private void insertStudentUsers(Connection connection) throws SQLException {
        String insertUserSql = "INSERT INTO users (nama, username, email, telepon, password, role) " +
                "VALUES (?, ?, ?, ?, ?, ?)";
        
        // Hanya buat 2 student: Bahlil Lahadalia dan Budi Arie
        String[][] students = {
            {"Bahlil Lahadalia", "bahlil", "bahlil@siak.ac.id", "082222222222", "student123", "student"},
            {"Budi Arie", "budiarie", "budiarie@siak.ac.id", "083333333333", "budipass", "student"}
        };
        
        for (String[] student : students) {
            try (PreparedStatement stmt = connection.prepareStatement(insertUserSql)) {
                stmt.setString(1, student[0]); // nama
                stmt.setString(2, student[1]); // username
                stmt.setString(3, student[2]); // email
                stmt.setString(4, student[3]); // telepon
                stmt.setString(5, BCrypt.hashpw(student[4], BCrypt.gensalt())); // password
                stmt.setString(6, student[5]); // role
                stmt.executeUpdate();
                System.out.println("Student user created: " + student[0]);
            }
        }
    }

    private void checkAndAddBudiArie(Connection connection) throws SQLException {
        // Cek jika user Budi Arie sudah ada
        String checkSql = "SELECT COUNT(*) FROM users WHERE username = 'budiarie'";
        try (PreparedStatement stmt = connection.prepareStatement(checkSql);
             ResultSet rs = stmt.executeQuery()) {
            
            rs.next();
            int count = rs.getInt(1);
            
            // Jika Budi Arie belum ada, tambahkan
            if (count == 0) {
                String insertSql = "INSERT INTO users (nama, username, email, telepon, password, role) " +
                        "VALUES (?, ?, ?, ?, ?, ?)";
                
                try (PreparedStatement insertStmt = connection.prepareStatement(insertSql)) {
                    insertStmt.setString(1, "Budi Arie");
                    insertStmt.setString(2, "budiarie");
                    insertStmt.setString(3, "budiarie@siak.ac.id");
                    insertStmt.setString(4, "083333333333");
                    insertStmt.setString(5, BCrypt.hashpw("budipass", BCrypt.gensalt()));
                    insertStmt.setString(6, "student");
                    insertStmt.executeUpdate();
                    System.out.println("Added missing user: Budi Arie");
                }
            } else {
                System.out.println("User Budi Arie already exists.");
            }
        }
    }

    private void insertEssentialCourses(Connection connection) throws SQLException {
        // Cek jika tabel mata_kuliah kosong
        String checkSql = "SELECT COUNT(*) FROM mata_kuliah";
        try (PreparedStatement stmt = connection.prepareStatement(checkSql);
             ResultSet rs = stmt.executeQuery()) {
            
            rs.next();
            if (rs.getInt(1) == 0) {
                String[] courses = {
                    "INSERT INTO mata_kuliah (kode, nama, sks, semester, dosen, harga_sks) VALUES ('MK001', 'Matematika Dasar', 3, 1, 'Dr. Ahmad', 250000)",
                    "INSERT INTO mata_kuliah (kode, nama, sks, semester, dosen, harga_sks) VALUES ('MK002', 'Pemrograman Java', 4, 1, 'Prof. Budi', 250000)",
                    "INSERT INTO mata_kuliah (kode, nama, sks, semester, dosen, harga_sks) VALUES ('MK003', 'Basis Data', 3, 2, 'Dr. Citra', 250000)"
                };
                
                for (String sql : courses) {
                    try (PreparedStatement courseStmt = connection.prepareStatement(sql)) {
                        courseStmt.executeUpdate();
                    }
                }
                System.out.println("Essential courses created.");
            }
        }
    }

    private void insertEssentialSchedules(Connection connection) throws SQLException {
        // Cek jika tabel jadwal kosong
        String checkSql = "SELECT COUNT(*) FROM jadwal";
        try (PreparedStatement stmt = connection.prepareStatement(checkSql);
             ResultSet rs = stmt.executeQuery()) {
            
            rs.next();
            if (rs.getInt(1) == 0) {
                // Pastikan ada mata kuliah terlebih dahulu
                String checkCourses = "SELECT id FROM mata_kuliah LIMIT 1";
                try (PreparedStatement courseStmt = connection.prepareStatement(checkCourses);
                     ResultSet courseRs = courseStmt.executeQuery()) {
                    
                    if (courseRs.next()) {
                        int courseId = courseRs.getInt("id");
                        String insertSql = "INSERT INTO jadwal (mata_kuliah_id, hari, jam_mulai, jam_selesai, ruangan) " +
                                "VALUES (?, 'Senin', '08:00', '10:00', 'A101')";
                        
                        try (PreparedStatement insertStmt = connection.prepareStatement(insertSql)) {
                            insertStmt.setInt(1, courseId);
                            insertStmt.executeUpdate();
                            System.out.println("Essential schedule created.");
                        }
                    }
                }
            }
        }
    }

    private void insertEssentialAssignments(Connection connection) throws SQLException {
        // Cek jika tabel tugas kosong
        String checkSql = "SELECT COUNT(*) FROM tugas";
        try (PreparedStatement stmt = connection.prepareStatement(checkSql);
             ResultSet rs = stmt.executeQuery()) {
            
            rs.next();
            if (rs.getInt(1) == 0) {
                // Pastikan ada mata kuliah terlebih dahulu
                String checkCourses = "SELECT id FROM mata_kuliah LIMIT 1";
                try (PreparedStatement courseStmt = connection.prepareStatement(checkCourses);
                     ResultSet courseRs = courseStmt.executeQuery()) {
                    
                    if (courseRs.next()) {
                        int courseId = courseRs.getInt("id");
                        String insertSql = "INSERT INTO tugas (mata_kuliah_id, judul, deskripsi, deadline) " +
                                "VALUES (?, 'Tugas Pertama', 'Ini adalah tugas pertama', DATE_ADD(NOW(), INTERVAL 7 DAY))";
                        
                        try (PreparedStatement insertStmt = connection.prepareStatement(insertSql)) {
                            insertStmt.setInt(1, courseId);
                            insertStmt.executeUpdate();
                            System.out.println("Essential assignment created.");
                        }
                    }
                }
            }
        }
    }

    // Method untuk seed data testing (opsional, dipanggil manual jika perlu)
    public void seedTestData() throws SQLException {
        try (Connection connection = getConnection()) {
            System.out.println("Seeding test data...");
            
            // Tambahkan lebih banyak mata kuliah untuk testing
            String[] additionalCourses = {
                "INSERT IGNORE INTO mata_kuliah (kode, nama, sks, semester, dosen, harga_sks) VALUES ('MK004', 'Pemrograman Web', 3, 2, 'Dr. Dian', 250000)",
                "INSERT IGNORE INTO mata_kuliah (kode, nama, sks, semester, dosen, harga_sks) VALUES ('MK005', 'AI Fundamentals', 2, 3, 'Prof. Eko', 300000)"
            };
            
            for (String sql : additionalCourses) {
                try (PreparedStatement stmt = connection.prepareStatement(sql)) {
                    stmt.executeUpdate();
                }
            }
            
            // Tambahkan jadwal untuk testing
            String checkJadwal = "SELECT COUNT(*) FROM jadwal";
            try (PreparedStatement stmt = connection.prepareStatement(checkJadwal);
                 ResultSet rs = stmt.executeQuery()) {
                
                rs.next();
                if (rs.getInt(1) < 3) {
                    String[] jadwalData = {
                        "INSERT IGNORE INTO jadwal (mata_kuliah_id, hari, jam_mulai, jam_selesai, ruangan) " +
                        "SELECT id, 'Selasa', '10:00', '12:00', 'Lab 1' FROM mata_kuliah WHERE kode = 'MK002'",
                        "INSERT IGNORE INTO jadwal (mata_kuliah_id, hari, jam_mulai, jam_selesai, ruangan) " +
                        "SELECT id, 'Rabu', '13:00', '15:00', 'B201' FROM mata_kuliah WHERE kode = 'MK003'"
                    };
                    
                    for (String sql : jadwalData) {
                        try (PreparedStatement jadwalStmt = connection.prepareStatement(sql)) {
                            jadwalStmt.executeUpdate();
                        }
                    }
                }
            }
            
            // Tambahkan tugas untuk testing
            String checkTugas = "SELECT COUNT(*) FROM tugas";
            try (PreparedStatement stmt = connection.prepareStatement(checkTugas);
                 ResultSet rs = stmt.executeQuery()) {
                
                rs.next();
                if (rs.getInt(1) < 3) {
                    String[] tugasData = {
                        "INSERT IGNORE INTO tugas (mata_kuliah_id, judul, deskripsi, deadline) " +
                        "SELECT id, 'Tugas Java', 'Buat program kalkulator', DATE_ADD(NOW(), INTERVAL 10 DAY) " +
                        "FROM mata_kuliah WHERE kode = 'MK002'",
                        "INSERT IGNORE INTO tugas (mata_kuliah_id, judul, deskripsi, deadline) " +
                        "SELECT id, 'Tugas Database', 'Buat ERD sistem', DATE_ADD(NOW(), INTERVAL 5 DAY) " +
                        "FROM mata_kuliah WHERE kode = 'MK003'"
                    };
                    
                    for (String sql : tugasData) {
                        try (PreparedStatement tugasStmt = connection.prepareStatement(sql)) {
                            tugasStmt.executeUpdate();
                        }
                    }
                }
            }
            
            System.out.println("Test data seeded successfully.");
        }
    }
}
package com.projektestsiak.models;

import org.mindrot.jbcrypt.BCrypt;
import java.sql.*;

public class LoginModel {
    private final DatabaseConnection dbConnection;

    public LoginModel() throws SQLException {
        this.dbConnection = new DatabaseConnection();
    }

    public boolean verifyLogin(String username, String password) {
        String query = "SELECT id, username, password, role, nama FROM users WHERE username = ?";
        
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            
            stmt.setString(1, username);
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                String storedPasswordHash = rs.getString("password");
                if (BCrypt.checkpw(password, storedPasswordHash)) {
                    // Set session
                    SessionManager.setCurrentUserId(rs.getInt("id"));
                    SessionManager.setCurrentUsername(rs.getString("username"));
                    SessionManager.setCurrentRole(rs.getString("role"));
                    SessionManager.setCurrentNama(rs.getString("nama"));
                    return true;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public String getUserRole(String username) {
        String query = "SELECT role FROM users WHERE username = ?";
        
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            
            stmt.setString(1, username);
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                return rs.getString("role");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }
}
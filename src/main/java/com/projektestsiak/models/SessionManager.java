package com.projektestsiak.models;

public class SessionManager {
    private static int currentUserId = -1;
    private static String currentUsername = "";
    private static String currentRole = "";
    private static String currentNama = "";

    public static int getCurrentUserId() {
        return currentUserId;
    }

    public static void setCurrentUserId(int userId) {
        currentUserId = userId;
    }

    public static String getCurrentUsername() {
        return currentUsername;
    }

    public static void setCurrentUsername(String username) {
        currentUsername = username;
    }

    public static String getCurrentRole() {
        return currentRole;
    }

    public static void setCurrentRole(String role) {
        currentRole = role;
    }

    public static String getCurrentNama() {
        return currentNama;
    }

    public static void setCurrentNama(String nama) {
        currentNama = nama;
    }

    public static boolean isUserLoggedIn() {
        return currentUserId != -1;
    }

    public static boolean isAdmin() {
        return "admin".equals(currentRole);
    }

    public static boolean isStudent() {
        return "student".equals(currentRole);
    }

    public static void logout() {
        currentUserId = -1;
        currentUsername = "";
        currentRole = "";
        currentNama = "";
    }
}
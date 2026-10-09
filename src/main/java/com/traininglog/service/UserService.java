package com.traininglog.service;

import com.traininglog.util.DBUtil;
import com.traininglog.util.PasswordUtil;

import java.sql.*;
import java.util.UUID;

public class UserService {

    public static class AuthUser {
        public int id;
        public String fullName;
        public String passwordHash;
        /** True only when findOrCreateByGoogle just inserted a brand-new row. */
        public boolean isNewUser;
    }

    /** Used by LoginServlet - looks a user up by email instead of username. */
    public static AuthUser findByEmail(String email) throws SQLException {
        String sql = "SELECT id, full_name, password_hash FROM users WHERE email = ?";
        try (Connection con = DBUtil.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, email);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return null;
                AuthUser u = new AuthUser();
                u.id = rs.getInt("id");
                u.fullName = rs.getString("full_name");
                u.passwordHash = rs.getString("password_hash");
                return u;
            }
        }
    }

    /** Used by GoogleLoginServlet - finds or creates an account for a verified Google email. */
    public static AuthUser findOrCreateByGoogle(String email, String fullName, String googleSub) throws SQLException {
        AuthUser existing = findByEmail(email);
        if (existing != null) {
            // link the Google account to the existing row if not already linked
            try (Connection con = DBUtil.getConnection();
                 PreparedStatement ps = con.prepareStatement(
                     "UPDATE users SET google_id = ? WHERE id = ? AND google_id IS NULL")) {
                ps.setString(1, googleSub);
                ps.setInt(2, existing.id);
                ps.executeUpdate();
            }
            return existing;
        }

        String username = generateUniqueUsername(email.substring(0, email.indexOf('@')));
        String randomPassword = PasswordUtil.hash(UUID.randomUUID().toString());

        String sql = "INSERT INTO users (full_name, email, username, password_hash, google_id) VALUES (?, ?, ?, ?, ?)";
        try (Connection con = DBUtil.getConnection();
             PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, fullName);
            ps.setString(2, email);
            ps.setString(3, username);
            ps.setString(4, randomPassword);
            ps.setString(5, googleSub);
            ps.executeUpdate();

            try (ResultSet keys = ps.getGeneratedKeys()) {
                keys.next();
                AuthUser u = new AuthUser();
                u.id = keys.getInt(1);
                u.fullName = fullName;
                u.isNewUser = true;
                return u;
            }
        }
    }

    /** Used by ForgotPasswordServlet - overwrites the password hash for an existing account. */
    public static boolean resetPassword(String email, String newHash) throws SQLException {
        String sql = "UPDATE users SET password_hash = ? WHERE email = ?";
        try (Connection con = DBUtil.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, newHash);
            ps.setString(2, email);
            return ps.executeUpdate() > 0;
        }
    }

    private static String generateUniqueUsername(String base) throws SQLException {
        String cleanBase = base.replaceAll("[^a-zA-Z0-9._]", "").toLowerCase();
        if (cleanBase.isEmpty()) cleanBase = "user";

        String candidate = cleanBase;
        try (Connection con = DBUtil.getConnection()) {
            int suffix = 0;
            while (usernameExists(con, candidate)) {
                suffix++;
                candidate = cleanBase + suffix;
            }
        }
        return candidate;
    }

    private static boolean usernameExists(Connection con, String username) throws SQLException {
        try (PreparedStatement ps = con.prepareStatement("SELECT 1 FROM users WHERE username = ?")) {
            ps.setString(1, username);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }
}

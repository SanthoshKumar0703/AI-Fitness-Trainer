package com.traininglog.service;

import com.traininglog.model.WorkoutEntry;
import com.traininglog.util.DBUtil;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class FitnessService {

    public static Map<String, Object> getProfile(int userId) throws SQLException {
        String sql = "SELECT u.full_name, u.age, u.goal, p.height_cm, p.weight_kg " +
                     "FROM users u LEFT JOIN profiles p ON p.user_id = u.id WHERE u.id = ?";
        try (Connection con = DBUtil.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                Map<String, Object> profile = new LinkedHashMap<>();
                if (rs.next()) {
                    profile.put("fullName", rs.getString("full_name"));
                    profile.put("age", rs.getObject("age"));
                    profile.put("goal", rs.getString("goal"));
                    profile.put("height", rs.getObject("height_cm"));
                    profile.put("weight", rs.getObject("weight_kg"));
                }
                return profile;
            }
        }
    }

    public static void saveProfile(int userId, int age, String goal, double height, double weight) throws SQLException {
        String updateUser = "UPDATE users SET age = ?, goal = ? WHERE id = ?";
        String upsertProfile =
            "INSERT INTO profiles (user_id, height_cm, weight_kg) VALUES (?, ?, ?) " +
            "ON DUPLICATE KEY UPDATE height_cm = VALUES(height_cm), weight_kg = VALUES(weight_kg)";

        try (Connection con = DBUtil.getConnection()) {
            try (PreparedStatement ps1 = con.prepareStatement(updateUser)) {
                ps1.setInt(1, age);
                ps1.setString(2, goal);
                ps1.setInt(3, userId);
                ps1.executeUpdate();
            }
            try (PreparedStatement ps2 = con.prepareStatement(upsertProfile)) {
                ps2.setInt(1, userId);
                ps2.setDouble(2, height);
                ps2.setDouble(3, weight);
                ps2.executeUpdate();
            }
        }
    }

    private static final Map<String, Integer> LEVEL_CALORIES = Map.of(
        "beginner", 170,
        "intermediate", 305,
        "advanced", 515
    );

    public static int caloriesForLevel(String level) {
        return LEVEL_CALORIES.getOrDefault(level, 150);
    }

    public static void insertWorkout(int userId, String level) throws SQLException {
        String sql = "INSERT INTO workout_log (user_id, level, calories, log_date) VALUES (?, ?, ?, CURDATE())";
        try (Connection con = DBUtil.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.setString(2, level);
            ps.setInt(3, caloriesForLevel(level));
            ps.executeUpdate();
        }
    }

    public static int getTodayWorkoutCount(int userId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM workout_log WHERE user_id = ? AND log_date = CURDATE()";
        try (Connection con = DBUtil.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getInt(1) : 0;
            }
        }
    }

    public static int getTodayCaloriesBurned(int userId) throws SQLException {
        String sql = "SELECT COALESCE(SUM(calories),0) FROM workout_log WHERE user_id = ? AND log_date = CURDATE()";
        try (Connection con = DBUtil.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getInt(1) : 0;
            }
        }
    }

    public static List<WorkoutEntry> getRecentWorkouts(int userId, int limit) throws SQLException {
        String sql = "SELECT id, level, calories, log_date FROM workout_log " +
                     "WHERE user_id = ? ORDER BY id DESC LIMIT ?";
        List<WorkoutEntry> list = new ArrayList<>();
        try (Connection con = DBUtil.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.setInt(2, limit);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(new WorkoutEntry(
                        rs.getInt("id"), rs.getString("level"),
                        rs.getInt("calories"), rs.getDate("log_date")));
                }
            }
        }
        return list;
    }

    public static int getStreak(int userId) throws SQLException {
        String sql = "SELECT DISTINCT log_date FROM workout_log WHERE user_id = ? ORDER BY log_date DESC";
        List<LocalDate> dates = new ArrayList<>();
        try (Connection con = DBUtil.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    dates.add(rs.getDate("log_date").toLocalDate());
                }
            }
        }
        if (dates.isEmpty()) return 0;

        LocalDate cursor = LocalDate.now();
        int streak = 0;
        if (!dates.contains(cursor)) cursor = cursor.minusDays(1);
        for (LocalDate d : dates) {
            if (d.equals(cursor)) {
                streak++;
                cursor = cursor.minusDays(1);
            } else if (d.isBefore(cursor)) {
                break;
            }
        }
        return streak;
    }

    public static Map<String, Integer> getWeeklyWorkoutCounts(int userId) throws SQLException {
        Map<String, Integer> result = new LinkedHashMap<>();
        LocalDate start = LocalDate.now().minusDays(6);
        for (int i = 0; i < 7; i++) result.put(start.plusDays(i).toString(), 0);

        String sql = "SELECT log_date, COUNT(*) c FROM workout_log " +
                     "WHERE user_id = ? AND log_date >= ? GROUP BY log_date";
        try (Connection con = DBUtil.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.setDate(2, Date.valueOf(start));
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    result.put(rs.getDate("log_date").toLocalDate().toString(), rs.getInt("c"));
                }
            }
        }
        return result;
    }

    public static int getTodayWater(int userId) throws SQLException {
        String sql = "SELECT glasses FROM water_log WHERE user_id = ? AND log_date = CURDATE()";
        try (Connection con = DBUtil.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getInt(1) : 0;
            }
        }
    }

    public static void addWater(int userId) throws SQLException {
        String sql = "INSERT INTO water_log (user_id, log_date, glasses) VALUES (?, CURDATE(), 1) " +
                     "ON DUPLICATE KEY UPDATE glasses = LEAST(glasses + 1, 8)";
        try (Connection con = DBUtil.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.executeUpdate();
        }
    }

    public static void resetWater(int userId) throws SQLException {
        String sql = "INSERT INTO water_log (user_id, log_date, glasses) VALUES (?, CURDATE(), 0) " +
                     "ON DUPLICATE KEY UPDATE glasses = 0";
        try (Connection con = DBUtil.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.executeUpdate();
        }
    }
}

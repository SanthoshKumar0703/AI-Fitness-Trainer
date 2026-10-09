package com.traininglog.servlet;

import com.traininglog.service.FitnessService;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Map;

@WebServlet("/dashboard")
public class DashboardServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session = request.getSession();
        int userId = (int) session.getAttribute("userId");

        try {
            Map<String, Object> profile = FitnessService.getProfile(userId);
            int workoutCount = FitnessService.getTodayWorkoutCount(userId);
            int caloriesToday = FitnessService.getTodayCaloriesBurned(userId);
            int waterToday = FitnessService.getTodayWater(userId);
            int streak = FitnessService.getStreak(userId);

            request.setAttribute("profile", profile);
            request.setAttribute("workoutCount", workoutCount);
            request.setAttribute("caloriesToday", caloriesToday);
            request.setAttribute("waterToday", waterToday);
            request.setAttribute("streak", streak);
            request.setAttribute("recentWorkouts", FitnessService.getRecentWorkouts(userId, 8));
            request.setAttribute("badgeStreak3", streak >= 3);
            request.setAttribute("badgeStreak7", streak >= 7);
            request.setAttribute("badgeFirstWorkout", workoutCount >= 1);

            Object h = profile.get("height");
            Object w = profile.get("weight");
            if (h != null && w != null) {
                double height = ((Number) h).doubleValue() / 100.0;
                double weight = ((Number) w).doubleValue();
                double bmi = weight / (height * height);
                request.setAttribute("bmi", String.format("%.1f", bmi));
            }

            request.getRequestDispatcher("dashboard.jsp").forward(request, response);
        } catch (SQLException e) {
            throw new ServletException("Could not load dashboard data", e);
        }
    }
}

package com.traininglog.servlet;

import com.traininglog.service.FitnessService;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.SQLException;
import java.util.Map;

/** Returns the last 7 days of workout counts as JSON for the dashboard chart. */
@WebServlet("/progress")
public class ProgressServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session = request.getSession();
        int userId = (int) session.getAttribute("userId");
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        try {
            Map<String, Integer> weekly = FitnessService.getWeeklyWorkoutCounts(userId);
            StringBuilder labels = new StringBuilder();
            StringBuilder values = new StringBuilder();
            boolean first = true;
            for (Map.Entry<String, Integer> e : weekly.entrySet()) {
                if (!first) { labels.append(","); values.append(","); }
                labels.append("\"").append(e.getKey().substring(5)).append("\"");
                values.append(e.getValue());
                first = false;
            }
            String json = "{\"labels\":[" + labels + "],\"values\":[" + values + "]}";

            try (PrintWriter out = response.getWriter()) {
                out.print(json);
            }
        } catch (SQLException e) {
            throw new ServletException("Could not load progress data", e);
        }
    }
}

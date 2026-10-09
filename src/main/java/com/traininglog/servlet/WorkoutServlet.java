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

@WebServlet("/workout")
public class WorkoutServlet extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session = request.getSession();
        int userId = (int) session.getAttribute("userId");
        String level = request.getParameter("level");
        if (level == null || !(level.equals("beginner") || level.equals("intermediate") || level.equals("advanced"))) {
            level = "beginner";
        }

        try {
            FitnessService.insertWorkout(userId, level);
            response.sendRedirect(request.getContextPath() + "/dashboard#workout");
        } catch (SQLException e) {
            throw new ServletException("Could not log workout", e);
        }
    }
}

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

@WebServlet("/water")
public class WaterServlet extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session = request.getSession();
        int userId = (int) session.getAttribute("userId");
        String action = request.getParameter("action");

        try {
            if ("reset".equals(action)) {
                FitnessService.resetWater(userId);
            } else {
                FitnessService.addWater(userId);
            }
            response.sendRedirect(request.getContextPath() + "/dashboard#hydration");
        } catch (SQLException e) {
            throw new ServletException("Could not update water log", e);
        }
    }
}

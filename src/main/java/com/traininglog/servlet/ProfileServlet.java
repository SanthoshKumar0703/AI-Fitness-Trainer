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

@WebServlet("/profile")
public class ProfileServlet extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session = request.getSession();
        int userId = (int) session.getAttribute("userId");

        try {
            int age = parseIntOrZero(request.getParameter("age"));
            String goal = request.getParameter("goal");
            double height = parseDoubleOrZero(request.getParameter("height"));
            double weight = parseDoubleOrZero(request.getParameter("weight"));

            FitnessService.saveProfile(userId, age, goal, height, weight);
            response.sendRedirect(request.getContextPath() + "/dashboard#profile");
        } catch (SQLException e) {
            throw new ServletException("Could not save profile", e);
        }
    }

    private int parseIntOrZero(String s) {
        try { return Integer.parseInt(s); } catch (Exception e) { return 0; }
    }
    private double parseDoubleOrZero(String s) {
        try { return Double.parseDouble(s); } catch (Exception e) { return 0; }
    }
}

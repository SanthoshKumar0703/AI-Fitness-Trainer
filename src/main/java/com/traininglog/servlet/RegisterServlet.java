package com.traininglog.servlet;

import com.traininglog.util.DBUtil;
import com.traininglog.util.PasswordUtil;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Connection;

@WebServlet("/register")
public class RegisterServlet extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String fullname = request.getParameter("fullname");
        String email = request.getParameter("email");
        String username = request.getParameter("username");
        String password = request.getParameter("password");
        String confirm = request.getParameter("confirm");
        String ageStr = request.getParameter("age");
        String goal = request.getParameter("goal");

        if (isBlank(fullname) || isBlank(email) || isBlank(username) || isBlank(password)
                || isBlank(confirm) || isBlank(ageStr) || isBlank(goal)) {
            request.setAttribute("error", "Please fill in every field.");
            forwardToRegister(request, response);
            return;
        }
        if (!password.equals(confirm)) {
            request.setAttribute("error", "Passwords do not match.");
            forwardToRegister(request, response);
            return;
        }
        if (password.length() < 6) {
            request.setAttribute("error", "Password should be at least 6 characters.");
            forwardToRegister(request, response);
            return;
        }

        String sql = "INSERT INTO users (full_name, email, username, password_hash, age, goal) VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection con = DBUtil.getConnection();
             PreparedStatement ps = con.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, fullname);
            ps.setString(2, email);
            ps.setString(3, username);
            ps.setString(4, PasswordUtil.hash(password));
            ps.setInt(5, Integer.parseInt(ageStr));
            ps.setString(6, goal);
            ps.executeUpdate();

            int newUserId;
            try (ResultSet keys = ps.getGeneratedKeys()) {
                keys.next();
                newUserId = keys.getInt(1);
            }

            // Sign the new account straight in instead of bouncing them to the
            // login page - they just typed this password, no need to ask again.
            HttpSession session = request.getSession(true);
            session.setAttribute("userId", newUserId);
            session.setAttribute("fullName", fullname);
            response.sendRedirect(request.getContextPath() + "/dashboard");
        } catch (SQLException e) {
            if (e.getMessage() != null && e.getMessage().toLowerCase().contains("duplicate")) {
                request.setAttribute("error", "That username or email is already registered.");
            } else {
                request.setAttribute("error", "Registration failed. Please try again.");
            }
            forwardToRegister(request, response);
        }
    }

    private void forwardToRegister(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.getRequestDispatcher("register.jsp").forward(request, response);
    }

    private boolean isBlank(String s) {
        return s == null || s.trim().isEmpty();
    }
}

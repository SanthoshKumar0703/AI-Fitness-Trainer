package com.traininglog.servlet;

import com.traininglog.service.UserService;
import com.traininglog.util.PasswordUtil;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.sql.SQLException;

@WebServlet("/login")
public class LoginServlet extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String email = request.getParameter("email");
        String password = request.getParameter("password");

        try {
            UserService.AuthUser user = UserService.findByEmail(email);
            if (user != null && user.passwordHash.equals(PasswordUtil.hash(password))) {
                HttpSession session = request.getSession(true);
                session.setAttribute("userId", user.id);
                session.setAttribute("fullName", user.fullName);
                response.sendRedirect("dashboard");
                return;
            }
        } catch (SQLException e) {
            request.setAttribute("error", "Login failed. Please try again.");
            request.getRequestDispatcher("login.jsp").forward(request, response);
            return;
        }

        request.setAttribute("error", "Incorrect email or password.");
        request.getRequestDispatcher("login.jsp").forward(request, response);
    }
}


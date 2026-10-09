package com.traininglog.servlet;

import com.traininglog.service.UserService;
import com.traininglog.util.PasswordUtil;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.SQLException;

@WebServlet("/forgot-password")
public class ForgotPasswordServlet extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String email = request.getParameter("email");
        String password = request.getParameter("password");
        String confirm = request.getParameter("confirm");

        if (email == null || email.trim().isEmpty()
                || password == null || password.isEmpty()
                || confirm == null || confirm.isEmpty()) {
            fail(request, response, "Please fill in every field.");
            return;
        }
        if (!password.equals(confirm)) {
            fail(request, response, "Passwords don't match.");
            return;
        }
        if (password.length() < 6) {
            fail(request, response, "Password must be at least 6 characters.");
            return;
        }

        try {
            boolean updated = UserService.resetPassword(email.trim(), PasswordUtil.hash(password));
            if (!updated) {
                fail(request, response, "No account found with that email.");
                return;
            }
            response.sendRedirect(request.getContextPath() + "/login.jsp?reset=1");
        } catch (SQLException e) {
            fail(request, response, "Something went wrong. Please try again.");
        }
    }

    private void fail(HttpServletRequest request, HttpServletResponse response, String message)
            throws ServletException, IOException {
        request.setAttribute("error", message);
        request.getRequestDispatcher("forgot-password.jsp").forward(request, response);
    }
}

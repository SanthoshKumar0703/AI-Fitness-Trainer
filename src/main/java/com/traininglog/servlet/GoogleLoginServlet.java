package com.traininglog.servlet;

import com.traininglog.service.UserService;
import com.traininglog.util.AppConfig;
import org.json.JSONObject;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.sql.SQLException;
import java.time.Duration;

/**
 * Handles the POST from the "Sign in with Google" button (login.jsp / register.jsp).
 * The browser gives us a signed ID token (JWT) - we ask Google to verify it rather
 * than decoding/verifying the signature ourselves, which keeps this simple and safe.
 */
@WebServlet("/google-login")
public class GoogleLoginServlet extends HttpServlet {

    private static final HttpClient HTTP = HttpClient.newBuilder()
        .connectTimeout(Duration.ofSeconds(8))
        .build();

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String idToken = request.getParameter("credential");
        if (idToken == null || idToken.isEmpty()) {
            response.sendRedirect("login.jsp?error=google");
            return;
        }

        try {
            HttpRequest verifyRequest = HttpRequest.newBuilder()
                .uri(URI.create("https://oauth2.googleapis.com/tokeninfo?id_token=" + idToken))
                .timeout(Duration.ofSeconds(8))
                .GET()
                .build();
            HttpResponse<String> verifyResponse = HTTP.send(verifyRequest, HttpResponse.BodyHandlers.ofString());

            if (verifyResponse.statusCode() != 200) {
                response.sendRedirect("login.jsp?error=google");
                return;
            }

            JSONObject payload = new JSONObject(verifyResponse.body());

            // aud must match OUR client id, or anyone else's Google login could log into our app
            String audience = payload.optString("aud", "");
            if (!AppConfig.GOOGLE_CLIENT_ID.equals(audience)) {
                response.sendRedirect("login.jsp?error=google");
                return;
            }
            if (!"true".equals(payload.optString("email_verified"))) {
                response.sendRedirect("login.jsp?error=google");
                return;
            }

            String email = payload.optString("email");
            String name = payload.optString("name", email);
            String googleSub = payload.optString("sub");

            UserService.AuthUser user = UserService.findOrCreateByGoogle(email, name, googleSub);

            HttpSession session = request.getSession(true);
            session.setAttribute("userId", user.id);
            session.setAttribute("fullName", user.fullName);

            // First time this Google account has signed in: Google only gives us a
            // name and email, not age/goal/height/weight - so send them to fill that
            // in (same data a normal registration collects) before they hit the dashboard.
            if (user.isNewUser) {
                response.sendRedirect(request.getContextPath() + "/complete-profile.jsp");
            } else {
                response.sendRedirect(request.getContextPath() + "/dashboard");
            }

        } catch (SQLException | InterruptedException e) {
            Thread.currentThread().interrupt();
            request.setAttribute("error", "Google sign-in failed. Please try again.");
            request.getRequestDispatcher("login.jsp").forward(request, response);
        }
    }
}

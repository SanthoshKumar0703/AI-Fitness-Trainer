<%@ page contentType="text/html;charset=UTF-8" %>
<%
    if (session.getAttribute("userId") != null) {
        response.sendRedirect("dashboard");
    } else {
        response.sendRedirect("login.jsp");
    }
%>

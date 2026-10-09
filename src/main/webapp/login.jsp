<%@ page contentType="text/html;charset=UTF-8" %>
<!DOCTYPE html>
<html lang="en">
<head>
<meta charset="UTF-8">
<meta name="viewport" content="width=device-width, initial-scale=1.0">
<title>Log in — Training Log</title>
<link rel="preconnect" href="https://fonts.googleapis.com">
<link rel="stylesheet" href="https://fonts.googleapis.com/css2?family=Sora:wght@600;700;800&family=Inter:wght@400;500;600;700;800&family=JetBrains+Mono:wght@400;500;600&display=swap" media="print" onload="this.media='all'">
<link rel="stylesheet" href="assets/style.css?v=2">
</head>
<body>
<div class="auth-body">
  <div class="spine"></div>

  <div class="auth-visual">
    <div class="brand">
      <span class="mark">TL</span>
      <span class="name" style="color:var(--paper)">Training Log</span>
    </div>
    <div>
      <div class="kicker">No guessing. Just logging.</div>
      <div class="quote">Show up.<br>Log it.<br><span class="accent">Watch it compound.</span></div>
    </div>
    <div class="stat-line">
      <div><b>12,400+</b><span>Sessions logged</span></div>
      <div><b>4.8 / 5</b><span>Member rating</span></div>
      <div><b>AI</b><span>Coach included</span></div>
    </div>
  </div>

  <div class="auth-form-wrap">
    <form class="auth-form" method="post" action="login">
      <div class="eyebrow">Welcome back</div>
      <h1>Log in</h1>
      <p class="lede">Pick up your streak where you left off.</p>

      <% if ("1".equals(request.getParameter("registered"))) { %>
        <div class="tag" style="margin-bottom:16px;">Account created — log in below</div>
      <% } %>
      <% if ("1".equals(request.getParameter("reset"))) { %>
        <div class="success-text">Password updated — log in with your new password.</div>
      <% } %>
      <% if ("google".equals(request.getParameter("error"))) { %>
        <div class="error-text" style="margin-bottom:16px;">Google sign-in didn't go through — please try again.</div>
      <% } %>

      <div class="field">
        <label>Email address</label>
        <input type="email" name="email" placeholder="you@example.com" autocomplete="email" required>
      </div>
      <div class="field">
        <label>Password</label>
        <input type="password" name="password" placeholder="••••••••" autocomplete="current-password" required>
      </div>
      <div class="forgot-row"><a href="forgot-password.jsp">Forgot password?</a></div>
      <div class="error-text">${error}</div>

      <button type="submit" class="btn btn-dark btn-block">Log in</button>

      <div class="divider">or</div>

      <div class="google-btn-wrap">
        <div id="g_id_onload"
             data-client_id="293011053545-3nu9dptriqju9g80tnolf6lo0s8svoot.apps.googleusercontent.com"
             data-callback="handleGoogleCredential"
             data-auto_prompt="false">
        </div>
        <div class="g_id_signin"
             data-type="standard"
             data-size="large"
             data-theme="outline"
             data-text="signin_with"
             data-shape="rectangular"
             data-logo_alignment="left"
             data-width="330">
        </div>
      </div>

      <p class="foot-link">Don't have an account? <a href="register.jsp">Register</a></p>
    </form>
  </div>

</div>

<!-- Moved outside the main <form> - a <form> cannot legally contain another <form>;
     having one nested inside was breaking the whole page layout in every browser. -->
<form id="googleForm" method="post" action="google-login" style="display:none;">
  <input type="hidden" name="credential" id="googleCredentialInput">
</form>

<script>
function handleGoogleCredential(resp){
  document.getElementById("googleCredentialInput").value = resp.credential;
  document.getElementById("googleForm").submit();
}
// Load the Google Sign-In script AFTER this page has already finished loading,
// so this page's own "loading" state never has to wait on Google's servers -
// the button below just quietly becomes clickable a moment after the page appears.
window.addEventListener("load", function(){
  var s = document.createElement("script");
  s.src = "https://accounts.google.com/gsi/client";
  s.async = true;
  document.head.appendChild(s);
});
</script>
</body>
</html>

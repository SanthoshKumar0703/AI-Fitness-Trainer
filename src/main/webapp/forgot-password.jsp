<%@ page contentType="text/html;charset=UTF-8" %>
<!DOCTYPE html>
<html lang="en">
<head>
<meta charset="UTF-8">
<meta name="viewport" content="width=device-width, initial-scale=1.0">
<title>Reset password — Training Log</title>
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
      <div class="kicker">Happens to everyone</div>
      <div class="quote">Reset it<br>and get<br><span class="accent">back to it.</span></div>
    </div>
    <div class="stat-line">
      <div><b>30 sec</b><span>To reset</span></div>
      <div><b>Private</b><span>Only you see it</span></div>
      <div><b>AI</b><span>Coach included</span></div>
    </div>
  </div>

  <div class="auth-form-wrap">
    <form class="auth-form" method="post" action="forgot-password">
      <div class="eyebrow">Reset password</div>
      <h1>Forgot password</h1>
      <p class="lede">Enter your account email and a new password to reset it.</p>

      <div class="error-text">${error}</div>

      <div class="field">
        <label>Email address</label>
        <input type="email" name="email" placeholder="you@example.com" autocomplete="email" required>
      </div>
      <div class="field">
        <label>New password</label>
        <input type="password" name="password" placeholder="••••••••" autocomplete="new-password" minlength="6" required>
      </div>
      <div class="field">
        <label>Confirm new password</label>
        <input type="password" name="confirm" placeholder="••••••••" autocomplete="new-password" minlength="6" required>
      </div>

      <button type="submit" class="btn btn-dark btn-block">Reset password</button>

      <p class="foot-link">Remembered it after all? <a href="login.jsp">Log in</a></p>
    </form>
  </div>

</div>
</body>
</html>

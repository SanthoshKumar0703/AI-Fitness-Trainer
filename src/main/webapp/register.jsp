<%@ page contentType="text/html;charset=UTF-8" %>
<!DOCTYPE html>
<html lang="en">
<head>
<meta charset="UTF-8">
<meta name="viewport" content="width=device-width, initial-scale=1.0">
<title>Create account — Training Log</title>
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
      <div class="kicker">Free to start. No card.</div>
      <div class="quote">Your first set<br>starts the<br><span class="accent">streak.</span></div>
    </div>
    <div class="stat-line">
      <div><b>3 min</b><span>To set up</span></div>
      <div><b>Free</b><span>No card needed</span></div>
      <div><b>AI</b><span>Coach included</span></div>
    </div>
  </div>

  <div class="auth-form-wrap">
    <form class="auth-form" method="post" action="register">
      <div class="eyebrow">Get started</div>
      <h1>Create account</h1>
      <p class="lede">Set up your profile and get a plan built around your goal.</p>

      <div class="field-row-2">
        <div class="field"><label>Full name</label><input type="text" name="fullname" required></div>
        <div class="field"><label>Age</label><input type="number" name="age" min="10" max="100" required></div>
      </div>
      <div class="field"><label>Email address</label><input type="email" name="email" required></div>
      <div class="field"><label>Username</label><input type="text" name="username" required></div>
      <div class="field-row-2">
        <div class="field"><label>Password</label><input type="password" name="password" required></div>
        <div class="field"><label>Confirm password</label><input type="password" name="confirm" required></div>
      </div>
      <div class="field">
        <label>Fitness goal</label>
        <select name="goal" required>
          <option value="">Select a goal</option>
          <option>Weight Loss</option>
          <option>Muscle Gain</option>
          <option>Stay Fit</option>
        </select>
      </div>
      <div class="error-text">${error}</div>

      <button type="submit" class="btn btn-dark btn-block">Create account</button>

      <p class="foot-link">Already have an account? <a href="login.jsp">Log in</a></p>
    </form>
  </div>

</div>
</body>
</html>

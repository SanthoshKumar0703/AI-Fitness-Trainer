<%@ page contentType="text/html;charset=UTF-8" %>
<!DOCTYPE html>
<html lang="en">
<head>
<meta charset="UTF-8">
<meta name="viewport" content="width=device-width, initial-scale=1.0">
<title>Complete your profile — Training Log</title>
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
      <div class="kicker">One last step</div>
      <div class="quote">Tell us<br>a little<br><span class="accent">about you.</span></div>
    </div>
    <div class="stat-line">
      <div><b>30 sec</b><span>To finish up</span></div>
      <div><b>Private</b><span>Only you see it</span></div>
      <div><b>AI</b><span>Coach included</span></div>
    </div>
  </div>

  <div class="auth-form-wrap">
    <form class="auth-form" method="post" action="profile">
      <div class="eyebrow">Welcome, ${sessionScope.fullName}</div>
      <h1>Complete your profile</h1>
      <p class="lede">You signed up with Google, so we just need a few more details to build your plan.</p>

      <div class="field-row-2">
        <div class="field"><label>Age</label><input type="number" name="age" min="10" max="100" required></div>
        <div class="field">
          <label>Fitness goal</label>
          <select name="goal" required>
            <option value="">Select a goal</option>
            <option>Weight Loss</option>
            <option>Muscle Gain</option>
            <option>Stay Fit</option>
          </select>
        </div>
      </div>
      <div class="field-row-2">
        <div class="field"><label>Height (cm)</label><input type="number" step="0.1" name="height" min="50" max="260"></div>
        <div class="field"><label>Weight (kg)</label><input type="number" step="0.1" name="weight" min="20" max="400"></div>
      </div>

      <button type="submit" class="btn btn-dark btn-block">Save and continue</button>
    </form>
  </div>

</div>
</body>
</html>

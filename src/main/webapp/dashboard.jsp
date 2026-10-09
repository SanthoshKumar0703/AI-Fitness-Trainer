<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<!DOCTYPE html>
<html lang="en">
<head>
<meta charset="UTF-8">
<meta name="viewport" content="width=device-width, initial-scale=1.0">
<title>Training Log — Dashboard</title>
<link rel="preconnect" href="https://fonts.googleapis.com">
<link rel="stylesheet" href="https://fonts.googleapis.com/css2?family=Sora:wght@600;700;800&family=Inter:wght@400;500;600;700;800&family=JetBrains+Mono:wght@400;500;600&display=swap" media="print" onload="this.media='all'">
<script src="https://cdnjs.cloudflare.com/ajax/libs/Chart.js/4.4.0/chart.umd.min.js"></script>
<link rel="stylesheet" href="assets/style.css?v=2">
</head>
<body>

<div class="shell">
  <div class="spine"></div>

  <!-- SIDEBAR -->
  <aside class="sidebar">
    <div class="brand">
      <span class="mark">TL</span>
      <span class="name">Training Log</span>
    </div>

    <nav class="sidenav">
      <a href="#overview" data-section="overview"><span class="num">01</span>Overview</a>
      <a href="#profile" data-section="profile"><span class="num">02</span>Profile</a>
      <a href="#calculators" data-section="calculators"><span class="num">03</span>BMI &amp; Calories</a>
      <a href="#workout" data-section="workout"><span class="num">04</span>Workout Plan</a>
      <a href="#diet" data-section="diet"><span class="num">05</span>Diet Plan</a>
      <a href="#progress" data-section="progress"><span class="num">06</span>Progress</a>
      <a href="#hydration" data-section="hydration"><span class="num">07</span>Hydration</a>
      <a href="#coach" data-section="coach"><span class="num">08</span>AI Coach</a>
    </nav>

    <div class="side-foot">
      <button class="theme-toggle" id="themeToggle" onclick="toggleDarkMode()">🌙 Dark mode</button>
      <a class="logout-btn" style="text-align:center;" href="logout">Log out</a>
    </div>
  </aside>

  <!-- MAIN -->
  <main class="main">

    <div class="topbar">
      <div>
        <h1>Overview</h1>
        <p class="sub">Welcome back, ${sessionScope.fullName}</p>
      </div>
    </div>

    <!-- HERO -->
    <section class="hero" id="overview">
      <div class="eyebrow">Session log · Live from MySQL</div>
      <h2>Consistency is the only supplement that always works.</h2>
      <p>Track weight, calories, hydration and every set you put in — then let the AI coach turn it into a plan for tomorrow.</p>
      <div class="hero-actions">
        <button class="btn btn-signal" onclick="document.getElementById('workout').scrollIntoView()">Start today's session</button>
        <button class="btn btn-ghost" onclick="document.getElementById('coach').scrollIntoView()">Ask the AI coach</button>
      </div>
    </section>

    <!-- STAT GRID -->
    <section class="stat-grid">
      <div class="stat-tile">
        <div class="label"><span>Workouts</span><span>Today</span></div>
        <div class="value">${workoutCount}</div>
        <div class="bar"><span style="width:${workoutCount * 20 > 100 ? 100 : workoutCount * 20}%"></span></div>
      </div>
      <div class="stat-tile">
        <div class="label"><span>Calories</span><span>Burned</span></div>
        <div class="value">${caloriesToday}<span class="unit">kcal</span></div>
      </div>
      <div class="stat-tile">
        <div class="label"><span>Water</span><span>Glasses</span></div>
        <div class="value">${waterToday}<span class="unit">/8</span></div>
        <div class="bar"><span style="width:${waterToday * 12.5}%"></span></div>
      </div>
      <div class="stat-tile">
        <div class="label"><span>Streak</span><span>Active</span></div>
        <div class="value">${streak}<span class="unit">day<c:if test="${streak != 1}">s</c:if></span></div>
      </div>
    </section>

    <!-- PROFILE -->
    <section class="section" id="profile">
      <div class="section-head"><h2>User Profile</h2><span class="count">02</span></div>
      <div class="card">
        <form method="post" action="profile">
          <div class="two-col">
            <div class="field"><label>Full name</label><input type="text" value="${profile.fullName}" disabled></div>
            <div class="field"><label>Age</label><input type="number" name="age" value="${profile.age}"></div>
            <div class="field"><label>Height (cm)</label><input type="number" step="0.1" name="height" value="${profile.height}"></div>
            <div class="field"><label>Weight (kg)</label><input type="number" step="0.1" name="weight" value="${profile.weight}"></div>
          </div>
          <div class="field">
            <label>Fitness goal</label>
            <select name="goal">
              <option ${profile.goal == 'Weight Loss' ? 'selected' : ''}>Weight Loss</option>
              <option ${profile.goal == 'Muscle Gain' ? 'selected' : ''}>Muscle Gain</option>
              <option ${profile.goal == 'Stay Fit' ? 'selected' : ''}>Stay Fit</option>
            </select>
          </div>
          <button type="submit" class="btn btn-dark">Save profile</button>
          <c:if test="${not empty bmi}">
            <span class="tag" style="margin-left:12px;">Current BMI ${bmi}</span>
          </c:if>
        </form>
      </div>
    </section>

    <!-- CALCULATORS -->
    <section class="section" id="calculators">
      <div class="section-head"><h2>BMI &amp; Calorie Calculators</h2><span class="count">03</span></div>
      <div class="two-col">
        <div class="card">
          <div class="eyebrow">Body mass index</div>
          <div class="field"><label>Height (cm)</label><input type="number" id="bmiHeight" value="${profile.height}" placeholder="e.g. 175"></div>
          <div class="field"><label>Weight (kg)</label><input type="number" id="bmiWeight" value="${profile.weight}" placeholder="e.g. 68"></div>
          <button class="btn btn-dark btn-block" onclick="calculateBMI()">Calculate BMI</button>
          <div class="result-line" id="bmiResult">Enter height and weight to calculate.</div>
        </div>
        <div class="card">
          <div class="eyebrow">Daily maintenance calories</div>
          <div class="two-col">
            <div class="field"><label>Age</label><input type="number" id="calAge" value="${profile.age}"></div>
            <div class="field"><label>Gender</label>
              <select id="gender"><option>Male</option><option>Female</option></select>
            </div>
          </div>
          <div class="two-col">
            <div class="field"><label>Weight (kg)</label><input type="number" id="calWeight" value="${profile.weight}"></div>
            <div class="field"><label>Height (cm)</label><input type="number" id="calHeight" value="${profile.height}"></div>
          </div>
          <div class="field">
            <label>Activity level</label>
            <select id="calActivity">
              <option value="1.2">Sedentary — desk job</option>
              <option value="1.375">Light — 1-3 workouts/wk</option>
              <option value="1.55" selected>Moderate — 3-5 workouts/wk</option>
              <option value="1.725">Active — 6-7 workouts/wk</option>
            </select>
          </div>
          <button class="btn btn-dark btn-block" onclick="calculateCalories()">Calculate calories</button>
          <div class="result-line" id="calorieResult">Fill in your details to calculate.</div>
        </div>
      </div>
    </section>

    <!-- WORKOUT -->
    <section class="section" id="workout">
      <div class="section-head"><h2>Workout Planner</h2><span class="count">04</span></div>
      <div class="card">
        <form method="post" action="workout" class="set-toggle">
          <button type="submit" name="level" value="beginner">Beginner</button>
          <button type="submit" name="level" value="intermediate">Intermediate</button>
          <button type="submit" name="level" value="advanced">Advanced</button>
        </form>
        <p class="sub" style="margin:10px 0 18px;">Tap a level to log today's session — it's saved straight to your account.</p>

        <div class="section-head" style="border:none; padding:0; margin-bottom:10px;">
          <h2 style="font-size:16px;">Recent sessions</h2>
        </div>
        <c:choose>
          <c:when test="${empty recentWorkouts}">
            <p class="sub">No sessions logged yet — pick a level above to get started.</p>
          </c:when>
          <c:otherwise>
            <c:forEach var="w" items="${recentWorkouts}" varStatus="loop">
              <div class="log-row">
                <div class="idx"><c:if test="${loop.index + 1 < 10}">0</c:if>${loop.index + 1}</div>
                <div><div class="ex-name" style="text-transform:capitalize;">${w.level}</div><div class="ex-meta">${w.logDate}</div></div>
                <div class="num">${w.calories} kcal</div>
                <div class="num"></div>
                <div class="diff"></div>
              </div>
            </c:forEach>
          </c:otherwise>
        </c:choose>
      </div>
    </section>

    <!-- DIET -->
    <section class="section" id="diet">
      <div class="section-head"><h2>Diet Planner</h2><span class="count">05</span></div>
      <div class="card">
        <div class="field" style="max-width:280px">
          <label>Plan type</label>
          <select id="dietType" onchange="generateDiet()">
            <option value="weightloss">Weight Loss</option>
            <option value="muscle">Muscle Gain</option>
            <option value="vegan">Vegan</option>
            <option value="healthy" selected>Stay Fit</option>
          </select>
        </div>
        <div id="dietResult"></div>
      </div>
    </section>

    <!-- PROGRESS -->
    <section class="section" id="progress">
      <div class="section-head"><h2>Progress</h2><span class="count">06</span></div>
      <div class="two-col">
        <div class="card">
          <div class="eyebrow">Sessions — last 7 days</div>
          <canvas id="progressChart" height="180" style="margin-top:16px;"></canvas>
        </div>
        <div class="card">
          <div class="eyebrow">Achievements</div>
          <div class="badge-row" style="margin-top:16px;">
            <div class="badge ${badgeStreak3 ? 'earned' : ''}"><div class="ico">🔥</div><div class="name">3-Day Streak</div></div>
            <div class="badge ${badgeStreak7 ? 'earned' : ''}"><div class="ico">🏆</div><div class="name">7-Day Streak</div></div>
            <div class="badge ${badgeFirstWorkout ? 'earned' : ''}"><div class="ico">💪</div><div class="name">First Workout</div></div>
          </div>
        </div>
      </div>
    </section>

    <!-- HYDRATION -->
    <section class="section" id="hydration">
      <div class="section-head"><h2>Hydration</h2><span class="count">07</span></div>
      <div class="card">
        <div class="water-row">
          <div class="glasses">
            <c:forEach begin="1" end="8" varStatus="g">
              <div class="glass ${g.count <= waterToday ? 'filled' : ''}"></div>
            </c:forEach>
          </div>
          <div class="eyebrow">${waterToday} / 8</div>
          <form method="post" action="water" style="display:flex; gap:8px;">
            <button type="submit" name="action" value="add" class="btn btn-dark">+ Log a glass</button>
            <button type="submit" name="action" value="reset" class="btn btn-ghost" style="border-color:var(--line); color:var(--ink);">Reset</button>
          </form>
        </div>
      </div>
    </section>

    <!-- AI COACH -->
    <section class="section" id="coach">
      <div class="section-head"><h2>AI Coach</h2><span class="count">08</span></div>
      <div class="card coach-card">
        <div class="coach-head">
          <div class="coach-head-left">
            <div class="coach-orb">✦</div>
            <div>
              <div class="coach-title">Coach Nova</div>
              <div class="coach-status"><span class="dot"></span>Online · instant answers</div>
            </div>
          </div>
          <div class="coach-chip">No sign-ups · built in</div>
        </div>
        <div class="chat-window" id="chatWindow">
          <div class="msg-row">
            <div class="msg-avatar bot">✦</div>
            <div class="msg bot">
              <div class="msg-intro">Hey! I'm Coach Nova, your AI Coach. Ask me about any muscle group's workout, diet, hydration, injury recovery, or how to use this app.</div>
            </div>
          </div>
        </div>
        <div class="chat-suggestions" id="chatSuggestions">
          <button type="button" class="chip-btn" onclick="askSuggested('Chest workout for beginners')">💪 Chest workout</button>
          <button type="button" class="chip-btn" onclick="askSuggested('What should I eat to lose weight?')">🥗 Weight-loss diet</button>
          <button type="button" class="chip-btn" onclick="askSuggested('How much water should I drink daily?')">💧 Hydration tips</button>
          <button type="button" class="chip-btn" onclick="askSuggested('My knee hurts after squats')">🩹 Injury help</button>
        </div>
        <div class="chat-input-row">
          <input type="text" id="userQuestion" placeholder="Ask Coach Nova anything…" onkeydown="handleChatKey(event)">
          <button class="btn btn-signal" id="chatSendBtn" onclick="askAI()">Send</button>
        </div>
      </div>
    </section>

    <footer class="site-foot">
      <span>© 2026 Training Log</span>
      <span>Signed in as ${sessionScope.fullName}</span>
    </footer>

  </main>
</div>

<script src="assets/script.js?v=2"></script>
</body>
</html>

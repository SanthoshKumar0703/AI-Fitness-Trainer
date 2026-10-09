/* =========================================================
   TRAINING LOG — client-side logic
   (Profile / Workout / Water are handled server-side via
   plain form POSTs to the Servlets — no JS needed for those.)
   ========================================================= */

/* ---------- Theme (client-only preference, no DB needed) ---------- */
function applyStoredTheme(){
  const theme = localStorage.getItem("theme");
  if(theme === "dark") document.body.classList.add("dark");
  const btn = document.getElementById("themeToggle");
  if(btn) btn.textContent = theme === "dark" ? "☀ Light mode" : "🌙 Dark mode";
}
function toggleDarkMode(){
  document.body.classList.toggle("dark");
  const isDark = document.body.classList.contains("dark");
  localStorage.setItem("theme", isDark ? "dark" : "light");
  const btn = document.getElementById("themeToggle");
  if(btn) btn.textContent = isDark ? "☀ Light mode" : "🌙 Dark mode";
}
applyStoredTheme();

/* ---------- Active nav highlight ---------- */
function initSideNav(){
  const links = document.querySelectorAll(".sidenav a[data-section]");
  if(!links.length) return;
  const sections = [...links].map(l => document.getElementById(l.dataset.section)).filter(Boolean);
  const io = new IntersectionObserver((entries)=>{
    entries.forEach(e=>{
      if(e.isIntersecting){
        links.forEach(l=>l.classList.remove("active"));
        const active = document.querySelector(`.sidenav a[data-section="${e.target.id}"]`);
        if(active) active.classList.add("active");
      }
    });
  }, { rootMargin: "-30% 0px -60% 0px" });
  sections.forEach(s=>io.observe(s));
}

/* ---------- BMI (client-side, instant preview tool) ---------- */
function calculateBMI(){
  const h = parseFloat(document.getElementById("bmiHeight").value);
  const w = parseFloat(document.getElementById("bmiWeight").value);
  const out = document.getElementById("bmiResult");
  if(!h || !w){ out.innerHTML = "Enter height and weight to calculate."; return; }
  const meters = h/100;
  const bmi = (w/(meters*meters)).toFixed(1);
  let cat = bmi < 18.5 ? "Underweight" : bmi < 25 ? "Normal range" : bmi < 30 ? "Overweight" : "Obese";
  out.innerHTML = `<span class="big">${bmi}</span><span class="tag">${cat}</span>`;
}

/* ---------- Calories (Mifflin-St Jeor) ---------- */
function calculateCalories(){
  const age = parseInt(document.getElementById("calAge").value);
  const w = parseFloat(document.getElementById("calWeight").value);
  const h = parseFloat(document.getElementById("calHeight").value);
  const gender = document.getElementById("gender").value;
  const activity = document.getElementById("calActivity").value || "1.2";
  const out = document.getElementById("calorieResult");
  if(!age || !w || !h){ out.innerHTML = "Enter age, weight and height."; return; }
  let bmr = gender === "Male" ? 10*w + 6.25*h - 5*age + 5 : 10*w + 6.25*h - 5*age - 161;
  const total = Math.round(bmr * parseFloat(activity));
  out.innerHTML = `<span class="big">${total} kcal</span>Estimated daily maintenance calories.`;
}

/* ---------- Diet planner (client-side, no persistence needed) ---------- */
const DIETS = {
  weightloss: { breakfast:["Oats with berries","Black coffee / green tea"], lunch:["Grilled chicken or tofu","Brown rice, steamed greens"], dinner:["Grilled fish or paneer","Large mixed salad"], snacks:["Handful of almonds","Green tea"], calories:"1,600 – 1,800 kcal", protein:"110g", water:"3.0 L" },
  muscle: { breakfast:["4 eggs + 2 toast","Peanut butter, banana"], lunch:["Chicken breast, rice","Mixed vegetables"], dinner:["Salmon or lean beef","Sweet potato"], snacks:["Protein shake","Greek yogurt"], calories:"2,800 – 3,200 kcal", protein:"170g", water:"3.5 L" },
  vegan: { breakfast:["Tofu scramble","Fruit bowl"], lunch:["Chickpea & quinoa bowl","Steamed broccoli"], dinner:["Lentil curry, brown rice","Sautéed spinach"], snacks:["Mixed nuts","Soy milk smoothie"], calories:"2,000 – 2,200 kcal", protein:"90g", water:"3.0 L" },
  healthy: { breakfast:["Greek yogurt, granola","Fresh fruit"], lunch:["Grilled fish, quinoa","Roasted vegetables"], dinner:["Paneer or chicken","Vegetable soup"], snacks:["Nuts and seeds","Herbal tea"], calories:"2,000 – 2,200 kcal", protein:"100g", water:"3.0 L" }
};
function generateDiet(){
  const type = document.getElementById("dietType")?.value || "healthy";
  const d = DIETS[type];
  document.getElementById("dietResult").innerHTML = `
    <div class="meal-grid">
      <div class="meal-card"><div class="eyebrow">Breakfast</div><ul>${d.breakfast.map(i=>`<li>${i}</li>`).join("")}</ul></div>
      <div class="meal-card"><div class="eyebrow">Lunch</div><ul>${d.lunch.map(i=>`<li>${i}</li>`).join("")}</ul></div>
      <div class="meal-card"><div class="eyebrow">Dinner</div><ul>${d.dinner.map(i=>`<li>${i}</li>`).join("")}</ul></div>
      <div class="meal-card"><div class="eyebrow">Snacks</div><ul>${d.snacks.map(i=>`<li>${i}</li>`).join("")}</ul></div>
    </div>
    <div class="result-line"><span class="big">${d.calories}</span>Protein target ${d.protein} · Water ${d.water}</div>`;
}

/* ---------- AI Coach — built-in rule-based assistant ---------- */
async function askAI(){
  const input = document.getElementById("userQuestion");
  const q = input.value.trim();
  if(!q) return;
  appendUserMsg(q);
  input.value = "";

  const btn = document.getElementById("chatSendBtn");
  if(btn){ btn.disabled = true; btn.textContent = "…"; }
  const typingRow = appendTyping();

  try{
    const res = await fetch("chat", {
      method: "POST",
      headers: { "Content-Type": "application/x-www-form-urlencoded" },
      body: "message=" + encodeURIComponent(q)
    });
    const data = await res.json();
    typingRow.remove();
    appendBotMsg(data.reply, data.points);
  }catch(e){
    typingRow.remove();
    appendBotMsg("Sorry, I'm having trouble connecting right now.", null);
  }finally{
    if(btn){ btn.disabled = false; btn.textContent = "Send"; }
  }
}
function appendUserMsg(text){
  const win = document.getElementById("chatWindow");
  if(!win) return;
  const row = document.createElement("div");
  row.className = "msg-row user-row";
  const avatar = document.createElement("div");
  avatar.className = "msg-avatar user";
  avatar.textContent = "YOU";
  const bubble = document.createElement("div");
  bubble.className = "msg user";
  bubble.textContent = text;
  row.appendChild(avatar);
  row.appendChild(bubble);
  win.appendChild(row);
  win.scrollTop = win.scrollHeight;
}
function appendBotMsg(intro, points){
  const win = document.getElementById("chatWindow");
  if(!win) return;
  const row = document.createElement("div");
  row.className = "msg-row";
  const avatar = document.createElement("div");
  avatar.className = "msg-avatar bot";
  avatar.textContent = "✦";
  const bubble = document.createElement("div");
  bubble.className = "msg bot";

  const introEl = document.createElement("div");
  introEl.className = "msg-intro";
  introEl.textContent = intro || "Here's what I've got:";
  bubble.appendChild(introEl);

  if(points && points.length){
    const ul = document.createElement("ul");
    ul.className = "msg-points";
    points.forEach(function(p){
      const li = document.createElement("li");
      li.textContent = p;
      ul.appendChild(li);
    });
    bubble.appendChild(ul);
  }

  row.appendChild(avatar);
  row.appendChild(bubble);
  win.appendChild(row);
  win.scrollTop = win.scrollHeight;
}
function appendTyping(){
  const win = document.getElementById("chatWindow");
  if(!win) return null;
  const row = document.createElement("div");
  row.className = "msg-row";
  const avatar = document.createElement("div");
  avatar.className = "msg-avatar bot";
  avatar.textContent = "✦";
  const bubble = document.createElement("div");
  bubble.className = "msg bot typing";
  bubble.innerHTML = "<span></span><span></span><span></span>";
  row.appendChild(avatar);
  row.appendChild(bubble);
  win.appendChild(row);
  win.scrollTop = win.scrollHeight;
  return row;
}
function handleChatKey(e){ if(e.key === "Enter") askAI(); }
function askSuggested(text){
  const input = document.getElementById("userQuestion");
  if(!input) return;
  input.value = text;
  askAI();
}

/* ---------- Progress chart — pulls real data from /progress (MySQL) ---------- */
let progressChart;
async function renderChart(){
  const canvas = document.getElementById("progressChart");
  if(!canvas || typeof Chart === "undefined") return;
  let data;
  try{
    const res = await fetch("progress");
    data = await res.json();
  }catch(e){
    console.error("Could not load progress data", e);
    return;
  }
  const styles = getComputedStyle(document.body);
  if(progressChart) progressChart.destroy();
  progressChart = new Chart(canvas, {
    type:"line",
    data:{
      labels: data.labels,
      datasets:[{
        label:"Sessions completed",
        data: data.values,
        borderColor: styles.getPropertyValue("--accent").trim() || "#FF5A36",
        backgroundColor: "rgba(255,90,54,.15)",
        borderWidth:2,
        tension:.35,
        fill:true,
        pointRadius:3,
      }]
    },
    options:{
      responsive:true,
      plugins:{ legend:{ display:false } },
      scales:{
        x:{ grid:{ display:false }, ticks:{ color: styles.getPropertyValue("--ink-soft").trim() } },
        y:{ grid:{ color: styles.getPropertyValue("--line").trim() }, ticks:{ color: styles.getPropertyValue("--ink-soft").trim(), precision:0 } }
      }
    }
  });
}

/* ---------- Boot ---------- */
document.addEventListener("DOMContentLoaded", function(){
  initSideNav();
  generateDiet();
  renderChart();

  // Automate: if we already have the user's saved height/weight/age from
  // registration/profile, calculate BMI and calories immediately instead of
  // making them re-type it here.
  if(document.getElementById("bmiHeight")?.value && document.getElementById("bmiWeight")?.value){
    calculateBMI();
  }
  if(document.getElementById("calAge")?.value && document.getElementById("calWeight")?.value && document.getElementById("calHeight")?.value){
    calculateCalories();
  }
});

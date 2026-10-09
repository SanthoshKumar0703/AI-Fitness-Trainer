package com.traininglog.servlet;

import com.traininglog.util.AppConfig;
import org.json.JSONArray;
import org.json.JSONObject;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Locale;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * AI Coach backend - hybrid design.
 *
 * Fast path: a self-contained keyword/intent matcher (no network call,
 * instant, free). Covers the common cases - specific body parts, diet,
 * hydration, injuries, app how-tos.
 *
 * Fallback: anything the keyword matcher doesn't recognize is sent to the
 * Claude API (Anthropic) so free-form questions still get a real, relevant
 * answer instead of "I don't have an answer for that one." If the API call
 * fails for any reason (no key configured, network issue, bad response),
 * we NEVER show a raw error - we fall back to a graceful generic reply.
 *
 * Order matters in the keyword section: more specific checks (e.g.
 * "knee pain") run before broader ones (e.g. "legs") so a specific
 * question doesn't get swallowed by a generic bucket.
 */
@WebServlet("/chat")
public class ChatServlet extends HttpServlet {

    private static final Random RNG = new Random();
    private static final Logger LOG = Logger.getLogger(ChatServlet.class.getName());

    private static final HttpClient HTTP = HttpClient.newBuilder()
        .connectTimeout(Duration.ofSeconds(8))
        .build();

    private static final String CLAUDE_MODEL = "claude-sonnet-5";

    private static final String SYSTEM_PROMPT =
        "You are Coach Nova, the built-in AI fitness coach inside the Training Log app. " +
        "Answer the user's fitness, nutrition, hydration, injury-recovery, or app-usage question " +
        "directly and concretely, the way an experienced personal trainer would. " +
        "Keep it practical and specific, never vague. " +
        "For anything resembling a medical concern (real injuries, persistent pain, illness), " +
        "give sensible first-step guidance but also recommend seeing a doctor - don't diagnose. " +
        "If the question has nothing to do with fitness, nutrition, or this app, politely say so " +
        "and steer back to what you can help with. " +
        "Respond with ONLY valid JSON, no markdown, no code fences, no text outside the JSON, " +
        "in exactly this shape: " +
        "{\"reply\": \"<one short intro sentence>\", \"points\": [\"<point 1>\", \"<point 2>\", \"...\"]} " +
        "Use 3-6 points. Keep each point one concise line.";

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        request.setCharacterEncoding("UTF-8");
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        String message = request.getParameter("message");
        if (message == null || message.trim().isEmpty()) {
            write(response, "Ask me anything about your training, diet, hydration, or this app.");
            return;
        }

        String msg = " " + message.toLowerCase(Locale.ROOT).trim() + " ";

        // ================= SMALL TALK =================
        if (matchesAny(msg, "hi", "hello", "hey", "yo", "sup", "hii", "helo")) {
            write(response, "Hey! I'm your AI Coach.",
                "Ask about any muscle group's workout",
                "Diet, hydration, or supplements",
                "Injury recovery advice",
                "How to use any part of this app");
            return;
        }
        if (containsAny(msg, "who are you", "what are you", "what is your name")) {
            write(response, "I'm the built-in AI Coach for Training Log.",
                "Runs entirely inside the app - no external AI service",
                "Focused on training, nutrition, and injury recovery",
                "Gives the same instant answer every time you ask");
            return;
        }
        if (containsAny(msg, "what can you do", "help me", "what do you know", "capabilities")) {
            write(response, "Here's what I can help with:",
                "Workouts for abs, arms, chest, back, legs, shoulders, glutes, or full body",
                "Diet, macros, and supplement guidance",
                "Hydration and calorie targets",
                "Injury recovery (knee, back, shoulder, sprains, and more)",
                "Motivation, streaks, and using this app");
            return;
        }
        if (containsAny(msg, "thank", "thanks", "thnx", "thx")) {
            write(response, "Anytime! Keep logging your sessions so I can watch your streak grow.");
            return;
        }
        if (matchesAny(msg, "bye", "goodbye", "see you", "cya")) {
            write(response, "See you at your next session! Don't forget to log it and grab some water.");
            return;
        }

        // ================= INJURY / PAIN — specific body parts first =================
        if (containsAny(msg, "knee pain", "knee injury", "hurt my knee", "knee hurts", "knee ache")) {
            write(response, "For knee pain:",
                "Rest from high-impact or loaded leg work",
                "Ice 15-20 minutes a few times a day for the first 48 hours",
                "Gentle range-of-motion work - heel slides, quad sets",
                "Skip squats, lunges, and running until pain-free",
                "See a doctor if there's swelling, instability, or pain lasting more than a few days");
            return;
        }
        if (containsAny(msg, "back pain", "hurt my back", "back injury", "lower back pain", "slipped disc")) {
            write(response, "For back pain:",
                "Avoid heavy lifting or deadlifts until it settles",
                "Keep moving gently - short walks, cat-cow stretches",
                "Ice for the first 48 hours, then switch to heat",
                "See a doctor right away for sharp pain, numbness, or pain radiating down a leg");
            return;
        }
        if (containsAny(msg, "shoulder pain", "hurt my shoulder", "shoulder injury", "rotator cuff")) {
            write(response, "For shoulder pain:",
                "Rest overhead pressing and heavy pushing movements",
                "Ice 15-20 minutes a few times a day",
                "Once acute pain eases: gentle pendulum swings, light band rotator-cuff work",
                "See a doctor for persistent pain, weakness, or reduced range of motion");
            return;
        }
        if (containsAny(msg, "sprain", "twisted ankle", "ankle pain", "wrist pain", "rolled my ankle", "twisted my")) {
            write(response, "For a sprain, remember RICE:",
                "Rest the joint completely",
                "Ice - 15-20 min every 2-3 hours for the first 48h",
                "Compression - a snug wrap, not tight",
                "Elevation above heart level",
                "See a doctor if there's major swelling, you can't bear weight, or it isn't improving in a few days");
            return;
        }
        if (containsAny(msg, "neck pain", "stiff neck", "hurt my neck")) {
            write(response, "For neck pain:",
                "Keep it gently moving with light range-of-motion stretches",
                "Try a warm compress",
                "Check your desk and pillow posture",
                "Avoid heavy overhead or shrug work until it eases",
                "Get medical attention right away for numbness/tingling down an arm or pain after an impact");
            return;
        }
        if (containsAny(msg, "injur", "hurt myself", "cramp", "muscle strain", "pulled a muscle")) {
            write(response, "For a fresh strain or minor injury:",
                "Rest the area completely",
                "Ice 15-20 minutes a few times a day for the first 48 hours",
                "Avoid loading it until the pain is gone",
                "See a doctor if pain is severe or isn't improving after a few days");
            return;
        }

        // ================= BODY-PART WORKOUTS =================
        if (containsAny(msg, "abs", "core workout", "six pack", "stomach fat", "obliques")) {
            write(response, "Abs/core routine (3-4 sets of 12-20 reps, 3x a week):",
                "Hanging leg raises",
                "Cable crunches",
                "Planks - 3x45-60 seconds",
                "Bicycle crunches",
                "Russian twists",
                "Visible abs mostly come from lower body fat, so pair this with your diet plan");
            return;
        }
        if (containsAny(msg, "arms", "biceps", "triceps", "forearm")) {
            write(response, "Arms routine (3-4 sets of 8-12 reps, 2x a week):",
                "Biceps - barbell curls, hammer curls, incline dumbbell curls",
                "Triceps - close-grip bench press, tricep pushdowns, overhead extensions, dips",
                "Arms also get worked during chest/back day, so 2x a week direct is plenty");
            return;
        }
        if (containsAny(msg, "chest", "pecs", "pec workout")) {
            write(response, "Chest routine (3-4 sets of 8-12 reps, 1-2x a week):",
                "Flat or incline bench press",
                "Push-ups",
                "Dumbbell flyes",
                "Cable crossovers",
                "Dips",
                "Give it at least 48h recovery before training chest again");
            return;
        }
        if (containsAny(msg, "back workout", "lats", "upper back", "back muscles", "lat pulldown", "build my back")) {
            write(response, "Back routine (3-4 sets of 6-12 reps):",
                "Pull-ups or lat pulldowns",
                "Bent-over rows",
                "Seated cable rows",
                "Deadlifts",
                "Face pulls for rear delts and posture");
            return;
        }
        if (containsAny(msg, "legs", "leg day", "quads", "hamstring", "calves", "thigh")) {
            write(response, "Legs routine (3-4 sets of 8-15 reps):",
                "Squats",
                "Lunges",
                "Leg press",
                "Romanian deadlifts for hamstrings",
                "Calf raises",
                "Leg curls/extensions",
                "Legs are your biggest muscle group - give them 48-72h to recover");
            return;
        }
        if (containsAny(msg, "shoulder workout", "delts", "shoulder exercise", "build my shoulders")) {
            write(response, "Shoulders routine (3-4 sets of 10-15 reps):",
                "Overhead press",
                "Lateral raises",
                "Front raises",
                "Rear-delt flyes",
                "Arnold press",
                "Go lighter and stricter here - shoulders are prone to overuse injury");
            return;
        }
        if (containsAny(msg, "glutes", "butt workout", "hips workout", "booty")) {
            write(response, "Glutes routine (3-4 sets of 10-15 reps, 2x a week):",
                "Hip thrusts",
                "Bulgarian split squats",
                "Glute bridges",
                "Cable kickbacks",
                "Sumo deadlifts");
            return;
        }
        if (containsAny(msg, "full body workout", "total body", "full body routine")) {
            write(response, "Full-body session (3 sets of 8-12 reps, one move per group):",
                "Squats - legs",
                "Push-ups or bench press - chest",
                "Bent-over rows - back",
                "Overhead press - shoulders",
                "Planks - core",
                "Great as a 3x/week beginner-to-intermediate routine");
            return;
        }
        if (containsAny(msg, "warm up", "warmup", "before workout")) {
            write(response, "A good warm-up (5-10 minutes):",
                "Light cardio first - jog or jumping jacks - to raise your heart rate",
                "Dynamic stretches for the muscles you're about to train - leg swings, arm circles, bodyweight squats",
                "Save static stretching for after your workout, not before");
            return;
        }

        // ================= WEIGHT GOALS =================
        if (containsAny(msg, "gain weight", "bulk", "bulking", "put on weight", "gain mass", "skinny", "underweight", "too thin")) {
            write(response, "To gain weight:",
                "Eat in a calorie surplus - roughly 300-500 kcal above maintenance",
                "About 1.6-2.2g protein per kg bodyweight daily",
                "Nutrient-dense foods - rice, oats, eggs, dairy, nuts, lean meat or legumes",
                "Pair it with progressive resistance training so the extra calories build muscle",
                "Check the Calorie Calculator on your dashboard for your exact target");
            return;
        }
        if (containsAny(msg, "lose weight", "weight loss", "cut fat", "cutting", "burn fat", "lose fat", "slim down", "obese", "overweight")) {
            write(response, "For weight loss:",
                "A moderate calorie deficit - about 300-500 kcal below maintenance",
                "Keep protein high to preserve muscle",
                "Fill up on vegetables and fiber",
                "Stay consistent with your workouts",
                "Use the BMI & Calorie Calculators on your dashboard for a realistic target");
            return;
        }
        if (containsAny(msg, "muscle gain", "muscle mass", "bodybuild", "hypertrophy", "build muscle")) {
            write(response, "For muscle gain:",
                "Protein - about 1.6-2.2g per kg bodyweight/day, spread across meals",
                "Progressive overload in your lifts",
                "Enough sleep to actually recover",
                "Ask me about a specific body part and I'll give you an exact routine");
            return;
        }
        if (containsAny(msg, "plateau", "not seeing results", "stuck", "no progress", "not losing", "not gaining")) {
            write(response, "Breaking a plateau:",
                "Check your Progress chart for the last 7 days",
                "If it's been flat for 2-3 weeks, your body has adapted",
                "Adjust calories OR increase training stimulus - not both at once",
                "Change one variable at a time and reassess after a week or two");
            return;
        }

        // ================= DIET / NUTRITION =================
        if (containsAny(msg, "diet", "nutrition", "food", " eat ", "meal", "calorie", "macro", "carb", "fat intake")) {
            write(response, "A solid everyday diet:",
                "Lean protein at every meal",
                "Vegetables and fiber",
                "Whole grains over refined carbs",
                "Healthy fats - nuts, olive oil, fish",
                "Enough water",
                "Tell me your goal - gain, lose, or maintain - for more specific advice");
            return;
        }
        if (containsAny(msg, "supplement", "creatine", "whey", "vitamin", "pre workout", "bcaa")) {
            write(response, "Supplements are optional extras, not requirements:",
                "Creatine monohydrate - 3-5g/day, most evidence-backed for performance",
                "Whey protein - useful for hitting protein targets",
                "Real food should always come first",
                "Check with a doctor before adding anything if you have a health condition");
            return;
        }
        if (containsAny(msg, "water", "hydrat", "drink", "thirsty")) {
            write(response, "Hydration target:",
                "About 8 glasses (roughly 2-2.5 liters) of water a day",
                "More if you're training hard or it's hot",
                "Use the Hydration tracker on your dashboard - it resets daily");
            return;
        }

        // ================= CARDIO / GENERAL TRAINING =================
        if (containsAny(msg, "cardio", "running", "run ", "jogging", "treadmill", "cycling", "stamina", "endurance")) {
            write(response, "For general fitness cardio:",
                "2-3 sessions a week is plenty",
                "20-40 minutes of running, cycling, or brisk walking",
                "For fat loss, diet still does most of the work - cardio just helps the deficit",
                "Mix steady-state with occasional intervals so it doesn't get stale");
            return;
        }
        if (containsAny(msg, "strength", "lifting", "form", "squat", "deadlift", "bench press", "reps", "sets")) {
            write(response, "Strength training basics:",
                "Prioritize form over weight, especially early on",
                "Compound lifts give the most return - squats, deadlifts, bench press",
                "Common beginner range: 3-4 sets of 6-12 reps, 3-4 sessions a week",
                "Log your sessions under a Workout level to track consistency");
            return;
        }
        if (containsAny(msg, "yoga", "stretch", "flexib", "mobility")) {
            write(response, "Stretching & mobility:",
                "10-15 minutes after a workout, when muscles are warm",
                "Static stretches held 20-30 seconds work well post-workout",
                "Save dynamic stretches for your warm-up instead");
            return;
        }

        // ================= RECOVERY / MENTAL =================
        if (containsAny(msg, "sleep", "recovery", "rest day", "rest ", "tired", "fatigue", "burnout")) {
            write(response, "Recovery is where muscle-building actually happens:",
                "Aim for 7-9 hours of sleep",
                "At least one full rest day between hard sessions for the same muscle group",
                "Keep protein and water intake up",
                "See a doctor if fatigue isn't easing after a few days of rest");
            return;
        }
        if (containsAny(msg, "sore", "soreness", "doms")) {
            write(response, "Muscle soreness (DOMS):",
                "Normal 1-2 days after training",
                "Light movement, water, and protein usually help it pass",
                "Should ease within 2-3 days",
                "Sharp or worsening pain (not dull soreness) means back off and consider seeing a doctor");
            return;
        }
        if (containsAny(msg, "stress", "anxious", "anxiety", "mental health", "mood")) {
            write(response, "On stress and exercise:",
                "Exercise is a genuinely good stress reliever - even a short walk helps",
                "I'm a fitness assistant, not a substitute for a doctor or therapist",
                "If stress or anxiety is affecting you a lot, please talk to a professional or someone you trust");
            return;
        }

        // ================= BEGINNER / MOTIVATION / MISC =================
        if (containsAny(msg, "beginner", "just started", "new to gym", "first time", "start working out")) {
            write(response, "As a beginner:",
                "Focus on consistency over intensity",
                "3 full-body sessions a week",
                "Learn proper form before adding heavy weight",
                "Pick 'Beginner' in the Workout Planner to log sessions and build your streak");
            return;
        }
        if (containsAny(msg, "motivat", "streak", "consisten", "lazy", "give up", "cant continue", "no energy to workout")) {
            write(response, "On staying consistent:",
                "Even a short, easy session keeps your streak alive on a low-energy day",
                "Check your Streak badge on the dashboard",
                "Your weekly chart is a quick reminder of how far you've already come");
            return;
        }
        if (containsAny(msg, "bmi", "body mass index")) {
            write(response, "About your BMI:",
                "Calculated automatically from the height/weight in your Profile tab",
                "Shown right on your dashboard",
                "It's a rough screening number, not a full picture of health - body composition matters too");
            return;
        }
        if (containsAny(msg, "how to", "how do i", "profile", "account", "login", "log in", "password", "streak reset", "reset")) {
            write(response, "Quick app guide:",
                "Update height/weight/goal from the Profile tab",
                "Log workouts and water right from the dashboard",
                "View your last 7 days of progress in the chart",
                "Ask me something specific and I'll walk you through it");
            return;
        }

        // ================= AI FALLBACK =================
        // Nothing in the keyword rules matched - ask Claude instead of
        // showing "I don't have an answer for that one." If that fails for
        // any reason, drop through to the generic guidance below.
        if (!AppConfig.ANTHROPIC_API_KEY.isEmpty()) {
            String[] aiReply = askClaude(message);
            if (aiReply != null) {
                write(response, aiReply[0], java.util.Arrays.copyOfRange(aiReply, 1, aiReply.length));
                return;
            }
        }

        String[] fallbackIntros = {
            "I don't have a specific answer for that right now.",
            "I'm the Training Log AI Coach, so I stick to fitness/diet/app topics."
        };
        write(response, fallbackIntros[RNG.nextInt(fallbackIntros.length)],
            "Try naming a body part - abs, arms, chest, back, legs, shoulders, glutes",
            "Or a goal - gain weight, lose weight, build muscle",
            "Or an injury - knee, back, shoulder pain",
            "Or ask how to use a feature of this app");
    }

    /**
     * Calls the Claude API for a free-form question the keyword matcher
     * didn't recognize. Returns {intro, point1, point2, ...} on success,
     * or null on ANY failure (missing/invalid key, network error, timeout,
     * bad status code, unparseable response) - callers must treat null as
     * "silently fall back", never surface these failures to the user.
     */
    private String[] askClaude(String userMessage) {
        try {
            JSONObject body = new JSONObject();
            body.put("model", CLAUDE_MODEL);
            body.put("max_tokens", 600);
            body.put("system", SYSTEM_PROMPT);
            JSONArray messages = new JSONArray();
            JSONObject userMsg = new JSONObject();
            userMsg.put("role", "user");
            userMsg.put("content", userMessage);
            messages.put(userMsg);
            body.put("messages", messages);

            HttpRequest apiRequest = HttpRequest.newBuilder()
                .uri(URI.create("https://api.anthropic.com/v1/messages"))
                .timeout(Duration.ofSeconds(20))
                .header("Content-Type", "application/json")
                .header("x-api-key", AppConfig.ANTHROPIC_API_KEY)
                .header("anthropic-version", "2023-06-01")
                .POST(HttpRequest.BodyPublishers.ofString(body.toString()))
                .build();

            HttpResponse<String> apiResponse = HTTP.send(apiRequest, HttpResponse.BodyHandlers.ofString());

            if (apiResponse.statusCode() != 200) {
                LOG.log(Level.WARNING, "Claude API returned status " + apiResponse.statusCode());
                return null;
            }

            JSONObject responseJson = new JSONObject(apiResponse.body());
            JSONArray content = responseJson.optJSONArray("content");
            if (content == null || content.isEmpty()) return null;

            String rawText = content.getJSONObject(0).optString("text", "").trim();
            // Strip accidental code fences in case the model adds them anyway.
            if (rawText.startsWith("```")) {
                rawText = rawText.replaceAll("^```[a-zA-Z]*\\n?", "").replaceAll("```$", "").trim();
            }

            JSONObject parsed = new JSONObject(rawText);
            String intro = parsed.optString("reply", "").trim();
            JSONArray points = parsed.optJSONArray("points");
            if (intro.isEmpty()) return null;

            if (points == null || points.isEmpty()) {
                return new String[]{ intro };
            }
            String[] out = new String[points.length() + 1];
            out[0] = intro;
            for (int i = 0; i < points.length(); i++) {
                out[i + 1] = points.optString(i, "");
            }
            return out;

        } catch (Exception e) {
            // Covers: JSON parse failure, network timeout, interrupted call,
            // malformed API response - anything at all. Never let this
            // bubble up as a visible error to the user.
            LOG.log(Level.WARNING, "Claude fallback failed, using generic reply instead", e);
            return null;
        }
    }

    private boolean containsAny(String msg, String... keywords) {
        for (String k : keywords) {
            if (msg.contains(k)) return true;
        }
        return false;
    }

    private boolean matchesAny(String msg, String... exact) {
        String trimmed = msg.trim();
        for (String e : exact) {
            if (trimmed.equals(e) || trimmed.startsWith(e + " ") || trimmed.startsWith(e + ",") || trimmed.startsWith(e + "!")) {
                return true;
            }
        }
        return false;
    }

    /**
     * Writes {"reply": intro, "points": [...]} - the frontend renders intro
     * plus a bullet list. A topic emoji is auto-prefixed to the intro so
     * replies read as a real coach's message rather than a flat sentence.
     */
    private void write(HttpServletResponse response, String intro, String... points) throws IOException {
        JSONObject out = new JSONObject();
        out.put("reply", pickEmoji(intro) + " " + intro);
        JSONArray arr = new JSONArray();
        for (String p : points) arr.put(p);
        out.put("points", arr);
        try (PrintWriter writer = response.getWriter()) {
            writer.print(out.toString());
        }
    }

    /** Picks a topic-relevant emoji purely from the intro's own wording - no new content, just presentation. */
    private String pickEmoji(String intro) {
        String t = intro.toLowerCase(Locale.ROOT);
        if (t.contains("pain") || t.contains("sprain") || t.contains("injur") || t.contains("strain")) return "\uD83E\uDE79"; // 🩹
        if (t.contains("abs")) return "\uD83D\uDD25"; // 🔥
        if (t.contains("arms")) return "\uD83D\uDCAA"; // 💪
        if (t.contains("chest")) return "\uD83D\uDCAA";
        if (t.contains("back routine") || t.contains("shoulders") || t.contains("glutes") || t.contains("legs") || t.contains("full-body")) return "\uD83C\uDFCB"; // 🏋
        if (t.contains("warm-up")) return "\u26A1"; // ⚡
        if (t.contains("gain weight")) return "\uD83D\uDCC8"; // 📈
        if (t.contains("weight loss")) return "\uD83D\uDCC9"; // 📉
        if (t.contains("muscle gain")) return "\uD83D\uDCAA";
        if (t.contains("plateau")) return "\uD83D\uDEA7"; // 🚧
        if (t.contains("diet")) return "\uD83E\uDD57"; // 🥗
        if (t.contains("supplement")) return "\uD83D\uDC8A"; // 💊
        if (t.contains("hydrat")) return "\uD83D\uDCA7"; // 💧
        if (t.contains("cardio")) return "\uD83C\uDFC3"; // 🏃
        if (t.contains("strength")) return "\uD83C\uDFCB";
        if (t.contains("stretching") || t.contains("mobility")) return "\uD83E\uDD38"; // 🤸
        if (t.contains("recovery")) return "\uD83D\uDE34"; // 😴
        if (t.contains("soreness")) return "\uD83E\uDD75"; // 🥵
        if (t.contains("stress")) return "\uD83E\uDDD8"; // 🧘
        if (t.contains("beginner")) return "\uD83C\uDF31"; // 🌱
        if (t.contains("consisten")) return "\uD83D\uDD25";
        if (t.contains("bmi")) return "\uD83D\uDCCF"; // 📏
        if (t.contains("app guide")) return "\uD83D\uDDFA"; // 🗺
        if (t.contains("hey!")) return "\uD83D\uDC4B"; // 👋
        if (t.contains("built-in")) return "\u2728"; // ✨
        if (t.contains("here's what i can help")) return "\uD83D\uDCCB"; // 📋
        if (t.contains("anytime")) return "\uD83D\uDE4C"; // 🙌
        if (t.contains("see you")) return "\uD83D\uDC4B";
        if (t.contains("outside what") || t.contains("stick to") || t.contains("don't have an answer")) return "\uD83E\uDD14"; // 🤔
        return "\u2728"; // ✨ default sparkle for anything uncategorized
    }
}

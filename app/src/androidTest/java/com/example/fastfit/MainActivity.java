package com.example.fastfit;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.TimeZone;

/**
 * MainActivity
 * ────────────
 * Reads the saved UserProfile (age, weight, height, sex, time-zone) and:
 *  • Shows the current time in the user's chosen time zone.
 *  • Adjusts exercise slot descriptions to the user's fitness tier
 *    (LIGHT / MODERATE / STANDARD – derived from age + BMI).
 *  • Displays a personalised BMR / BMI summary card.
 *
 * All slot timing logic is identical to the original; only the displayed
 * text for exercise type and tip changes per fitness tier.
 */
public class MainActivity extends AppCompatActivity {

    // ── UI Views ──────────────────────────────────────────────────────────────
    private TextView tvCurrentTime;
    private TextView tvStatusBadge;
    private TextView tvPeriod;
    private TextView tvTimeRange;
    private TextView tvExerciseType;
    private TextView tvTip;
    private TextView tvProfileSummary;   // NEW – shows age/BMI/BMR

    // ── Auto-refresh every minute ─────────────────────────────────────────────
    private final Handler handler = new Handler();
    private final Runnable clockRunnable = new Runnable() {
        @Override public void run() {
            updateRecommendation();
            handler.postDelayed(this, 60_000);
        }
    };

    // ── Data ──────────────────────────────────────────────────────────────────
    private List<ExerciseSlot> slots;
    private UserProfile        profile;
    private TimeZone           userTz;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // If the user hasn't completed setup, go to ProfileActivity first.
        SharedPreferences prefs = getSharedPreferences(ProfileActivity.PREFS, MODE_PRIVATE);
        if (!prefs.getBoolean(ProfileActivity.KEY_SETUP_OK, false)) {
            startActivity(new Intent(this, ProfileActivity.class));
            finish();
            return;
        }

        setContentView(R.layout.activity_main);
        loadProfile(prefs);
        bindViews();
        buildSlots();
        displayProfileSummary();

        // "✎ Profile" button → re-opens ProfileActivity
        findViewById(R.id.tvEditProfile).setOnClickListener(v -> {
            startActivity(new Intent(this, ProfileActivity.class));
        });

        handler.post(clockRunnable);
    }

    @Override protected void onDestroy() {
        super.onDestroy();
        handler.removeCallbacks(clockRunnable);
    }

    // ── Load profile from SharedPreferences ───────────────────────────────────
    private void loadProfile(SharedPreferences p) {
        int   age    = p.getInt(ProfileActivity.KEY_AGE, 30);
        float weight = p.getFloat(ProfileActivity.KEY_WEIGHT, 70f);
        float height = p.getFloat(ProfileActivity.KEY_HEIGHT, 170f);
        String sexStr = p.getString(ProfileActivity.KEY_SEX, "MALE");
        String tzId   = p.getString(ProfileActivity.KEY_TIMEZONE,
                TimeZone.getDefault().getID());

        UserProfile.Sex sex = "FEMALE".equals(sexStr)
                ? UserProfile.Sex.FEMALE : UserProfile.Sex.MALE;

        profile = new UserProfile(age, weight, height, sex, tzId);
        userTz  = TimeZone.getTimeZone(tzId);
    }

    // ── Bind XML views ────────────────────────────────────────────────────────
    private void bindViews() {
        tvCurrentTime   = findViewById(R.id.tvCurrentTime);
        tvStatusBadge   = findViewById(R.id.tvStatusBadge);
        tvPeriod        = findViewById(R.id.tvPeriod);
        tvTimeRange     = findViewById(R.id.tvTimeRange);
        tvExerciseType  = findViewById(R.id.tvExerciseType);
        tvTip           = findViewById(R.id.tvTip);
        tvProfileSummary = findViewById(R.id.tvProfileSummary);
    }

    // ── Show personalised stats under the clock ───────────────────────────────
    private void displayProfileSummary() {
        String tierLabel;
        switch (profile.fitnessTier()) {
            case LIGHT:    tierLabel = "Light intensity"; break;
            case MODERATE: tierLabel = "Moderate intensity"; break;
            default:       tierLabel = "Standard intensity"; break;
        }
        String summary = String.format(
                "Age %d  •  %.1f kg  •  %.0f cm  •  BMI %.1f  •  BMR ~%d kcal/day  •  %s",
                profile.ageYears, profile.weightKg, profile.heightCm,
                profile.bmi(), profile.bmrKcal(), tierLabel);
        tvProfileSummary.setText(summary);
    }

    // ── Build personalised exercise schedule ──────────────────────────────────
    //    Each slot has three variants keyed on FitnessTier.
    private void buildSlots() {
        slots = new ArrayList<>();
        UserProfile.FitnessTier tier = profile.fitnessTier();

        // Helper lambdas (plain static methods would also work)
        slots.add(slot_preSuhoor(tier));
        slots.add(slot_suhoorFajr(tier));
        slots.add(slot_morningFast(tier));
        slots.add(slot_afternoonFast(tier));
        slots.add(slot_lateAfternoon(tier));
        slots.add(slot_nearIftar(tier));
        slots.add(slot_justAfterIftar(tier));
        slots.add(slot_afterIftar(tier));
        slots.add(slot_evening(tier));
        slots.add(slot_lateNight(tier));
    }

    // ── Individual slot factories (exercise type + tip vary by tier) ──────────

    private static ExerciseSlot slot_preSuhoor(UserProfile.FitnessTier t) {
        switch (t) {
            case LIGHT:
                return new ExerciseSlot("3:00 AM – 4:00 AM", "Pre-Suhoor",
                        "Gentle Breathing & Stretching",
                        "Deep breathing and seated stretches to wake the body slowly.",
                        ExerciseSlot.Status.GOOD);
            case MODERATE:
                return new ExerciseSlot("3:00 AM – 4:00 AM", "Pre-Suhoor",
                        "Yoga / Light Stretching",
                        "A 15-min yoga flow is ideal before your pre-dawn meal.",
                        ExerciseSlot.Status.GOOD);
            default:
                return new ExerciseSlot("3:00 AM – 4:00 AM", "Pre-Suhoor",
                        "Yoga / Core Activation",
                        "Core planks + yoga sun salutations to prime the body.",
                        ExerciseSlot.Status.GOOD);
        }
    }

    private static ExerciseSlot slot_suhoorFajr(UserProfile.FitnessTier t) {
        switch (t) {
            case LIGHT:
                return new ExerciseSlot("4:00 AM – 6:00 AM", "Suhoor → Fajr",
                        "Short Easy Walk",
                        "5–10 min gentle stroll after eating; don't overexert.",
                        ExerciseSlot.Status.GOOD);
            case MODERATE:
                return new ExerciseSlot("4:00 AM – 6:00 AM", "Suhoor → Fajr",
                        "Brisk Walk (20 min)",
                        "Light cardio is fine just after Suhoor; keep heart rate low.",
                        ExerciseSlot.Status.GOOD);
            default:
                return new ExerciseSlot("4:00 AM – 6:00 AM", "Suhoor → Fajr",
                        "Brisk Walk / Light Jog",
                        "20–30 min brisk walk or easy jog while the body is fuelled.",
                        ExerciseSlot.Status.GOOD);
        }
    }

    private static ExerciseSlot slot_morningFast(UserProfile.FitnessTier t) {
        // All tiers: avoid
        return new ExerciseSlot("6:00 AM – 1:00 PM", "Morning Fast",
                "Rest – Avoid Exercise",
                "Body needs energy for fasting. Stay mentally hydrated.",
                ExerciseSlot.Status.AVOID);
    }

    private static ExerciseSlot slot_afternoonFast(UserProfile.FitnessTier t) {
        return new ExerciseSlot("1:00 PM – 4:00 PM", "Afternoon Fast",
                "Rest – Avoid Exercise",
                "Energy and hydration are at their lowest. Save it for later.",
                ExerciseSlot.Status.AVOID);
    }

    private static ExerciseSlot slot_lateAfternoon(UserProfile.FitnessTier t) {
        switch (t) {
            case LIGHT:
                return new ExerciseSlot("4:00 PM – 5:30 PM", "Late Afternoon",
                        "Seated / Supported Stretching",
                        "Chair yoga or gentle limb stretches only. Sip water if allowed.",
                        ExerciseSlot.Status.GOOD);
            default:
                return new ExerciseSlot("4:00 PM – 5:30 PM", "Late Afternoon",
                        "Light Stretching Only",
                        "Very gentle movement. No intense cardio – you're nearly at Iftar.",
                        ExerciseSlot.Status.GOOD);
        }
    }

    private static ExerciseSlot slot_nearIftar(UserProfile.FitnessTier t) {
        return new ExerciseSlot("5:30 PM – 6:30 PM", "Near Iftar",
                "Rest – Prepare for Iftar",
                "Relax and get ready to break your fast. No exercise.",
                ExerciseSlot.Status.AVOID);
    }

    private static ExerciseSlot slot_justAfterIftar(UserProfile.FitnessTier t) {
        switch (t) {
            case LIGHT:
                return new ExerciseSlot("6:30 PM – 7:30 PM", "Just After Iftar",
                        "Slow Walk",
                        "A gentle 10-min walk aids digestion. Don't rush.",
                        ExerciseSlot.Status.GOOD);
            default:
                return new ExerciseSlot("6:30 PM – 7:30 PM", "Just After Iftar",
                        "Slow Walk / Stretching",
                        "Give your body 30–60 min to digest before increasing intensity.",
                        ExerciseSlot.Status.GOOD);
        }
    }

    private static ExerciseSlot slot_afterIftar(UserProfile.FitnessTier t) {
        switch (t) {
            case LIGHT:
                return new ExerciseSlot("7:30 PM – 9:00 PM", "After Iftar",
                        "Walking / Gentle Cycling",
                        "30 min easy-paced walk or stationary bike at low resistance.",
                        ExerciseSlot.Status.BEST);
            case MODERATE:
                return new ExerciseSlot("7:30 PM – 9:00 PM", "After Iftar",
                        "Jogging / Moderate Cardio",
                        "Great window for moderate cardio once digestion begins.",
                        ExerciseSlot.Status.BEST);
            default:
                return new ExerciseSlot("7:30 PM – 9:00 PM", "After Iftar",
                        "Running / Moderate Cardio",
                        "Excellent cardio window. Target 70–80% max heart rate.",
                        ExerciseSlot.Status.BEST);
        }
    }

    private static ExerciseSlot slot_evening(UserProfile.FitnessTier t) {
        switch (t) {
            case LIGHT:
                return new ExerciseSlot("9:00 PM – 11:00 PM", "Evening",
                        "Light Resistance / Chair Exercises",
                        "Resistance bands or body-weight moves at a comfortable pace.",
                        ExerciseSlot.Status.BEST);
            case MODERATE:
                return new ExerciseSlot("9:00 PM – 11:00 PM", "Evening",
                        "Strength Training (moderate weight)",
                        "Compound lifts at 60–70% 1RM. Good hydration window.",
                        ExerciseSlot.Status.BEST);
            default:
                return new ExerciseSlot("9:00 PM – 11:00 PM", "Evening",
                        "Strength Training / HIIT",
                        "Peak time! Body is fuelled and hydrated. Push hard.",
                        ExerciseSlot.Status.BEST);
        }
    }

    private static ExerciseSlot slot_lateNight(UserProfile.FitnessTier t) {
        switch (t) {
            case LIGHT:
                return new ExerciseSlot("11:00 PM – 3:00 AM", "Late Night",
                        "Gentle Stretching / Breathing",
                        "Wind down with slow stretches and diaphragmatic breathing.",
                        ExerciseSlot.Status.GOOD);
            default:
                return new ExerciseSlot("11:00 PM – 3:00 AM", "Late Night",
                        "Yoga / Light Stretching",
                        "Wind down with gentle movement before sleep.",
                        ExerciseSlot.Status.GOOD);
        }
    }

    // ── Match current time (in user's TZ) → slot → update UI ─────────────────
    private void updateRecommendation() {
        Calendar cal = Calendar.getInstance(userTz);
        int hour   = cal.get(Calendar.HOUR_OF_DAY);
        int minute = cal.get(Calendar.MINUTE);
        int nowMin = hour * 60 + minute;

        // Display current time with TZ abbreviation
        String tzAbbr = userTz.getDisplayName(false, TimeZone.SHORT);
        tvCurrentTime.setText(formatTime(hour, minute) + " " + tzAbbr);

        ExerciseSlot match = findSlot(nowMin);
        if (match == null) return;

        tvPeriod.setText(match.getPeriod());
        tvTimeRange.setText(match.getTimeRange());
        tvExerciseType.setText(match.getExerciseType());
        tvTip.setText(match.getTip());

        switch (match.getStatus()) {
            case BEST:
                tvStatusBadge.setText("✦ BEST TIME");
                tvStatusBadge.setBackgroundColor(Color.parseColor("#1B6B3A"));
                break;
            case GOOD:
                tvStatusBadge.setText("✔ GOOD TIME");
                tvStatusBadge.setBackgroundColor(Color.parseColor("#5C7A29"));
                break;
            case AVOID:
                tvStatusBadge.setText("✕ AVOID");
                tvStatusBadge.setBackgroundColor(Color.parseColor("#8B2020"));
                break;
        }
    }

    // ── Find which slot the current minute falls into ─────────────────────────
    private ExerciseSlot findSlot(int nowMin) {
        int[][] b = {
                {toMin(3,  0), toMin(4,  0)},
                {toMin(4,  0), toMin(6,  0)},
                {toMin(6,  0), toMin(13, 0)},
                {toMin(13, 0), toMin(16, 0)},
                {toMin(16, 0), toMin(17, 30)},
                {toMin(17,30), toMin(18, 30)},
                {toMin(18,30), toMin(19, 30)},
                {toMin(19,30), toMin(21,  0)},
                {toMin(21, 0), toMin(23,  0)},
                {toMin(23, 0), toMin(27,  0)},   // wraps past midnight
        };

        // Times 0–3 AM are after midnight → shift by 24 h for range comparison
        int adjusted = (nowMin < toMin(3, 0)) ? nowMin + 1440 : nowMin;

        for (int i = 0; i < b.length; i++) {
            if (adjusted >= b[i][0] && adjusted < b[i][1]) return slots.get(i);
        }
        return slots.get(0);
    }

    // ── Helpers ───────────────────────────────────────────────────────────────
    private static int toMin(int h, int m) { return h * 60 + m; }

    private static String formatTime(int h, int m) {
        String amPm = h < 12 ? "AM" : "PM";
        int hr = h % 12;
        if (hr == 0) hr = 12;
        return String.format("%d:%02d %s", hr, m, amPm);
    }
}
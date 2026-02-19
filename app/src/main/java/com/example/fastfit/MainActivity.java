package com.example.fastfit;

import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    // ── UI Views ──────────────────────────────────────────────────────────────
    private TextView tvCurrentTime;
    private TextView tvStatusBadge;
    private TextView tvPeriod;
    private TextView tvTimeRange;
    private TextView tvExerciseType;
    private TextView tvTip;

    // ── Auto-refresh every minute ─────────────────────────────────────────────
    private final Handler handler = new Handler();
    private final Runnable clockRunnable = new Runnable() {
        @Override public void run() {
            updateRecommendation();
            handler.postDelayed(this, 60_000); // refresh every 60 s
        }
    };

    // ── All time slots (24-hour schedule for Ramadan) ─────────────────────────
    private List<ExerciseSlot> slots;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        bindViews();
        buildSlots();
        handler.post(clockRunnable);          // start immediately
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        handler.removeCallbacks(clockRunnable);
    }

    // ── Bind XML views ────────────────────────────────────────────────────────
    private void bindViews() {
        tvCurrentTime  = findViewById(R.id.tvCurrentTime);
        tvStatusBadge  = findViewById(R.id.tvStatusBadge);
        tvPeriod       = findViewById(R.id.tvPeriod);
        tvTimeRange    = findViewById(R.id.tvTimeRange);
        tvExerciseType = findViewById(R.id.tvExerciseType);
        tvTip          = findViewById(R.id.tvTip);
    }

    // ── Build the daily exercise schedule ─────────────────────────────────────
    //    Times are intentionally simple (whole/half hours) so you can adjust
    //    them to match local prayer times later.
    private void buildSlots() {
        slots = new ArrayList<>();

        // startHour, startMin, endHour, endMin
        slots.add(new ExerciseSlot(
                "3:00 AM – 4:00 AM", "Pre-Suhoor",
                "Yoga / Light Stretching",
                "Gentle warm-up before your pre-dawn meal.",
                ExerciseSlot.Status.GOOD));

        slots.add(new ExerciseSlot(
                "4:00 AM – 6:00 AM", "Suhoor → Fajr",
                "Brisk Walk",
                "Light cardio is fine just after Suhoor.",
                ExerciseSlot.Status.GOOD));

        slots.add(new ExerciseSlot(
                "6:00 AM – 1:00 PM", "Morning Fast",
                "Rest – Avoid Exercise",
                "Body needs energy for fasting. Stay hydrated mentally.",
                ExerciseSlot.Status.AVOID));

        slots.add(new ExerciseSlot(
                "1:00 PM – 4:00 PM", "Afternoon Fast",
                "Rest – Avoid Exercise",
                "Energy and hydration are lowest. Save it for later.",
                ExerciseSlot.Status.AVOID));

        slots.add(new ExerciseSlot(
                "4:00 PM – 5:30 PM", "Late Afternoon",
                "Light Stretching Only",
                "Very gentle movement. No intense cardio.",
                ExerciseSlot.Status.GOOD));

        slots.add(new ExerciseSlot(
                "5:30 PM – 6:30 PM", "Near Iftar",
                "Rest – Prepare for Iftar",
                "Relax and get ready to break your fast.",
                ExerciseSlot.Status.AVOID));

        slots.add(new ExerciseSlot(
                "6:30 PM – 7:30 PM", "Just After Iftar",
                "Slow Walk / Stretching",
                "Give your body 30–60 min to digest before moving.",
                ExerciseSlot.Status.GOOD));

        slots.add(new ExerciseSlot(
                "7:30 PM – 9:00 PM", "After Iftar",
                "Running / Moderate Cardio",
                "Great window for cardio once digestion begins.",
                ExerciseSlot.Status.BEST));

        slots.add(new ExerciseSlot(
                "9:00 PM – 11:00 PM", "Evening",
                "Strength Training / HIIT",
                "Peak time! Body is fuelled and hydrated. Go hard.",
                ExerciseSlot.Status.BEST));

        slots.add(new ExerciseSlot(
                "11:00 PM – 3:00 AM", "Late Night",
                "Yoga / Light Stretching",
                "Wind down with gentle movement before sleep.",
                ExerciseSlot.Status.GOOD));
    }

    // ── Match current time → slot → update UI ────────────────────────────────
    private void updateRecommendation() {
        Calendar cal         = Calendar.getInstance();
        int      hour        = cal.get(Calendar.HOUR_OF_DAY);
        int      minute      = cal.get(Calendar.MINUTE);
        int      nowMinutes  = hour * 60 + minute;          // e.g. 14:30 → 870

        // Display current time
        tvCurrentTime.setText(formatTime(hour, minute));

        ExerciseSlot match = findSlot(nowMinutes);
        if (match == null) return;

        // ── Update text fields ────────────────────────────────────────────────
        tvPeriod.setText(match.getPeriod());
        tvTimeRange.setText(match.getTimeRange());
        tvExerciseType.setText(match.getExerciseType());
        tvTip.setText(match.getTip());

        // ── Color-code the status badge ───────────────────────────────────────
        switch (match.getStatus()) {
            case BEST:
                tvStatusBadge.setText("✦ BEST TIME");
                tvStatusBadge.setBackgroundColor(Color.parseColor("#1B6B3A")); // deep green
                break;
            case GOOD:
                tvStatusBadge.setText("✔ GOOD TIME");
                tvStatusBadge.setBackgroundColor(Color.parseColor("#5C7A29")); // olive
                break;
            case AVOID:
                tvStatusBadge.setText("✕ AVOID");
                tvStatusBadge.setBackgroundColor(Color.parseColor("#8B2020")); // deep red
                break;
        }
    }

    // ── Find which slot the current minute falls into ─────────────────────────
    //    Slot boundaries derived from the time-range strings (see buildSlots).
    private ExerciseSlot findSlot(int nowMinutes) {
        // [startMinutes, endMinutes] for each slot (matching buildSlots order)
        int[][] boundaries = {
                {toMin(3,  0), toMin(4,  0)},   // Pre-Suhoor
                {toMin(4,  0), toMin(6,  0)},   // Suhoor → Fajr
                {toMin(6,  0), toMin(13, 0)},   // Morning Fast
                {toMin(13, 0), toMin(16, 0)},   // Afternoon Fast
                {toMin(16, 0), toMin(17, 30)},  // Late Afternoon
                {toMin(17, 30), toMin(18, 30)}, // Near Iftar
                {toMin(18, 30), toMin(19, 30)}, // Just After Iftar
                {toMin(19, 30), toMin(21, 0)},  // After Iftar
                {toMin(21, 0), toMin(23, 0)},   // Evening
                {toMin(23, 0), toMin(27, 0)},   // Late Night (wraps past midnight)
        };

        // Handle post-midnight times (0–3 AM → add 24 h for comparison)
        int adjusted = (nowMinutes < toMin(3, 0)) ? nowMinutes + 1440 : nowMinutes;

        for (int i = 0; i < boundaries.length; i++) {
            if (adjusted >= boundaries[i][0] && adjusted < boundaries[i][1]) {
                return slots.get(i);
            }
        }
        return slots.get(0); // fallback
    }

    // ── Helpers ───────────────────────────────────────────────────────────────
    private static int toMin(int h, int m) { return h * 60 + m; }

    private static String formatTime(int h, int m) {
        String amPm  = h < 12 ? "AM" : "PM";
        int    hour  = h % 12;
        if (hour == 0) hour = 12;
        return String.format("%d:%02d %s", hour, m, amPm);
    }
}
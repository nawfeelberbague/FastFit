# FastFit 🕌 – Exercise Guide for Fasting Muslims

Simple Android app (Java) that shows the **best time to work out** based on
the current clock time during Ramadan.

---

## Screenshots

| Home Screen | Schedule View |
|:-----------:|:-------------:|
| ![Home Screen](screenshots/home_screen.png) | ![Schedule View](screenshots/schedule_view.png) |
| *Current time & recommendation card* | *Full daily schedule reference* |

> 📁 Place your screenshots in a `screenshots/` folder at the root of the project.

---

## Developed By

| Developer | Role |
|-----------|------|
| **Nawfel Berbague** | Android Developer |
| **Yaakoub Bouacha** | Android Developer |

---

## Project Structure

```
app/
├── java/com/example/fastfit/
│   ├── ExerciseSlot.java      ← Data model (time window + exercise info)
│   └── MainActivity.java     ← All logic (time detection, UI update)
│
└── res/layout/
    └── activity_main.xml      ← Single-screen UI (no fragments needed)
```

---

## How It Works

```
Current Time
     │
     ▼
findSlot()   ←── compares hour:minute against slot boundaries
     │
     ▼
ExerciseSlot  (period, timeRange, exerciseType, tip, status)
     │
     ▼
UI updated   (badge colour = GREEN / OLIVE / RED based on status)
```

Auto-refreshes every **60 seconds** so the recommendation stays live.

---

## Daily Schedule (default)

| Time Window       | Period            | Exercise              | Status |
|-------------------|-------------------|-----------------------|--------|
| 3:00 – 4:00 AM    | Pre-Suhoor        | Yoga / Stretching     | GOOD   |
| 4:00 – 6:00 AM    | Suhoor → Fajr     | Brisk Walk            | GOOD   |
| 6:00 AM – 1:00 PM | Morning Fast      | Rest                  | AVOID  |
| 1:00 – 4:00 PM    | Afternoon Fast    | Rest                  | AVOID  |
| 4:00 – 5:30 PM    | Late Afternoon    | Light Stretching      | GOOD   |
| 5:30 – 6:30 PM    | Near Iftar        | Rest                  | AVOID  |
| 6:30 – 7:30 PM    | Just After Iftar  | Slow Walk             | GOOD   |
| 7:30 – 9:00 PM    | After Iftar       | Running / Cardio      | BEST ✦ |
| 9:00 – 11:00 PM   | Evening           | Strength / HIIT       | BEST ✦ |
| 11:00 PM – 3:00 AM| Late Night        | Yoga / Stretching     | GOOD   |

---
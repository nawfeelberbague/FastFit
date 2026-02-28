package com.example.fastfit;

/**
 * Simple value object holding the user's personal details.
 * Stored/retrieved via SharedPreferences in MainActivity.
 */
public class UserProfile {

    public enum Sex { MALE, FEMALE }

    public final int    ageYears;        // e.g. 30
    public final float  weightKg;        // e.g. 75.0
    public final float  heightCm;        // e.g. 175.0
    public final Sex    sex;
    public final String timeZoneId;      // e.g. "Asia/Riyadh"

    public UserProfile(int ageYears, float weightKg, float heightCm,
                       Sex sex, String timeZoneId) {
        this.ageYears   = ageYears;
        this.weightKg   = weightKg;
        this.heightCm   = heightCm;
        this.sex        = sex;
        this.timeZoneId = timeZoneId;
    }

    // ── Derived helpers ───────────────────────────────────────────────────────

    /** Body-Mass Index */
    public float bmi() {
        float hM = heightCm / 100f;
        return weightKg / (hM * hM);
    }

    /**
     * Basal Metabolic Rate – Mifflin-St Jeor equation (kcal/day).
     * Used only for informational display; exercise slots are time-based.
     */
    public int bmrKcal() {
        // 10×weight(kg) + 6.25×height(cm) − 5×age + sex_constant
        float base = 10 * weightKg + 6.25f * heightCm - 5 * ageYears;
        return Math.round(sex == Sex.MALE ? base + 5 : base - 161);
    }

    /**
     * Simple fitness tier used to adjust exercise intensity text.
     *   LIGHT  → age < 18 || age > 60 || bmi > 30
     *   MODERATE → bmi 25–30 or age 50–60
     *   STANDARD → everyone else
     */
    public enum FitnessTier { LIGHT, MODERATE, STANDARD }

    public FitnessTier fitnessTier() {
        float b = bmi();
        if (ageYears < 18 || ageYears > 60 || b > 30f) return FitnessTier.LIGHT;
        if (b > 25f || ageYears > 50)                  return FitnessTier.MODERATE;
        return FitnessTier.STANDARD;
    }
}
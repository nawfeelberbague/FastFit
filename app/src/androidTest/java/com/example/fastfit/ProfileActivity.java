package com.example.fastfit;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import java.util.TimeZone;

/**
 * ProfileActivity
 * ───────────────
 * Shown on first launch (or when the user taps "Edit Profile").
 * Collects: age, weight (kg), height (cm), sex, time-zone.
 * Persists everything to SharedPreferences and launches MainActivity.
 */
public class ProfileActivity extends AppCompatActivity {

    public static final String PREFS        = "fastfit_prefs";
    public static final String KEY_AGE      = "age";
    public static final String KEY_WEIGHT   = "weight";
    public static final String KEY_HEIGHT   = "height";
    public static final String KEY_SEX      = "sex";          // "MALE" | "FEMALE"
    public static final String KEY_TIMEZONE = "timezone";
    public static final String KEY_SETUP_OK = "setup_done";

    private EditText   etAge, etWeight, etHeight;
    private RadioGroup rgSex;
    private Spinner    spTimezone;
    private Button     btnSave;

    /** All available time-zone IDs, alphabetically sorted. */
    private String[] tzIds;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        bindViews();
        populateTimezoneSpinner();
        prefillIfExists();

        btnSave.setOnClickListener(v -> saveAndContinue());
    }

    // ── Bind XML views ────────────────────────────────────────────────────────
    private void bindViews() {
        etAge      = findViewById(R.id.etAge);
        etWeight   = findViewById(R.id.etWeight);
        etHeight   = findViewById(R.id.etHeight);
        rgSex      = findViewById(R.id.rgSex);
        spTimezone = findViewById(R.id.spTimezone);
        btnSave    = findViewById(R.id.btnSave);
    }

    // ── Populate time-zone spinner ────────────────────────────────────────────
    private void populateTimezoneSpinner() {
        tzIds = TimeZone.getAvailableIDs();
        // Sort alphabetically for easier browsing
        java.util.Arrays.sort(tzIds);

        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this, android.R.layout.simple_spinner_item, tzIds);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spTimezone.setAdapter(adapter);

        // Default to device time zone
        String deviceTz = TimeZone.getDefault().getID();
        for (int i = 0; i < tzIds.length; i++) {
            if (tzIds[i].equals(deviceTz)) {
                spTimezone.setSelection(i);
                break;
            }
        }
    }

    // ── Pre-fill from saved prefs (edit-profile flow) ─────────────────────────
    private void prefillIfExists() {
        SharedPreferences p = getSharedPreferences(PREFS, MODE_PRIVATE);
        if (!p.getBoolean(KEY_SETUP_OK, false)) return;

        etAge.setText(String.valueOf(p.getInt(KEY_AGE, 0)));
        etWeight.setText(String.valueOf(p.getFloat(KEY_WEIGHT, 0)));
        etHeight.setText(String.valueOf(p.getFloat(KEY_HEIGHT, 0)));

        String sex = p.getString(KEY_SEX, "MALE");
        if ("FEMALE".equals(sex)) {
            ((RadioButton) findViewById(R.id.rbFemale)).setChecked(true);
        } else {
            ((RadioButton) findViewById(R.id.rbMale)).setChecked(true);
        }

        String savedTz = p.getString(KEY_TIMEZONE, "");
        for (int i = 0; i < tzIds.length; i++) {
            if (tzIds[i].equals(savedTz)) { spTimezone.setSelection(i); break; }
        }
    }

    // ── Validate → save → launch main screen ─────────────────────────────────
    private void saveAndContinue() {
        String ageStr    = etAge.getText().toString().trim();
        String weightStr = etWeight.getText().toString().trim();
        String heightStr = etHeight.getText().toString().trim();

        if (ageStr.isEmpty() || weightStr.isEmpty() || heightStr.isEmpty()) {
            Toast.makeText(this, "Please fill in all fields.", Toast.LENGTH_SHORT).show();
            return;
        }

        int   age;
        float weight, height;
        try {
            age    = Integer.parseInt(ageStr);
            weight = Float.parseFloat(weightStr);
            height = Float.parseFloat(heightStr);
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Invalid number entered.", Toast.LENGTH_SHORT).show();
            return;
        }

        if (age < 10 || age > 110 || weight < 20 || weight > 300
                || height < 100 || height > 250) {
            Toast.makeText(this, "Please check your values.", Toast.LENGTH_SHORT).show();
            return;
        }

        int checkedId = rgSex.getCheckedRadioButtonId();
        if (checkedId == -1) {
            Toast.makeText(this, "Please select your sex.", Toast.LENGTH_SHORT).show();
            return;
        }
        String sex = (checkedId == R.id.rbFemale) ? "FEMALE" : "MALE";

        String tz = tzIds[spTimezone.getSelectedItemPosition()];

        // Persist
        getSharedPreferences(PREFS, MODE_PRIVATE).edit()
                .putInt(KEY_AGE, age)
                .putFloat(KEY_WEIGHT, weight)
                .putFloat(KEY_HEIGHT, height)
                .putString(KEY_SEX, sex)
                .putString(KEY_TIMEZONE, tz)
                .putBoolean(KEY_SETUP_OK, true)
                .apply();

        startActivity(new Intent(this, MainActivity.class));
        finish();
    }
}
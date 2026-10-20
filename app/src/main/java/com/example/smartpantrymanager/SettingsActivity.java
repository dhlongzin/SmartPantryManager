package com.example.smartpantrymanager;

import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.switchmaterial.SwitchMaterial;

/**
 * Settings screen. The expiring-soon toggle is saved in SharedPreferences,
 * so the choice is remembered after the app is closed and reopened.
 */
public class SettingsActivity extends AppCompatActivity {

    // Shared with MainActivity, which reads the setting
    public static final String PREFS_NAME = "pantry_settings";
    public static final String KEY_EXPIRY_WARNINGS = "expiry_warnings";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_settings);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, windowInsets) -> {
            Insets systemBars = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return windowInsets;
        });

        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);

        // Show the saved value (warnings are ON by default)
        SwitchMaterial switchExpiry = findViewById(R.id.switchExpiryWarnings);
        switchExpiry.setChecked(prefs.getBoolean(KEY_EXPIRY_WARNINGS, true));

        // Save the new value as soon as the switch is flipped
        switchExpiry.setOnCheckedChangeListener((buttonView, isChecked) ->
                prefs.edit().putBoolean(KEY_EXPIRY_WARNINGS, isChecked).apply());

        // Bottom navigation: highlight the Settings tab
        NavigationHelper.setup(this, R.id.nav_settings);
    }
}
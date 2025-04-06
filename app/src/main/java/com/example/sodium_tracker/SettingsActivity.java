package com.example.sodium_tracker;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.CompoundButton;
import android.widget.Switch;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.preference.PreferenceManager;

import com.google.android.material.bottomnavigation.BottomNavigationView;

public class SettingsActivity extends AppCompatActivity {

    private Switch themeSwitch;
    private ConstraintLayout settingsLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        settingsLayout = findViewById(R.id.settingsLayout);
        themeSwitch = findViewById(R.id.themeSwitch);

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        boolean isYellow = prefs.getBoolean("paleYellow", false); // FIXED

        // Set switch based on preference
        themeSwitch.setChecked(isYellow);
        if (isYellow) {
            settingsLayout.setBackgroundColor(getResources().getColor(R.color.pale_yellow));
        }

        themeSwitch.setOnCheckedChangeListener((CompoundButton buttonView, boolean isChecked) -> {
            SharedPreferences.Editor editor = prefs.edit();
            prefs.edit().putBoolean("paleYellow", isChecked).apply(); // FIXED
            editor.apply();

            // Apply background immediately
            if (isChecked) {
                settingsLayout.setBackgroundColor(getResources().getColor(R.color.pale_yellow));
            } else {
                settingsLayout.setBackgroundColor(getResources().getColor(android.R.color.white));
            }
        });

        BottomNavigationView bottomNav = findViewById(R.id.bottomNavigationView);
        bottomNav.setSelectedItemId(R.id.nav_settings);
        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_menu) {
                startActivity(new Intent(this, MainActivity.class));
                finish();
                return true;
            } else if (id == R.id.nav_database) {
                startActivity(new Intent(this, DatabaseActivity.class));
                finish();
                return true;
            } else if (id == R.id.nav_daily_tracker) {
                startActivity(new Intent(this, DailyTrackerActivity.class));
                finish();
                return true;
            }
            return false;
        });
    }
}

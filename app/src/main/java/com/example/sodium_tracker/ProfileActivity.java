package com.example.sodium_tracker;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.preference.PreferenceManager;

import com.google.android.material.bottomnavigation.BottomNavigationView;

public class ProfileActivity extends AppCompatActivity {

    private TextView usernameText, emailText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        // Apply yellow background preference
        ConstraintLayout layout = findViewById(R.id.profileLayout);
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        boolean isYellow = prefs.getBoolean("yellow_background", false);
        if (isYellow) {
            layout.setBackgroundColor(getResources().getColor(R.color.pale_yellow));
        } else {
            layout.setBackgroundColor(getResources().getColor(android.R.color.white));
        }

        // Profile info
        usernameText = findViewById(R.id.usernameField);
        emailText = findViewById(R.id.emailField);
        usernameText.setText("Dundee");
        emailText.setText("acyantune@gmail.com");

        // Bottom navigation
        BottomNavigationView bottomNav = findViewById(R.id.bottomNavigationView);
        bottomNav.setSelectedItemId(R.id.nav_daily_tracker);

        bottomNav.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.nav_menu) {
                startActivity(new Intent(this, MainActivity.class));
                finish();
                return true;
            } else if (itemId == R.id.nav_database) {
                startActivity(new Intent(this, DatabaseActivity.class));
                finish();
                return true;
            } else if (itemId == R.id.nav_settings) {
                startActivity(new Intent(this, SettingsActivity.class));
                finish();
                return true;
            }
            return false;
        });
    }
}

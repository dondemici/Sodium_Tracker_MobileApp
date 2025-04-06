package com.example.sodium_tracker;

import android.app.DatePickerDialog;
import android.content.ContentValues;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.*;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class DailyTrackerActivity extends AppCompatActivity implements DailyLogAdapter.OnLogActionListener {

    private RecyclerView dailyLogRecyclerView;
    private DailyLogAdapter logAdapter;
    private List<DailyLogEntry> logList;
    private double totalSodium = 0;
    private Button datePickerButton, addLogButton, weeklyAvgButton, monthlyAvgButton;
    private TextView dateTextView, totalSodiumText, sodiumWarning;
    private Spinner recipeSpinner, mealTypeSpinner;
    private ConstraintLayout layout;

    private SQLiteDatabase database;
    private DatabaseHelper dbHelper;
    private SharedPreferences prefs;
    private BarChart barChart;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_daily_tracker);

        layout = findViewById(R.id.dailyTrackerLayout);
        prefs = PreferenceManager.getDefaultSharedPreferences(this);
        boolean isYellow = prefs.getBoolean("paleYellow", false);
        if (isYellow) {
            layout.setBackgroundColor(getResources().getColor(R.color.pale_yellow));
        }

        totalSodiumText = findViewById(R.id.totalSodiumText);
        sodiumWarning = findViewById(R.id.sodiumWarning);
        dailyLogRecyclerView = findViewById(R.id.dailyLogRecyclerView);
        datePickerButton = findViewById(R.id.datePickerButton);
        dateTextView = findViewById(R.id.dateTextView);
        recipeSpinner = findViewById(R.id.recipeSpinner);
        mealTypeSpinner = findViewById(R.id.mealTypeSpinner);
        addLogButton = findViewById(R.id.addLogButton);
        weeklyAvgButton = findViewById(R.id.weeklyAvgButton);
        monthlyAvgButton = findViewById(R.id.monthlyAvgButton);
        barChart = findViewById(R.id.sodiumBarChart);

        dbHelper = new DatabaseHelper(this);
        database = dbHelper.getWritableDatabase();

        logList = new ArrayList<>();
        logAdapter = new DailyLogAdapter(logList, this);
        dailyLogRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        dailyLogRecyclerView.setAdapter(logAdapter);

        BottomNavigationView bottomNav = findViewById(R.id.bottomNavigationView);
        bottomNav.setSelectedItemId(R.id.nav_daily_tracker);

        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_menu) {
                startActivity(new Intent(this, MainActivity.class));
                return true;
            } else if (id == R.id.nav_database) {
                startActivity(new Intent(this, DatabaseActivity.class));
                return true;
            } else if (id == R.id.nav_settings) {
                startActivity(new Intent(this, SettingsActivity.class));
                return true;
            }
            return false;
        });

        datePickerButton.setOnClickListener(v -> {
            Calendar calendar = Calendar.getInstance();
            int year = calendar.get(Calendar.YEAR);
            int month = calendar.get(Calendar.MONTH);
            int day = calendar.get(Calendar.DAY_OF_MONTH);

            DatePickerDialog datePickerDialog = new DatePickerDialog(
                    DailyTrackerActivity.this,
                    (view, year1, month1, dayOfMonth) -> {
                        String selectedDate = String.format(Locale.getDefault(), "%04d-%02d-%02d", year1, month1 + 1, dayOfMonth);
                        dateTextView.setText(selectedDate);
                        loadLogsForDate(selectedDate);
                    },
                    year, month, day
            );
            datePickerDialog.show();
        });

        ArrayList<String> recipeNames = new ArrayList<>();
        recipeNames.add("Select Recipe");
        Cursor recipeCursor = database.rawQuery("SELECT Name FROM Recipe", null);
        while (recipeCursor.moveToNext()) {
            recipeNames.add(recipeCursor.getString(0));
        }
        recipeCursor.close();
        ArrayAdapter<String> recipeAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, recipeNames);
        recipeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        recipeSpinner.setAdapter(recipeAdapter);

        ArrayList<String> mealTypes = new ArrayList<>();
        mealTypes.add("Select Meal Type");
        mealTypes.add("Breakfast");
        mealTypes.add("Lunch");
        mealTypes.add("Dinner");
        mealTypes.add("Snack");
        ArrayAdapter<String> mealAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, mealTypes);
        mealAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mealTypeSpinner.setAdapter(mealAdapter);

        addLogButton.setOnClickListener(v -> {
            String selectedDate = dateTextView.getText().toString().trim();
            String selectedRecipe = recipeSpinner.getSelectedItem().toString();
            String mealType = mealTypeSpinner.getSelectedItem().toString();

            if (selectedRecipe.equals("Select Recipe") || mealType.equals("Select Meal Type")) {
                Toast.makeText(this, "Please select a valid recipe and meal type", Toast.LENGTH_SHORT).show();
                return;
            }

            double sodiumPerServing = 0;
            Cursor servingCursor = database.rawQuery(
                    "SELECT SUM(SodiumAmount), (SELECT ServingCount FROM Recipe WHERE Name = ?) FROM RecipeIngredient WHERE RecipeID = (SELECT RecipeID FROM Recipe WHERE Name = ?)",
                    new String[]{selectedRecipe, selectedRecipe}
            );
            if (servingCursor.moveToFirst()) {
                double totalSodium = servingCursor.getDouble(0);
                int servingCount = servingCursor.getInt(1);
                if (servingCount > 0) {
                    sodiumPerServing = totalSodium / servingCount;
                } else {
                    sodiumPerServing = totalSodium;
                }
            }
            servingCursor.close();

            ContentValues values = new ContentValues();
            values.put("LogDate", selectedDate);
            values.put("RecipeName", selectedRecipe);
            values.put("MealType", mealType);
            values.put("SodiumAmount", sodiumPerServing);
            database.insert("DailyLog", null, values);

            loadLogsForDate(selectedDate);
        });

        weeklyAvgButton.setOnClickListener(v -> showSodiumAverage("week"));
        monthlyAvgButton.setOnClickListener(v -> showSodiumAverage("month"));

        String today = String.format(Locale.getDefault(), "%04d-%02d-%02d",
                Calendar.getInstance().get(Calendar.YEAR),
                Calendar.getInstance().get(Calendar.MONTH) + 1,
                Calendar.getInstance().get(Calendar.DAY_OF_MONTH));
        dateTextView.setText(today);
        loadLogsForDate(today);
    }

    private void showSodiumAverage(String type) {
        View chartView = getLayoutInflater().inflate(R.layout.popup_chart, null);
        BarChart popupBarChart = chartView.findViewById(R.id.popupBarChart);

        ArrayList<BarEntry> entries = new ArrayList<>();
        ArrayList<String> labels = new ArrayList<>();

        Cursor cursor;
        if (type.equals("week")) {
            cursor = database.rawQuery("SELECT strftime('%W', LogDate) as week, AVG(SodiumAmount) FROM DailyLog GROUP BY week ORDER BY week DESC LIMIT 4", null);
            int index = 0;
            while (cursor.moveToNext()) {
                float avg = cursor.getFloat(1);
                entries.add(new BarEntry(index, avg));
                labels.add("Wk " + cursor.getString(0));
                index++;
            }
        } else {
            cursor = database.rawQuery("SELECT strftime('%m', LogDate) as month, AVG(SodiumAmount) FROM DailyLog GROUP BY month ORDER BY month DESC LIMIT 3", null);
            int index = 0;
            while (cursor.moveToNext()) {
                float avg = cursor.getFloat(1);
                entries.add(new BarEntry(index, avg));
                labels.add("Mo " + cursor.getString(0));
                index++;
            }
        }
        cursor.close();

        BarDataSet dataSet = new BarDataSet(entries, "Average Sodium Intake (mg)");
        BarData barData = new BarData(dataSet);
        popupBarChart.setData(barData);
        popupBarChart.invalidate();

        new android.app.AlertDialog.Builder(this)
                .setTitle(type.equals("week") ? "Weekly Sodium Average" : "Monthly Sodium Average")
                .setView(chartView)
                .setPositiveButton("OK", null)
                .show();
    }

    private void updateTotalSodium() {
        totalSodium = 0;
        for (DailyLogEntry entry : logList) {
            totalSodium += entry.getSodium();
        }

        //totalSodiumText.setText("Total Sodium Today: " + totalSodium + " mg");
        totalSodiumText.setText(String.format("Total Sodium Today: %.2f mg", totalSodium));

        if (totalSodium > 2300) {
            totalSodiumText.setTextColor(Color.RED);
            sodiumWarning.setVisibility(View.VISIBLE);
        } else {
            totalSodiumText.setTextColor(Color.BLACK);
            sodiumWarning.setVisibility(View.GONE);
        }
    }

    @Override
    public void onEditClick(DailyLogEntry entry) {
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_edit_log, null);
        Spinner mealTypeSpinner = dialogView.findViewById(R.id.editMealTypeSpinner);

        ArrayAdapter<String> mealTypeAdapter = new ArrayAdapter<>(
                this, android.R.layout.simple_spinner_item,
                new String[]{"Breakfast", "Lunch", "Dinner", "Snack"}
        );
        mealTypeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mealTypeSpinner.setAdapter(mealTypeAdapter);

        int index = mealTypeAdapter.getPosition(entry.getMealType());
        mealTypeSpinner.setSelection(index);

        new android.app.AlertDialog.Builder(this)
                .setTitle("Edit Meal Type")
                .setView(dialogView)
                .setPositiveButton("Save", (dialog, which) -> {
                    String newMealType = mealTypeSpinner.getSelectedItem().toString();
                    ContentValues values = new ContentValues();
                    values.put("MealType", newMealType);
                    database.update("DailyLog", values, "LogID = ?", new String[]{String.valueOf(entry.getId())});

                    entry.updateMealType(newMealType);
                    logAdapter.notifyDataSetChanged();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    @Override
    public void onDeleteClick(DailyLogEntry entry) {
        database.delete("DailyLog", "LogID = ?", new String[]{String.valueOf(entry.getId())});
        logList.remove(entry);
        logAdapter.notifyDataSetChanged();
        updateTotalSodium();
    }

    private void loadLogsForDate(String date) {
        logList.clear();
        Cursor cursor = database.rawQuery("SELECT LogID, LogDate, RecipeName, MealType, SodiumAmount FROM DailyLog WHERE LogDate = ?", new String[]{date});
        while (cursor.moveToNext()) {
            int id = cursor.getInt(0);
            String logDate = cursor.getString(1);
            String recipeName = cursor.getString(2);
            String mealType = cursor.getString(3);
            double sodium = cursor.getDouble(4);
            logList.add(new DailyLogEntry(id, logDate, recipeName, mealType, sodium));
        }
        cursor.close();
        logAdapter.notifyDataSetChanged();
        updateTotalSodium();
    }
}

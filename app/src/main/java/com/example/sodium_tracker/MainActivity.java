package com.example.sodium_tracker;

import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.graphics.Color;
import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.widget.*;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.preference.PreferenceManager;
import android.content.SharedPreferences;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    private EditText ingredientName, ingredientQuantity;
    private Spinner unitSpinner;
    private TextView sodiumContent, totalSodiumText, sodiumWarning;
    private Button addButton, clearButton, saveButton;
    private ListView ingredientListView;
    private ArrayList<String> ingredientList;
    private ArrayAdapter<String> adapter;
    private SQLiteDatabase database;
    private double totalSodium = 0;
    private SharedPreferences prefs;
    private ConstraintLayout layout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        layout = findViewById(R.id.mainLayout); // Make sure your root layout has this ID
        prefs = PreferenceManager.getDefaultSharedPreferences(this);

// Apply background color immediately
        boolean isYellow = prefs.getBoolean("paleYellow", false);
        layout.setBackgroundColor(getResources().getColor(isYellow ? R.color.pale_yellow : android.R.color.white));

        ConstraintLayout layout = findViewById(R.id.mainLayout);
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        //boolean isYellow = prefs.getBoolean("yellow_background", false);

        //if (isYellow) {
        //    layout.setBackgroundColor(getResources().getColor(R.color.pale_yellow));
        //} else {
        //    layout.setBackgroundColor(getResources().getColor(android.R.color.white));
       // }

        bindViews();
        setupDatabase();
        setupSpinner();
        setupListeners();

        saveButton.setOnClickListener(v -> promptSaveRecipe());

        BottomNavigationView bottomNav = findViewById(R.id.bottomNavigationView);
        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_database) {
                Intent intent = new Intent(MainActivity.this, DatabaseActivity.class);
                startActivityForResult(intent, 100);
                return true;
            } else if (id == R.id.nav_daily_tracker) {
                startActivity(new Intent(this, DailyTrackerActivity.class));
                return true;
            } else if (id == R.id.nav_settings) {
                startActivity(new Intent(this, SettingsActivity.class));
                return true;
            }
            return true;
        });

        bottomNav.setSelectedItemId(R.id.nav_menu);
    }

    private void bindViews() {
        ingredientName = findViewById(R.id.ingredientName);
        ingredientQuantity = findViewById(R.id.ingredientQuantity);
        unitSpinner = findViewById(R.id.spinner);
        sodiumContent = findViewById(R.id.sodiumContent);
        totalSodiumText = findViewById(R.id.totalSodiumText);
        sodiumWarning = findViewById(R.id.sodiumWarning);
        addButton = findViewById(R.id.addButton);
        clearButton = findViewById(R.id.clearButton);
        saveButton = findViewById(R.id.saveButton);
        ingredientListView = findViewById(R.id.ingredientList);

        ingredientList = new ArrayList<>();
        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, ingredientList);
        ingredientListView.setAdapter(adapter);
    }

    private void setupListeners() {
        addButton.setOnClickListener(v -> addIngredient());
        clearButton.setOnClickListener(v -> clearIngredientFields());
    }

    private void setupDatabase() {
        DatabaseHelper dbHelper = new DatabaseHelper(this);
        database = dbHelper.getWritableDatabase();
    }

    private void setupSpinner() {
        ArrayList<String> units = new ArrayList<>();
        units.add("Select Unit Gauge");
        units.add("cup");
        units.add("tablespoon");
        units.add("teaspoon");
        units.add("gram");
        units.add("piece");

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, units);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        unitSpinner.setAdapter(adapter);
    }

    private void addIngredient() {
        String name = ingredientName.getText().toString().trim();
        String quantityStr = ingredientQuantity.getText().toString().trim();

        if (name.isEmpty() || quantityStr.isEmpty()) {
            Toast.makeText(this, "Please enter an ingredient and quantity", Toast.LENGTH_SHORT).show();
            return;
        }

        String unit = unitSpinner.getSelectedItem().toString();
        double quantity;
        try {
            quantity = Double.parseDouble(quantityStr);
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Invalid quantity entered", Toast.LENGTH_SHORT).show();
            return;
        }

        Cursor cursor = database.rawQuery(
                "SELECT SodiumPerUnit, Unit FROM SodiumTable WHERE Ingredient = ? COLLATE NOCASE",
                new String[]{name}
        );

        if (cursor.moveToFirst()) {
            double sodiumPerUnit = cursor.getDouble(0);
            String dbUnit = cursor.getString(1);
            cursor.close();

            double conversionFactor = getConversionFactor(unit, dbUnit);
            double sodiumAmount = quantity * conversionFactor * sodiumPerUnit;

            addIngredientToList(name, quantity, unit, sodiumAmount);
        } else {
            cursor.close();
            promptForSodiumInput(name, quantity, unit);
        }
    }

    private void promptForSodiumInput(String name, double quantity, String unit) {
        EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
        input.setHint("Enter sodium content in mg");

        new AlertDialog.Builder(this)
                .setTitle("Ingredient Not Found")
                .setMessage("Enter sodium content per " + unit + " for " + name + ":")
                .setView(input)
                .setPositiveButton("Add", (dialog, which) -> {
                    try {
                        double sodiumPerUnit = Double.parseDouble(input.getText().toString());
                        double sodiumAmount = quantity * sodiumPerUnit;
                        addIngredientToList(name, quantity, unit, sodiumAmount);
                    } catch (NumberFormatException e) {
                        Toast.makeText(this, "Invalid sodium input. Ingredient not added.", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void addIngredientToList(String name, double quantity, String unit, double sodiumAmount) {
        sodiumContent.setText("Sodium: " + sodiumAmount + " mg");

        if (sodiumAmount == 0) {
            sodiumContent.setTextColor(Color.RED);
            Toast.makeText(this, "Warning: Sodium is 0 mg. Check entry.", Toast.LENGTH_LONG).show();
        } else {
            sodiumContent.setTextColor(Color.BLACK);
        }

        totalSodium += sodiumAmount;
        totalSodiumText.setText("Total Sodium: " + totalSodium + " mg");

        ingredientList.add(name + " - " + quantity + " " + unit + " : " + sodiumAmount + " mg");
        adapter.notifyDataSetChanged();
        updateSodiumWarning();
    }
    private double getConversionFactor(String userUnit, String dbUnit) {
        if (userUnit.equals(dbUnit)) return 1.0;

        // Convert user input to tablespoon equivalents
        double userToTbsp = 1.0;
        switch (userUnit) {
            case "cup": userToTbsp = 16.0; break;
            case "tablespoon": userToTbsp = 1.0; break;
            case "teaspoon": userToTbsp = 1.0 / 3.0; break;
            case "gram": userToTbsp = 1.0 / 15.0; break; // approximate
        }

        // Convert database unit to tablespoon equivalents
        double dbToTbsp = 1.0;
        switch (dbUnit) {
            case "cup": dbToTbsp = 16.0; break;
            case "tablespoon": dbToTbsp = 1.0; break;
            case "teaspoon": dbToTbsp = 1.0 / 3.0; break;
            case "gram": dbToTbsp = 1.0 / 15.0; break; // approximate
        }

        // Return conversion factor to adjust from userUnit to dbUnit
        return userToTbsp / dbToTbsp;
    }

    private void clearIngredientFields() {
        ingredientName.setText("");
        ingredientQuantity.setText("");
        unitSpinner.setSelection(0);
        sodiumContent.setText("");
        totalSodium = 0;
        totalSodiumText.setText("Total Sodium: 0 mg");
        sodiumWarning.setVisibility(View.GONE);
        totalSodiumText.setTextColor(Color.BLACK);
        ingredientList.clear();
        adapter.notifyDataSetChanged();
    }

    private void promptSaveRecipe() {
        if (ingredientList.isEmpty()) {
            Toast.makeText(this, "No ingredients to save.", Toast.LENGTH_SHORT).show();
            return;
        }

        EditText input = new EditText(this);
        input.setHint("e.g., Pancit Canton");

        new AlertDialog.Builder(this)
                .setTitle("Save Recipe")
                .setMessage("Name your collection:")
                .setView(input)
                .setPositiveButton("Save", (dialog, which) -> {
                    String recipeName = input.getText().toString().trim();
                    if (!recipeName.isEmpty()) {
                        saveRecipeToDatabase(recipeName);
                    } else {
                        Toast.makeText(this, "Recipe name cannot be empty", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void saveRecipeToDatabase(String recipeName) {
        Cursor checkCursor = database.rawQuery("SELECT RecipeID FROM Recipe WHERE Name = ?", new String[]{recipeName});
        long recipeId;

        if (checkCursor.moveToFirst()) {
            recipeId = checkCursor.getLong(0);
            database.delete("RecipeIngredient", "RecipeID = ?", new String[]{String.valueOf(recipeId)});
        } else {
            ContentValues recipeValues = new ContentValues();
            recipeValues.put("Name", recipeName);
            recipeId = database.insert("Recipe", null, recipeValues);
        }
        checkCursor.close();

        database.beginTransaction();
        try {
            for (String item : ingredientList) {
                String[] parts = item.split(" - | : ");
                if (parts.length == 3) {
                    String name = parts[0].trim();
                    String[] qtyAndUnit = parts[1].trim().split(" ");
                    double quantity = Double.parseDouble(qtyAndUnit[0]);
                    String unit = qtyAndUnit[1].trim();

                    double sodiumAmount = 0;
                    boolean foundInDb = false;

                    Cursor sodiumCursor = database.rawQuery(
                            "SELECT SodiumPerUnit, Unit FROM SodiumTable WHERE LOWER(Ingredient) = ?",
                            new String[]{name.toLowerCase()}
                    );

                    if (sodiumCursor.moveToFirst()) {
                        foundInDb = true;
                        double sodiumPerUnit = sodiumCursor.getDouble(0);
                        String dbUnit = sodiumCursor.getString(1);
                        double conversionFactor = getConversionFactor(unit, dbUnit);
                        sodiumAmount = quantity * conversionFactor * sodiumPerUnit;
                    }
                    sodiumCursor.close();

                    if (!foundInDb) {
                        // Ask user for sodium if not found (for now, just skip or default to 0)
                        // Optional: You can show a dialog to manually enter sodium
                        Toast.makeText(this, "Sodium data for \"" + name + "\" not found. Using 0 mg.", Toast.LENGTH_SHORT).show();
                    }

                    // Debug log (optional)
                    android.util.Log.d("SAVE_RECIPE", name + " | Qty: " + quantity + " " + unit + " | Sodium: " + sodiumAmount);

                    ContentValues ingredientValues = new ContentValues();
                    ingredientValues.put("RecipeID", recipeId);
                    ingredientValues.put("Ingredient", name);
                    ingredientValues.put("Quantity", quantity);
                    ingredientValues.put("Unit", unit);
                    ingredientValues.put("SodiumAmount", sodiumAmount);

                    database.insert("RecipeIngredient", null, ingredientValues);
                }
            }

            database.setTransactionSuccessful();
            Toast.makeText(this, "Recipe saved!", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Toast.makeText(this, "Error saving recipe: " + e.getMessage(), Toast.LENGTH_LONG).show();
        } finally {
            database.endTransaction();
        }
    }

    private void updateSodiumWarning() {
        if (totalSodium > 2300) {
            totalSodiumText.setTextColor(Color.RED);
            sodiumWarning.setVisibility(View.VISIBLE);
        } else {
            totalSodiumText.setTextColor(Color.BLACK);
            sodiumWarning.setVisibility(View.GONE);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 100 && resultCode == RESULT_OK && data != null) {
            int recipeId = data.getIntExtra("recipeId", -1);
            if (recipeId != -1) {
                loadRecipeById(recipeId);
            }
        }
    }

    private void loadRecipeById(int recipeId) {
        ingredientList.clear();
        totalSodium = 0;

        Cursor cursor = database.rawQuery(
                "SELECT Ingredient, Quantity, Unit, SodiumAmount FROM RecipeIngredient WHERE RecipeID = ?",
                new String[]{String.valueOf(recipeId)}
        );

        while (cursor.moveToNext()) {
            String name = cursor.getString(0);
            double qty = cursor.getDouble(1);
            String unit = cursor.getString(2);
            double sodium = cursor.getDouble(3);

            totalSodium += sodium;
            ingredientList.add(name + " - " + qty + " " + unit + " : " + sodium + " mg");
        }

        cursor.close();

        adapter.notifyDataSetChanged();
        totalSodiumText.setText("Total Sodium: " + totalSodium + " mg");
        updateSodiumWarning();

        // Clear inputs
        ingredientName.setText("");
        ingredientQuantity.setText("");
        unitSpinner.setSelection(0);
        sodiumContent.setText("");
    }

    @Override
    protected void onDestroy() {
        if (database != null && database.isOpen()) {
            database.close();
        }
        super.onDestroy();
    }
    @Override
    protected void onResume() {
        super.onResume();
        boolean isYellow = prefs.getBoolean("paleYellow", false);
        layout.setBackgroundColor(getResources().getColor(
                isYellow ? R.color.pale_yellow : android.R.color.white
        ));
    }
}


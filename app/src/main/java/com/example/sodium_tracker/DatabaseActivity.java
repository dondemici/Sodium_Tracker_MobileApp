package com.example.sodium_tracker;

import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.preference.PreferenceManager;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.ArrayList;

public class DatabaseActivity extends AppCompatActivity implements RecipeAdapter.OnItemClickListener {

    private ConstraintLayout layout;
    private RecyclerView recyclerView;
    private RecipeAdapter adapter;
    private ArrayList<Recipe> recipeList;
    private SQLiteDatabase database;
    private SharedPreferences prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_database);

        layout = findViewById(R.id.databaseLayout);
        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        prefs = PreferenceManager.getDefaultSharedPreferences(this);
        applyThemePreference();

        DatabaseHelper dbHelper = new DatabaseHelper(this);
        database = dbHelper.getWritableDatabase();

        recipeList = new ArrayList<>();
        adapter = new RecipeAdapter(recipeList, this);
        recyclerView.setAdapter(adapter);

        loadRecipes();
        setupSwipeToDelete();
        setupBottomNav();
    }

    private void applyThemePreference() {
        boolean isYellow = prefs.getBoolean("paleYellow", false);
        layout.setBackgroundColor(getResources().getColor(
                isYellow ? R.color.pale_yellow : android.R.color.white
        ));
    }

    private void loadRecipes() {
        recipeList.clear();

        Cursor recipeCursor = database.rawQuery("SELECT RecipeID, Name FROM Recipe", null);
        while (recipeCursor.moveToNext()) {
            int recipeId = recipeCursor.getInt(0);
            String recipeName = recipeCursor.getString(1);

            Cursor detailCursor = database.rawQuery(
                    "SELECT COUNT(*), SUM(SodiumAmount) FROM RecipeIngredient WHERE RecipeID = ?",
                    new String[]{String.valueOf(recipeId)}
            );

            int ingredientCount = 0;
            double totalSodium = 0;
            if (detailCursor.moveToFirst()) {
                ingredientCount = detailCursor.getInt(0);
                totalSodium = detailCursor.getDouble(1);
            }
            detailCursor.close();

            recipeList.add(new Recipe(recipeId, recipeName, ingredientCount, totalSodium));
        }

        recipeCursor.close();
        adapter.notifyDataSetChanged();
    }

    private void setupSwipeToDelete() {
        ItemTouchHelper.SimpleCallback swipeCallback = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.RIGHT) {
            @Override
            public boolean onMove(@NonNull RecyclerView rv, @NonNull RecyclerView.ViewHolder vh, @NonNull RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                int position = viewHolder.getAdapterPosition();
                Recipe recipe = recipeList.get(position);
                deleteRecipe(recipe.getId());
                recipeList.remove(position);
                adapter.notifyItemRemoved(position);
            }
        };

        new ItemTouchHelper(swipeCallback).attachToRecyclerView(recyclerView);
    }

    private void deleteRecipe(int recipeId) {
        database.delete("RecipeIngredient", "RecipeID = ?", new String[]{String.valueOf(recipeId)});
        database.delete("Recipe", "RecipeID = ?", new String[]{String.valueOf(recipeId)});
        Toast.makeText(this, "Recipe deleted", Toast.LENGTH_SHORT).show();
    }

    private void setupBottomNav() {
        BottomNavigationView bottomNav = findViewById(R.id.bottomNavigationView);
        bottomNav.setSelectedItemId(R.id.nav_database);

        bottomNav.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.nav_menu) {
                startActivity(new Intent(this, MainActivity.class));
                finish();
                return true;
            } else if (itemId == R.id.nav_daily_tracker) {
                startActivity(new Intent(this, DailyTrackerActivity.class));
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

    @Override
    public void onItemClick(Recipe recipe) {
        Intent resultIntent = new Intent();
        resultIntent.putExtra("recipeId", recipe.getId());
        setResult(RESULT_OK, resultIntent);
        finish();
    }

    @Override
    protected void onResume() {
        super.onResume();
        applyThemePreference();
    }

    @Override
    protected void onDestroy() {
        if (database != null && database.isOpen()) {
            database.close();
        }
        super.onDestroy();
    }
}

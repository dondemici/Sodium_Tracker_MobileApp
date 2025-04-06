package com.example.sodium_tracker;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "SodiumDB";
    private static final int DATABASE_VERSION = 1;

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // Sodium ingredient base
        db.execSQL("CREATE TABLE IF NOT EXISTS SodiumTable (" +
                "Ingredient TEXT PRIMARY KEY, " +
                "SodiumPerUnit REAL, " +
                "Unit TEXT)");

        // Recipe names - updated with ServingCount
        db.execSQL("CREATE TABLE IF NOT EXISTS Recipe (" +
                "RecipeID INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "Name TEXT UNIQUE, " +
                "ServingCount INTEGER DEFAULT 1)");

        // Recipe ingredients
        db.execSQL("CREATE TABLE IF NOT EXISTS RecipeIngredient (" +
                "RecipeID INTEGER, " +
                "Ingredient TEXT, " +
                "Quantity REAL, " +
                "Unit TEXT, " +
                "SodiumAmount REAL)");

        // Daily log
        db.execSQL("CREATE TABLE IF NOT EXISTS DailyLog (" +
                "LogID INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "LogDate TEXT, " +
                "RecipeName TEXT, " +
                "MealType TEXT, " +
                "SodiumAmount REAL)");

        insertInitialSodiumData(db);
    }


    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS SodiumTable");
        db.execSQL("DROP TABLE IF EXISTS Recipe");
        db.execSQL("DROP TABLE IF EXISTS RecipeIngredient");
        db.execSQL("DROP TABLE IF EXISTS DailyLog");
        onCreate(db);
    }

    private void insertInitialSodiumData(SQLiteDatabase db) {
        insertIngredient(db, "Soy Sauce", 900, "tablespoon");
        insertIngredient(db, "Salt", 2325, "tablespoon");
        insertIngredient(db, "Pho Powder", 2010, "tablespoon");
        insertIngredient(db, "Dashi", 1770, "tablespoon");
        insertIngredient(db, "Fish Sauce", 1230, "tablespoon");
        insertIngredient(db, "Char Siu Paste", 965, "tablespoon");
        insertIngredient(db, "Hainanese Paste", 955, "tablespoon");
        insertIngredient(db, "Tom Yum Paste", 940, "tablespoon");
        insertIngredient(db, "Red Curry Paste", 855, "tablespoon");
        insertIngredient(db, "Oyster Sauce", 820, "tablespoon");
        insertIngredient(db, "Hoisin Sauce", 545, "tablespoon");
        insertIngredient(db, "Rice Vinegar", 530, "tablespoon");
        insertIngredient(db, "Gochugang", 430, "tablespoon");
        insertIngredient(db, "Capers", 345, "tablespoon");
        insertIngredient(db, "Sriracha", 210, "tablespoon");
        insertIngredient(db, "Furikake", 200, "tablespoon");
        insertIngredient(db, "Worcestershire Sauce", 195, "tablespoon");
        insertIngredient(db, "Sake", 150, "tablespoon");
        insertIngredient(db, "Shaoxing Wine", 75, "tablespoon");
        insertIngredient(db, "Ricotta Cheese", 12, "tablespoon");
    }

    private void insertIngredient(SQLiteDatabase db, String name, double sodium, String unit) {
        ContentValues values = new ContentValues();
        values.put("Ingredient", name);
        values.put("SodiumPerUnit", sodium);
        values.put("Unit", unit);
        db.insertWithOnConflict("SodiumTable", null, values, SQLiteDatabase.CONFLICT_IGNORE);
    }
}


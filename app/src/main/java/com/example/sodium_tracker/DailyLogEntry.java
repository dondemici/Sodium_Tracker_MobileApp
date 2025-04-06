package com.example.sodium_tracker;

public class DailyLogEntry {
    private int id;              // <-- This is the 1st argument
    private String date;         // 2nd
    private String recipeName;   // 3rd
    private String mealType;     // 4th
    private double sodium;       // 5th

    public DailyLogEntry(int id, String date, String recipeName, String mealType, double sodium) {
        this.id = id;
        this.date = date;
        this.recipeName = recipeName;
        this.mealType = mealType;
        this.sodium = sodium;
    }
    public int getId() { return id; }
    public String getDate() { return date; }
    public String getRecipeName() { return recipeName; }
    public String getMealType() { return mealType; }
    public double getSodium() { return sodium; }

    public void updateMealType(String newMealType) {
        this.mealType = newMealType;
    }
}

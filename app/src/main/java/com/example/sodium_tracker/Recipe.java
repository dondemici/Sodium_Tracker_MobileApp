package com.example.sodium_tracker;

public class Recipe {
    private int id;
    private String name;
    private int ingredientCount;
    private double totalSodium;

    public Recipe(int id, String name, int ingredientCount, double totalSodium) {
        this.id = id;
        this.name = name;
        this.ingredientCount = ingredientCount;
        this.totalSodium = totalSodium;
    }

    public int getId() { return id; }
    public String getName() { return name; }
    public int getIngredientCount() { return ingredientCount; }
    public double getTotalSodium() { return totalSodium; }
}

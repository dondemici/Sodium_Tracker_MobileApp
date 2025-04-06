package com.example.sodium_tracker;

public class Recipe {
    private int id;
    private String name;
    private int ingredientCount;
    private double totalSodium;
    private int servingCount;

    public Recipe(int id, String name, int ingredientCount, double totalSodium, int servingCount) {
        this.id = id;
        this.name = name;
        this.ingredientCount = ingredientCount;
        this.totalSodium = totalSodium;
        this.servingCount = servingCount;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public int getIngredientCount() {
        return ingredientCount;
    }

    public double getTotalSodium() {
        return totalSodium;
    }

    public int getServingCount() {
        return servingCount;
    }

    public double getSodiumPerServing() {
        return servingCount > 0 ? totalSodium / servingCount : totalSodium;
    }
}

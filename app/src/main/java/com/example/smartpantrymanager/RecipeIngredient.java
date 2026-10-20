package com.example.smartpantrymanager;

/**
 * One ingredient line a recipe needs (row in "recipe_ingredient").
 * ingredientName is stored lowercase and singular so matching against
 * the pantry stays simple (e.g. "tomato", not "Tomatoes").
 */
public class RecipeIngredient {
    private long id;
    private long recipeId;
    private String ingredientName;
    private double requiredQty;
    private String unit;

    public RecipeIngredient(long id, long recipeId, String ingredientName, double requiredQty, String unit) {
        this.id = id;
        this.recipeId = recipeId;
        this.ingredientName = ingredientName;
        this.requiredQty = requiredQty;
        this.unit = unit;
    }

    public long getId() { return id; }
    public long getRecipeId() { return recipeId; }
    public String getIngredientName() { return ingredientName; }
    public double getRequiredQty() { return requiredQty; }
    public String getUnit() { return unit; }
}

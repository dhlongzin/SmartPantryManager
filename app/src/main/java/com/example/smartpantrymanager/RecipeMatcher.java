package com.example.smartpantrymanager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class RecipeMatcher {

    private static final double EPSILON = 0.0001;

    public static List<Recipe> getSuggestedRecipes(List<Ingredient> pantry, List<Recipe> allRecipes) {
        Map<String, List<Ingredient>> pantryByName = groupPantryByName(pantry);
        List<Recipe> suggested = new ArrayList<>();

        for (Recipe recipe : allRecipes) {
            if (canMake(recipe, pantryByName)) {
                suggested.add(recipe);
            }
        }
        return suggested;
    }

    //True only if every required ingredient is available in enough quantity
    private static boolean canMake(Recipe recipe, Map<String, List<Ingredient>> pantryByName) {
        if (recipe.getIngredients().isEmpty()) {
            return false;
        }
        for (RecipeIngredient needed : recipe.getIngredients()) {
            if (!hasEnough(needed, pantryByName)) {
                return false;
            }
        }
        return true;
    }

    //Checks required ingredient against the pantry
    private static boolean hasEnough(RecipeIngredient needed, Map<String, List<Ingredient>> pantryByName) {
        List<Ingredient> matches = pantryByName.get(normalize(needed.getIngredientName()));
        if (matches == null) {
            return false; // ingredient is not in the pantry at all
        }

        String group = unitGroup(needed.getUnit());
        double required = toBaseUnit(needed.getRequiredQty(), needed.getUnit());
        double available = 0;

        for (Ingredient item : matches) {
            // Only count pantry entries measured in a compatible unit (e.g. ml with tbsp)
            if (unitGroup(item.getUnit()).equals(group)) {
                available += toBaseUnit(item.getQuantity(), item.getUnit());
            }
        }
        return available + EPSILON >= required;
    }

    private static Map<String, List<Ingredient>> groupPantryByName(List<Ingredient> pantry) {
        Map<String, List<Ingredient>> map = new HashMap<>();
        for (Ingredient item : pantry) {
            String key = normalize(item.getName());
            if (!map.containsKey(key)) {
                map.put(key, new ArrayList<>());
            }
            map.get(key).add(item);
        }
        return map;
    }

    //normalisation
    static String normalize(String name) {
        if (name == null) return "";
        String s = name.trim().toLowerCase(Locale.ROOT).replaceAll("[\\s\\-]+", " ");
        int lastSpace = s.lastIndexOf(' ');
        String prefix = (lastSpace == -1) ? "" : s.substring(0, lastSpace + 1);
        String lastWord = s.substring(lastSpace + 1);
        return prefix + toSingular(lastWord);
    }

    private static String toSingular(String w) {
        if (w.length() <= 3) return w;
        if (w.endsWith("ies")) return w.substring(0, w.length() - 3) + "y";
        if (w.endsWith("oes") || w.endsWith("ches") || w.endsWith("shes")
                || w.endsWith("xes") || w.endsWith("sses")) {
            return w.substring(0, w.length() - 2);
        }
        if (w.endsWith("ss")) return w;
        if (w.endsWith("s")) return w.substring(0, w.length() - 1);
        return w;
    }

    // Unit handling

    private static String unitGroup(String unit) {
        String u = unit == null ? "" : unit.trim().toLowerCase(Locale.ROOT);
        switch (u) {
            case "g":
            case "kg":
                return "mass";
            case "ml":
            case "l":
            case "tsp":
            case "tbsp":
                return "volume";
            case "pcs":
            case "piece":
            case "pieces":
                return "count";
            default:
                return "other:" + u;
        }
    }

    //Converts a quantity to the unit
    private static double toBaseUnit(double qty, String unit) {
        String u = unit == null ? "" : unit.trim().toLowerCase(Locale.ROOT);
        switch (u) {
            case "kg":
            case "l":
                return qty * 1000;
            case "tsp":
                return qty * 5;
            case "tbsp":
                return qty * 15;
            default:
                return qty; // g, ml, pcs and unknown units stay as they are
        }
    }
}
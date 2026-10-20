package com.example.smartpantrymanager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Core business logic of the app: the strict-matching rule.
 *
 * A recipe is only "suggested" if EVERY ingredient it needs is in the pantry
 * in at least the required quantity. If even one ingredient is missing or
 * short, the whole recipe is excluded (no partial or "almost" matches).
 *
 * To cope with simple real-world messiness the matcher:
 *  - normalises names (case, extra spaces, simple plurals: tomatoes -> tomato)
 *  - converts compatible units (tbsp/tsp/ml) before comparing quantities
 *  - adds up quantities when the pantry holds the same item more than once
 */
public class RecipeMatcher {

    // Small tolerance so decimal rounding never rejects an exact match
    private static final double EPSILON = 0.0001;

    /** Returns only the recipes the user can make with what is in the pantry right now. */
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

    /** True only if every required ingredient is available in enough quantity. */
    private static boolean canMake(Recipe recipe, Map<String, List<Ingredient>> pantryByName) {
        if (recipe.getIngredients().isEmpty()) {
            return false; // a recipe with no ingredients should never be suggested
        }
        for (RecipeIngredient needed : recipe.getIngredients()) {
            if (!hasEnough(needed, pantryByName)) {
                return false; // one missing or short ingredient excludes the whole recipe
            }
        }
        return true;
    }

    /** Checks one required ingredient against the pantry. */
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

    /** Groups pantry items by their normalised name for quick lookup. */
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

    // ---------------------------------------------------------------
    // Name normalisation
    // ---------------------------------------------------------------

    /**
     * Lowercases, trims, collapses spaces/hyphens and makes the last word
     * singular. The SAME method is applied to pantry names and recipe names,
     * so both sides always end up in the same form.
     */
    static String normalize(String name) {
        if (name == null) return "";
        String s = name.trim().toLowerCase(Locale.ROOT).replaceAll("[\\s\\-]+", " ");
        int lastSpace = s.lastIndexOf(' ');
        String prefix = (lastSpace == -1) ? "" : s.substring(0, lastSpace + 1);
        String lastWord = s.substring(lastSpace + 1);
        return prefix + toSingular(lastWord);
    }

    /** Simple plural handling - not full NLP, but covers the common cases. */
    private static String toSingular(String w) {
        if (w.length() <= 3) return w;
        if (w.endsWith("ies")) return w.substring(0, w.length() - 3) + "y";   // berries -> berry
        if (w.endsWith("oes") || w.endsWith("ches") || w.endsWith("shes")
                || w.endsWith("xes") || w.endsWith("sses")) {
            return w.substring(0, w.length() - 2);                            // tomatoes -> tomato
        }
        if (w.endsWith("ss")) return w;                                       // keep "glass"
        if (w.endsWith("s")) return w.substring(0, w.length() - 1);           // eggs -> egg
        return w;
    }

    // ---------------------------------------------------------------
    // Unit handling
    // ---------------------------------------------------------------

    /** Units can only be compared if they measure the same kind of thing. */
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
                return "other:" + u; // unknown units only match themselves
        }
    }

    /** Converts a quantity to the base unit of its group (g, ml or pcs). */
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
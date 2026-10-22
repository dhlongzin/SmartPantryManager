package com.example.smartpantrymanager;


import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.List;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DB_NAME = "smart_pantry.db";
    private static final int DB_VERSION = 1;

    // Table: ingredient (the user's pantry)
    public static final String TABLE_INGREDIENT = "ingredient";
    public static final String COL_ING_ID = "id";
    public static final String COL_ING_NAME = "name";
    public static final String COL_ING_QTY = "quantity";
    public static final String COL_ING_UNIT = "unit";
    public static final String COL_ING_EXPIRY = "expiry_date";

    // Table: recipe
    public static final String TABLE_RECIPE = "recipe";
    public static final String COL_REC_ID = "id";
    public static final String COL_REC_NAME = "name";
    public static final String COL_REC_STEPS = "steps";

    // Table: recipe_ingredient (what each recipe needs)
    public static final String TABLE_RECIPE_ING = "recipe_ingredient";
    public static final String COL_RI_ID = "id";
    public static final String COL_RI_RECIPE_ID = "recipe_id";
    public static final String COL_RI_NAME = "ingredient_name";
    public static final String COL_RI_QTY = "required_qty";
    public static final String COL_RI_UNIT = "unit";

    public DatabaseHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onConfigure(SQLiteDatabase db) {
        super.onConfigure(db);
        db.setForeignKeyConstraintsEnabled(true); // enforce recipe_id foreign key
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE " + TABLE_INGREDIENT + " ("
                + COL_ING_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + COL_ING_NAME + " TEXT NOT NULL, "
                + COL_ING_QTY + " REAL NOT NULL, "
                + COL_ING_UNIT + " TEXT NOT NULL, "
                + COL_ING_EXPIRY + " TEXT)");

        db.execSQL("CREATE TABLE " + TABLE_RECIPE + " ("
                + COL_REC_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + COL_REC_NAME + " TEXT NOT NULL, "
                + COL_REC_STEPS + " TEXT NOT NULL)");

        db.execSQL("CREATE TABLE " + TABLE_RECIPE_ING + " ("
                + COL_RI_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + COL_RI_RECIPE_ID + " INTEGER NOT NULL, "
                + COL_RI_NAME + " TEXT NOT NULL, "
                + COL_RI_QTY + " REAL NOT NULL, "
                + COL_RI_UNIT + " TEXT NOT NULL, "
                + "FOREIGN KEY(" + COL_RI_RECIPE_ID + ") REFERENCES "
                + TABLE_RECIPE + "(" + COL_REC_ID + ") ON DELETE CASCADE)");

        seedRecipes(db); // pre-load recipes on first run only
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_RECIPE_ING);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_RECIPE);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_INGREDIENT);
        onCreate(db);
    }

    // Seed data

    private static RecipeIngredient ri(String name, double qty, String unit) {
        return new RecipeIngredient(0, 0, name, qty, unit);
    }

    private void addRecipe(SQLiteDatabase db, String name, String steps, RecipeIngredient... items) {
        ContentValues recipeValues = new ContentValues();
        recipeValues.put(COL_REC_NAME, name);
        recipeValues.put(COL_REC_STEPS, steps);
        long recipeId = db.insert(TABLE_RECIPE, null, recipeValues);

        for (RecipeIngredient item : items) {
            ContentValues v = new ContentValues();
            v.put(COL_RI_RECIPE_ID, recipeId);
            v.put(COL_RI_NAME, item.getIngredientName());
            v.put(COL_RI_QTY, item.getRequiredQty());
            v.put(COL_RI_UNIT, item.getUnit());
            db.insert(TABLE_RECIPE_ING, null, v);
        }
    }

    private void seedRecipes(SQLiteDatabase db) {
        addRecipe(db, "Pap and Tomato Relish",
                "1. Cook the pap until thick.\n2. Fry chopped onion in oil, then add chopped tomatoes.\n3. Simmer the relish for 10 minutes and serve with the pap.",
                ri("maize meal", 250, "g"), ri("tomato", 2, "pcs"),
                ri("onion", 1, "pcs"), ri("cooking oil", 1, "tbsp"));

        addRecipe(db, "Phutu and Amasi",
                "1. Cook the pap.\n2. Simmer for 10 minutes and pour amasi of your choice.",
                ri("maize meal", 200, "g"), ri("amasi", 250, "ml"));

        addRecipe(db, "Vetkoek",
                "1. Mix the flour, yeast and sugar with warm water into a soft dough.\n2. Cover and leave to rise for 1 hour.\n3. Shape into balls and deep-fry in hot oil until golden brown.",
                ri("flour", 300, "g"), ri("yeast", 10, "g"),
                ri("sugar", 1, "tbsp"), ri("cooking oil", 250, "ml"));


        addRecipe(db, "Chakalaka",
                "1. Fry chopped onion in oil.\n2. Add grated carrot and curry powder and cook for 5 minutes.\n3. Add chopped tomatoes and simmer for 10 minutes.",
                ri("carrot", 2, "pcs"), ri("onion", 1, "pcs"), ri("tomato", 2, "pcs"),
                ri("curry powder", 2, "tsp"), ri("cooking oil", 1, "tbsp"));

        addRecipe(db, "Morogo (Spinach) Stew",
                "1. Wash and chop the spinach.\n2. Fry the onion in oil, add the tomato and cook until soft.\n3. Add the spinach and simmer for 10 minutes.",
                ri("spinach", 300, "g"), ri("onion", 1, "pcs"),
                ri("tomato", 1, "pcs"), ri("cooking oil", 1, "tbsp"));

        addRecipe(db, "Boerewors and Pap",
                "1. Cook the pap.\n2. Fry or grill the boerewors until cooked through.\n3. Fry sliced onion and serve everything together.",
                ri("boerewors", 400, "g"), ri("maize meal", 250, "g"), ri("onion", 1, "pcs"));

        addRecipe(db, "Maize Meal Porridge",
                "1. Heat the milk until it starts to simmer.\n2. Slowly stir in the maize meal.\n3. Cook on low heat for 10 minutes, stirring often.\n4. Add sugar and serve.",
                ri("maize meal", 100, "g"), ri("milk", 250, "ml"), ri("sugar", 1, "tbsp"));

        addRecipe(db, "Chicken Stew",
                "1. Brown the chicken pieces in oil.\n2. Add chopped onion and tomatoes and cook until soft.\n3. Add the potatoes and a cup of water.\n4. Simmer for 40 minutes until the chicken is tender.",
                ri("chicken", 400, "g"), ri("potato", 2, "pcs"), ri("onion", 1, "pcs"),
                ri("tomato", 2, "pcs"), ri("cooking oil", 1, "tbsp"));

        addRecipe(db, "Beef Stew",
                "1. Brown the beef cubes in oil.\n2. Add chopped onion and cook for 5 minutes.\n3. Add the carrots, potatoes and enough water to cover.\n4. Simmer for 1 hour 30 minutes until the meat is soft.",
                ri("beef", 400, "g"), ri("potato", 2, "pcs"), ri("carrot", 2, "pcs"),
                ri("onion", 1, "pcs"), ri("cooking oil", 1, "tbsp"));

        addRecipe(db, "Chicken Curry and Rice",
                "1. Cook the rice.\n2. Fry the onion in oil, add the curry powder and chicken.\n3. Add chopped tomato and a little water.\n4. Simmer for 30 minutes and serve over the rice.",
                ri("chicken", 400, "g"), ri("rice", 200, "g"), ri("onion", 1, "pcs"),
                ri("tomato", 1, "pcs"), ri("curry powder", 2, "tsp"));

        addRecipe(db, "Cabbage and Potato Stew",
                "1. Fry the onion in oil.\n2. Add the diced potatoes and a little water and cook for 10 minutes.\n3. Add the chopped cabbage and cook until soft.",
                ri("cabbage", 400, "g"), ri("potato", 2, "pcs"), ri("onion", 1, "pcs"),
                ri("cooking oil", 1, "tbsp"));

        addRecipe(db, "Pilchards and Pap",
                "1. Cook the pap until thick.\n2. Fry the onion and tomato until soft.\n3. Add the pilchards and heat through.\n4. Serve with the pap.",
                ri("pilchard", 1, "pcs"), ri("maize meal", 250, "g"), ri("onion", 1, "pcs"),
                ri("tomato", 1, "pcs"));

        addRecipe(db, "Morogo with Pap",
                "1. Cook the pap until thick.\n2. Fry the onion in oil, add the chopped spinach and cook for 10 minutes.\n3. Serve the morogo with the pap.",
                ri("spinach", 300, "g"), ri("maize meal", 250, "g"), ri("onion", 1, "pcs"),
                ri("cooking oil", 1, "tbsp"));

        addRecipe(db, "Tomato Gravy and Bread",
                "1. Fry the chopped onion in oil.\n2. Add the chopped tomatoes and simmer for 15 minutes until thick.\n3. Serve the gravy with the bread.",
                ri("tomato", 3, "pcs"), ri("onion", 1, "pcs"), ri("cooking oil", 1, "tbsp"),
                ri("bread", 4, "pcs"));

        addRecipe(db, "Slap Chips",
                "1. Peel the potatoes and cut them into thick strips.\n2. Fry in hot oil until soft and golden.\n3. Drain and sprinkle with vinegar.",
                ri("potato", 4, "pcs"), ri("cooking oil", 300, "ml"), ri("vinegar", 1, "tbsp"));

        addRecipe(db, "Mealie Bread",
                "1. Mix the flour, baking powder and sugar.\n2. Stir in the sweetcorn, eggs, milk and melted butter.\n3. Pour into a greased tin.\n4. Bake at 180C for 45 minutes.",
                ri("flour", 250, "g"), ri("sweetcorn", 200, "g"), ri("egg", 2, "pcs"),
                ri("milk", 250, "ml"), ri("sugar", 2, "tbsp"), ri("butter", 50, "g"),
                ri("baking powder", 2, "tsp"));

        addRecipe(db, "Pannekoek with Cinnamon Sugar",
                "1. Whisk the flour, eggs and milk into a thin batter.\n2. Fry a thin layer in a hot pan until golden on both sides.\n3. Sprinkle with sugar and cinnamon and roll up.",
                ri("flour", 150, "g"), ri("egg", 2, "pcs"), ri("milk", 300, "ml"),
                ri("sugar", 2, "tbsp"), ri("cinnamon", 1, "tsp"));

        addRecipe(db, "Pumpkin Fritters",
                "1. Boil and mash the pumpkin.\n2. Mix in the flour, egg and sugar to make a thick batter.\n3. Fry spoonfuls in hot oil until golden.\n4. Roll in cinnamon.",
                ri("pumpkin", 300, "g"), ri("flour", 100, "g"), ri("egg", 1, "pcs"),
                ri("sugar", 1, "tbsp"), ri("cooking oil", 100, "ml"), ri("cinnamon", 1, "tsp"));

        addRecipe(db, "Baked Sweet Potato",
                "1. Wash the sweet potatoes and prick them with a fork.\n2. Bake at 200C for 45 minutes until soft.\n3. Cut open and add butter.",
                ri("sweet potato", 2, "pcs"), ri("butter", 20, "g"));

        addRecipe(db, "Peanut Butter Sandwich",
                "1. Butter both slices of bread.\n2. Spread peanut butter on one slice.\n3. Close the sandwich and cut in half.",
                ri("bread", 2, "pcs"), ri("peanut butter", 2, "tbsp"), ri("butter", 10, "g"));
    }


    public int getRecipeCount() {
        SQLiteDatabase db = getReadableDatabase();
        Cursor c = db.rawQuery("SELECT COUNT(*) FROM " + TABLE_RECIPE, null);
        int count = 0;
        if (c.moveToFirst()) count = c.getInt(0);
        c.close();
        return count;
    }

    public int getRecipeIngredientCount() {
        SQLiteDatabase db = getReadableDatabase();
        Cursor c = db.rawQuery("SELECT COUNT(*) FROM " + TABLE_RECIPE_ING, null);
        int count = 0;
        if (c.moveToFirst()) count = c.getInt(0);
        c.close();
        return count;
    }



    //Adds a pantry item
    public long insertIngredient(Ingredient ingredient) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues v = new ContentValues();
        v.put(COL_ING_NAME, ingredient.getName().trim().toLowerCase());
        v.put(COL_ING_QTY, ingredient.getQuantity());
        v.put(COL_ING_UNIT, ingredient.getUnit());
        v.put(COL_ING_EXPIRY, ingredient.getExpiryDate());
        return db.insert(TABLE_INGREDIENT, null, v);
    }

    //Returns every pantry item
    public List<Ingredient> getAllIngredients() {
        List<Ingredient> list = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();
        Cursor c = db.query(TABLE_INGREDIENT, null, null, null, null, null, COL_ING_NAME + " ASC");
        while (c.moveToNext()) {
            list.add(new Ingredient(
                    c.getLong(c.getColumnIndexOrThrow(COL_ING_ID)),
                    c.getString(c.getColumnIndexOrThrow(COL_ING_NAME)),
                    c.getDouble(c.getColumnIndexOrThrow(COL_ING_QTY)),
                    c.getString(c.getColumnIndexOrThrow(COL_ING_UNIT)),
                    c.getString(c.getColumnIndexOrThrow(COL_ING_EXPIRY))));
        }
        c.close();
        return list;
    }


    public Ingredient getIngredientById(long id) {
        SQLiteDatabase db = getReadableDatabase();
        Cursor c = db.query(TABLE_INGREDIENT, null, COL_ING_ID + " = ?",
                new String[]{String.valueOf(id)}, null, null, null);
        Ingredient ingredient = null;
        if (c.moveToFirst()) {
            ingredient = new Ingredient(
                    c.getLong(c.getColumnIndexOrThrow(COL_ING_ID)),
                    c.getString(c.getColumnIndexOrThrow(COL_ING_NAME)),
                    c.getDouble(c.getColumnIndexOrThrow(COL_ING_QTY)),
                    c.getString(c.getColumnIndexOrThrow(COL_ING_UNIT)),
                    c.getString(c.getColumnIndexOrThrow(COL_ING_EXPIRY)));
        }
        c.close();
        return ingredient;
    }

    /** Updates an existing pantry item. Returns the number of rows changed. */
    public int updateIngredient(Ingredient ingredient) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues v = new ContentValues();
        v.put(COL_ING_NAME, ingredient.getName().trim().toLowerCase());
        v.put(COL_ING_QTY, ingredient.getQuantity());
        v.put(COL_ING_UNIT, ingredient.getUnit());
        v.put(COL_ING_EXPIRY, ingredient.getExpiryDate());
        return db.update(TABLE_INGREDIENT, v, COL_ING_ID + " = ?",
                new String[]{String.valueOf(ingredient.getId())});
    }

    /** True if another pantry item already has this name and unit (excludeId is skipped). */
    public boolean ingredientExists(String name, String unit, long excludeId) {
        SQLiteDatabase db = getReadableDatabase();
        Cursor c = db.rawQuery("SELECT 1 FROM " + TABLE_INGREDIENT
                        + " WHERE " + COL_ING_NAME + " = ? AND " + COL_ING_UNIT + " = ? AND "
                        + COL_ING_ID + " != ?",
                new String[]{name.trim().toLowerCase(), unit, String.valueOf(excludeId)});
        boolean exists = c.moveToFirst();
        c.close();
        return exists;
    }
    //Deletes one pantry item by id
    public int deleteIngredient(long id) {
        SQLiteDatabase db = getWritableDatabase();
        return db.delete(TABLE_INGREDIENT, COL_ING_ID + " = ?",
                new String[]{String.valueOf(id)});
    }
    public List<Recipe> getAllRecipes() {
        SQLiteDatabase db = getReadableDatabase();
        List<Recipe> recipes = new ArrayList<>();
        Map<Long, Recipe> recipeById = new HashMap<>();

        //Read all recipes
        Cursor c = db.query(TABLE_RECIPE, null, null, null, null, null, COL_REC_NAME + " ASC");
        while (c.moveToNext()) {
            Recipe recipe = new Recipe(
                    c.getLong(c.getColumnIndexOrThrow(COL_REC_ID)),
                    c.getString(c.getColumnIndexOrThrow(COL_REC_NAME)),
                    c.getString(c.getColumnIndexOrThrow(COL_REC_STEPS)));
            recipes.add(recipe);
            recipeById.put(recipe.getId(), recipe);
        }
        c.close();

        // Read all ingredient lines and attach each to its recipe
        Cursor ci = db.query(TABLE_RECIPE_ING, null, null, null, null, null, null);
        while (ci.moveToNext()) {
            RecipeIngredient ri = new RecipeIngredient(
                    ci.getLong(ci.getColumnIndexOrThrow(COL_RI_ID)),
                    ci.getLong(ci.getColumnIndexOrThrow(COL_RI_RECIPE_ID)),
                    ci.getString(ci.getColumnIndexOrThrow(COL_RI_NAME)),
                    ci.getDouble(ci.getColumnIndexOrThrow(COL_RI_QTY)),
                    ci.getString(ci.getColumnIndexOrThrow(COL_RI_UNIT)));
            Recipe owner = recipeById.get(ri.getRecipeId());
            if (owner != null) {
                owner.getIngredients().add(ri);
            }
        }
        ci.close();
        return recipes;
    }
    public Recipe getRecipeById(long id) {
        SQLiteDatabase db = getReadableDatabase();
        Recipe recipe = null;

        Cursor c = db.query(TABLE_RECIPE, null, COL_REC_ID + " = ?",
                new String[]{String.valueOf(id)}, null, null, null);
        if (c.moveToFirst()) {
            recipe = new Recipe(
                    c.getLong(c.getColumnIndexOrThrow(COL_REC_ID)),
                    c.getString(c.getColumnIndexOrThrow(COL_REC_NAME)),
                    c.getString(c.getColumnIndexOrThrow(COL_REC_STEPS)));
        }
        c.close();
        if (recipe == null) return null;

        Cursor ci = db.query(TABLE_RECIPE_ING, null, COL_RI_RECIPE_ID + " = ?",
                new String[]{String.valueOf(id)}, null, null, null);
        while (ci.moveToNext()) {
            recipe.getIngredients().add(new RecipeIngredient(
                    ci.getLong(ci.getColumnIndexOrThrow(COL_RI_ID)),
                    ci.getLong(ci.getColumnIndexOrThrow(COL_RI_RECIPE_ID)),
                    ci.getString(ci.getColumnIndexOrThrow(COL_RI_NAME)),
                    ci.getDouble(ci.getColumnIndexOrThrow(COL_RI_QTY)),
                    ci.getString(ci.getColumnIndexOrThrow(COL_RI_UNIT))));
        }
        ci.close();
        return recipe;
    }
}
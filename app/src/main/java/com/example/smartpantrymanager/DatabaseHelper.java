package com.example.smartpantrymanager;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Creates the SQLite database, defines the three tables and seeds the
 * recipe collection on first run. CRUD methods for pantry items are
 * added in later steps.
 *
 * Units used everywhere: g, ml, pcs, tbsp, tsp (the Add/Edit screen will
 * use a Spinner with these so units always match the recipes).
 */
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

    // ---------------------------------------------------------------
    // Seed data
    // ---------------------------------------------------------------

    /** Shortcut to build one recipe ingredient line (ids are filled by SQLite). */
    private static RecipeIngredient ri(String name, double qty, String unit) {
        return new RecipeIngredient(0, 0, name, qty, unit);
    }

    /** Inserts one recipe and all of its ingredient lines. */
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
        addRecipe(db, "Scrambled Eggs on Toast",
                "1. Toast the bread.\n2. Whisk the eggs and cook in melted butter, stirring gently.\n3. Serve the eggs on the toast.",
                ri("egg", 2, "pcs"), ri("bread", 2, "pcs"), ri("butter", 10, "g"));

        addRecipe(db, "Tomato Pasta",
                "1. Boil the pasta until soft.\n2. Fry chopped onion and garlic in olive oil.\n3. Add chopped tomatoes and simmer for 10 minutes.\n4. Mix in the pasta and serve.",
                ri("pasta", 200, "g"), ri("tomato", 3, "pcs"), ri("onion", 1, "pcs"),
                ri("garlic", 2, "pcs"), ri("olive oil", 2, "tbsp"));

        addRecipe(db, "Cheese Omelette",
                "1. Beat the eggs.\n2. Melt butter in a pan and pour in the eggs.\n3. Add grated cheese, fold and cook until set.",
                ri("egg", 3, "pcs"), ri("cheese", 50, "g"), ri("butter", 10, "g"));

        addRecipe(db, "Simple Pancakes",
                "1. Mix flour, egg, sugar and milk into a smooth batter.\n2. Fry a ladle of batter in a hot pan.\n3. Flip when bubbles appear and cook the other side.",
                ri("flour", 150, "g"), ri("milk", 200, "ml"), ri("egg", 1, "pcs"), ri("sugar", 1, "tbsp"));

        addRecipe(db, "Egg Fried Rice",
                "1. Cook the rice and let it cool.\n2. Stir-fry chopped onion and carrot.\n3. Push to the side, scramble the eggs, then mix in the rice.\n4. Season with soy sauce.",
                ri("rice", 200, "g"), ri("egg", 2, "pcs"), ri("onion", 1, "pcs"),
                ri("carrot", 1, "pcs"), ri("soy sauce", 2, "tbsp"));

        addRecipe(db, "Chicken Stir-Fry",
                "1. Slice the chicken, pepper and onion.\n2. Fry the chicken until golden.\n3. Add the vegetables and garlic and cook for 5 minutes.\n4. Add soy sauce and toss.",
                ri("chicken", 300, "g"), ri("onion", 1, "pcs"), ri("bell pepper", 1, "pcs"),
                ri("soy sauce", 2, "tbsp"), ri("garlic", 2, "pcs"));

        addRecipe(db, "Mashed Potatoes",
                "1. Peel and boil the potatoes until soft.\n2. Drain and mash with butter and warm milk.",
                ri("potato", 4, "pcs"), ri("butter", 30, "g"), ri("milk", 100, "ml"));

        addRecipe(db, "Cheese Toastie",
                "1. Butter the outside of the bread.\n2. Place cheese between the slices.\n3. Toast in a pan until golden on both sides.",
                ri("bread", 2, "pcs"), ri("cheese", 40, "g"), ri("butter", 10, "g"));

        addRecipe(db, "Vegetable Soup",
                "1. Chop the potatoes, carrots, onion and tomatoes.\n2. Add everything to a pot with the stock.\n3. Simmer for 25 minutes until soft.",
                ri("potato", 2, "pcs"), ri("carrot", 2, "pcs"), ri("onion", 1, "pcs"),
                ri("tomato", 2, "pcs"), ri("vegetable stock", 500, "ml"));

        addRecipe(db, "Garlic Bread",
                "1. Mash crushed garlic into the butter.\n2. Spread on the bread.\n3. Bake at 180C for 10 minutes.",
                ri("bread", 4, "pcs"), ri("butter", 40, "g"), ri("garlic", 3, "pcs"));

        addRecipe(db, "Egg Fried Noodles",
                "1. Boil the noodles and drain.\n2. Fry chopped onion, then scramble in the eggs.\n3. Add the noodles and soy sauce and toss well.",
                ri("noodle", 200, "g"), ri("egg", 2, "pcs"), ri("soy sauce", 2, "tbsp"), ri("onion", 1, "pcs"));

        addRecipe(db, "Banana Pancakes",
                "1. Mash the bananas.\n2. Mix with the egg and flour.\n3. Fry small spoonfuls until golden on both sides.",
                ri("banana", 2, "pcs"), ri("egg", 1, "pcs"), ri("flour", 50, "g"));

        addRecipe(db, "Tuna Pasta",
                "1. Boil the pasta.\n2. Fry chopped onion in olive oil.\n3. Add the tuna, then toss through the pasta.",
                ri("pasta", 200, "g"), ri("tuna", 1, "pcs"), ri("onion", 1, "pcs"), ri("olive oil", 1, "tbsp"));

        addRecipe(db, "Cheesy Baked Potato",
                "1. Prick the potatoes and bake at 200C for 1 hour.\n2. Cut open and add butter and grated cheese.\n3. Return to the oven for 5 minutes.",
                ri("potato", 2, "pcs"), ri("cheese", 60, "g"), ri("butter", 20, "g"));

        addRecipe(db, "Chicken and Rice",
                "1. Brown the chicken pieces with onion and garlic.\n2. Add the rice and enough water to cover.\n3. Cover and simmer for 20 minutes until the rice is cooked.",
                ri("chicken", 300, "g"), ri("rice", 200, "g"), ri("onion", 1, "pcs"), ri("garlic", 2, "pcs"));

        addRecipe(db, "Tomato Soup",
                "1. Fry onion and garlic until soft.\n2. Add chopped tomatoes and stock.\n3. Simmer for 20 minutes, then blend until smooth.",
                ri("tomato", 6, "pcs"), ri("onion", 1, "pcs"), ri("garlic", 2, "pcs"), ri("vegetable stock", 400, "ml"));

        addRecipe(db, "French Toast",
                "1. Whisk the eggs and milk together.\n2. Dip the bread slices in the mixture.\n3. Fry in butter until golden on both sides.",
                ri("bread", 4, "pcs"), ri("egg", 2, "pcs"), ri("milk", 100, "ml"), ri("butter", 10, "g"));

        addRecipe(db, "Rice and Beans",
                "1. Cook the rice.\n2. Fry onion, then add chopped tomatoes and beans.\n3. Simmer for 10 minutes and serve over the rice.",
                ri("rice", 200, "g"), ri("kidney bean", 200, "g"), ri("onion", 1, "pcs"), ri("tomato", 2, "pcs"));
    }

    // ---------------------------------------------------------------
    // Quick checks used to verify the seed (Logcat)
    // ---------------------------------------------------------------

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
}

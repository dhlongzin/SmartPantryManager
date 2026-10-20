package com.example.smartpantrymanager;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;


import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.content.Intent;

import java.util.ArrayList;
import java.util.List;

/**
 * Suggested Recipes screen: runs the strict-matching rule (RecipeMatcher)
 * against the current pantry and lists only the recipes the user can make.
 */
public class SuggestedRecipesActivity extends AppCompatActivity {

    private DatabaseHelper dbHelper;
    private RecipeAdapter adapter;
    private RecyclerView recyclerRecipes;
    private TextView textNoMatches;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_suggested_recipes);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, windowInsets) -> {
            Insets systemBars = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return windowInsets;
        });

        dbHelper = new DatabaseHelper(this);
        recyclerRecipes = findViewById(R.id.recyclerRecipes);
        textNoMatches = findViewById(R.id.textNoMatches);

        adapter = new RecipeAdapter(new ArrayList<>(), recipe -> {
            // Open the detail screen and pass the chosen recipe's id
            Intent intent = new Intent(SuggestedRecipesActivity.this, RecipeDetailActivity.class);
            intent.putExtra(RecipeDetailActivity.EXTRA_RECIPE_ID, recipe.getId());
            startActivity(intent);
        });
        recyclerRecipes.setLayoutManager(new LinearLayoutManager(this));
        recyclerRecipes.setAdapter(adapter);
        // Bottom navigation: highlight the Recipes tab
        NavigationHelper.setup(this, R.id.nav_suggested);
    }

    /** Re-runs the matching every time the screen is shown, so it always reflects the pantry. */
    @Override
    protected void onResume() {
        super.onResume();
        loadSuggestions();
    }

    private void loadSuggestions() {
        List<Ingredient> pantry = dbHelper.getAllIngredients();
        List<Recipe> allRecipes = dbHelper.getAllRecipes();
        List<Recipe> suggested = RecipeMatcher.getSuggestedRecipes(pantry, allRecipes);

        adapter.updateData(suggested);

        // Friendly message instead of a blank screen when nothing matches
        if (suggested.isEmpty()) {
            recyclerRecipes.setVisibility(View.GONE);
            textNoMatches.setVisibility(View.VISIBLE);
        } else {
            recyclerRecipes.setVisibility(View.VISIBLE);
            textNoMatches.setVisibility(View.GONE);
        }
    }
}
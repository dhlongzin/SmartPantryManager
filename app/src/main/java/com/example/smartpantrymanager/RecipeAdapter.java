package com.example.smartpantrymanager;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

/**
 * Custom Adapter that shows the suggested recipes in a RecyclerView.
 * Each row shows the recipe name and a short list of its ingredients.
 */
public class RecipeAdapter extends RecyclerView.Adapter<RecipeAdapter.ViewHolder> {

    /** Lets the Activity react when a recipe row is tapped. */
    public interface OnRecipeClickListener {
        void onRecipeClick(Recipe recipe);
    }

    private List<Recipe> recipes;
    private final OnRecipeClickListener listener;

    public RecipeAdapter(List<Recipe> recipes, OnRecipeClickListener listener) {
        this.recipes = recipes;
        this.listener = listener;
    }

    /** Replaces the data and redraws the list. */
    public void updateData(List<Recipe> newRecipes) {
        this.recipes = newRecipes;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_recipe, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Recipe recipe = recipes.get(position);
        holder.textName.setText(recipe.getName());

        // Build "egg, bread, butter" from the recipe's ingredient lines
        StringBuilder names = new StringBuilder();
        for (RecipeIngredient ri : recipe.getIngredients()) {
            if (names.length() > 0) names.append(", ");
            names.append(ri.getIngredientName());
        }
        holder.textIngredients.setText(names.toString());

        holder.itemView.setOnClickListener(v -> listener.onRecipeClick(recipe));
    }

    @Override
    public int getItemCount() {
        return recipes.size();
    }

    /** Holds the views of one row. */
    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView textName;
        TextView textIngredients;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            textName = itemView.findViewById(R.id.textRecipeName);
            textIngredients = itemView.findViewById(R.id.textRecipeIngredients);
        }
    }
}
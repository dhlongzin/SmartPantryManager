package com.example.smartpantrymanager;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

/**
 * Custom Adapter that binds a list of Ingredient objects (read from the
 * SQLite database) to the rows of the pantry RecyclerView.
 */
public class IngredientAdapter extends RecyclerView.Adapter<IngredientAdapter.ViewHolder> {

    /** Lets the Activity react when a row is tapped. */
    public interface OnItemClickListener {
        void onItemClick(Ingredient ingredient);
    }

    private List<Ingredient> items;
    private final OnItemClickListener listener;

    public IngredientAdapter(List<Ingredient> items, OnItemClickListener listener) {
        this.items = items;
        this.listener = listener;
    }

    /** Replaces the data and redraws the list (called after the database is re-read). */
    public void updateData(List<Ingredient> newItems) {
        this.items = newItems;
        notifyDataSetChanged();
    }

    /** Creates a row view from item_ingredient.xml and wraps it in a ViewHolder. */
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_ingredient, parent, false);
        return new ViewHolder(view);
    }

    /** Fills one row with the data of the ingredient at this position. */
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Ingredient ingredient = items.get(position);

        holder.textName.setText(capitalise(ingredient.getName()));
        holder.textQuantity.setText(formatQuantity(ingredient.getQuantity()) + " " + ingredient.getUnit());

        String expiry = ingredient.getExpiryDate();
        if (expiry == null || expiry.isEmpty()) {
            holder.textExpiry.setVisibility(View.GONE);
        } else {
            holder.textExpiry.setVisibility(View.VISIBLE);
            holder.textExpiry.setText("Expires: " + expiry);
        }

        // Tapping a row tells the Activity which ingredient was chosen
        holder.itemView.setOnClickListener(v -> listener.onItemClick(ingredient));
    }

    @Override
    public int getItemCount() {
        return items.size();
    }
    /** Returns the ingredient shown at a given row. */
    public Ingredient getItemAt(int position) {
        return items.get(position);
    }

    /** Shows 6 instead of 6.0 for whole numbers. */
    private String formatQuantity(double qty) {
        if (qty == Math.floor(qty)) {
            return String.valueOf((long) qty);
        }
        return String.valueOf(qty);
    }

    private String capitalise(String s) {
        if (s == null || s.isEmpty()) return s;
        return s.substring(0, 1).toUpperCase() + s.substring(1);
    }

    /** Holds references to the views in one row so they are not looked up repeatedly. */
    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView textName;
        TextView textQuantity;
        TextView textExpiry;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            textName = itemView.findViewById(R.id.textName);
            textQuantity = itemView.findViewById(R.id.textQuantity);
            textExpiry = itemView.findViewById(R.id.textExpiry);
        }
    }
}
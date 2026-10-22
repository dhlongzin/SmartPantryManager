package com.example.smartpantrymanager;

import android.content.res.ColorStateList;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class IngredientAdapter extends RecyclerView.Adapter<IngredientAdapter.ViewHolder> {


    public interface OnItemClickListener {
        void onItemClick(Ingredient ingredient);
    }

    private static final int WARNING_DAYS = 3;
    private static final int COLOR_RED = Color.parseColor("#E53935");
    private static final int COLOR_ORANGE = Color.parseColor("#FB8C00");

    private List<Ingredient> items;
    private final OnItemClickListener listener;
    private boolean showExpiryWarnings = true;

    public IngredientAdapter(List<Ingredient> items, OnItemClickListener listener) {
        this.items = items;
        this.listener = listener;
    }

    //Turns the expiring-soon
    public void setShowExpiryWarnings(boolean show) {
        this.showExpiryWarnings = show;
    }


    public void updateData(List<Ingredient> newItems) {
        this.items = newItems;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_ingredient, parent, false);
        return new ViewHolder(view);
    }

    //Fills one row with the data of the ingredient at this position
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
            // Reset first, because RecyclerView re-uses row views
            holder.textExpiry.setTextColor(holder.defaultExpiryColors);
            holder.textExpiry.setText("Expires: " + expiry);

            if (showExpiryWarnings) {
                applyExpiryWarning(holder.textExpiry, expiry);
            }
        }

        // Tapping a row tells the Activity which ingredient was chosen
        holder.itemView.setOnClickListener(v -> listener.onItemClick(ingredient));
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public Ingredient getItemAt(int position) {
        return items.get(position);
    }

    //Changes the expiry text and colour
    private void applyExpiryWarning(TextView textExpiry, String expiry) {
        Long days = daysUntil(expiry);
        if (days == null) return;

        if (days < 0) {
            long ago = -days;
            textExpiry.setText("Expired " + ago + (ago == 1 ? " day ago" : " days ago"));
            textExpiry.setTextColor(COLOR_RED);
        } else if (days == 0) {
            textExpiry.setText("Expires today!");
            textExpiry.setTextColor(COLOR_RED);
        } else if (days <= WARNING_DAYS) {
            textExpiry.setText("Expires in " + days + (days == 1 ? " day" : " days"));
            textExpiry.setTextColor(COLOR_ORANGE);
        }
    }

    private Long daysUntil(String dateText) {
        try {
            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
            format.setLenient(false);
            Date expiry = format.parse(dateText);

            Calendar today = Calendar.getInstance();
            today.set(Calendar.HOUR_OF_DAY, 0);
            today.set(Calendar.MINUTE, 0);
            today.set(Calendar.SECOND, 0);
            today.set(Calendar.MILLISECOND, 0);

            long diff = expiry.getTime() - today.getTimeInMillis();
            return Math.round(diff / (double) (24 * 60 * 60 * 1000));
        } catch (ParseException e) {
            return null;
        }
    }

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

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView textName;
        TextView textQuantity;
        TextView textExpiry;
        ColorStateList defaultExpiryColors;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            textName = itemView.findViewById(R.id.textName);
            textQuantity = itemView.findViewById(R.id.textQuantity);
            textExpiry = itemView.findViewById(R.id.textExpiry);
            defaultExpiryColors = textExpiry.getTextColors();
        }
    }
}
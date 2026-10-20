package com.example.smartpantrymanager;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;

/**
 * Pantry List screen: shows every ingredient stored in the database using a
 * RecyclerView and the IngredientAdapter. From here the user can add an
 * ingredient (+ button), edit one (tap a row), or delete one (swipe a row).
 */
public class MainActivity extends AppCompatActivity {

    private DatabaseHelper dbHelper;
    private IngredientAdapter adapter;
    private RecyclerView recyclerPantry;
    private TextView textEmpty;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, windowInsets) -> {
            Insets systemBars = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return windowInsets;
        });

        dbHelper = new DatabaseHelper(this);

        // Link the views from activity_main.xml
        recyclerPantry = findViewById(R.id.recyclerPantry);
        textEmpty = findViewById(R.id.textEmpty);
        FloatingActionButton fabAdd = findViewById(R.id.fabAdd);

        // Set up the RecyclerView with a vertical list layout and our adapter.
        // Tapping a row opens the Add/Edit screen in edit mode.
        adapter = new IngredientAdapter(new ArrayList<>(), ingredient -> {
            Intent intent = new Intent(MainActivity.this, AddEditIngredientActivity.class);
            intent.putExtra(AddEditIngredientActivity.EXTRA_INGREDIENT_ID, ingredient.getId());
            startActivity(intent);
        });
        recyclerPantry.setLayoutManager(new LinearLayoutManager(this));
        recyclerPantry.setAdapter(adapter);

        // Swipe a row left or right to delete it (asks for confirmation first)
        ItemTouchHelper.SimpleCallback swipeCallback = new ItemTouchHelper.SimpleCallback(
                0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
            @Override
            public boolean onMove(@NonNull RecyclerView rv, @NonNull RecyclerView.ViewHolder vh,
                                  @NonNull RecyclerView.ViewHolder target) {
                return false; // drag-and-drop reordering is not used
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                int position = viewHolder.getAdapterPosition();
                if (position == RecyclerView.NO_POSITION) return;
                confirmDelete(adapter.getItemAt(position), position);
            }
        };
        new ItemTouchHelper(swipeCallback).attachToRecyclerView(recyclerPantry);

        // The + button opens the same screen in add mode (no id passed)
        fabAdd.setOnClickListener(v ->
                startActivity(new Intent(MainActivity.this, AddEditIngredientActivity.class)));

        // Bottom navigation: highlight the Pantry tab
        NavigationHelper.setup(this, R.id.nav_pantry);
    }

    /** Re-reads the database every time the screen comes back into view. */
    @Override
    protected void onResume() {
        super.onResume();
        // Read the Settings toggle before drawing the list
        SharedPreferences prefs = getSharedPreferences(SettingsActivity.PREFS_NAME, MODE_PRIVATE);
        adapter.setShowExpiryWarnings(prefs.getBoolean(SettingsActivity.KEY_EXPIRY_WARNINGS, true));
        loadPantry();
    }

    /** Reads all ingredients and shows either the list or the empty message. */
    private void loadPantry() {
        List<Ingredient> ingredients = dbHelper.getAllIngredients();
        adapter.updateData(ingredients);

        if (ingredients.isEmpty()) {
            recyclerPantry.setVisibility(View.GONE);
            textEmpty.setVisibility(View.VISIBLE);
        } else {
            recyclerPantry.setVisibility(View.VISIBLE);
            textEmpty.setVisibility(View.GONE);
        }
    }

    /** Asks the user to confirm, then deletes the ingredient and refreshes the list. */
    private void confirmDelete(Ingredient ingredient, int position) {
        new AlertDialog.Builder(this)
                .setTitle("Delete ingredient")
                .setMessage("Remove " + ingredient.getName() + " from your pantry?")
                .setPositiveButton("Delete", (dialog, which) -> {
                    dbHelper.deleteIngredient(ingredient.getId());
                    Toast.makeText(this, "Ingredient deleted", Toast.LENGTH_SHORT).show();
                    loadPantry();
                })
                // On cancel, redraw the row so it slides back into place
                .setNegativeButton("Cancel", (dialog, which) -> adapter.notifyItemChanged(position))
                .setOnCancelListener(dialog -> adapter.notifyItemChanged(position))
                .show();
    }
}
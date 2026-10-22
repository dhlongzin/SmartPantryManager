package com.example.smartpantrymanager;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.util.Calendar;
import java.util.Locale;


public class AddEditIngredientActivity extends AppCompatActivity {
    public static final String EXTRA_INGREDIENT_ID = "ingredient_id";

    // Units of ingredient
    private static final String[] UNITS = {"g", "kg", "ml", "L", "pcs", "tbsp", "tsp"};

    private DatabaseHelper dbHelper;
    private EditText editName, editQuantity, editExpiry;
    private Spinner spinnerUnit;
    private long ingredientId = -1;
    private String expiryDate = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_add_edit_ingredient);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, windowInsets) -> {
            Insets systemBars = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return windowInsets;
        });

        dbHelper = new DatabaseHelper(this);

        // Link the views
        TextView textScreenTitle = findViewById(R.id.textScreenTitle);
        editName = findViewById(R.id.editName);
        editQuantity = findViewById(R.id.editQuantity);
        spinnerUnit = findViewById(R.id.spinnerUnit);
        editExpiry = findViewById(R.id.editExpiry);
        Button buttonSave = findViewById(R.id.buttonSave);
        Button buttonCancel = findViewById(R.id.buttonCancel);

        // Fill the unit Spinner
        ArrayAdapter<String> unitAdapter = new ArrayAdapter<>(
                this, android.R.layout.simple_spinner_dropdown_item, UNITS);
        spinnerUnit.setAdapter(unitAdapter);

        // Read the Intent: an id means edit mode, no id means add mode
        ingredientId = getIntent().getLongExtra(EXTRA_INGREDIENT_ID, -1);
        if (ingredientId != -1) {
            textScreenTitle.setText("Edit Ingredient");
            loadIngredientIntoForm();
        }

        // Tap the expiry field to pick a date, long-press to clear it
        editExpiry.setOnClickListener(v -> showDatePicker());
        editExpiry.setOnLongClickListener(v -> {
            expiryDate = null;
            editExpiry.setText("");
            return true;
        });

        buttonSave.setOnClickListener(v -> saveIngredient());
        buttonCancel.setOnClickListener(v -> finish());
    }

    private void loadIngredientIntoForm() {
        Ingredient ingredient = dbHelper.getIngredientById(ingredientId);
        if (ingredient == null) {
            Toast.makeText(this, "Ingredient not found", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        editName.setText(ingredient.getName());
        editQuantity.setText(formatQuantity(ingredient.getQuantity()));
        for (int i = 0; i < UNITS.length; i++) {
            if (UNITS[i].equals(ingredient.getUnit())) {
                spinnerUnit.setSelection(i);
                break;
            }
        }
        expiryDate = ingredient.getExpiryDate();
        if (expiryDate != null) {
            editExpiry.setText(expiryDate);
        }
    }

    //enter date
    private void showDatePicker() {
        Calendar cal = Calendar.getInstance();
        int year = cal.get(Calendar.YEAR);
        int month = cal.get(Calendar.MONTH);
        int day = cal.get(Calendar.DAY_OF_MONTH);

        // If a date is already set, start the picker on it
        if (expiryDate != null) {
            String[] parts = expiryDate.split("-");
            if (parts.length == 3) {
                year = Integer.parseInt(parts[0]);
                month = Integer.parseInt(parts[1]) - 1;
                day = Integer.parseInt(parts[2]);
            }
        }

        new DatePickerDialog(this, (view, y, m, d) -> {
            expiryDate = String.format(Locale.US, "%04d-%02d-%02d", y, m + 1, d);
            editExpiry.setText(expiryDate);
        }, year, month, day).show();
    }

    private void saveIngredient() {
        String name = editName.getText().toString().trim();
        String qtyText = editQuantity.getText().toString().trim();
        String unit = spinnerUnit.getSelectedItem().toString();

        editName.setError(null);
        editQuantity.setError(null);
        boolean valid = true;

        // Name of ingredient
        if (name.isEmpty()) {
            editName.setError("Enter an ingredient name");
            valid = false;
        } else if (!name.matches("[A-Za-z][A-Za-z \\-]*")) {
            editName.setError("Use letters only (no numbers or symbols)");
            valid = false;
        }

        // Quantity
        double quantity = 0;
        if (qtyText.isEmpty()) {
            editQuantity.setError("Enter a quantity");
            valid = false;
        } else {
            try {
                quantity = Double.parseDouble(qtyText);
                if (quantity <= 0) {
                    editQuantity.setError("Quantity must be greater than 0");
                    valid = false;
                } else if (quantity > 100000) {
                    editQuantity.setError("Quantity is too large");
                    valid = false;
                }
            } catch (NumberFormatException e) {
                editQuantity.setError("Enter a valid number");
                valid = false;
            }
        }

        if (!valid) return;

        // Reject duplicates
        if (dbHelper.ingredientExists(name, unit, ingredientId)) {
            editName.setError("Already in your pantry");
            return;
        }

        Ingredient ingredient = new Ingredient(ingredientId, name, quantity, unit, expiryDate);

        if (ingredientId == -1) {
            dbHelper.insertIngredient(ingredient);
            Toast.makeText(this, "Ingredient added", Toast.LENGTH_SHORT).show();
        } else {
            dbHelper.updateIngredient(ingredient);
            Toast.makeText(this, "Ingredient updated", Toast.LENGTH_SHORT).show();
        }
        finish();

    }

    private String formatQuantity(double qty) {
        if (qty == Math.floor(qty)) {
            return String.valueOf((long) qty);
        }
        return String.valueOf(qty);
    }
}
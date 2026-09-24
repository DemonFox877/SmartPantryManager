package com.example.smartpantrymanager;

import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class AddIngredientActivity extends AppCompatActivity {

    private EditText ingredientName;
    private EditText quantity;
    private Spinner unit;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_add_ingredient);

        // Keep the form above the keyboard and system navigation bar.
        ViewCompat.setOnApplyWindowInsetsListener(
                findViewById(R.id.main), (view, insets) -> {
                    Insets bars = insets.getInsets(
                            WindowInsetsCompat.Type.systemBars());
                    Insets keyboard = insets.getInsets(
                            WindowInsetsCompat.Type.ime());

                    view.setPadding(
                            bars.left,
                            bars.top,
                            bars.right,
                            Math.max(bars.bottom, keyboard.bottom));

                    return insets;
                });

        ingredientName = findViewById(R.id.edtIngredientName);
        quantity = findViewById(R.id.edtQuantity);
        unit = findViewById(R.id.spinnerUnit);

        // Restrict units to options we can handle consistently.
        String[] units = {"g", "kg", "ml", "l", "pcs"};

        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                units);

        adapter.setDropDownViewResource(
                android.R.layout.simple_spinner_dropdown_item);
        unit.setAdapter(adapter);

        // Apply the saved default when opening a new ingredient form.
        if (savedInstanceState == null) {
            String defaultUnit = getSharedPreferences(
                    SettingsActivity.PREFS_NAME, MODE_PRIVATE)
                    .getString(SettingsActivity.KEY_DEFAULT_UNIT, "g");

            int defaultPosition = adapter.getPosition(defaultUnit);
            unit.setSelection(defaultPosition >= 0 ? defaultPosition : 0);
        }

        // Close this screen and return to the pantry.
        findViewById(R.id.btnCancel).setOnClickListener(view -> finish());

        findViewById(R.id.btnSaveIngredient).setOnClickListener(
                view -> validateIngredient());
    }

    private void validateIngredient() {
        String name = ingredientName.getText().toString().trim();
        String quantityText = quantity.getText().toString().trim();

        if (name.isEmpty()) {
            ingredientName.setError("Enter an ingredient name");
            ingredientName.requestFocus();
            return;
        }

        if (quantityText.isEmpty()) {
            quantity.setError("Enter a quantity");
            quantity.requestFocus();
            return;
        }

        double amount;

        try {
            amount = Double.parseDouble(quantityText);
        } catch (NumberFormatException exception) {
            quantity.setError("Enter a valid number");
            quantity.requestFocus();
            return;
        }

        if (Double.isNaN(amount) || Double.isInfinite(amount)
                || amount <= 0) {
            quantity.setError("Enter a quantity greater than zero");
            quantity.requestFocus();
            return;
        }

        String selectedUnit = unit.getSelectedItem().toString();

// Prevent repeated taps while saving.
        findViewById(R.id.btnSaveIngredient).setEnabled(false);
        findViewById(R.id.btnCancel).setEnabled(false);

// Run database work away from the screen's main thread.
        new Thread(() -> {
            boolean saved = false;

            try (DatabaseHelper database =
                         new DatabaseHelper(getApplicationContext())) {

                long recordId = database.addIngredient(
                        name, amount, selectedUnit);

                saved = recordId != -1;

            } catch (android.database.sqlite.SQLiteException exception) {
                android.util.Log.e(
                        "AddIngredient",
                        "Could not save ingredient",
                        exception);
            }

            final boolean saveSuccessful = saved;

            // Screen updates must run on the main thread.
            runOnUiThread(() -> {
                if (isFinishing() || isDestroyed()) {
                    return;
                }

                findViewById(R.id.btnSaveIngredient).setEnabled(true);
                findViewById(R.id.btnCancel).setEnabled(true);

                if (saveSuccessful) {
                    Toast.makeText(
                            this,
                            "Ingredient saved",
                            Toast.LENGTH_SHORT
                    ).show();

                    finish();
                } else {
                    Toast.makeText(
                            this,
                            "Could not save. Please try again.",
                            Toast.LENGTH_LONG
                    ).show();
                }
            });
        }).start();
    }
}


package com.example.smartpantrymanager;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteException;
import android.os.Bundle;
import android.text.InputFilter;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private PantryAdapter pantryAdapter;
    private TextView emptyMessage;
    private int latestLoad = 0;

    private final String[] units = {"g", "kg", "ml", "l", "pcs"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        ViewCompat.setOnApplyWindowInsetsListener(
                findViewById(R.id.main), (view, insets) -> {
                    Insets bars = insets.getInsets(
                            WindowInsetsCompat.Type.systemBars());

                    view.setPadding(
                            bars.left, bars.top, bars.right, bars.bottom);

                    return insets;
                });

        ListView pantryList = findViewById(R.id.pantryListView);
        emptyMessage = findViewById(R.id.txtEmptyPantry);

        pantryAdapter = new PantryAdapter(this);
        pantryList.setAdapter(pantryAdapter);
        emptyMessage.setText("Loading pantry...");

        findViewById(R.id.btnAddIngredient).setOnClickListener(view -> {
            Intent intent = new Intent(
                    MainActivity.this, AddIngredientActivity.class);
            startActivity(intent);
        });

        // Open the options for the ingredient that was tapped.
        pantryList.setOnItemClickListener((parent, view, position, id) -> {
            PantryItem item = pantryAdapter.getItem(position);
            showIngredientOptions(item);
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadPantry();
    }

    private void loadPantry() {
        final int requestId = ++latestLoad;

        new Thread(() -> {
            List<PantryItem> items = new ArrayList<>();

            try (DatabaseHelper database =
                         new DatabaseHelper(getApplicationContext());
                 Cursor cursor = database.getAllIngredients()) {

                int idColumn = cursor.getColumnIndexOrThrow(
                        DatabaseHelper.COL_ID);
                int nameColumn = cursor.getColumnIndexOrThrow(
                        DatabaseHelper.COL_NAME);
                int quantityColumn = cursor.getColumnIndexOrThrow(
                        DatabaseHelper.COL_QUANTITY);
                int unitColumn = cursor.getColumnIndexOrThrow(
                        DatabaseHelper.COL_UNIT);

                while (cursor.moveToNext()) {
                    items.add(new PantryItem(
                            cursor.getLong(idColumn),
                            cursor.getString(nameColumn),
                            cursor.getDouble(quantityColumn),
                            cursor.getString(unitColumn)
                    ));
                }

            } catch (SQLiteException exception) {
                Log.e("Pantry", "Could not load ingredients", exception);

                runOnUiThread(() -> {
                    if (isFinishing() || isDestroyed()
                            || requestId != latestLoad) {
                        return;
                    }

                    if (pantryAdapter.getCount() == 0) {
                        emptyMessage.setText("Could not load pantry.");
                        emptyMessage.setVisibility(View.VISIBLE);
                    }

                    Toast.makeText(this,
                            "Could not load ingredients. Please try again.",
                            Toast.LENGTH_LONG).show();
                });
                return;
            }

            runOnUiThread(() -> {
                if (isFinishing() || isDestroyed()
                        || requestId != latestLoad) {
                    return;
                }

                pantryAdapter.setItems(items);
                emptyMessage.setText(
                        "Your pantry is empty. Add an ingredient to get started.");
                emptyMessage.setVisibility(
                        items.isEmpty() ? View.VISIBLE : View.GONE);
            });
        }).start();
    }

    private void showIngredientOptions(PantryItem item) {
        new AlertDialog.Builder(this)
                .setTitle(item.getName())
                .setItems(new String[]{"Edit", "Delete"}, (dialog, choice) -> {
                    if (choice == 0) {
                        showEditDialog(item);
                    } else {
                        showDeleteDialog(item);
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void showEditDialog(PantryItem item) {
        // Build a small, scrollable edit form.
        LinearLayout form = new LinearLayout(this);
        form.setOrientation(LinearLayout.VERTICAL);

        int padding = Math.round(
                24 * getResources().getDisplayMetrics().density);
        form.setPadding(padding, padding / 2, padding, padding / 2);

        addLabel(form, "Ingredient name");

        EditText nameInput = new EditText(this);
        nameInput.setSingleLine(true);
        nameInput.setInputType(
                InputType.TYPE_CLASS_TEXT
                        | InputType.TYPE_TEXT_FLAG_CAP_SENTENCES);
        nameInput.setFilters(
                new InputFilter[]{new InputFilter.LengthFilter(80)});
        nameInput.setText(item.getName());
        form.addView(nameInput);

        addLabel(form, "Quantity");

        EditText quantityInput = new EditText(this);
        quantityInput.setSingleLine(true);
        quantityInput.setInputType(
                InputType.TYPE_CLASS_NUMBER
                        | InputType.TYPE_NUMBER_FLAG_DECIMAL);
        quantityInput.setText(
                BigDecimal.valueOf(item.getQuantity())
                        .stripTrailingZeros().toPlainString());
        quantityInput.setSelectAllOnFocus(true);
        form.addView(quantityInput);

        addLabel(form, "Unit");

        Spinner unitInput = new Spinner(this);
        ArrayAdapter<String> unitAdapter = new ArrayAdapter<>(
                this, android.R.layout.simple_spinner_item, units);
        unitAdapter.setDropDownViewResource(
                android.R.layout.simple_spinner_dropdown_item);
        unitInput.setAdapter(unitAdapter);

        for (int index = 0; index < units.length; index++) {
            if (units[index].equals(item.getUnit())) {
                unitInput.setSelection(index);
                break;
            }
        }

        int controlHeight = Math.round(
                48 * getResources().getDisplayMetrics().density);
        form.addView(unitInput, new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, controlHeight));

        ScrollView scroll = new ScrollView(this);
        scroll.addView(form);

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle("Edit Ingredient")
                .setView(scroll)
                .setPositiveButton("Save", null)
                .setNegativeButton("Cancel", null)
                .create();

        dialog.show();

        // A custom listener keeps the form open if validation fails.
        dialog.getButton(AlertDialog.BUTTON_POSITIVE)
                .setOnClickListener(view -> {
                    String name = nameInput.getText().toString().trim();
                    String quantityText =
                            quantityInput.getText().toString().trim();

                    if (name.isEmpty()) {
                        nameInput.setError("Enter an ingredient name");
                        nameInput.requestFocus();
                        return;
                    }

                    double quantity;

                    try {
                        quantity = Double.parseDouble(quantityText);
                    } catch (NumberFormatException exception) {
                        quantityInput.setError("Enter a valid quantity");
                        quantityInput.requestFocus();
                        return;
                    }

                    if (Double.isNaN(quantity)
                            || Double.isInfinite(quantity)
                            || quantity <= 0) {
                        quantityInput.setError(
                                "Quantity must be greater than zero");
                        quantityInput.requestFocus();
                        return;
                    }

                    String unit = unitInput.getSelectedItem().toString();

                    changeIngredient(
                            dialog, item, name, quantity, unit, false);
                });
    }

    private void addLabel(LinearLayout form, String text) {
        TextView label = new TextView(this);
        label.setText(text);
        form.addView(label);
    }

    private void showDeleteDialog(PantryItem item) {
        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle("Delete Ingredient?")
                .setMessage("Remove " + item.getName()
                        + " from your pantry?")
                .setPositiveButton("Delete", null)
                .setNegativeButton("Cancel", null)
                .create();

        dialog.show();

        dialog.getButton(AlertDialog.BUTTON_POSITIVE)
                .setOnClickListener(view -> changeIngredient(
                        dialog, item, null, 0, null, true));
    }

    private void changeIngredient(
            AlertDialog dialog, PantryItem item, String name,
            double quantity, String unit, boolean deleting) {

        // Prevent repeated taps while the database operation runs.
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(false);
        dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setEnabled(false);
        dialog.setCancelable(false);

        new Thread(() -> {
            int affectedRows = -1;

            try (DatabaseHelper database =
                         new DatabaseHelper(getApplicationContext())) {

                if (deleting) {
                    affectedRows = database.deleteIngredient(item.getId());
                } else {
                    affectedRows = database.updateIngredient(
                            item.getId(), name, quantity, unit);
                }

            } catch (SQLiteException exception) {
                Log.e("Pantry", "Could not change ingredient", exception);
            }

            final int result = affectedRows;

            runOnUiThread(() -> {
                if (isFinishing() || isDestroyed()) {
                    return;
                }

                if (result == 1) {
                    dialog.dismiss();

                    Toast.makeText(this,
                            deleting ? "Ingredient deleted"
                                    : "Ingredient updated",
                            Toast.LENGTH_SHORT).show();

                    loadPantry();

                } else if (result == 0) {
                    dialog.dismiss();
                    loadPantry();

                    Toast.makeText(this,
                            "This ingredient no longer exists.",
                            Toast.LENGTH_LONG).show();

                } else {
                    dialog.getButton(AlertDialog.BUTTON_POSITIVE)
                            .setEnabled(true);
                    dialog.getButton(AlertDialog.BUTTON_NEGATIVE)
                            .setEnabled(true);
                    dialog.setCancelable(true);

                    Toast.makeText(this,
                            "Could not save the change. Please try again.",
                            Toast.LENGTH_LONG).show();
                }
            });
        }).start();
    }
}

package com.example.smartpantrymanager;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteException;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private PantryAdapter pantryAdapter;
    private TextView emptyMessage;
    private int latestLoad = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        // Keep content clear of the system bars.
        ViewCompat.setOnApplyWindowInsetsListener(
                findViewById(R.id.main), (view, insets) -> {
                    Insets bars = insets.getInsets(
                            WindowInsetsCompat.Type.systemBars());

                    view.setPadding(
                            bars.left, bars.top,
                            bars.right, bars.bottom);

                    return insets;
                });

        ListView pantryList = findViewById(R.id.pantryListView);
        emptyMessage = findViewById(R.id.txtEmptyPantry);

        pantryAdapter = new PantryAdapter(this);
        pantryList.setAdapter(pantryAdapter);
        emptyMessage.setText("Loading pantry...");

        findViewById(R.id.btnAddIngredient).setOnClickListener(view -> {
            Intent intent = new Intent(
                    MainActivity.this,
                    AddIngredientActivity.class);
            startActivity(intent);
        });
    }

    @Override
    protected void onResume() {
        super.onResume();

        // Refresh when this screen opens or we return from the form.
        loadPantry();
    }

    private void loadPantry() {
        final int requestId = ++latestLoad;

        new Thread(() -> {
            List<PantryItem> loadedItems = new ArrayList<>();

            // Close the cursor and database automatically when finished.
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
                    PantryItem item = new PantryItem(
                            cursor.getLong(idColumn),
                            cursor.getString(nameColumn),
                            cursor.getDouble(quantityColumn),
                            cursor.getString(unitColumn));

                    loadedItems.add(item);
                }

            } catch (SQLiteException exception) {
                Log.e("MainActivity", "Could not load pantry", exception);

                runOnUiThread(() -> {
                    if (isFinishing() || isDestroyed()
                            || requestId != latestLoad) {
                        return;
                    }

                    if (pantryAdapter.getCount() == 0) {
                        emptyMessage.setText(
                                "Could not load pantry. Reopen this screen to retry.");
                        emptyMessage.setVisibility(View.VISIBLE);
                    }

                    Toast.makeText(
                            this,
                            "Could not load pantry",
                            Toast.LENGTH_LONG
                    ).show();
                });
                return;
            }

            runOnUiThread(() -> {
                // Ignore results from an older loading request.
                if (isFinishing() || isDestroyed()
                        || requestId != latestLoad) {
                    return;
                }

                pantryAdapter.setItems(loadedItems);

                if (loadedItems.isEmpty()) {
                    emptyMessage.setText(
                            "Your pantry is empty. Add an ingredient to get started.");
                    emptyMessage.setVisibility(View.VISIBLE);
                } else {
                    emptyMessage.setVisibility(View.GONE);
                }
            });
        }).start();
    }
}

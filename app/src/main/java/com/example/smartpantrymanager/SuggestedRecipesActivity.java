package com.example.smartpantrymanager;

import android.content.Intent;
import android.database.sqlite.SQLiteException;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class SuggestedRecipesActivity extends AppCompatActivity {
    private final ExecutorService worker = Executors.newSingleThreadExecutor();
    private final List<Recipe> matches = new ArrayList<>();
    private ArrayAdapter<String> adapter;
    private TextView status;
    private View retry;
    private int latestLoad = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_suggested_recipes);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.recipeScreenRoot),
                (view, insets) -> {
                    Insets bars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
                    view.setPadding(bars.left, bars.top, bars.right, bars.bottom);
                    return insets;
                });

        status = findViewById(R.id.recipeStatus);
        retry = findViewById(R.id.btnRetryRecipes);
        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1);
        ListView list = findViewById(R.id.recipeList);
        list.setAdapter(adapter);
        list.setOnItemClickListener((parent, view, position, id) -> {
            Intent intent = new Intent(this, RecipeDetailActivity.class);
            intent.putExtra(RecipeDetailActivity.EXTRA_RECIPE_ID,
                    matches.get(position).getId());
            startActivity(intent);
        });
        retry.setOnClickListener(view -> loadRecipes());
        findViewById(R.id.btnBackToPantry).setOnClickListener(view -> finish());
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadRecipes();
    }

    private void loadRecipes() {
        final int request = ++latestLoad;
        status.setText(R.string.recipe_loading);
        retry.setVisibility(View.GONE);
        matches.clear();
        adapter.clear();

        worker.execute(() -> {
            try (DatabaseHelper database = new DatabaseHelper(getApplicationContext())) {
                List<PantryItem> pantry = database.getPantryItems();
                List<Recipe> recipes = database.getAllRecipes();
                List<Recipe> found = new ArrayList<>();
                for (Recipe recipe : recipes) {
                    if (RecipeMatcher.canMake(recipe, pantry)) found.add(recipe);
                }

                runOnUiThread(() -> {
                    if (isFinishing() || isDestroyed() || request != latestLoad) return;
                    matches.addAll(found);
                    for (Recipe recipe : found) adapter.add(recipe.getName());
                    if (found.isEmpty()) {
                        status.setText(pantry.isEmpty()
                                ? R.string.recipe_empty_pantry : R.string.recipe_no_matches);
                    } else {
                        status.setText(getString(R.string.recipe_match_count,
                                found.size(), recipes.size()));
                    }
                });
            } catch (SQLiteException exception) {
                Log.e("Recipes", "Could not load recipes", exception);
                runOnUiThread(() -> {
                    if (isFinishing() || isDestroyed() || request != latestLoad) return;
                    status.setText(R.string.recipe_load_failed);
                    retry.setVisibility(View.VISIBLE);
                });
            }
        });
    }

    @Override
    protected void onDestroy() {
        latestLoad++;
        worker.shutdown();
        super.onDestroy();
    }
}

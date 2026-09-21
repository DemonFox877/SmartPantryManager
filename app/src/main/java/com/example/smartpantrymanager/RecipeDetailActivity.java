package com.example.smartpantrymanager;

import android.database.sqlite.SQLiteException;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.math.BigDecimal;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class RecipeDetailActivity extends AppCompatActivity {
    public static final String EXTRA_RECIPE_ID = "recipe_id";
    private final ExecutorService worker = Executors.newSingleThreadExecutor();
    private TextView title;
    private TextView status;
    private TextView ingredients;
    private TextView instructions;
    private View content;
    private View retry;
    private int latestLoad = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_recipe_detail);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.recipeDetailRoot),
                (view, insets) -> {
                    Insets bars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
                    view.setPadding(bars.left, bars.top, bars.right, bars.bottom);
                    return insets;
                });

        title = findViewById(R.id.recipeTitle);
        status = findViewById(R.id.recipeDetailStatus);
        ingredients = findViewById(R.id.recipeIngredients);
        instructions = findViewById(R.id.recipeInstructions);
        content = findViewById(R.id.recipeDetailContent);
        retry = findViewById(R.id.btnRetryRecipeDetail);
        retry.setOnClickListener(view -> loadRecipe());
        findViewById(R.id.btnBackToRecipes).setOnClickListener(view -> finish());
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadRecipe();
    }

    private void loadRecipe() {
        final int request = ++latestLoad;
        long recipeId = getIntent().getLongExtra(EXTRA_RECIPE_ID, -1);
        content.setVisibility(View.GONE);
        retry.setVisibility(View.GONE);
        status.setText(R.string.recipe_loading);

        worker.execute(() -> {
            try (DatabaseHelper database = new DatabaseHelper(getApplicationContext())) {
                Recipe recipe = database.getRecipe(recipeId);
                boolean available = recipe != null
                        && RecipeMatcher.canMake(recipe, database.getPantryItems());

                runOnUiThread(() -> {
                    if (isFinishing() || isDestroyed() || request != latestLoad) return;
                    if (recipe == null) {
                        status.setText(R.string.recipe_not_found);
                        return;
                    }
                    title.setText(recipe.getName());
                    status.setText(available ? R.string.recipe_available
                            : R.string.recipe_not_available);

                    StringBuilder text = new StringBuilder();
                    for (Recipe.Ingredient item : recipe.getIngredients()) {
                        text.append("\u2022 ").append(item.getName()).append(" — ")
                                .append(BigDecimal.valueOf(item.getQuantity())
                                        .stripTrailingZeros().toPlainString())
                                .append(" ").append(item.getUnit()).append("\n");
                    }
                    ingredients.setText(text.toString().trim());
                    instructions.setText(recipe.getInstructions());
                    content.setVisibility(View.VISIBLE);
                });
            } catch (SQLiteException exception) {
                Log.e("Recipes", "Could not load recipe detail", exception);
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

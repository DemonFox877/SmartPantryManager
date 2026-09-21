package com.example.smartpantrymanager;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.List;

public class DatabaseHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "smart_pantry.db";
    private static final int DATABASE_VERSION = 2;

    public static final String TABLE_PANTRY = "pantry";
    public static final String COL_ID = "_id";
    public static final String COL_NAME = "name";
    public static final String COL_QUANTITY = "quantity";
    public static final String COL_UNIT = "unit";

    public DatabaseHelper(Context context) {
        super(context.getApplicationContext(), DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onConfigure(SQLiteDatabase db) {
        super.onConfigure(db);
        db.setForeignKeyConstraintsEnabled(true);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE pantry ("
                + "_id INTEGER PRIMARY KEY AUTOINCREMENT, "
                + "name TEXT NOT NULL CHECK(length(trim(name)) > 0), "
                + "quantity REAL NOT NULL CHECK(quantity > 0), "
                + "unit TEXT NOT NULL CHECK(unit IN ('g', 'kg', 'ml', 'l', 'pcs')))" );
        createRecipeTables(db);
        RecipeSeed.insertAll(db);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // SQLiteOpenHelper wraps this migration in a transaction.
        // Keep the existing pantry table and its rows intact.
        if (oldVersion < 2) {
            createRecipeTables(db);
            RecipeSeed.insertAll(db);
        }
    }

    private void createRecipeTables(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE recipes ("
                + "_id INTEGER PRIMARY KEY AUTOINCREMENT, "
                + "name TEXT NOT NULL UNIQUE, instructions TEXT NOT NULL)");
        db.execSQL("CREATE TABLE recipe_ingredients ("
                + "_id INTEGER PRIMARY KEY AUTOINCREMENT, "
                + "recipe_id INTEGER NOT NULL REFERENCES recipes(_id) ON DELETE CASCADE, "
                + "name TEXT NOT NULL CHECK(length(trim(name)) > 0), "
                + "quantity REAL NOT NULL CHECK(quantity > 0), "
                + "unit TEXT NOT NULL CHECK(unit IN ('g', 'kg', 'ml', 'l', 'pcs')))");
        db.execSQL("CREATE INDEX idx_recipe_ingredients_recipe "
                + "ON recipe_ingredients(recipe_id)");
    }

    private ContentValues ingredientValues(String name, double quantity, String unit) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Ingredient name is required");
        }
        if (Double.isNaN(quantity) || Double.isInfinite(quantity) || quantity <= 0) {
            throw new IllegalArgumentException("Quantity must be positive");
        }
        ContentValues values = new ContentValues();
        values.put(COL_NAME, name.trim());
        values.put(COL_QUANTITY, quantity);
        values.put(COL_UNIT, unit);
        return values;
    }

    public long addIngredient(String name, double quantity, String unit) {
        return getWritableDatabase().insert(TABLE_PANTRY, null,
                ingredientValues(name, quantity, unit));
    }

    public int updateIngredient(long id, String name, double quantity, String unit) {
        return getWritableDatabase().update(TABLE_PANTRY,
                ingredientValues(name, quantity, unit), COL_ID + " = ?",
                new String[]{String.valueOf(id)});
    }

    public int deleteIngredient(long id) {
        return getWritableDatabase().delete(TABLE_PANTRY, COL_ID + " = ?",
                new String[]{String.valueOf(id)});
    }

    public Cursor getAllIngredients() {
        return getReadableDatabase().query(TABLE_PANTRY,
                new String[]{COL_ID, COL_NAME, COL_QUANTITY, COL_UNIT},
                null, null, null, null, COL_NAME + " COLLATE NOCASE ASC");
    }

    public List<PantryItem> getPantryItems() {
        List<PantryItem> items = new ArrayList<>();
        try (Cursor cursor = getAllIngredients()) {
            while (cursor.moveToNext()) {
                items.add(new PantryItem(cursor.getLong(0), cursor.getString(1),
                        cursor.getDouble(2), cursor.getString(3)));
            }
        }
        return items;
    }

    public List<Recipe> getAllRecipes() {
        SQLiteDatabase db = getReadableDatabase();
        List<Recipe> recipes = new ArrayList<>();
        try (Cursor cursor = db.query("recipes",
                new String[]{"_id", "name", "instructions"},
                null, null, null, null, "name COLLATE NOCASE ASC")) {
            while (cursor.moveToNext()) {
                recipes.add(readRecipe(db, cursor));
            }
        }
        return recipes;
    }

    public Recipe getRecipe(long id) {
        SQLiteDatabase db = getReadableDatabase();
        try (Cursor cursor = db.query("recipes",
                new String[]{"_id", "name", "instructions"},
                "_id = ?", new String[]{String.valueOf(id)}, null, null, null)) {
            return cursor.moveToFirst() ? readRecipe(db, cursor) : null;
        }
    }

    private Recipe readRecipe(SQLiteDatabase db, Cursor cursor) {
        long id = cursor.getLong(0);
        List<Recipe.Ingredient> ingredients = new ArrayList<>();
        try (Cursor rows = db.query("recipe_ingredients",
                new String[]{"name", "quantity", "unit"},
                "recipe_id = ?", new String[]{String.valueOf(id)},
                null, null, "_id ASC")) {
            while (rows.moveToNext()) {
                ingredients.add(new Recipe.Ingredient(rows.getString(0),
                        rows.getDouble(1), rows.getString(2)));
            }
        }
        return new Recipe(id, cursor.getString(1), cursor.getString(2), ingredients);
    }
}

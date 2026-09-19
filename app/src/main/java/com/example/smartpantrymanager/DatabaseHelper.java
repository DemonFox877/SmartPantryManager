package com.example.smartpantrymanager;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "smart_pantry.db";
    private static final int DATABASE_VERSION = 1;

    public static final String TABLE_PANTRY = "pantry";
    public static final String COL_ID = "_id";
    public static final String COL_NAME = "name";
    public static final String COL_QUANTITY = "quantity";
    public static final String COL_UNIT = "unit";

    public DatabaseHelper(Context context) {
        super(context.getApplicationContext(),
                DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // Each pantry record has an ID, name, quantity and unit.
        String createPantryTable =
                "CREATE TABLE " + TABLE_PANTRY + " (" +
                        COL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        COL_NAME + " TEXT NOT NULL CHECK(length(trim(name)) > 0), " +
                        COL_QUANTITY + " REAL NOT NULL CHECK(quantity > 0), " +
                        COL_UNIT + " TEXT NOT NULL " +
                        "CHECK(unit IN ('g', 'kg', 'ml', 'l', 'pcs')))";

        db.execSQL(createPantryTable);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db,
                          int oldVersion, int newVersion) {
        // Add a migration here when we change the database version.
        // Stop rather than silently deleting existing pantry data.
        throw new IllegalStateException(
                "Database migration required from "
                        + oldVersion + " to " + newVersion);
    }

    public long addIngredient(String name, double quantity, String unit) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Ingredient name is required");
        }

        if (Double.isNaN(quantity) || Double.isInfinite(quantity)
                || quantity <= 0) {
            throw new IllegalArgumentException("Quantity must be positive");
        }

        ContentValues values = new ContentValues();
        values.put(COL_NAME, name.trim());
        values.put(COL_QUANTITY, quantity);
        values.put(COL_UNIT, unit);

        // Returns the new record ID, or -1 if insertion fails.
        return getWritableDatabase().insert(
                TABLE_PANTRY, null, values);
    }

    public Cursor getAllIngredients() {
        // The caller must close this Cursor after reading the results.
        return getReadableDatabase().query(
                TABLE_PANTRY,
                new String[]{COL_ID, COL_NAME, COL_QUANTITY, COL_UNIT},
                null,
                null,
                null,
                null,
                COL_NAME + " COLLATE NOCASE ASC"
        );
    }
}

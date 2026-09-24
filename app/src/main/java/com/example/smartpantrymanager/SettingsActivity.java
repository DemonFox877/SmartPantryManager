package com.example.smartpantrymanager;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class SettingsActivity extends AppCompatActivity {

    public static final String PREFS_NAME = "dormbite_settings";
    public static final String KEY_DEFAULT_UNIT = "default_unit";

    private static final String[] UNITS = {
            "g", "kg", "ml", "l", "pcs"
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_settings);

        // Keep the content clear of the phone's system bars.
        ViewCompat.setOnApplyWindowInsetsListener(
                findViewById(R.id.main), (view, windowInsets) -> {
                    Insets systemBars = windowInsets.getInsets(
                            WindowInsetsCompat.Type.systemBars());

                    view.setPadding(
                            systemBars.left,
                            systemBars.top,
                            systemBars.right,
                            systemBars.bottom);

                    return windowInsets;
                });

        Spinner unitSpinner = findViewById(R.id.spinnerDefaultUnit);

        ArrayAdapter<String> unitAdapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                UNITS);

        unitAdapter.setDropDownViewResource(
                android.R.layout.simple_spinner_dropdown_item);

        unitSpinner.setAdapter(unitAdapter);

        SharedPreferences preferences = getSharedPreferences(
                PREFS_NAME, MODE_PRIVATE);

        // Use grams until the user saves a different preference.
        String savedUnit = preferences.getString(KEY_DEFAULT_UNIT, "g");

        int savedPosition = unitAdapter.getPosition(savedUnit);
        unitSpinner.setSelection(savedPosition >= 0 ? savedPosition : 0);

        findViewById(R.id.btnSaveSettings).setOnClickListener(view -> {
            String selectedUnit = unitSpinner.getSelectedItem().toString();

            preferences.edit()
                    .putString(KEY_DEFAULT_UNIT, selectedUnit)
                    .apply();

            Toast.makeText(
                    this,
                    "Default unit saved: " + selectedUnit,
                    Toast.LENGTH_SHORT
            ).show();

            finish();
        });

        // Leave without changing the saved preference.
        findViewById(R.id.btnCancelSettings).setOnClickListener(view ->
                finish());
    }
}
# Dormbite

*Keep track of the ingredients you have at home.*

Dormbite is a Java Android app for students living alone. It tracks pantry ingredients and suggests meals you can make with the quantities you have. The interface uses mint cream backgrounds, green accents, and a cursive Dormbite heading.

Developed by Asanda Shezi for Mobile App Development 700 at Richfield. The project folder and Java package retain their original names: `SmartPantryManager` and `com.example.smartpantrymanager`.

## Features

- Add ingredients with a name, quantity, and unit: `g`, `kg`, `ml`, `l` or `pcs`.
- Display saved ingredients using a custom pantry list adapter.
- Tap an ingredient to edit it or request deletion, with confirmation before deleting.
- Validate missing names, missing quantities, and invalid or non-positive numbers.
- Suggest recipes only when every required ingredient is available in sufficient quantity.
- View recipe ingredients, quantities, and preparation instructions.
- Store 15 built-in recipes, including stove and microwave preparations.
- Save a default unit in Settings for new ingredients; users can change the unit on each form.
- Navigate using screen buttons and the pantry toolbar's three-dot menu.
- Keep pantry data and the saved default unit after closing and reopening the app.

## Technology and storage

The app uses Java, XML layouts, AndroidX, Material Components, and Gradle with Kotlin DSL build files.

**SQLite through `SQLiteOpenHelper`** stores pantry items and recipes locally. This suits the app because the data is structured and the core features should work offline without a database server, account, or API key. Database operations run in background threads to avoid blocking the interface.

The database is `smart_pantry.db`, version 2:

| Table | Purpose |
| --- | --- |
| `pantry` | Ingredient ID, name, quantity, and unit |
| `recipes` | Recipe ID, name and preparation instructions |
| `recipe_ingredients` | Required ingredients and quantities, linked to each recipe by `recipe_id` |

The database is created automatically. The upgrade from version 1 adds recipe storage while retaining existing pantry rows. Recipes are seeded when the recipe tables are first created.

**SharedPreferences** stores the default ingredient unit separately. Saving a preference affects new ingredient forms; it does not change existing pantry entries.

## Setup and run

1. Clone this repository, or download and extract its ZIP.
2. Open Android Studio and choose **Open**. Select the project root containing `settings.gradle.kts`, `gradlew`, and the `app` folder.
3. Allow Gradle to sync. Install any Android SDK components requested by the project. You need internet access for initial dependency and SDK downloads.
4. Use a Gradle JDK compatible with the project's Android Gradle plugin. Keep the included Gradle wrapper and build configuration when opening the project.
5. Start an Android emulator or connect an Android device with USB debugging enabled. The device must meet the `minSdk` specified in `app/build.gradle.kts`.
6. Select the **app** run configuration and the device, then click **Run**.
7. On a fresh installation, add pantry ingredients before opening **Suggested Recipes**. Recipes are supplied automatically; no SQL import is needed.

Manual development checks were performed on the Medium Phone emulator with API 37.1. The app needs no backend service or login to use its pantry and recipe features.

## Using Dormbite

| Screen or action | What to do |
| --- | --- |
| Pantry | View ingredients or tap a row for Edit/Delete options |
| Add Ingredient | Enter a name and positive quantity, choose a unit and save |
| Suggested Recipes | View meals that match the current pantry and tap a recipe |
| Recipe Details | Read the required quantities and preparation steps |
| Settings | Choose and save a default unit, or cancel without saving |
| Three-dot menu | Navigate to Pantry, Suggested Recipes or Settings |

## Recipe matching rules

Matching ignores capitalisation and extra spaces, and recognises a defined set of singular/plural names, such as `egg` and `eggs`. It converts kilograms to grams and litres to millilitres. Mass, volume, and pieces remain separate: grams are not converted to millilitres.

Duplicate pantry entries with matching names and compatible units are added together. Every requirement must be met, including water or oil where listed. Partial matches are excluded. Each recipe is checked independently; opening a recipe does not deduct ingredients from the pantry.

The matcher does not infer substitutions or arbitrary synonyms. For example, `oil` and `olive oil` are different ingredient names.

## Manual verification

Development checks covered ingredient validation, adding/editing/deleting items, persistence after Stop and Run, recipe matching, saving/cancelling the default unit preference, and the three toolbar menu actions.

To repeat the Simple Pancakes matching check, use these pantry totals:

| Ingredient | Quantity | Unit |
| --- | ---: | --- |
| Flour | 0.1 | kg |
| Milk | 0.2 | l |
| Eggs | 1 | pcs |
| Oil | 10 | ml |

1. Open Suggested Recipes: **Simple Pancakes** should appear.
2. Open it and check its ingredients and instructions.
3. Reduce the total flour to `0.099 kg`: Simple Pancakes should disappear.
4. Restore flour to `0.1 kg`: it should reappear.
5. Remove all egg entries: it should disappear again.
6. Restore the egg, stop and run the app, and confirm that the pantry remains saved.

For the Settings check, save `kg`, open Add Ingredient and check the selected unit. Cancel the form, change Settings to `ml` but cancel without saving, and confirm a new form still defaults to `kg`. Restart and check that the saved preference remains.

## Main source files

Java files are under `app/src/main/java/com/example/smartpantrymanager/`.

| File | Responsibility |
| --- | --- |
| `MainActivity.java` | Pantry display, edit/delete dialogs and navigation |
| `AddIngredientActivity.java` | Ingredient entry, validation and saving |
| `PantryAdapter.java` / `PantryItem.java` | Pantry row display and item data |
| `DatabaseHelper.java` | SQLite schema, migration and database operations |
| `Recipe.java` / `RecipeSeed.java` | Recipe data model and built-in recipes |
| `RecipeMatcher.java` | Ingredient and quantity comparison |
| `SuggestedRecipesActivity.java` | Loading and displaying matching recipes |
| `RecipeDetailActivity.java` | Displaying a selected recipe |
| `SettingsActivity.java` | Saving the default unit preference |

Layouts are in `app/src/main/res/layout/`. The toolbar menu is in `app/src/main/res/menu/pantry_menu.xml`. The manifest registers the screens, and the light/night theme resources define the app colours.

## Current scope

This version uses a fixed recipe collection. It has no recipe editor, expiry tracking, cloud sync, or automatic stock deduction after cooking. Stove/microwave instructions are part of individual recipes; there is no appliance filter. Pantry contents belong to the local installation, and clearing app storage removes that local data.

## Development references

- [Android Developers: Build and run your app](https://developer.android.com/studio/run)
- [Android Developers: Java versions in Android builds](https://developer.android.com/build/jdks)



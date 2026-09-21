package com.example.smartpantrymanager;

import android.content.ContentValues;
import android.database.sqlite.SQLiteDatabase;

/** Called only during database creation or the version 1 to 2 migration. */
final class RecipeSeed {
    private RecipeSeed() { }

    static void insertAll(SQLiteDatabase db) {
        add(db, "Scrambled Eggs",
                "1. Beat the eggs and milk together.\n2. Melt the butter in a pan over low heat.\n3. Add the mixture and gently stir until the eggs are fully set.",
                new Recipe.Ingredient("egg", 2, "pcs"),
                new Recipe.Ingredient("butter", 10, "g"),
                new Recipe.Ingredient("milk", 30, "ml"));

        add(db, "Tomato Omelette",
                "1. Chop the tomato and beat the eggs.\n2. Heat the oil and soften the tomato in the pan.\n3. Pour in the eggs and cook until fully set, then fold.",
                new Recipe.Ingredient("egg", 2, "pcs"),
                new Recipe.Ingredient("tomato", 1, "pcs"),
                new Recipe.Ingredient("oil", 10, "ml"));

        add(db, "Banana Porridge",
                "1. Put the oats and milk in a saucepan.\n2. Simmer gently, stirring, until the oats are soft.\n3. Peel and slice the banana, then stir it into the porridge.",
                new Recipe.Ingredient("oats", 50, "g"),
                new Recipe.Ingredient("milk", 250, "ml"),
                new Recipe.Ingredient("banana", 1, "pcs"));

        add(db, "Apple Porridge",
                "1. Core the apple and cut it into small pieces.\n2. Combine the apple, oats and milk in a saucepan.\n3. Simmer gently, stirring, until the oats and apple are soft.",
                new Recipe.Ingredient("oats", 50, "g"),
                new Recipe.Ingredient("milk", 250, "ml"),
                new Recipe.Ingredient("apple", 1, "pcs"));

        add(db, "Peanut Butter Toast",
                "1. Toast the two bread slices.\n2. Spread the peanut butter evenly over both slices.",
                new Recipe.Ingredient("bread", 2, "pcs"),
                new Recipe.Ingredient("peanut butter", 30, "g"));

        add(db, "Cheese Toast",
                "1. Spread the butter over the bread slices.\n2. Top with grated or sliced cheese.\n3. Place under a grill until the cheese melts and the bread is toasted.",
                new Recipe.Ingredient("bread", 2, "pcs"),
                new Recipe.Ingredient("cheese", 40, "g"),
                new Recipe.Ingredient("butter", 5, "g"));

        add(db, "Tomato Sandwich",
                "1. Slice the tomato.\n2. Butter the bread slices.\n3. Place the tomato between them and cut the sandwich in half.",
                new Recipe.Ingredient("bread", 2, "pcs"),
                new Recipe.Ingredient("tomato", 1, "pcs"),
                new Recipe.Ingredient("butter", 5, "g"));

        add(db, "Banana Toast",
                "1. Toast the bread.\n2. Spread with peanut butter.\n3. Peel and slice the banana, then arrange the slices on top.",
                new Recipe.Ingredient("bread", 2, "pcs"),
                new Recipe.Ingredient("banana", 1, "pcs"),
                new Recipe.Ingredient("peanut butter", 20, "g"));

        add(db, "Simple Pancakes",
                "1. Whisk the flour, milk and egg into a smooth batter.\n2. Heat a little of the measured oil in a non-stick pan.\n3. Add a thin layer of batter and cook until set, then turn and cook the other side.\n4. Repeat with the remaining batter and oil.",
                new Recipe.Ingredient("flour", 100, "g"),
                new Recipe.Ingredient("milk", 200, "ml"),
                new Recipe.Ingredient("egg", 1, "pcs"),
                new Recipe.Ingredient("oil", 10, "ml"));

        add(db, "French Toast",
                "1. Beat the egg and milk together.\n2. Dip both sides of each bread slice in the mixture.\n3. Melt the butter in a pan and cook the bread on both sides until golden and the egg is fully set.",
                new Recipe.Ingredient("bread", 2, "pcs"),
                new Recipe.Ingredient("egg", 1, "pcs"),
                new Recipe.Ingredient("milk", 50, "ml"),
                new Recipe.Ingredient("butter", 10, "g"));

        add(db, "Rice and Peas",
                "1. Heat the oil in a saucepan and stir in the rice.\n2. Add the measured water and bring to a gentle simmer.\n3. Cover and cook over low heat until the rice is almost tender.\n4. Stir in the peas and cook until the rice is tender and the peas are hot.",
                new Recipe.Ingredient("rice", 150, "g"),
                new Recipe.Ingredient("peas", 100, "g"),
                new Recipe.Ingredient("water", 300, "ml"),
                new Recipe.Ingredient("oil", 10, "ml"));

        add(db, "Tomato Rice",
                "1. Chop the onion and tomatoes.\n2. Heat the oil and cook the onion until soft, then stir in the tomatoes.\n3. Add the rice and measured water.\n4. Cover and simmer gently until the rice is tender and the liquid is absorbed.",
                new Recipe.Ingredient("rice", 150, "g"),
                new Recipe.Ingredient("tomato", 2, "pcs"),
                new Recipe.Ingredient("onion", 1, "pcs"),
                new Recipe.Ingredient("water", 300, "ml"),
                new Recipe.Ingredient("oil", 10, "ml"));

        add(db, "Mashed Potatoes",
                "1. Pierce the potatoes, place in a covered microwave-safe dish with a vent, and microwave until tender.\n2. Carefully peel if desired and mash.\n3. Stir in the milk and butter, then heat briefly if needed.",
                new Recipe.Ingredient("potato", 300, "g"),
                new Recipe.Ingredient("milk", 50, "ml"),
                new Recipe.Ingredient("butter", 15, "g"));

        add(db, "Carrot Potato Soup",
                "1. Chop the potato, carrot and onion into small pieces.\n2. Heat the oil in a saucepan and soften the onion.\n3. Add the potato, carrot and measured water.\n4. Simmer until the vegetables are tender, then mash lightly for a thicker soup.",
                new Recipe.Ingredient("potato", 200, "g"),
                new Recipe.Ingredient("carrot", 150, "g"),
                new Recipe.Ingredient("onion", 1, "pcs"),
                new Recipe.Ingredient("water", 600, "ml"),
                new Recipe.Ingredient("oil", 10, "ml"));

        add(db, "Tomato Pasta",
                "1. Bring the measured water to the boil and cook the pasta until tender, then drain.\n2. Chop the onion and tomatoes.\n3. Heat the oil in a pan and soften the onion.\n4. Add the tomatoes and simmer until soft, then stir in the pasta.",
                new Recipe.Ingredient("pasta", 150, "g"),
                new Recipe.Ingredient("tomato", 2, "pcs"),
                new Recipe.Ingredient("onion", 1, "pcs"),
                new Recipe.Ingredient("oil", 15, "ml"),
                new Recipe.Ingredient("water", 1000, "ml"));

    }

    private static void add(SQLiteDatabase db, String name, String steps,
                            Recipe.Ingredient... ingredients) {
        ContentValues recipe = new ContentValues();
        recipe.put("name", name);
        recipe.put("instructions", steps);
        long recipeId = db.insertOrThrow("recipes", null, recipe);
        for (Recipe.Ingredient ingredient : ingredients) {
            ContentValues row = new ContentValues();
            row.put("recipe_id", recipeId);
            row.put("name", ingredient.getName());
            row.put("quantity", ingredient.getQuantity());
            row.put("unit", ingredient.getUnit());
            db.insertOrThrow("recipe_ingredients", null, row);
        }
    }
}

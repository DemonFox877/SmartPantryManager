package com.example.smartpantrymanager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** One stored recipe and the quantities required to make it once. */
public final class Recipe {
    private final long id;
    private final String name;
    private final String instructions;
    private final List<Ingredient> ingredients;

    public Recipe(long id, String name, String instructions,
                  List<Ingredient> ingredients) {
        this.id = id;
        this.name = name;
        this.instructions = instructions;
        this.ingredients = Collections.unmodifiableList(new ArrayList<>(ingredients));
    }

    public long getId() { return id; }
    public String getName() { return name; }
    public String getInstructions() { return instructions; }
    public List<Ingredient> getIngredients() { return ingredients; }

    public static final class Ingredient {
        private final String name;
        private final double quantity;
        private final String unit;

        public Ingredient(String name, double quantity, String unit) {
            this.name = name;
            this.quantity = quantity;
            this.unit = unit;
        }

        public String getName() { return name; }
        public double getQuantity() { return quantity; }
        public String getUnit() { return unit; }
    }
}

package com.example.smartpantrymanager;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/** Pure Java matching logic; no Android classes or database access here. */
public final class RecipeMatcher {
    private RecipeMatcher() { }

    public static boolean canMake(Recipe recipe, List<PantryItem> pantry) {
        if (recipe.getIngredients().isEmpty()) return false;

        Map<String, BigDecimal> available = new HashMap<>();
        for (PantryItem item : pantry) {
            addAmount(available, item.getName(), item.getQuantity(), item.getUnit());
        }

        Map<String, BigDecimal> required = new HashMap<>();
        for (Recipe.Ingredient ingredient : recipe.getIngredients()) {
            if (!addAmount(required, ingredient.getName(),
                    ingredient.getQuantity(), ingredient.getUnit())) {
                return false;
            }
        }

        // Repeated recipe ingredients are added together before comparison.
        for (Map.Entry<String, BigDecimal> entry : required.entrySet()) {
            BigDecimal amount = available.get(entry.getKey());
            if (amount == null || amount.compareTo(entry.getValue()) < 0) return false;
        }
        return true;
    }

    private static boolean addAmount(Map<String, BigDecimal> totals,
                                     String name, double quantity, String unit) {
        if (Double.isNaN(quantity) || Double.isInfinite(quantity) || quantity <= 0) {
            return false;
        }
        String ingredient = normalizeName(name);
        String baseUnit = baseUnit(unit);
        if (ingredient.isEmpty() || baseUnit == null) return false;

        BigDecimal amount = BigDecimal.valueOf(quantity);
        String cleanedUnit = clean(unit);
        if (cleanedUnit.equals("kg") || cleanedUnit.equals("l")) {
            amount = amount.multiply(BigDecimal.valueOf(1000));
        }

        // Mass, volume and pieces remain separate even for the same ingredient.
        String key = ingredient + "|" + baseUnit;
        BigDecimal previous = totals.get(key);
        totals.put(key, previous == null ? amount : previous.add(amount));
        return true;
    }

    private static String baseUnit(String unit) {
        switch (clean(unit)) {
            case "g": case "kg": return "g";
            case "ml": case "l": return "ml";
            case "pcs": return "pcs";
            default: return null;
        }
    }

    public static String normalizeName(String name) {
        String cleaned = clean(name);
        // Explicit aliases avoid damaging words such as "peas" and "oats".
        switch (cleaned) {
            case "eggs": return "egg";
            case "tomatoes": return "tomato";
            case "potatoes": return "potato";
            case "onions": return "onion";
            case "carrots": return "carrot";
            case "bananas": return "banana";
            case "apples": return "apple";
            case "pea": return "peas";
            case "oat": return "oats";
            default: return cleaned;
        }
    }

    private static String clean(String text) {
        return text == null ? "" : text.trim().toLowerCase(Locale.ROOT)
                .replaceAll("\\s+", " ");
    }
}

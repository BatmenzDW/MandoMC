package com.astuteflamez.mandomc.features.items;

import com.astuteflamez.mandomc.MandoMC;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.BlastingRecipe;
import org.bukkit.inventory.ItemStack;

import java.util.Set;

public class BlastingRecipes {

    private final MandoMC plugin;
    private final ItemsManager itemsManager;

    public BlastingRecipes(MandoMC plugin, ItemsManager itemsManager) {
        this.plugin = plugin;
        this.itemsManager = itemsManager;
    }

    public void register() {
        // ✅ Get all custom item IDs from ItemsManager
        Set<String> allItemIds = itemsManager.getAllItemIds();
        Bukkit.getLogger().info("[DEBUG] Found item IDs: " + allItemIds);
        // ⬆️ Make sure ItemsManager has a method to return all IDs from items.yml

        for (String itemId : allItemIds) {
            // ✅ Only handle items that end with "_sheet"
            if (!itemId.toLowerCase().endsWith("_sheet")) continue;

            // ✅ Output is the sheet itself
            ItemStack outputSheet = itemsManager.getItem(itemId);
            if (outputSheet == null) {
                Bukkit.getLogger().warning("[MandoMC] Could not find sheet item for: " + itemId);
                continue;
            }

            // ✅ Find base material by stripping "_sheet"
            String baseId = itemId.substring(0, itemId.length() - "_sheet".length());
            ItemStack baseMaterial = itemsManager.getItem(baseId);
            if (baseMaterial == null) {
                Bukkit.getLogger().warning("[MandoMC] Could not find base material for: " + baseId);
                continue;
            }

            // ✅ Create NamespacedKey automatically based on sheet name
            NamespacedKey key = new NamespacedKey(plugin, baseId + "_to_sheet");

            // ✅ Create and register blasting recipe
            BlastingRecipe blastingRecipe = new BlastingRecipe(
                    key,
                    outputSheet,
                    ItemsManager.exactChoice(baseMaterial),
                    1.0f,   // XP reward
                    100     // 5 seconds (blast furnaces are faster)
            );

            Bukkit.addRecipe(blastingRecipe);
            Bukkit.getLogger().info("[MandoMC] Registered blasting recipe: " + baseId + " → " + itemId);
        }
    }
}

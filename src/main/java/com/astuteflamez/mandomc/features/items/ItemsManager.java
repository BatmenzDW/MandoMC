package com.astuteflamez.mandomc.features.items;

import com.astuteflamez.mandomc.features.sabers.SaberManager;
import org.bukkit.Bukkit;
import org.bukkit.Keyed;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.*;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.Plugin;

import java.util.*;

import static org.bukkit.ChatColor.translateAlternateColorCodes;

public class ItemsManager {

    private final Plugin plugin;
    private final SaberManager saberManager;
    private final Map<String, ItemStack> loadedItems = new HashMap<>();

    public ItemsManager(Plugin plugin) {
        this.plugin = plugin;
        this.saberManager = new SaberManager(plugin);
    }

    /**
     * ✅ Loads all items fresh from items.yml
     */
    public void loadItems() {
        loadedItems.clear(); // ✅ clear cache
        plugin.getLogger().info("🔄 [ItemsManager] Reloading items...");

        ConfigurationSection section = ItemsConfig.get().getConfigurationSection("items");
        if (section == null) {
            plugin.getLogger().warning("⚠ No items found in items.yml");
            return;
        }

        for (String itemId : section.getKeys(false)) {
            ItemStack item = createItem(itemId);
            if (item != null) {
                loadedItems.put(itemId, item);
            }
        }

        plugin.getLogger().info("✅ [ItemsManager] Loaded " + loadedItems.size() + " items.");
    }

    /**
     * ✅ Creates a single item from items.yml
     */
    private ItemStack createItem(String itemId) {
        ConfigurationSection sec = ItemsConfig.get().getConfigurationSection("items." + itemId);
        if (sec == null) return null;

        String name = sec.getString("name", "&fUnknown Item");
        String typeName = sec.getString("item_type", "STONE");
        String speciality = sec.getString("speciality", "MATERIAL").toUpperCase(Locale.ROOT);

        Material type = Material.matchMaterial(typeName);
        if (type == null) {
            plugin.getLogger().warning("⚠ [ItemsManager] Item '" + itemId + "' has unknown item_type '" + typeName + "'. Skipping.");
            return null;
        }

        ItemStack item = new ItemStack(type);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(color(name));

            // ⬇️ Get Rarity & Category from items.yml
            String rarity = sec.getString("Rarity", "Common");
            String category = sec.getString("Category", "Material");

            // ⬇️ Map to Unicode icon codes (bitmap font)
            Map<String, String> rarityIcons = Map.of(
                    "Common", "\u0111",
                    "Uncommon", "\u0116",
                    "Rare", "\u0115",
                    "Epic", "\u0112",
                    "Legendary", "\u0113",
                    "Mythic", "\u0114"
            );

            Map<String, String> categoryIcons = Map.of(
                    "Armor", "\u0109",
                    "Component", "\u010A",
                    "Consumable", "\u010B",
                    "Material", "\u010C",
                    "Metal", "\u010D",
                    "Valuable", "\u010E",
                    "Vehicle", "\u010F",
                    "Weapon", "\u0110",
                    "Fuel", "\u0122"
            );

            // ⬇️ Build first lore line using ONLY icons
            String rarityIcon = rarityIcons.getOrDefault(rarity, "");
            String categoryIcon = categoryIcons.getOrDefault(category, "");

            // Force color reset (white) so icons aren’t tinted by previous formatting
            String firstLoreLine = "§r§f" + rarityIcon + "" + categoryIcon;

            // ⬇️ Build lore list
            List<String> loreLines = new ArrayList<>();
            loreLines.add(firstLoreLine); // ✅ always first line

            // ⬇️ Add other lore lines from items.yml or saber lore
            if (sec.contains("lore") && !sec.getStringList("lore").isEmpty()) {
                for (String line : sec.getStringList("lore")) {
                    loreLines.add(color(line));
                }
            } else if (speciality.equals("SABER")) {
                double damage = sec.getDouble("damage", 5);
                loreLines.addAll(saberManager.generateSaberLore(damage));
            }


            meta.setLore(loreLines);

            // ✅ Custom model data
            if (sec.contains("custom-model-data")) {
                meta.setCustomModelData(sec.getInt("custom-model-data"));
            }

            meta.setUnbreakable(true);
            meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES, ItemFlag.HIDE_UNBREAKABLE);
            item.setItemMeta(meta);
        }

        if (speciality.equals("SABER")) {
            item = saberManager.applySaberAttributes(item, itemId, sec);
        }

        return item;
    }

    /**
     * ✅ Registers all recipes from items.yml
     */
    public void registerRecipes() {
        plugin.getLogger().info("🔄 [ItemsManager] Registering recipes...");

        // ✅ Remove old plugin recipes
        Iterator<Recipe> it = Bukkit.recipeIterator();
        while (it.hasNext()) {
            Recipe recipe = it.next();
            if (recipe instanceof Keyed) {
                NamespacedKey key = ((Keyed) recipe).getKey();
                if (key.getNamespace().equals(plugin.getName().toLowerCase())) {
                    it.remove();
                }
            }
        }

        ConfigurationSection section = ItemsConfig.get().getConfigurationSection("items");
        if (section == null) return;

        for (String itemId : section.getKeys(false)) {
            ConfigurationSection sec = section.getConfigurationSection(itemId);
            if (sec == null || !sec.contains("recipe")) continue;

            ItemStack result = getItem(itemId);
            if (result == null) continue;

            List<String> recipeLines = sec.getStringList("recipe");
            if (recipeLines.size() != 3) continue;

            NamespacedKey key = new NamespacedKey(plugin, itemId.toLowerCase());
            ShapedRecipe recipe = new ShapedRecipe(key, result);
            recipe.shape(recipeLines.get(0), recipeLines.get(1), recipeLines.get(2));

            ConfigurationSection ingSec = sec.getConfigurationSection("ingredients");
            if (ingSec == null) continue;

            for (String symbol : ingSec.getKeys(false)) {
                String value = ingSec.getString(symbol);
                if (value == null) continue;

                if (loadedItems.containsKey(value)) {
                    recipe.setIngredient(symbol.charAt(0), new RecipeChoice.ExactChoice(loadedItems.get(value)));
                } else {
                    Material mat = Material.matchMaterial(value);
                    if (mat != null) {
                        recipe.setIngredient(symbol.charAt(0), mat);
                    }
                }
            }

            Bukkit.addRecipe(recipe);
            plugin.getLogger().info("✅ Registered recipe: " + itemId);
        }
    }

    public ItemStack getItem(String id) {
        return loadedItems.get(id) != null ? loadedItems.get(id).clone() : null;
    }

    public static org.bukkit.inventory.RecipeChoice.ExactChoice exactChoice(ItemStack item) {
        return new org.bukkit.inventory.RecipeChoice.ExactChoice(item);
    }

    public Set<String> getAllItemIds() {
        return ItemsConfig.get().getConfigurationSection("items").getKeys(false);
    }

    private String color(String text) {
        return translateAlternateColorCodes('&', text);
    }
}

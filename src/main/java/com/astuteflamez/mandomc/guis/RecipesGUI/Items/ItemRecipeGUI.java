package com.astuteflamez.mandomc.guis.RecipesGUI.Items;

import com.astuteflamez.mandomc.MandoMC;
import com.astuteflamez.mandomc.guis.GUIManager;
import com.astuteflamez.mandomc.guis.InventoryButton;
import com.astuteflamez.mandomc.guis.InventoryGUI;
import com.astuteflamez.mandomc.features.items.ItemsConfig;
import com.astuteflamez.mandomc.guis.RecipesGUI.RecipesHub;
import com.astuteflamez.mandomc.features.items.ItemsManager;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.*;

public class ItemRecipeGUI extends InventoryGUI {

    private final GUIManager guiManager;
    private final String itemId;
    private final String parentId;

    public ItemRecipeGUI(GUIManager guiManager, String itemId) {
        this(guiManager, itemId, null);
    }

    public ItemRecipeGUI(GUIManager guiManager, String itemId, String parentId) {
        this.guiManager = guiManager;
        this.itemId = itemId;
        this.parentId = parentId;
    }

    @Override
    protected org.bukkit.inventory.Inventory createInventory() {
        return Bukkit.createInventory(null, 5 * 9,
                LegacyComponentSerializer.legacyAmpersand().deserialize("&a&lRecipe Viewer"));
    }

    @Override
    public void decorate(Player player) {
        ItemsManager itemsManager = MandoMC.getInstance().getItemsManager();
        ItemStack displayItem = itemsManager.getItem(itemId);

        if (displayItem == null) {
            displayItem = new ItemStack(Material.BARRIER);
            ItemMeta meta = displayItem.getItemMeta();
            meta.setDisplayName("§cInvalid Item: " + itemId);
            displayItem.setItemMeta(meta);
        }

        // ✅ Auto-detect "sheet" items for smelting UI
        if (itemId.toLowerCase().contains("sheet")) {
            showSmeltingRecipe(itemsManager, displayItem);
            super.decorate(player);
            return;
        }

        // ✅ Standard crafting recipe layout
        List<String> recipe = ItemsConfig.getRecipe(itemId);
        Map<Character, String> ingredients = ItemsConfig.getIngredients(itemId);

        int[] gridSlots = {10, 11, 12, 19, 20, 21, 28, 29, 30};
        int index = 0;
        Set<Integer> usedSlots = new HashSet<>();

        for (String row : recipe) {
            for (char c : row.toCharArray()) {
                int slot = gridSlots[index++];
                ItemStack item;
                if (c == ' ' || !ingredients.containsKey(c)) {
                    item = new ItemStack(Material.AIR);
                } else {
                    String ingredientId = ingredients.get(c);
                    item = parseIngredient(ingredientId, itemsManager);
                    this.addButton(slot, createIngredientButton(item, ingredientId));
                }
                usedSlots.add(slot);
            }
        }

        // final product in the center
        usedSlots.add(24);
        this.addButton(24, createDullButton(displayItem));

        addBackButtonAndFillers(usedSlots);
        super.decorate(player);
    }

    /**
     * ✅ Shows a Blast Furnace smelting recipe for any "*_sheet" item.
     */
    private void showSmeltingRecipe(ItemsManager itemsManager, ItemStack output) {
        // Infer raw material by removing "_sheet"
        String baseId = itemId.replace("_sheet", "");
        ItemStack inputItem = itemsManager.getItem(baseId);

        if (inputItem == null) {
            inputItem = new ItemStack(Material.BARRIER);
            ItemMeta meta = inputItem.getItemMeta();
            meta.setDisplayName("§cMissing base: " + baseId);
            inputItem.setItemMeta(meta);
        }

        // Furnace icon
        ItemStack blastFurnace = new ItemStack(Material.BLAST_FURNACE);
        ItemMeta furnaceMeta = blastFurnace.getItemMeta();
        furnaceMeta.setDisplayName("§6Blast Furnace");
        blastFurnace.setItemMeta(furnaceMeta);

        // Layout in GUI
        this.addButton(20, createDullButton(inputItem));     // Left slot: input item
        this.addButton(22, createDullButton(blastFurnace));  // Middle slot: furnace icon
        this.addButton(24, createDullButton(output));        // Right slot: output sheet

        Set<Integer> used = Set.of(20, 22, 24);
        addBackButtonAndFillers(used);
    }

    private InventoryButton createIngredientButton(ItemStack itemStack, String ingredientId) {
        return new InventoryButton()
                .creator(player -> itemStack)
                .consumer(event -> {
                    Player clicker = (Player) event.getWhoClicked();
                    if (ItemsConfig.hasRecipe(ingredientId) || ingredientId.toLowerCase().contains("sheet")) {
                        guiManager.openGUI(new ItemRecipeGUI(guiManager, ingredientId, itemId), clicker);
                    } else {
                        clicker.sendMessage("§7No recipe available for §e" + ingredientId);
                    }
                });
    }

    private InventoryButton createDullButton(ItemStack itemStack) {
        return new InventoryButton()
                .creator(player -> itemStack)
                .consumer(event -> {});
    }

    private InventoryButton createBackButton(ItemStack itemStack) {
        return new InventoryButton()
                .creator(player -> itemStack)
                .consumer(event -> {
                    Player player = (Player) event.getWhoClicked();
                    if (parentId != null) {
                        guiManager.openGUI(new ItemRecipeGUI(guiManager, parentId), player);
                    } else {
                        guiManager.openGUI(new RecipesHub(guiManager), player);
                    }
                });
    }

    private ItemStack parseIngredient(String id, ItemsManager itemsManager) {
        ItemStack custom = itemsManager.getItem(id);
        if (custom != null) return custom;

        try {
            Material material = Material.valueOf(id.toUpperCase());
            return new ItemStack(material);
        } catch (IllegalArgumentException e) {
            Bukkit.getLogger().warning("[MandoMC] Invalid ingredient in items.yml: " + id);
            return new ItemStack(Material.BARRIER);
        }
    }

    /**
     * ✅ Utility to fill unused slots & add back button
     */
    private void addBackButtonAndFillers(Set<Integer> usedSlots) {
        // Back button
        ItemStack back = new ItemStack(Material.ARROW);
        ItemMeta backMeta = back.getItemMeta();
        backMeta.setDisplayName(parentId != null ? "§c§lBack" : "§c§lReturn to Hub");
        back.setItemMeta(backMeta);
        this.addButton(44, createBackButton(back));

        // Fillers
        ItemStack filler = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
        ItemMeta fillerMeta = filler.getItemMeta();
        fillerMeta.setDisplayName(" ");
        filler.setItemMeta(fillerMeta);

        for (int i = 0; i < 45; i++) {
            if (!usedSlots.contains(i) && i != 44) {
                this.addButton(i, new InventoryButton()
                        .creator(p -> filler)
                        .consumer(e -> {}));
            }
        }
    }
}

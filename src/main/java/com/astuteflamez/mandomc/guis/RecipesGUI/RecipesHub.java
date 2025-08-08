package com.astuteflamez.mandomc.guis.RecipesGUI;

import com.astuteflamez.mandomc.MandoMC;
import com.astuteflamez.mandomc.guis.GUIManager;
import com.astuteflamez.mandomc.guis.InventoryButton;
import com.astuteflamez.mandomc.guis.InventoryGUI;
import com.astuteflamez.mandomc.guis.RecipesGUI.Items.AmmoRecipeGUI;
import com.astuteflamez.mandomc.guis.RecipesGUI.Items.ItemRecipeGUI;
import com.astuteflamez.mandomc.features.items.ItemsManager;
import com.astuteflamez.mandomc.features.items.ItemsConfig;
import me.deecaad.weaponmechanics.WeaponMechanicsAPI;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class RecipesHub extends InventoryGUI {

    private final GUIManager guiManager;

    public RecipesHub(GUIManager guiManager) {
        this.guiManager = guiManager;
    }

    @Override
    protected org.bukkit.inventory.Inventory createInventory() {
        return org.bukkit.Bukkit.createInventory(null, 6 * 9,
                LegacyComponentSerializer.legacyAmpersand().deserialize("&a&lRecipes"));
    }

    @Override
    public void decorate(Player player) {
        ItemsManager itemsManager = MandoMC.getInstance().getItemsManager();

        // Grab all items from items.yml
        ConfigurationSection section = ItemsConfig.get().getConfigurationSection("items");
        if (section == null) {
            player.sendMessage("§cNo items found in items.yml.");
            return;
        }

        int slot = 0;
        for (String itemId : section.getKeys(false)) {
            boolean hasCraftingRecipe = ItemsConfig.hasRecipe(itemId);
            boolean isSmeltedSheet = itemId.toLowerCase().contains("sheet");

            // ✅ Only add items that are either craftable OR smeltable sheets
            if (!hasCraftingRecipe && !isSmeltedSheet) continue;

            ItemStack item = itemsManager.getItem(itemId);
            if (item == null) continue;

            // ✅ Add a button for the item
            this.addButton(slot, createItemButton(item, itemId));

            slot++;
            if (slot >= this.getInventory().getSize()) break; // Prevent overflow
        }

        // Add ammo recipes directly from AmmoRecipes.java
        //this.addButton(slot++, createAmmoButton("Standard_Energy_Cell"));
        //this.addButton(slot++, createAmmoButton("Hyper_Energy_Cell"));
        //this.addButton(slot++, createAmmoButton("Dense_Energy_Cell"));

        super.decorate(player);
    }

    private InventoryButton createItemButton(ItemStack itemStack, String itemId) {
        return new InventoryButton()
                .creator(player -> itemStack)
                .consumer(event -> {
                    Player player = (Player) event.getWhoClicked();
                    guiManager.openGUI(new ItemRecipeGUI(guiManager, itemId), player);
                });
    }

    private InventoryButton createAmmoButton(String ammoKey) {
        ItemStack ammo = WeaponMechanicsAPI.generateAmmo(ammoKey, false);
        ammo.setAmount(64);

        return new InventoryButton()
                .creator(player -> ammo)
                .consumer(event -> {
                    Player p = (Player) event.getWhoClicked();
                    guiManager.openGUI(new AmmoRecipeGUI(guiManager, ammoKey), p);
                });
    }

}

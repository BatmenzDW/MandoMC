package com.astuteflamez.mandomc.guis.RecipesGUI.Items;

import com.astuteflamez.mandomc.MandoMC;
import com.astuteflamez.mandomc.guis.GUIManager;
import com.astuteflamez.mandomc.guis.InventoryButton;
import com.astuteflamez.mandomc.guis.InventoryGUI;
import com.astuteflamez.mandomc.guis.RecipesGUI.RecipesHub;
import com.astuteflamez.mandomc.features.items.ItemsManager;
import me.deecaad.weaponmechanics.WeaponMechanicsAPI;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.*;

public class AmmoRecipeGUI extends InventoryGUI {

    private final GUIManager guiManager;
    private final String ammoKey;
    private final String parentId;

    public AmmoRecipeGUI(GUIManager guiManager, String ammoKey) {
        this(guiManager, ammoKey, null);
    }

    public AmmoRecipeGUI(GUIManager guiManager, String ammoKey, String parentId) {
        this.guiManager = guiManager;
        this.ammoKey = ammoKey;
        this.parentId = parentId;
    }

    @Override
    protected org.bukkit.inventory.Inventory createInventory() {
        return Bukkit.createInventory(null, 5 * 9,
                LegacyComponentSerializer.legacyAmpersand().deserialize("&b&lAmmo Recipe Viewer"));
    }

    @Override
    public void decorate(Player player) {
        ItemsManager itemsManager = MandoMC.getInstance().getItemsManager();
        ItemStack ammo = WeaponMechanicsAPI.generateAmmo(ammoKey, false);
        ammo.setAmount(64);

        // Select proper recipe
        Map<Character, String> ingredients = switch (ammoKey) {
            case "Standard_Energy_Cell" -> Map.of(
                    'P', "plastoid",
                    'E', "energy_conduit",
                    'B', "batteries"
            );
            case "Hyper_Energy_Cell" -> Map.of(
                    'P', "plastoid",
                    'E', "energy_conduit",
                    'B', "glitterstim_fibers"
            );
            case "Dense_Energy_Cell" -> Map.of(
                    'P', "plastoid",
                    'E', "energy_conduit",
                    'B', "isotope_5"
            );
            default -> new HashMap<>();
        };

        String[] layout = {"PEP", "EBE", "PEP"};
        int[] gridSlots = {10, 11, 12, 19, 20, 21, 28, 29, 30};
        int index = 0;
        Set<Integer> usedSlots = new HashSet<>();

        for (String row : layout) {
            for (char c : row.toCharArray()) {
                int slot = gridSlots[index++];
                if (c == ' ' || !ingredients.containsKey(c)) continue;

                String ingredientKey = ingredients.get(c);
                ItemStack ingredientItem = itemsManager.getItem(ingredientKey);

                if (ingredientItem != null) {
                    this.addButton(slot, createIngredientButton(ingredientItem, ingredientKey));
                    usedSlots.add(slot);
                }
            }
        }

        // Output stack of 64 ammo in center
        this.addButton(24, createDullButton(ammo));
        usedSlots.add(24);

        addBackButtonAndFillers(usedSlots);
        super.decorate(player);
    }

    private InventoryButton createIngredientButton(ItemStack itemStack, String itemId) {
        return new InventoryButton()
                .creator(player -> itemStack)
                .consumer(event -> {
                    Player clicker = (Player) event.getWhoClicked();
                    clicker.sendMessage("§7No recipe page for §e" + itemId); // Avoid recursion
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
                        guiManager.openGUI(new AmmoRecipeGUI(guiManager, parentId), player);
                    } else {
                        guiManager.openGUI(new RecipesHub(guiManager), player);
                    }
                });
    }

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

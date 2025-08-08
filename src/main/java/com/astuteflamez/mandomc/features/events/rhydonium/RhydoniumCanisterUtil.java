package com.astuteflamez.mandomc.features.events.rhydonium;

import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.util.List;

public class RhydoniumCanisterUtil {

    private static final NamespacedKey CANISTER_KEY = new NamespacedKey("mandomc", "canister_fuel");

    public static boolean isValidCanister(ItemStack item) {
        if (item == null || item.getType() != Material.SADDLE) return false;
        if (!item.hasItemMeta()) return false;

        ItemMeta meta = item.getItemMeta();
        if (!meta.hasCustomModelData()) return false;

        int cmd = meta.getCustomModelData();
        boolean valid = cmd >= 6 && cmd <= 10;
        return valid;
    }

    public static void setFuel(ItemStack item, int fuel) {
        if (!isValidCanister(item)) return;

        ItemMeta meta = item.getItemMeta();
        PersistentDataContainer container = meta.getPersistentDataContainer();
        container.set(CANISTER_KEY, PersistentDataType.INTEGER, fuel);

        // ✅ Preserve existing lore
        List<String> existingLore = meta.hasLore() ? meta.getLore() : null;

        // ✅ Determine color and CMD
        String color;
        int cmd;

        if (fuel >= 100) {
            color = "§2"; // Dark Green
            cmd = 10;
        } else if (fuel >= 75) {
            color = "§a"; // Green
            cmd = 9;
        } else if (fuel >= 50) {
            color = "§e"; // Yellow
            cmd = 8;
        } else if (fuel >= 25) {
            color = "§6"; // Gold
            cmd = 7;
        } else {
            color = "§c"; // Red
            cmd = 6;
        }

        meta.setCustomModelData(cmd);
        meta.setDisplayName(color + "Rhydonium Canister §7[" + fuel + "/100]");

        if (existingLore != null) meta.setLore(existingLore);
        item.setItemMeta(meta);

    }

    public static int getFuel(ItemStack item) {
        if (!isValidCanister(item)) return 0;
        ItemMeta meta = item.getItemMeta();
        return meta.getPersistentDataContainer().getOrDefault(CANISTER_KEY, PersistentDataType.INTEGER, 0);
    }

    public static int getMaxFuel() {
        return 100;
    }
}

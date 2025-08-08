package com.astuteflamez.mandomc.features.sabers;

import com.astuteflamez.mandomc.features.items.ItemsManager;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.inventory.ItemStack;

public class SaberToggleListener implements Listener {

    private final ItemsManager itemsManager;

    public SaberToggleListener(ItemsManager itemsManager) {
        this.itemsManager = itemsManager;
    }

    @EventHandler
    public void onHotbarSwitch(PlayerItemHeldEvent event) {
        Player player = event.getPlayer();

        // Slot the player is switching to
        int newSlot = event.getNewSlot();
        int prevSlot = event.getPreviousSlot();

        ItemStack newItem = player.getInventory().getItem(newSlot);
        ItemStack prevItem = player.getInventory().getItem(prevSlot);

        // Handle toggling ON
        if (newItem != null && newItem.getType() == Material.SADDLE && newItem.hasItemMeta() && newItem.getItemMeta().hasCustomModelData()) {
            String itemId = getItemIdByCMD(newItem.getItemMeta().getCustomModelData(), newItem.getType());
            if (itemId != null && itemId.endsWith("_hilt")) {
                String saberId = itemId.replace("_hilt", "");
                ItemStack saber = itemsManager.getItem(saberId);
                if (saber != null) {
                    player.getInventory().setItem(newSlot, saber);
                    player.playSound(player.getLocation(), "melee.lightsaber.on", 1.0f, 1.0f);
                }
            }
        }

        // Handle toggling OFF
        if (prevItem != null && prevItem.getType() == Material.SHIELD && prevItem.hasItemMeta() && prevItem.getItemMeta().hasCustomModelData()) {
            String itemId = getItemIdByCMD(prevItem.getItemMeta().getCustomModelData(), prevItem.getType());
            if (itemId != null && !itemId.endsWith("_hilt")) {
                ItemStack hilt = itemsManager.getItem(itemId + "_hilt");
                if (hilt != null) {
                    player.getInventory().setItem(prevSlot, hilt);
                    player.playSound(player.getLocation(), "melee.lightsaber.off", 1.0f, 1.0f);
                }
            }
        }
    }

    private String getItemIdByCMD(int cmd, Material mat) {
        for (String id : itemsManager.getAllItemIds()) {
            ItemStack loaded = itemsManager.getItem(id);
            if (loaded == null || loaded.getItemMeta() == null) continue;

            if (loaded.getType() == mat &&
                    loaded.getItemMeta().hasCustomModelData() &&
                    loaded.getItemMeta().getCustomModelData() == cmd) {
                return id;
            }
        }
        return null;
    }
}

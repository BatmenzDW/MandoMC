package com.astuteflamez.mandomc.features.events.chesthunt;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class ChestHuntWand implements Listener {

    public static final String WAND_NAME = ChatColor.GOLD + "ChestHunt Wand";

    // Create the wand item
    public static ItemStack createWand() {
        ItemStack stick = new ItemStack(Material.BLAZE_ROD);
        ItemMeta meta = stick.getItemMeta();
        meta.setDisplayName(WAND_NAME);
        stick.setItemMeta(meta);
        return stick;
    }

    // Listen for right-click block with wand
    @EventHandler
    public void onRightClick(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK && event.getAction() != Action.LEFT_CLICK_BLOCK) return;

        Player player = event.getPlayer();
        ItemStack item = event.getItem();

        if (item == null || !item.hasItemMeta()) return;
        if (!WAND_NAME.equals(item.getItemMeta().getDisplayName())) return;

        Location loc = event.getClickedBlock().getLocation();

        if (event.getAction() == Action.RIGHT_CLICK_BLOCK) {
            ChestLocationManager.getInstance().addLocation(loc);
            player.sendMessage(ChatColor.GREEN + "[ChestHunt] Location added: " + formatLoc(loc));
        } else if (event.getAction() == Action.LEFT_CLICK_BLOCK) {
            ChestLocationManager.getInstance().removeLocation(loc);
            player.sendMessage(ChatColor.RED + "[ChestHunt] Location removed: " + formatLoc(loc));
        }

        event.setCancelled(true);
    }

    private String formatLoc(Location loc) {
        return String.format("%s (%.0f, %.0f, %.0f)",
                loc.getWorld().getName(), loc.getX(), loc.getY(), loc.getZ());
    }
}

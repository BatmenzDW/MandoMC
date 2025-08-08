// ✅ OreEventWand.java
package com.astuteflamez.mandomc.features.events.beskar;

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

public class OreEventWand implements Listener {

    public static final String WAND_NAME = ChatColor.GOLD + "Ore Event Wand";

    public static ItemStack createWand() {
        ItemStack stick = new ItemStack(Material.BLAZE_ROD);
        ItemMeta meta = stick.getItemMeta();
        meta.setDisplayName(WAND_NAME);
        stick.setItemMeta(meta);
        return stick;
    }

    @EventHandler
    public void onClick(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK && event.getAction() != Action.LEFT_CLICK_BLOCK) return;

        Player player = event.getPlayer();
        ItemStack item = event.getItem();

        if (item == null || !item.hasItemMeta()) return;
        if (!WAND_NAME.equals(item.getItemMeta().getDisplayName())) return;

        Location loc = event.getClickedBlock().getLocation();

        if (event.getAction() == Action.RIGHT_CLICK_BLOCK) {
            OreLocationManager.getInstance().addLocation(loc);
            player.sendMessage(ChatColor.GREEN + "[OreEvent] Location added: " + format(loc));
        } else {
            OreLocationManager.getInstance().removeLocation(loc);
            player.sendMessage(ChatColor.RED + "[OreEvent] Location removed: " + format(loc));
        }

        event.setCancelled(true);
    }

    private String format(Location loc) {
        return String.format("%s (%.0f, %.0f, %.0f)", loc.getWorld().getName(), loc.getX(), loc.getY(), loc.getZ());
    }
}
package com.astuteflamez.mandomc.features.events.rhydonium;

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

public class RhydoniumWand implements Listener {

    private static final String NAME = ChatColor.RED + "Rhydonium Wand";

    public static ItemStack create() {
        ItemStack wand = new ItemStack(Material.BLAZE_ROD);
        ItemMeta meta = wand.getItemMeta();
        meta.setDisplayName(NAME);
        wand.setItemMeta(meta);
        return wand;
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
        if (event.getItem() == null || !event.getItem().hasItemMeta()) return;
        if (!NAME.equals(event.getItem().getItemMeta().getDisplayName())) return;

        Player player = event.getPlayer();
        Location loc = event.getClickedBlock() != null ? event.getClickedBlock().getLocation() : null;
        if (loc == null) return;

        if (event.getAction() == Action.RIGHT_CLICK_BLOCK) {
            RhydoniumLocationManager.getInstance().add(loc);
            player.sendMessage(ChatColor.GREEN + "Added Rhydonium location: " + format(loc));
        } else if (event.getAction() == Action.LEFT_CLICK_BLOCK) {
            RhydoniumLocationManager.getInstance().remove(loc);
            player.sendMessage(ChatColor.RED + "Removed Rhydonium location: " + format(loc));
        }

        event.setCancelled(true);
    }

    private String format(Location loc) {
        return String.format("%s (%.0f, %.0f, %.0f)", loc.getWorld().getName(), loc.getX(), loc.getY(), loc.getZ());
    }
}

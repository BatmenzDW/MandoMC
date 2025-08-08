package com.astuteflamez.mandomc.features.events.beskar;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;

public class OreEventListener implements Listener {

    @EventHandler
    public void onBreak(BlockBreakEvent e) {
        var manager = OreEventManager.getInstance();

        // Only restrict blocks in the configured Ore Event world
        if (!manager.isInWorld(e.getBlock().getWorld())) return;

        // Only allow breaking if it's a tracked Ancient Debris block
        if (manager.isOre(e.getBlock().getLocation())) {
            e.setDropItems(false);
            manager.handleBreak(e.getBlock().getLocation());
        } else {
            e.setCancelled(true);
        }
    }
}

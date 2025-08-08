package com.astuteflamez.mandomc.features.events.chesthunt;

import org.bukkit.*;
import org.bukkit.block.Chest;
import org.bukkit.entity.Firework;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.meta.FireworkMeta;

import java.util.Objects;

public class ChestHuntListener implements Listener {

    // ✅ Fires whenever a player clicks an item in a chest
    @EventHandler
    public void onChestClick(InventoryClickEvent event) {
        if (!(event.getInventory().getHolder() instanceof Chest)) return;

        Chest chest = (Chest) event.getInventory().getHolder();
        Location loc = chest.getLocation();

        // ✅ Only handle ChestHunt chests
        if (!ChestHuntManager.getInstance().getActiveChests().contains(loc)) return;

        // ✅ Delay check until AFTER click is processed (1 tick later)
        Bukkit.getScheduler().runTaskLater(
                Objects.requireNonNull(Bukkit.getPluginManager().getPlugin("MandoMC")),
                () -> checkIfChestIsEmpty(chest, loc),
                1
        );
    }

    // ✅ Fires when player closes the chest inventory
    @EventHandler
    public void onChestClose(InventoryCloseEvent event) {
        if (!(event.getInventory().getHolder() instanceof Chest)) return;

        Chest chest = (Chest) event.getInventory().getHolder();
        Location loc = chest.getLocation();

        if (!ChestHuntManager.getInstance().getActiveChests().contains(loc)) return;

        checkIfChestIsEmpty(chest, loc);
    }

    // ✅ Checks if chest is empty and handles removal/firework
    private void checkIfChestIsEmpty(Chest chest, Location loc) {
        Inventory inv = chest.getBlockInventory();

        if (inv.isEmpty() && loc.getBlock().getType() == Material.CHEST) {
            // ✅ Remove chest from world
            loc.getBlock().setType(Material.AIR);

            // ✅ Update manager
            ChestHuntManager.getInstance().removeChest(loc);

            // ✅ Firework (visual only, no damage) — ONLY if in "DeathStar"
            if (loc.getWorld() != null && "DeathStar".equals(loc.getWorld().getName())) {
                playFirework(loc);
                loc.getWorld().playSound(loc, Sound.BLOCK_ENDER_CHEST_OPEN, 0.8f, 1.0f);
            }
        }
    }

    // ✅ Spawns a purely visual firework (no damage, no knockback)
    private void playFirework(Location loc) {
        FireworkEffect effect = FireworkEffect.builder()
                .with(FireworkEffect.Type.STAR)
                .withColor(Color.YELLOW)
                .withFade(Color.RED)
                .trail(true)
                .flicker(true)
                .build();

        // ✅ Spawn firework entity for effect, then remove before it can cause damage
        loc.getWorld().spawn(loc.clone().add(0.5, 1, 0.5), Firework.class, fw -> {
            FireworkMeta meta = fw.getFireworkMeta();
            meta.clearEffects();
            meta.addEffect(effect);
            meta.setPower(0); // ✅ no flight time
            fw.setFireworkMeta(meta);

            // ✅ Remove entity next tick to prevent any damage/interaction
            Bukkit.getScheduler().runTaskLater(
                    Bukkit.getPluginManager().getPlugin("MandoMC"),
                    fw::remove,
                    1
            );
        });
    }
}

package com.astuteflamez.mandomc.features.events.rhydonium;

import com.astuteflamez.mandomc.MandoMC;
import com.astuteflamez.mandomc.features.items.ItemsManager;
import org.bukkit.*;
import org.bukkit.entity.ArmorStand;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;

import java.util.ArrayList;
import java.util.List;

public class RhydoniumBarrelSpawner {

    private final MandoMC plugin;
    private final NamespacedKey barrelKey;
    private final List<ArmorStand> spawnedBarrels = new ArrayList<>();

    public RhydoniumBarrelSpawner(MandoMC plugin) {
        this.plugin = plugin;
        this.barrelKey = new NamespacedKey(plugin, "rhydonium_barrel");
    }

    public void spawnBarrels() {
        ItemsManager itemsManager = MandoMC.getInstance().getItemsManager();

        for (Location loc : RhydoniumLocationManager.getInstance().getLocations()) {
            loc.getChunk().load();

            // Check for existing barrel at that location
            ArmorStand existing = null;
            for (ArmorStand stand : loc.getWorld().getNearbyEntitiesByType(ArmorStand.class, loc, 0.5)) {
                if (isRhydoniumBarrel(stand)) {
                    existing = stand;
                    break;
                }
            }

            if (existing != null) {
                // ✅ Reset fuel if barrel already exists
                setFuel(existing, 100);
                continue;
            }

            // ✅ Otherwise spawn a new barrel
            ArmorStand stand = loc.getWorld().spawn(loc, ArmorStand.class);
            stand.setInvisible(true);
            stand.setMarker(false);
            stand.setGravity(false);
            stand.setCustomNameVisible(true);
            stand.setSmall(false);
            stand.setCollidable(true);
            stand.getPersistentDataContainer().set(barrelKey, PersistentDataType.INTEGER, 100);
            setFuel(stand, 100); // Also sets the name
            // Set item on helmet (example: Redstone Block)
            ItemStack helmetItem = itemsManager.getItem("rhydonium_barrel");
            stand.setHelmet(helmetItem);
            spawnedBarrels.add(stand);
        }
    }


    private String locToString(Location loc) {
        return "(" + loc.getWorld().getName() + ", " + loc.getBlockX() + ", " + loc.getBlockY() + ", " + loc.getBlockZ() + ")";
    }

    public boolean isRhydoniumBarrel(ArmorStand stand) {
        return stand.getPersistentDataContainer().has(barrelKey, PersistentDataType.INTEGER);
    }

    public int getFuel(ArmorStand stand) {
        return stand.getPersistentDataContainer().getOrDefault(barrelKey, PersistentDataType.INTEGER, 0);
    }

    public void setFuel(ArmorStand stand, int fuel) {
        stand.getPersistentDataContainer().set(barrelKey, PersistentDataType.INTEGER, fuel);

        // Clamp fuel range
        int clampedFuel = Math.max(0, Math.min(100, fuel));

        // ⛽ Fuel bar: 10 segments
        int bars = clampedFuel / 10;

        // 🔵 Bar color (based on bar fill)
        String barColor;
        if (clampedFuel >= 75) barColor = "§a";      // Green
        else if (clampedFuel >= 50) barColor = "§6"; // Orange
        else if (clampedFuel >= 25) barColor = "§e"; // Yellow
        else barColor = "§c";                        // Red

        StringBuilder barBuilder = new StringBuilder("§7[");
        barBuilder.append(barColor).append("|".repeat(bars));
        barBuilder.append("§7".repeat(10 - bars)).append("§7]");

        // 🎯 Percent color logic
        String percentColor;
        if (clampedFuel == 100) percentColor = "§2";        // Dark Green
        else if (clampedFuel >= 75) percentColor = "§a";    // Green
        else if (clampedFuel >= 50) percentColor = "§6";    // Orange
        else if (clampedFuel >= 25) percentColor = "§e";    // Yellow
        else if (clampedFuel >= 1) percentColor = "§c";     // Red
        else percentColor = "§4";                           // Dark Red

        String name = "§bFuel " + barBuilder + " " + percentColor + "(" + clampedFuel + "%)";
        stand.setCustomName(name);
    }

}

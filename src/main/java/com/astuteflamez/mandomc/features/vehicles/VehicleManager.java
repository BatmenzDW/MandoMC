package com.astuteflamez.mandomc.features.vehicles;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class VehicleManager {

    private static final Map<UUID, Vehicle> activeVehicles = new HashMap<>();

    public static void register(Player player, Vehicle vehicle) {
        activeVehicles.put(player.getUniqueId(), vehicle);
    }

    public static Vehicle get(Player player) {
        return activeVehicles.get(player.getUniqueId());
    }

    public static void unregister(Player player) {
        Vehicle vehicle = activeVehicles.remove(player.getUniqueId());
    }

    public static boolean isAlive(Player player) {
        return activeVehicles.containsKey(player.getUniqueId());
    }

    public static Map<UUID, Vehicle> getAll() {
        return Collections.unmodifiableMap(activeVehicles);
    }
}

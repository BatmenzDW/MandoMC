package com.astuteflamez.mandomc.features.vehicles.variants;

import com.astuteflamez.mandomc.features.vehicles.Vehicle;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Zombie;
import org.bukkit.inventory.ItemStack;

public enum VehicleType {
    // Air (Phantom)
    XWING(Material.ECHO_SHARD, 79, true, "vehicles.xwing_fly") {
        @Override
        public Vehicle construct(Player owner, Entity mount, Zombie zombie, ItemStack vehicleItem) {
            return new XWing(owner, mount, zombie, vehicleItem, soundKey);
        }
    },
    TIE_FIGHTER(Material.ECHO_SHARD, 88, true, "vehicles.tie_fighter_fly") {
        @Override
        public Vehicle construct(Player owner, Entity mount, Zombie zombie, ItemStack vehicleItem) {
            return new TieFighter(owner, mount, zombie, vehicleItem, soundKey);
        }
    },

    // Ground (Horse) — update CMD if you have a specific model
    SPEEDER_BIKE(Material.ECHO_SHARD, 90, false, "vehicles.speederbike_start") {
        @Override
        public Vehicle construct(Player owner, Entity mount, Zombie zombie, ItemStack vehicleItem) {
            return new SpeederBike(owner, mount, zombie, vehicleItem, soundKey);
        }
    };

    public final Material material;
    public final int cmd;
    public final boolean air; // true = Phantom, false = Horse
    public final String soundKey;

    VehicleType(Material material, int cmd, boolean air, String soundKey) {
        this.material = material;
        this.cmd = cmd;
        this.air = air;
        this.soundKey = soundKey;
    }

    public boolean matches(ItemStack item) {
        if (item == null || item.getType() != material) return false;
        if (!item.hasItemMeta() || !item.getItemMeta().hasCustomModelData()) return false;
        return item.getItemMeta().getCustomModelData() == cmd;
    }

    public static VehicleType resolve(ItemStack item) {
        for (VehicleType t : values()) if (t.matches(item)) return t;
        return null;
    }

    public Vehicle construct(Player owner, Entity mount, Zombie zombie, ItemStack vehicleItem) {
        // Fallback generic if you add new constants later
        return new Vehicle(owner, mount, zombie, vehicleItem, soundKey);
    }
}

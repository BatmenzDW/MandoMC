package com.astuteflamez.mandomc.features.vehicles.variants;

import com.astuteflamez.mandomc.features.vehicles.Vehicle;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Phantom;
import org.bukkit.entity.Player;
import org.bukkit.entity.Zombie;
import org.bukkit.inventory.ItemStack;

public class TieFighter extends Vehicle {

    public TieFighter(Player player, Entity entity, Zombie zombie, ItemStack item, String sound) {
        super(player, entity, zombie, item, sound);
        setMaxFuel(500);
        setMaxBoost(25);
        setSpeed(3);
    }
}

package com.astuteflamez.mandomc.features.vehicles.variants;

import com.astuteflamez.mandomc.features.vehicles.Vehicle;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Zombie;
import org.bukkit.inventory.ItemStack;

public class SpeederBike extends Vehicle {

    public SpeederBike(Player player, Entity entity, Zombie zombie, ItemStack item, String sound) {
        super(player, entity, zombie, item, sound);
        setMaxFuel(300);
        setMaxBoost(30);
        setSpeed(1);
        setHoverMode(true);
    }
}

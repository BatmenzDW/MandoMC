package com.astuteflamez.mandomc.features.vehicles;

import org.bukkit.entity.Phantom;
import org.bukkit.entity.Player;
import org.bukkit.entity.Zombie;
import org.bukkit.inventory.ItemStack;

public class XWing extends Vehicle {

    private int torpedos;
    private int coaxium;

    public XWing(Player player, Phantom phantom, Zombie zombie, ItemStack item) {
        super(player, phantom, zombie, item);
    }

    public int getCoaxium() {
        return coaxium;
    }

    public void setCoaxium(int coaxium) {
        this.coaxium = coaxium;
    }

    public int getTorpedos() {
        return torpedos;
    }

    public void setTorpedos(int torpedos) {
        this.torpedos = torpedos;
    }
}

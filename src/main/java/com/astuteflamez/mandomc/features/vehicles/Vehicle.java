package com.astuteflamez.mandomc.features.vehicles;

import org.bukkit.ChatColor;
import org.bukkit.entity.Phantom;
import org.bukkit.entity.Player;
import org.bukkit.entity.Zombie;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.List;

public class Vehicle {

    private Player player;
    private Phantom phantom;
    private Zombie zombie;
    private ItemStack item;
    private int health;
    private int maxFuel;
    private int fuel;
    private boolean boost;
    private boolean flying;

    public Vehicle(Player player, Phantom phantom, Zombie zombie, ItemStack item) {
        this.player = player;
        this.phantom = phantom;
        this.zombie = zombie;
        this.item = item;
        this.maxFuel = 1000;
        this.boost = false;
        this.flying = true;

        int defaultHealth = 100;
        int defaultFuel = 0;

        try {
            if (item != null && item.hasItemMeta()) {
                ItemMeta meta = item.getItemMeta();
                if (meta.hasLore()) {
                    List<String> lore = meta.getLore();
                    if (lore != null) {
                        for (String line : lore) {
                            String stripped = ChatColor.stripColor(line).toLowerCase();

                            if (stripped.startsWith("health:")) {
                                String[] parts = stripped.replace("health:", "").trim().split("/");
                                this.health = Integer.parseInt(parts[0]);
                                continue;
                            }
                            if (stripped.startsWith("fuel:")) {
                                String[] parts = stripped.replace("fuel:", "").trim().split("/");
                                this.fuel = Integer.parseInt(parts[0]);
                            }
                        }
                    }
                }
            }

            // If no values set, apply defaults
            if (this.health <= 0) this.health = defaultHealth;
            if (this.fuel < 0) this.fuel = defaultFuel;

        } catch (Exception e) {
            // Fallback to defaults if parsing fails
            this.health = defaultHealth;
            this.fuel = defaultFuel;
        }
    }

    public Player getPlayer() {
        return player;
    }

    public Phantom getPhantom() {
        return phantom;
    }

    public Zombie getZombie() {
        return zombie;
    }

    public ItemStack getItem() {
        return item;
    }

    public int getHealth() {
        return health;
    }

    public void setHealth(int health) {
        this.health = health;
    }

    public int getFuel() {
        return fuel;
    }

    public int getMaxFuel() {
        return maxFuel;
    }

    public void setFuel(int fuel) {
        this.fuel = fuel;
    }

    public boolean isBoost() {
        return boost;
    }

    public boolean isFlying() {
        return boost;
    }

    public void updateVisual() {
        if (item == null || !item.hasItemMeta()) return;

        ItemMeta meta = item.getItemMeta();
        List<String> lore = meta.hasLore() ? meta.getLore() : new java.util.ArrayList<>();

        // Remove old health/fuel lines
        lore.removeIf(line -> {
            String stripped = ChatColor.stripColor(line).toLowerCase();
            return stripped.startsWith("health:") || stripped.startsWith("fuel:");
        });

        // Add updated stats
        lore.add(ChatColor.RED + "Health: " + ChatColor.WHITE + health + ChatColor.GRAY + "/" + ChatColor.WHITE + "100");
        lore.add(ChatColor.YELLOW + "Fuel: " + ChatColor.WHITE + fuel + ChatColor.GRAY + "/" + ChatColor.WHITE + "1000");

        meta.setLore(lore);
        item.setItemMeta(meta);
    }

    public void crash(){

    }
}

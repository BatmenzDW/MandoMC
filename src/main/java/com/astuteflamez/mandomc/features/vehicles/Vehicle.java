package com.astuteflamez.mandomc.features.vehicles;

import org.bukkit.ChatColor;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Zombie;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

public class Vehicle {

    // Core entities (kept final; they shouldn't be swapped out after spawn)
    private final Player player;
    private final Entity entity;
    private final Zombie zombie;
    private final ItemStack item;
    private final String sound;

    // Stats / state
    private int health;
    private int maxFuel  = 1000;
    private int fuel;
    private int boost    = 0;
    private int maxBoost = 50;
    private int speed    = 2;

    private boolean boosting = false;
    private boolean inDrive  = false;

    // Hover mode = ground vehicle (uses ground/hover logic).
    // Air vehicles (e.g., X‑Wing) should set hoverMode=false.
    private boolean hoverMode = false;

    // -------- New: inputs (written by InputListener) --------
    private double forwardInput;  // [-1, 1]  W=+1, S=-1
    private double turnInput;     // [-1, 1]  D=+1 (turn right), A=-1 (turn left)
    private boolean braking;      // true when sneak is held

    public Vehicle(Player player, Entity entity, Zombie zombie, ItemStack item, String sound) {
        this.player = player;
        this.entity = entity;
        this.zombie = zombie;
        this.item = item;
        this.sound = sound;

        // Defaults
        int defaultHealth = 100;
        int defaultFuel   = 0;

        // Parse from lore if present (non-fatal)
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
                                this.health = parseSafeInt(parts[0], defaultHealth);
                            } else if (stripped.startsWith("fuel:")) {
                                String[] parts = stripped.replace("fuel:", "").trim().split("/");
                                this.fuel = parseSafeInt(parts[0], defaultFuel);
                            }
                        }
                    }
                }
            }
        } catch (Exception ignored) { }

        // Fallback to defaults if unset/invalid
        if (this.health <= 0) this.health = defaultHealth;
        if (this.fuel   <  0) this.fuel   = defaultFuel;

        // Clamp to bounds
        this.health = clamp(this.health, 0, 100);
        this.fuel   = clamp(this.fuel,   0, maxFuel);
    }

    // -------- Getters --------
    public Player getPlayer() { return player; }
    public Entity getEntity() { return entity; }
    public Zombie getZombie() { return zombie; }
    public ItemStack getItem() { return item; }
    public String getSound() { return sound; }

    public int getHealth() { return health; }
    public int getFuel() { return fuel; }
    public int getMaxFuel() { return maxFuel; }
    public int getBoost() { return boost; }
    public int getMaxBoost() { return maxBoost; }
    public int getSpeed() { return speed; }

    public boolean isBoosting() { return boosting; }
    public boolean isInDrive() { return inDrive; }
    public boolean isHoverMode() { return hoverMode; }

    // Inputs
    public double getForwardInput() { return forwardInput; }
    public double getTurnInput()    { return turnInput; }
    public boolean isBraking()      { return braking; }

    // -------- Setters / Mutators (with clamps) --------
    public void setHealth(int health) {
        this.health = clamp(health, 0, 100);
    }

    public void setFuel(int fuel) {
        this.fuel = clamp(fuel, 0, maxFuel);
    }

    public void setMaxFuel(int maxFuel) {
        this.maxFuel = Math.max(1, maxFuel);
        this.fuel = clamp(this.fuel, 0, this.maxFuel);
    }

    public void setBoost(int boost) {
        this.boost = clamp(boost, 0, maxBoost);
    }

    public void setMaxBoost(int maxBoost) {
        this.maxBoost = Math.max(0, maxBoost);
        this.boost = clamp(this.boost, 0, this.maxBoost);
    }

    public void setSpeed(int speed) {
        this.speed = Math.max(0, speed);
    }

    public void setBoosting(boolean boosting) {
        this.boosting = boosting;
    }

    public void setInDrive(boolean inDrive){
        this.inDrive = inDrive;
    }

    public void setHoverMode(boolean hoverMode) { this.hoverMode = hoverMode; }

    public void setForwardInput(double v) { this.forwardInput = Math.max(-1, Math.min(1, v)); }
    public void setTurnInput(double v)    { this.turnInput    = Math.max(-1, Math.min(1, v)); }
    public void setBraking(boolean b)     { this.braking = b; }

    // -------- Lore sync --------
    public void updateVisual() {
        if (item == null || !item.hasItemMeta()) return;

        ItemMeta meta = item.getItemMeta();
        List<String> lore = meta.hasLore() ? new ArrayList<>(meta.getLore()) : new ArrayList<>();

        // Remove old health/fuel lines
        lore.removeIf(line -> {
            String stripped = ChatColor.stripColor(line).toLowerCase();
            return stripped.startsWith("health:") || stripped.startsWith("fuel:");
        });

        // Add updated stats
        lore.add(ChatColor.RED + "Health: " + ChatColor.WHITE + health + ChatColor.GRAY + "/" + ChatColor.WHITE + "100");
        lore.add(ChatColor.YELLOW + "Fuel: " + ChatColor.WHITE + fuel + ChatColor.GRAY + "/" + ChatColor.WHITE + maxFuel);

        meta.setLore(lore);
        item.setItemMeta(meta);
    }

    // -------- Lifecycle --------
    public void crash(){
        if (entity != null && !entity.isDead()) entity.remove();
        if (zombie  != null && !zombie.isDead())  zombie.remove();
        VehicleManager.unregister(player);
        item.setAmount(1);
        player.getInventory().remove(item);
    }

    public void returnVehicle(){
        // Write the latest stats to the item BEFORE giving it back
        updateVisual();

        // Hand the player a fresh clone so the inventory doesn't hold a reference
        ItemStack toReturn = item != null ? item.clone() : null;
        if (toReturn != null) {
            player.getInventory().addItem(toReturn);
        }
    }

    // -------- Utils --------
    private static int clamp(int v, int min, int max) {
        return Math.max(min, Math.min(max, v));
    }

    private static int parseSafeInt(String s, int fallback) {
        try {
            return Integer.parseInt(s.trim());
        } catch (Exception e) {
            return fallback;
        }
    }
}

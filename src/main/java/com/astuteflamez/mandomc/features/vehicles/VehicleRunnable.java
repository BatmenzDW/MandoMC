package com.astuteflamez.mandomc.features.vehicles;

import com.astuteflamez.mandomc.MandoMC;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Phantom;
import org.bukkit.entity.Player;
import org.bukkit.entity.Zombie;
import org.bukkit.scheduler.BukkitRunnable;

public class VehicleRunnable extends BukkitRunnable {

    private final MandoMC mandoMC;
    private int tickCounter = 0; // Track ticks for fuel drain

    public VehicleRunnable(MandoMC mandoMC) {
        this.mandoMC = mandoMC;
    }

    @Override
    public void run() {
        tickCounter++;

        for (Player player : Bukkit.getOnlinePlayers()) {

            Vehicle vehicle = VehicleManager.get(player);
            if (vehicle == null) continue;

            Phantom phantom = vehicle.getPhantom();
            Zombie zombie = vehicle.getZombie();
            Location loc = player.getLocation();

            // Movement logic
            phantom.setAI(true);
            phantom.setRotation(loc.getYaw(), loc.getPitch());
            phantom.setVelocity(phantom.getLocation().getDirection().multiply(2));
            zombie.teleport(phantom.getLocation());

            // Fuel drain every second if player is mounted
            if (tickCounter % 20 == 0 && phantom.getPassengers().contains(player)) {
                vehicle.setFuel(Math.max(0, vehicle.getFuel() - 1));
            }

            // Build bars
            String healthBar = buildBar(vehicle.getHealth(), 100, ChatColor.GREEN, ChatColor.YELLOW, ChatColor.RED);
            String fuelBar = buildBar(vehicle.getFuel(), 1000, ChatColor.DARK_GREEN, ChatColor.YELLOW, ChatColor.RED);

            // Base HUD
            String hud = ChatColor.RED + "HP " + healthBar + " " + vehicle.getHealth() + "%" +
                    ChatColor.GRAY + "   |   " +
                    ChatColor.GOLD + "Fuel " + fuelBar + " " + vehicle.getFuel();

            // Add torpedo count if XWing
            if (vehicle instanceof XWing) {
                XWing xwing = (XWing) vehicle;
                hud += ChatColor.GRAY + "   |   " +
                        ChatColor.LIGHT_PURPLE + "Torpedos: " + ChatColor.WHITE + xwing.getTorpedos();
            }

            // Show HUD in action bar
            player.sendActionBar(Component.text(hud));

            // Emergency warnings & crash
            if (vehicle.getHealth() <= 0 || vehicle.getFuel() <= 0) {
                player.sendTitle(
                        ChatColor.RED + ChatColor.BOLD.toString() + "EMERGENCY!",
                        ChatColor.YELLOW + "Vehicle Critical Failure!",
                        5, 20, 5
                );
                vehicle.crash();
                VehicleManager.unregister(player);
            } else if (vehicle.getHealth() <= 20 || vehicle.getFuel() <= 100) {
                // Flash warning when low
                player.sendTitle(
                        ChatColor.RED + ChatColor.BOLD.toString() + "WARNING",
                        ChatColor.YELLOW + "Fuel or Health Low!",
                        0, 10, 0
                );
            }
        }

        // Reset tick counter to avoid integer overflow
        if (tickCounter >= 1200) tickCounter = 0;
    }


    private String buildBar(int value, int max, ChatColor highColor, ChatColor midColor, ChatColor lowColor) {
        int totalBars = 10;
        double percent = (double) value / max;
        int filledBars = (int) Math.round(percent * totalBars);

        ChatColor color;
        if (percent > 0.5) {
            color = highColor;
        } else if (percent > 0.25) {
            color = midColor;
        } else {
            color = lowColor;
        }

        StringBuilder bar = new StringBuilder();
        for (int i = 0; i < filledBars; i++) {
            bar.append(color).append("|");
        }
        for (int i = filledBars; i < totalBars; i++) {
            bar.append(ChatColor.DARK_GRAY).append("|");
        }
        return bar.toString();
    }
}

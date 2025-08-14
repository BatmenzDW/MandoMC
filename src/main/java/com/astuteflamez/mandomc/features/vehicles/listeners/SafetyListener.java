package com.astuteflamez.mandomc.features.vehicles.listeners;

import com.astuteflamez.mandomc.features.vehicles.Vehicle;
import com.astuteflamez.mandomc.features.vehicles.VehicleManager;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.projectiles.ProjectileSource;

public class SafetyListener implements Listener {

    @EventHandler
    public void onDrop(PlayerDropItemEvent event) {
        Player player = event.getPlayer();

        com.astuteflamez.mandomc.features.vehicles.Vehicle vehicle = VehicleManager.get(player);
        if (vehicle != null) event.setCancelled(true);
    }

    @EventHandler
    public void onFallDamage(EntityDamageEvent event) {
        // Check if the entity is a player
        if (event.getEntity() instanceof Player player) {

            // Check if the cause is fall damage
            if (event.getCause() == EntityDamageEvent.DamageCause.FALL) {
                com.astuteflamez.mandomc.features.vehicles.Vehicle vehicle = VehicleManager.get(player);
                if (vehicle != null) {
                    if (vehicle.isInDrive()){
                        event.setCancelled(true);
                    }
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onVehicleHitByProjectile(EntityDamageByEntityEvent event) {
        // Only care about projectile -> entity
        if (!(event.getDamager() instanceof Projectile projectile)) return;

        Entity hit = event.getEntity();

        // Find which vehicle (if any) owns the hit entity
        com.astuteflamez.mandomc.features.vehicles.Vehicle vehicle = getVehicleByPart(hit);
        if (vehicle == null) return;

        // Shooter must NOT be the vehicle owner
        ProjectileSource src = projectile.getShooter();
        if (src instanceof Player shooter) {
            if (shooter.getUniqueId().equals(vehicle.getPlayer().getUniqueId())) {
                // Owner fired this projectile -> ignore
                return;
            }
        }

        // Reduce vehicle health by 1 (clamped)
        int newHp = Math.max(0, vehicle.getHealth() - 1);
        vehicle.setHealth(newHp);

        // Cancel actual damage to phantom/zombie so visuals only
        event.setDamage(0.0);
        event.setCancelled(true);

        // Tiny feedback at hit location
        hit.getWorld().spawnParticle(Particle.CRIT, hit.getLocation().add(0, 1.2, 0), 8, 0.2, 0.2, 0.2, 0.05);
        hit.getWorld().playSound(hit.getLocation(), Sound.ENTITY_ZOMBIE_ATTACK_IRON_DOOR, 0.6f, 1.4f);

        // Optional: if destroyed, trigger crash()
        if (newHp <= 0) {
            vehicle.crash();
        }
    }

    private com.astuteflamez.mandomc.features.vehicles.Vehicle getVehicleByPart(Entity e) {
        for (Vehicle v : VehicleManager.getAll().values()) {
            Entity p = v.getEntity();
            Zombie z = v.getZombie();
            if ((p != null && p.equals(e)) || (z != null && z.equals(e))) {
                return v;
            }
        }
        return null;
    }
}

package com.astuteflamez.mandomc.features.vehicles.listeners;

import com.astuteflamez.mandomc.features.vehicles.Vehicle;
import com.astuteflamez.mandomc.features.vehicles.VehicleManager;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDismountEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class ExitListener implements Listener {

    @EventHandler
    public void onDismount(EntityDismountEvent event) {
        if (!(event.getEntity() instanceof Player player)) return;

        Vehicle vehicle = VehicleManager.get(player);
        if (vehicle == null) return;

        if (vehicle.getEntity().equals(event.getDismounted())) {
            cleanupVehicle(player, vehicle);
        }
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        Vehicle vehicle = VehicleManager.get(player);
        if (vehicle == null) return;

        cleanupVehicle(player, vehicle);
    }

    private void cleanupVehicle(Player player, Vehicle vehicle) {
        if (!vehicle.getEntity().isDead()) vehicle.getEntity().remove();
        if (!vehicle.getZombie().isDead()) vehicle.getZombie().remove();
        vehicle.returnVehicle();              // give item back to player
        VehicleManager.unregister(player);    // clear from registry
    }
}

package com.astuteflamez.mandomc.features.vehicles;

import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;

public class VehicleCheckListener implements Listener {

    @EventHandler
    public void onDrop(PlayerDropItemEvent event) {
        Player player = event.getPlayer();

        Vehicle vehicle = VehicleManager.get(player);
        if (vehicle != null) event.setCancelled(true);
    }

    @EventHandler
    public void onInteract(PlayerInteractAtEntityEvent event){
        Player player = event.getPlayer();
        Entity entity = event.getRightClicked();

        Vehicle vehicle = VehicleManager.get(player);
        if (vehicle == null) return;

        if(entity == vehicle.getPhantom() || entity == vehicle.getZombie()) event.setCancelled(true);
    }
}

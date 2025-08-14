package com.astuteflamez.mandomc.features.vehicles.listeners;

import com.astuteflamez.mandomc.features.vehicles.Vehicle;
import com.astuteflamez.mandomc.features.vehicles.VehicleManager;
import com.astuteflamez.mandomc.guis.GUIManager;
import com.astuteflamez.mandomc.guis.VehiclesGUI.VehiclesHub;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;
import org.bukkit.inventory.EquipmentSlot;

public class EnterListener implements Listener {

    private final GUIManager guiManager;

    public EnterListener(GUIManager guiManager) {
        this.guiManager = guiManager;
    }

    @EventHandler
    public void onClickVehicle(PlayerInteractAtEntityEvent event) {
        if (event.getHand() != EquipmentSlot.HAND) return;

        Player player = event.getPlayer();
        Vehicle vehicle = VehicleManager.get(player);
        if (vehicle == null) return;

        Entity clicked = event.getRightClicked();
        if (clicked.equals(vehicle.getEntity()) || clicked.equals(vehicle.getZombie())) {
            event.setCancelled(true);
            // Open your vehicles UI when clicking your own vehicle
            guiManager.openGUI(new VehiclesHub(guiManager), player);
        }
    }
}

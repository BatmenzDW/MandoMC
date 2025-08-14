package com.astuteflamez.mandomc.features.vehicles.listeners;

import com.astuteflamez.mandomc.features.vehicles.Vehicle;
import com.astuteflamez.mandomc.features.vehicles.VehicleFactory;
import com.astuteflamez.mandomc.features.vehicles.VehicleManager;
import com.astuteflamez.mandomc.features.vehicles.variants.VehicleType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

import static org.bukkit.event.block.Action.RIGHT_CLICK_AIR;
import static org.bukkit.event.block.Action.RIGHT_CLICK_BLOCK;

public class SpawnListener implements Listener {

    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
        if (event.getHand() != EquipmentSlot.HAND) return;
        if (event.getAction() != RIGHT_CLICK_AIR && event.getAction() != RIGHT_CLICK_BLOCK) return;

        ItemStack item = event.getPlayer().getInventory().getItemInMainHand();
        VehicleType type = VehicleType.resolve(item);
        if (type == null) return; // not a vehicle item

        // Don’t double-spawn if they already have an active vehicle
        if (VehicleManager.isAlive(event.getPlayer())) return;

        event.setCancelled(true);

        Vehicle v = VehicleFactory.spawn(event.getPlayer(), item, type);
        if (v == null) return;
        // (Optional) Immediately set default “not driving” state, UI, etc.
    }
}

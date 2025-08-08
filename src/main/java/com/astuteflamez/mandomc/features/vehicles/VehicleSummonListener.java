package com.astuteflamez.mandomc.features.vehicles;

import com.astuteflamez.mandomc.guis.GUIManager;
import com.astuteflamez.mandomc.guis.VehiclesGUI.VehiclesHub;
import org.bukkit.Material;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Phantom;
import org.bukkit.entity.Player;
import org.bukkit.entity.Zombie;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDismountEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

public class VehicleSummonListener implements Listener {

    private final GUIManager guiManager;

    public VehicleSummonListener(GUIManager guiManager) {
        this.guiManager = guiManager;
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
        if (event.getHand() != EquipmentSlot.HAND) return;

        Player player = event.getPlayer();
        ItemStack item = player.getInventory().getItemInMainHand();
        if (!isXwingItem(item)) return;

        if (event.getAction().isRightClick()) {
            handleRightClick(player, item);
        }
        else if (event.getAction().isLeftClick()) {
            handleLeftClick(player);
        }
    }

    private void handleRightClick(Player player, ItemStack item) {
        if (VehicleManager.isFlying(player)) return;

        // Fuel check before summoning
        Vehicle tempVehicle = new Vehicle(player, null, null, item);
        if (tempVehicle.getFuel() <= 0) {
            player.sendMessage("§cYour X-Wing has no fuel!");
            return;
        }

        spawnVehicle(player, item);
    }

    private void handleLeftClick(Player player) {
        if(VehicleManager.isFlying(player)) return;
        guiManager.openGUI(new VehiclesHub(guiManager), player);
        player.sendMessage("§eYou left-clicked with your X-Wing!");
    }

    private void spawnVehicle(Player player, ItemStack item) {
        Phantom phantom = player.getWorld().spawn(player.getLocation(), Phantom.class, p -> {
            p.setSilent(true);
            p.setInvulnerable(true);
            p.setRemoveWhenFarAway(false);
            p.setPersistent(true);
            p.setCollidable(false);
            p.setAI(false);
            p.setGravity(false);
            p.setSize(1);
            p.setInvisible(false);
            p.setShouldBurnInDay(false);
            p.getAttribute(Attribute.GENERIC_SCALE).setBaseValue(3.0); // 3x bigger
        });

        Zombie zombie = player.getWorld().spawn(player.getLocation(), Zombie.class, z -> {
            z.setSilent(true);
            z.setInvulnerable(true);
            z.setRemoveWhenFarAway(false);
            z.setPersistent(true);
            z.setCollidable(false);
            z.setAI(false);
            z.setGravity(false);
            z.setShouldBurnInDay(false);
            z.stopDrowning();
            z.setInvisible(true);
            z.getEquipment().setHelmet(item);
            z.getEquipment().setHelmetDropChance(0.0f);
        });

        phantom.addPassenger(player);

        Vehicle vehicle = new Vehicle(player, phantom, zombie, item);
        VehicleManager.register(player, vehicle);
    }

    private boolean isXwingItem(ItemStack item) {
        return item != null
                && item.getType() == Material.ECHO_SHARD
                && item.hasItemMeta()
                && item.getItemMeta().hasCustomModelData()
                && item.getItemMeta().getCustomModelData() == 79;
    }

    @EventHandler
    public void onDismount(EntityDismountEvent event) {
        if (!(event.getEntity() instanceof Player player)) return;

        Vehicle vehicle = VehicleManager.get(player);
        if (vehicle != null && vehicle.getPhantom().equals(event.getDismounted())) {
            vehicle.getPhantom().remove();
            vehicle.getZombie().remove();
            VehicleManager.unregister(player);
        }
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        Vehicle vehicle = VehicleManager.get(player);
        if (vehicle != null) {
            vehicle.getPhantom().remove();
            vehicle.getZombie().remove();
            VehicleManager.unregister(player);
        }
    }
}

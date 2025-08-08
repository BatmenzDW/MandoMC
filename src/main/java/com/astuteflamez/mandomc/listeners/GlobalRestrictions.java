package com.astuteflamez.mandomc.listeners;

import com.astuteflamez.mandomc.LangConfig;
import com.astuteflamez.mandomc.MandoMC;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.Villager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.inventory.BrewEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

public class GlobalRestrictions implements Listener {

    private final MandoMC plugin;
    private final FileConfiguration config;

    private final String disabledFeature = MandoMC.PREFIX + MandoMC.color(LangConfig.get().getString("DisabledFeatured"));

    public GlobalRestrictions(MandoMC plugin) {
        this.plugin = plugin;
        this.config = plugin.getConfig();
    }

    // ✅ 1. BLOCK PLACEMENT RESTRICTIONS
    @EventHandler
    public void onBlockBreak(BlockBreakEvent event){
        Player player = event.getPlayer();

        if(player.getLocation().getWorld().getName().equalsIgnoreCase("Ilum")){
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        Material block = event.getBlockPlaced().getType();
        Player player = event.getPlayer();

        if (block == Material.END_CRYSTAL && config.getBoolean("disable-block-place.end_crystal", true)) {
            cancelPlacement(event);
        }
        if (block == Material.END_PORTAL_FRAME && config.getBoolean("disable-block-place.end_portal_frame", true)) {
            cancelPlacement(event);
        }
        if (block == Material.BEDROCK && config.getBoolean("disable-block-place.bedrock", true)) {
            cancelPlacement(event);
        }
        if(player.getLocation().getWorld().getName().equalsIgnoreCase("Ilum")){
            event.setCancelled(true);
        }
    }

    private void cancelPlacement(BlockPlaceEvent event) {
        event.setCancelled(true);
        event.getPlayer().sendMessage(disabledFeature);
    }

    // ✅ 2. DISABLE POTION BREWING
    @EventHandler
    public void onPotionBrew(BrewEvent event) {
        if (config.getBoolean("restrictions.disable-potion-brewing", true)) {
            event.setCancelled(true);
        }
    }

    // ✅ 3. DISABLE PHANTOM SPAWNS
    @EventHandler
    public void onPhantomSpawn(CreatureSpawnEvent event) {
        if (event.getEntityType() == EntityType.PHANTOM &&
                config.getBoolean("mobs.disable-natural-phantoms", true)) {
            event.setCancelled(true);
        }
    }

    // ✅ 4. DISABLE ENDERPEARLS
    @EventHandler
    public void onPearlUse(PlayerInteractEvent event) {
        if (!config.getBoolean("restrictions.disable-enderpearls", true)) return;
        if (event.getHand() != EquipmentSlot.HAND) return; // ignore off-hand

        Player player = event.getPlayer();
        ItemStack item = player.getInventory().getItemInMainHand();

        if (item.getType() == Material.ENDER_PEARL) {
            event.setCancelled(true);
            player.sendMessage(disabledFeature);
        }
    }

    @EventHandler
    public void onPearlTeleport(PlayerTeleportEvent event) {
        if (event.getCause() == PlayerTeleportEvent.TeleportCause.ENDER_PEARL &&
                config.getBoolean("restrictions.disable-enderpearls", true)) {
            event.setCancelled(true);
            event.getPlayer().sendMessage(disabledFeature);
        }
    }

    // ✅ 5. DISABLE VILLAGER TRADING
    @EventHandler
    public void onVillagerTrade(PlayerInteractEntityEvent event) {
        if (config.getBoolean("restrictions.disable-villager-trading", true) &&
                event.getRightClicked() instanceof Villager) {
            event.setCancelled(true);
            event.getPlayer().sendMessage(disabledFeature);
        }
    }

    @EventHandler
    public void onVillagerInventory(InventoryClickEvent event) {
        if (config.getBoolean("restrictions.disable-villager-trading", true) &&
                event.getInventory().getType() == InventoryType.MERCHANT) {
            event.setCancelled(true);
            if (event.getWhoClicked() instanceof Player player) {
                player.sendMessage(disabledFeature);
            }
        }
    }
}

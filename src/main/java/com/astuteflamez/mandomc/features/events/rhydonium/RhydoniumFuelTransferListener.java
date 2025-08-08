package com.astuteflamez.mandomc.features.events.rhydonium;

import com.astuteflamez.mandomc.LangConfig;
import com.astuteflamez.mandomc.MandoMC;
import org.bukkit.Sound;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class RhydoniumFuelTransferListener implements Listener {

    private final String barrelEmpty = MandoMC.PREFIX + MandoMC.color(LangConfig.get().getString("BarrelEmpty"));
    private final String canisterFill = MandoMC.PREFIX + MandoMC.color(LangConfig.get().getString("CanisterFill"));

    private final Map<UUID, Long> lastRefuelSound = new HashMap<>();
    private static final long REFUEL_SOUND_COOLDOWN_MS = 1000; // 1 second

    @EventHandler
    public void onFuelTransfer(PlayerInteractAtEntityEvent event) {
        Entity clicked = event.getRightClicked();

        // Check: entity is armor stand
        if (!(clicked instanceof ArmorStand stand)) return;

        RhydoniumBarrelSpawner spawner = RhydoniumManager.getInstance().getBarrelSpawner();

        // Check: is this a rhydonium barrel?
        if (!spawner.isRhydoniumBarrel(stand)) return;

        Player player = event.getPlayer();
        ItemStack item = player.getInventory().getItemInMainHand();

        // ✅ Prevent armor theft via empty-hand right click
        if (item.getType().isAir()) {
            event.setCancelled(true);
            return;
        }

        // Check: valid canister
        if (!RhydoniumCanisterUtil.isValidCanister(item)) return;

        int barrelFuel = spawner.getFuel(stand);
        int canisterFuel = RhydoniumCanisterUtil.getFuel(item);
        int maxFuel = RhydoniumCanisterUtil.getMaxFuel();

        if (barrelFuel <= 0) {
            player.sendMessage(barrelEmpty);
            player.playSound(player.getLocation(), Sound.BLOCK_FIRE_EXTINGUISH, 0.8f, 1.0f);
            return;
        }

        if (canisterFuel >= maxFuel) {
            player.sendMessage(canisterFill);
            player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BELL, 1.0f, 1.3f);
            return;
        }

        int transfer = Math.min(1, Math.min(barrelFuel, maxFuel - canisterFuel));

        spawner.setFuel(stand, barrelFuel - transfer);
        RhydoniumCanisterUtil.setFuel(item, canisterFuel + transfer);

        // Play refueling sound (throttled)
        long now = System.currentTimeMillis();
        UUID uuid = player.getUniqueId();
        if (!lastRefuelSound.containsKey(uuid) || now - lastRefuelSound.get(uuid) >= REFUEL_SOUND_COOLDOWN_MS) {
            player.playSound(player.getLocation(), Sound.BLOCK_BREWING_STAND_BREW, 1.0f, 1.0f);
            lastRefuelSound.put(uuid, now);
        }

        event.setCancelled(true);
    }

}

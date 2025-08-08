package com.astuteflamez.mandomc.features.sabers;

import org.bukkit.Material;
import org.bukkit.SoundCategory;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Random;

public class SaberHitListener implements Listener {

    private final Random random = new Random();

    @EventHandler
    public void onLeftClickSwing(PlayerInteractEvent event) {
        // ✅ Only react to LEFT clicks in the air or on blocks
        if (!(event.getAction() == Action.LEFT_CLICK_AIR || event.getAction() == Action.LEFT_CLICK_BLOCK)) return;

        Player player = event.getPlayer();
        ItemStack item = player.getInventory().getItemInMainHand();

        // ✅ Only shields can be lightsabers
        if (item == null || item.getType() != Material.SHIELD) return;

        ItemMeta meta = item.getItemMeta();
        if (meta == null || !meta.hasCustomModelData()) return; // must have CMD

        // 🎲 Pick a random swing sound (1–3)
        int swingNum = random.nextInt(3) + 1; // gives 1, 2, or 3
        String soundKey = "melee.lightsaber.swing-" + swingNum; // no namespace prefix

        // 🔊 Play the chosen sound at the player’s location
        player.getWorld().playSound(
                player.getLocation(),
                soundKey,
                SoundCategory.PLAYERS,
                1.0f,
                1.0f
        );
    }

    @EventHandler
    public void onEntityHit(EntityDamageByEntityEvent event) {
        // ✅ Ensure the damager is a player
        if (!(event.getDamager() instanceof Player)) return;
        Player player = (Player) event.getDamager();

        // ✅ Check if the player is holding a lightsaber (custom shield)
        ItemStack item = player.getInventory().getItemInMainHand();
        if (item == null || item.getType() != Material.SHIELD) return;

        ItemMeta meta = item.getItemMeta();
        if (meta == null || !meta.hasCustomModelData()) return; // must have CMD

        // 🔊 Play the hit sound at the entity's location
        Entity hitEntity = event.getEntity();
        hitEntity.getWorld().playSound(
                hitEntity.getLocation(),
                "melee.lightsaber.hit",
                SoundCategory.PLAYERS,
                1.0f,
                1.0f
        );
    }
}

package com.astuteflamez.mandomc.features.blasters;

import com.astuteflamez.mandomc.MandoMC;
import com.massivecraft.factions.FPlayer;
import com.massivecraft.factions.FPlayers;
import com.massivecraft.factions.Faction;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import com.sk89q.worldguard.protection.regions.RegionContainer;
import me.deecaad.weaponmechanics.WeaponMechanicsAPI;
import me.deecaad.weaponmechanics.weapon.reload.ammo.Ammo;
import me.deecaad.weaponmechanics.weapon.weaponevents.WeaponDamageEntityEvent;
import me.deecaad.weaponmechanics.weapon.weaponevents.WeaponPostShootEvent;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerItemDamageEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;

import java.util.HashMap;
import java.util.Map;

public class SpawnPvP implements Listener {

    private static final String REGION_NAME = "warzone";
    private static final String WORLD_NAME = "Naboo";

    private boolean isInWarzone(Player player) {
        if (!player.getWorld().getName().equalsIgnoreCase(WORLD_NAME)) return false;

        RegionContainer container = WorldGuard.getInstance().getPlatform().getRegionContainer();
        RegionManager regions = container.get(BukkitAdapter.adapt(player.getWorld()));
        if (regions == null) return false;

        ApplicableRegionSet set = regions.getApplicableRegions(BukkitAdapter.asBlockVector(player.getLocation()));
        for (ProtectedRegion region : set) {
            if (region.getId().equalsIgnoreCase(REGION_NAME)) return true;
        }
        return false;
    }

    @EventHandler
    public void onDeath(PlayerDeathEvent event) {
        Player player = event.getEntity();
        if (!isInWarzone(player)) return;

        // No loot/XP drop
        event.setKeepInventory(true);
        event.setKeepLevel(true);
        event.getDrops().clear();
        event.setDroppedExp(0);

        // No DTR loss
        FPlayer fPlayer = FPlayers.getInstance().getByPlayer(player);
        if (fPlayer == null) return;

        Faction faction = fPlayer.getFaction();
        if (faction == null || faction.isNone()) return;

        double currentDtr = faction.getDTR();
        Bukkit.getScheduler().runTask(MandoMC.getInstance(), () -> faction.setDTR(currentDtr));
    }

    @EventHandler
    public void onItemDamage(PlayerItemDamageEvent event) {
        Player player = event.getPlayer();
        if (isInWarzone(player)) {
            ItemStack item = event.getItem();
            if (item.getType().toString().endsWith("_HELMET") ||
                    item.getType().toString().endsWith("_CHESTPLATE") ||
                    item.getType().toString().endsWith("_LEGGINGS") ||
                    item.getType().toString().endsWith("_BOOTS") ||
                    item.getType().toString().endsWith("_SWORD") ||
                    item.getType().toString().endsWith("_AXE") ||
                    item.getType().toString().endsWith("_PICKAXE") ||
                    item.getType().toString().endsWith("_SHOVEL") ||
                    item.getType().toString().endsWith("_HOE")) {

                event.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onShoot(WeaponPostShootEvent event) {
        if (!(event.getShooter() instanceof Player player)) return;
        if (!isInWarzone(player)) return;

        ItemStack weaponStack = event.getWeaponStack();
        if (weaponStack == null) return;

        Ammo currentAmmo = WeaponMechanicsAPI.getCurrentAmmo(weaponStack);
        if (currentAmmo == null) return;

        ItemStack ammoItem = WeaponMechanicsAPI.generateAmmo(currentAmmo.getAmmoTitle(), false);
        if (ammoItem == null) return;

        player.getInventory().addItem(ammoItem.clone());
    }

    @EventHandler
    public void onWeaponDamage(WeaponDamageEntityEvent event) {
        if (!(event.getVictim() instanceof Player player)) return;
        if (!isInWarzone(player)) return;

        // Store armor durability BEFORE damage
        ItemStack[] armor = player.getInventory().getArmorContents();
        Map<Integer, Integer> beforeDurability = new HashMap<>();
        for (int i = 0; i < armor.length; i++) {
            ItemStack piece = armor[i];
            if (piece != null && piece.hasItemMeta() && piece.getItemMeta() instanceof Damageable dmg) {
                beforeDurability.put(i, dmg.getDamage());
            }
        }

        // Reapply original durability AFTER the damage tick
        Bukkit.getScheduler().runTaskLater(MandoMC.getInstance(), () -> {
            ItemStack[] currentArmor = player.getInventory().getArmorContents();
            for (int i = 0; i < currentArmor.length; i++) {
                ItemStack piece = currentArmor[i];
                if (piece == null || !beforeDurability.containsKey(i)) continue;

                if (piece.hasItemMeta() && piece.getItemMeta() instanceof Damageable dmg) {
                    int original = beforeDurability.get(i);
                    dmg.setDamage(original);
                    piece.setItemMeta(dmg);
                }
            }
        }, 1L);
    }

}

package com.astuteflamez.mandomc.features.vehicles.listeners;

import com.astuteflamez.mandomc.MandoMC;
import com.astuteflamez.mandomc.features.vehicles.Vehicle;
import com.astuteflamez.mandomc.features.vehicles.VehicleManager;
import com.astuteflamez.mandomc.features.vehicles.variants.TieFighter;
import com.astuteflamez.mandomc.features.vehicles.variants.XWing;
import me.deecaad.weaponmechanics.WeaponMechanicsAPI;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.SoundCategory;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

public class ShootListener implements Listener {

    private final MandoMC plugin;
    private final Random rng = new Random();

    public ShootListener(MandoMC plugin) {
        this.plugin = plugin;
    }

    // === Torpedo cooldown (X-Wing only) ===
    private static final long TORPEDO_COOLDOWN_MS = 2000; // 2 seconds
    private final Map<UUID, Long> torpedoCooldownUntil = new HashMap<>();

    // === TIE Fighter burst config ===
    private static final int TIE_BURST_COUNT = 3;          // shots per burst
    private static final int TIE_BURST_INTERVAL_TICKS = 3; // delay between shots in a burst
    private static final double TIE_SPREAD_DEG_STD = 2.0;  // standard deviation of spread in degrees

    @EventHandler
    public void onShoot(PlayerInteractEvent event) {
        // Only handle main hand right-click to avoid duplicate firing
        if (event.getHand() != EquipmentSlot.HAND) return;
        Action a = event.getAction();
        if (a != Action.RIGHT_CLICK_AIR && a != Action.RIGHT_CLICK_BLOCK) return;

        Player player = event.getPlayer();
        Vehicle vehicle = VehicleManager.get(player);
        if (vehicle == null || !vehicle.isInDrive()) return;

        if (vehicle instanceof XWing) {
            handleXWingFire(player);
        } else if (vehicle instanceof TieFighter) {
            handleTieFighterBurst(player);
        }
    }

    // ---------------- X-Wing: Proton Torpedo with cooldown ----------------
    private void handleXWingFire(Player player) {
        UUID id = player.getUniqueId();
        long now = System.currentTimeMillis();
        long until = torpedoCooldownUntil.getOrDefault(id, 0L);

        if (now < until) {
            long msLeft = until - now;
            double sLeft = Math.ceil(msLeft / 100.0) / 10.0; // round up to 0.1s
            player.sendMessage("§cProton Torpedo recharging: §e" + sLeft + "s");
            return;
        }

        if (!hasAmmo(player, "Proton_Torpedo")) {
            player.sendMessage("§cYou don't have any Proton Torpedoes!");
            return;
        }

        // Consume and fire
        removeOneAmmo(player, "Proton_Torpedo");
        shootProjectile(player, "RPG_7", "vehicles.xwing_shoot");

        // Start cooldown
        torpedoCooldownUntil.put(id, now + TORPEDO_COOLDOWN_MS);
    }

    // --------------- TIE Fighter: Burst fire with spread ------------------
    private void handleTieFighterBurst(Player player) {
        // We'll fire TIE_BURST_COUNT shots, each consuming one Tibanna cell if available.
        // If ammo runs out mid-burst, we stop.
        final UUID shooterId = player.getUniqueId();

        for (int i = 0; i < TIE_BURST_COUNT; i++) {
            int delay = i * TIE_BURST_INTERVAL_TICKS;

            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                Player p = Bukkit.getPlayer(shooterId);
                if (p == null) return;

                Vehicle v = VehicleManager.get(p);
                if (!(v instanceof TieFighter) || !v.isInDrive()) return;

                if (!hasAmmo(p, "Tibanna_Gas_Cell")) {
                    p.sendMessage("§cYou're out of Tibanna Gas Cells!");
                    return;
                }

                removeOneAmmo(p, "Tibanna_Gas_Cell");

                // Apply spread to the current look direction
                Vector dir = p.getLocation().getDirection().normalize();
                Vector spreadDir = applySpread(dir, TIE_SPREAD_DEG_STD);
                WeaponMechanicsAPI.shoot(p, "Uzi", spreadDir);
                player.getWorld().playSound(player.getLocation(), "vehicles.tie_fighter_shoot", SoundCategory.MASTER, 1.0f, 1.0f);
            }, delay);
        }
    }

    // Gaussian spread around the given direction vector
    private Vector applySpread(Vector dir, double degStd) {
        // Build an orthonormal basis around dir
        Vector w = dir.clone().normalize();
        Vector temp = Math.abs(w.getY()) < 0.999 ? new Vector(0, 1, 0) : new Vector(1, 0, 0);
        Vector u = w.clone().getCrossProduct(temp).normalize(); // first perpendicular
        Vector v = w.clone().getCrossProduct(u).normalize();    // second perpendicular

        // Convert degrees to radians; small-angle approx via tangent for offset scale
        double radStd = Math.toRadians(degStd);
        double offsetU = rng.nextGaussian() * Math.tan(radStd);
        double offsetV = rng.nextGaussian() * Math.tan(radStd);

        Vector perturbed = w.clone()
                .add(u.multiply(offsetU))
                .add(v.multiply(offsetV))
                .normalize();

        return perturbed;
    }

    // ---------------- Inventory helpers ----------------
    private boolean hasAmmo(Player player, String ammoTitle) {
        ItemStack ammo = WeaponMechanicsAPI.generateAmmo(ammoTitle, false);
        if (ammo == null || ammo.getType() == Material.AIR) return false;

        for (ItemStack item : player.getInventory().getContents()) {
            if (item != null && item.isSimilar(ammo) && item.getAmount() > 0) {
                return true;
            }
        }
        return false;
    }

    private void removeOneAmmo(Player player, String ammoTitle) {
        ItemStack ammo = WeaponMechanicsAPI.generateAmmo(ammoTitle, false);
        if (ammo == null || ammo.getType() == Material.AIR) return;

        ammo.setAmount(1);
        player.getInventory().removeItem(ammo);
    }

    private void shootProjectile(Player player, String blasterTitle, String sound) {
        Vector vector = player.getLocation().getDirection();
        WeaponMechanicsAPI.shoot(player, blasterTitle, vector);
        player.getWorld().playSound(player.getLocation(), sound, SoundCategory.MASTER, 1.0f, 1.0f);
    }
}

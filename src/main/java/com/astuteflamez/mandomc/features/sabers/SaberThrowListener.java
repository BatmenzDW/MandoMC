package com.astuteflamez.mandomc.features.sabers;

import com.astuteflamez.mandomc.LangConfig;
import com.astuteflamez.mandomc.MandoMC;
import com.astuteflamez.mandomc.features.items.ItemsConfig;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.EulerAngle;
import org.bukkit.util.Vector;

import java.util.Map;
import java.util.UUID;

public class SaberThrowListener implements Listener {

    private final Map<UUID, Long> lightsaberCooldown;
    private static final String PREFIX = "§6§lMandoMC §8» §7"; // Gold bold title, dark gray divider, gray message

    // ✅ Loadable settings from SaberConfig
    private double speed;
    private double curveAmplitude;
    private double curveFrequency;
    private int maxTicks;
    private int cooldown;
    private double damage;

    private final String throwSaber = MandoMC.PREFIX + MandoMC.color(LangConfig.get().getString("SaberThrow"));
    private final String tired = MandoMC.PREFIX + MandoMC.color(LangConfig.get().getString("SaberCooldown"));

    public SaberThrowListener(Map<UUID, Long> lightsaberCooldown) {
        this.lightsaberCooldown = lightsaberCooldown;
        loadSettings();
    }

    /**
     * Load curve and throw settings from SaberConfig.
     */
    public void loadSettings() {
        this.speed = ItemsConfig.get().getDouble("throw-settings.speed", 0.6);
        this.curveAmplitude = ItemsConfig.get().getDouble("throw-settings.curve-amplitude", 0.1);
        this.curveFrequency = ItemsConfig.get().getDouble("throw-settings.curve-frequency", 4.0);
        this.maxTicks = ItemsConfig.get().getInt("throw-settings.max-ticks", 30);
        this.cooldown = ItemsConfig.get().getInt("throw-settings.cooldown", 10000);
        this.damage = ItemsConfig.get().getDouble("throw-settings.damage", 24.0);
    }

    /**
     * Handles the throwing animation, movement, and collisions for the lightsaber.
     */
    public void playerThrowEvent(Player player) {
        ItemStack handItem = player.getInventory().getItemInMainHand();
        if (handItem.getType() != Material.SHIELD) return;

        // ✅ Clone saber in hand to throw
        ItemStack throwStack = handItem.clone();
        throwStack.setAmount(1);

        // ✅ Spawn invisible armor stand holding the saber
        ArmorStand armorStand = player.getWorld().spawn(player.getLocation().add(0, 0.5, 0), ArmorStand.class, stand -> {
            stand.setArms(true);
            stand.setGravity(false);
            stand.setVisible(false);
            stand.setSmall(true);
            stand.setMarker(true);
            stand.getEquipment().setItemInMainHand(throwStack);
            stand.setRightArmPose(new EulerAngle(0, 0, Math.toRadians(90)));
        });

        // ✅ Remove one saber from player’s hand
        handItem.setAmount(handItem.getAmount() - 1);

        // ✅ Direction setup
        Vector forward = player.getLocation().getDirection().normalize();
        Vector side = forward.clone().crossProduct(new Vector(0, 1, 0)).normalize().multiply(-1); // LEFT curve

        // ✅ Play throw sound and particles
        player.getWorld().spawnParticle(Particle.CRIT, player.getLocation().add(0, 1, 0), 10, 0.2, 0.2, 0.2, 0.05);

        new BukkitRunnable() {
            int ticks = 0;

            @Override
            public void run() {
                // Spin saber visually
                EulerAngle rot = armorStand.getRightArmPose();
                armorStand.setRightArmPose(rot.add(20, 0, 0));

                // ✅ Calculate curve offset
                double curveOffset = Math.sin(ticks / curveFrequency) * curveAmplitude;
                Vector curveVector = side.clone().multiply(curveOffset);

                // ✅ Move saber forward with left curve
                armorStand.teleport(armorStand.getLocation()
                        .add(forward.clone().multiply(speed))
                        .add(curveVector));

                // ✅ Loop throw sound while flying
                if (ticks % 16 == 0) {
                    armorStand.getWorld().playSound(armorStand.getLocation(), "melee.lightsaber.throw", 0.6f, 1.0f);
                }

                // Particle trail
                armorStand.getWorld().spawnParticle(Particle.SWEEP_ATTACK, armorStand.getLocation(), 1);

                // ✅ Block collision
                if (armorStand.getTargetBlockExact(1) != null && !armorStand.getTargetBlockExact(1).isPassable()) {
                    returnSaber(player, throwStack, armorStand);
                    cancel();
                    return;
                }

                // ✅ Entity collision
                for (Entity entity : armorStand.getNearbyEntities(1, 1, 1)) {
                    if (entity != player && entity instanceof LivingEntity) {
                        LivingEntity living = (LivingEntity) entity;
                        living.damage(damage, player);

                        // Hit effects
                        player.getWorld().playSound(armorStand.getLocation(), "melee.lightsaber.hit", 0.6f, 1.0f);
                        entity.getWorld().spawnParticle(Particle.CRIT, entity.getLocation().add(0, 1, 0), 10);

                        returnSaber(player, throwStack, armorStand);
                        cancel();
                        return;
                    }
                }

                // ✅ Max distance
                if (ticks > maxTicks) {
                    returnSaber(player, throwStack, armorStand);
                    cancel();
                }

                ticks++;
            }
        }.runTaskTimer(MandoMC.getInstance(), 0L, 1L);
    }

    /**
     * Handles returning the saber to player or dropping it.
     */
    private void returnSaber(Player player, ItemStack saber, ArmorStand stand) {
        if (!stand.isDead()) stand.remove();
        if (player.getInventory().firstEmpty() != -1) {
            player.getInventory().addItem(saber);
        } else {
            player.getWorld().dropItemNaturally(player.getLocation(), saber);
        }

        // ✅ return sound & particles
        player.getWorld().spawnParticle(Particle.END_ROD, player.getLocation().add(0, 1, 0), 10, 0.3, 0.3, 0.3, 0.05);
    }

    @EventHandler
    public void throwLightsaber(PlayerInteractEvent event) {
        // Only main hand clicks should count (ignore off-hand)
        if (event.getHand() != EquipmentSlot.HAND) return;

        Player player = event.getPlayer();
        ItemStack inHand = player.getInventory().getItemInMainHand();

        // ✅ Only works with shields that have custom model data
        if ((event.getAction() == Action.LEFT_CLICK_AIR || event.getAction() == Action.LEFT_CLICK_BLOCK) && player.isSneaking()) {
            if (inHand.getType() == Material.SHIELD) {
                ItemMeta meta = inHand.getItemMeta();
                if (meta != null && meta.hasCustomModelData()) {
                    // ✅ Check cooldown
                    long now = System.currentTimeMillis();
                    long lastThrow = lightsaberCooldown.getOrDefault(player.getUniqueId(), 0L);

                    if (now - lastThrow >= cooldown) {
                        lightsaberCooldown.put(player.getUniqueId(), now);
                        player.sendMessage(throwSaber);
                        playerThrowEvent(player);
                    } else {
                        long secondsLeft = (cooldown - (now - lastThrow)) / 1000;
                        player.sendMessage(tired.replace("{var}", String.valueOf(secondsLeft)));
                    }
                }
            }
        }
    }
}

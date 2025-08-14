package com.astuteflamez.mandomc.features.vehicles;

import com.astuteflamez.mandomc.features.vehicles.variants.VehicleType;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.*;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

public final class VehicleFactory {

    private VehicleFactory() {}

    public static Vehicle spawn(Player player, ItemStack handItem, VehicleType type) {
        // 1) Decrement main hand by exactly 1
        PlayerInventory inv = player.getInventory();
        ItemStack inHand = inv.getItemInMainHand();
        if (inHand == null || inHand.getType() == Material.AIR) return null;
        if (inHand.getAmount() <= 0) return null;

        ItemStack vehicleItem = handItem.clone(); vehicleItem.setAmount(1);
        ItemStack helmetItem  = handItem.clone(); helmetItem.setAmount(1);

        inHand.setAmount(inHand.getAmount() - 1);
        inv.setItemInMainHand(inHand.getAmount() > 0 ? inHand : null);

        // 2) Spawn mount: Phantom (air) or Horse (ground/hover)
        Entity mount = type.air ? createPhantom(player) : createArmorStand(player);
        if (!mount.isValid()) return null;

        // keep mount pitch flat at spawn (no tilted helmet visuals)
        Location mLoc = mount.getLocation();
        mount.setRotation(mLoc.getYaw(), 0f);

        // 3) Spawn invisible “carrier/skin” zombie and put the model as a helmet
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
            z.setInvisible(false);
            if (z.getEquipment() != null) {
                z.getEquipment().setHelmet(helmetItem);
                z.getEquipment().setHelmetDropChance(0.0f);
            }
        });
        // zero the visual pitch so helmets don’t tilt
        zombie.teleport(zombie.getLocation().setDirection(zombie.getLocation().getDirection().setY(0)));

        // 4) Build the concrete vehicle + register
        Vehicle vehicle = type.construct(player, mount, zombie, vehicleItem);
        VehicleManager.register(player, vehicle);

        // 5) Play the vehicle spawn sound (from Vehicle/Type)
        playSpawnSound(player, mount.getLocation(), vehicle.getSound());

        return vehicle;
    }

    private static Phantom createPhantom(Player player) {
        World w = player.getWorld();
        return w.spawn(player.getLocation(), Phantom.class, p -> {
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
            if (p.getAttribute(Attribute.GENERIC_SCALE) != null) {
                p.getAttribute(Attribute.GENERIC_SCALE).setBaseValue(3.0); // larger visual
            }
        });
    }

    private static ArmorStand createArmorStand(Player player) {
        World w = player.getWorld();
        return w.spawn(player.getLocation(), ArmorStand.class, stand -> {
            stand.setSilent(true);
            stand.setInvulnerable(true);
            stand.setRemoveWhenFarAway(false);
            stand.setPersistent(true);
            stand.setCollidable(false);

            stand.setGravity(true);       // Let natural physics handle Y movement
            stand.setVisible(false);      // Hide model if you’re attaching a vehicle model
            stand.setMarker(false);       // false = has hitbox for mounting
            stand.setSmall(true);        // true = small hitbox, false = full size
            stand.setArms(false);         // Disable visual arms
            stand.setBasePlate(false);    // Remove base plate visual
        });
    }

    private static void playSpawnSound(Player owner, Location loc, String soundKey) {
        if (soundKey == null || soundKey.isEmpty()) return;

        float vol = 2.5f; // ~40 blocks audible
        float pitch = 1.0f;

        loc.getWorld().playSound(loc, soundKey, vol, pitch);
        owner.playSound(owner.getLocation(), soundKey, vol, pitch);
    }
}

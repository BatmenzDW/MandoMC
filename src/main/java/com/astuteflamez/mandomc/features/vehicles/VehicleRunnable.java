package com.astuteflamez.mandomc.features.vehicles;

import net.kyori.adventure.text.Component;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.type.Fence;
import org.bukkit.block.data.type.Slab;
import org.bukkit.block.data.type.Snow;
import org.bukkit.block.data.type.TrapDoor;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Zombie;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class VehicleRunnable extends BukkitRunnable {

    // =======================
    // Tunables
    // =======================

    // Boost
    private static final double BOOST_MULTIPLIER                = 1.75;
    private static final int    BOOST_DRAIN_PER_TICK            = 1;
    private static final int    BOOST_REGEN_RATE                = 1;
    private static final int    BOOST_REGEN_EVERY_TICKS         = 4;
    private static final int    BOOST_ZERO_REGEN_COOLDOWN_TICKS = 60;

    // Fuel
    private static final int    FUEL_DRAIN_EVERY_TICKS          = 20;
    private static final int    FUEL_DRAIN_AMOUNT               = 1;

    // Health
    private static final int    HEALTH_REGEN_EVERY_TICKS        = 200;
    private static final int    HEALTH_REGEN_AMOUNT             = 1;

    // HUD / warnings
    private static final int    HUD_BARS                        = 10;
    private static final int    MAX_HEALTH                      = 100;
    private static final int    LOW_WARN_HEALTH_THRESHOLD       = 20;
    private static final int    LOW_WARN_FUEL_THRESHOLD         = 100;
    private static final int    WARN_COOLDOWN_TICKS             = 20;

    // --- Ground physics (MTVehicles-like) ---
    private static final double MAX_SPEED_FWD                   = 1.20;
    private static final double MAX_SPEED_BACK                  = 0.45;
    private static final double ACCELERATION                    = 0.035;
    private static final double BRAKING                         = 0.06;
    private static final double FRICTION                        = 0.02;
    private static final boolean ICE_SLIPPERY                   = true;
    private static final boolean HONEY_SLOWDOWN                 = true;

    // Step configuration
    public enum StepMode { SLABS_ONLY, BLOCKS_ONLY, BOTH }
    private static final StepMode STEP_MODE                     = StepMode.BOTH;

    // Vertical ground-hug
    private static final double FALL_FAST_Y                     = -0.80;
    private static final double FLOAT_ON_GROUND_Y               = 0.00001;

    // Rotation (visual smoothing toward player yaw)
    private static final float  ROTATION_SPEED_DEG              = 4f;
    private static final float  ROTATION_SPEED_SLOW_DIV         = 3f;

    // Probe tuning
    private static final double PROBE_X_OFFSET                  = 0.95;
    private static final double PROBE_Y_OFFSET                  = 0.51;

    // NMS method names (modern uses "a", older uses "setLocation")
    private static final String NMS_MOVE_METHOD_PRIMARY         = "a";
    private static final String NMS_MOVE_METHOD_FALLBACK        = "setLocation";

    // =======================
    // Runtime state
    // =======================

    private int tickCounter = 0;
    private int lastWarnTick = -WARN_COOLDOWN_TICKS;

    private final Map<UUID, Integer> boostRegenCooldown = new HashMap<>();
    private final Map<UUID, Double> groundSpeed = new HashMap<>();

    // Cached reflective method per class to avoid lookups every tick
    private final Map<Class<?>, Method> cachedMoveMethod = new HashMap<>();

    private static final class BoostState {
        final double speedMultiplier;
        final int cooldownTicks;
        final int boost;

        BoostState(double speedMultiplier, int cooldownTicks, int boost) {
            this.speedMultiplier = speedMultiplier;
            this.cooldownTicks = cooldownTicks;
            this.boost = boost;
        }
    }

    @Override
    public void run() {
        tickCounter++;

        for (Player player : Bukkit.getOnlinePlayers()) {
            Vehicle vehicle = VehicleManager.get(player);
            if (vehicle == null || !vehicle.isInDrive()) {
                ensureCorrectVisibility(player, /*visible*/ true);
                continue;
            }

            if(!vehicle.isHoverMode()) ensureCorrectVisibility(player, /*visible*/ false);

            Entity entity = vehicle.getEntity();
            Zombie zombie = vehicle.getZombie();
            if (entity == null || zombie == null || entity.isDead() || zombie.isDead()) continue;

            UUID id = player.getUniqueId();

            // ---- BOOST ----
            BoostState boostState = computeAndApplyBoost(vehicle, id);

            // ---- MOVEMENT (drive by player yaw) ----
            if (vehicle.isHoverMode()) tickGround(vehicle, player, entity, boostState.speedMultiplier);
            else                       tickAir(vehicle,   player, entity, boostState.speedMultiplier);

            // keep tethered zombie with main entity
            zombie.teleport(entity.getLocation());

            // ---- FUEL / HEALTH ----
            drainFuelIfNeeded(vehicle);
            regenHealthIfNeeded(vehicle);

            // ---- HUD ----
            updateHud(player, vehicle, boostState);

            // ---- CRITICALS / WARN ----
            if (handleCriticalFailures(player, vehicle)) continue;
            maybeWarnLow(player, vehicle);
        }

        if (tickCounter >= 1200) tickCounter = 0;
    }

    // =======================
    // Visibility
    // =======================

    private void ensureCorrectVisibility(Player player, boolean visible) {
        if (player.isInvisible() == visible) {
            player.setInvisible(!visible);
        }
    }

    // =======================
    // Boost
    // =======================

    private BoostState computeAndApplyBoost(Vehicle vehicle, UUID id) {
        int maxBoost = vehicle.getMaxBoost();
        int boost = clamp(vehicle.getBoost(), 0, maxBoost);

        boolean isBoostKeyHeld = vehicle.isBoosting();
        boolean hasBoost = boost > 0;
        boolean currentlyBoosting = isBoostKeyHeld && hasBoost;

        int oldBoost = boost;
        double speedMul = 1.0;

        if (currentlyBoosting) {
            speedMul = BOOST_MULTIPLIER;
            boost = Math.max(0, boost - BOOST_DRAIN_PER_TICK);
        }

        if (oldBoost > 0 && boost == 0) {
            boostRegenCooldown.put(id, BOOST_ZERO_REGEN_COOLDOWN_TICKS);
        }

        int cd = boostRegenCooldown.getOrDefault(id, 0);
        if (cd > 0) {
            cd -= 1;
            if (cd <= 0) boostRegenCooldown.remove(id);
            else boostRegenCooldown.put(id, cd);
        }

        if (!currentlyBoosting && boost < maxBoost && cd == 0 && tickCounter % BOOST_REGEN_EVERY_TICKS == 0) {
            boost = Math.min(maxBoost, boost + BOOST_REGEN_RATE);
        }

        vehicle.setBoost(boost);
        return new BoostState(speedMul, cd, boost);
    }

    // =======================
    // AIR MOVEMENT  (drive with player yaw/pitch)
    // =======================

    private void tickAir(Vehicle vehicle, Player player, Entity entity, double speedMultiplier) {
        LivingEntity livingEntity = (LivingEntity) entity;
        livingEntity.setAI(true);

        int baseSpeed = Math.max(0, vehicle.getSpeed());
        entity.setGravity(false);

        float yaw = player.getLocation().getYaw();
        float pitch = player.getLocation().getPitch();

        // Visual: try hard to rotate body (optional but nice)
        setEntityYawHard(entity, yaw, pitch);

        // Movement: compute direction from camera yaw/pitch
        Vector forward = dirFromYaw(yaw);
        double yComp = Math.sin(Math.toRadians(-pitch));
        Vector dir = new Vector(forward.getX(), yComp, forward.getZ()).normalize();

        entity.setVelocity(dir.multiply(baseSpeed * speedMultiplier));
    }

    // =======================
    // GROUND / HOVER MOVEMENT (drive with player yaw)
    // =======================

    private void tickGround(Vehicle vehicle, Player player, Entity entity, double speedMultiplier) {
        // --- ROTATION: use A/D (turnInput) for hover yaw ---
        float maxStep = ROTATION_SPEED_DEG;
        if (ICE_SLIPPERY && getBlockBelow(entity).getType().toString().contains("ICE")) maxStep *= 2f;

        // scale turn at very low speeds so it doesn’t snap-spin when nearly stopped
        UUID id = player.getUniqueId();
        double curSpeed = groundSpeed.getOrDefault(id, 0.0);
        float speedScale = (curSpeed < 0.1) ? (1f / ROTATION_SPEED_SLOW_DIV) : 1f;

        // Clamp turn input to [-1,1] and convert to degrees-per-tick
        double ti = vehicle.getTurnInput();
        if (ti > 1) ti = 1; else if (ti < -1) ti = -1;
        float appliedDeg = (float)(maxStep * speedScale * ti);

        float currentYaw = entity.getLocation().getYaw();
        float newYaw = wrapDegrees(currentYaw + appliedDeg);

        // Write the yaw robustly (keeps rider)
        setEntityYawHard(entity, newYaw, entity.getLocation().getPitch());

        // --- SPEED INTEGRATION (unchanged) ---
        curSpeed = advanceSpeed(curSpeed, vehicle.getForwardInput(), vehicle.isBraking(), getBlockBelow(entity).getType());
        groundSpeed.put(id, curSpeed);

        // --- STEP LOGIC (unchanged) ---
        boolean movedUp = stepCheckAndAdjust(entity);

        // --- VELOCITY: drive by the entity’s facing (not camera yaw) ---
        Vector vel = composeGroundVelocityFromEntityFacing(entity, curSpeed * speedMultiplier, movedUp);

        // Honey slowdown (unchanged)
        if (HONEY_SLOWDOWN && getBlockBelow(entity).getType() == Material.HONEY_BLOCK) {
            vel.multiply(0.2);
            if (vel.clone().setY(0).length() < 0.05) {
                Vector dir = entity.getLocation().getDirection().setY(0);
                if (dir.lengthSquared() > 0) dir.normalize();
                vel.setX(dir.getX() * 0.05);
                vel.setZ(dir.getZ() * 0.05);
            }
        }

        entity.setVelocity(vel);
    }

    private Vector composeGroundVelocityFromEntityFacing(Entity entity, double speed, boolean movedUp) {
        Location loc = entity.getLocation();
        Vector dir = loc.getDirection().clone();
        dir.setY(0);
        if (dir.lengthSquared() == 0) dir.zero(); else dir.normalize();

        Vector xz = dir.multiply(speed);

        Material below = getBlockBelow(entity).getType();
        double y;
        if (isPassable(below)) {
            y = below.toString().contains("WATER") ? +0.01 : FALL_FAST_Y;
        } else {
            y = FLOAT_ON_GROUND_Y;
        }

        if (movedUp) y = Math.min(y, 0.0);
        return new Vector(xz.getX(), y, xz.getZ());
    }

    private double advanceSpeed(double current, double forward, boolean braking, Material blockBelow) {
        double accel = ACCELERATION;
        double brake = BRAKING;
        double friction = FRICTION;

        if (ICE_SLIPPERY && blockBelow.toString().contains("ICE")) friction *= 0.5;

        if (braking) {
            if (current > 0) return Math.max(0, current - brake);
            if (current < 0) return Math.min(0, current + brake);
            return 0.0;
        }

        if (forward > 0) {
            if (current < 0) return Math.min(0, current + brake);
            return Math.min(current + accel, MAX_SPEED_FWD);
        } else if (forward < 0) {
            if (current > 0) return Math.max(0, current - brake);
            return Math.max(current - accel, -MAX_SPEED_BACK);
        } else {
            BigDecimal round = BigDecimal.valueOf(current).setScale(2, BigDecimal.ROUND_DOWN);
            double r = round.doubleValue();
            if (r == 0.0) return 0.0;
            if (r > 0) return Math.max(0, current - friction);
            return Math.min(0, current + friction);
        }
    }

    // --- Step logic (carpet/snow/slab/block) using NMS move for Y steps ---

    private boolean stepCheckAndAdjust(Entity entity) {
        Location base = entity.getLocation();
        Location ahead = blockAhead(base, PROBE_X_OFFSET, PROBE_Y_OFFSET, 0.0);
        Location belowAhead = ahead.clone().add(0, -1, 0);
        Location aboveAhead = ahead.clone().add(0, 1, 0);

        Block blockAhead = ahead.getBlock();
        Block blockBelowAhead = belowAhead.getBlock();
        Block blockAboveAhead = aboveAhead.getBlock();
        Block blockBelow = base.clone().add(0, -0.2, 0).getBlock();

        double frac = fractional(base.getY());
        boolean onGroundY = (frac < 0.01 || frac > 0.99);
        boolean onBottomSlabY = Math.abs(frac - 0.5) < 0.01;

        BlockData dataAhead = blockAhead.getBlockData();
        BlockData dataBelowAhead = blockBelowAhead.getBlockData();

        // Path/Farmland gentle exit
        if (blockBelow.getType().toString().contains("PATH") || blockBelow.getType().toString().contains("FARMLAND")) {
            if (!isPassable(blockAboveAhead)) { stopHorizontal(entity); return false; }
            if (!blockAhead.getType().toString().contains("PATH") && !blockAhead.getType().toString().contains("FARMLAND")) {
                stepNms(entity, +0.0625);
                return true;
            }
            return false;
        }

        // Carpets
        if (blockAhead.getType().toString().contains("CARPET")) {
            if (!isPassable(blockAboveAhead)) { stopHorizontal(entity); return false; }
            if (onGroundY) { stepNms(entity, +0.0625); return true; }
            return false;
        }

        // Snow layers
        if (dataAhead instanceof Snow snow) {
            double layerHeight = snowLayerHeight(snow.getLayers());
            if (horizontalSpeed(entity) > 0.1) slowTo(entity, 0.1);

            if (Math.abs(layerHeight - frac) < 1e-6) return false;
            stepNms(entity, layerHeight - frac);
            return true;
        }

        // Hard blockers: fence/wall/trapdoor
        if (dataAhead instanceof Fence || blockAhead.getType().toString().contains("WALL") || dataAhead instanceof TrapDoor) {
            stopHorizontal(entity);
            return false;
        }

        boolean passableAhead = isPassable(blockAhead);
        boolean passableAboveAhead = isPassable(blockAboveAhead);

        // On bottom slab Y
        if (STEP_MODE != StepMode.BLOCKS_ONLY && onBottomSlabY) {
            if (passableAhead) {
                stepNms(entity, -0.5);
                return false;
            }
            if (dataAhead instanceof Slab slab && slab.getType() == Slab.Type.BOTTOM) {
                return false; // continue on bottom slab
            }
            if (!passableAboveAhead) { stopHorizontal(entity); return false; }
            stepNms(entity, +0.5);
            return true;
        }

        // Not on slab Y
        if (!passableAhead) {
            if (dataAhead instanceof Slab slab) {
                if (slab.getType() == Slab.Type.BOTTOM) {
                    if (STEP_MODE != StepMode.BLOCKS_ONLY) {
                        if (!passableAboveAhead) { stopHorizontal(entity); return false; }
                        if (onGroundY) stepNms(entity, +0.5);
                        else stepNms(entity, Math.max(0, 0.5 - frac));
                        return true;
                    } else {
                        stopHorizontal(entity); return false;
                    }
                }
            }
            // Full block / top slab
            if (!passableAboveAhead) { stopHorizontal(entity); return false; }
            if (STEP_MODE != StepMode.SLABS_ONLY) {
                if (onGroundY) stepNms(entity, +1.0);
                else stepNms(entity, Math.max(0, 1.0 - frac));
                return true;
            } else {
                stopHorizontal(entity); return false;
            }
        } else {
            // Air ahead: maybe step down if bottom slab below
            if (dataBelowAhead instanceof Slab slab && slab.getType() == Slab.Type.BOTTOM) {
                stepNms(entity, -0.5);
                return false;
            }
        }

        return false;
    }

    private static boolean isPassable(Block b) { return b.isPassable(); }

    private static void stopHorizontal(Entity e) {
        Vector v = e.getVelocity();
        e.setVelocity(new Vector(0, v.getY(), 0));
    }

    // === NMS step with forward nudge (no Bukkit teleport; keeps passenger) ===
    private void stepNms(Entity e, double dy) {
        final double FORWARD_EPSILON = (dy > 0.0) ? 0.28 : 0.14;
        Location l = e.getLocation();
        Vector dir = dirFromYaw(l.getYaw());
        double nx = l.getX() + dir.getX() * FORWARD_EPSILON;
        double ny = l.getY() + dy;
        double nz = l.getZ() + dir.getZ() * FORWARD_EPSILON;
        moveEntityRaw(e, nx, ny, nz, l.getYaw(), l.getPitch());
    }

    // === LOW-LEVEL RAW MOVE (reflection) ===
    private void moveEntityRaw(Entity bukkitEntity, double x, double y, double z, float yaw, float pitch) {
        try {
            Object craftEntity = bukkitEntity; // CraftEntity
            Method getHandle = craftEntity.getClass().getMethod("getHandle");
            Object nmsEntity = getHandle.invoke(craftEntity);

            Method m = cachedMoveMethod.get(nmsEntity.getClass());
            if (m == null) {
                try {
                    m = nmsEntity.getClass().getMethod(NMS_MOVE_METHOD_PRIMARY, double.class, double.class, double.class, float.class, float.class);
                } catch (NoSuchMethodException ignored) {
                    m = nmsEntity.getClass().getMethod(NMS_MOVE_METHOD_FALLBACK, double.class, double.class, double.class, float.class, float.class);
                }
                m.setAccessible(true);
                cachedMoveMethod.put(nmsEntity.getClass(), m);
            }
            m.invoke(nmsEntity, x, y, z, yaw, pitch);
        } catch (Throwable t) {
            // Last resort
            Location loc = bukkitEntity.getLocation();
            loc.set(x, y, z);
            loc.setYaw(yaw);
            loc.setPitch(pitch);
            bukkitEntity.teleport(loc);
        }
    }

    // =======================
    // Direction/Velocity helpers (player-yaw based)
    // =======================

    // Build a flat XZ direction from yaw degrees (no pitch)
    private static Vector dirFromYaw(float yawDeg) {
        double yawRad = Math.toRadians(yawDeg);
        double x = -Math.sin(yawRad); // MC yaw: 0 -> -Z
        double z =  Math.cos(yawRad);
        return new Vector(x, 0, z);
    }

    private Vector composeGroundVelocityFacingYaw(float yawDeg, Entity entity, double speed, boolean movedUp) {
        Vector dir = dirFromYaw(yawDeg);
        Vector xz = dir.multiply(speed);

        Material below = getBlockBelow(entity).getType();
        double y;
        if (isPassable(below)) {
            if (below.toString().contains("WATER")) y = +0.01;
            else y = FALL_FAST_Y;
        } else {
            y = FLOAT_ON_GROUND_Y;
        }

        if (movedUp) y = Math.min(y, 0.0);
        return new Vector(xz.getX(), y, xz.getZ());
    }

    // Try multiple ways to set yaw so it visually rotates
    private void setEntityYawHard(Entity e, float yaw, float pitch) {
        Location cur = e.getLocation();

        // 1) NMS raw move (best with rider)
        moveEntityRaw(e, cur.getX(), cur.getY(), cur.getZ(), yaw, pitch);

        // 2) Bukkit hint
        try { e.setRotation(yaw, pitch); } catch (Throwable ignored) {}

        // 3) Tiny rotate-teleport to force render refresh
        try {
            Location tweak = cur.clone();
            tweak.setYaw(yaw);
            tweak.setPitch(pitch);
            tweak.add(0.00001, 0.0, 0.00001);
            e.teleport(tweak);
        } catch (Throwable ignored) {}
    }

    // =======================
    // World/block helpers
    // =======================

    private static double snowLayerHeight(int layers) {
        return switch (layers) {
            case 1 -> 0.125;
            case 2 -> 0.25;
            case 3 -> 0.375;
            case 4 -> 0.5;
            case 5 -> 0.625;
            case 6 -> 0.75;
            case 7 -> 0.875;
            default -> 1.0;
        };
    }

    private static double fractional(double y) {
        double f = y - Math.floor(y);
        if (f < 0) f += 1.0;
        return f;
    }

    private static double horizontalSpeed(Entity e) {
        Vector v = e.getVelocity().clone();
        v.setY(0);
        return v.length();
    }

    private static void slowTo(Entity e, double target) {
        Vector v = e.getVelocity();
        Vector xz = v.clone().setY(0);
        if (xz.length() > target) {
            xz.normalize().multiply(target);
            e.setVelocity(new Vector(xz.getX(), v.getY(), xz.getZ()));
        }
    }

    private static Location blockAhead(Location base, double xOffset, double yOffset, double zOffset) {
        Location locvp = base.clone();
        Location fbvp = locvp.add(locvp.getDirection().setY(0).normalize().multiply(xOffset));
        float zvp = (float) (fbvp.getZ() + zOffset * Math.sin(Math.toRadians(fbvp.getYaw())));
        float xvp = (float) (fbvp.getX() + zOffset * Math.cos(Math.toRadians(fbvp.getYaw())));
        return new Location(base.getWorld(), xvp, base.getY() + yOffset, zvp, fbvp.getYaw(), fbvp.getPitch());
    }

    private static Block getBlockBelow(Entity e) {
        return e.getLocation().clone().add(0, -0.2, 0).getBlock();
    }

    private static boolean isPassable(Material m) {
        return m.isAir() || m.isTransparent() || m == Material.WATER || m == Material.CAVE_AIR;
    }

    // =======================
    // Fuel & Health
    // =======================

    private void drainFuelIfNeeded(Vehicle vehicle) {
        if (tickCounter % FUEL_DRAIN_EVERY_TICKS == 0) {
            vehicle.setFuel(Math.max(0, vehicle.getFuel() - FUEL_DRAIN_AMOUNT));
        }
    }

    private void regenHealthIfNeeded(Vehicle vehicle) {
        if (tickCounter % HEALTH_REGEN_EVERY_TICKS == 0) {
            int hp = vehicle.getHealth();
            if (hp > 0 && hp < MAX_HEALTH) {
                vehicle.setHealth(Math.min(MAX_HEALTH, hp + HEALTH_REGEN_AMOUNT));
            }
        }
    }

    // =======================
    // HUD / Warnings
    // =======================

    private void updateHud(Player player, Vehicle vehicle, BoostState boostState) {
        int hp   = clamp(vehicle.getHealth(), 0, MAX_HEALTH);
        int fuel = clamp(vehicle.getFuel(), 0, vehicle.getMaxFuel());
        int maxBoost = vehicle.getMaxBoost();

        String fuelBar = buildBar(
                fuel, vehicle.getMaxFuel(), HUD_BARS,
                ChatColor.DARK_GREEN, ChatColor.YELLOW, ChatColor.RED
        );

        String boostBar = (boostState.cooldownTicks > 0)
                ? buildBar(boostState.boost, maxBoost, HUD_BARS, ChatColor.DARK_GRAY, ChatColor.DARK_GRAY, ChatColor.DARK_GRAY)
                : buildBar(boostState.boost, maxBoost, HUD_BARS, ChatColor.AQUA, ChatColor.AQUA, ChatColor.DARK_AQUA);

        StringBuilder hud = new StringBuilder()
                .append(ChatColor.RED).append("HP ").append(hp).append("% ")
                .append(ChatColor.GRAY).append("| ")
                .append(ChatColor.GOLD).append("Fuel ").append(fuelBar).append(" ").append(fuel).append(" ")
                .append(ChatColor.GRAY).append("| ")
                .append(ChatColor.BLUE).append("Boost ").append(boostBar).append(" ").append(boostState.boost).append("/").append(maxBoost);

        if (boostState.cooldownTicks > 0) {
            int secs = (int) Math.ceil(boostState.cooldownTicks / 20.0);
            hud.append(" ").append(ChatColor.GRAY).append("(Cooling ").append(secs).append("s)");
        }

        player.sendActionBar(Component.text(hud.toString()));
    }

    private boolean handleCriticalFailures(Player player, Vehicle vehicle) {
        if (vehicle.getHealth() <= 0 || vehicle.getFuel() <= 0) {
            player.sendTitle(
                    ChatColor.RED + "" + ChatColor.BOLD + "EMERGENCY!",
                    ChatColor.YELLOW + "Vehicle Critical Failure!",
                    5, 20, 5
            );
            vehicle.crash();
            return true;
        }
        return false;
    }

    private void maybeWarnLow(Player player, Vehicle vehicle) {
        boolean lowHealth = vehicle.getHealth() <= LOW_WARN_HEALTH_THRESHOLD;
        boolean lowFuel   = vehicle.getFuel()  <= LOW_WARN_FUEL_THRESHOLD;

        if ((lowHealth || lowFuel) && tickCounter - lastWarnTick >= WARN_COOLDOWN_TICKS) {
            lastWarnTick = tickCounter;
            player.sendTitle(
                    ChatColor.RED + "" + ChatColor.BOLD + "WARNING",
                    ChatColor.YELLOW + "Fuel or Health Low!",
                    0, 10, 0
            );
        }
    }

    // =======================
    // Utils
    // =======================

    private static int clamp(int v, int min, int max) {
        return Math.max(min, Math.min(max, v));
    }

    private static float clampFloat(float v, float min, float max) {
        return Math.max(min, Math.min(max, v));
    }

    private static float shortestAngle(float deg) {
        deg = wrapDegrees(deg);
        if (deg > 180f) deg -= 360f;
        if (deg < -180f) deg += 360f;
        return deg;
    }

    private static float wrapDegrees(float deg) {
        deg %= 360f;
        if (deg < 0) deg += 360f;
        return deg;
    }

    private static String buildBar(int value, int max, int segments,
                                   ChatColor highColor, ChatColor midColor, ChatColor lowColor) {
        if (max <= 0) max = 1;
        double percent = Math.max(0.0, Math.min(1.0, (double) value / max));
        int filled = (int) Math.floor(percent * segments);

        ChatColor color = (percent > 0.5) ? highColor : (percent > 0.25 ? midColor : lowColor);

        StringBuilder sb = new StringBuilder();
        sb.append(ChatColor.DARK_GRAY).append("[");
        for (int i = 0; i < segments; i++) {
            sb.append(i < filled ? color : ChatColor.DARK_GRAY).append("|");
        }
        sb.append(ChatColor.DARK_GRAY).append("]");
        return sb.toString();
    }
}

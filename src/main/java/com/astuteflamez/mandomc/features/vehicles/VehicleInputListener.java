package com.astuteflamez.mandomc.features.vehicles;

import com.github.retrooper.packetevents.event.PacketListener;

public class VehicleInputListener implements PacketListener {

    /*private static final float  DEADZONE    = 0.01f; // tiny noise filter

    // Tunables
    private static final double BASE_SPEED  = 0.9;   // horizontal target speed
    private static final double VERT_SPEED  = 0.7;   // vertical speed (space/shift)
    private static final double ACCEL_BLEND = 0.35;  // 0..1 approach factor
    private static final double DAMPING     = 0.10;  // 0..1 velocity bleed
    private static final double MAX_SPEED   = 2.5;   // hard clamp

    private static final class InputState {
        volatile float forward;   // [-1..1]
        volatile float sideways;  // [-1..1]
        volatile boolean jump;
        volatile boolean sneak;
    }

    // Last inputs per player (updated from Netty thread)
    private final Map<UUID, InputState> inputs  = new ConcurrentHashMap<>();
    // Per-player driver tasks (run on main thread every tick)
    private final Map<UUID, BukkitTask> drivers = new ConcurrentHashMap<>();

    public FlyingCartInputListener() {
        Bukkit.getLogger().info("[MandoMC] FlyingCartInputListener (PLAYER_INPUT, Phantom, AI toggle, face player) registered.");
    }

    @Override
    public void onPacketReceive(PacketReceiveEvent event) {
        final PacketTypeCommon type = event.getPacketType();
        if (type != PacketType.Play.Client.PLAYER_INPUT) return;

        final UUID uuid = event.getUser().getUUID();
        if (uuid == null) return;

        // Parse packet (booleans → axes in [-1, 0, +1])
        final WrapperPlayClientPlayerInput packet = new WrapperPlayClientPlayerInput(event);
        float fwd  = (packet.isForward() ? 1f : 0f) + (packet.isBackward() ? -1f : 0f);
        float strf = (packet.isRight()   ? 1f : 0f) + (packet.isLeft()     ? -1f : 0f);
        boolean jump  = packet.isJump();
        boolean sneak = packet.isShift();

        // Deadzone
        if (Math.abs(fwd)  < DEADZONE) fwd = 0f;
        if (Math.abs(strf) < DEADZONE) strf = 0f;

        // Store the latest input
        final float forward  = fwd;
        final float sideways = strf;
        inputs.compute(uuid, (k, st) -> {
            if (st == null) st = new InputState();
            st.forward = forward;
            st.sideways = sideways;
            st.jump = jump;
            st.sneak = sneak;
            return st;
        });

        // Ensure a driver task is running for this pilot
        ensureDriver(uuid);
    }

    // ---------------------- helpers ----------------------

    private void ensureDriver(UUID uuid) {
        if (drivers.containsKey(uuid)) return;

        BukkitTask task = Bukkit.getScheduler().runTaskTimer(MandoMC.getInstance(), () -> {
            Player player = Bukkit.getPlayer(uuid);
            if (player == null || !player.isOnline()) {
                stopDriver(uuid);
                return;
            }

            FlyingCart fc = FlyingCartManager.get(player);
            if (fc == null) {
                stopDriver(uuid);
                return;
            }

            Entity mount = player.getVehicle();
            if (!(mount instanceof Phantom phantom)) {
                stopDriver(uuid);
                return;
            }

            // Pull latest inputs
            InputState st = inputs.get(uuid);
            if (st == null) {
                // No inputs yet; keep phantom idle with AI off
                setAI(phantom, false);
                phantom.setVelocity(new Vector(0, 0, 0));
                facePlayer(phantom, player); // still keep it oriented
                return;
            }

            boolean hasInput = st.forward != 0f || st.sideways != 0f || st.jump || st.sneak;

            // Toggle AI based on input
            setAI(phantom, hasInput);

            if (!hasInput) {
                // Stop and keep facing the player
                phantom.setVelocity(new Vector(0, 0, 0));
                facePlayer(phantom, player);
                return;
            }

            // ------- compute desired velocity from yaw + inputs -------
            double yawRad = Math.toRadians(normalizeYaw(player.getLocation()));
            double x = (-Math.sin(yawRad) * st.forward) + (Math.cos(yawRad) * st.sideways);
            double z = ( Math.cos(yawRad) * st.forward) + (Math.sin(yawRad) * st.sideways);

            // Normalize so diagonals aren't faster
            double mag = Math.hypot(x, z);
            if (mag > 1e-6) { x /= mag; z /= mag; }

            // Scale by input intensity (pressing both caps at 1.0)
            double inputScale = Math.min(1.0, Math.abs(st.forward) + Math.abs(st.sideways));
            double dx = x * BASE_SPEED * inputScale;
            double dz = z * BASE_SPEED * inputScale;

            double dy = 0.0;
            if (st.jump)  dy += VERT_SPEED;
            if (st.sneak) dy -= VERT_SPEED;

            Vector current = phantom.getVelocity();
            Vector damped  = current.multiply(1.0 - DAMPING);
            Vector desired = new Vector(dx, dy, dz);
            Vector blended = damped.multiply(1.0 - ACCEL_BLEND).add(desired.multiply(ACCEL_BLEND));

            // Clamp
            if (blended.lengthSquared() > MAX_SPEED * MAX_SPEED) {
                blended.normalize().multiply(MAX_SPEED);
            }

            // Make phantom look exactly where the player is facing
            facePlayer(phantom, player);

            // Apply movement
            phantom.setVelocity(blended);

            // Avoid hostile behavior if AI is on
            phantom.setTarget(null);
        }, 1L, 1L); // run every tick

        drivers.put(uuid, task);
    }

    private void stopDriver(UUID uuid) {
        BukkitTask t = drivers.remove(uuid);
        if (t != null) t.cancel();
        inputs.remove(uuid);
    }

    private void setAI(Phantom phantom, boolean enabled) {
        LivingEntity le = phantom;
        if (le.hasAI() != enabled) {
            le.setAI(enabled);
        }
        // Keep physics predictable
        phantom.setGravity(false);
        phantom.setCollidable(false);
        phantom.setPersistent(true);
        phantom.setRemoveWhenFarAway(false);
    }

    private void facePlayer(Phantom phantom, Player player) {
        // Rotate phantom to match player's facing without moving position
        Location playerLoc = player.getLocation();
        phantom.setRotation(playerLoc.getYaw(), -playerLoc.getPitch());
    }

    private float normalizeYaw(Location loc) {
        float yaw = loc.getYaw();
        while (yaw < 0) yaw += 360f;
        while (yaw >= 360f) yaw -= 360f;
        return yaw;
    }*/
}

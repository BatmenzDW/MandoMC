package com.astuteflamez.mandomc.features.vehicles.listeners;

import com.astuteflamez.mandomc.features.vehicles.Vehicle;
import com.astuteflamez.mandomc.features.vehicles.VehicleManager;
import com.github.retrooper.packetevents.event.PacketListener;
import com.github.retrooper.packetevents.event.PacketReceiveEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientPlayerInput;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.UUID;

public class InputListener implements PacketListener {

    private static final float AXIS_DEADZONE = 0.08f;

    @Override
    public void onPacketReceive(PacketReceiveEvent event) {
        if (event.getPacketType() != PacketType.Play.Client.PLAYER_INPUT) return;

        UUID uuid = event.getUser().getUUID();
        if (uuid == null) return;

        Player player = Bukkit.getPlayer(uuid);
        if (player == null) return;

        Vehicle vehicle = VehicleManager.get(player);
        if (vehicle == null) return;

        WrapperPlayClientPlayerInput packet = new WrapperPlayClientPlayerInput(event);

        // Axes (preferred); fall back to booleans if not available
        float forwardAxis  = getForwardAxis(packet);
        float sidewaysAxis = getSidewaysAxis(packet);

        forwardAxis  = applyDeadzone(forwardAxis);
        sidewaysAxis = applyDeadzone(sidewaysAxis);

        // Map to your Vehicle fields
        vehicle.setForwardInput(forwardAxis);

        // Ensure D=+1 (turn right), A=-1 (turn left). Flip sign if your impl differs.
        double turnInput = sidewaysAxis;
        vehicle.setTurnInput(turnInput);

        // Boost logic
        boolean forwardPressed  = safeIsForward(packet);
        boolean backwardPressed = safeIsBackward(packet);
        boolean jumpPressed     = safeIsJump(packet);

        boolean isBoost;
        if (vehicle.isHoverMode()) {
            isBoost = jumpPressed; // hover: boost on jump
        } else {
            isBoost = forwardPressed && !backwardPressed; // air (or non-hover ground): boost when only W
        }
        vehicle.setBoosting(isBoost);

        // Braking / handbrake
        vehicle.setBraking(safeIsSneak(packet));
    }

    // ---------- Helpers ----------

    private static float getForwardAxis(WrapperPlayClientPlayerInput p) {
        boolean forward = safeIsForward(p);
        boolean backward = safeIsBackward(p);
        if (forward == backward) return 0.0f; // both pressed or neither
        return forward ? 1.0f : -1.0f;
    }

    private static float getSidewaysAxis(WrapperPlayClientPlayerInput p) {
        boolean right = safeIsRight(p);
        boolean left = safeIsLeft(p);
        if (right == left) return 0.0f; // both pressed or neither
        return right ? 1.0f : -1.0f;
    }

    private static boolean safeIsForward(WrapperPlayClientPlayerInput p) {
        try { return p.isForward(); } catch (Throwable ignored) {
            return getForwardAxis(p) > 0.5f;
        }
    }

    private static boolean safeIsBackward(WrapperPlayClientPlayerInput p) {
        try { return p.isBackward(); } catch (Throwable ignored) {
            return getForwardAxis(p) < -0.5f;
        }
    }

    private static boolean safeIsRight(WrapperPlayClientPlayerInput p) {
        try { return p.isRight(); } catch (Throwable t) { return false; }
    }

    private static boolean safeIsLeft(WrapperPlayClientPlayerInput p) {
        try { return p.isLeft(); } catch (Throwable t) { return false; }
    }

    private static boolean safeIsJump(WrapperPlayClientPlayerInput p) {
        try { return p.isJump(); } catch (Throwable ignored) { return false; }
    }

    private static boolean safeIsSneak(WrapperPlayClientPlayerInput p) {
        try { return p.isShift(); } catch (Throwable ignored) { return false; }
    }

    private static float applyDeadzone(float v) {
        return Math.abs(v) < AXIS_DEADZONE ? 0.0f : v;
    }
}

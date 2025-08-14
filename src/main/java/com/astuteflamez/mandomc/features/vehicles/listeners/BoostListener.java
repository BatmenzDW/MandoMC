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

public class BoostListener implements PacketListener {

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

        // Forward key (W) means forward = true, Backward key (S) means backward = true
        boolean forwardPressed = packet.isForward();
        boolean backwardPressed = packet.isBackward();

        boolean isBoost;

        // Set boost if pressing W (forward) and NOT pressing S (backwards)
        if(!vehicle.isHoverMode()){
            isBoost = packet.isJump();
        }else{
            isBoost = forwardPressed && !backwardPressed;
        }

        // Store it in the vehicle
        vehicle.setBoosting(isBoost);

        // Optional: debug log
        // player.sendMessage("§7Boost: " + isBoost);
    }
}

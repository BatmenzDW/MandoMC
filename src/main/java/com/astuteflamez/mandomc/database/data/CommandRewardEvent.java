package com.astuteflamez.mandomc.database.data;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.UUID;

public class CommandRewardEvent extends RewardEvent {
    public CommandRewardEvent(int id, String eventName, String metaData, int poolId) {
        super(id, eventName, metaData, poolId);
    }

    @Override
    public void givePlayer(UUID uuid) {
        Player player = Bukkit.getPlayer(uuid);
        if (player != null) return;

        String command = String.format(metaData, player.getName());

        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command);
    }
}

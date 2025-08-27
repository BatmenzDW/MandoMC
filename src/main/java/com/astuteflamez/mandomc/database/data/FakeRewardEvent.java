package com.astuteflamez.mandomc.database.data;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.UUID;

public class FakeRewardEvent extends RewardEvent {
    public FakeRewardEvent(int id, String eventName, String metaData, int poolId) {
        super(id, eventName, metaData, poolId);
    }

    @Override
    public void givePlayer(UUID uuid) {

    }

    public static String trimBraces(String str) {
        if (str.startsWith("{") && str.endsWith("}")) str = str.substring(1, str.length() - 1);
        return str;
    }

    public String toString(){
        if (eventName.contains("Fake")){
            return eventName.split("Fake")[1];
        }
        return eventName;
    }
}

package com.astuteflamez.mandomc.database.data;

import java.util.UUID;

public class RewardEvent extends QuestReward {

    private int id;
    private String eventName;
    private String metaData;
    private int poolId;

    public RewardEvent(int id, String eventName, String metaData, int poolId) {
        this.id = id;
        this.eventName = eventName;
        this.metaData = metaData;
        this.poolId = poolId;
    }

    @Override
    public void givePlayer(UUID uuid) {

    }
}

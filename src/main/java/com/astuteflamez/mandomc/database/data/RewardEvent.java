package com.astuteflamez.mandomc.database.data;

import java.util.UUID;

public abstract class RewardEvent extends QuestReward {

    protected String eventName;
    protected String metaData;

    public RewardEvent(int id, String eventName, String metaData, int poolId) {
        this.id = id;
        this.eventName = eventName;
        this.metaData = metaData;
        this.poolId = poolId;
    }

    @Override
    public String getRewardDescription() {
        return eventName;
    }

    public static RewardEvent getRewardEvent(int id, String eventName, String metaData, int poolId) {
        if (eventName.contains("Command"))
        {
            return new CommandRewardEvent(id, eventName, metaData, poolId);
        }

        return null;
    }
}

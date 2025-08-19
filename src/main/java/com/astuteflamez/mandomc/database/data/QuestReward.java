package com.astuteflamez.mandomc.database.data;

import java.util.UUID;

public abstract class QuestReward {

    protected int poolId;
    protected int id;

    public void givePlayer(String uuid)
    {
        givePlayer(UUID.fromString(uuid));
    }

    public abstract void givePlayer(UUID uuid);

    public int getPoolId() {
        return poolId;
    }

    public int getId() {
        return id;
    }

    public String getRewardDescription() {
        return "None";
    }
}

package com.astuteflamez.mandomc.database.data;

import java.util.UUID;

public abstract class QuestReward {

    public void givePlayer(String uuid)
    {
        givePlayer(UUID.fromString(uuid));
    }

    public abstract void givePlayer(UUID uuid);
}

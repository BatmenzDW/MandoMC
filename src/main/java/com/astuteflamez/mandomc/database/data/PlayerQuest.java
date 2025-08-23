package com.astuteflamez.mandomc.database.data;

import java.util.UUID;

public class PlayerQuest {
    private final UUID playerUUID;
    private final String questName;
    private float questProgress = 0;

    public PlayerQuest(UUID playerUUID, String questName) {
        this.playerUUID = playerUUID;
        this.questName = questName;
    }

    public PlayerQuest(UUID playerUUID, String questName, float questProgress) {
        this.playerUUID = playerUUID;
        this.questName = questName;
        this.questProgress = questProgress;
    }

    public UUID getPlayerUUID() {
        return playerUUID;
    }

    public String getQuestName() {
        return questName;
    }

    public float getQuestProgress() {
        return questProgress;
    }

    public void setQuestProgress(float questProgress) {
        this.questProgress = questProgress;
    }

    public String toString(){
        return questName;
    }
}

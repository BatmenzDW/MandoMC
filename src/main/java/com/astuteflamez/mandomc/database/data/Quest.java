package com.astuteflamez.mandomc.database.data;

import java.sql.Timestamp;

public class Quest {
    private String QuestName;
    private String QuestDesc;
    private String Parent;
    private Timestamp ExpirationDateTime;
    private String QuestTrigger;
    private Integer RewardsPool;

    public Quest(String questName, String questDesc, Timestamp expirationDateTime, String trigger, Integer rewardsPool, String parent) {
        QuestName = questName;
        QuestDesc = questDesc;
        ExpirationDateTime = expirationDateTime;
        Parent = parent;
        QuestTrigger = trigger;
        RewardsPool = rewardsPool;
    }

    public Quest(String questName) {
        QuestName = questName;
    }

    public String getQuestName() {
        return QuestName;
    }

    public void setQuestName(String questName) {
        QuestName = questName;
    }

    public String getQuestDesc() {
        return QuestDesc;
    }

    public void setQuestDesc(String questDesc) {
        QuestDesc = questDesc;
    }

    public Timestamp getQuestExpirationDateTime(){
        return ExpirationDateTime;
    }

    public void setQuestExpirationDateTime(Timestamp expirationDateTime) {
        ExpirationDateTime = expirationDateTime;
    }

    public String getParent() {
        return Parent;
    }

    public void setParent(String parent) {
        Parent = parent;
    }

    public String getQuestTrigger() {return QuestTrigger;}

    public void setQuestTrigger(String questTrigger) {QuestTrigger = questTrigger;}

    public Integer getRewardsPool() {return RewardsPool;}

    public void setRewardsPool(Integer rewardsPool) {RewardsPool = rewardsPool;}
}

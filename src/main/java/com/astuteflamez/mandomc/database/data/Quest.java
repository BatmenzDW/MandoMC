package com.astuteflamez.mandomc.database.data;

import java.sql.Timestamp;
import java.util.List;

public class Quest {
    private String QuestName;
    private String QuestDesc;
    private String Parent;
    private Timestamp Expiration;
    private DurationEnum Duration;
    private String QuestTrigger;
    private Integer RewardsPool;
    private Integer Weight;

    public Quest(String questName, String questDesc, Timestamp expirationDateTime, String trigger, Integer rewardsPool, String parent, int duration, int weight) {
        QuestName = questName;
        QuestDesc = questDesc;
        Expiration = expirationDateTime;
        Parent = parent;
        QuestTrigger = trigger;
        RewardsPool = rewardsPool;
        Duration = DurationEnum.values()[duration];
        Weight = weight;
    }

    public Quest(String questName, String questDesc, Timestamp expirationDateTime, String trigger, Integer rewardsPool, String parent, int duration) {
        QuestName = questName;
        QuestDesc = questDesc;
        Expiration = expirationDateTime;
        Parent = parent;
        QuestTrigger = trigger;
        RewardsPool = rewardsPool;
        Duration = DurationEnum.values()[duration];
    }

    public Quest(String questName) {
        QuestName = questName;
    }

    public Quest(String questName, String questDesc, String trigger, String parent, int weight) {
        QuestName = questName;
        QuestDesc = questDesc;
        Parent = parent;
        QuestTrigger = trigger;
        Duration = DurationEnum.NONE;
        Weight = weight;
    }

    public Quest(String questName, String questDesc, String trigger, DurationEnum duration, String parent, int weight) {
        QuestName = questName;
        QuestDesc = questDesc;
        Duration = duration;
        Parent = parent;
        QuestTrigger = trigger;
        Weight = weight;
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

    public String getParent() {
        return Parent;
    }

    public void setParent(String parent) {
        Parent = parent;
    }

    public DurationEnum getDuration() {
        return Duration;
    }

    public void setDuration(DurationEnum duration) {
        Duration = duration;
    }

    public Timestamp getExpiration()
    {
        return Expiration;
    }

    public void setExpiration(Timestamp expiration){
        Expiration = expiration;
    }

    public int getWeight(){
        return Weight;
    }

    public void setWeight(int weight){
        Weight = weight;
    }

    public String getQuestTrigger() {return QuestTrigger;}

    public void setQuestTrigger(String questTrigger) {QuestTrigger = questTrigger;}

    public Integer getRewardsPool() {return RewardsPool;}

    public void setRewardsPool(Integer rewardsPool) {RewardsPool = rewardsPool;}

    public String getDisplayString(List<QuestReward> rewards){
        return QuestName + ": " + QuestDesc + " \n\t Rewards: " + rewards;
    }

    public String getDisplayString(List<QuestReward> rewards, float progress){
        return QuestName + ": " + QuestDesc + "   Parent: " + Parent + " \n§7   Rewards: " + rewards + "\n   Progress: " + Math.round(progress * 1000)/10 + "%§r";
    }

    public enum DurationEnum {
        NONE,
        DAILY,
        WEEKLY
    }
}

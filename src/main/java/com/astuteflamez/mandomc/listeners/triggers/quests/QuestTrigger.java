package com.astuteflamez.mandomc.listeners.triggers.quests;

import com.astuteflamez.mandomc.LangConfig;
import com.astuteflamez.mandomc.MandoMC;
import com.astuteflamez.mandomc.database.PlayerQuestsTable;
import com.astuteflamez.mandomc.database.data.PlayerQuest;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.advancement.AdvancementProgress;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public abstract class QuestTrigger implements Listener {

    protected static List<PlayerQuest> getTriggeredQuests(Player player, String trigger){
        ConsoleCommandSender console = Bukkit.getConsoleSender();
        List<PlayerQuest> quests = new ArrayList<PlayerQuest>();

        try {
            quests = PlayerQuestsTable.getTriggeredQuests(player.getUniqueId().toString(), trigger);
        } catch (SQLException e) {
            console.sendMessage("[MandoMC] there was an issue triggering " + trigger + " for "  + player.getName());
        }

        return quests;
    }

    protected static void updateTriggeredQuests(Player player, List<PlayerQuest> quests, String trigger){
        ConsoleCommandSender console = Bukkit.getConsoleSender();
        try {
            for (PlayerQuest quest : quests) {
                PlayerQuestsTable.updateQuestProgress(player.getUniqueId().toString(), quest.getQuestName(), quest.getQuestProgress());
            }
        } catch (SQLException e) {
            console.sendMessage("[MandoMC] there was an issue updating quests triggered by " + trigger + " for "  + player.getName());
        }
    }

    protected static void triggerQuests(Player player, String trigger, float amount){
        FileConfiguration config = LangConfig.get();
        List<PlayerQuest> quests = getTriggeredQuests(player, trigger);

        Bukkit.getLogger().info(player.getName() + " triggered " + trigger + " for amount" + amount);
        for (PlayerQuest quest : quests){
            quest.setQuestProgress(quest.getQuestProgress() + amount);
            player.sendMessage(String.format(config.getString("quests.progress", "InvalidKey"), quest.getQuestName(), quest.getQuestProgress() * 100));
            if (quest.getQuestProgress() >= 1.0f){
                triggerAdvancement(player, quest.getQuestName());
            }
        }

        updateTriggeredQuests(player, quests, trigger);
    }

    public static void triggerAdvancement(Player player, String quest){
        FileConfiguration config = LangConfig.get();

        String questKey = config.getString("quests." + quest + ".advancement");
        if (questKey == null) return;

        NamespacedKey advKey = NamespacedKey.fromString(questKey, MandoMC.getInstance());

        AdvancementProgress progress = player.getAdvancementProgress(Bukkit.getAdvancement(advKey));
        for (String criteria : progress.getRemainingCriteria())
        {
            progress.awardCriteria(criteria);
        }
    }
}

package com.astuteflamez.mandomc.listeners;

import com.astuteflamez.mandomc.LangConfig;
import com.astuteflamez.mandomc.MandoMC;

import com.astuteflamez.mandomc.database.PlayerQuestsTable;
import com.astuteflamez.mandomc.database.data.PlayerQuest;
import org.bukkit.Bukkit;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import java.sql.SQLException;
import java.util.List;

public class PlayerJoinListener implements Listener {
    private final MandoMC plugin;

    public PlayerJoinListener(MandoMC plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event){
        Player player = event.getPlayer();
        ConsoleCommandSender console = Bukkit.getConsoleSender();

        try {
            if (!player.hasPlayedBefore() | PlayerQuestsTable.getQuest(player.getUniqueId().toString(), LangConfig.get().getString("quests.phone_home.name")) == null) {
                giveTutorialQuest(player);
            }
        }
        catch (SQLException e) {
            console.sendMessage("[MandoMC] there was an issue checking tutorial quest for " + player.getName());
            console.sendMessage(e.getMessage());
        }

        try {
            // Gives daily/weekly quests
            List<PlayerQuest> inactive = PlayerQuestsTable.getInactiveTimedQuests(player.getUniqueId().toString());
            for (PlayerQuest quest : inactive){
                PlayerQuestsTable.playerStartQuest(player.getUniqueId().toString(), quest.getQuestName());
            }
        }
        catch (SQLException e) {
            console.sendMessage("[MandoMC] there was an issue giving daily/weekly quests to " + player.getName());
            console.sendMessage(e.getMessage());
        }
    }

    private void giveTutorialQuest(Player player) throws SQLException {
        String questName = LangConfig.get().getString("quests.phone_home.name");

        PlayerQuestsTable.playerStartQuest(player.getUniqueId().toString(), questName);

        player.sendMessage(LangConfig.get().getString("quests.tutorial.1.start", "InvalidKey"));
    }
}

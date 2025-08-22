package com.astuteflamez.mandomc.listeners.triggers.quests;

import com.astuteflamez.mandomc.MandoMC;
import com.astuteflamez.mandomc.database.data.Quest;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerCommandSendEvent;

import java.util.Collection;
import java.util.Set;

public class PlayerUseCommandListener extends QuestTrigger {

    public PlayerUseCommandListener() {}

    @EventHandler
    public void onPlayerCommandPreprocess(PlayerCommandPreprocessEvent e) {
        ConfigurationSection triggerConfig = MandoMC.getInstance().getConfig().getConfigurationSection("quest-commands");

        if (triggerConfig == null) return;

        Player p = e.getPlayer();
        String command = e.getMessage();

        Set<String> commandKeys = triggerConfig.getKeys(false);

        for (String commandKey : commandKeys) {
            if (command.contains(commandKey)) {
                String trigger = triggerConfig.getString(commandKey + ".quest.trigger");
                float amount = (float) triggerConfig.getDouble(commandKey + ".quest.amount");

                triggerQuests(p, trigger, amount);
            }
        }
    }
}

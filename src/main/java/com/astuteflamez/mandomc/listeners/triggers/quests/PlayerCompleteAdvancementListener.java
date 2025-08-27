package com.astuteflamez.mandomc.listeners.triggers.quests;

import com.astuteflamez.mandomc.MandoMC;
import com.astuteflamez.mandomc.database.data.Quest;
import org.bukkit.advancement.Advancement;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerAdvancementDoneEvent;

import java.util.Set;

public class PlayerCompleteAdvancementListener extends QuestTrigger {

    public PlayerCompleteAdvancementListener() {}

    @EventHandler
    public void onPlayerAdvancementDone(PlayerAdvancementDoneEvent event) {
        ConfigurationSection triggerConfig = MandoMC.getInstance().getConfig().getConfigurationSection("quest-advancements");

        if (triggerConfig == null) return;

        Player player = event.getPlayer();
        Advancement advancement = event.getAdvancement();

        String name = advancement.getKey().getKey();

        Set<String> commandKeys = triggerConfig.getKeys(false);

        for (String commandKey : commandKeys) {
            if (commandKey.contains(name)) {
                String trigger = triggerConfig.getString(commandKey + ".quest.trigger");
                float amount = (float) triggerConfig.getDouble(commandKey + ".quest.amount", 1.0);

                triggerQuests(player, trigger, amount);
            }
        }
    }
}

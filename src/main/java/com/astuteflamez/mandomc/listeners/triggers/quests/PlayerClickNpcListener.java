package com.astuteflamez.mandomc.listeners.triggers.quests;

import com.astuteflamez.mandomc.MandoMC;
import com.astuteflamez.mandomc.database.data.PlayerQuest;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerInteractEntityEvent;

import java.util.List;
import java.util.Set;

public class PlayerClickNpcListener extends QuestTrigger {

    public PlayerClickNpcListener() {}

    @EventHandler
    public void onPlayerInteractEntityEvent(PlayerInteractEntityEvent event){
        ConfigurationSection npcConfig = MandoMC.getInstance().getConfig().getConfigurationSection("quest-npcs");

        Player player = event.getPlayer();
        Entity target = event.getRightClicked();

        // target is player or fake-player
        if (target instanceof Player pTarget) {
            var tags = pTarget.getScoreboardTags();

            // Change if our npcs have different tag
            if (!tags.contains("CITIZENS_NPC")){
                return;
            }

            String targetName = pTarget.getCustomName();
            if (targetName == null || npcConfig == null) return;

            Set<String> npcs = npcConfig.getKeys(false);

            for (String npc : npcs) {
                String customName = npcConfig.getString(npc + ".custom-name");
                if (!targetName.equals(customName)) continue;

                String trigger = npcConfig.getString(npc + ".quest.trigger");
                float amount = (float) npcConfig.getDouble(npc + "quest.amount");
                triggerQuests(player, trigger, amount);
            }
        }
    }


}

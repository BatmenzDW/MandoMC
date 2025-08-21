package com.astuteflamez.mandomc.listeners.triggers.quests;

import com.astuteflamez.mandomc.database.data.PlayerQuest;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerInteractEntityEvent;

import java.util.List;

public class PlayerClickNpcListener extends QuestTrigger {

    // replace with NPC custom name
    private final String REG_SHOP = "{\"text\":\"Reg Shop\",\"strikethrough\":false,\"obfuscated\":false,\"bold\":true,\"italic\":false,\"underlined\":false,\"color\":\"gold\"}";

    public PlayerClickNpcListener() {}

    @EventHandler
    public void onPlayerInteractEntityEvent(PlayerInteractEntityEvent event){
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
            if (targetName == null) return;

            switch (targetName){
                case REG_SHOP:
                    triggerQuests(player, "open_reg_shop", 1.0f);
                    break;
                default:
                    triggerQuests(player, "talk_to_npc_generic", 1.0f);
                    break;
            }
        }
    }

    private void triggerQuests(Player player, String trigger, float amount){
        List<PlayerQuest> quests = getTriggeredQuests(player, trigger);

        for (PlayerQuest quest : quests){
            quest.setQuestProgress(quest.getQuestProgress() + amount);
        }

        updateTriggeredQuests(player, quests, trigger);
    }
}

package com.astuteflamez.mandomc.features.items;

import com.astuteflamez.mandomc.guis.GUIManager;
import com.astuteflamez.mandomc.guis.RecipesGUI.RecipesHub;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class RecipesCommand implements CommandExecutor {

    private final GUIManager guiManager;

    public RecipesCommand(GUIManager guiManager) {
        this.guiManager = guiManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("Only players can use this command.");
            return true;
        }

        guiManager.openGUI(new RecipesHub(guiManager), player);
        return true;
    }

}

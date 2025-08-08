package com.astuteflamez.mandomc.commands;

import com.astuteflamez.mandomc.guis.EventsGUI.EventsHub;
import com.astuteflamez.mandomc.guis.GUIManager;
import org.bukkit.entity.Player;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class EventsCommand implements CommandExecutor {

    private final GUIManager guiManager;

    public EventsCommand(GUIManager guiManager) {
        this.guiManager = guiManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("Only players can use this command.");
            return true;
        }

        guiManager.openGUI(new EventsHub(guiManager), player);
        return true;
    }

}

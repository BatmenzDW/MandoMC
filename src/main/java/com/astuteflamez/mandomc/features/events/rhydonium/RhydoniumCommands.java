package com.astuteflamez.mandomc.features.events.rhydonium;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class RhydoniumCommands implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player p)) {
            sender.sendMessage("Only players can use this.");
            return true;
        }
        p.getInventory().addItem(RhydoniumWand.create());
        p.sendMessage(ChatColor.RED + "Rhydonium Wand received.");
        return true;
    }
}


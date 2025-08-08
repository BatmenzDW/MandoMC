package com.astuteflamez.mandomc.features.events.beskar;

import com.astuteflamez.mandomc.features.events.chesthunt.ChestHuntManager;
import com.astuteflamez.mandomc.features.events.koth.KothManager;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class OreEventCommands implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0 || args[0].equalsIgnoreCase("help")) {
            sender.sendMessage(ChatColor.YELLOW + "/oreevent start");
            sender.sendMessage(ChatColor.YELLOW + "/oreevent stop");
            sender.sendMessage(ChatColor.YELLOW + "/oreevent wand");
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "start" -> {
                if (OreEventManager.getInstance().isEventActive()) {
                    sender.sendMessage(ChatColor.RED + "Ore Event is already active.");
                    return true;
                }
                if(KothManager.getInstance().isActive()){
                    KothManager.getInstance().endActiveKoth();
                }
                if(ChestHuntManager.getInstance().isActive()){
                    ChestHuntManager.getInstance().stopChestHunt();
                }
                OreEventManager.getInstance().startOreEvent();
                sender.sendMessage(ChatColor.GREEN + "Ore Event started.");
                return true;
            }
            case "stop" -> {
                if (!OreEventManager.getInstance().isEventActive()) {
                    sender.sendMessage(ChatColor.RED + "Ore Event is not running.");
                    return true;
                }
                OreEventManager.getInstance().stop();
                sender.sendMessage(ChatColor.GREEN + "Ore Event stopped.");
                return true;
            }
            case "wand" -> {
                if (!(sender instanceof Player player)) {
                    sender.sendMessage(ChatColor.RED + "Only players can receive the wand.");
                    return true;
                }
                player.getInventory().addItem(OreEventWand.createWand());
                player.sendMessage(ChatColor.AQUA + "You received the Ore Event Wand!");
                return true;
            }
        }

        return false;
    }
}

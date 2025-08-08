package com.astuteflamez.mandomc.features.events.chesthunt;

import com.astuteflamez.mandomc.features.events.beskar.OreEventManager;
import com.astuteflamez.mandomc.features.events.koth.KothManager;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class ChestHuntCommands implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (args.length == 0) {
            sender.sendMessage(ChatColor.YELLOW + "/chesthunt start | stop | wand");
            return true;
        }

        if (args[0].equalsIgnoreCase("start")) {
            ChestHuntManager.getInstance().startChestHunt();
            if(KothManager.getInstance().isActive()){
                KothManager.getInstance().endActiveKoth();
            }
            if(OreEventManager.getInstance().isEventActive()){
                OreEventManager.getInstance().stop();
            }
            sender.sendMessage(ChatColor.GREEN + "Chest Hunt started!");
            return true;
        }

        if (args[0].equalsIgnoreCase("stop")) {
            ChestHuntManager.getInstance().stopChestHunt();
            sender.sendMessage(ChatColor.RED + "Chest Hunt stopped!");
            return true;
        }

        if (args[0].equalsIgnoreCase("wand") && sender instanceof Player) {
            Player player = (Player) sender;
            player.getInventory().addItem(ChestHuntWand.createWand());
            player.sendMessage(ChatColor.AQUA + "You received the ChestHunt Wand!");
            return true;
        }

        return true;
    }
}

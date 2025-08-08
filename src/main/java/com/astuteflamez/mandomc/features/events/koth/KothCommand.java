package com.astuteflamez.mandomc.features.events.koth;

import com.astuteflamez.mandomc.features.events.EventsConfig;
import com.astuteflamez.mandomc.features.events.beskar.OreEventManager;
import com.astuteflamez.mandomc.features.events.chesthunt.ChestHuntManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import java.util.Set;

public class KothCommand implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("koth.admin")) {
            sender.sendMessage(Component.text("You do not have permission to use this command.")
                    .color(NamedTextColor.RED));
            return true;
        }

        if (args.length == 0) {
            sendHelp(sender);
            return true;
        }

        String sub = args[0].toLowerCase();

        switch (sub) {
            case "start" -> {
                if (args.length < 2) {
                    sender.sendMessage(Component.text("Usage: /koth start <region>")
                            .color(NamedTextColor.RED));
                    return true;
                }

                if(OreEventManager.getInstance().isEventActive()){
                    OreEventManager.getInstance().stop();
                }
                if(ChestHuntManager.getInstance().isActive()){
                    ChestHuntManager.getInstance().stopChestHunt();
                }

                // Assuming you want to start the KOTH for the given region id args[1]
                // If your startKoth() needs the region id, modify accordingly:
                // KothManager.getInstance().startKoth(args[1]);
                KothManager.getInstance().startNextKoth();
                sender.sendMessage(Component.text("KOTH manually started.")
                        .color(NamedTextColor.GREEN));
            }

            case "end" -> {
                if (!KothManager.getInstance().isActive()) {
                    sender.sendMessage(Component.text("No KOTH is currently active.")
                            .color(NamedTextColor.YELLOW));
                    return true;
                }

                KothManager.getInstance().endActiveKoth();
                sender.sendMessage(Component.text("KOTH event force-ended.")
                        .color(NamedTextColor.GREEN));
            }

            default -> {
                sendHelp(sender);
            }
        }

        return true;
    }

    private void sendHelp(CommandSender sender) {
        sender.sendMessage(Component.text("KOTH Admin Commands:").color(NamedTextColor.YELLOW));
        sender.sendMessage(Component.text("/koth start <region> ").color(NamedTextColor.GRAY)
                .append(Component.text("- Start KOTH at region").color(NamedTextColor.DARK_GRAY)));
        sender.sendMessage(Component.text("/koth end ").color(NamedTextColor.GRAY)
                .append(Component.text("- End current KOTH").color(NamedTextColor.DARK_GRAY)));
    }
}

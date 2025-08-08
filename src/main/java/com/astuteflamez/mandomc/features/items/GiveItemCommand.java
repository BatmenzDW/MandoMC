package com.astuteflamez.mandomc.features.items;

import com.astuteflamez.mandomc.MandoMC;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class GiveItemCommand implements CommandExecutor, TabCompleter {

    private final ItemsManager itemsManager;

    public GiveItemCommand(MandoMC plugin) {
        this.itemsManager = plugin.getItemsManager();
    }

    private static final String PREFIX = "§6§lMandoMC §8» §7";

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        if (!(sender instanceof Player)) {
            sender.sendMessage(PREFIX + "§cOnly players can use this command.");
            return true;
        }

        Player player = (Player) sender;

        if (args.length < 1) {
            player.sendMessage(PREFIX + "§cUsage: /giveitem <item_id|all>");
            return true;
        }

        String itemId = args[0].toLowerCase();

        // ✅ Give all items
        if (itemId.equalsIgnoreCase("all")) {
            ConfigurationSection section = ItemsConfig.get().getConfigurationSection("items");
            if (section != null) {
                for (String id : section.getKeys(false)) {
                    ItemStack item = itemsManager.getItem(id);
                    if (item != null) {
                        player.getInventory().addItem(item);
                    }
                }
            }
            player.sendMessage(PREFIX + "§aAll items given.");
            return true;
        }

        // ✅ Give one item
        ItemStack item = itemsManager.getItem(itemId);
        if (item == null) {
            player.sendMessage(PREFIX + "§cThat item doesn’t exist.");
            return true;
        }

        player.getInventory().addItem(item);
        player.sendMessage(PREFIX + "§aYou received §e" + itemId + "§a!");
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> suggestions = new ArrayList<>();

        if (args.length == 1) {
            suggestions.add("all");
            ConfigurationSection section = ItemsConfig.get().getConfigurationSection("items");
            if (section != null) {
                Set<String> keys = section.getKeys(false);
                suggestions.addAll(keys);
            }
        }
        return suggestions;
    }
}

package com.astuteflamez.mandomc.commands;

import com.astuteflamez.mandomc.LangConfig;
import com.astuteflamez.mandomc.MandoMC;
import com.astuteflamez.mandomc.features.events.EventsConfig;
import com.astuteflamez.mandomc.features.items.BlastingRecipes;
import com.astuteflamez.mandomc.features.items.ItemsConfig;
import com.astuteflamez.mandomc.features.warps.WarpConfig;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class ReloadCommand implements CommandExecutor {

    private final MandoMC plugin;

    public ReloadCommand(MandoMC plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        if (!sender.hasPermission("mmc.reload")) {
            sender.sendMessage(Component.text("You don’t have permission to run this command.", NamedTextColor.RED));
            return true;
        }

        sender.sendMessage(Component.text("[MandoMC] Reloading configs...", NamedTextColor.YELLOW));

        // ✅ Reload configs
        plugin.reloadConfig();
        WarpConfig.reload();
        ItemsConfig.reload();
        EventsConfig.reload();
        LangConfig.reload();

        // ✅ Reload items and recipes
        plugin.getItemsManager().loadItems();
        plugin.getItemsManager().registerRecipes();
        new BlastingRecipes(plugin, plugin.getItemsManager()).register();

        sender.sendMessage(Component.text("✔ items.yml, warps.yml, events.yml, lang.yml reloaded.", NamedTextColor.GREEN));
        sender.sendMessage(Component.text("[MandoMC] Reload complete.", NamedTextColor.GOLD));

        return true;
    }
}

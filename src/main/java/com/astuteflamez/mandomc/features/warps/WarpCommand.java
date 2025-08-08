package com.astuteflamez.mandomc.features.warps;

import com.astuteflamez.mandomc.LangConfig;
import com.astuteflamez.mandomc.MandoMC;
import com.astuteflamez.mandomc.guis.GUIManager;
import com.astuteflamez.mandomc.guis.WarpsGUI.WarpsHub;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class WarpCommand implements CommandExecutor {

    private final GUIManager guiManager;
    private final String noPerm = MandoMC.PREFIX + MandoMC.color(LangConfig.get().getString("NoPerm"));
    private final String openWarpMenu = MandoMC.PREFIX + MandoMC.color(LangConfig.get().getString("OpenWarpMenu"));

    public WarpCommand(GUIManager guiManager) {
        this.guiManager = guiManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(noPerm);
            return true;
        }

        // ✅ Open the Warps GUI for the player
        guiManager.openGUI(new WarpsHub(guiManager), player);
        player.sendMessage(openWarpMenu);
        return true;
    }
}

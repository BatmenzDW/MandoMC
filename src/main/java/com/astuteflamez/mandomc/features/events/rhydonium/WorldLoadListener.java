package com.astuteflamez.mandomc.features.events.rhydonium;

import com.astuteflamez.mandomc.MandoMC;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.world.WorldLoadEvent;

public class WorldLoadListener implements Listener {

    @EventHandler
    public void onWorldLoad(WorldLoadEvent event) {
        World loadedWorld = event.getWorld();

        // 🔍 Log all currently loaded worlds
        for (World world : Bukkit.getWorlds()) {
            Bukkit.getLogger().info(" - " + world.getName());
        }

        // 🛢️ Check for 'world' specifically
        if (loadedWorld.getName().equalsIgnoreCase("world")) {
            RhydoniumManager.init(MandoMC.getInstance());
            RhydoniumLocationManager.getInstance().load();
            RhydoniumManager.getInstance().enable();
        }
    }
}

package com.astuteflamez.mandomc.features.events.rhydonium;

import com.astuteflamez.mandomc.MandoMC;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.ArmorStand;

public class RhydoniumManager {

    private static RhydoniumManager instance;
    private final RhydoniumBarrelSpawner barrelSpawner;
    private int fuelRegenTaskId = -1;

    private RhydoniumManager(MandoMC plugin) {
        this.barrelSpawner = new RhydoniumBarrelSpawner(plugin);
    }

    public static void init(MandoMC plugin) {
        if (instance == null) {
            instance = new RhydoniumManager(plugin);
        }
    }

    public static RhydoniumManager getInstance() {
        return instance;
    }

    public void enable() {
        barrelSpawner.spawnBarrels();
        startFuelRegenTask();
    }

    public void startFuelRegenTask() {
        if (fuelRegenTaskId != -1) return; // already running

        fuelRegenTaskId = Bukkit.getScheduler().scheduleSyncRepeatingTask(
                MandoMC.getInstance(),
                () -> {
                    RhydoniumBarrelSpawner spawner = getBarrelSpawner();
                    int refilled = 0;

                    for (World world : Bukkit.getWorlds()) {
                        for (ArmorStand stand : world.getEntitiesByClass(ArmorStand.class)) {
                            if (!spawner.isRhydoniumBarrel(stand)) continue;

                            int current = spawner.getFuel(stand);
                            if (current >= 100) continue;

                            spawner.setFuel(stand, current + 1);
                            refilled++;
                        }
                    }

                    if (refilled > 0) {
                    }
                },
                200L,  // ⏳ 10 seconds delay before first run
                200L   // 🔁 run every 10 seconds
        );
    }

    public RhydoniumBarrelSpawner getBarrelSpawner() {
        return barrelSpawner;
    }
}

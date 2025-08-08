package com.astuteflamez.mandomc.features.events.koth;

import com.astuteflamez.mandomc.LangConfig;
import com.astuteflamez.mandomc.MandoMC;
import com.astuteflamez.mandomc.features.events.EventsConfig;
import com.astuteflamez.mandomc.features.events.RandomEventScheduler;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;

import java.util.Optional;
import java.util.Set;

public class KothManager {

    private static KothManager instance;
    private final String kothStart = MandoMC.PREFIX + MandoMC.color(LangConfig.get().getString("KothStart"));

    private KothRegion activeRegion;
    private CaptureTracker captureTracker;

    private KothManager() {
        // Still needed for capture time or config values
        EventsConfig.get().getInt("events.koth.capture-time-seconds", 60);
    }

    public static KothManager getInstance() {
        if (instance == null) {
            instance = new KothManager();
        }
        return instance;
    }

    public void startNextKoth() {
        ConfigurationSection regionsSection = EventsConfig.get().getConfigurationSection("events.koth.regions");
        if (regionsSection == null || regionsSection.getKeys(false).isEmpty()) {
            Bukkit.getLogger().warning("[MandoMC] No regions defined for KOTH.");
            return;
        }

        Optional<String> regionId = regionsSection.getKeys(false).stream().findFirst();
        if (regionId.isEmpty()) {
            Bukkit.getLogger().warning("[MandoMC] KOTH region ID is empty.");
            return;
        }

        String regionPath = "events.koth.regions." + regionId.get();
        double centerX = EventsConfig.get().getDouble(regionPath + ".center.x");
        double centerZ = EventsConfig.get().getDouble(regionPath + ".center.z");

        activeRegion = new KothRegion(regionId.get());
        captureTracker = new CaptureTracker(activeRegion);

        Bukkit.broadcastMessage(kothStart.replace("{var}", activeRegion.getName()).replace("{x}", String.valueOf(centerX)).replace("{z}", String.valueOf(centerZ)));
    }

    public void endActiveKoth() {
        if (captureTracker != null) captureTracker.stopTracking();
        activeRegion = null;
        captureTracker = null;

        RandomEventScheduler.getInstance().start();
    }

    public boolean isActive() {
        return activeRegion != null;
    }

    public KothRegion getActiveRegion() {
        return activeRegion;
    }

    public CaptureTracker getCaptureTracker() {
        return captureTracker;
    }
}

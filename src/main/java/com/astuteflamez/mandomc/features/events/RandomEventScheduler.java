package com.astuteflamez.mandomc.features.events;

import com.astuteflamez.mandomc.MandoMC;
import com.astuteflamez.mandomc.features.events.beskar.OreEventManager;
import com.astuteflamez.mandomc.features.events.chesthunt.ChestHuntManager;
import com.astuteflamez.mandomc.features.events.koth.KothManager;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class RandomEventScheduler {

    private static final String PREFIX = "§4§lEvents §8» §7";

    private static RandomEventScheduler instance;
    private final Random random = new Random();
    private final List<EventType> weightedPool = new ArrayList<>();
    private EventType currentEvent = EventType.NONE;

    public static RandomEventScheduler getInstance() {
        if (instance == null) {
            instance = new RandomEventScheduler();
        }
        return instance;
    }

    public EventType getCurrentEvent() {
        return currentEvent;
    }

    public void start() {
        loadWeightedPoolFromConfig();
        scheduleNextEvent();
    }

    private void loadWeightedPoolFromConfig() {
        weightedPool.clear();

        var weights = MandoMC.getInstance().getConfig().getConfigurationSection("events.randomizer.weights");
        if (weights == null) {
            Bukkit.getLogger().warning("[Events] No weights found in config. Using fallback.");
            addWeighted(EventType.NONE, 10);
            addWeighted(EventType.KOTH, 20);
            addWeighted(EventType.ORE_EVENT, 30);
            addWeighted(EventType.CHEST_HUNT, 40);
            return;
        }

        addWeighted(EventType.NONE, weights.getInt("none", 10));
        addWeighted(EventType.KOTH, weights.getInt("koth", 30));
        addWeighted(EventType.ORE_EVENT, weights.getInt("ore_event", 30));
        addWeighted(EventType.CHEST_HUNT, weights.getInt("chest_hunt", 30));
    }

    private void addWeighted(EventType type, int weight) {
        for (int i = 0; i < weight; i++) {
            weightedPool.add(type);
        }
    }

    private void scheduleNextEvent() {
        int min = MandoMC.getInstance().getConfig().getInt("events.randomizer.min-delay", 20);
        int max = MandoMC.getInstance().getConfig().getInt("events.randomizer.max-delay", 30);

        int delayMinutes = min + random.nextInt(Math.max(1, max - min + 1));
        long delayTicks = delayMinutes * 60L * 20;

        new BukkitRunnable() {
            @Override
            public void run() {
                if (weightedPool.isEmpty()) {
                    Bukkit.getLogger().warning("[Events] Weighted pool is empty. Skipping event.");
                    return;
                }

                EventType selected = weightedPool.get(random.nextInt(weightedPool.size()));
                currentEvent = selected;

                if (selected == EventType.NONE) {
                    scheduleNextEvent();
                    return;
                }

                String eventName = getDisplayName(selected);
                Bukkit.broadcastMessage(PREFIX + "§e" + eventName + " §fis starting in §c1 minute§f...");

                for (Player player : Bukkit.getOnlinePlayers()) {
                    player.playSound(player.getLocation(), Sound.ENTITY_ENDER_DRAGON_AMBIENT, 0.8f, 1.0f);
                    player.sendTitle("§6§lServer Event", getColoredSubtitle(selected), 10, 60, 10);
                }

                new BukkitRunnable() {
                    @Override
                    public void run() {
                        triggerEvent(selected);
                    }
                }.runTaskLater(MandoMC.getInstance(), 60 * 20); // 1 minute delay
            }
        }.runTaskLater(MandoMC.getInstance(), delayTicks);
    }

    private void triggerEvent(EventType type) {
        switch (type) {
            case KOTH -> KothManager.getInstance().startNextKoth();
            case CHEST_HUNT -> ChestHuntManager.getInstance().startChestHunt();
            case ORE_EVENT -> OreEventManager.getInstance().startOreEvent();
        }

        Bukkit.broadcastMessage(PREFIX + "§a" + getDisplayName(type) + " §fhas started!");
        lastEventEndTime = System.currentTimeMillis(); // mark as last "event time"
    }

    private String getDisplayName(EventType type) {
        return switch (type) {
            case KOTH -> "KOTH";
            case CHEST_HUNT -> "Crate Rush";
            case ORE_EVENT -> "Beskar Mining";
            default -> "Unknown";
        };
    }

    private String getColoredSubtitle(EventType type) {
        return switch (type) {
            case KOTH -> "§cKOTH!";
            case CHEST_HUNT -> "§6Crate Rush!";
            case ORE_EVENT -> "§bBeskar Mining Event!";
            default -> "§7A mysterious event approaches...";
        };
    }

    private long lastEventEndTime = System.currentTimeMillis();
    public long getLastEventEndTime() {
        return lastEventEndTime;
    }
}

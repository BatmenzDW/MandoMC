package com.astuteflamez.mandomc.features.events.beskar;

import com.astuteflamez.mandomc.LangConfig;
import com.astuteflamez.mandomc.MandoMC;
import com.astuteflamez.mandomc.features.events.EventsConfig;
import com.astuteflamez.mandomc.features.events.RandomEventScheduler;
import com.astuteflamez.mandomc.features.items.ItemsManager;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.Item;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

import java.util.*;

public class OreEventManager {

    private static OreEventManager instance;
    private final Set<Location> activeOres = new HashSet<>();
    private final Map<Location, Material> originalBlocks = new HashMap<>();
    private boolean active = false;

    private final ItemsManager itemsManager = MandoMC.getInstance().getItemsManager();
    private int oreCount;
    private String worldName;

    private final String start = MandoMC.PREFIX + MandoMC.color(LangConfig.get().getString("BeskarMiningStart"));
    private final String end = MandoMC.PREFIX + MandoMC.color(LangConfig.get().getString("BeskarMiningEnd"));

    public static OreEventManager getInstance() {
        if (instance == null) instance = new OreEventManager();
        return instance;
    }

    private OreEventManager() {
        // Config loading is deferred to startOreEvent()
    }

    public void startOreEvent() {
        if (active) return;
        active = true;
        activeOres.clear();
        originalBlocks.clear();

        var cfg = EventsConfig.get();
        oreCount = cfg.getInt("events.oreevent.ore-count", 50);
        worldName = cfg.getString("events.oreevent.world", "Geonosis");

        OreLocationManager.getInstance().load();
        List<Location> allLocations = new ArrayList<>(OreLocationManager.getInstance().getLocations());
        Collections.shuffle(allLocations);

        List<Location> selected = allLocations.subList(0, Math.min(oreCount, allLocations.size()));

        for (Location loc : selected) {
            Location blockLoc = loc.getBlock().getLocation(); // Normalize to block coords
            Block block = blockLoc.getBlock();

            originalBlocks.put(blockLoc, block.getType());
            block.setType(Material.ANCIENT_DEBRIS);
            activeOres.add(blockLoc);
        }

        Bukkit.broadcastMessage(start);
    }

    public void stop() {
        if (!active) return;
        active = false;

        for (Location loc : activeOres) {
            Material original = originalBlocks.getOrDefault(loc, Material.STONE);
            loc.getBlock().setType(original);
        }

        activeOres.clear();
        originalBlocks.clear();
        Bukkit.broadcastMessage(end);

        RandomEventScheduler.getInstance().start();
    }

    public void handleBreak(Location loc) {
        Location blockLoc = loc.getBlock().getLocation(); // Normalize to block coords

        if (!active || !activeOres.contains(blockLoc)) return;

        ItemStack drop = itemsManager.getItem("beskar");
        if (drop == null) {
            Bukkit.getLogger().warning("[OreEvent] Failed to drop 'beskar': item not found.");
        } else {
            Item item = blockLoc.getWorld().dropItem(blockLoc.clone().add(0.5, 0.5, 0.5), drop);
            item.setVelocity(new Vector(0, 0, 0));
            loc.getWorld().playSound(loc, Sound.BLOCK_ANVIL_LAND, 0.8f, 1.0f);
        }

        Material original = originalBlocks.getOrDefault(blockLoc, Material.STONE);
        blockLoc.getBlock().setType(original);

        originalBlocks.remove(blockLoc);
        activeOres.remove(blockLoc);

        if (activeOres.isEmpty()) {
            stop();
        }

    }

    public boolean isOre(Location loc) {
        return activeOres.contains(loc.getBlock().getLocation()); // Normalize
    }

    public boolean isInWorld(World world) {
        return worldName != null && world.getName().equalsIgnoreCase(worldName);
    }

    public boolean isEventActive() {
        return active;
    }

    public int getRemainingOreCount() {
        return activeOres.size();
    }
}

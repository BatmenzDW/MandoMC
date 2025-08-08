package com.astuteflamez.mandomc.features.events.chesthunt;

import com.astuteflamez.mandomc.LangConfig;
import com.astuteflamez.mandomc.MandoMC;
import com.astuteflamez.mandomc.features.events.EventsConfig;
import com.astuteflamez.mandomc.features.events.RandomEventScheduler;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.*;

public class ChestHuntManager {

    private static ChestHuntManager instance;
    private static final String PREFIX = "§6§lChest Hunt §8» §7";

    private final Set<Location> activeChests = new HashSet<>();
    private final Random random = new Random();

    private boolean active = false;

    private final String start = MandoMC.PREFIX + MandoMC.color(LangConfig.get().getString("ChestHuntStart"));
    private final String end = MandoMC.PREFIX + MandoMC.color(LangConfig.get().getString("ChestHuntEnd"));

    private ChestHuntManager() {}

    public static ChestHuntManager getInstance() {
        if (instance == null) {
            instance = new ChestHuntManager();
        }
        return instance;
    }

    public void startChestHunt() {
        if (active) return;
        active = true;
        activeChests.clear();

        Bukkit.broadcastMessage(start);

        int chestCount = EventsConfig.get().getInt("events.chesthunt.chest-count", 15);
        List<Location> locations = new ArrayList<>(ChestLocationManager.getInstance().getLocations());
        Collections.shuffle(locations);

        List<Location> chosen = locations.subList(0, Math.min(chestCount, locations.size()));

        for (Location loc : chosen) {
            if (loc.getWorld().getName().equalsIgnoreCase("world")) continue;

            Block block = loc.getBlock();
            block.setType(Material.CHEST);
            Inventory chestInv = ((org.bukkit.block.Chest) block.getState()).getBlockInventory();
            fillChest(chestInv);

            activeChests.add(loc);
        }
    }

    public void stopChestHunt() {
        if (!active) return;

        active = false;
        for (Location loc : activeChests) {
            if (loc.getBlock().getType() == Material.CHEST) {
                loc.getBlock().setType(Material.AIR);
            }
        }
        activeChests.clear();

        Bukkit.broadcastMessage(end);

        RandomEventScheduler.getInstance().start();
    }

    public void fillChest(Inventory inv) {
        inv.clear();

        int rewardCount = 2 + random.nextInt(4); // 2–5 items

        List<Integer> slots = new ArrayList<>();
        for (int i = 0; i < inv.getSize(); i++) slots.add(i);
        Collections.shuffle(slots);

        for (int i = 0; i < rewardCount; i++) {
            ItemStack reward = ItemRewardManager.getInstance().getRandomReward();
            if (reward != null) {
                inv.setItem(slots.get(i), reward);
                Bukkit.getLogger().info("[ChestHunt] Placed " + reward.getType() + " into a chest.");
            }
        }
    }

    public void removeChest(Location loc) {
        activeChests.remove(loc);
        Bukkit.getLogger().info("[ChestHunt] Chest removed at " + loc + ". " + activeChests.size() + " chests remain.");
        if (active && activeChests.isEmpty()) {
            stopChestHunt();
        }
    }

    public boolean isActive() {
        return active;
    }

    public int getActiveChestCount() {
        return activeChests.size();
    }

    public Set<Location> getActiveChests() {
        return activeChests;
    }
}

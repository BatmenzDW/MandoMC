package com.astuteflamez.mandomc.features.events.chesthunt;

import com.astuteflamez.mandomc.MandoMC;
import com.astuteflamez.mandomc.features.items.ItemsManager;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class ItemRewardManager {

    private static ItemRewardManager instance;
    private final List<RewardEntry> rewardPool = new ArrayList<>();
    private double totalWeight = 0;
    private final Random random = new Random();

    private final ItemsManager itemsManager = MandoMC.getInstance().getItemsManager(); // ✅ hook into ItemsManager

    public static ItemRewardManager getInstance() {
        if (instance == null) {
            instance = new ItemRewardManager();
        }
        return instance;
    }

    private ItemRewardManager() {
        loadItems();
    }

    public void loadItems() {
        rewardPool.clear();
        totalWeight = 0;

        File file = new File(MandoMC.getInstance().getDataFolder(), "items.yml");
        if (!file.exists()) {
            Bukkit.getLogger().warning("[MandoMC] items.yml not found! No chest rewards loaded.");
            return;
        }

        FileConfiguration config = YamlConfiguration.loadConfiguration(file);

        if (config.getConfigurationSection("items") == null) return;

        for (String key : config.getConfigurationSection("items").getKeys(false)) {
            String path = "items." + key;

            // ✅ only load items that have rarity
            if (!config.contains(path + ".rarity")) continue;

            double rarity = config.getDouble(path + ".rarity", 1.0);

            ItemStack item = itemsManager.getItem(key); // ✅ use ItemsManager
            if (item == null) {
                Bukkit.getLogger().warning("[MandoMC] Item " + key + " has rarity but cannot be loaded from ItemsManager.");
                continue;
            }

            rewardPool.add(new RewardEntry(item, rarity));
            totalWeight += rarity;
        }

        Bukkit.getLogger().info("[MandoMC] Loaded " + rewardPool.size() + " rare-able items.");
    }

    public ItemStack getRandomReward() {
        if (rewardPool.isEmpty()) return null;

        double r = Math.random() * totalWeight;
        double cumulative = 0.0;

        for (RewardEntry entry : rewardPool) {
            cumulative += entry.rarity;
            if (r <= cumulative) {
                return entry.item.clone(); // ✅ clone so we don’t modify the original
            }
        }

        // fallback: last item
        return rewardPool.get(rewardPool.size() - 1).item.clone();
    }

    private static class RewardEntry {
        ItemStack item;
        double rarity;

        RewardEntry(ItemStack item, double rarity) {
            this.item = item;
            this.rarity = rarity;
        }
    }
}

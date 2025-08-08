package com.astuteflamez.mandomc.features.items;

import com.astuteflamez.mandomc.MandoMC;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ItemsConfig {

    private static File file;
    private static FileConfiguration customFile;

    /**
     * ✅ Setup on first load
     */
    public static void setup() {
        file = new File(MandoMC.getInstance().getDataFolder(), "items.yml");

        if (!file.exists()) {
            try (InputStream inputStream = MandoMC.class.getResourceAsStream("/items.yml");
                 OutputStream outputStream = new FileOutputStream(file)) {

                if (inputStream != null) {
                    byte[] buffer = new byte[1024];
                    int length;
                    while ((length = inputStream.read(buffer)) > 0) {
                        outputStream.write(buffer, 0, length);
                    }
                }
            } catch (IOException e) {
                MandoMC.getInstance().getLogger().warning("⚠ Could not create items.yml!");
                e.printStackTrace();
            }
        }

        customFile = YamlConfiguration.loadConfiguration(file);
    }

    public static FileConfiguration get() {
        return customFile;
    }

    public static void save() {
        try {
            customFile.save(file);
        } catch (IOException e) {
            MandoMC.getInstance().getLogger().warning("⚠ Couldn't save items.yml");
            e.printStackTrace();
        }
    }

    /**
     * ✅ Reloads items.yml from disk
     */
    public static void reload() {
        customFile = YamlConfiguration.loadConfiguration(file);
        int count = get().getConfigurationSection("items") != null
                ? get().getConfigurationSection("items").getKeys(false).size() : 0;
        MandoMC.getInstance().getLogger().info("🔄 items.yml reloaded. Found " + count + " items.");
    }

    /**
     * ✅ Helper for recipe lines
     */
    public static List<String> getRecipe(String id) {
        return get().getStringList("items." + id + ".recipe");
    }

    /**
     * ✅ Helper for ingredients
     */
    public static Map<Character, String> getIngredients(String id) {
        Map<Character, String> map = new HashMap<>();
        String path = "items." + id + ".ingredients";

        if (get().isConfigurationSection(path)) {
            for (String key : get().getConfigurationSection(path).getKeys(false)) {
                map.put(key.charAt(0), get().getString(path + "." + key));
            }
        }
        return map;
    }

    public static boolean hasRecipe(String id) {
        // Checks if the recipe section exists and has at least one row defined
        return get().contains("items." + id + ".recipe")
                && !get().getStringList("items." + id + ".recipe").isEmpty();
    }

}

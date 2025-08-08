package com.astuteflamez.mandomc.features.events.chesthunt;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class ChestLocationManager {

    private static ChestLocationManager instance;
    private final File file;
    private final Gson gson = new Gson();
    private final List<ChestLocation> locations = new ArrayList<>();

    // ✅ Only chests in this world are allowed
    private static final String ALLOWED_WORLD = "DeathStar";

    private ChestLocationManager() {
        file = new File(Bukkit.getServer().getPluginManager().getPlugin("MandoMC").getDataFolder(), "chest_locations.json");
        load();
    }

    public static ChestLocationManager getInstance() {
        if (instance == null) {
            instance = new ChestLocationManager();
        }
        return instance;
    }

    // ✅ Returns only valid locations in DeathStar world
    public List<Location> getLocations() {
        List<Location> result = new ArrayList<>();
        for (ChestLocation cl : locations) {
            if (!ALLOWED_WORLD.equals(cl.world)) continue; // 🚫 skip any non-DeathStar entries

            World world = Bukkit.getWorld(cl.world);
            if (world != null) {
                result.add(new Location(world, cl.x, cl.y, cl.z));
            }
        }
        return result;
    }

    // ✅ Only add chests from DeathStar
    public void addLocation(Location loc) {
        if (loc.getWorld() == null || !ALLOWED_WORLD.equals(loc.getWorld().getName())) {
            Bukkit.getLogger().warning("[ChestHunt] Tried to add a chest in world '" +
                    (loc.getWorld() != null ? loc.getWorld().getName() : "null") +
                    "' but only '" + ALLOWED_WORLD + "' is allowed.");
            return;
        }
        locations.add(new ChestLocation(loc.getWorld().getName(), loc.getX(), loc.getY(), loc.getZ()));
        save();
    }

    public void removeLocation(Location loc) {
        locations.removeIf(cl -> cl.matches(loc));
        save();
    }

    private void load() {
        if (!file.exists()) return;
        try (FileReader reader = new FileReader(file)) {
            Type listType = new TypeToken<List<ChestLocation>>() {}.getType();
            List<ChestLocation> loaded = gson.fromJson(reader, listType);
            if (loaded != null) {
                // ✅ Only load chests in DeathStar
                for (ChestLocation cl : loaded) {
                    if (ALLOWED_WORLD.equals(cl.world)) {
                        locations.add(cl);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void save() {
        try (FileWriter writer = new FileWriter(file)) {
            gson.toJson(locations, writer);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // ✅ Inner class for JSON storage
    private static class ChestLocation {
        String world;
        double x, y, z;

        ChestLocation(String world, double x, double y, double z) {
            this.world = world;
            this.x = x;
            this.y = y;
            this.z = z;
        }

        boolean matches(Location loc) {
            return world.equals(loc.getWorld().getName())
                    && x == loc.getX()
                    && y == loc.getY()
                    && z == loc.getZ();
        }
    }
}

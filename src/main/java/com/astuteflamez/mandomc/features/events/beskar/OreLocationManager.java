// ✅ OreLocationManager.java
package com.astuteflamez.mandomc.features.events.beskar;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import java.io.*;
import java.util.*;

public class OreLocationManager {

    private static OreLocationManager instance;
    private final Set<Location> locations = new HashSet<>();
    private final File file = new File("plugins/MandoMC/ore-locations.json");

    public static OreLocationManager getInstance() {
        if (instance == null) instance = new OreLocationManager();
        return instance;
    }

    public void addLocation(Location loc) {
        locations.add(loc);
        save();
    }

    public void removeLocation(Location loc) {
        locations.remove(loc);
        save();
    }

    public Set<Location> getLocations() {
        return locations;
    }

    public void load() {
        locations.clear();
        if (!file.exists()) return;
        try (FileReader reader = new FileReader(file)) {
            JSONArray array = (JSONArray) new JSONParser().parse(reader);
            for (Object obj : array) {
                JSONObject o = (JSONObject) obj;
                Location loc = new Location(
                        Bukkit.getWorld((String) o.get("world")),
                        ((Long) o.get("x")),
                        ((Long) o.get("y")),
                        ((Long) o.get("z"))
                );
                locations.add(loc);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void save() {
        JSONArray array = new JSONArray();
        for (Location loc : locations) {
            JSONObject obj = new JSONObject();
            obj.put("world", loc.getWorld().getName());
            obj.put("x", loc.getBlockX());
            obj.put("y", loc.getBlockY());
            obj.put("z", loc.getBlockZ());
            array.add(obj);
        }
        try (FileWriter writer = new FileWriter(file)) {
            writer.write(array.toJSONString());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
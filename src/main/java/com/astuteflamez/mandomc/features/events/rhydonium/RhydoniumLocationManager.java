package com.astuteflamez.mandomc.features.events.rhydonium;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import java.io.*;
import java.util.HashSet;
import java.util.Set;

public class RhydoniumLocationManager {
    private static RhydoniumLocationManager instance;
    private final Set<Location> locations = new HashSet<>();
    private final File file = new File("plugins/MandoMC/rhydonium-locations.json");

    public static RhydoniumLocationManager getInstance() {
        if (instance == null) instance = new RhydoniumLocationManager();
        return instance;
    }

    public void add(Location loc) {
        locations.add(loc);
        save();
    }

    public void remove(Location loc) {
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

                // ✅ World safety check
                World world = Bukkit.getWorld((String) o.get("world"));
                if (world == null) {
                    continue;
                }

                // ✅ Correct coordinate casting
                double x = ((Long) o.get("x")).doubleValue();
                double y = ((Long) o.get("y")).doubleValue();
                double z = ((Long) o.get("z")).doubleValue();

                Location loc = new Location(world, x, y, z);
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

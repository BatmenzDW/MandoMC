package com.astuteflamez.mandomc.features.events.koth;

import com.astuteflamez.mandomc.MandoMC;
import com.astuteflamez.mandomc.features.events.EventsConfig;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.World;
import org.bukkit.scheduler.BukkitRunnable;

public class KothRegion {

    private final String id;
    private final String name;
    private final String worldName;

    // Shape type: "circle" or "square"
    private final String shape;

    // Circle params
    private final double centerX;
    private final double centerY;
    private final double centerZ;
    private final double radius;

    // Square params
    private final double minX, maxX, minY, maxY, minZ, maxZ;

    public KothRegion(String id) {
        this.id = id;

        // ✅ Updated paths: events.koth.regions.<id>.<field>
        String basePath = "events.koth.regions." + id;

        this.name = EventsConfig.get().getString(basePath + ".name", "Unknown");
        this.shape = EventsConfig.get().getString(basePath + ".shape", "circle").toLowerCase();
        this.worldName = EventsConfig.get().getString(basePath + ".world", "world");

        if (shape.equals("circle")) {
            this.centerX = EventsConfig.get().getDouble(basePath + ".center.x");
            this.centerY = EventsConfig.get().getDouble(basePath + ".center.y");
            this.centerZ = EventsConfig.get().getDouble(basePath + ".center.z");
            this.radius = EventsConfig.get().getDouble(basePath + ".radius");
            // Not used for circles
            this.minX = this.maxX = this.minY = this.maxY = this.minZ = this.maxZ = 0;
        } else if (shape.equals("square")) {
            this.minX = EventsConfig.get().getDouble(basePath + ".min.x");
            this.maxX = EventsConfig.get().getDouble(basePath + ".max.x");
            this.minY = EventsConfig.get().getDouble(basePath + ".min.y");
            this.maxY = EventsConfig.get().getDouble(basePath + ".max.y");
            this.minZ = EventsConfig.get().getDouble(basePath + ".min.z");
            this.maxZ = EventsConfig.get().getDouble(basePath + ".max.z");
            // Not used for squares
            this.centerX = this.centerY = this.centerZ = this.radius = 0;
        } else {
            throw new IllegalArgumentException("Invalid shape for KOTH region: " + shape);
        }
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getWorldName() {
        return worldName;
    }

    public boolean isInRegion(Location loc) {
        if (!loc.getWorld().getName().equals(worldName)) return false;

        double x = loc.getX();
        double y = loc.getY();
        double z = loc.getZ();

        if (shape.equals("circle")) {
            double dx = x - centerX;
            double dz = z - centerZ;
            double horizontalDistSq = dx * dx + dz * dz;
            return horizontalDistSq <= (radius * radius);
        } else { // square
            return x >= minX && x <= maxX
                    && y >= minY && y <= maxY
                    && z >= minZ && z <= maxZ;
        }
    }

    /**
     * Shows the KOTH region outline with particles for a few seconds.
     */
    public void showRegionParticles() {
        World world = Bukkit.getWorld(worldName);
        if (world == null) return;

        new BukkitRunnable() {
            KothManager kothManager = KothManager.getInstance();

            @Override
            public void run() {
                if (!kothManager.isActive()) {
                    cancel();
                    return;
                }

                if (shape.equals("circle")) {
                    drawCircle(world, centerX, centerY, centerZ, radius, Particle.HAPPY_VILLAGER);
                } else {
                    drawBox(world, minX, maxX, minY, maxY, minZ, maxZ, Particle.HAPPY_VILLAGER);
                }
            }
        }.runTaskTimer(MandoMC.getInstance(), 0L, 2L);
    }

    private void drawCircle(World world, double cx, double cy, double cz, double radius, Particle particle) {
        Location center = new Location(world, cx, cy, cz);

        for (double theta = 0; theta < 2 * Math.PI; theta += Math.PI / 16) {
            double xOffset = radius * Math.cos(theta);
            double zOffset = radius * Math.sin(theta);
            Location particleLocation = center.clone().add(xOffset, 0, zOffset);

            world.spawnParticle(particle, particleLocation, 1);
        }
    }

    private void drawBox(World world, double minX, double maxX, double minY, double maxY,
                         double minZ, double maxZ, Particle particle) {
        double step = 0.5;

        // Bottom square
        for (double x = minX; x <= maxX; x += step) {
            world.spawnParticle(particle, x, minY, minZ, 1);
            world.spawnParticle(particle, x, minY, maxZ, 1);
        }
        for (double z = minZ; z <= maxZ; z += step) {
            world.spawnParticle(particle, minX, minY, z, 1);
            world.spawnParticle(particle, maxX, minY, z, 1);
        }

        // Top square
        for (double x = minX; x <= maxX; x += step) {
            world.spawnParticle(particle, x, maxY, minZ, 1);
            world.spawnParticle(particle, x, maxY, maxZ, 1);
        }
        for (double z = minZ; z <= maxZ; z += step) {
            world.spawnParticle(particle, minX, maxY, z, 1);
            world.spawnParticle(particle, maxX, maxY, z, 1);
        }

        // Vertical edges
        for (double y = minY; y <= maxY; y += step) {
            world.spawnParticle(particle, minX, y, minZ, 1);
            world.spawnParticle(particle, maxX, y, minZ, 1);
            world.spawnParticle(particle, minX, y, maxZ, 1);
            world.spawnParticle(particle, maxX, y, maxZ, 1);
        }
    }
}

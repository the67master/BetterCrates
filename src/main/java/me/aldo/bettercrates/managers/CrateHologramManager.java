package me.aldo.bettercrates.managers;

import me.aldo.bettercrates.BetterCrates;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.Chunk;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;

import java.util.Arrays;
import java.util.List;

public class CrateHologramManager {
    private static final String MAIN_TAG = "BetterCratesHologram";
    private final BetterCrates plugin;

    public CrateHologramManager(BetterCrates plugin) {
        this.plugin = plugin;
    }

    public void refreshAll() {
        for (String id : plugin.getCrateManager().getCrateIds()) {
            for (String stored : plugin.getCrateManager().getConfiguration().getStringList("crates." + id + ".placed")) {
                Location location = parseLocation(stored);
                if (location != null && location.getWorld().isChunkLoaded(location.getBlockX() >> 4, location.getBlockZ() >> 4)) {
                    refreshAt(id, location);
                }
            }
        }
    }

    public void refreshCrate(String id) {
        if (!plugin.getCrateManager().crateExists(id)) return;
        for (String stored : plugin.getCrateManager().getConfiguration().getStringList("crates." + id + ".placed")) {
            Location location = parseLocation(stored);
            if (location != null && location.getWorld().isChunkLoaded(location.getBlockX() >> 4, location.getBlockZ() >> 4)) {
                refreshAt(id, location);
            }
        }
    }

    public void refreshAt(String id, Location blockLocation) {
        removeAt(blockLocation);
        YamlConfiguration config = plugin.getCrateManager().getConfiguration();
        String base = "crates." + id + ".hologram.";
        if (!config.getBoolean(base + "enabled", true)) return;
        List<String> lines = config.getStringList(base + "lines");
        if (lines.isEmpty()) lines = Arrays.asList("%crate_name%", "&7Right-Click to open!", "&7Left-Click to Preview!");
        String displayName = config.getString("crates." + id + ".settings.display-name", "&e" + id);
        String locationTag = locationTag(blockLocation);
        int count = Math.min(3, lines.size());
        for (int i = 0; i < count; i++) {
            String line = lines.get(i);
            if (line == null) line = "";
            line = line.replace("%crate_name%", displayName);
            Location spawn = blockLocation.clone().add(0.5, 2.15 - (i * 0.27), 0.5);
            ArmorStand stand = (ArmorStand) blockLocation.getWorld().spawnEntity(spawn, EntityType.ARMOR_STAND);
            stand.setVisible(false);
            stand.setGravity(false);
            stand.setMarker(true);
            stand.setBasePlate(false);
            stand.setSmall(true);
            stand.setCustomName(plugin.color(line));
            stand.setCustomNameVisible(true);
            stand.setCanPickupItems(false);
            stand.setRemoveWhenFarAway(false);
            stand.addScoreboardTag(MAIN_TAG);
            stand.addScoreboardTag(locationTag);
        }
    }

    public void removeAt(Location blockLocation) {
        if (blockLocation == null || blockLocation.getWorld() == null) return;
        String tag = locationTag(blockLocation);
        for (Entity entity : blockLocation.getWorld().getNearbyEntities(blockLocation.clone().add(0.5, 1.5, 0.5), 1.5, 3.0, 1.5)) {
            if (entity.getType() == EntityType.ARMOR_STAND && entity.getScoreboardTags().contains(MAIN_TAG) && entity.getScoreboardTags().contains(tag)) {
                entity.remove();
            }
        }
    }

    public void handleChunkLoad(Chunk chunk) {
        World world = chunk.getWorld();
        for (String id : plugin.getCrateManager().getCrateIds()) {
            for (String stored : plugin.getCrateManager().getConfiguration().getStringList("crates." + id + ".placed")) {
                String[] parts = stored.split(";");
                if (parts.length != 4 || !world.getName().equals(parts[0])) continue;
                try {
                    int x = Integer.parseInt(parts[1]);
                    int y = Integer.parseInt(parts[2]);
                    int z = Integer.parseInt(parts[3]);
                    if ((x >> 4) == chunk.getX() && (z >> 4) == chunk.getZ()) {
                        refreshAt(id, new Location(world, x, y, z));
                    }
                } catch (NumberFormatException ignored) {
                }
            }
        }
    }

    private Location parseLocation(String stored) {
        String[] parts = stored.split(";");
        if (parts.length != 4) return null;
        World world = Bukkit.getWorld(parts[0]);
        if (world == null) return null;
        try {
            return new Location(world, Integer.parseInt(parts[1]), Integer.parseInt(parts[2]), Integer.parseInt(parts[3]));
        } catch (NumberFormatException ex) {
            return null;
        }
    }

    private String locationTag(Location location) {
        return "BCH_" + location.getWorld().getUID().toString().replace("-", "") + "_" +
                location.getBlockX() + "_" + location.getBlockY() + "_" + location.getBlockZ();
    }
}

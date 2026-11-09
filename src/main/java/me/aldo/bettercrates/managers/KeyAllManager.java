package me.aldo.bettercrates.managers;

import me.aldo.bettercrates.BetterCrates;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.configuration.ConfigurationSection;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class KeyAllManager {
    private final BetterCrates plugin;
    private int taskId = -1;
    private final Random random = new Random();

    public KeyAllManager(BetterCrates plugin) { this.plugin = plugin; }

    public void start() {
        if (taskId != -1) Bukkit.getScheduler().cancelTask(taskId);
        if (!plugin.getConfig().getBoolean("keyall.enabled", true)) return;
        long interval = Math.max(1L, plugin.getConfig().getLong("keyall.interval-seconds", 86400L));
        long next = plugin.getConfig().getLong("keyall.next-run", 0L);
        if (next <= 0L) { next = System.currentTimeMillis() + interval * 1000L; plugin.getConfig().set("keyall.next-run", next); plugin.saveConfig(); }
        taskId = Bukkit.getScheduler().scheduleSyncRepeatingTask(plugin, new Runnable() {
            public void run() {
                long target = plugin.getConfig().getLong("keyall.next-run", 0L);
                if (target <= 0L) { setNextRun(); return; }
                if (System.currentTimeMillis() >= target) { distribute(); setNextRun(); }
            }
        }, 20L, 20L);
    }

    public void stop() { if (taskId != -1) { Bukkit.getScheduler().cancelTask(taskId); taskId = -1; } }
    public void reload() { stop(); start(); }

    public void setInterval(long seconds) {
        seconds = Math.max(1L, seconds);
        plugin.getConfig().set("keyall.interval-seconds", seconds);
        plugin.getConfig().set("keyall.next-run", System.currentTimeMillis() + seconds * 1000L);
        plugin.saveConfig();
        reload();
    }

    private void setNextRun() {
        long interval = Math.max(1L, plugin.getConfig().getLong("keyall.interval-seconds", 86400L));
        plugin.getConfig().set("keyall.next-run", System.currentTimeMillis() + interval * 1000L);
        plugin.saveConfig();
    }

    public long getRemainingSeconds() {
        long next = plugin.getConfig().getLong("keyall.next-run", 0L);
        if (next <= 0L) return Math.max(0L, plugin.getConfig().getLong("keyall.interval-seconds", 86400L));
        return Math.max(0L, (next - System.currentTimeMillis() + 999L) / 1000L);
    }

    public String getFormattedRemaining() { return formatDuration(getRemainingSeconds()); }

    public static String formatDuration(long seconds) {
        seconds = Math.max(0L, seconds);
        long days = seconds / 86400L;
        long hours = (seconds % 86400L) / 3600L;
        long minutes = (seconds % 3600L) / 60L;
        long secs = seconds % 60L;
        if (days > 0) return days + "d " + hours + "h";
        if (seconds >= 3600L) return (seconds / 3600L) + "h " + minutes + "m";
        if (seconds >= 60L) return minutes + "m " + secs + "s";
        return secs + "s";
    }

    public static long parseDuration(String input) {
        if (input == null) return -1L;
        Matcher matcher = Pattern.compile("(\\d+)\\s*([dhms])", Pattern.CASE_INSENSITIVE).matcher(input);
        int end = 0; long total = 0L; boolean found = false;
        while (matcher.find()) {
            String gap = input.substring(end, matcher.start()).trim();
            if (!gap.isEmpty()) return -1L;
            long amount;
            try { amount = Long.parseLong(matcher.group(1)); } catch (NumberFormatException ex) { return -1L; }
            char unit = Character.toLowerCase(matcher.group(2).charAt(0));
            long multiplier = unit == 'd' ? 86400L : unit == 'h' ? 3600L : unit == 'm' ? 60L : 1L;
            if (amount > Long.MAX_VALUE / multiplier || total > Long.MAX_VALUE - amount * multiplier) return -1L;
            total += amount * multiplier; end = matcher.end(); found = true;
        }
        return found && input.substring(end).trim().isEmpty() ? total : -1L;
    }

    private List<String> getEligibleKeys() {
        ConfigurationSection section = plugin.getConfig().getConfigurationSection("keyall.keys");
        List<String> ids = new ArrayList<String>();
        if (section != null && !section.getKeys(false).isEmpty()) {
            for (String id : section.getKeys(false)) if (plugin.getCrateManager().keyExists(id)) ids.add(id);
        } else {
            ids.addAll(plugin.getCrateManager().getKeyIds());
        }
        Collections.sort(ids);
        return ids;
    }

    private String chooseWeighted(List<String> ids) {
        double total = 0.0;
        for (String id : ids) { double w = plugin.getConfig().getDouble("keyall.keys." + id, 1.0); if (w > 0 && !Double.isNaN(w) && !Double.isInfinite(w)) total += w; }
        if (total <= 0) return null;
        double roll = random.nextDouble() * total;
        for (String id : ids) { double w = plugin.getConfig().getDouble("keyall.keys." + id, 1.0); if (w <= 0 || Double.isNaN(w) || Double.isInfinite(w)) continue; roll -= w; if (roll < 0) return id; }
        for (String id : ids) { double weight = plugin.getConfig().getDouble("keyall.keys." + id, 1.0); if (weight > 0 && !Double.isNaN(weight) && !Double.isInfinite(weight)) return id; }
        return ids.isEmpty() ? null : ids.get(0);
    }

    private void distribute() {
        if (!plugin.getConfig().getBoolean("keyall.enabled", true)) return;
        List<String> ids = getEligibleKeys();
        if (ids.isEmpty()) { plugin.getLogger().warning("KeyAll skipped: no keys are configured."); return; }
        String mode = plugin.getConfig().getString("keyall.distribution", "random").toLowerCase(java.util.Locale.ENGLISH);
        List<String> rewards = new ArrayList<String>();
        if (mode.equals("all")) rewards.addAll(ids);
        else if (mode.equals("weighted")) { String selected = chooseWeighted(ids); if (selected != null) rewards.add(selected); }
        else rewards.add(ids.get(random.nextInt(ids.size())));
        if (rewards.isEmpty()) { plugin.getLogger().warning("KeyAll skipped: no eligible key has a positive weight."); return; }
        for (Player player : Bukkit.getOnlinePlayers()) {
            for (String id : rewards) {
                ItemStack item = plugin.getCrateManager().createKeyItem(id, 1);
                if (item == null) continue;
                java.util.Map<Integer, ItemStack> leftovers = player.getInventory().addItem(item);
                for (ItemStack leftover : leftovers.values()) player.getWorld().dropItemNaturally(player.getLocation(), leftover);
            }
        }
        String broadcast = plugin.getConfig().getString("keyall.broadcast", "&6&lKEYALL! &eEveryone has received their KeyAll reward.");
        if (broadcast != null && !broadcast.isEmpty()) Bukkit.broadcastMessage(plugin.color(plugin.guiText(broadcast)));
        plugin.getLogger().info("KeyAll distributed keys using mode: " + mode);
    }
}

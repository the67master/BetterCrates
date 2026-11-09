package me.aldo.bettercrates.managers;

import me.aldo.bettercrates.BetterCrates;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class CrateManager {
    private final BetterCrates plugin;
    private final File file;
    private YamlConfiguration configuration;

    public CrateManager(BetterCrates plugin) {
        this.plugin = plugin;
        this.file = new File(plugin.getDataFolder(), "crates.yml");
    }

    public void load() {
        if (!file.exists()) plugin.saveResource("crates.yml", false);
        configuration = YamlConfiguration.loadConfiguration(file);
        if (!configuration.isConfigurationSection("crates")) configuration.createSection("crates");
        if (!configuration.isConfigurationSection("keys")) configuration.createSection("keys");
        for (String id : getCrateIds()) {
            String hologram = "crates." + id + ".hologram.";
            if (!configuration.contains(hologram + "enabled")) configuration.set(hologram + "enabled", true);
            if (!configuration.isList(hologram + "lines")) configuration.set(hologram + "lines", Arrays.asList("%crate_name%", "&7Right-Click to open!", "&7Left-Click to Preview!"));
            String messages = "crates." + id + ".messages.";
            if (!configuration.contains(messages + "enabled")) configuration.set(messages + "enabled", true);
            if (!configuration.contains(messages + "player.enabled")) configuration.set(messages + "player.enabled", false);
            if (!configuration.contains(messages + "player.message")) configuration.set(messages + "player.message", "&aYou opened %crate%&a.");
            if (!configuration.contains(messages + "global.enabled")) configuration.set(messages + "global.enabled", false);
            if (!configuration.contains(messages + "global.message")) configuration.set(messages + "global.message", "&e%player% &7opened %crate%&7.");
            if (!configuration.contains(messages + "reward.enabled")) configuration.set(messages + "reward.enabled", true);
            if (!configuration.contains(messages + "reward.message")) configuration.set(messages + "reward.message", "&eYou won &f%reward%&e!");
        }
        save();
    }

    public void save() {
        if (configuration == null) return;
        try { configuration.save(file); }
        catch (IOException exception) { plugin.getLogger().severe("No se pudo guardar crates.yml: " + exception.getMessage()); }
    }

    public boolean createCrate(String id) {
        if (crateExists(id)) return false;
        String p = "crates." + id;
        configuration.set(p + ".settings.display-name", "&e" + id);
        configuration.set(p + ".settings.color", "&e");
        configuration.set(p + ".settings.enabled", true);
        configuration.set(p + ".settings.permission.enabled", false);
        configuration.set(p + ".settings.permission.node", "bettercrates.crate." + id + ".open");
        configuration.set(p + ".key-id", id);
        configuration.set(p + ".block.material", plugin.getConfig().getString("customization.crate.default-block", "CHEST"));
        configuration.set(p + ".block.glint", plugin.getConfig().getBoolean("customization.crate.default-glint", false));
        configuration.set(p + ".hologram.enabled", plugin.getConfig().getBoolean("customization.hologram.enabled-by-default", true));
        configuration.set(p + ".hologram.lines", plugin.getConfig().getStringList("customization.hologram.default-lines").isEmpty() ? Arrays.asList("%crate_name%", "&7Right-Click to open!", "&7Left-Click to preview!") : plugin.getConfig().getStringList("customization.hologram.default-lines"));
        configuration.set(p + ".messages.enabled", true);
        configuration.set(p + ".messages.player.enabled", false);
        configuration.set(p + ".messages.player.message", "&aHas abierto %crate%&a.");
        configuration.set(p + ".messages.global.enabled", false);
        configuration.set(p + ".messages.global.message", "&e%player% &7ha abierto %crate%&7.");
        configuration.set(p + ".messages.reward.enabled", true);
        configuration.set(p + ".messages.reward.message", "&e¡Te ha tocado &f%reward%&e!");
        configuration.set(p + ".placed", new ArrayList<String>());
        configuration.createSection(p + ".rewards");
        if (!keyExists(id)) createKey(id);
        save();
        return true;
    }

    public boolean createKey(String id) {
        if (keyExists(id)) return false;
        String p = "keys." + id;
        configuration.set(p + ".material", plugin.getConfig().getString("customization.key.default-material", "TRIPWIRE_HOOK"));
        configuration.set(p + ".amount", 1);
        configuration.set(p + ".display-name", "&6Key " + id);
        configuration.set(p + ".color", "&6");
        configuration.set(p + ".lore", Arrays.asList("&7A BetterCrates key", "&e" + id));
        configuration.set(p + ".glint", plugin.getConfig().getBoolean("customization.key.default-glint", false));
        configuration.set(p + ".permission.enabled", false);
        configuration.set(p + ".permission.node", "bettercrates.key." + id + ".use");
        save();
        return true;
    }


    public void normalizeRewardChances(String crateId) {
        String base = "crates." + crateId + ".rewards";
        ConfigurationSection section = configuration.getConfigurationSection(base);
        if (section == null) return;
        List<String> ids = new ArrayList<String>(section.getKeys(false));
        double manualTotal = 0.0;
        int automatic = 0;
        for (String id : ids) {
            String path = base + "." + id;
            ItemStack item = configuration.getItemStack(path + ".item");
            if (item == null || item.getType() == Material.AIR) continue;
            if (configuration.getBoolean(path + ".chance-manual", false)) {
                double chance = configuration.getDouble(path + ".chance", 0.0);
                if (!Double.isNaN(chance) && !Double.isInfinite(chance) && chance > 0) manualTotal += chance;
            } else automatic++;
        }
        double remaining = Math.max(0.0, 100.0 - manualTotal);
        double each = automatic > 0 ? remaining / automatic : 0.0;
        for (String id : ids) {
            String path = base + "." + id;
            ItemStack item = configuration.getItemStack(path + ".item");
            if (item == null || item.getType() == Material.AIR) continue;
            if (!configuration.getBoolean(path + ".chance-manual", false)) configuration.set(path + ".chance", each);
        }
    }

    public boolean crateExists(String id) { return configuration != null && configuration.isConfigurationSection("crates." + id); }
    public boolean keyExists(String id) { return configuration != null && configuration.isConfigurationSection("keys." + id); }

    public Set<String> getCrateIds() {
        ConfigurationSection s = configuration == null ? null : configuration.getConfigurationSection("crates");
        return s == null ? Collections.<String>emptySet() : s.getKeys(false);
    }
    public Set<String> getKeyIds() {
        ConfigurationSection s = configuration == null ? null : configuration.getConfigurationSection("keys");
        return s == null ? Collections.<String>emptySet() : s.getKeys(false);
    }
    public YamlConfiguration getConfiguration() { return configuration; }

    public ItemStack createKeyItem(String id, int amount) {
        if (!keyExists(id)) return null;
        String p = "keys." + id + ".";
        Material material = Material.matchMaterial(configuration.getString(p + "material", "TRIPWIRE_HOOK"));
        if (material == null || material == Material.AIR || !material.isItem()) material = Material.TRIPWIRE_HOOK;
        ItemStack stack = new ItemStack(material, Math.max(1, Math.min(64, amount)));
        ItemMeta meta = stack.getItemMeta();
        meta.setDisplayName(plugin.color(configuration.getString(p + "display-name", "&6Llave " + id)));
        List<String> lore = new ArrayList<String>();
        for (String line : configuration.getStringList(p + "lore")) lore.add(plugin.color(line));
        lore.add(plugin.color("&0BC_KEY:" + id));
        meta.setLore(lore);
        if (configuration.getBoolean(p + "glint", false)) {
            meta.addEnchant(Enchantment.DURABILITY, 1, true);
            meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        }
        stack.setItemMeta(meta);
        return stack;
    }

    public ItemStack createCrateItem(String id, int amount) {
        if (!crateExists(id)) return null;
        String p = "crates." + id + ".";
        Material material = Material.matchMaterial(configuration.getString(p + "block.material", "CHEST"));
        if (material == null || material == Material.AIR || !material.isBlock()) material = Material.CHEST;
        ItemStack stack = new ItemStack(material, Math.max(1, Math.min(64, amount)));
        ItemMeta meta = stack.getItemMeta();
        meta.setDisplayName(plugin.color(configuration.getString(p + "settings.display-name", "&e" + id)));
        List<String> lore = new ArrayList<String>();
        lore.add(plugin.color("&7Place this block to create a crate."));
        lore.add(plugin.color("&0BC_CRATE:" + id));
        meta.setLore(lore);
        if (configuration.getBoolean(p + "block.glint", false)) {
            meta.addEnchant(Enchantment.DURABILITY, 1, true);
            meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        }
        stack.setItemMeta(meta);
        return stack;
    }

    public boolean isTagged(ItemStack item, String marker) {
        if (item == null || !item.hasItemMeta() || !item.getItemMeta().hasLore()) return false;
        for (String line : item.getItemMeta().getLore()) if (line.startsWith("§0" + marker + ":")) return true;
        return false;
    }

    public String getTag(ItemStack item, String marker) {
        if (!isTagged(item, marker)) return null;
        for (String line : item.getItemMeta().getLore()) {
            String prefix = "§0" + marker + ":";
            if (line.startsWith(prefix)) return line.substring(prefix.length());
        }
        return null;
    }

    public void addPlaced(String id, String location) {
        List<String> locations = configuration.getStringList("crates." + id + ".placed");
        if (!locations.contains(location)) locations.add(location);
        configuration.set("crates." + id + ".placed", locations);
        save();
    }

    public void removePlaced(String id, String location) {
        List<String> locations = configuration.getStringList("crates." + id + ".placed");
        locations.remove(location);
        configuration.set("crates." + id + ".placed", locations);
        save();
    }

    public String getPlacedCrate(String location) {
        for (String id : getCrateIds()) {
            if (configuration.getStringList("crates." + id + ".placed").contains(location)) return id;
        }
        return null;
    }
}

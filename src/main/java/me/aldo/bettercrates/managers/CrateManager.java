package me.aldo.bettercrates.managers;

import me.aldo.bettercrates.BetterCrates;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.Set;
import java.util.logging.Level;

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
        save();
    }

    public void save() {
        if (configuration == null) return;
        try {
            configuration.save(file);
        } catch (IOException exception) {
            plugin.getLogger().log(Level.SEVERE, "No se pudo guardar crates.yml", exception);
        }
    }

    public boolean createCrate(String id) {
        if (crateExists(id)) return false;
        String path = "crates." + id;
        configuration.set(path + ".settings.display-name", "&e" + id);
        configuration.set(path + ".settings.enabled", true);
        configuration.set(path + ".settings.permission.enabled", false);
        configuration.set(path + ".settings.permission.node", "bettercrates.crate." + id + ".open");
        configuration.set(path + ".key-id", id);
        configuration.createSection(path + ".rewards");
        if (!keyExists(id)) createKey(id);
        save();
        return true;
    }

    public boolean createKey(String id) {
        if (keyExists(id)) return false;
        String path = "keys." + id;
        configuration.set(path + ".material", "TRIPWIRE_HOOK");
        configuration.set(path + ".amount", 1);
        configuration.set(path + ".display-name", "&6Llave " + id);
        configuration.set(path + ".lore", java.util.Arrays.asList("&7Una llave de BetterCrates", "&e" + id));
        configuration.set(path + ".permission.enabled", false);
        configuration.set(path + ".permission.node", "bettercrates.key." + id + ".use");
        save();
        return true;
    }

    public boolean crateExists(String id) {
        return configuration != null && configuration.isConfigurationSection("crates." + id);
    }
    public boolean keyExists(String id) {
        return configuration != null && configuration.isConfigurationSection("keys." + id);
    }
    public Set<String> getCrateIds() {
        ConfigurationSection section = configuration == null ? null : configuration.getConfigurationSection("crates");
        return section == null ? Collections.<String>emptySet() : section.getKeys(false);
    }
    public Set<String> getKeyIds() {
        ConfigurationSection section = configuration == null ? null : configuration.getConfigurationSection("keys");
        return section == null ? Collections.<String>emptySet() : section.getKeys(false);
    }
    public YamlConfiguration getConfiguration() { return configuration; }
}

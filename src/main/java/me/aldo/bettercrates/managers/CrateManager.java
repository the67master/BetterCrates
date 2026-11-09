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
        if (!file.exists()) {
            plugin.saveResource("crates.yml", false);
        }

        configuration = YamlConfiguration.loadConfiguration(file);
        if (!configuration.isConfigurationSection("crates")) {
            configuration.createSection("crates");
            save();
        }
    }

    public void save() {
        if (configuration == null) {
            return;
        }

        try {
            configuration.save(file);
        } catch (IOException exception) {
            plugin.getLogger().log(Level.SEVERE, "No se pudo guardar crates.yml", exception);
        }
    }

    public boolean createCrate(String id) {
        if (crateExists(id)) {
            return false;
        }

        String path = "crates." + id;
        configuration.set(path + ".settings.display-name", "&e" + id);
        configuration.set(path + ".settings.enabled", true);

        configuration.set(path + ".key.material", "TRIPWIRE_HOOK");
        configuration.set(path + ".key.amount", 1);
        configuration.set(path + ".key.display-name", "&6Llave " + id);
        configuration.set(path + ".key.lore", java.util.Arrays.asList(
                "&7Una llave para abrir la crate",
                "&e" + id
        ));

        configuration.createSection(path + ".rewards");
        save();
        return true;
    }

    public boolean crateExists(String id) {
        return configuration != null
                && configuration.isConfigurationSection("crates." + id);
    }

    public Set<String> getCrateIds() {
        if (configuration == null) {
            return Collections.emptySet();
        }

        ConfigurationSection section = configuration.getConfigurationSection("crates");
        return section == null ? Collections.<String>emptySet() : section.getKeys(false);
    }

    public YamlConfiguration getConfiguration() {
        return configuration;
    }
}

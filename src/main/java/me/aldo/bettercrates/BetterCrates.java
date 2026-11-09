package me.aldo.bettercrates;

import me.aldo.bettercrates.commands.CrateCommand;
import me.aldo.bettercrates.listeners.CrateListener;
import me.aldo.bettercrates.managers.CrateManager;
import me.aldo.bettercrates.menus.CrateMenu;
import me.aldo.bettercrates.managers.CrateHologramManager;
import me.aldo.bettercrates.managers.KeyAllManager;
import me.aldo.bettercrates.hooks.KeyAllExpansion;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.List;

public class BetterCrates extends JavaPlugin {
    private CrateManager crateManager;
    private CrateMenu crateMenu;
    private CrateHologramManager hologramManager;
    private YamlConfiguration messages;
    private KeyAllManager keyAllManager;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        saveIfMissing("messages.yml");
        saveIfMissing("messages_en.yml");
        saveIfMissing("messages_es.yml");
        saveIfMissing("crates.yml");
        loadMessages();

        crateManager = new CrateManager(this);
        crateManager.load();
        crateMenu = new CrateMenu(this);
        hologramManager = new CrateHologramManager(this);
        hologramManager.refreshAll();
        keyAllManager = new KeyAllManager(this);
        keyAllManager.start();
        if (getServer().getPluginManager().getPlugin("PlaceholderAPI") != null) {
            new KeyAllExpansion(this).register();
            getLogger().info("PlaceholderAPI hook enabled. Use %bckey%.");
        }

        getCommand("bc").setExecutor(new CrateCommand(this));
        getServer().getPluginManager().registerEvents(new CrateListener(this), this);
        getLogger().info("BetterCrates has been enabled successfully.");
    }

    @Override
    public void onDisable() {
        if (crateManager != null) crateManager.save();
        if (keyAllManager != null) keyAllManager.stop();
    }

    public void reloadBetterCrates() {
        reloadConfig();
        loadMessages();
        crateManager.load();
        if (keyAllManager != null) keyAllManager.reload();
        if (hologramManager != null) hologramManager.refreshAll();
    }

    public CrateManager getCrateManager() { return crateManager; }
    public CrateMenu getCrateMenu() { return crateMenu; }
    public CrateHologramManager getHologramManager() { return hologramManager; }
    public KeyAllManager getKeyAllManager() { return keyAllManager; }

    public String getMessage(String path) {
        String prefix = getConfig().getString("settings.prefix", "&8[&6BetterCrates&8] ");
        return color(prefix + messages.getString(path, "&cMessage not configured: " + path));
    }

    public void sendMessage(CommandSender sender, String path) { sender.sendMessage(getMessage(path)); }
    public void sendMessage(CommandSender sender, String path, String placeholder, String replacement) {
        sender.sendMessage(getMessage(path).replace(placeholder, replacement));
    }

    public void sendHelp(CommandSender sender) {
        for (String line : messages.getStringList("help")) sender.sendMessage(color(line));
    }

    public String color(String text) {
        return ChatColor.translateAlternateColorCodes('&', text);
    }

    private void loadMessages() {
        String language = getConfig().getString("settings.language", "en").toLowerCase(java.util.Locale.ENGLISH);
        if (!language.equals("en") && !language.equals("es")) language = "en";
        File languageFile = new File(getDataFolder(), "messages_" + language + ".yml");
        if (!languageFile.exists()) saveIfMissing("messages_" + language + ".yml");
        messages = YamlConfiguration.loadConfiguration(languageFile);
        if (messages.getKeys(false).isEmpty()) messages = YamlConfiguration.loadConfiguration(new File(getDataFolder(), "messages.yml"));
    }

    private void saveIfMissing(String resource) {
        File file = new File(getDataFolder(), resource);
        if (!file.exists()) saveResource(resource, false);
    }
}

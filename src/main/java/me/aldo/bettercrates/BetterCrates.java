package me.aldo.bettercrates;

import me.aldo.bettercrates.commands.CrateCommand;
import me.aldo.bettercrates.listeners.CrateListener;
import me.aldo.bettercrates.managers.CrateManager;
import me.aldo.bettercrates.menus.CrateMenu;
import me.aldo.bettercrates.managers.CrateHologramManager;
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

    @Override
    public void onEnable() {
        saveDefaultConfig();
        saveIfMissing("messages.yml");
        saveIfMissing("crates.yml");
        loadMessages();

        crateManager = new CrateManager(this);
        crateManager.load();
        crateMenu = new CrateMenu(this);
        hologramManager = new CrateHologramManager(this);
        hologramManager.refreshAll();

        getCommand("bc").setExecutor(new CrateCommand(this));
        getServer().getPluginManager().registerEvents(new CrateListener(this), this);
        getLogger().info("BetterCrates se ha habilitado correctamente.");
    }

    @Override
    public void onDisable() {
        if (crateManager != null) crateManager.save();
    }

    public void reloadBetterCrates() {
        reloadConfig();
        loadMessages();
        crateManager.load();
        if (hologramManager != null) hologramManager.refreshAll();
    }

    public CrateManager getCrateManager() { return crateManager; }
    public CrateMenu getCrateMenu() { return crateMenu; }
    public CrateHologramManager getHologramManager() { return hologramManager; }

    public String getMessage(String path) {
        String prefix = getConfig().getString("settings.prefix", "&8[&6BetterCrates&8] ");
        return color(prefix + messages.getString(path, "&cMensaje no configurado: " + path));
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
        messages = YamlConfiguration.loadConfiguration(new File(getDataFolder(), "messages.yml"));
    }

    private void saveIfMissing(String resource) {
        File file = new File(getDataFolder(), resource);
        if (!file.exists()) saveResource(resource, false);
    }
}

package me.aldo.bettercrates;

import me.aldo.bettercrates.commands.CrateCommand;
import me.aldo.bettercrates.managers.CrateManager;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.util.List;

public class BetterCrates extends JavaPlugin {

    private CrateManager crateManager;
    private YamlConfiguration messages;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        saveIfMissing("messages.yml");
        saveIfMissing("crates.yml");

        loadMessages();
        this.crateManager = new CrateManager(this);
        this.crateManager.load();

        getCommand("bc").setExecutor(new CrateCommand(this));

        getLogger().info("BetterCrates se ha habilitado correctamente.");
    }

    @Override
    public void onDisable() {
        if (crateManager != null) {
            crateManager.save();
        }
        getLogger().info("BetterCrates se ha deshabilitado.");
    }

    public void reloadBetterCrates() {
        reloadConfig();
        loadMessages();
        crateManager.load();
    }

    public CrateManager getCrateManager() {
        return crateManager;
    }

    public String getMessage(String path) {
        String prefix = getConfig().getString("settings.prefix", "&8[&6BetterCrates&8] ");
        String message = messages.getString(path, "&cMensaje no configurado: " + path);
        return color(prefix + message);
    }

    public void sendMessage(CommandSender sender, String path) {
        sender.sendMessage(getMessage(path));
    }

    public void sendMessage(CommandSender sender, String path, String placeholder, String replacement) {
        sender.sendMessage(getMessage(path).replace(placeholder, replacement));
    }

    public void sendHelp(CommandSender sender) {
        List<String> lines = messages.getStringList("help");
        for (String line : lines) {
            sender.sendMessage(color(line));
        }
    }

    public String color(String text) {
        return ChatColor.translateAlternateColorCodes('&', text);
    }

    private void loadMessages() {
        File file = new File(getDataFolder(), "messages.yml");
        this.messages = YamlConfiguration.loadConfiguration(file);
    }

    private void saveIfMissing(String resource) {
        File file = new File(getDataFolder(), resource);
        if (!file.exists()) {
            saveResource(resource, false);
        }
    }
}

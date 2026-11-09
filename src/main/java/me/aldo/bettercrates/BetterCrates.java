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
        getConfig().options().copyDefaults(true);
        saveConfig();
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
        getConfig().options().copyDefaults(true);
        saveConfig();
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


    public String guiText(String text) {
        if (!"es".equalsIgnoreCase(getConfig().getString("settings.language", "en"))) return text;
        String[][] translations = new String[][] {
            {"Create Crate","Crear crate"},{"Create a new crate","Crear una crate nueva"},{"Create Key","Crear llave"},{"Create a key","Crear una llave"},
            {"Configure Crates","Configurar crates"},{"Configure Keys","Configurar llaves"},{"Main Menu","Menú principal"},{"Edit Crate","Editar crate"},{"Edit Key","Editar llave"},
            {"Available Rewards","Premios disponibles"},{"Crate Block","Bloque de crate"},{"Key Item","Objeto de llave"},{"Crate Hologram","Holograma de crate"},{"Crate Messages","Mensajes de crate"},
            {"Keys","Llaves"},{"Rewards","Recompensas"},{"Status: ","Estado: "},{"Enabled","Activado"},{"Disabled","Desactivado"},{"Yes","Sí"},{"No","No"},{"Back","Volver"},{"Previous Page","Página anterior"},{"Next Page","Página siguiente"},{"Page ","Página "},
            {"Create crate","Crear crate"},{"Create key","Crear llave"},{"Enable / Disable","Activar / desactivar"},{"Name and Color","Nombre y color"},{"Current: ","Actual: "},{"Assign Key","Asignar llave"},{"Required: ","Requerido: "},
            {"Edit Crate Block","Editar bloque de crate"},{"Material: ","Material: "},{"Click to open editor","Click para abrir editor"},{"Edit Rewards","Editar recompensas"},{"Configure rewards and their chances","Configura los premios y sus probabilidades"},
            {"Enchantment Glint","Brillo encantado"},{"Edit Hologram","Editar holograma"},{"Configure the 3 lines above the crate","Configura las 3 líneas sobre la crate"},{"Opening messages","Mensajes de apertura"},{"Customize private and global messages","Personaliza los mensajes privados y globales"},
            {"All Messages","Todos los mensajes"},{"Disable this to silence all messages","Desactiva esto para silenciar todos los mensajes"},{"Private message on open","Mensaje privado al abrir"},{"Edit private message","Editar mensaje privado"},{"Click to write the message","Click para escribir el mensaje"},
            {"Global message on open","Mensaje global al abrir"},{"Edit global message","Editar mensaje global"},{"Reward message","Mensaje de recompensa"},{"Edit reward message","Editar mensaje de recompensa"},{"Permission to open","Permiso para abrir"},{"Permission to use key","Permiso para usar llave"},{"Permission node","Nodo de permiso"},
            {"Write Material","Escribir material"},{"Enter the Bukkit block material name","Escribe el material Bukkit del bloque"},{"Selected Block","Bloque seleccionado"},{"Drag a block here from your inventory","Arrastra aquí un bloque de tu inventario"},{"Save Block","Guardar bloque"},{"Save the selected material","Guarda el material seleccionado"},
            {"Selected Item","Objeto seleccionado"},{"Drag an item here from your inventory","Arrastra aquí un objeto de tu inventario"},{"Save Item","Guardar objeto"},{"Enter the Bukkit item material name","Escribe el material Bukkit del objeto"},
            {"Chance: ","Probabilidad: "},{"Manual chance","Probabilidad manual"},{"Automatic chance","Probabilidad automática"},{"Left-click: edit chance","Click izquierdo: cambiar probabilidad"},{"Right-click: remove reward","Click derecho: eliminar premio"},
            {"Reward Editor","Editor de recompensas"},{"Drag an item from your inventory","Arrastra un objeto de tu inventario"},{"to an empty center slot","a un espacio libre del centro"},{"Chances auto-balance to 100%","Las probabilidades se ajustan al 100%"},{"Exit","Salir"},{"Return to crate settings","Volver a la configuración de la crate"},
            {"Approximate chance: ","Probabilidad aproximada: "},{"Rewards for ","Premios de "},{"Available items: ","Objetos disponibles: "},{"This crate has no rewards configured","Esta crate no tiene premios configurados"},{"Close preview","Cerrar vista previa"},
            {"Line 1","Línea 1"},{"Line 2","Línea 2"},{"Line 3","Línea 3"},{"Restore defaults","Restaurar valores por defecto"},{"Edit name and color","Editar nombre y color"},{"Click to edit","Click para editar"},{"Click to open","Click para abrir"},{"Click and type a colored name (&)","Click y escribe un nombre con colores (&)"},{"Enable / Disable Hologram","Activar / desactivar holograma"},{"Right-Click to open!","¡Click derecho para abrir!"},{"Left-Click to Preview!","¡Click izquierdo para previsualizar!"},{"Enabled","Activado"},{"Disabled","Desactivado"},{"Yes","Sí"},{"No","No"}
        };
        for (String[] pair : translations) text = text.replace(pair[0], pair[1]);
        return text;
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

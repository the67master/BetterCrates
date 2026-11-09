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
            {"Invalid duration. Examples: 30m, 1h5m, 1d6h.","Duración inválida. Ejemplos: 30m, 1h5m, 1d6h."},{"Modes: random, all, weighted.","Modos: random, all, weighted."},{"Unknown key: ","Llave desconocida: "},{"Weight must be a positive number.","El peso debe ser un número positivo."},{"Added ","Se agregó "},{" to the KeyAll pool with weight "," al grupo de KeyAll con peso "},{"Removed ","Se quitó "},{"from the KeyAll pool.","del grupo de KeyAll."},{"KeyAll keys (","Llaves de KeyAll ("},{"No keys configured. Random mode uses all registered keys.","No hay llaves configuradas. El modo random usa todas las llaves registradas."},{" (weight: "," (peso: "},{"Next KeyAll in: ","Próximo KeyAll en: "},{"Unknown KeyAll subcommand. Use ","Subcomando KeyAll desconocido. Usa "},{"KeyAll interval set to ","Intervalo de KeyAll establecido en "},{"KeyAll distribution mode: ","Modo de distribución de KeyAll: "},{"Set the KeyAll interval","Establece el intervalo de KeyAll"},{"Choose the distribution mode","Elige el modo de distribución"},{"Add a key to the pool","Agrega una llave al grupo"},{"Remove a key from the pool","Quita una llave del grupo"},{"Everyone has received their KeyAll reward.","Todos han recibido su recompensa de KeyAll."},{"You must assign a block. The crate block has been restored to CHEST.","Debes asignar un bloque. El bloque de la crate se restauró a CHEST."},{"Block selection cleared. Choose a block or save to restore the chest.","Selección de bloque eliminada. Elige un bloque o guarda para restaurar el cofre."},{"Selected block: ","Bloque seleccionado: "},{"Selected: ","Seleccionado: "},{"Material saved: ","Material guardado: "},{"Invalid material.","Material inválido."},{"Enter a number between 0 and 100.","Ingresa un número entre 0 y 100."},{"That key does not exist.","Esa llave no existe."},{"Crate created: ","Crate creada: "},{"Key created: ","Llave creada: "},{"That ID already exists.","Ese ID ya existe."},{"Invalid ID.","ID inválido."},{"Cancelled.","Cancelado."},{"Enter the crate ID","Ingresa el ID de la crate"},{"Enter the key ID","Ingresa el ID de la llave"},{"Enter the crate name (color codes supported)","Ingresa el nombre de la crate (se permiten códigos de color)"},{"Enter the ID of an existing key","Ingresa el ID de una llave existente"},{"Enter the permission node","Ingresa el nodo de permiso"},{"Enter the key name (color codes supported)","Ingresa el nombre de la llave (se permiten códigos de color)"},{"Enter a Bukkit material name","Ingresa un material de Bukkit"},{"Enter the reward chance from 0 to 100","Ingresa la probabilidad del premio entre 0 y 100"},{"The total of manual chances cannot exceed 100%. Maximum available: ","La suma de probabilidades manuales no puede superar el 100%. Máximo disponible: "},{"%.2f","%.2f"},{"You opened ","Abriste "},{"opened ","abrió "},{"You won ","Ganaste "},{"No valid keys are available.","No hay llaves válidas disponibles."},{"Crate ","Crate "},{" placed."," colocada."},
            {"Only blocks can be assigned as a crate block.","Solo se pueden asignar bloques como bloque de crate."},{"Block selection cleared. Choose a block or save to restore the default block.","Se eliminó la selección. Elige un bloque o guarda para restaurar el bloque predeterminado."},{"You must assign a block. ","Debes asignar un bloque. "},{"The crate block has been restored to ","El bloque de la crate se restauró a "},{"Material saved: ","Material guardado: "},{"Selected block: ","Bloque seleccionado: "},{"Selected: ","Seleccionado: "},{"That player is not online.","Ese jugador no está conectado."},{"Invalid amount.","Cantidad inválida."},{"Unknown key.","Llave desconocida."},{"Unknown crate.","Crate desconocida."},{"Gave ","Entregaste "},{" to "," a "},{"Invalid duration. Examples: 30m, 1h5m, 1d6h.","Duración inválida. Ejemplos: 30m, 1h5m, 1d6h."},{"Modes: random, all, weighted.","Modos: random, all, weighted."},{"Weight must be a positive number.","El peso debe ser un número positivo."},{"Added ","Agregaste "},{" to the KeyAll pool with weight "," al grupo de llaves KeyAll con peso "},{"Removed ","Quitaste "},{"from the KeyAll pool.","del grupo de llaves KeyAll."},{"KeyAll keys (","Llaves de KeyAll ("},{"No keys configured. Random mode uses all registered keys.","No hay llaves configuradas. Se usarán todas las llaves registradas."},{" (weight: "," (peso: "},{"Next KeyAll in: ","Próximo KeyAll en: "},{"Unknown KeyAll subcommand. Use ","Subcomando KeyAll desconocido. Usa "},{"KeyAll interval set to ","Intervalo de KeyAll establecido en "},{"KeyAll distribution mode: ","Modo de distribución de KeyAll: "},{"Set the KeyAll interval","Establece el intervalo de KeyAll"},{"Choose the distribution mode","Elige el modo de distribución"},{"Add a key to the pool","Agrega una llave al grupo"},{"Remove a key from the pool","Quita una llave del grupo"},{"Everyone has received their KeyAll reward.","Todos han recibido su recompensa de KeyAll."},{"type cancel to exit","escribe cancel para salir"},{"Cancelled.","Cancelado."},{"Invalid ID.","ID inválido."},{"Crate created: ","Crate creada: "},{"Key created: ","Llave creada: "},{"That ID already exists.","Ese ID ya existe."},{"That key does not exist.","Esa llave no existe."},{"This crate is disabled.","Esta crate está desactivada."},{"You do not have permission to open this crate.","No tienes permiso para abrir esta crate."},{"The key configured for this crate does not exist.","La llave configurada para esta crate no existe."},{"You do not have permission to use this key.","No tienes permiso para usar esta llave."},{"You do not have a key to open this crate.","No tienes una llave para abrir esta crate."},{"This crate has no rewards configured yet.","Esta crate todavía no tiene recompensas configuradas."},{"This crate has no rewards with a valid chance.","Esta crate no tiene recompensas con probabilidades válidas."},{"The total of manual chances cannot exceed 100%. Maximum available: ","La suma de probabilidades manuales no puede superar el 100%. Máximo disponible: "},{"Enter a number between 0 and 100.","Ingresa un número entre 0 y 100."},{"Enter the crate ID","Ingresa el ID de la crate"},{"Enter the key ID","Ingresa el ID de la llave"},{"Enter the crate name (color codes supported)","Ingresa el nombre de la crate (se permiten códigos de color)"},{"Enter the ID of an existing key","Ingresa el ID de una llave existente"},{"Enter the permission node","Ingresa el nodo de permiso"},{"Enter the key name (color codes supported)","Ingresa el nombre de la llave (se permiten códigos de color)"},{"Enter a Bukkit material name","Ingresa un material de Bukkit"},{"Enter the reward chance from 0 to 100","Ingresa la probabilidad del premio entre 0 y 100"},{"You opened ","Abriste "},{"opened ","abrió "},{"You won ","Ganaste "},
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

package me.aldo.bettercrates.menus;

import me.aldo.bettercrates.BetterCrates;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class CrateMenu {
    private final BetterCrates plugin;
    public static final String MAIN = "§8BetterCrates §7» §6Panel";
    public static final String CRATES = "§8BetterCrates §7» §6Crates";
    public static final String KEYS = "§8BetterCrates §7» §6Llaves";
    public static final String CRATE_PREFIX = "§8Editar crate §7» §e";
    public static final String KEY_PREFIX = "§8Editar llave §7» §e";

    public CrateMenu(BetterCrates plugin) { this.plugin = plugin; }

    private ItemStack item(Material material, String name, String... lore) {
        ItemStack stack = new ItemStack(material);
        ItemMeta meta = stack.getItemMeta();
        meta.setDisplayName(plugin.color(name));
        List<String> lines = new ArrayList<String>();
        for (String line : lore) lines.add(plugin.color(line));
        meta.setLore(lines);
        stack.setItemMeta(meta);
        return stack;
    }

    private void fill(Inventory inv) {
        ItemStack pane = item(Material.STAINED_GLASS_PANE, "&8 ");
        for (int i = 0; i < inv.getSize(); i++) if (inv.getItem(i) == null) inv.setItem(i, pane);
    }

    public void openMain(Player player) {
        Inventory inv = Bukkit.createInventory(null, 27, MAIN);
        inv.setItem(10, item(Material.EMERALD, "&a&lCrear crate", "&7Crea una nueva crate", "&eClick para continuar"));
        inv.setItem(12, item(Material.TRIPWIRE_HOOK, "&6&lCrear llave", "&7Crea una llave reutilizable", "&eClick para continuar"));
        inv.setItem(14, item(Material.CHEST, "&e&lConfigurar crates", "&7Editar crates y sus permisos"));
        inv.setItem(16, item(Material.TRIPWIRE_HOOK, "&e&lConfigurar llaves", "&7Editar llaves y sus permisos"));
        fill(inv);
        player.openInventory(inv);
    }

    public void openCrates(Player player) {
        Inventory inv = Bukkit.createInventory(null, 54, CRATES);
        int slot = 0;
        for (String id : plugin.getCrateManager().getCrateIds()) {
            if (slot >= 45) break;
            YamlConfiguration c = plugin.getCrateManager().getConfiguration();
            String path = "crates." + id + ".settings.";
            boolean enabled = c.getBoolean(path + "enabled", true);
            boolean perm = c.getBoolean(path + "permission.enabled", false);
            inv.setItem(slot++, item(Material.CHEST, "&e" + id,
                    "&7Estado: " + (enabled ? "&aActivada" : "&cDesactivada"),
                    "&7Permiso requerido: " + (perm ? "&aSí" : "&cNo"),
                    "&eClick para editar"));
        }
        inv.setItem(49, item(Material.ARROW, "&cVolver"));
        fill(inv);
        player.openInventory(inv);
    }

    public void openKeys(Player player) {
        Inventory inv = Bukkit.createInventory(null, 54, KEYS);
        int slot = 0;
        for (String id : plugin.getCrateManager().getKeyIds()) {
            if (slot >= 45) break;
            YamlConfiguration c = plugin.getCrateManager().getConfiguration();
            boolean perm = c.getBoolean("keys." + id + ".permission.enabled", false);
            inv.setItem(slot++, item(Material.TRIPWIRE_HOOK, "&6" + id,
                    "&7Permiso requerido: " + (perm ? "&aSí" : "&cNo"),
                    "&eClick para editar"));
        }
        inv.setItem(49, item(Material.ARROW, "&cVolver"));
        fill(inv);
        player.openInventory(inv);
    }

    public void openCrateSettings(Player player, String id) {
        YamlConfiguration c = plugin.getCrateManager().getConfiguration();
        String p = "crates." + id + ".settings.";
        if (!plugin.getCrateManager().crateExists(id)) { openCrates(player); return; }
        boolean enabled = c.getBoolean(p + "enabled", true);
        boolean permission = c.getBoolean(p + "permission.enabled", false);
        Inventory inv = Bukkit.createInventory(null, 27, CRATE_PREFIX + id);
        inv.setItem(10, item(Material.REDSTONE_TORCH_ON, "&aActivar / desactivar", "&7Estado actual: " + (enabled ? "&aActivada" : "&cDesactivada")));
        inv.setItem(12, item(Material.NAME_TAG, "&eCambiar nombre visible", "&7Actual: " + c.getString(p + "display-name", id), "&eClick y escribe en el chat"));
        inv.setItem(14, item(Material.TRIPWIRE_HOOK, "&6Asignar llave", "&7Actual: &f" + c.getString("crates." + id + ".key-id", id), "&eClick y escribe el ID de la llave"));
        inv.setItem(16, item(Material.IRON_DOOR, "&dPermiso para abrir", "&7Requerido: " + (permission ? "&aSí" : "&cNo"),
                "&7Nodo: &f" + c.getString(p + "permission.node"), "&eClick para activar/desactivar"));
        inv.setItem(22, item(Material.ARROW, "&cVolver"));
        fill(inv);
        player.openInventory(inv);
    }

    public void openKeySettings(Player player, String id) {
        YamlConfiguration c = plugin.getCrateManager().getConfiguration();
        String p = "keys." + id + ".";
        if (!plugin.getCrateManager().keyExists(id)) { openKeys(player); return; }
        boolean permission = c.getBoolean(p + "permission.enabled", false);
        Inventory inv = Bukkit.createInventory(null, 27, KEY_PREFIX + id);
        inv.setItem(10, item(Material.NAME_TAG, "&eCambiar nombre visible", "&7Actual: " + c.getString(p + "display-name", id), "&eClick y escribe en el chat"));
        inv.setItem(12, item(Material.CHEST, "&bCambiar material", "&7Actual: &f" + c.getString(p + "material", "TRIPWIRE_HOOK"), "&eClick y escribe un material válido"));
        inv.setItem(14, item(Material.IRON_DOOR, "&dPermiso para usar llave", "&7Requerido: " + (permission ? "&aSí" : "&cNo"),
                "&7Nodo: &f" + c.getString(p + "permission.node"), "&eClick para activar/desactivar"));
        inv.setItem(16, item(Material.PAPER, "&eCambiar nodo de permiso", "&7Actual: &f" + c.getString(p + "permission.node"), "&eClick y escribe el nodo"));
        inv.setItem(22, item(Material.ARROW, "&cVolver"));
        fill(inv);
        player.openInventory(inv);
    }

    public void refresh(Player player, String title) {
        if (MAIN.equals(title)) openMain(player);
        else if (CRATES.equals(title)) openCrates(player);
        else if (KEYS.equals(title)) openKeys(player);
        else if (title.startsWith(CRATE_PREFIX)) openCrateSettings(player, title.substring(CRATE_PREFIX.length()));
        else if (title.startsWith(KEY_PREFIX)) openKeySettings(player, title.substring(KEY_PREFIX.length()));
    }
}

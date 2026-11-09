package me.aldo.bettercrates.listeners;

import me.aldo.bettercrates.BetterCrates;
import me.aldo.bettercrates.menus.CrateMenu;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class CrateListener implements Listener {
    private enum InputType { CREATE_CRATE, CREATE_KEY, CRATE_NAME, CRATE_KEY, CRATE_PERMISSION, KEY_NAME, KEY_MATERIAL, KEY_PERMISSION }
    private static class Pending {
        private final InputType type;
        private final String id;
        private Pending(InputType type, String id) { this.type = type; this.id = id; }
    }

    private final BetterCrates plugin;
    private final Map<UUID, Pending> pending = new HashMap<UUID, Pending>();

    public CrateListener(BetterCrates plugin) { this.plugin = plugin; }

    private void prompt(Player player, InputType type, String id, String message) {
        pending.put(player.getUniqueId(), new Pending(type, id));
        player.closeInventory();
        player.sendMessage(plugin.color("&6BetterCrates &8» &e" + message + " &7(escribe &ccancelar&7 para cancelar)"));
    }

    @EventHandler
    public void onMenuClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) return;
        String title = event.getView().getTitle();
        if (!(title.equals(CrateMenu.MAIN) || title.equals(CrateMenu.CRATES) || title.equals(CrateMenu.KEYS)
                || title.startsWith(CrateMenu.CRATE_PREFIX) || title.startsWith(CrateMenu.KEY_PREFIX))) return;
        event.setCancelled(true);
        if (event.getRawSlot() < 0 || event.getRawSlot() >= event.getView().getTopInventory().getSize()) return;
        Player player = (Player) event.getWhoClicked();
        int slot = event.getRawSlot();
        CrateMenu menu = plugin.getCrateMenu();
        YamlConfiguration c = plugin.getCrateManager().getConfiguration();

        if (title.equals(CrateMenu.MAIN)) {
            if (slot == 10) prompt(player, InputType.CREATE_CRATE, null, "Escribe el ID de la nueva crate:");
            else if (slot == 12) prompt(player, InputType.CREATE_KEY, null, "Escribe el ID de la nueva llave:");
            else if (slot == 14) menu.openCrates(player);
            else if (slot == 16) menu.openKeys(player);
            return;
        }
        if (title.equals(CrateMenu.CRATES)) {
            if (slot == 49) { menu.openMain(player); return; }
            ItemStack clicked = event.getCurrentItem();
            if (clicked != null && clicked.hasItemMeta() && clicked.getItemMeta().hasDisplayName()) {
                String id = org.bukkit.ChatColor.stripColor(clicked.getItemMeta().getDisplayName());
                if (plugin.getCrateManager().crateExists(id)) menu.openCrateSettings(player, id);
            }
            return;
        }
        if (title.equals(CrateMenu.KEYS)) {
            if (slot == 49) { menu.openMain(player); return; }
            ItemStack clicked = event.getCurrentItem();
            if (clicked != null && clicked.hasItemMeta() && clicked.getItemMeta().hasDisplayName()) {
                String id = org.bukkit.ChatColor.stripColor(clicked.getItemMeta().getDisplayName());
                if (plugin.getCrateManager().keyExists(id)) menu.openKeySettings(player, id);
            }
            return;
        }
        if (title.startsWith(CrateMenu.CRATE_PREFIX)) {
            String id = title.substring(CrateMenu.CRATE_PREFIX.length());
            String p = "crates." + id + ".settings.";
            if (slot == 22) menu.openCrates(player);
            else if (slot == 10) {
                c.set(p + "enabled", !c.getBoolean(p + "enabled", true));
                plugin.getCrateManager().save(); menu.openCrateSettings(player, id);
            } else if (slot == 12) prompt(player, InputType.CRATE_NAME, id, "Escribe el nuevo nombre visible:");
            else if (slot == 14) prompt(player, InputType.CRATE_KEY, id, "Escribe el ID de una llave existente:");
            else if (slot == 16) {
                c.set(p + "permission.enabled", !c.getBoolean(p + "permission.enabled", false));
                plugin.getCrateManager().save(); menu.openCrateSettings(player, id);
            } else if (slot == 18) prompt(player, InputType.CRATE_PERMISSION, id, "Escribe el nodo de permiso para abrir esta crate:");
            return;
        }
        if (title.startsWith(CrateMenu.KEY_PREFIX)) {
            String id = title.substring(CrateMenu.KEY_PREFIX.length());
            String p = "keys." + id + ".";
            if (slot == 22) menu.openKeys(player);
            else if (slot == 10) prompt(player, InputType.KEY_NAME, id, "Escribe el nuevo nombre visible:");
            else if (slot == 12) prompt(player, InputType.KEY_MATERIAL, id, "Escribe el material Bukkit (por ejemplo DIAMOND):");
            else if (slot == 14) {
                c.set(p + "permission.enabled", !c.getBoolean(p + "permission.enabled", false));
                plugin.getCrateManager().save(); menu.openKeySettings(player, id);
            } else if (slot == 16) prompt(player, InputType.KEY_PERMISSION, id, "Escribe el nodo de permiso:");
        }
    }

    @EventHandler
    public void onChat(AsyncPlayerChatEvent event) {
        final Player player = event.getPlayer();
        final Pending action = pending.remove(player.getUniqueId());
        if (action == null) return;
        event.setCancelled(true);
        final String value = event.getMessage().trim();
        Bukkit.getScheduler().runTask(plugin, new Runnable() {
            @Override public void run() {
                if (value.equalsIgnoreCase("cancelar")) {
                    player.sendMessage(plugin.color("&cAcción cancelada.")); menuMain(player); return;
                }
                if (!value.matches("[A-Za-z0-9_.-]+") && (action.type == InputType.CREATE_CRATE || action.type == InputType.CREATE_KEY || action.type == InputType.CRATE_KEY)) {
                    player.sendMessage(plugin.color("&cID inválido. Usa letras, números, guion, punto o guion bajo.")); menuMain(player); return;
                }
                YamlConfiguration c = plugin.getCrateManager().getConfiguration();
                switch (action.type) {
                    case CREATE_CRATE:
                        if (plugin.getCrateManager().createCrate(value)) player.sendMessage(plugin.color("&aCrate creada: &e" + value));
                        else player.sendMessage(plugin.color("&cYa existe esa crate."));
                        menuMain(player); break;
                    case CREATE_KEY:
                        if (plugin.getCrateManager().createKey(value)) player.sendMessage(plugin.color("&aLlave creada: &e" + value));
                        else player.sendMessage(plugin.color("&cYa existe esa llave."));
                        menuMain(player); break;
                    case CRATE_NAME:
                        c.set("crates." + action.id + ".settings.display-name", value);
                        saveAndOpen(player, false, action.id); break;
                    case CRATE_PERMISSION:
                        c.set("crates." + action.id + ".settings.permission.node", value);
                        saveAndOpen(player, false, action.id); break;
                    case CRATE_KEY:
                        if (!plugin.getCrateManager().keyExists(value)) {
                            player.sendMessage(plugin.color("&cEsa llave no existe. Primero créala desde el panel."));
                        } else c.set("crates." + action.id + ".key-id", value);
                        saveAndOpen(player, false, action.id); break;
                    case KEY_NAME:
                        c.set("keys." + action.id + ".display-name", value);
                        saveAndOpen(player, true, action.id); break;
                    case KEY_MATERIAL:
                        Material material = Material.matchMaterial(value);
                        if (material == null || material == Material.AIR) player.sendMessage(plugin.color("&cMaterial no válido."));
                        else c.set("keys." + action.id + ".material", material.name());
                        saveAndOpen(player, true, action.id); break;
                    case KEY_PERMISSION:
                        c.set("keys." + action.id + ".permission.node", value);
                        saveAndOpen(player, true, action.id); break;
                }
            }
        });
    }

    private void saveAndOpen(Player player, boolean key, String id) {
        plugin.getCrateManager().save();
        if (key) plugin.getCrateMenu().openKeySettings(player, id);
        else plugin.getCrateMenu().openCrateSettings(player, id);
    }
    private void menuMain(Player player) { plugin.getCrateMenu().openMain(player); }
}

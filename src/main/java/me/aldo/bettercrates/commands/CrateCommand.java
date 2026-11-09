package me.aldo.bettercrates.commands;

import me.aldo.bettercrates.BetterCrates;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Set;

public class CrateCommand implements CommandExecutor {
    private final BetterCrates plugin;
    public CrateCommand(BetterCrates plugin) { this.plugin = plugin; }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            if (args.length > 0 && args[0].equalsIgnoreCase("reload")) {
                if (!sender.hasPermission("bettercrates.admin")) { plugin.sendMessage(sender, "no-permission"); return true; }
                plugin.reloadBetterCrates(); plugin.sendMessage(sender, "reloaded"); return true;
            }
            plugin.sendHelp(sender); return true;
        }
        Player player = (Player) sender;
        if (!player.hasPermission("bettercrates.admin")) { plugin.sendMessage(sender, "no-permission"); return true; }
        if (args.length == 0 || args[0].equalsIgnoreCase("gui") || args[0].equalsIgnoreCase("menu")) {
            plugin.getCrateMenu().openMain(player); return true;
        }
        if (args[0].equalsIgnoreCase("help")) { plugin.sendHelp(sender); return true; }
        if (args[0].equalsIgnoreCase("reload")) {
            plugin.reloadBetterCrates(); plugin.sendMessage(sender, "reloaded"); return true;
        }
        if (args[0].equalsIgnoreCase("list")) {
            Set<String> ids = plugin.getCrateManager().getCrateIds();
            if (ids.isEmpty()) plugin.sendMessage(sender, "crate-list-empty");
            else {
                sender.sendMessage(plugin.getMessage("crate-list-header"));
                for (String id : ids) sender.sendMessage(plugin.color("&8- &e" + id));
            }
            return true;
        }
        if (args[0].equalsIgnoreCase("create") && args.length >= 2) {
            String id = args[1];
            if (!id.matches("[A-Za-z0-9_.-]+")) { plugin.sendMessage(sender, "invalid-id"); return true; }
            if (!plugin.getCrateManager().createCrate(id)) plugin.sendMessage(sender, "crate-already-exists");
            else plugin.sendMessage(sender, "crate-created", "%crate%", id);
            return true;
        }
        plugin.sendMessage(sender, "unknown-command");
        return true;
    }
}

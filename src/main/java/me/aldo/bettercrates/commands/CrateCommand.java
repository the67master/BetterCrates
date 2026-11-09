package me.aldo.bettercrates.commands;

import me.aldo.bettercrates.BetterCrates;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import java.util.Set;

public class CrateCommand implements CommandExecutor {

    private final BetterCrates plugin;

    public CrateCommand(BetterCrates plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0 || args[0].equalsIgnoreCase("help")) {
            plugin.sendHelp(sender);
            return true;
        }

        if (!sender.hasPermission("bettercrates.admin")) {
            plugin.sendMessage(sender, "no-permission");
            return true;
        }

        if (args[0].equalsIgnoreCase("reload")) {
            plugin.reloadBetterCrates();
            plugin.sendMessage(sender, "reloaded");
            return true;
        }

        if (args[0].equalsIgnoreCase("create")) {
            if (args.length < 2) {
                sender.sendMessage(plugin.color("&cUso: /bc create <id>"));
                return true;
            }

            String id = args[1];
            if (!id.matches("[A-Za-z0-9_-]+")) {
                plugin.sendMessage(sender, "invalid-id");
                return true;
            }

            if (!plugin.getCrateManager().createCrate(id)) {
                plugin.sendMessage(sender, "crate-already-exists");
                return true;
            }

            plugin.sendMessage(sender, "crate-created", "%crate%", id);
            return true;
        }

        if (args[0].equalsIgnoreCase("list")) {
            Set<String> crateIds = plugin.getCrateManager().getCrateIds();
            if (crateIds.isEmpty()) {
                plugin.sendMessage(sender, "crate-list-empty");
                return true;
            }

            sender.sendMessage(plugin.getMessage("crate-list-header"));
            for (String id : crateIds) {
                sender.sendMessage(plugin.color("&8- &e" + id));
            }
            return true;
        }

        plugin.sendMessage(sender, "unknown-command");
        return true;
    }
}

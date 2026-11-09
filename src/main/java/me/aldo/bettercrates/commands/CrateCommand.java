package me.aldo.bettercrates.commands;

import me.aldo.bettercrates.BetterCrates;
import me.aldo.bettercrates.managers.KeyAllManager;
import org.bukkit.Bukkit;
import org.bukkit.command.*;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import java.util.Set;

public class CrateCommand implements CommandExecutor {
    private final BetterCrates plugin;
    public CrateCommand(BetterCrates plugin){this.plugin=plugin;}
    @Override public boolean onCommand(CommandSender sender,Command command,String label,String[] args){
        if(args.length>0&&args[0].equalsIgnoreCase("keyall")){handleKeyAll(sender,args);return true;}
        if(args.length>0&&args[0].equalsIgnoreCase("reload")){if(!sender.hasPermission("bettercrates.admin")){plugin.sendMessage(sender,"no-permission");return true;}plugin.reloadBetterCrates();plugin.sendMessage(sender,"reloaded");return true;}
        if(!(sender instanceof Player)){plugin.sendHelp(sender);return true;}
        Player player=(Player)sender;if(!player.hasPermission("bettercrates.admin")){plugin.sendMessage(sender,"no-permission");return true;}
        if(args.length==0||args[0].equalsIgnoreCase("gui")||args[0].equalsIgnoreCase("menu")){plugin.getCrateMenu().openMain(player);return true;}
        if(args[0].equalsIgnoreCase("help")){plugin.sendHelp(sender);return true;}
        if(args[0].equalsIgnoreCase("list")){Set<String> ids=plugin.getCrateManager().getCrateIds();if(ids.isEmpty())plugin.sendMessage(sender,"crate-list-empty");else{sender.sendMessage(plugin.getMessage("crate-list-header"));for(String id:ids)sender.sendMessage(plugin.color("&8- &e"+id));}return true;}
        if(args[0].equalsIgnoreCase("create")&&args.length>=2){String id=args[1];if(!id.matches("[A-Za-z0-9_.-]+")){plugin.sendMessage(sender,"invalid-id");return true;}if(!plugin.getCrateManager().createCrate(id))plugin.sendMessage(sender,"crate-already-exists");else plugin.sendMessage(sender,"crate-created","%crate%",id);return true;}
        if((args[0].equalsIgnoreCase("givekey")||args[0].equalsIgnoreCase("givecrate"))&&args.length>=3){
            Player target=Bukkit.getPlayer(args[1]);if(target==null){sender.sendMessage(plugin.color("&cThat player is not online."));return true;}
            int amount=1;if(args.length>=4)try{amount=Math.max(1,Math.min(64,Integer.parseInt(args[3])));}catch(NumberFormatException ex){sender.sendMessage(plugin.color("&cInvalid amount."));return true;}
            boolean key=args[0].equalsIgnoreCase("givekey");
            ItemStack item=key?plugin.getCrateManager().createKeyItem(args[2],amount):plugin.getCrateManager().createCrateItem(args[2],amount);
            if(item==null){sender.sendMessage(plugin.color("&cUnknown "+(key?"llave.":"crate.")));return true;}
            target.getInventory().addItem(item);
            sender.sendMessage(plugin.color("&aEntregaste &e"+amount+" &a"+args[2]+" a &e"+target.getName()+"&a."));
            return true;
        }
        plugin.sendMessage(sender,"unknown-command");return true;
    }

    private void handleKeyAll(CommandSender sender,String[] args){
        if(!sender.hasPermission("bettercrates.admin")){plugin.sendMessage(sender,"no-permission");return;}
        if(args.length<2){sender.sendMessage(plugin.color("&e/bc keyall set <duration> &7- Set the KeyAll interval"));sender.sendMessage(plugin.color("&e/bc keyall mode <random|all|weighted>"));sender.sendMessage(plugin.color("&e/bc keyall add <key> [weight] &7- Add a key to the pool"));sender.sendMessage(plugin.color("&e/bc keyall remove <key>"));sender.sendMessage(plugin.color("&e/bc keyall list"));sender.sendMessage(plugin.color("&e/bc keyall time"));return;}
        String sub=args[1].toLowerCase(java.util.Locale.ENGLISH);
        org.bukkit.configuration.file.FileConfiguration config=plugin.getConfig();
        if(sub.equals("set")&&args.length>=3){
            long seconds=KeyAllManager.parseDuration(args[2]);
            if(seconds<=0){sender.sendMessage(plugin.color("&cInvalid duration. Examples: 30m, 1h5m, 1d6h."));return;}
            plugin.getKeyAllManager().setInterval(seconds);sender.sendMessage(plugin.color("&aKeyAll interval set to &e"+KeyAllManager.formatDuration(seconds)+"&a."));return;
        }
        if(sub.equals("mode")&&args.length>=3){String mode=args[2].toLowerCase(java.util.Locale.ENGLISH);if(!mode.equals("random")&&!mode.equals("all")&&!mode.equals("weighted")){sender.sendMessage(plugin.color("&cModes: random, all, weighted."));return;}config.set("keyall.distribution",mode);plugin.saveConfig();sender.sendMessage(plugin.color("&aKeyAll distribution mode: &e"+mode));return;}
        if(sub.equals("add")&&args.length>=3){String id=args[2];if(!plugin.getCrateManager().keyExists(id)){sender.sendMessage(plugin.color("&cUnknown key: &e"+id));return;}double weight=1.0;if(args.length>=4)try{weight=Double.parseDouble(args[3]);}catch(NumberFormatException ex){weight=-1;}if(weight<=0||Double.isNaN(weight)||Double.isInfinite(weight)){sender.sendMessage(plugin.color("&cWeight must be a positive number."));return;}config.set("keyall.keys."+id,weight);plugin.saveConfig();sender.sendMessage(plugin.color("&aAdded &e"+id+"&a to the KeyAll pool with weight &e"+weight+"&a."));return;}
        if(sub.equals("remove")&&args.length>=3){config.set("keyall.keys."+args[2],null);plugin.saveConfig();sender.sendMessage(plugin.color("&aRemoved &e"+args[2]+" &afrom the KeyAll pool."));return;}
        if(sub.equals("list")){java.util.Set<String> ids=config.getConfigurationSection("keyall.keys")==null?java.util.Collections.<String>emptySet():config.getConfigurationSection("keyall.keys").getKeys(false);sender.sendMessage(plugin.color("&6KeyAll keys (&e"+config.getString("keyall.distribution","random")+"&6):"));if(ids.isEmpty())sender.sendMessage(plugin.color("&7No keys configured. Random mode uses all registered keys."));for(String id:ids)sender.sendMessage(plugin.color("&8- &e"+id+" &7(weight: "+config.getDouble("keyall.keys."+id)+")"));return;}
        if(sub.equals("time")){sender.sendMessage(plugin.color("&6Next KeyAll in: &e"+plugin.getKeyAllManager().getFormattedRemaining()));return;}
        sender.sendMessage(plugin.color("&cUnknown KeyAll subcommand. Use &e/bc keyall&c."));
    }


}

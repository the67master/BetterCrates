package me.aldo.bettercrates.commands;

import me.aldo.bettercrates.BetterCrates;
import org.bukkit.Bukkit;
import org.bukkit.command.*;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import java.util.Set;

public class CrateCommand implements CommandExecutor {
    private final BetterCrates plugin;
    public CrateCommand(BetterCrates plugin){this.plugin=plugin;}
    @Override public boolean onCommand(CommandSender sender,Command command,String label,String[] args){
        if(args.length>0&&args[0].equalsIgnoreCase("reload")){if(!sender.hasPermission("bettercrates.admin")){plugin.sendMessage(sender,"no-permission");return true;}plugin.reloadBetterCrates();plugin.sendMessage(sender,"reloaded");return true;}
        if(!(sender instanceof Player)){plugin.sendHelp(sender);return true;}
        Player player=(Player)sender;if(!player.hasPermission("bettercrates.admin")){plugin.sendMessage(sender,"no-permission");return true;}
        if(args.length==0||args[0].equalsIgnoreCase("gui")||args[0].equalsIgnoreCase("menu")){plugin.getCrateMenu().openMain(player);return true;}
        if(args[0].equalsIgnoreCase("help")){plugin.sendHelp(sender);return true;}
        if(args[0].equalsIgnoreCase("list")){Set<String> ids=plugin.getCrateManager().getCrateIds();if(ids.isEmpty())plugin.sendMessage(sender,"crate-list-empty");else{sender.sendMessage(plugin.getMessage("crate-list-header"));for(String id:ids)sender.sendMessage(plugin.color("&8- &e"+id));}return true;}
        if(args[0].equalsIgnoreCase("create")&&args.length>=2){String id=args[1];if(!id.matches("[A-Za-z0-9_.-]+")){plugin.sendMessage(sender,"invalid-id");return true;}if(!plugin.getCrateManager().createCrate(id))plugin.sendMessage(sender,"crate-already-exists");else plugin.sendMessage(sender,"crate-created","%crate%",id);return true;}
        if((args[0].equalsIgnoreCase("givekey")||args[0].equalsIgnoreCase("givecrate"))&&args.length>=3){
            Player target=Bukkit.getPlayer(args[1]);if(target==null){sender.sendMessage(plugin.color("&cEse jugador no está conectado."));return true;}
            int amount=1;if(args.length>=4)try{amount=Math.max(1,Math.min(64,Integer.parseInt(args[3])));}catch(NumberFormatException ex){sender.sendMessage(plugin.color("&cCantidad inválida."));return true;}
            boolean key=args[0].equalsIgnoreCase("givekey");
            ItemStack item=key?plugin.getCrateManager().createKeyItem(args[2],amount):plugin.getCrateManager().createCrateItem(args[2],amount);
            if(item==null){sender.sendMessage(plugin.color("&cNo existe esa "+(key?"llave.":"crate.")));return true;}
            target.getInventory().addItem(item);
            sender.sendMessage(plugin.color("&aEntregaste &e"+amount+" &a"+args[2]+" a &e"+target.getName()+"&a."));
            return true;
        }
        plugin.sendMessage(sender,"unknown-command");return true;
    }
}

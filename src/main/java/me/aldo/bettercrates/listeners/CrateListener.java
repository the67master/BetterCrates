package me.aldo.bettercrates.listeners;

import me.aldo.bettercrates.BetterCrates;
import me.aldo.bettercrates.menus.CrateMenu;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.inventory.ItemStack;

import java.util.*;

public class CrateListener implements Listener {
    private enum InputType { CREATE_CRATE, CREATE_KEY, CRATE_NAME, CRATE_KEY, CRATE_PERMISSION, KEY_NAME, KEY_MATERIAL, KEY_PERMISSION, CRATE_BLOCK_MATERIAL }
    private static class Pending { final InputType type; final String id; Pending(InputType t,String i){type=t;id=i;} }
    private final BetterCrates plugin;
    private final Map<UUID,Pending> pending=new HashMap<UUID,Pending>();
    private final Map<UUID,String> selectedMaterial=new HashMap<UUID,String>();
    public CrateListener(BetterCrates plugin){this.plugin=plugin;}
    private void prompt(Player p,InputType t,String id,String message){pending.put(p.getUniqueId(),new Pending(t,id));p.closeInventory();p.sendMessage(plugin.color("&6BetterCrates &8» &e"+message+" &7(escribe cancelar para salir)"));}
    private boolean isMenu(String t){return t.equals(CrateMenu.MAIN)||t.equals(CrateMenu.CRATES)||t.equals(CrateMenu.KEYS)||t.startsWith(CrateMenu.CRATE_PREFIX)||t.startsWith(CrateMenu.KEY_PREFIX)||t.startsWith(CrateMenu.CRATE_BLOCK_PREFIX)||t.startsWith(CrateMenu.KEY_ITEM_PREFIX);}
    @EventHandler public void onClick(InventoryClickEvent e){
        if(!(e.getWhoClicked() instanceof Player))return; String title=e.getView().getTitle(); if(!isMenu(title))return; e.setCancelled(true);
        if(e.getRawSlot()<0||e.getRawSlot()>=e.getView().getTopInventory().getSize())return;
        Player p=(Player)e.getWhoClicked(); int s=e.getRawSlot(); CrateMenu m=plugin.getCrateMenu(); YamlConfiguration c=plugin.getCrateManager().getConfiguration();
        if(title.equals(CrateMenu.MAIN)){if(s==10)prompt(p,InputType.CREATE_CRATE,null,"Escribe el ID de la crate");else if(s==12)prompt(p,InputType.CREATE_KEY,null,"Escribe el ID de la llave");else if(s==14)m.openCrates(p);else if(s==16)m.openKeys(p);return;}
        if(title.equals(CrateMenu.CRATES)||title.equals(CrateMenu.KEYS)){if(s==49){m.openMain(p);return;} ItemStack it=e.getCurrentItem();if(it==null||!it.hasItemMeta())return;String id=ChatColor.stripColor(it.getItemMeta().getDisplayName());if(title.equals(CrateMenu.CRATES)&&plugin.getCrateManager().crateExists(id))m.openCrateSettings(p,id);if(title.equals(CrateMenu.KEYS)&&plugin.getCrateManager().keyExists(id))m.openKeySettings(p,id);return;}
        if(title.startsWith(CrateMenu.CRATE_BLOCK_PREFIX)||title.startsWith(CrateMenu.KEY_ITEM_PREFIX)){
            boolean crate=title.startsWith(CrateMenu.CRATE_BLOCK_PREFIX);String id=title.substring((crate?CrateMenu.CRATE_BLOCK_PREFIX:CrateMenu.KEY_ITEM_PREFIX).length());
            if(s==22){selectedMaterial.remove(p.getUniqueId());if(crate)m.openCrateSettings(p,id);else m.openKeySettings(p,id);}
            else if(s==11)prompt(p,crate?InputType.CRATE_BLOCK_MATERIAL:InputType.KEY_MATERIAL,id,"Escribe el material Bukkit");
            else if(s==13){ItemStack cursor=e.getCursor();if(cursor!=null&&cursor.getType()!=Material.AIR&&((crate&&cursor.getType().isBlock())||(!crate&&cursor.getType().isItem()))){selectedMaterial.put(p.getUniqueId(),cursor.getType().name());p.sendMessage(plugin.color("&aSeleccionado: &e"+cursor.getType().name()));}}
            else if(s==15){String mat=selectedMaterial.remove(p.getUniqueId());if(mat==null)mat=c.getString(crate?"crates."+id+".block.material":"keys."+id+".material");c.set(crate?"crates."+id+".block.material":"keys."+id+".material",mat);plugin.getCrateManager().save();p.sendMessage(plugin.color("&aMaterial guardado: &e"+mat));if(crate)m.openCrateSettings(p,id);else m.openKeySettings(p,id);}
            return;
        }
        if(title.startsWith(CrateMenu.CRATE_PREFIX)){
            String id=title.substring(CrateMenu.CRATE_PREFIX.length()),path="crates."+id+".settings.";
            if(s==22)m.openCrates(p);
            else if(s==10){c.set(path+"enabled",!c.getBoolean(path+"enabled",true));plugin.getCrateManager().save();m.openCrateSettings(p,id);}
            else if(s==11)prompt(p,InputType.CRATE_NAME,id,"Escribe el nombre de la crate con colores (&e, &6, etc.)");
            else if(s==12)prompt(p,InputType.CRATE_KEY,id,"Escribe el ID de una llave existente");
            else if(s==14){c.set(path+"permission.enabled",!c.getBoolean(path+"permission.enabled",false));plugin.getCrateManager().save();m.openCrateSettings(p,id);}
            else if(s==15)prompt(p,InputType.CRATE_PERMISSION,id,"Escribe el nodo de permiso");
            else if(s==16)m.openCrateBlockEditor(p,id);
            else if(s==18){String q="crates."+id+".block.glint";c.set(q,!c.getBoolean(q,false));plugin.getCrateManager().save();m.openCrateSettings(p,id);}
            return;
        }
        if(title.startsWith(CrateMenu.KEY_PREFIX)){
            String id=title.substring(CrateMenu.KEY_PREFIX.length()),path="keys."+id+".";
            if(s==22)m.openKeys(p);
            else if(s==10)prompt(p,InputType.KEY_NAME,id,"Escribe el nombre de la llave con colores");
            else if(s==12)m.openKeyItemEditor(p,id);
            else if(s==14){c.set(path+"permission.enabled",!c.getBoolean(path+"permission.enabled",false));plugin.getCrateManager().save();m.openKeySettings(p,id);}
            else if(s==15)prompt(p,InputType.KEY_PERMISSION,id,"Escribe el nodo de permiso");
            else if(s==16){c.set(path+"glint",!c.getBoolean(path+"glint",false));plugin.getCrateManager().save();m.openKeySettings(p,id);}
        }
    }
    @EventHandler public void onDrag(InventoryDragEvent e){
        String title=e.getView().getTitle();if(!title.startsWith(CrateMenu.CRATE_BLOCK_PREFIX)&&!title.startsWith(CrateMenu.KEY_ITEM_PREFIX))return;
        if(e.getRawSlots().contains(13)){e.setCancelled(true);if(e.getWhoClicked() instanceof Player){Player p=(Player)e.getWhoClicked();ItemStack old=e.getOldCursor();if(old!=null&&old.getType()!=Material.AIR){boolean crate=title.startsWith(CrateMenu.CRATE_BLOCK_PREFIX);if((crate&&old.getType().isBlock())||(!crate&&old.getType().isItem())){selectedMaterial.put(p.getUniqueId(),old.getType().name());p.sendMessage(plugin.color("&aSeleccionado: &e"+old.getType().name()));}}}}
    }
    @EventHandler public void onChat(AsyncPlayerChatEvent e){
        final Player p=e.getPlayer();final Pending a=pending.remove(p.getUniqueId());if(a==null)return;e.setCancelled(true);final String value=e.getMessage().trim();
        Bukkit.getScheduler().runTask(plugin,new Runnable(){public void run(){
            if(value.equalsIgnoreCase("cancelar")){p.sendMessage(plugin.color("&cCancelado."));mMain(p);return;}
            YamlConfiguration c=plugin.getCrateManager().getConfiguration();
            if((a.type==InputType.CREATE_CRATE||a.type==InputType.CREATE_KEY||a.type==InputType.CRATE_KEY)&&!value.matches("[A-Za-z0-9_.-]+")){p.sendMessage(plugin.color("&cID inválido."));mMain(p);return;}
            switch(a.type){
                case CREATE_CRATE: if(plugin.getCrateManager().createCrate(value))p.sendMessage(plugin.color("&aCrate creada: &e"+value));else p.sendMessage(plugin.color("&cYa existe."));mMain(p);break;
                case CREATE_KEY: if(plugin.getCrateManager().createKey(value))p.sendMessage(plugin.color("&aLlave creada: &e"+value));else p.sendMessage(plugin.color("&cYa existe."));mMain(p);break;
                case CRATE_NAME:c.set("crates."+a.id+".settings.display-name",value);saveOpen(p,false,a.id);break;
                case CRATE_PERMISSION:c.set("crates."+a.id+".settings.permission.node",value);saveOpen(p,false,a.id);break;
                case CRATE_KEY:if(!plugin.getCrateManager().keyExists(value))p.sendMessage(plugin.color("&cEsa llave no existe."));else c.set("crates."+a.id+".key-id",value);saveOpen(p,false,a.id);break;
                case KEY_NAME:c.set("keys."+a.id+".display-name",value);saveOpen(p,true,a.id);break;
                case KEY_PERMISSION:c.set("keys."+a.id+".permission.node",value);saveOpen(p,true,a.id);break;
                case KEY_MATERIAL:case CRATE_BLOCK_MATERIAL:Material mat=Material.matchMaterial(value);boolean crate=a.type==InputType.CRATE_BLOCK_MATERIAL;if(mat==null||mat==Material.AIR||(crate&&!mat.isBlock()))p.sendMessage(plugin.color("&cMaterial inválido."));else{c.set(crate?"crates."+a.id+".block.material":"keys."+a.id+".material",mat.name());plugin.getCrateManager().save();}if(crate)plugin.getCrateMenu().openCrateBlockEditor(p,a.id);else plugin.getCrateMenu().openKeyItemEditor(p,a.id);break;
            }
        }});
    }
    private void saveOpen(Player p,boolean key,String id){plugin.getCrateManager().save();if(key)plugin.getCrateMenu().openKeySettings(p,id);else plugin.getCrateMenu().openCrateSettings(p,id);}
    private void mMain(Player p){plugin.getCrateMenu().openMain(p);}
    private String loc(Block b){return b.getWorld().getName()+";"+b.getX()+";"+b.getY()+";"+b.getZ();}
    @EventHandler public void onPlace(BlockPlaceEvent e){String id=plugin.getCrateManager().getTag(e.getItemInHand(),"BC_CRATE");if(id!=null&&plugin.getCrateManager().crateExists(id)){plugin.getCrateManager().addPlaced(id,loc(e.getBlockPlaced()));e.getPlayer().sendMessage(plugin.color("&aCrate &e"+id+" &acolocada."));}}
    @EventHandler public void onBreak(BlockBreakEvent e){String id=plugin.getCrateManager().getPlacedCrate(loc(e.getBlock()));if(id!=null){plugin.getCrateManager().removePlaced(id,loc(e.getBlock()));e.setDropItems(false);e.getBlock().getWorld().dropItemNaturally(e.getBlock().getLocation(),plugin.getCrateManager().createCrateItem(id,1));}}
    @EventHandler public void onInteract(PlayerInteractEvent e){if(e.getAction()!=Action.RIGHT_CLICK_BLOCK||e.getClickedBlock()==null)return;String id=plugin.getCrateManager().getPlacedCrate(loc(e.getClickedBlock()));if(id!=null){e.setCancelled(true);e.getPlayer().sendMessage(plugin.color("&eCrate &6"+id+"&e: el sistema de apertura y recompensas se conectará en la siguiente etapa."));}}
}

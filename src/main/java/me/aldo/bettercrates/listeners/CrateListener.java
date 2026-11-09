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
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.EquipmentSlot;

import java.util.*;

public class CrateListener implements Listener {
    private enum InputType { CREATE_CRATE, CREATE_KEY, CRATE_NAME, CRATE_KEY, CRATE_PERMISSION, KEY_NAME, KEY_MATERIAL, KEY_PERMISSION, CRATE_BLOCK_MATERIAL, REWARD_CHANCE, HOLOGRAM_LINE, CRATE_MESSAGE }
    private static class Pending { final InputType type; final String id; Pending(InputType t,String i){type=t;id=i;} }
    private final BetterCrates plugin;
    private final Map<UUID,Pending> pending=new HashMap<UUID,Pending>();
    private final Map<UUID,String> selectedMaterial=new HashMap<UUID,String>();
    public CrateListener(BetterCrates plugin){this.plugin=plugin;}
    private void prompt(Player p,InputType t,String id,String message){pending.put(p.getUniqueId(),new Pending(t,id));p.closeInventory();p.sendMessage(plugin.color("&6BetterCrates &8» &e"+plugin.guiText(message)+" &7(type cancel to exit)"));}
    private boolean isMenu(String t){return t.equals(CrateMenu.MAIN)||t.equals(CrateMenu.CRATES)||t.equals(CrateMenu.KEYS)||t.startsWith(CrateMenu.CRATE_PREFIX)||t.startsWith(CrateMenu.KEY_PREFIX)||t.startsWith(CrateMenu.CRATE_BLOCK_PREFIX)||t.startsWith(CrateMenu.KEY_ITEM_PREFIX)||t.startsWith(CrateMenu.REWARDS_PREFIX)||t.startsWith(CrateMenu.PREVIEW_PREFIX)||t.startsWith(CrateMenu.HOLOGRAM_PREFIX)||t.startsWith(CrateMenu.MESSAGES_PREFIX);}
    @EventHandler public void onClick(InventoryClickEvent e){
        if(!(e.getWhoClicked() instanceof Player))return; String title=e.getView().getTitle(); if(!isMenu(title))return; e.setCancelled(true);
        if(e.getRawSlot()<0||e.getRawSlot()>=e.getView().getTopInventory().getSize())return;
        Player p=(Player)e.getWhoClicked(); int s=e.getRawSlot(); CrateMenu m=plugin.getCrateMenu(); YamlConfiguration c=plugin.getCrateManager().getConfiguration();
        if(title.equals(CrateMenu.MAIN)){if(s==10)prompt(p,InputType.CREATE_CRATE,null,"Enter the crate ID");else if(s==12)prompt(p,InputType.CREATE_KEY,null,"Enter the key ID");else if(s==14)m.openCrates(p);else if(s==16)m.openKeys(p);return;}
        if(title.equals(CrateMenu.CRATES)||title.equals(CrateMenu.KEYS)){
            if(s==49){m.openMain(p);return;}
            ItemStack it=e.getCurrentItem();
            if(it==null||!it.hasItemMeta()||!it.getItemMeta().hasLore())return;
            String id=null;
            for(String line:it.getItemMeta().getLore()){
                String plain=ChatColor.stripColor(line);
                if(plain.startsWith("ID:")){id=plain.substring(3).trim();break;}
            }
            if(id==null||id.isEmpty())return;
            if(title.equals(CrateMenu.CRATES)&&plugin.getCrateManager().crateExists(id))m.openCrateSettings(p,id);
            if(title.equals(CrateMenu.KEYS)&&plugin.getCrateManager().keyExists(id))m.openKeySettings(p,id);
            return;
        }


        if(title.startsWith(CrateMenu.MESSAGES_PREFIX)){
            String id=title.substring(CrateMenu.MESSAGES_PREFIX.length()),base="crates."+id+".messages.";
            if(s==40)m.openCrateSettings(p,id);
            else if(s==10){c.set(base+"enabled",!c.getBoolean(base+"enabled",true));plugin.getCrateManager().save();m.openMessagesEditor(p,id);}
            else if(s==12){c.set(base+"player.enabled",!c.getBoolean(base+"player.enabled",false));plugin.getCrateManager().save();m.openMessagesEditor(p,id);}
            else if(s==13)prompt(p,InputType.CRATE_MESSAGE,id+"|player.message","Enter the private message (%crate%, %player%)");
            else if(s==14){c.set(base+"global.enabled",!c.getBoolean(base+"global.enabled",false));plugin.getCrateManager().save();m.openMessagesEditor(p,id);}
            else if(s==15)prompt(p,InputType.CRATE_MESSAGE,id+"|global.message","Enter the global message (%crate%, %player%)");
            else if(s==16){c.set(base+"reward.enabled",!c.getBoolean(base+"reward.enabled",true));plugin.getCrateManager().save();m.openMessagesEditor(p,id);}
            else if(s==17)prompt(p,InputType.CRATE_MESSAGE,id+"|reward.message","Enter the reward message (%crate%, %player%, %reward%)");
            return;
        }
        if(title.startsWith(CrateMenu.HOLOGRAM_PREFIX)){
            String id=title.substring(CrateMenu.HOLOGRAM_PREFIX.length()),base="crates."+id+".hologram.";
            if(s==40)m.openCrateSettings(p,id);
            else if(s==10){c.set(base+"enabled",!c.getBoolean(base+"enabled",true));plugin.getCrateManager().save();plugin.getHologramManager().refreshCrate(id);m.openHologramEditor(p,id);}
            else if(s==12)prompt(p,InputType.HOLOGRAM_LINE,id+"|0","Enter hologram line 1");
            else if(s==13)prompt(p,InputType.HOLOGRAM_LINE,id+"|1","Enter hologram line 2");
            else if(s==14)prompt(p,InputType.HOLOGRAM_LINE,id+"|2","Enter hologram line 3");
            else if(s==16){c.set(base+"enabled",true);c.set(base+"lines",Arrays.asList("%crate_name%","&7Right-Click to open!","&7Left-Click to Preview!"));plugin.getCrateManager().save();plugin.getHologramManager().refreshCrate(id);m.openHologramEditor(p,id);}
            return;
        }
        if(title.startsWith(CrateMenu.PREVIEW_PREFIX)){
            String data=title.substring(CrateMenu.PREVIEW_PREFIX.length());int split=data.lastIndexOf(':');
            if(split<1)return;String crateId=data.substring(0,split);int page;
            try{page=Integer.parseInt(data.substring(split+1));}catch(NumberFormatException ex){return;}
            if(s==49){p.closeInventory();return;}
            if(s==45){m.openPreview(p,crateId,Math.max(0,page-1));return;}
            if(s==53){m.openPreview(p,crateId,page+1);return;}
            return;
        }
        if(title.startsWith(CrateMenu.REWARDS_PREFIX)){
            String data=title.substring(CrateMenu.REWARDS_PREFIX.length());int split=data.lastIndexOf(':');
            if(split<1)return;String crateId=data.substring(0,split);int page;
            try{page=Integer.parseInt(data.substring(split+1));}catch(NumberFormatException ex){return;}
            String base="crates."+crateId+".rewards.";
            if(s==49){m.openCrateSettings(p,crateId);return;}
            if(s==45){m.openRewards(p,crateId,Math.max(0,page-1));return;}
            if(s==53){m.openRewards(p,crateId,page+1);return;}
            int contentIndex=CrateMenu.contentIndex(s);if(contentIndex<0)return;
            List<String> ids=new ArrayList<String>();
            if(c.isConfigurationSection(base.substring(0,base.length()-1)))ids.addAll(c.getConfigurationSection(base.substring(0,base.length()-1)).getKeys(false));
            Collections.sort(ids);
            int index=page*CrateMenu.contentSlots().length+contentIndex;String rewardId=index<ids.size()?ids.get(index):null;
            ItemStack cursor=e.getCursor();
            if(cursor!=null&&cursor.getType()!=Material.AIR){
                if(rewardId==null){int n=1;while(c.isConfigurationSection(base+"reward-"+n))n++;rewardId="reward-"+n;}
                c.set(base+rewardId+".item",cursor.clone());
                if(!c.contains(base+rewardId+".chance-manual"))c.set(base+rewardId+".chance-manual",false);
                plugin.getCrateManager().normalizeRewardChances(crateId);
                e.setCursor(new ItemStack(Material.AIR));plugin.getCrateManager().save();m.openRewards(p,crateId,page);
            }else if(rewardId!=null){
                if(e.isRightClick()){c.set(base+rewardId,null);plugin.getCrateManager().normalizeRewardChances(crateId);plugin.getCrateManager().save();m.openRewards(p,crateId,page);}
                else prompt(p,InputType.REWARD_CHANCE,crateId+"|"+rewardId,"Enter the reward chance from 0 to 100");
            }
            return;
        }
        if(title.startsWith(CrateMenu.CRATE_BLOCK_PREFIX)||title.startsWith(CrateMenu.KEY_ITEM_PREFIX)){
            boolean crate=title.startsWith(CrateMenu.CRATE_BLOCK_PREFIX);String id=title.substring((crate?CrateMenu.CRATE_BLOCK_PREFIX:CrateMenu.KEY_ITEM_PREFIX).length());
            if(s==22){selectedMaterial.remove(p.getUniqueId());if(crate)m.openCrateSettings(p,id);else m.openKeySettings(p,id);}
            else if(s==11)prompt(p,crate?InputType.CRATE_BLOCK_MATERIAL:InputType.KEY_MATERIAL,id,"Enter a Bukkit material name");
            else if(s==13&&crate){ItemStack cursor=e.getCursor();if(cursor!=null&&cursor.getType()!=Material.AIR&&cursor.getType().isBlock()){selectedMaterial.put(p.getUniqueId(),cursor.getType().name());p.sendMessage(plugin.color("&aSelected block: &e"+cursor.getType().name()));}else{selectedMaterial.put(p.getUniqueId(),"");e.getView().getTopInventory().setItem(13,null);p.sendMessage(plugin.color("&eBlock selection cleared. Choose a block or save to restore the chest."));}}else if(s==13&&!crate){ItemStack cursor=e.getCursor();if(cursor!=null&&cursor.getType()!=Material.AIR&&cursor.getType().isItem()){selectedMaterial.put(p.getUniqueId(),cursor.getType().name());p.sendMessage(plugin.color("&aSelected: &e"+cursor.getType().name()));}}
            else if(s==15){String mat=selectedMaterial.remove(p.getUniqueId());if(crate&&mat!=null&&mat.isEmpty()){c.set("crates."+id+".block.material","CHEST");plugin.getCrateManager().save();p.sendMessage(plugin.color("&cYou must assign a block. The crate block has been restored to CHEST."));m.openCrateBlockEditor(p,id);return;}if(mat==null)mat=c.getString(crate?"crates."+id+".block.material":"keys."+id+".material");c.set(crate?"crates."+id+".block.material":"keys."+id+".material",mat);plugin.getCrateManager().save();p.sendMessage(plugin.color("&aMaterial saved: &e"+mat));if(crate)m.openCrateSettings(p,id);else m.openKeySettings(p,id);}
            return;
        }
        if(title.startsWith(CrateMenu.CRATE_PREFIX)){
            String id=title.substring(CrateMenu.CRATE_PREFIX.length()),path="crates."+id+".settings.";
            if(s==40)m.openCrates(p);
            else if(s==10){c.set(path+"enabled",!c.getBoolean(path+"enabled",true));plugin.getCrateManager().save();m.openCrateSettings(p,id);}
            else if(s==11)prompt(p,InputType.CRATE_NAME,id,"Enter the crate name (color codes supported)");
            else if(s==12)prompt(p,InputType.CRATE_KEY,id,"Enter the ID of an existing key");
            else if(s==14){c.set(path+"permission.enabled",!c.getBoolean(path+"permission.enabled",false));plugin.getCrateManager().save();m.openCrateSettings(p,id);}
            else if(s==15)prompt(p,InputType.CRATE_PERMISSION,id,"Enter the permission node");
            else if(s==16)m.openCrateBlockEditor(p,id);
            else if(s==19)m.openRewards(p,id,0);
            else if(s==20){String q="crates."+id+".block.glint";c.set(q,!c.getBoolean(q,false));plugin.getCrateManager().save();m.openCrateSettings(p,id);}
            else if(s==21)m.openHologramEditor(p,id);
            else if(s==23)m.openMessagesEditor(p,id);
            return;
        }
        if(title.startsWith(CrateMenu.KEY_PREFIX)){
            String id=title.substring(CrateMenu.KEY_PREFIX.length()),path="keys."+id+".";
            if(s==40)m.openKeys(p);
            else if(s==10)prompt(p,InputType.KEY_NAME,id,"Enter the key name (color codes supported)");
            else if(s==12)m.openKeyItemEditor(p,id);
            else if(s==14){c.set(path+"permission.enabled",!c.getBoolean(path+"permission.enabled",false));plugin.getCrateManager().save();m.openKeySettings(p,id);}
            else if(s==15)prompt(p,InputType.KEY_PERMISSION,id,"Enter the permission node");
            else if(s==16){c.set(path+"glint",!c.getBoolean(path+"glint",false));plugin.getCrateManager().save();m.openKeySettings(p,id);}
        }
    }
    @EventHandler public void onDrag(InventoryDragEvent e){
        String title=e.getView().getTitle();
        if(title.startsWith(CrateMenu.REWARDS_PREFIX)){
            e.setCancelled(true);
            if(e.getRawSlots().size()==1){
                int raw=e.getRawSlots().iterator().next();ItemStack old=e.getOldCursor();int contentIndex=CrateMenu.contentIndex(raw);
                if(contentIndex>=0&&old!=null&&old.getType()!=Material.AIR&&e.getWhoClicked() instanceof Player){
                    String data=title.substring(CrateMenu.REWARDS_PREFIX.length());int split=data.lastIndexOf(':');
                    if(split>0){
                        String crateId=data.substring(0,split);int page;
                        try{page=Integer.parseInt(data.substring(split+1));}catch(NumberFormatException ex){return;}
                        YamlConfiguration c=plugin.getCrateManager().getConfiguration();String base="crates."+crateId+".rewards.";
                        List<String> ids=new ArrayList<String>();
                        if(c.isConfigurationSection(base.substring(0,base.length()-1)))ids.addAll(c.getConfigurationSection(base.substring(0,base.length()-1)).getKeys(false));
                        Collections.sort(ids);int index=page*CrateMenu.contentSlots().length+contentIndex;String rewardId=index<ids.size()?ids.get(index):null;
                        if(rewardId==null){int n=1;while(c.isConfigurationSection(base+"reward-"+n))n++;rewardId="reward-"+n;}
                        c.set(base+rewardId+".item",old.clone());
                        if(!c.contains(base+rewardId+".chance-manual"))c.set(base+rewardId+".chance-manual",false);
                        plugin.getCrateManager().normalizeRewardChances(crateId);
                        e.setCursor(new ItemStack(Material.AIR));
                        plugin.getCrateManager().save();plugin.getCrateMenu().openRewards((Player)e.getWhoClicked(),crateId,page);
                    }
                }
            }
            return;
        }
        if(!title.startsWith(CrateMenu.CRATE_BLOCK_PREFIX)&&!title.startsWith(CrateMenu.KEY_ITEM_PREFIX))return;
        if(e.getRawSlots().contains(13)){e.setCancelled(true);if(e.getWhoClicked() instanceof Player){Player p=(Player)e.getWhoClicked();ItemStack old=e.getOldCursor();if(old!=null&&old.getType()!=Material.AIR){boolean crate=title.startsWith(CrateMenu.CRATE_BLOCK_PREFIX);if((crate&&old.getType().isBlock())||(!crate&&old.getType().isItem())){selectedMaterial.put(p.getUniqueId(),old.getType().name());p.sendMessage(plugin.color("&aSelected: &e"+old.getType().name()));}}}}
    }
    @EventHandler public void onChat(AsyncPlayerChatEvent e){
        final Player p=e.getPlayer();final Pending a=pending.remove(p.getUniqueId());if(a==null)return;e.setCancelled(true);final String value=e.getMessage().trim();
        Bukkit.getScheduler().runTask(plugin,new Runnable(){public void run(){
            if(value.equalsIgnoreCase("cancel")||value.equalsIgnoreCase("cancelar")){p.sendMessage(plugin.color("&cCancelled."));mMain(p);return;}
            YamlConfiguration c=plugin.getCrateManager().getConfiguration();
            if((a.type==InputType.CREATE_CRATE||a.type==InputType.CREATE_KEY||a.type==InputType.CRATE_KEY)&&!value.matches("[A-Za-z0-9_.-]+")){p.sendMessage(plugin.color("&cInvalid ID."));mMain(p);return;}
            switch(a.type){
                case CREATE_CRATE: if(plugin.getCrateManager().createCrate(value))p.sendMessage(plugin.color("&aCrate created: &e"+value));else p.sendMessage(plugin.color("&cThat ID already exists."));mMain(p);break;
                case CREATE_KEY: if(plugin.getCrateManager().createKey(value))p.sendMessage(plugin.color("&aKey created: &e"+value));else p.sendMessage(plugin.color("&cThat ID already exists."));mMain(p);break;
                case CRATE_NAME:c.set("crates."+a.id+".settings.display-name",value);plugin.getCrateManager().save();plugin.getHologramManager().refreshCrate(a.id);plugin.getCrateMenu().openCrateSettings(p,a.id);break;
                case CRATE_PERMISSION:c.set("crates."+a.id+".settings.permission.node",value);saveOpen(p,false,a.id);break;
                case CRATE_KEY:if(!plugin.getCrateManager().keyExists(value))p.sendMessage(plugin.color("&cThat key does not exist."));else c.set("crates."+a.id+".key-id",value);saveOpen(p,false,a.id);break;
                case KEY_NAME:c.set("keys."+a.id+".display-name",value);saveOpen(p,true,a.id);break;
                case KEY_PERMISSION:c.set("keys."+a.id+".permission.node",value);saveOpen(p,true,a.id);break;
                case CRATE_MESSAGE:{
                    String[] parts=a.id.split("\\|",2);
                    if(parts.length!=2){mMain(p);break;}
                    String field=parts[1];
                    if(!field.equals("player.message")&&!field.equals("global.message")&&!field.equals("reward.message")){mMain(p);break;}
                    c.set("crates."+parts[0]+".messages."+field,value);
                    plugin.getCrateManager().save();
                    plugin.getCrateMenu().openMessagesEditor(p,parts[0]);
                    break;
                }
                case HOLOGRAM_LINE:{
                    String[] parts=a.id.split("\\|",2);
                    if(parts.length!=2){mMain(p);break;}
                    int line;
                    try{line=Integer.parseInt(parts[1]);}catch(NumberFormatException ex){mMain(p);break;}
                    if(line<0||line>2){mMain(p);break;}
                    List<String> lines=c.getStringList("crates."+parts[0]+".hologram.lines");
                    while(lines.size()<3)lines.add("");
                    lines.set(line,value);
                    c.set("crates."+parts[0]+".hologram.lines",lines);
                    plugin.getCrateManager().save();
                    plugin.getHologramManager().refreshCrate(parts[0]);
                    plugin.getCrateMenu().openHologramEditor(p,parts[0]);
                    break;
                }
                case REWARD_CHANCE:{
                    String[] parts=a.id.split("\\|",2);
                    if(parts.length!=2){mMain(p);break;}
                    try{double chance=Double.parseDouble(value);if(Double.isNaN(chance)||chance<0||chance>100)throw new NumberFormatException();c.set("crates."+parts[0]+".rewards."+parts[1]+".chance",chance);c.set("crates."+parts[0]+".rewards."+parts[1]+".chance-manual",true);plugin.getCrateManager().normalizeRewardChances(parts[0]);plugin.getCrateManager().save();plugin.getCrateMenu().openRewards(p,parts[0],0);}
                    catch(NumberFormatException ex){p.sendMessage(plugin.color("&cEnter a number between 0 and 100."));plugin.getCrateMenu().openRewards(p,parts[0],0);}
                    break;
                }
                case KEY_MATERIAL:case CRATE_BLOCK_MATERIAL:Material mat=Material.matchMaterial(value);boolean crate=a.type==InputType.CRATE_BLOCK_MATERIAL;if(mat==null||mat==Material.AIR||(crate&&!mat.isBlock()))p.sendMessage(plugin.color("&cInvalid material."));else{c.set(crate?"crates."+a.id+".block.material":"keys."+a.id+".material",mat.name());plugin.getCrateManager().save();}if(crate)plugin.getCrateMenu().openCrateBlockEditor(p,a.id);else plugin.getCrateMenu().openKeyItemEditor(p,a.id);break;
            }
        }});
    }
    private String getRewardName(ItemStack prize){
        String name=null;
        if(prize.hasItemMeta()&&prize.getItemMeta().hasDisplayName()){
            name=ChatColor.stripColor(prize.getItemMeta().getDisplayName());
        }
        if(name==null||name.trim().isEmpty()){
            String type=prize.getType().name().toLowerCase(Locale.ENGLISH).replace('_',' ');
            if(type.equals("grilled pork"))name="chuleta";
            else if(type.equals("pork"))name="chuleta cruda";
            else name=type;
        }
        if(prize.getAmount()>1)name=pluralize(name);
        return (prize.getAmount()>1?prize.getAmount()+" ":"")+name;
    }
    private String pluralize(String name){
        if(name.equalsIgnoreCase("cooked pork"))return "cooked pork chops";
        if(name.equalsIgnoreCase("raw pork"))return "raw pork chops";
        if(name.endsWith("s")||name.endsWith("x"))return name;
        return name+"s";
    }
    private void saveOpen(Player p,boolean key,String id){plugin.getCrateManager().save();if(key)plugin.getCrateMenu().openKeySettings(p,id);else plugin.getCrateMenu().openCrateSettings(p,id);}
    private void mMain(Player p){plugin.getCrateMenu().openMain(p);}
    private String loc(Block b){return b.getWorld().getName()+";"+b.getX()+";"+b.getY()+";"+b.getZ();}
    @EventHandler public void onPlace(BlockPlaceEvent e){String id=plugin.getCrateManager().getTag(e.getItemInHand(),"BC_CRATE");if(id!=null&&plugin.getCrateManager().crateExists(id)){plugin.getCrateManager().addPlaced(id,loc(e.getBlockPlaced()));plugin.getHologramManager().refreshAt(id,e.getBlockPlaced().getLocation());e.getPlayer().sendMessage(plugin.color("&aCrate &e"+id+" &aplaced."));}}
    @EventHandler public void onBreak(BlockBreakEvent e){String id=plugin.getCrateManager().getPlacedCrate(loc(e.getBlock()));if(id!=null){plugin.getHologramManager().removeAt(e.getBlock().getLocation());plugin.getCrateManager().removePlaced(id,loc(e.getBlock()));e.setDropItems(false);e.getBlock().getWorld().dropItemNaturally(e.getBlock().getLocation(),plugin.getCrateManager().createCrateItem(id,1));}}
    @EventHandler public void onChunkLoad(ChunkLoadEvent e){plugin.getHologramManager().handleChunkLoad(e.getChunk());}

    @EventHandler public void onInteract(PlayerInteractEvent e){
        if(e.getHand()!=EquipmentSlot.HAND)return;
        if(e.getClickedBlock()==null)return;
        if(e.getAction()!=Action.LEFT_CLICK_BLOCK&&e.getAction()!=Action.RIGHT_CLICK_BLOCK)return;
        String id=plugin.getCrateManager().getPlacedCrate(loc(e.getClickedBlock()));
        if(id==null)return;
        e.setCancelled(true);
        Player p=e.getPlayer();
        YamlConfiguration c=plugin.getCrateManager().getConfiguration();
        String cratePath="crates."+id+".";
        if(!c.getBoolean(cratePath+"settings.enabled",true)){
            p.sendMessage(plugin.color("&cThis crate is disabled."));
            return;
        }
        if(e.getAction()==Action.LEFT_CLICK_BLOCK){
            plugin.getCrateMenu().openPreview(p,id,0);
            return;
        }
        String cratePermission=c.getString(cratePath+"settings.permission.node","bettercrates.crate."+id+".open");
        if(c.getBoolean(cratePath+"settings.permission.enabled",false)&&!p.hasPermission(cratePermission)){
            p.sendMessage(plugin.color("&cYou do not have permission to open this crate."));
            return;
        }
        String keyId=c.getString(cratePath+"key-id",id);
        if(!plugin.getCrateManager().keyExists(keyId)){
            p.sendMessage(plugin.color("&cThe key configured for this crate does not exist."));
            return;
        }
        String keyPath="keys."+keyId+".";
        String keyPermission=c.getString(keyPath+"permission.node","bettercrates.key."+keyId+".use");
        if(c.getBoolean(keyPath+"permission.enabled",false)&&!p.hasPermission(keyPermission)){
            p.sendMessage(plugin.color("&cYou do not have permission to use this key."));
            return;
        }
        int keySlot=-1;
        ItemStack[] contents=p.getInventory().getContents();
        for(int i=0;i<contents.length;i++){
            if(keyId.equals(plugin.getCrateManager().getTag(contents[i],"BC_KEY"))){keySlot=i;break;}
        }
        if(keySlot<0){
            org.bukkit.util.Vector push=p.getLocation().toVector().subtract(e.getClickedBlock().getLocation().add(0.5,0.5,0.5).toVector());
            push.setY(0);
            if(push.lengthSquared()<0.0001)push=p.getLocation().getDirection().multiply(-1).setY(0);
            if(push.lengthSquared()>0)push.normalize().multiply(0.8);
            push.setY(0.25);
            p.setVelocity(push);
            p.sendMessage(plugin.color("&cYou do not have a key to open this crate."));
            return;
        }
        String rewardsPath=cratePath+"rewards.";
        if(!c.isConfigurationSection(cratePath+"rewards")){
            p.sendMessage(plugin.color("&cThis crate has no rewards configured yet."));
            return;
        }
        List<String> rewardIds=new ArrayList<String>(c.getConfigurationSection(cratePath+"rewards").getKeys(false));
        double total=0.0;
        for(String rewardId:rewardIds){
            ItemStack reward=c.getItemStack(rewardsPath+rewardId+".item");
            double chance=c.getDouble(rewardsPath+rewardId+".chance",100.0);
            if(reward!=null&&reward.getType()!=Material.AIR&&chance>0&&!Double.isNaN(chance)&&!Double.isInfinite(chance))total+=chance;
        }
        if(total<=0){
            p.sendMessage(plugin.color("&cThis crate has no rewards with a valid chance."));
            return;
        }
        double roll=Math.random()*total;
        String selected=null;
        for(String rewardId:rewardIds){
            ItemStack reward=c.getItemStack(rewardsPath+rewardId+".item");
            double chance=c.getDouble(rewardsPath+rewardId+".chance",100.0);
            if(reward==null||reward.getType()==Material.AIR||chance<=0||Double.isNaN(chance)||Double.isInfinite(chance))continue;
            roll-=chance;
            if(roll<0){selected=rewardId;break;}
        }
        if(selected==null){
            for(String rewardId:rewardIds){
                ItemStack reward=c.getItemStack(rewardsPath+rewardId+".item");
                double chance=c.getDouble(rewardsPath+rewardId+".chance",100.0);
                if(reward!=null&&reward.getType()!=Material.AIR&&chance>0&&!Double.isNaN(chance)&&!Double.isInfinite(chance)){selected=rewardId;break;}
            }
        }
        if(selected==null)return;
        ItemStack key=contents[keySlot];
        if(key.getAmount()>1)key.setAmount(key.getAmount()-1);else p.getInventory().setItem(keySlot,null);
        ItemStack prize=c.getItemStack(rewardsPath+selected+".item").clone();
        Map<Integer,ItemStack> leftovers=p.getInventory().addItem(prize);
        for(ItemStack leftover:leftovers.values())p.getWorld().dropItemNaturally(p.getLocation(),leftover);
        String messageBase=cratePath+"messages.";
        if(c.getBoolean(messageBase+"enabled",true)){
            String crateName=c.getString(cratePath+"settings.display-name","&e"+id);
            String playerMessage=c.getString(messageBase+"player.message","&aYou opened %crate%&a.");
            if(c.getBoolean(messageBase+"player.enabled",false)&&!playerMessage.isEmpty()){
                p.sendMessage(plugin.color(playerMessage.replace("%crate%",crateName).replace("%player%",p.getName())));
            }
            String globalMessage=c.getString(messageBase+"global.message","&e%player% &7opened %crate%&7.");
            if(c.getBoolean(messageBase+"global.enabled",false)&&!globalMessage.isEmpty()){
                String rendered=plugin.color(globalMessage.replace("%crate%",crateName).replace("%player%",p.getName()));
                for(Player online:Bukkit.getOnlinePlayers())online.sendMessage(rendered);
            }
            if(c.getBoolean(messageBase+"reward.enabled",true)){
                String rewardMessage=c.getString(messageBase+"reward.message","&eYou won &f%reward%&e!");
                if(!rewardMessage.isEmpty()){
                    String rewardName=getRewardName(prize);
                    String rendered=rewardMessage.replace("%crate%",crateName).replace("%player%",p.getName()).replace("%reward%",rewardName);
                    p.sendMessage(plugin.color(rendered));
                }
            }
        }
        p.updateInventory();
    }
}

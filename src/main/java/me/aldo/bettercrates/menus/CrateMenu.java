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
import java.util.List;

public class CrateMenu {
    private final BetterCrates plugin;
    public static final String MAIN="§8BetterCrates §7» §6Main Menu", CRATES="§8BetterCrates §7» §6Crates", KEYS="§8BetterCrates §7» §6Keys";
    public static final String CRATE_PREFIX="§8Edit Crate §7» §e", KEY_PREFIX="§8Edit Key §7» §e";
    public static final String REWARDS_PREFIX="§8Rewards §7» §e";
    public static final String PREVIEW_PREFIX="§8Available Rewards §7» §e";
    public static final String CRATE_BLOCK_PREFIX="§8Crate Block §7» §e", KEY_ITEM_PREFIX="§8Key Item §7» §e";
    public static final String HOLOGRAM_PREFIX="§8Crate Hologram §7» §e";
    public static final String MESSAGES_PREFIX="§8Crate Messages §7» §e";
    public CrateMenu(BetterCrates plugin){this.plugin=plugin;}

    private ItemStack item(Material material,String name,String... lore){
        ItemStack stack=new ItemStack(material); ItemMeta meta=stack.getItemMeta(); meta.setDisplayName(plugin.color(plugin.guiText(name)));
        List<String> lines=new ArrayList<String>(); for(String line:lore) lines.add(plugin.color(plugin.guiText(line))); meta.setLore(lines); stack.setItemMeta(meta); return stack;
    }
    private void fill(Inventory inv){ Material filler=Material.matchMaterial(plugin.getConfig().getString("customization.gui.filler-material","STAINED_GLASS_PANE")); if(filler==null||!filler.isItem())filler=Material.STAINED_GLASS_PANE; ItemStack pane=new ItemStack(filler,1,(short)plugin.getConfig().getInt("customization.gui.filler-data",7)); ItemMeta paneMeta=pane.getItemMeta(); paneMeta.setDisplayName(plugin.color(plugin.guiText(plugin.getConfig().getString("customization.gui.filler-name","&8 ")))); pane.setItemMeta(paneMeta); for(int i=0;i<inv.getSize();i++) if(inv.getItem(i)==null) inv.setItem(i,pane); }
    public void openMain(Player p){
        Inventory inv=Bukkit.createInventory(null,27,MAIN);
        inv.setItem(10,item(Material.EMERALD,"&a&lCreate Crate","&7Create a new crate"));
        inv.setItem(12,item(Material.TRIPWIRE_HOOK,"&6&lCreate Key","&7Create a key"));
        inv.setItem(14,item(Material.CHEST,"&e&lConfigure Crates"));
        inv.setItem(16,item(Material.TRIPWIRE_HOOK,"&e&lConfigure Keys")); fill(inv); p.openInventory(inv);
    }
    public void openCrates(Player p){
        Inventory inv=Bukkit.createInventory(null,54,CRATES); int slot=0; YamlConfiguration c=plugin.getCrateManager().getConfiguration();
        for(String id:plugin.getCrateManager().getCrateIds()){if(slot>=45)break; String path="crates."+id+".settings.";
            inv.setItem(slot++,item(Material.CHEST,c.getString(path+"display-name","&e"+id),"&7ID: &f"+id,"&7Status: "+(c.getBoolean(path+"enabled",true)?"&aEnabled":"&cDisabled"),"&eClick to edit"));}
        inv.setItem(49,item(Material.ARROW,"&cBack")); fill(inv); p.openInventory(inv);
    }
    public void openKeys(Player p){
        Inventory inv=Bukkit.createInventory(null,54,KEYS); int slot=0; YamlConfiguration c=plugin.getCrateManager().getConfiguration();
        for(String id:plugin.getCrateManager().getKeyIds()){if(slot>=45)break; inv.setItem(slot++,item(Material.TRIPWIRE_HOOK,c.getString("keys."+id+".display-name","&6"+id),"&7ID: &f"+id,"&eClick to edit"));}
        inv.setItem(49,item(Material.ARROW,"&cBack")); fill(inv); p.openInventory(inv);
    }
    public void openCrateSettings(Player p,String id){
        YamlConfiguration c=plugin.getCrateManager().getConfiguration(); String path="crates."+id+".settings.";
        if(!plugin.getCrateManager().crateExists(id)){openCrates(p);return;}
        Inventory inv=Bukkit.createInventory(null,45,CRATE_PREFIX+id);
        inv.setItem(10,item(Material.REDSTONE_TORCH_ON,"&aEnable / Disable","&7Status: "+(c.getBoolean(path+"enabled",true)?"&aEnabled":"&cDisabled")));
        inv.setItem(11,item(Material.NAME_TAG,"&eName and Color","&7Current: "+c.getString(path+"display-name","&e"+id),"&eClick and type a colored name (&)"));
        inv.setItem(12,item(Material.TRIPWIRE_HOOK,"&6Assign Key","&7Current: &f"+c.getString("crates."+id+".key-id",id)));
        inv.setItem(14,item(Material.IRON_DOOR,"&dPermission to open","&7Required: "+(c.getBoolean(path+"permission.enabled",false)?"&aYes":"&cNo")));
        inv.setItem(15,item(Material.PAPER,"&ePermission Node","&7"+c.getString(path+"permission.node","")));
        inv.setItem(16,item(Material.CHEST,"&bEdit Crate Block","&7Material: &f"+c.getString("crates."+id+".block.material","CHEST"),"&eClick to open editor"));
        inv.setItem(19,item(Material.CHEST,"&6Edit Rewards","&7Configure rewards and their chances","&eClick to open"));
        inv.setItem(20,item(Material.ENCHANTED_BOOK,"&dEnchantment Glint","&7"+(c.getBoolean("crates."+id+".block.glint",false)?"&aEnabled":"&cDisabled")));
        inv.setItem(21,item(Material.NAME_TAG,"&bEdit Hologram","&7Status: "+(c.getBoolean("crates."+id+".hologram.enabled",true)?"&aEnabled":"&cDisabled"),"&eConfigure the 3 lines above the crate"));
        inv.setItem(23,item(Material.PAPER,"&bOpening messages","&7Customize private and global messages"));
        inv.setItem(40,item(Material.ARROW,"&cBack")); fill(inv); p.openInventory(inv);
    }
    public void openHologramEditor(Player p,String id){
        YamlConfiguration c=plugin.getCrateManager().getConfiguration();
        String base="crates."+id+".hologram.";
        if(!plugin.getCrateManager().crateExists(id)){openCrates(p);return;}
        List<String> lines=c.getStringList(base+"lines");
        while(lines.size()<3)lines.add("");
        String crateName=c.getString("crates."+id+".settings.display-name","&e"+id);
        Inventory inv=Bukkit.createInventory(null,45,HOLOGRAM_PREFIX+id);
        inv.setItem(10,item(Material.REDSTONE_TORCH_ON,"&aEnable / Disable Hologram","&7Status: "+(c.getBoolean(base+"enabled",true)?"&aEnabled":"&cDisabled")));
        inv.setItem(12,item(Material.NAME_TAG,"&eLine 1","&7Current: "+lines.get(0).replace("%crate_name%",crateName),"&eClick to edit"));
        inv.setItem(13,item(Material.PAPER,"&eLine 2","&7Current: "+lines.get(1),"&eClick to edit"));
        inv.setItem(14,item(Material.PAPER,"&eLine 3","&7Current: "+lines.get(2),"&eClick to edit"));
        inv.setItem(16,item(Material.BOOK,"&bRestore defaults","&7"+crateName,"&7&fRight-Click to open!","&7&fLeft-Click to Preview!"));
        inv.setItem(40,item(Material.ARROW,"&cBack"));
        fill(inv);p.openInventory(inv);
    }
    public void openMessagesEditor(Player p,String id){
        YamlConfiguration c=plugin.getCrateManager().getConfiguration();
        String base="crates."+id+".messages.";
        if(!plugin.getCrateManager().crateExists(id)){openCrates(p);return;}
        Inventory inv=Bukkit.createInventory(null,45,MESSAGES_PREFIX+id);
        inv.setItem(10,item(Material.REDSTONE_TORCH_ON,"&aAll Messages","&7Status: "+(c.getBoolean(base+"enabled",true)?"&aEnableds":"&cDisableds"),"&7Disable this to silence all messages"));
        inv.setItem(12,item(Material.NAME_TAG,"&ePrivate message on open","&7Status: "+(c.getBoolean(base+"player.enabled",false)?"&aEnabled":"&cDisabled"),"&7Current: "+c.getString(base+"player.message","")));
        inv.setItem(13,item(Material.PAPER,"&eEdit private message","&7Click to write the message"));
        inv.setItem(14,item(Material.BOOK,"&6Global message on open","&7Status: "+(c.getBoolean(base+"global.enabled",false)?"&aEnabled":"&cDisabled"),"&7Current: "+c.getString(base+"global.message","")));
        inv.setItem(15,item(Material.PAPER,"&6Edit global message","&7Click to write the message"));
        inv.setItem(16,item(Material.EMERALD,"&bReward message","&7Status: "+(c.getBoolean(base+"reward.enabled",true)?"&aEnabled":"&cDisabled"),"&7Current: "+c.getString(base+"reward.message","")));
        inv.setItem(17,item(Material.PAPER,"&bEdit reward message","&7Click to write the message"));
        inv.setItem(40,item(Material.ARROW,"&cBack"));
        fill(inv);p.openInventory(inv);
    }
    public void openKeySettings(Player p,String id){
        YamlConfiguration c=plugin.getCrateManager().getConfiguration(); String path="keys."+id+".";
        if(!plugin.getCrateManager().keyExists(id)){openKeys(p);return;}
        Inventory inv=Bukkit.createInventory(null,45,KEY_PREFIX+id);
        inv.setItem(10,item(Material.NAME_TAG,"&eName and Color","&7Current: "+c.getString(path+"display-name","&6"+id),"&eClick and type a colored name (&)"));
        inv.setItem(12,item(Material.CHEST,"&bEdit Key Item","&7Material: &f"+c.getString(path+"material","TRIPWIRE_HOOK"),"&eClick to open editor"));
        inv.setItem(14,item(Material.IRON_DOOR,"&dPermission to use key","&7Required: "+(c.getBoolean(path+"permission.enabled",false)?"&aSí":"&cNo")));
        inv.setItem(15,item(Material.PAPER,"&ePermission Node","&7"+c.getString(path+"permission.node","")));
        inv.setItem(16,item(Material.ENCHANTED_BOOK,"&dEnchantment Glint","&7"+(c.getBoolean(path+"glint",false)?"&aEnabled":"&cDisabled")));
        inv.setItem(40,item(Material.ARROW,"&cBack")); fill(inv); p.openInventory(inv);
    }
    public void openCrateBlockEditor(Player p,String id){
        Inventory inv=Bukkit.createInventory(null,27,CRATE_BLOCK_PREFIX+id); YamlConfiguration c=plugin.getCrateManager().getConfiguration();
        String m=c.getString("crates."+id+".block.material","CHEST"); Material mat=Material.matchMaterial(m); if(mat==null)mat=Material.CHEST;
        inv.setItem(11,item(Material.PAPER,"&eWrite Material","&7Enter the Bukkit block material name"));
        inv.setItem(13,item(mat,"&aSelected Block","&7Current: &f"+m,"&eDrag a block here from your inventory"));
        inv.setItem(15,item(Material.EMERALD,"&aSave Block","&7Save the selected material"));
        inv.setItem(22,item(Material.ARROW,"&cBack")); fill(inv); p.openInventory(inv);
    }
    public void openKeyItemEditor(Player p,String id){
        Inventory inv=Bukkit.createInventory(null,27,KEY_ITEM_PREFIX+id); YamlConfiguration c=plugin.getCrateManager().getConfiguration();
        String m=c.getString("keys."+id+".material","TRIPWIRE_HOOK"); Material mat=Material.matchMaterial(m); if(mat==null)mat=Material.TRIPWIRE_HOOK;
        inv.setItem(11,item(Material.PAPER,"&eWrite Material","&7Enter the Bukkit item material name"));
        inv.setItem(13,item(mat,"&aSelected Item","&7Current: &f"+m,"&eDrag an item here from your inventory"));
        inv.setItem(15,item(Material.EMERALD,"&aSave Item","&7Save the selected material"));
        inv.setItem(22,item(Material.ARROW,"&cBack")); fill(inv); p.openInventory(inv);
    }
    public static int[] contentSlots(){
        return new int[]{10,11,12,13,14,15,16,19,20,21,22,23,24,25,28,29,30,31,32,33,34,37,38,39,40,41,42,43};
    }
    public static int contentIndex(int slot){
        int[] slots=contentSlots();
        for(int i=0;i<slots.length;i++)if(slots[i]==slot)return i;
        return -1;
    }
    private void decorateSides(Inventory inv){
        Material filler=Material.matchMaterial(plugin.getConfig().getString("customization.gui.filler-material","STAINED_GLASS_PANE"));if(filler==null||!filler.isItem())filler=Material.STAINED_GLASS_PANE;ItemStack pane=new ItemStack(filler,1,(short)plugin.getConfig().getInt("customization.gui.filler-data",7));
        ItemMeta meta=pane.getItemMeta();meta.setDisplayName(plugin.color(plugin.guiText(plugin.getConfig().getString("customization.gui.filler-name","&8 "))));pane.setItemMeta(meta);
        int[] sides={9,18,27,36,17,26,35,44};
        for(int slot:sides)inv.setItem(slot,pane);
    }
    public void openRewards(Player p,String id,int page){
        plugin.getCrateManager().normalizeRewardChances(id);
        plugin.getCrateManager().save();
        YamlConfiguration c=plugin.getCrateManager().getConfiguration();
        if(!plugin.getCrateManager().crateExists(id)){openCrates(p);return;}
        Inventory inv=Bukkit.createInventory(null,54,REWARDS_PREFIX+id+":"+page);
        List<String> rewards=new ArrayList<String>();
        if(c.isConfigurationSection("crates."+id+".rewards"))rewards.addAll(c.getConfigurationSection("crates."+id+".rewards").getKeys(false));
        java.util.Collections.sort(rewards);
        int[] slots=contentSlots();
        int start=page*slots.length;
        for(int i=0;i<slots.length&&start+i<rewards.size();i++){
            String rewardId=rewards.get(start+i),path="crates."+id+".rewards."+rewardId;
            ItemStack reward=c.getItemStack(path+".item");
            if(reward==null||reward.getType()==Material.AIR)continue;
            ItemStack display=reward.clone();ItemMeta meta=display.getItemMeta();
            List<String> lore=meta.hasLore()?new ArrayList<String>(meta.getLore()):new ArrayList<String>();
            lore.add(plugin.color("&8ID: &7"+rewardId));
            lore.add(plugin.color("&eChance: &f"+String.format(java.util.Locale.US,"%.2f",c.getDouble(path+".chance",0.0))+"%"));
            lore.add(plugin.color(c.getBoolean(path+".chance-manual",false)?"&aManual chance":"&bAutomatic chance"));
            lore.add(plugin.color("&7Left-click: edit chance"));
            lore.add(plugin.color("&7Right-click: remove reward"));
            meta.setLore(lore);display.setItemMeta(meta);inv.setItem(slots[i],display);
        }
        decorateSides(inv);
        inv.setItem(45,item(Material.ARROW,"&ePrevious Page"));
        inv.setItem(47,item(Material.BOOK,"&6Reward editor","&7Drag an item from your inventory","&7to an empty center slot","&7Chances auto-balance to 100%"));
        inv.setItem(49,item(Material.ARROW,"&cExit","&7Return to crate settings"));
        inv.setItem(51,item(Material.PAPER,"&ePage &f"+(page+1),"&7Rewards: &f"+rewards.size()));
        inv.setItem(53,item(Material.ARROW,"&eNext Page"));
        p.openInventory(inv);
    }

    public void openPreview(Player p,String id,int page){
        YamlConfiguration c=plugin.getCrateManager().getConfiguration();
        if(!plugin.getCrateManager().crateExists(id)){p.closeInventory();return;}
        Inventory inv=Bukkit.createInventory(null,54,PREVIEW_PREFIX+id+":"+page);
        List<String> rewards=new ArrayList<String>();
        String base="crates."+id+".rewards.";
        if(c.isConfigurationSection(base.substring(0,base.length()-1)))rewards.addAll(c.getConfigurationSection(base.substring(0,base.length()-1)).getKeys(false));
        java.util.Collections.sort(rewards);
        double total=0.0;
        for(String rewardId:rewards){double chance=c.getDouble(base+rewardId+".chance",100.0);ItemStack configured=c.getItemStack(base+rewardId+".item");if(configured!=null&&configured.getType()!=Material.AIR&&chance>0&&!Double.isNaN(chance)&&!Double.isInfinite(chance))total+=chance;}
        int[] slots=contentSlots();int start=page*slots.length;int shown=0;
        for(int i=0;i<slots.length&&start+i<rewards.size();i++){
            String rewardId=rewards.get(start+i),path=base+rewardId;ItemStack reward=c.getItemStack(path+".item");
            if(reward==null||reward.getType()==Material.AIR)continue;
            ItemStack display=reward.clone();ItemMeta meta=display.getItemMeta();
            List<String> lore=meta.hasLore()?new ArrayList<String>(meta.getLore()):new ArrayList<String>();
            double weight=c.getDouble(path+".chance",100.0);double actual=total>0&&weight>0?(weight/total)*100.0:0.0;
            lore.add(plugin.color("&8 "));lore.add(plugin.color("&eApproximate chance: &f"+String.format(java.util.Locale.US,"%.2f",actual)+"%"));
            meta.setLore(lore);display.setItemMeta(meta);inv.setItem(slots[i],display);shown++;
        }
        decorateSides(inv);
        inv.setItem(45,item(Material.ARROW,"&ePrevious Page"));
        inv.setItem(49,item(Material.ARROW,"&cExit","&7Cerrar vista previa"));
        inv.setItem(51,item(Material.PAPER,"&6Rewards for &e"+id,"&7Available items: &f"+rewards.size()));
        inv.setItem(53,item(Material.ARROW,"&eNext Page"));
        if(shown==0)inv.setItem(22,item(Material.BARRIER,"&cThis crate has no rewards configured"));
        p.openInventory(inv);
    }

}

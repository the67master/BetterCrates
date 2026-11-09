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
    public static final String MAIN="§8BetterCrates §7» §6Panel", CRATES="§8BetterCrates §7» §6Crates", KEYS="§8BetterCrates §7» §6Llaves";
    public static final String CRATE_PREFIX="§8Editar crate §7» §e", KEY_PREFIX="§8Editar llave §7» §e";
    public static final String REWARDS_PREFIX="§8Recompensas §7» §e";
    public static final String PREVIEW_PREFIX="§8Premios disponibles §7» §e";
    public static final String CRATE_BLOCK_PREFIX="§8Bloque crate §7» §e", KEY_ITEM_PREFIX="§8Objeto llave §7» §e";
    public static final String HOLOGRAM_PREFIX="§8Holograma crate §7» §e";
    public static final String MESSAGES_PREFIX="§8Mensajes crate §7» §e";
    public CrateMenu(BetterCrates plugin){this.plugin=plugin;}

    private ItemStack item(Material material,String name,String... lore){
        ItemStack stack=new ItemStack(material); ItemMeta meta=stack.getItemMeta(); meta.setDisplayName(plugin.color(name));
        List<String> lines=new ArrayList<String>(); for(String line:lore) lines.add(plugin.color(line)); meta.setLore(lines); stack.setItemMeta(meta); return stack;
    }
    private void fill(Inventory inv){ ItemStack pane=new ItemStack(Material.STAINED_GLASS_PANE,1,(short)7); ItemMeta paneMeta=pane.getItemMeta(); paneMeta.setDisplayName(plugin.color("&8 ")); pane.setItemMeta(paneMeta); for(int i=0;i<inv.getSize();i++) if(inv.getItem(i)==null) inv.setItem(i,pane); }
    public void openMain(Player p){
        Inventory inv=Bukkit.createInventory(null,27,MAIN);
        inv.setItem(10,item(Material.EMERALD,"&a&lCrear crate","&7Crear una nueva crate"));
        inv.setItem(12,item(Material.TRIPWIRE_HOOK,"&6&lCrear llave","&7Crear una llave"));
        inv.setItem(14,item(Material.CHEST,"&e&lConfigurar crates"));
        inv.setItem(16,item(Material.TRIPWIRE_HOOK,"&e&lConfigurar llaves")); fill(inv); p.openInventory(inv);
    }
    public void openCrates(Player p){
        Inventory inv=Bukkit.createInventory(null,54,CRATES); int slot=0; YamlConfiguration c=plugin.getCrateManager().getConfiguration();
        for(String id:plugin.getCrateManager().getCrateIds()){if(slot>=45)break; String path="crates."+id+".settings.";
            inv.setItem(slot++,item(Material.CHEST,c.getString(path+"display-name","&e"+id),"&7ID: &f"+id,"&7Estado: "+(c.getBoolean(path+"enabled",true)?"&aActivada":"&cDesactivada"),"&eClick para editar"));}
        inv.setItem(49,item(Material.ARROW,"&cVolver")); fill(inv); p.openInventory(inv);
    }
    public void openKeys(Player p){
        Inventory inv=Bukkit.createInventory(null,54,KEYS); int slot=0; YamlConfiguration c=plugin.getCrateManager().getConfiguration();
        for(String id:plugin.getCrateManager().getKeyIds()){if(slot>=45)break; inv.setItem(slot++,item(Material.TRIPWIRE_HOOK,c.getString("keys."+id+".display-name","&6"+id),"&7ID: &f"+id,"&eClick para editar"));}
        inv.setItem(49,item(Material.ARROW,"&cVolver")); fill(inv); p.openInventory(inv);
    }
    public void openCrateSettings(Player p,String id){
        YamlConfiguration c=plugin.getCrateManager().getConfiguration(); String path="crates."+id+".settings.";
        if(!plugin.getCrateManager().crateExists(id)){openCrates(p);return;}
        Inventory inv=Bukkit.createInventory(null,45,CRATE_PREFIX+id);
        inv.setItem(10,item(Material.REDSTONE_TORCH_ON,"&aActivar / desactivar","&7Estado: "+(c.getBoolean(path+"enabled",true)?"&aActivada":"&cDesactivada")));
        inv.setItem(11,item(Material.NAME_TAG,"&eNombre y color","&7Actual: "+c.getString(path+"display-name","&e"+id),"&eClick y escribe el nombre con colores &"));
        inv.setItem(12,item(Material.TRIPWIRE_HOOK,"&6Asignar llave","&7Actual: &f"+c.getString("crates."+id+".key-id",id)));
        inv.setItem(14,item(Material.IRON_DOOR,"&dPermiso para abrir","&7Requerido: "+(c.getBoolean(path+"permission.enabled",false)?"&aSí":"&cNo")));
        inv.setItem(15,item(Material.PAPER,"&eNodo de permiso","&7"+c.getString(path+"permission.node","")));
        inv.setItem(16,item(Material.CHEST,"&bEditar bloque de crate","&7Material: &f"+c.getString("crates."+id+".block.material","CHEST"),"&eClick para abrir editor"));
        inv.setItem(19,item(Material.CHEST,"&6Editar recompensas","&7Configura los premios y sus probabilidades","&eClick para abrir"));
        inv.setItem(20,item(Material.ENCHANTED_BOOK,"&dBrillo encantado","&7"+(c.getBoolean("crates."+id+".block.glint",false)?"&aActivado":"&cDesactivado")));
        inv.setItem(21,item(Material.NAME_TAG,"&bEditar holograma","&7Estado: "+(c.getBoolean("crates."+id+".hologram.enabled",true)?"&aActivado":"&cDesactivado"),"&eConfigura las 3 líneas sobre la crate"));
        inv.setItem(23,item(Material.PAPER,"&bOpening messages","&7Customize private and global messages"));
        inv.setItem(40,item(Material.ARROW,"&cVolver")); fill(inv); p.openInventory(inv);
    }
    public void openHologramEditor(Player p,String id){
        YamlConfiguration c=plugin.getCrateManager().getConfiguration();
        String base="crates."+id+".hologram.";
        if(!plugin.getCrateManager().crateExists(id)){openCrates(p);return;}
        List<String> lines=c.getStringList(base+"lines");
        while(lines.size()<3)lines.add("");
        String crateName=c.getString("crates."+id+".settings.display-name","&e"+id);
        Inventory inv=Bukkit.createInventory(null,45,HOLOGRAM_PREFIX+id);
        inv.setItem(10,item(Material.REDSTONE_TORCH_ON,"&aHolograma activado / desactivado","&7Estado: "+(c.getBoolean(base+"enabled",true)?"&aActivado":"&cDesactivado")));
        inv.setItem(12,item(Material.NAME_TAG,"&eLínea 1","&7Actual: "+lines.get(0).replace("%crate_name%",crateName),"&eClick para editar"));
        inv.setItem(13,item(Material.PAPER,"&eLínea 2","&7Actual: "+lines.get(1),"&eClick para editar"));
        inv.setItem(14,item(Material.PAPER,"&eLínea 3","&7Actual: "+lines.get(2),"&eClick para editar"));
        inv.setItem(16,item(Material.BOOK,"&bRestaurar valores por defecto","&7"+crateName,"&7&fRight-Click to open!","&7&fLeft-Click to Preview!"));
        inv.setItem(40,item(Material.ARROW,"&cVolver"));
        fill(inv);p.openInventory(inv);
    }
    public void openMessagesEditor(Player p,String id){
        YamlConfiguration c=plugin.getCrateManager().getConfiguration();
        String base="crates."+id+".messages.";
        if(!plugin.getCrateManager().crateExists(id)){openCrates(p);return;}
        Inventory inv=Bukkit.createInventory(null,45,MESSAGES_PREFIX+id);
        inv.setItem(10,item(Material.REDSTONE_TORCH_ON,"&aTodos los mensajes","&7Estado: "+(c.getBoolean(base+"enabled",true)?"&aActivados":"&cDesactivados"),"&7Desactiva esto para silenciar todos"));
        inv.setItem(12,item(Material.NAME_TAG,"&eMensaje privado al abrir","&7Estado: "+(c.getBoolean(base+"player.enabled",false)?"&aActivado":"&cDesactivado"),"&7Actual: "+c.getString(base+"player.message","")));
        inv.setItem(13,item(Material.PAPER,"&eEditar mensaje privado","&7Click para escribir el mensaje"));
        inv.setItem(14,item(Material.BOOK,"&6Mensaje global al abrir","&7Estado: "+(c.getBoolean(base+"global.enabled",false)?"&aActivado":"&cDesactivado"),"&7Actual: "+c.getString(base+"global.message","")));
        inv.setItem(15,item(Material.PAPER,"&6Editar mensaje global","&7Click para escribir el mensaje"));
        inv.setItem(16,item(Material.EMERALD,"&bMensaje de recompensa","&7Estado: "+(c.getBoolean(base+"reward.enabled",true)?"&aActivado":"&cDesactivado"),"&7Actual: "+c.getString(base+"reward.message","")));
        inv.setItem(17,item(Material.PAPER,"&bEditar mensaje de recompensa","&7Click para escribir el mensaje"));
        inv.setItem(40,item(Material.ARROW,"&cVolver"));
        fill(inv);p.openInventory(inv);
    }
    public void openKeySettings(Player p,String id){
        YamlConfiguration c=plugin.getCrateManager().getConfiguration(); String path="keys."+id+".";
        if(!plugin.getCrateManager().keyExists(id)){openKeys(p);return;}
        Inventory inv=Bukkit.createInventory(null,45,KEY_PREFIX+id);
        inv.setItem(10,item(Material.NAME_TAG,"&eNombre y color","&7Actual: "+c.getString(path+"display-name","&6"+id),"&eClick y escribe el nombre con colores &"));
        inv.setItem(12,item(Material.CHEST,"&bEditar objeto de llave","&7Material: &f"+c.getString(path+"material","TRIPWIRE_HOOK"),"&eClick para abrir editor"));
        inv.setItem(14,item(Material.IRON_DOOR,"&dPermiso para usar llave","&7Requerido: "+(c.getBoolean(path+"permission.enabled",false)?"&aSí":"&cNo")));
        inv.setItem(15,item(Material.PAPER,"&eNodo de permiso","&7"+c.getString(path+"permission.node","")));
        inv.setItem(16,item(Material.ENCHANTED_BOOK,"&dBrillo encantado","&7"+(c.getBoolean(path+"glint",false)?"&aActivado":"&cDesactivado")));
        inv.setItem(40,item(Material.ARROW,"&cVolver")); fill(inv); p.openInventory(inv);
    }
    public void openCrateBlockEditor(Player p,String id){
        Inventory inv=Bukkit.createInventory(null,27,CRATE_BLOCK_PREFIX+id); YamlConfiguration c=plugin.getCrateManager().getConfiguration();
        String m=c.getString("crates."+id+".block.material","CHEST"); Material mat=Material.matchMaterial(m); if(mat==null)mat=Material.CHEST;
        inv.setItem(11,item(Material.PAPER,"&eEscribir material","&7Escribe el nombre Bukkit del bloque"));
        inv.setItem(13,item(mat,"&aBloque seleccionado","&7Actual: &f"+m,"&eArrastra aquí un bloque de tu inventario"));
        inv.setItem(15,item(Material.EMERALD,"&aGuardar bloque","&7Guarda el material seleccionado"));
        inv.setItem(22,item(Material.ARROW,"&cVolver")); fill(inv); p.openInventory(inv);
    }
    public void openKeyItemEditor(Player p,String id){
        Inventory inv=Bukkit.createInventory(null,27,KEY_ITEM_PREFIX+id); YamlConfiguration c=plugin.getCrateManager().getConfiguration();
        String m=c.getString("keys."+id+".material","TRIPWIRE_HOOK"); Material mat=Material.matchMaterial(m); if(mat==null)mat=Material.TRIPWIRE_HOOK;
        inv.setItem(11,item(Material.PAPER,"&eEscribir material","&7Escribe el nombre Bukkit del objeto"));
        inv.setItem(13,item(mat,"&aObjeto seleccionado","&7Actual: &f"+m,"&eArrastra aquí un objeto de tu inventario"));
        inv.setItem(15,item(Material.EMERALD,"&aGuardar objeto","&7Guarda el material seleccionado"));
        inv.setItem(22,item(Material.ARROW,"&cVolver")); fill(inv); p.openInventory(inv);
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
        ItemStack pane=new ItemStack(Material.STAINED_GLASS_PANE,1,(short)7);
        ItemMeta meta=pane.getItemMeta();meta.setDisplayName(plugin.color("&8 "));pane.setItemMeta(meta);
        int[] sides={9,18,27,36,17,26,35,44};
        for(int slot:sides)inv.setItem(slot,pane);
    }
    public void openRewards(Player p,String id,int page){
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
            lore.add(plugin.color("&7Click izquierdo: cambiar probabilidad"));
            lore.add(plugin.color("&7Click derecho: eliminar premio"));
            meta.setLore(lore);display.setItemMeta(meta);inv.setItem(slots[i],display);
        }
        decorateSides(inv);
        inv.setItem(45,item(Material.ARROW,"&ePágina anterior"));
        inv.setItem(47,item(Material.BOOK,"&6Reward editor","&7Drag an item from your inventory","&7to an empty center slot","&7Chances auto-balance to 100%"));
        inv.setItem(49,item(Material.ARROW,"&cEscape","&7Volver a la configuración de la crate"));
        inv.setItem(51,item(Material.PAPER,"&ePágina &f"+(page+1),"&7Premios: &f"+rewards.size()));
        inv.setItem(53,item(Material.ARROW,"&ePágina siguiente"));
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
            lore.add(plugin.color("&8 "));lore.add(plugin.color("&eProbabilidad aproximada: &f"+String.format(java.util.Locale.US,"%.2f",actual)+"%"));
            meta.setLore(lore);display.setItemMeta(meta);inv.setItem(slots[i],display);shown++;
        }
        decorateSides(inv);
        inv.setItem(45,item(Material.ARROW,"&ePágina anterior"));
        inv.setItem(49,item(Material.ARROW,"&cEscape","&7Cerrar vista previa"));
        inv.setItem(51,item(Material.PAPER,"&6Premios de &e"+id,"&7Objetos disponibles: &f"+rewards.size()));
        inv.setItem(53,item(Material.ARROW,"&ePágina siguiente"));
        if(shown==0)inv.setItem(22,item(Material.BARRIER,"&cEsta crate no tiene premios configurados"));
        p.openInventory(inv);
    }

}

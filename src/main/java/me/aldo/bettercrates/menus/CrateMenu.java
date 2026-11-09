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
    public static final String CRATE_BLOCK_PREFIX="§8Bloque crate §7» §e", KEY_ITEM_PREFIX="§8Objeto llave §7» §e";
    public CrateMenu(BetterCrates plugin){this.plugin=plugin;}

    private ItemStack item(Material material,String name,String... lore){
        ItemStack stack=new ItemStack(material); ItemMeta meta=stack.getItemMeta(); meta.setDisplayName(plugin.color(name));
        List<String> lines=new ArrayList<String>(); for(String line:lore) lines.add(plugin.color(line)); meta.setLore(lines); stack.setItemMeta(meta); return stack;
    }
    private void fill(Inventory inv){ ItemStack pane=item(Material.STAINED_GLASS_PANE,"&8 "); for(int i=0;i<inv.getSize();i++) if(inv.getItem(i)==null) inv.setItem(i,pane); }
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
        Inventory inv=Bukkit.createInventory(null,27,CRATE_PREFIX+id);
        inv.setItem(10,item(Material.REDSTONE_TORCH_ON,"&aActivar / desactivar","&7Estado: "+(c.getBoolean(path+"enabled",true)?"&aActivada":"&cDesactivada")));
        inv.setItem(11,item(Material.NAME_TAG,"&eNombre y color","&7Actual: "+c.getString(path+"display-name","&e"+id),"&eClick y escribe el nombre con colores &"));
        inv.setItem(12,item(Material.TRIPWIRE_HOOK,"&6Asignar llave","&7Actual: &f"+c.getString("crates."+id+".key-id",id)));
        inv.setItem(14,item(Material.IRON_DOOR,"&dPermiso para abrir","&7Requerido: "+(c.getBoolean(path+"permission.enabled",false)?"&aSí":"&cNo")));
        inv.setItem(15,item(Material.PAPER,"&eNodo de permiso","&7"+c.getString(path+"permission.node","")));
        inv.setItem(16,item(Material.CHEST,"&bEditar bloque de crate","&7Material: &f"+c.getString("crates."+id+".block.material","CHEST"),"&eClick para abrir editor"));
        inv.setItem(18,item(Material.ENCHANTED_BOOK,"&dBrillo encantado","&7"+(c.getBoolean("crates."+id+".block.glint",false)?"&aActivado":"&cDesactivado")));
        inv.setItem(22,item(Material.ARROW,"&cVolver")); fill(inv); p.openInventory(inv);
    }
    public void openKeySettings(Player p,String id){
        YamlConfiguration c=plugin.getCrateManager().getConfiguration(); String path="keys."+id+".";
        if(!plugin.getCrateManager().keyExists(id)){openKeys(p);return;}
        Inventory inv=Bukkit.createInventory(null,27,KEY_PREFIX+id);
        inv.setItem(10,item(Material.NAME_TAG,"&eNombre y color","&7Actual: "+c.getString(path+"display-name","&6"+id),"&eClick y escribe el nombre con colores &"));
        inv.setItem(12,item(Material.CHEST,"&bEditar objeto de llave","&7Material: &f"+c.getString(path+"material","TRIPWIRE_HOOK"),"&eClick para abrir editor"));
        inv.setItem(14,item(Material.IRON_DOOR,"&dPermiso para usar llave","&7Requerido: "+(c.getBoolean(path+"permission.enabled",false)?"&aSí":"&cNo")));
        inv.setItem(15,item(Material.PAPER,"&eNodo de permiso","&7"+c.getString(path+"permission.node","")));
        inv.setItem(16,item(Material.ENCHANTED_BOOK,"&dBrillo encantado","&7"+(c.getBoolean(path+"glint",false)?"&aActivado":"&cDesactivado")));
        inv.setItem(22,item(Material.ARROW,"&cVolver")); fill(inv); p.openInventory(inv);
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
}

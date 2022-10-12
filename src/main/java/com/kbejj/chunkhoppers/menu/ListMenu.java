package com.kbejj.chunkhoppers.menu;

import com.kbejj.chunkhoppers.base.ChunkHopper;
import com.kbejj.chunkhoppers.base.ChunkHopperManager;
import com.kbejj.chunkhoppers.utils.*;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

public class ListMenu extends Menu {
    public ListMenu(Player player, ChunkHopper chunkHopper) {
        super(player, chunkHopper);
    }

    OfflinePlayer offlinePlayer;

    @Override
    public String title() {
        return offlinePlayer == null ? "&8Owner List" : "&8" + offlinePlayer.getName() + "'s Chunk Hoppers";
    }

    @Override
    public int size() {
        return 54;
    }

    @Override
    public void setContents() {
        List<ChunkHopper> chunkHoppers = offlinePlayer == null ? new ArrayList<>() : ChunkHopperManager.getPlayerChunkHoppers(offlinePlayer.getUniqueId().toString());
        List<String> uuids = ChunkHopperManager.getUuidKeys();
        int size = offlinePlayer == null ? uuids.size() : chunkHoppers.size();
        if(size > 45){
            if(page > 0){
                inventory.setItem(45, new CustomItem().material(Material.ARROW).displayname("&b▶ &fPrevious Page").getItemStack());
            }
            if(page < size / 45){
                inventory.setItem(53, new CustomItem().material(Material.ARROW).displayname("&b▶ &fNext Page").getItemStack());
            }
        }
        for(int i = page * 45; i < 45 + (page  * 45); i++){
            if(i >= size) break;
            if(offlinePlayer == null){
                ItemStack skull = new CustomItem().material(Material.PLAYER_HEAD).displayname("&e" + Bukkit.getOfflinePlayer(UUID.fromString(uuids.get(i))).getName())
                        .lore("&7click to select!").persistentString("uuid", uuids.get(i)).skull(uuids.get(i)).getItemStack();
                inventory.addItem(skull);
            }else{
                ChunkHopper chunkHopper = chunkHoppers.get(i);
                Location location = chunkHopper.getLocation();
                List<String> lore = new ArrayList<>(Arrays.asList(
                        "&6World: &7" + location.getWorld().getName(),
                        "&6X: &7" + location.getBlockX(),
                        "&6Y: &7" + location.getBlockY(),
                        "&6Z: &7" + location.getBlockZ()));
                if(ConfigValues.isAutoKill()){
                    lore.add("&f----------------------");
                    lore.add("&6Auto Kill: &7" + chunkHopper.isAutoKill());
                }
                if(ConfigValues.isAutoSell()){
                    if(!ConfigValues.isAutoKill()){
                        lore.add("&f----------------------");
                    }
                    lore.add("&6Auto Sell: &7" + chunkHopper.isAutoSell());
                    lore.add("&6Total Earnings: &7$" + StringUtil.getDecimalFormat(chunkHopper.getTotalEarnings()));
                    lore.add("&f----------------------");
                }else{
                    lore.add("&f----------------------");
                }
                lore.add("&7click to teleport!");
                ItemStack itemStack = new CustomItem().material(Material.HOPPER).glow().displayname(ConfigValues.getDisplayname()).lore(lore)
                        .persistentString("location", ItemSerializer.serializeLocation(location)).getItemStack();
                inventory.addItem(itemStack);
            }
        }
        if(offlinePlayer != null){
            inventory.setItem(49, new CustomItem().material(Material.OAK_SIGN).displayname("&eBack").lore("&7click to go back!").getItemStack());
        }
    }

    @Override
    public void handleClickListener(InventoryClickEvent e) {
        e.setCancelled(true);
        ItemStack itemStack = e.getCurrentItem();
        int slot = e.getSlot();
        if(itemStack == null){
            return;
        }
        if(slot < 45){
            if(offlinePlayer == null){
                String uuid = PersistentData.getPersistentValue(itemStack, "uuid");
                offlinePlayer = Bukkit.getOfflinePlayer(UUID.fromString(uuid));
            }else{
                page = 0;
                Location location = ItemSerializer.deserializeLocation(PersistentData.getPersistentValue(itemStack, "location").split(";"));
                player.closeInventory();
                player.teleport(location.clone().add(new Vector(.5, 1, .5)));
                player.getWorld().playSound(location, Sound.ENTITY_ENDERMAN_TELEPORT, 1, 1);
                return;
            }
            openInventory();
            return;
        }
        if(slot == 45){
            page--;
            openInventory();
            return;
        }
        if(slot == 49){
            page = 0;
            offlinePlayer = null;
            openInventory();
            return;
        }
        if(slot == 53){
            page++;
            openInventory();
        }
    }

    @Override
    public Inventory getInventory() {
        return inventory;
    }
}

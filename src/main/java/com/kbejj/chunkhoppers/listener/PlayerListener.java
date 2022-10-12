package com.kbejj.chunkhoppers.listener;

import com.kbejj.chunkhoppers.ChunkHoppers;
import com.kbejj.chunkhoppers.base.ChunkHopper;
import com.kbejj.chunkhoppers.base.ChunkHopperManager;
import com.kbejj.chunkhoppers.menu.ChunkHopperMenu;
import com.kbejj.chunkhoppers.menu.Menu;
import com.kbejj.chunkhoppers.utils.ChunkHopperUtil;
import com.kbejj.chunkhoppers.utils.ConfigValues;
import com.kbejj.chunkhoppers.utils.EffectUtil;
import com.kbejj.chunkhoppers.utils.StringUtil;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

public class PlayerListener implements Listener {

    private final ChunkHoppers plugin = ChunkHoppers.getInstance();

    @EventHandler
    public void onInteract(PlayerInteractEvent e){
        if(!e.getAction().name().endsWith("BLOCK")){
            return;
        }
        Player player = e.getPlayer();
        Block block = e.getClickedBlock();
        Location location = block.getLocation();
        if(!block.getType().equals(Material.HOPPER)){
            return;
        }
        Action action = e.getAction();
        if(ChunkHopperUtil.NotChunkHopper(location)){
            return;
        }
        if(!player.isSneaking()){
            return;
        }
        if(ChunkHopperUtil.NotOwner(location, player.getUniqueId().toString()) && !plugin.isBypassed(player)){
            if(player.hasPermission("chunkhoppers.bypass")){
                StringUtil.sendMessage(player, ConfigValues.getMessage("modify-message"));
                return;
            }
            StringUtil.sendMessage(player, ConfigValues.getMessage("not-owner-message"));
            return;
        }
        ChunkHopper chunkHopper = ChunkHopperManager.getChunkHopper(location);
        if(action == Action.RIGHT_CLICK_BLOCK){
            e.setCancelled(true);
            (new ChunkHopperMenu(player, chunkHopper)).openInventory();
            EffectUtil.playSound(location, Sound.BLOCK_ENDER_CHEST_OPEN);
        }else{
            if(e.getItem() == null){
                return;
            }
            ItemStack itemStack = e.getItem().clone();
            itemStack.setAmount(1);
            e.setCancelled(true);

            if(chunkHopper.getFilteredItems().contains(itemStack)){
                StringUtil.sendMessage(player, ConfigValues.getMessage("already-filtered-message"));
                return;
            }
            if(itemStack.hasItemMeta() && ConfigValues.isAllowedCustomItems()){
                chunkHopper.addFilteredItem(itemStack);
            }else{
                if(!ConfigValues.getAllowedItems().contains(itemStack)){
                    StringUtil.sendMessage(player, ConfigValues.getMessage("item-not-allowed-message"));
                    return;
                }
                chunkHopper.addFilteredItem(itemStack);
            }
            chunkHopper.reload();
            StringUtil.sendMessage(player, ConfigValues.getMessage("filtered-message"));
        }
    }

    @EventHandler
    public void playerClickInventory(InventoryClickEvent e){
        if(!(e.getView().getTopInventory().getHolder() instanceof Menu)){
            return;
        }
        if(e.getCurrentItem() == null){
            return;
        }
        if(e.getClickedInventory() == null){
            return;
        }
        if(e.getClickedInventory() instanceof PlayerInventory){
            e.setCancelled(true);
        }
        InventoryHolder holder = e.getClickedInventory().getHolder();
        if(holder instanceof Menu){
            ((Menu) holder).handleClickListener(e);
        }
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent e){
        Player player = e.getPlayer();
        String uuid = player.getUniqueId().toString();
        if(plugin.getConfig().contains(uuid)){
            double totalEarnings = plugin.getConfig().getDouble(uuid);
            plugin.getEconomy().depositPlayer(player, totalEarnings);
            StringUtil.sendMessage(player, ConfigValues.getMessage("force-withdraw-message")
                    .replace("%amount%", String.valueOf(totalEarnings)));
            plugin.getConfig().set(uuid, null);
            plugin.saveConfig();
        }
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent e){
        Player player = e.getPlayer();
        ChunkHoppers plugin = ChunkHoppers.getInstance();
        if(plugin.isBypassed(player)){
            plugin.removeBypassedUser(player);
        }
    }
}

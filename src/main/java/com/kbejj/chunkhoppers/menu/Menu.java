package com.kbejj.chunkhoppers.menu;

import com.kbejj.chunkhoppers.ChunkHoppers;
import com.kbejj.chunkhoppers.base.ChunkHopper;
import com.kbejj.chunkhoppers.utils.StringUtil;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;

public abstract class Menu implements InventoryHolder {

    protected ChunkHoppers plugin = ChunkHoppers.getInstance();
    protected Inventory inventory;
    protected Player player;
    protected ChunkHopper chunkHopper;
    protected int page;

    public Menu(Player player, ChunkHopper chunkHopper) {
        this.player = player;
        this.chunkHopper = chunkHopper;
    }

    public abstract String title();
    public abstract int size();
    public abstract void setContents();
    public abstract void handleClickListener(InventoryClickEvent e);
    public void openInventory(){
        inventory = Bukkit.createInventory(this, size(), StringUtil.translate(title()));
        setContents();
        player.openInventory(inventory);
    }
}

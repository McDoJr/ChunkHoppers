package com.kbejj.chunkhoppers.menu;

import com.kbejj.chunkhoppers.base.ChunkHopper;
import com.kbejj.chunkhoppers.utils.ConfigValues;
import com.kbejj.chunkhoppers.utils.CustomItem;
import com.kbejj.chunkhoppers.utils.DataUtil;
import com.kbejj.chunkhoppers.utils.StringUtil;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class ChunkHopperMenu extends Menu {
    public ChunkHopperMenu(Player player, ChunkHopper chunkHopper) {
        super(player, chunkHopper);
    }

    private boolean filtered = true;

    @Override
    public String title() {
        return "&8" + chunkHopper.getOfflinePlayer().getName() + "'s Filtered list";
    }

    @Override
    public int size() {
        return 54;
    }

    @Override
    public void setContents() {
        List<ItemStack> filteredItems = chunkHopper.getFilteredItems().stream().map(itemStack ->
                new CustomItem().item(itemStack.clone()).lore("&cClick to remove!").getItemStack()).collect(Collectors.toList());
        List<ItemStack> filteredMobs = chunkHopper.getFilteredMobs().stream().map(itemStack ->
                new CustomItem().item(itemStack.clone()).displayname("&6" + StringUtil.capitalize(itemStack.clone())).lore("&cClick to remove!").getItemStack()).collect(Collectors.toList());
        int size = filtered ? filteredItems.size() : filteredMobs.size();
        if(size > 45){
            if(page > 0){
                inventory.setItem(45, new CustomItem().material(Material.ARROW).displayname("&b▶ &fPrevious Page").getItemStack());
            }
            if(page < size / 45){
                inventory.setItem(53, new CustomItem().material(Material.ARROW).displayname("&b▶ &fNext Page").getItemStack());
            }
        }
        for(int i = page * 45; i < 45 + (page  * 45); i++){
            if(i >= size){
                break;
            }
            inventory.addItem(filtered ? filteredItems.get(i) : filteredMobs.get(i));
        }

        if(ConfigValues.isAutoSell() && plugin.getEconomy() != null){
            if(DataUtil.hasShopGuiPlus() || ConfigValues.isEssentialsWorth()){
                inventory.setItem(49, new CustomItem().material(Material.PLAYER_HEAD).skull(chunkHopper.getUuid())
                        .displayname("&fTotal Earnings: &e" + StringUtil.getDecimalFormat(chunkHopper.getTotalEarnings())).lore("&7click to withdraw!").getItemStack());
                if(player.hasPermission("chunkhoppers.autosell")){
                    inventory.setItem(51, new CustomItem().material(chunkHopper.isAutoSell() ? Material.LIME_WOOL : Material.RED_WOOL)
                            .displayname("&fAutoSell: &a" + chunkHopper.isAutoSell()).lore("&7click to toggle!").getItemStack());
                }
            }
        }

        if(ConfigValues.isAutoKill()){
            if(player.hasPermission("chunkhoppers.autokill")){
                inventory.setItem(46, new CustomItem().material(filtered ? Material.GRASS_BLOCK : Material.DRAGON_EGG)
                        .displayname(filtered ? "&aItems" : "&6Mobs").lore("&7click to toggle!").getItemStack());
                inventory.setItem(52, new CustomItem().material(chunkHopper.isAutoKill() ? Material.LIME_WOOL : Material.RED_WOOL)
                        .displayname("&fAutoKill: &a" + chunkHopper.isAutoKill()).lore("&7click to toggle!").getItemStack());
            }
        }
        List<String> lore = new ArrayList<>(Collections.singletonList("&7left-click (&6Items&7)"));
        if(ConfigValues.isAutoKill() && player.hasPermission("chunkhoppers.autokill")){
            lore.add("&7right-click (&aMobs&7)");
        }
        inventory.setItem(47, new CustomItem().material(Material.COMPASS)
                .displayname("&eSearch").lore(lore).getItemStack());
    }

    @Override
    public void handleClickListener(InventoryClickEvent e) {
        e.setCancelled(true);
        ItemStack itemStack = e.getCurrentItem();
        int slot = e.getSlot();
        ClickType clickType = e.getClick();
        if(itemStack == null || itemStack.getType().equals(Material.AIR)){
            return;
        }
        if(slot < 45){
            itemStack = new CustomItem().item(itemStack).removeLoreLastLineBy(1).getItemStack();
            if(filtered){
                chunkHopper.getFilteredItems().remove(itemStack);
            }else{
                itemStack = new ItemStack(itemStack.getType());
                chunkHopper.getFilteredMobs().remove(itemStack);
            }
            chunkHopper.updatePersistentValue();
            StringUtil.sendMessage(player, ConfigValues.getMessage("removed-filtered-message"));
            openInventory();
            return;
        }
        if(slot == 45){
            page--;
        }
        if(slot == 46 && ConfigValues.isAutoKill()){
            filtered = !filtered;
        }
        if(slot == 47){
            SearchMenu menu = new SearchMenu(player, chunkHopper);
            if(clickType.isLeftClick()){
                menu.setSearchType(SearchType.ITEMS);
            }
            if(clickType.isRightClick() && ConfigValues.isAutoKill() && player.hasPermission("chunkhoppers.autokill")){
                menu.setSearchType(SearchType.MOBS);
            }else{
                menu.setSearchType(SearchType.ITEMS);
            }
            menu.openInventory();
            return;
        }
        if(slot == 49 && chunkHopper.getTotalEarnings() > 0 && plugin.getEconomy() != null){
            plugin.getEconomy().depositPlayer(player, chunkHopper.getTotalEarnings());
            StringUtil.sendMessage(player, ConfigValues.getMessage("withdraw-message").replace("%amount%", String.valueOf(chunkHopper.getTotalEarnings())));
            chunkHopper.setTotalEarnings(0.0D);
            chunkHopper.updatePersistentValue();
            player.closeInventory();
            return;
        }
        if(slot == 51){
            chunkHopper.setAutoSell(!chunkHopper.isAutoSell());
            chunkHopper.saveConfiguration();
        }
        if(slot == 52){
            chunkHopper.setAutoKill(!chunkHopper.isAutoKill());
            chunkHopper.saveConfiguration();
        }
        if(slot == 53){
            page++;
        }
        openInventory();
    }

    @Override
    public Inventory getInventory() {
        return inventory;
    }

    public void setFiltered(boolean filtered){
        this.filtered = filtered;
    }
}

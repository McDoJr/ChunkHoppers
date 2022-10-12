package com.kbejj.chunkhoppers.utils;

import com.earth2me.essentials.Essentials;
import com.earth2me.essentials.IEssentials;
import com.earth2me.essentials.Worth;
import com.kbejj.chunkhoppers.ChunkHoppers;
import com.kbejj.chunkhoppers.base.ChunkHopper;
import com.kbejj.chunkhoppers.base.ChunkHopperManager;
import net.brcdev.shopgui.ShopGuiPlugin;
import net.brcdev.shopgui.ShopGuiPlusApi;
import net.brcdev.shopgui.shop.item.ShopItem;
import org.bukkit.*;
import org.bukkit.entity.*;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class ChunkHopperUtil {

    public static void collectRemainingGroundItems(Inventory inventory, Item item, ChunkHopper chunkHopper){
        Location location = chunkHopper.getLocation();
        for(int i=0; i<5; i++){
            ItemStack inventoryStack = inventory.getItem(i);
            ItemStack groundStack = item.getItemStack();
            int inventoryStackAmount = inventoryStack.getAmount();
            int groundStackAmount = groundStack.getAmount();
            int maxStackSize = inventoryStack.getMaxStackSize();
            if(inventoryStack.isSimilar(groundStack)){
                if(inventoryStackAmount + groundStackAmount <= maxStackSize){
                    inventoryStack.setAmount(inventoryStackAmount + groundStackAmount);
                    EffectUtil.playEffect(item.getLocation());
                    item.remove();
                    EffectUtil.spawnParticle(location.clone().add(new Vector(0.5, 1, 0.5)));
                    EffectUtil.playSound(location, Sound.ENTITY_ITEM_PICKUP);
                }else{
                    if(inventoryStackAmount < maxStackSize){
                        inventoryStack.setAmount(maxStackSize);
                        groundStack.setAmount(groundStackAmount - (maxStackSize - inventoryStackAmount));
                        EffectUtil.playEffect(item.getLocation());
                        item.remove();
                        location.getWorld().dropItemNaturally(item.getLocation(), groundStack);
                        EffectUtil.spawnParticle(location.clone().add(new Vector(0.5, 1, 0.5)));
                        EffectUtil.playSound(location, Sound.ENTITY_ITEM_PICKUP);
                    }
                }
            }
        }
    }

    public static List<Item> getGroundItems(Chunk chunk, List<ItemStack> filteredItems){
        List<Item> items =  Arrays.stream(chunk.getEntities()).filter(Entity::isOnGround).filter(entity -> !entity.isDead()).filter(entity -> entity instanceof Item)
                .map(entity -> (Item) entity).collect(Collectors.toList());
        if(!filteredItems.isEmpty()){
            items = items.stream().filter(item -> {
                ItemStack itemStack = item.getItemStack().clone();
                itemStack.setAmount(1);
                return filteredItems.contains(itemStack);
            }).collect(Collectors.toList());
        }
        return items;
    }

    public static void autoKillMobs(Chunk chunk, ChunkHopper chunkHopper){
        if(!ConfigValues.isAutoKill()){
            return;
        }
        if(!chunkHopper.isAutoKill()){
            return;
        }
        Arrays.stream(chunk.getEntities()).filter(entity -> entity instanceof LivingEntity)
                .filter(entity -> isKillable(entity, chunkHopper.getFilteredMobs())).map(entity -> (LivingEntity) entity)
                .forEach(livingEntity ->{
                    Location location = chunkHopper.getLocation().clone().add(.5, 1, .5);
                    livingEntity.teleport(location);
                    location.getWorld().spawnParticle(Particle.PORTAL, location, 100);
                    Entity killer = chunkHopper.getOfflinePlayer().isOnline() && ConfigValues.isPlayerAsKiller() ? chunkHopper.getOfflinePlayer().getPlayer() : null;
                    Bukkit.getScheduler().scheduleSyncDelayedTask(ChunkHoppers.getInstance(), ()-> livingEntity.damage(100, killer), 10);
                });
    }

    private static boolean isKillable(Entity entity, List<ItemStack> filteredMobs){
        for(ItemStack itemStack : filteredMobs){
            if(entity.getName().toLowerCase().startsWith("magma") && itemStack.getType().name().toLowerCase().startsWith("magma")){
                return true;
            }
            if(itemStack.getType().name().startsWith(entity.getName().toUpperCase())){
                return true;
            }
        }
        return false;
    }

    public static void sellItems(Inventory inventory, ChunkHopper chunkHopper){
        if(!ConfigValues.isAutoSell()){
            return;
        }
        if(!chunkHopper.isAutoSell()){
            return;
        }
        if(ChunkHoppers.getInstance().getEconomy() == null){
            return;
        }
        if(!DataUtil.hasShopGuiPlus()){
            if(ConfigValues.isEssentialsWorth()){
                Essentials essentials = ChunkHoppers.getInstance().getEssentials();
                setEssentialsItems(inventory, chunkHopper, essentials);
            }
            return;
        }

        ShopGuiPlugin shop = ShopGuiPlusApi.getPlugin();
        double totalSold = 0;

        for(ItemStack itemStack : getContents(inventory)){
            ShopItem shopItem = shop.getShopManager().findShopItemByItemStack(itemStack, true);
            if(shopItem != null){
                totalSold += shopItem.getSellPrice() * itemStack.getAmount();
                inventory.remove(itemStack);
            }
        }

        chunkHopper.setTotalEarnings(chunkHopper.getTotalEarnings() + totalSold);
    }

    public static void setEssentialsItems(Inventory inventory, ChunkHopper chunkHopper, Essentials essentials){
        Worth worth = essentials.getWorth();
        double totalSold = 0;
        for(ItemStack itemStack : getContents(inventory)){
            BigDecimal bigDecimal = worth.getPrice(essentials, itemStack);
            if(bigDecimal != null){
                totalSold += bigDecimal.doubleValue() * itemStack.getAmount();
                inventory.remove(itemStack);
            }
        }

        chunkHopper.setTotalEarnings(chunkHopper.getTotalEarnings() + totalSold);
    }

    private static List<ItemStack> getContents(Inventory inventory){
        return Arrays.stream(inventory.getContents()).filter(Objects::nonNull).collect(Collectors.toList());
    }

    public static boolean hasChunkHopper(Chunk chunk){
        if(ChunkHopperManager.getAllChunkHoppers().isEmpty()){
            return false;
        }
        for(Location location : ChunkHopperManager.getLocationKeys()){
            if(location.getChunk().equals(chunk)){
                return true;
            }
        }
        return false;
    }

    public static boolean NotOwner(Location location, String uuid){
        return !ChunkHopperManager.getChunkHopper(location).getUuid().equals(uuid);
    }

    public static boolean NotChunkHopper(Location location){
        return !ChunkHopperManager.getLocationKeys().contains(location);
    }

    public static ItemStack getChunkHopper(int amount){
        return new CustomItem().material(Material.HOPPER).amount(amount).displayname(ConfigValues.getDisplayname())
                .lore(ConfigValues.getLores()).glow().persistentString("chunkhopper", null).getItemStack();
    }

    public static int getTotalChunkHoppers(Player player){
        String uuid = player.getUniqueId().toString();
        if(!ChunkHopperManager.getUuidKeys().contains(uuid)){
            return 0;
        }
        return ChunkHopperManager.getPlayerChunkHoppers(uuid).size();
    }
}

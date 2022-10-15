package com.kbejj.chunkhoppers.base;

import com.kbejj.chunkhoppers.utils.ItemSerializer;
import com.kbejj.chunkhoppers.utils.PersistentData;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Block;
import org.bukkit.block.Hopper;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class ChunkHopperFile extends CustomConfiguration{

    private ChunkHopper chunkHopper;

    public ChunkHopperFile(ChunkHopper chunkHopper) {
        super(chunkHopper.getUuid(), "chunkhoppers");
        this.chunkHopper = chunkHopper;
    }

    public ChunkHopperFile(File file) {
        super(file);
    }

    public void save(boolean configSave){
        String locationString = ItemSerializer.serializeLocation(chunkHopper.getLocation());
        if(!configSave){
            chunkHopper.updatePersistentValue();
        }
        configuration.set(locationString + ".totalEarnings", chunkHopper.getTotalEarnings());
        configuration.set(locationString + ".autoSell", chunkHopper.isAutoSell());
        configuration.set(locationString + ".autoKill", chunkHopper.isAutoKill());
        saveConfiguration();

    }

    public void remove(){
        String locationString = ItemSerializer.serializeLocation(chunkHopper.getLocation());
        configuration.set(locationString, null);
        saveConfiguration();
        if(configuration.getKeys(false).isEmpty()){
            file.delete();
        }
    }

    public void loadAll(){
        for(String locationString : configuration.getKeys(false)){
            Location location = ItemSerializer.deserializeLocation(locationString.split(";"));
            Block block = location.getBlock();
            if(!block.getType().equals(Material.HOPPER)){
                continue;
            }
            Hopper hopper = (Hopper) block.getState();
            PersistentData.checkPersistentData(hopper, "filteredItems");
            PersistentData.checkPersistentData(hopper, "filteredMobs");
            String stringValue = hopper.getPersistentDataContainer().get(new NamespacedKey(plugin, "filteredItems"), PersistentDataType.STRING);
            String stringValueMobs = hopper.getPersistentDataContainer().get(new NamespacedKey(plugin, "filteredMobs"), PersistentDataType.STRING);
            List<ItemStack> filteredItems = stringValue.equalsIgnoreCase("NONE") ? new ArrayList<>() : ItemSerializer.deserializeItemStacks(stringValue);
            List<ItemStack> filteredMobs = stringValueMobs.equalsIgnoreCase("NONE") ? new ArrayList<>() : ItemSerializer.deserializeItemStacks(stringValueMobs);
            double totalEarnings = configuration.getDouble(locationString + ".totalEarnings");
            boolean autoSell = configuration.getBoolean(locationString + ".autoSell");
            boolean autoKill = configuration.getBoolean(locationString + ".autoKill");
            ChunkHopperManager.addChunkHopper(new ChunkHopper(filename, location, filteredItems, filteredMobs, totalEarnings, autoSell, autoKill));
        }
    }
}

package com.kbejj.chunkhoppers.base;

import com.kbejj.chunkhoppers.ChunkHoppers;
import com.kbejj.chunkhoppers.utils.ConfigValues;
import com.kbejj.chunkhoppers.utils.DataUtil;
import com.kbejj.chunkhoppers.utils.StringUtil;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;

import java.io.File;
import java.util.*;

public class ChunkHopperManager {
    private static final ChunkHoppers plugin = ChunkHoppers.getInstance();
    private static final Map<String, Map<Location, ChunkHopper>> chunkHoppers = new HashMap<>();

    public static void addChunkHopper(ChunkHopper chunkHopper){
        if(!chunkHoppers.containsKey(chunkHopper.getUuid())){
            chunkHoppers.put(chunkHopper.getUuid(), new HashMap<>());
        }
        chunkHoppers.get(chunkHopper.getUuid()).put(chunkHopper.getLocation(), chunkHopper);
        (new ChunkHopperFile(chunkHopper)).save(false);
    }

    public static void removeChunkHopper(Location location){
        ChunkHopper chunkHopper = getChunkHopper(location);
        if(chunkHopper.getTotalEarnings() > 0 && DataUtil.hasShopGuiPlus()){
            OfflinePlayer offlinePlayer = chunkHopper.getOfflinePlayer();
            if(offlinePlayer.isOnline()){
                plugin.getEconomy().depositPlayer(offlinePlayer, chunkHopper.getTotalEarnings());
                StringUtil.sendMessage(offlinePlayer.getPlayer(), ConfigValues.getMessage("force-withdraw-message").replace("%amount%", String.valueOf(chunkHopper.getTotalEarnings())));
            }else{
                plugin.getConfig().set(chunkHopper.getUuid(), chunkHopper.getTotalEarnings());
                plugin.saveConfig();
            }
        }
        (new ChunkHopperFile(chunkHopper)).remove();
        chunkHoppers.get(chunkHopper.getUuid()).remove(location);
    }

    public static List<ChunkHopper> getAllChunkHoppers(){
        List<ChunkHopper> chunkHopperList = new ArrayList<>();
        for(Map.Entry<String, Map<Location, ChunkHopper>> entry : chunkHoppers.entrySet()){
            chunkHopperList.addAll(entry.getValue().values());
        }
        return chunkHopperList;
    }

    public static void saveAllChunkHoppers(){
        getAllChunkHoppers().forEach(chunkHopper -> (new ChunkHopperFile(chunkHopper)).save(false));
    }

    public static void loadAllChunkHoppers(){
        File folder = new File(plugin.getDataFolder() + File.separator + "chunkhoppers");
        if(folder.exists()){
            Arrays.stream(folder.listFiles()).forEach(file -> (new ChunkHopperFile(file)).loadAll());
        }
    }

    public static boolean chunkHoppersHasBeenLoaded(){
        return !chunkHoppers.isEmpty();
    }

    public static ChunkHopper getChunkHopper(Location location){
        for(Map.Entry<String, Map<Location, ChunkHopper>> entry : chunkHoppers.entrySet()){
            if(entry.getValue().containsKey(location)){
                return entry.getValue().get(location);
            }
        }
        return null;
    }

    public static List<String> getUuidKeys(){
        return new ArrayList<>(chunkHoppers.keySet());
    }

    public static List<ChunkHopper> getPlayerChunkHoppers(String uuid){
        return new ArrayList<>(chunkHoppers.get(uuid).values());
    }

    public static List<Location> getLocationKeys(){
        List<Location> locations = new ArrayList<>();
        for(Map.Entry<String, Map<Location, ChunkHopper>> entry : chunkHoppers.entrySet()){
            locations.addAll(entry.getValue().keySet());
        }
        return locations;
    }
}

package com.kbejj.chunkhoppers.listener;

import com.kbejj.chunkhoppers.ChunkHoppers;
import org.bukkit.Bukkit;
import org.bukkit.event.Listener;

public class ListenerManager {


    public ListenerManager(){
        registerListeners(new BlockListener(), new PlayerListener(), new PreventionListener());
    }

    private void registerListeners(Listener...listeners){
        for(Listener listener : listeners){
            Bukkit.getPluginManager().registerEvents(listener, ChunkHoppers.getInstance());
        }
    }
}

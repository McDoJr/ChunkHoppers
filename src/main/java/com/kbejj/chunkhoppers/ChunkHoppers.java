package com.kbejj.chunkhoppers;

import com.earth2me.essentials.Essentials;
import com.kbejj.chunkhoppers.base.ChunkHopper;
import com.kbejj.chunkhoppers.base.ChunkHopperManager;
import com.kbejj.chunkhoppers.commands.BaseCommand;
import com.kbejj.chunkhoppers.commands.CommandManager;
import com.kbejj.chunkhoppers.listener.ListenerManager;
import com.kbejj.chunkhoppers.utils.ConfigValues;
import com.kbejj.chunkhoppers.utils.StringUtil;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public final class ChunkHoppers extends JavaPlugin {

    private static ChunkHoppers instance;
    private int taskID = 0;
    private Economy economy;
    private final List<UUID> bypassedUsers = new ArrayList<>();
    private Essentials essentials;

    @Override
    public void onEnable() {
        instance = this;
        getConfig().options().copyDefaults(true);
        saveDefaultConfig();
        if(!setupEconomy()){
            StringUtil.consoleMessage("&cDisabled due to no Economy found!");
            Bukkit.getPluginManager().disablePlugin(this);
            return;
        }else{
            StringUtil.consoleMessage("&a[ChunkHopper] Successfully hooked to Vault!");
        }
        setupEssentials();
        ConfigValues.loadConfigValues();
        this.getCommand("chunkhopper").setExecutor(new BaseCommand());
        this.getCommand("chunkhopper").setTabCompleter(new BaseCommand());
        new ListenerManager();
        new CommandManager();
        StringUtil.consoleMessage("&a[ChunkHopper] Loading chunk hoppers...");
        Bukkit.getScheduler().runTaskLater(instance, () ->{
            ChunkHopperManager.loadAllChunkHoppers();
            StringUtil.consoleMessage("&a[ChunkHopper] Chunk hoppers has been loaded!");
            collectItems();
        }, 20L);

    }

    @Override
    public void onDisable() {
        cancelTask();
        if(ChunkHopperManager.chunkHoppersHasBeenLoaded()){
            ChunkHopperManager.saveAllChunkHoppers();
        }
    }

    private void setupEssentials(){
        essentials = (Essentials) Bukkit.getPluginManager().getPlugin("Essentials");
    }

    public Essentials getEssentials() {
        return essentials;
    }

    public static ChunkHoppers getInstance(){ return instance; }

    public boolean isBypassed(Player player){
        return bypassedUsers.contains(player.getUniqueId());
    }

    public void toggleBypassedUser(Player player){
        if(bypassedUsers.contains(player.getUniqueId())){
            bypassedUsers.remove(player.getUniqueId());
        }else{
            bypassedUsers.add(player.getUniqueId());
        }
    }

    public void removeBypassedUser(Player player){
        bypassedUsers.removeIf(uuid -> player.getUniqueId().equals(uuid));
    }

    private boolean setupEconomy(){
        if(Bukkit.getPluginManager().getPlugin("Vault") == null){
            return false;
        }
        RegisteredServiceProvider<Economy> provider = getServer().getServicesManager().getRegistration(Economy.class);
        if(provider == null){
            return false;
        }
        economy = provider.getProvider();
        return economy != null;
    }

    public void collectItems(){
        taskID = Bukkit.getScheduler().scheduleSyncRepeatingTask(instance, ()->{
            ChunkHopperManager.getAllChunkHoppers().stream().iterator().forEachRemaining(ChunkHopper::collectGroundItems);
        }, 10, ConfigValues.getPickupDelay());
    }

    public void cancelTask(){
        Bukkit.getScheduler().cancelTask(taskID);
    }

    public Economy getEconomy(){
        return economy;
    }
}

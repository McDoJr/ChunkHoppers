package com.kbejj.chunkhoppers.base;

import com.kbejj.chunkhoppers.ChunkHoppers;
import com.kbejj.chunkhoppers.utils.StringUtil;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;

public class CustomConfiguration {

    protected final ChunkHoppers plugin = ChunkHoppers.getInstance();
    protected File file;
    protected FileConfiguration configuration;
    protected String filename;
    protected String folder;

    public CustomConfiguration(String filename, String folder) {
        this.filename = filename;
        this.folder = folder;
        file = new File(plugin.getDataFolder() + File.separator + folder, filename + ".yml");
        reloadConfiguration();
    }

    public CustomConfiguration(File file) {
        this.file = file;
        this.filename = file.getName().replace(".yml", "");
        reloadConfiguration();
    }

    public void reloadConfiguration(){
        if(!file.exists()){
            try{
                file.createNewFile();
            }catch (IOException e){
                StringUtil.consoleMessage("&cFailed to create " + filename + ".yml");
            }
        }
        configuration = YamlConfiguration.loadConfiguration(file);
    }

    protected void saveConfiguration(){
        try{
            configuration.save(file);
        }catch (IOException e){
            StringUtil.consoleMessage("&cFailed to save " + filename + ".yml");
        }
    }
}

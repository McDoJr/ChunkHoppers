package com.kbejj.chunkhoppers.commands;

import com.kbejj.chunkhoppers.ChunkHoppers;
import org.bukkit.command.CommandSender;

public abstract class Subcommand {
    protected final ChunkHoppers plugin = ChunkHoppers.getInstance();
    public void handleCommand(CommandSender sender, String[] args){
        executeCommand(sender, args);
    }

    public abstract void executeCommand(CommandSender sender, String[] args);

    public CommandInfo values(){
        return getClass().getAnnotation(CommandInfo.class);
    }
}

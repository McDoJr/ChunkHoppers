package com.kbejj.chunkhoppers.commands;

import com.kbejj.chunkhoppers.utils.ConfigValues;
import com.kbejj.chunkhoppers.utils.StringUtil;
import org.bukkit.command.CommandSender;

@CommandInfo(command = "reload", permission = "chunkhoppers.reload", syntax = "/chunkhopper reload")
public class ReloadCommand extends Subcommand{
    @Override
    public void executeCommand(CommandSender sender, String[] args) {
        plugin.reloadConfig();
        ConfigValues.loadConfigValues();
        plugin.cancelTask();
        plugin.collectItems();
        StringUtil.sendMessage(sender, ConfigValues.getMessage("reload-message"));
    }
}
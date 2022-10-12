package com.kbejj.chunkhoppers.commands;

import com.kbejj.chunkhoppers.utils.ChunkHopperUtil;
import com.kbejj.chunkhoppers.utils.ConfigValues;
import com.kbejj.chunkhoppers.utils.StringUtil;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

@CommandInfo(command = "give", permission = "chunkhoppers.give", length = 3, syntax = "/chunkhopper give <player> <amount>")
public class GiveCommand extends Subcommand {
    @Override
    public void executeCommand(CommandSender sender, String[] args) {
        Player player = Bukkit.getPlayerExact(args[1]);
        if(player == null){
            StringUtil.sendMessage(sender, ConfigValues.getMessage("invalid-player-message"));
            return;
        }
        if(StringUtil.NotNumeric(args[2])){
            StringUtil.sendMessage(sender, ConfigValues.getMessage("invalid-amount-message"));
            return;
        }
        player.getInventory().addItem(ChunkHopperUtil.getChunkHopper(Integer.parseInt(args[2])));
        StringUtil.sendMessage(player, ConfigValues.getMessage("received-message").replace("%amount%", args[2]));
        StringUtil.sendMessage(sender, ConfigValues.getMessage("give-message").replace("%amount%", args[2]).replace("%player%", player.getName()));
    }
}

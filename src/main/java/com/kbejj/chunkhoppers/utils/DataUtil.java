package com.kbejj.chunkhoppers.utils;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.permissions.PermissionAttachmentInfo;

import java.util.List;
import java.util.stream.Collectors;

public class DataUtil {

    public static int getPlayerPermissionChunkHopperLimit(Player player){
        if(player.hasPermission("chunkhoppers.limit.*") || player.hasPermission("*") || player.isOp()){
            return Integer.MAX_VALUE;
        }
        List<PermissionAttachmentInfo> attachment = player.getEffectivePermissions()
                .stream().filter(attachmentInfo -> attachmentInfo.getPermission().startsWith("chunkhoppers.limit.")).sorted().collect(Collectors.toList());
        if(attachment.isEmpty()){
            return 1;
        }
        String permission = attachment.get(attachment.size()-1).getPermission();
        return Integer.parseInt(permission.substring(permission.lastIndexOf(".") + 1));
    }

    public static boolean hasShopGuiPlus(){
        return Bukkit.getPluginManager().getPlugin("ShopGUIPlus") != null;
    }
}

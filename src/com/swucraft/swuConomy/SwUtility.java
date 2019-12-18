package com.swucraft.swuConomy;

import org.bukkit.Material;

public class SwUtility {
    public static boolean IsSign(Material mat){
        return (mat == Material.ACACIA_SIGN || mat == Material.ACACIA_WALL_SIGN ||
                mat == Material.OAK_SIGN || mat == Material.OAK_WALL_SIGN ||
                mat == Material.BIRCH_SIGN || mat == Material.BIRCH_WALL_SIGN ||
                mat == Material.SPRUCE_SIGN || mat == Material.SPRUCE_WALL_SIGN ||
                mat == Material.DARK_OAK_SIGN || mat == Material.DARK_OAK_WALL_SIGN ||
                mat == Material.JUNGLE_SIGN || mat == Material.JUNGLE_WALL_SIGN);
    }
}

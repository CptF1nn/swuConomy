package com.swucraft.swuConomy;

import org.bukkit.Material;
import org.bukkit.block.data.type.Sign;
import org.bukkit.block.data.type.WallSign;

public class SwUtility {
    public static boolean IsSign(Material mat){
        Class<?> clazz = mat.data;
        return Sign.class.equals(clazz) || WallSign.class.equals(clazz);
    }

    public static String currencyName;
}

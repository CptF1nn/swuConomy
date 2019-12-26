package com.swucraft.swuConomy;

import org.bukkit.Material;
import org.bukkit.block.data.type.Sign;
import org.bukkit.block.data.type.WallSign;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class SwUtility {
    public static boolean IsSign(Material mat){
        Class<?> clazz = mat.data;
        return Sign.class.equals(clazz) || WallSign.class.equals(clazz);
    }

    public static String currencyName;

    public static int currencyValue;

    public static boolean add(ItemStack[] stacks, Material type, int amount) {
        List<Runnable> updates = new ArrayList<>();
        for (int i = 0; i < stacks.length; ++i) {
            ItemStack stack = stacks[i];
            if (stack == null) {
                if (amount - type.getMaxStackSize() > 0) {
                    amount -= type.getMaxStackSize();
                    int index = i;
                    updates.add(() -> stacks[index] = new ItemStack(type, type.getMaxStackSize()));
                } else {
                    stacks[i] = new ItemStack(type, amount);
                    for (Runnable r : updates) r.run();
                    return true;
                }
            } else if (stack.getType() == type) {
                int stackAmount = stack.getAmount();
                int difference = type.getMaxStackSize() - stackAmount;
                if (difference <= amount) {
                    int index = i;
                    updates.add(() -> stacks[index].setAmount(type.getMaxStackSize()));
                    amount -= difference;
                } else {
                    stack.setAmount(stackAmount + amount);
                    for (Runnable r : updates) r.run();
                    return true;
                }
            }
        }
        return false;
    }

    public static boolean remove(ItemStack[] stacks, Material type, int amount) {
        List<Runnable> updates = new ArrayList<>();
        for (int i = stacks.length - 1; i >= 0; --i) {
            ItemStack stack = stacks[i];
            if (stack == null)
                continue;
            if (stack.getType() == type) {
                if (amount > stack.getAmount()) {
                    amount -= stack.getAmount();
                    int index = i;
                    updates.add(() -> stacks[index] = null);
                } else {
                    if (amount == stack.getAmount())
                        stacks[i] = null;
                    else
                        stack.setAmount(stack.getAmount() - amount);
                    for (Runnable r : updates) r.run();
                    return true;
                }
            }
        }
        return false;
    }
}

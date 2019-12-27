package com.swucraft.swuConomy;

import org.bukkit.Material;
import org.bukkit.block.*;
import org.bukkit.block.data.Directional;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.inventory.InventoryHolder;

public class RemovalListener implements Listener {
    DataHandler dHandler;

    public RemovalListener(DataHandler dHandler) {
        this.dHandler = dHandler;
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent e) {
        Block block = e.getBlock();
        Material mat = block.getType();
        if (!(mat == Material.CHEST || mat == Material.TRAPPED_CHEST || SwUtility.IsSign(mat)))
            return;
        OwnedBlock ownership = dHandler.getInformation(block);
        if (ownership != null) {
            String playerUUID = e.getPlayer().getUniqueId().toString();
            if (ownership.getUUID().equals(playerUUID) || e.getPlayer().hasPermission("swuConomy.canRemoveAll")) {
                dHandler.remove(ownership);
                if (SwUtility.IsSign(mat))
                    removeSign(block, ownership, playerUUID);
            } else
                e.setCancelled(true);
        }
    }

    private void removeSign(Block block, OwnedBlock ownership, String playerUUID) {
        Sign sign = (Sign) block.getState();
        dHandler.removeSign(new SerialSign(ownership, 0, sign.getLine(1)));
        if (!sign.getLine(0).toLowerCase().contains("buy"))
            return;
        BlockFace modifier = ((Directional) block).getFacing().getOppositeFace();
        Vector3 possibleChestLocation = ownership.getLocation().offset(modifier);
        BlockState chest = block.getWorld().getBlockAt(
                possibleChestLocation.x,
                possibleChestLocation.y,
                possibleChestLocation.z
        ).getState();
        if (chest instanceof Chest) {
            Chest actualChest = (Chest) chest;
            String worldName = ownership.getWorld();
            dHandler.remove(new OwnedBlock(worldName, possibleChestLocation, playerUUID));
            InventoryHolder holder = actualChest.getInventory().getHolder();
            if (holder instanceof DoubleChest) {
                DoubleChest doubleChest = (DoubleChest) holder;
                Chest right = (Chest) doubleChest.getRightSide();
                Chest left = (Chest) doubleChest.getLeftSide();
                dHandler.remove(new OwnedBlock(worldName, new Vector3(right.getLocation()), playerUUID));
                dHandler.remove(new OwnedBlock(worldName, new Vector3(left.getLocation()), playerUUID));
            }
        }
    }

    @EventHandler
    public void onExplosion(EntityExplodeEvent e) {
        String world = null;
        for (Block block : e.blockList()) {
            Material mat = block.getType();
            if (mat == Material.CHEST || mat == Material.TRAPPED_CHEST || SwUtility.IsSign(mat)) {
                if (world == null) world = block.getLocation().getWorld().getName();
                if (dHandler.isProtected(new Vector3(block.getLocation()), world)) {
                    e.setCancelled(true);
                    return;
                }
            }
        }
    }
}

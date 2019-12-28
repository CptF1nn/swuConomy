package com.swucraft.swuConomy;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.*;
import org.bukkit.block.data.Directional;
import org.bukkit.block.data.type.WallSign;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.inventory.InventoryHolder;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class PlacementListener implements org.bukkit.event.Listener {
    final DataHandler dHandler;

    public PlacementListener(DataHandler dHandler) {
        this.dHandler = dHandler;
    }

    @EventHandler
    public void onHopperPlace(BlockPlaceEvent e) {
        if (e.getItemInHand().getType() != Material.HOPPER)
            return;
        Location loc = e.getBlockPlaced().getLocation();
        if (dHandler.isProtected(new Vector3(loc).offset(BlockFace.UP), loc.getWorld().getName()))
            e.setCancelled(true);
    }

    @EventHandler
    public void onSignChange(SignChangeEvent e) {
        String[] lines = e.getLines();
        String command = lines[0].toLowerCase();
        if (command.contains("[withdraw]"))
            handleCurrencySignCreate(e, command, "&1", "Get: 1 Diamond", "Give: $1000");
        else if (command.contains("[deposit]"))
            handleCurrencySignCreate(e, command, "&2", "Get: $1000", "Give: 1 Diamond");
        else if (command.contains("[buy]"))
            handleBuySignCreate(e, lines, command);
    }

    private void handleCurrencySignCreate(SignChangeEvent e, String command, String color, String line1, String line2) {
        if (!e.getPlayer().hasPermission("swuConomy.makeBank")) {
            cancelWithError(e, "Error: Not high enough permissions.");
            return;
        }
        e.setLine(0, ChatColor.translateAlternateColorCodes('&', color + command));
        e.setLine(1, "");
        e.setLine(2, line1);
        e.setLine(3, line2);
        SerialSign sign = new SerialSign(new OwnedBlock(e.getBlock().getLocation(), e.getPlayer()), 1000, "ICE");
        dHandler.add(sign);
        dHandler.add(sign.getOwnedBlock());
    }

    private void handleBuySignCreate(SignChangeEvent e, String[] lines, String command) {
        if (!e.getPlayer().hasPermission("swuConomy.makeShop")) {
            cancelWithError(e, "Error: Not high enough permissions.");
            return;
        } else if (!lines[3].matches("\\d+") || lines[3].trim().equals("0")) {
            cancelWithError(e, "Error: The bottom line should be the cost of the item, as an integer.");
            return;
        }
        Block block = e.getBlock();
        if (block.getType().data != WallSign.class) {
            cancelWithError(e, "Error: The buy sign should be a wall sign.");
            return;
        }
        Player player = e.getPlayer();
        List<OwnedBlock> chests = protectAttachedChests(block, player);
        if (chests.isEmpty()) {
            cancelWithError(e, "Error: The buy sign should be attached to a chest.");
            return;
        }
        if (Material.matchMaterial(lines[1]) == null) {
            cancelWithError(e, "Error: The buy sign should specify an item type on the second line.");
            return;
        }
        int price = Integer.parseInt(lines[3].trim());
        int amount = lines[2].matches("\\d+") ? Integer.parseInt(lines[2].trim()) : 1;
        SerialSign sign = new SerialSign(block.getLocation(), player, price, amount, lines[1]);
        dHandler.initBuy(sign);
        if (areProtected(chests)) {
            cancelWithError(e, "Error: The chests are already part of a buy-sign.");
            return;
        }
        protectChests(chests);
        e.setLine(0, ChatColor.translateAlternateColorCodes('&', "&d" + command));
        e.setLine(2, Integer.toString(amount));
    }

    private List<OwnedBlock> protectAttachedChests(Block block, Player player) {
        List<OwnedBlock> list = new ArrayList<>();
        Directional sign = (Directional) block.getBlockData();
        BlockFace mod = sign.getFacing().getOppositeFace();
        Vector3 chestLocation = new Vector3(block.getLocation()).offset(mod);
        BlockState chest = block.getWorld().getBlockAt(chestLocation.x, chestLocation.y, chestLocation.z).getState();
        if (chest instanceof Chest) {
            Chest actualChest = (Chest) chest;
            String worldName = block.getWorld().getName();
            UUID uuid = player.getUniqueId();
            list.add(new OwnedBlock(worldName, chestLocation, uuid));
            InventoryHolder holder = actualChest.getInventory().getHolder();
            if (holder instanceof DoubleChest) {
                DoubleChest doubleChest = (DoubleChest) holder;
                Chest right = (Chest) doubleChest.getRightSide();
                Chest left = (Chest) doubleChest.getLeftSide();
                Vector3 rightPos = new Vector3(right.getLocation());
                if (rightPos.equals(list.get(0).location)) {
                    list.add(new OwnedBlock(worldName, new Vector3(left.getLocation()), uuid));
                } else {
                    list.add(new OwnedBlock(worldName, rightPos, uuid));
                }
            }
        }
        return list;
    }

    private boolean areProtected(List<OwnedBlock> chests) {
        for (OwnedBlock chest : chests) {
            if (dHandler.isProtected(chest))
                return true;
        }
        return false;
    }

    private void protectChests(List<OwnedBlock> chests) {
        for (OwnedBlock chest : chests) {
            dHandler.add(chest);
        }
    }

    private void cancelWithError(SignChangeEvent e, String message) {
        e.setCancelled(true);
        e.getPlayer().sendRawMessage(ChatColor.translateAlternateColorCodes('&', "&4" + message));
    }
}

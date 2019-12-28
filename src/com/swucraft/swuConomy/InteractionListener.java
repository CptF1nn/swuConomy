package com.swucraft.swuConomy;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.*;
import org.bukkit.block.data.Directional;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;

import java.util.UUID;

import static org.bukkit.event.inventory.InventoryType.CHEST;

public class InteractionListener implements Listener {
    final DataHandler dHandler;

    public InteractionListener(DataHandler dHandler) {
        this.dHandler = dHandler;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        dHandler.UserJoined(player);
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (!event.hasBlock())
            return;
        if (event.getAction() == Action.RIGHT_CLICK_BLOCK) {
            Block block = event.getClickedBlock();
            if (block == null)
                return;
            Material mat = block.getType();
            if (SwUtility.IsSign(mat)) {
                Sign sign = (Sign)block.getState();
                Player player = event.getPlayer();
                String line = sign.getLine(0).toLowerCase();
                if (line.contains("[withdraw]")) {
                    if (!player.hasPermission("swuConomy.useBank"))
                        return;
                    Withdraw(player);
                } else if (line.contains("[deposit]")) {
                    if (!player.hasPermission("swuConomy.useBank"))
                        return;
                    Deposit(player);
                } else if (line.contains("[buy]")) {
                    if (!player.hasPermission("swuConomy.useShop"))
                        return;
                    Buy(player, sign);
                }
            }
        }
    }

    @EventHandler
    public void onChestOpen(InventoryOpenEvent e) {
        if (e.getInventory().getType() != CHEST)
            return;
        Location loc = e.getInventory().getLocation();
        if (loc == null)
            return;
        OwnedBlock ownership = dHandler.getInformation(loc);
        if (ownership == null)
            return;
        UUID player = e.getPlayer().getUniqueId();
        if (e.getPlayer().hasPermission("swuConomy.canTrespass"))
            return;
        if (!ownership.getUUID().equals(player)) {
            e.setCancelled(true);
        }
    }

    private void Buy(Player player, Sign sign) {
        String world = sign.getWorld().getName();
        Vector3 location = new Vector3(sign.getLocation());
        SerialSign serialSign = dHandler.getSign(world, location);
        UUID owner = serialSign.getOwnedBlock().getUUID();
        UUID buyer = player.getUniqueId();
        if (!dHandler.hasEnough(buyer, serialSign.getPrice())) {
            String message = "Error: Not enough money.";
            player.sendRawMessage(ChatColor.translateAlternateColorCodes('&', "&4" + message));
            return;
        }
        if (owner.equals(buyer))
            return;
        BlockFace mod = ((Directional) sign.getBlockData()).getFacing().getOppositeFace();
        Vector3 chestLocation = location.offset(mod);
        BlockState chest = sign.getWorld().getBlockAt(chestLocation.x, chestLocation.y, chestLocation.z).getState();
        if (chest instanceof Chest) {
            Chest actualChest = (Chest) chest;
            InventoryHolder holder = actualChest.getInventory().getHolder();
            if (!holder.getInventory().containsAtLeast(new ItemStack(serialSign.getType()), serialSign.getAmount())) {
                String message = "Error: Not enough stock.";
                player.sendRawMessage(ChatColor.translateAlternateColorCodes('&', "&4" + message));
                return;
            }
            ItemStack[] playerStacks = player.getInventory().getStorageContents();
            boolean wasAdded = SwUtility.add(playerStacks, serialSign.getType(), serialSign.getAmount());
            if (!wasAdded) {
                String message = "Error: Not enough room for items in your inventory.";
                player.sendRawMessage(ChatColor.translateAlternateColorCodes('&', "&4" + message));
                return;
            }
            ItemStack[] stacks = holder.getInventory().getStorageContents();
            boolean wasRemoved = SwUtility.remove(stacks, serialSign.getType(), serialSign.getAmount());
            if (!wasRemoved) {
                String message = "Error: Something went wrong in accessing the storage.";
                player.sendRawMessage(ChatColor.translateAlternateColorCodes('&', "&4" + message));
                return;
            }
            player.getInventory().setStorageContents(playerStacks);
            holder.getInventory().setStorageContents(stacks);
        }
        dHandler.transfer(buyer, owner, serialSign.getPrice());
    }

    private void Deposit(Player player) {
        int i = 0;
        for(ItemStack stack : player.getInventory().getContents()) {
            if (stack != null) {
                if (stack.getType() == Material.DIAMOND) {
                    if (dHandler.Deposit(player)) {
                        if (stack.getAmount() == 1)
                            stack = null;
                        else
                            stack.setAmount(stack.getAmount() - 1);
                        player.getInventory().setItem(i, stack);
                    }
                    return;
                }
            }
            i++;
        }
    }

    private void Withdraw(Player player) {
        int i = 0;
        for(ItemStack stack : player.getInventory().getContents()) {
            if (stack == null || (stack.getType() == Material.DIAMOND && stack.getAmount() != 64)) {
                if (dHandler.Withdraw(player)) {
                    if (stack == null) {
                        stack = new ItemStack(Material.DIAMOND);
                    } else {
                        stack.setAmount(stack.getAmount()+1);
                    }
                    player.getInventory().setItem(i,stack);
                } else {
                    player.sendMessage("§cYou do not have enough "+SwUtility.currencyName);
                }
                return;
            }
            i++;
        }
        player.sendMessage("§3You do not have inventory space for this!");
    }
}

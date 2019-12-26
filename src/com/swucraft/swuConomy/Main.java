package com.swucraft.swuConomy;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.*;
import org.bukkit.block.data.Directional;
import org.bukkit.block.data.type.WallSign;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.List;

import static org.bukkit.event.inventory.InventoryType.CHEST;

public final class Main extends JavaPlugin implements Listener, CommandExecutor {
    FileConfiguration config = getConfig();
    DataHandler dHandler = new DataHandler(getDataFolder());

    @Override
    public void onEnable() {
        saveDefaultConfig();
        config.addDefault("diamond.value",1000);
        config.addDefault("currency", "SwuCoins");
        config.options().copyDefaults(true);
        saveConfig();
        SwUtility.currencyName = config.getString("currency");
        SwUtility.currencyValue = config.getInt("diamond.value");
        getServer().getPluginManager().registerEvents(this,this);
        getCommand("balance").setExecutor(this);
        dHandler.load();
    }

    @Override
    public void onDisable() {
        dHandler.save();
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (!event.hasBlock())
            return;
        if (event.getAction() == Action.RIGHT_CLICK_BLOCK) {
            Block block = event.getClickedBlock();
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
        OwnedBlock ownership = dHandler.getInformation(loc);
        if (ownership == null)
            return;
        String player = e.getPlayer().getUniqueId().toString();
        if (e.getPlayer().hasPermission("swuConomy.canTrespass"))
            return;
        if (!ownership.getUUID().equals(player)) {
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void onHopperPlace(BlockPlaceEvent e) {
        if (e.getItemInHand().getType() != Material.HOPPER)
            return;
        Location loc = e.getBlockPlaced().getLocation();
        if (dHandler.isProtected(new Vector3(loc).offset(BlockFace.UP), loc.getWorld().getName()))
            e.setCancelled(true);
    }

    private void Buy(Player player, Sign sign) {
        String world = sign.getWorld().getName();
        Vector3 location = new Vector3(sign.getLocation());
        SerialSign serialSign = dHandler.getSign(world, location);
        String owner = serialSign.getOwnedBlock().getUUID();
        String buyer = player.getUniqueId().toString();
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

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        dHandler.UserJoined(player);
    }

    @Override public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args){
        Player player = (Player) sender;

        if (!player.hasPermission("swuConomy.getBalance")) {
            String message = "You are not allowed to use this command.";
            player.sendRawMessage(ChatColor.translateAlternateColorCodes('&', "&4" + message));
            return false;
        }

        player.sendMessage("You have " + dHandler.Balance(player) + " " +SwUtility.currencyName);

        return true;
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
            String uuid = player.getUniqueId().toString();
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

package com.swucraft.swuConomy;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.block.Sign;

public final class Main extends JavaPlugin implements Listener {
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
        getServer().getPluginManager().registerEvents(this,this);
    }

    @Override
    public void onDisable(){
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event){
        if (!event.hasBlock())
            return;
        if (event.getAction() == Action.RIGHT_CLICK_BLOCK){
            Block block = event.getClickedBlock();
            Material mat = block.getType();
            if (SwUtility.IsSign(mat)){
                Sign sign = (Sign)block.getState();
                Player player = event.getPlayer();
                if (!player.hasPermission("swuConomy.user")) return;
                String line = sign.getLine(0).toLowerCase();
                if (line.contains("[withdraw]")) {
                    Withdraw(player);
                    return;
                } else if (line.contains("[deposit]")){
                    Deposit(player);
                    return;
                } else if (line.contains("[buy]")){
                    Buy(player, sign);
                    return;
                }
            }
        }
    }

    private void Buy(Player player, Sign sign) {
        //Gem buy-sign's sammen med UUID
    }

    private void Deposit(Player player) {

        dHandler.Deposit(player);
    }

    private void Withdraw(Player player) {
        for(ItemStack stack : player.getInventory().getContents()){
            if (stack == null || (stack.getType() == Material.DIAMOND && stack.getAmount() != 64)){
                if (dHandler.Withdraw(player)){
                    if (stack.getType() == Material.DIAMOND)
                        stack.setAmount(stack.getAmount()+1);
                    return;
                } else {
                    player.sendMessage("§cYou do not have enough "+SwUtility.currencyName);
                }
            }
        }
        player.sendMessage("§3You do not have inventory space for this!");
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event){
        Player player = event.getPlayer();
        dHandler.UserJoinned(player);
    }

    @EventHandler
    public void onSignChange(SignChangeEvent e) {
        String[] lines = e.getLines();
        String command = lines[0].toLowerCase();
        if (command.contains("[withdraw]")) {
            if (!e.getPlayer().hasPermission("SwuCraft.admin")) {
                cancelWithError(e, "Error: Not high enough permissions.");
                return;
            }
            e.setLine(0, ChatColor.translateAlternateColorCodes('&', "&1" + command));
            e.setLine(1, "");
            e.setLine(2, "Get: 1 Diamond");
            e.setLine(3, "Give: $1000");
        } else if (command.contains("[deposit]")) {
            if (!e.getPlayer().hasPermission("SwuCraft.admin")) {
                cancelWithError(e, "Error: Not high enough permissions.");
                return;
            }
            e.setLine(0, ChatColor.translateAlternateColorCodes('&', "&2" + command));
            e.setLine(1, "");
            e.setLine(2, "Get: $1000");
            e.setLine(3, "Give: 1 Diamond");
        } else if (command.contains("[buy]")) {
            if (!e.getPlayer().hasPermission("SwuCraft.user")) {
                cancelWithError(e, "Error: Not high enough permissions.");
                return;
            } else if (!lines[3].matches("\\d+") && lines[3].trim().equals("0")) {
                cancelWithError(e, "Error: The bottom line should be the cost of the item, as an integer.");
                return;
            }
            int amount = Integer.parseInt(lines[3].trim());
            Player player = e.getPlayer();
            Location location = e.getBlock().getLocation();
            // TODO: Stuff
            e.setLine(0, ChatColor.translateAlternateColorCodes('&', "&d" + command));
        }
    }

    private void cancelWithError(SignChangeEvent e, String message) {
        e.setCancelled(true);
        e.getPlayer().sendRawMessage(ChatColor.translateAlternateColorCodes('&', "&4" + message));
    }
}

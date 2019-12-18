package com.swucraft.swuConomy;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.block.Sign;

public final class Main extends JavaPlugin implements Listener {
    FileConfiguration config = getConfig();
    @Override
    public void onEnable() {
        saveDefaultConfig();
        config.addDefault("testing", true);
        config.options().copyDefaults(true);
        saveConfig();

        getServer().getPluginManager().registerEvents(this,this);
    }

    @Override
    public void onDisable(){
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event){
        if (event.getAction() == Action.RIGHT_CLICK_BLOCK){
            Block block = event.getClickedBlock();
            Material mat = block.getType();
            if (mat == Material.ACACIA_SIGN || mat == Material.ACACIA_WALL_SIGN ||
                    mat == Material.OAK_SIGN || mat == Material.OAK_WALL_SIGN ||
                    mat == Material.BIRCH_SIGN || mat == Material.BIRCH_WALL_SIGN ||
                    mat == Material.SPRUCE_SIGN || mat == Material.SPRUCE_WALL_SIGN ||
                    mat == Material.DARK_OAK_SIGN || mat == Material.DARK_OAK_WALL_SIGN ||
                    mat == Material.JUNGLE_SIGN || mat == Material.JUNGLE_WALL_SIGN){
                event.getPlayer().sendMessage("Det her ryger i bad code");
            }
        }
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event){
        Player player = event.getPlayer();

    }
}

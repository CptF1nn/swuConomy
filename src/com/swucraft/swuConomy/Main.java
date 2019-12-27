package com.swucraft.swuConomy;

import org.bukkit.command.CommandExecutor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.event.Listener;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

public final class Main extends JavaPlugin implements Listener, CommandExecutor {
    FileConfiguration config = getConfig();
    DataHandler dHandler = new DataHandler(getDataFolder());

    @Override
    public void onEnable() {
        configure();
        registerEvents();
        getCommand("balance").setExecutor(new BalanceCommand(dHandler));
        dHandler.load();
    }

    private void configure() {
        saveDefaultConfig();
        config.addDefault("diamond.value",1000);
        config.addDefault("currency", "SwuCoins");
        config.options().copyDefaults(true);
        saveConfig();
        SwUtility.currencyName = config.getString("currency");
        SwUtility.currencyValue = config.getInt("diamond.value");
    }

    private void registerEvents() {
        PluginManager manager = getServer().getPluginManager();
        manager.registerEvents(new PlacementListener(dHandler), this);
        manager.registerEvents(new InteractionListener(dHandler), this);
        manager.registerEvents(new RemovalListener(dHandler), this);
    }

    @Override
    public void onDisable() {
        dHandler.save();
    }
}

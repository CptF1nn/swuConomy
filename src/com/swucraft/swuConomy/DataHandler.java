package com.swucraft.swuConomy;

import org.bukkit.Location;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class DataHandler {

    FileConfiguration config;
    File file;

    public DataHandler(File fileLocation){
        file = new File(fileLocation+File.separator+"data.yml");
        config = YamlConfiguration.loadConfiguration(file);
        if (!file.exists()) {
            try{
                file.createNewFile();
                config = YamlConfiguration.loadConfiguration(file);
            } catch(Exception e){
                e.printStackTrace();
            }
        }
    }

    public void UserJoined(Player player){
        if (config.getString(player.getUniqueId().toString()) != null)
            return;
        config.set(player.getUniqueId().toString(), 0);
        try {
            config.save(file);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public boolean Withdraw(Player player) {
        int currency = config.getInt(player.getUniqueId().toString());
        if (currency < SwUtility.currencyValue){
            return false;
        } else{
            currency -= SwUtility.currencyValue;
            config.set(player.getUniqueId().toString(), currency);
            try {
                config.save(file);
                return true;
            } catch (IOException e) {
                e.printStackTrace();
                return false;
            }
        }
    }

    public boolean Deposit(Player player){
        int currency = config.getInt(player.getUniqueId().toString());
        currency += SwUtility.currencyValue;
        config.set(player.getUniqueId().toString(), currency);
        try {
            config.save(file);
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean Buy(Player buyer, Player seller){
        return false;
    }

    public boolean initBuy(Location location, Player player, int amount) {
//        List<String> list = config.getList("signs");
        return false;
    }

    public int Balance(Player player){
        return config.getInt(player.getUniqueId().toString());
    }
}

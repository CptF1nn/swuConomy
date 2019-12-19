package com.swucraft.swuConomy;

import org.bukkit.Location;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DataHandler {
    Map<String, Map<Integer, List<SerialSign>>> signs;
    FileConfiguration config;
    File file;

    public DataHandler(File fileLocation){
        signs = new HashMap<>();
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
        SerialSign sign = new SerialSign(location, player, amount);
        Map<Integer, List<SerialSign>> inner = signs.getOrDefault(sign.world, new HashMap<>());
        List<SerialSign> list = inner.getOrDefault(sign.x, new ArrayList<>());
        list.add(sign);
        inner.put(sign.x, list);
        signs.put(sign.world, inner);
        return true;
    }

    public void load() {
        // TODO: Load
    }

    public void save() {
        // TODO: write
    }

    public int Balance(Player player){
        return config.getInt(player.getUniqueId().toString());
    }
}

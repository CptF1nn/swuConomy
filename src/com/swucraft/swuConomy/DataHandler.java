package com.swucraft.swuConomy;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;

public class DataHandler {

    public DataHandler(File fileLocation){
        File file = new File(fileLocation+File.separator+"data.yml");
        FileConfiguration config = YamlConfiguration.loadConfiguration(file);
        if (!file.exists()) {
            try{
                file.createNewFile();
            } catch(Exception e){
                e.printStackTrace();
            }
        }
    }
}

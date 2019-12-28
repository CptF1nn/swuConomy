package com.swucraft.swuConomy;

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.*;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class DataHandler {
    private static final String SIGNS_FILE = "signs.data";
    private static final String BLOCKS_FILE = "blocks.data";
    private final Map<String, Map<Vector3, SerialSign>> signs;
    private final Map<UUID, List<OwnedBlock>> uuidChestMap;
    private final Map<String, Map<Vector3, OwnedBlock>> locationChestMap;
    private FileConfiguration config;
    private final File fileLocation;
    private final File file;

    public DataHandler(File fileLocation){
        signs = new HashMap<>();
        uuidChestMap = new HashMap<>();
        locationChestMap = new HashMap<>();
        this.fileLocation = fileLocation;
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
        trySave();
    }

    public boolean Withdraw(Player player) {
        int currency = config.getInt(player.getUniqueId().toString());
        if (currency < SwUtility.currencyValue){
            return false;
        } else{
            currency -= SwUtility.currencyValue;
            config.set(player.getUniqueId().toString(), currency);
            return trySave();
        }
    }

    private boolean trySave() {
        try {
            config.save(file);
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean Deposit(Player player){
        int currency = config.getInt(player.getUniqueId().toString());
        currency += SwUtility.currencyValue;
        config.set(player.getUniqueId().toString(), currency);
        return trySave();
    }

    public boolean hasEnough(UUID buyer, int amount) {
        return config.getInt(buyer.toString()) >= amount;
    }

    public boolean transfer(UUID buyer, UUID seller, int amount) {
        String actualBuyer = buyer.toString();
        String actualSeller = seller.toString();
        int buyerAmount = config.getInt(actualBuyer);
        if (buyerAmount < amount) return false;
        int sellerAmount = config.getInt(actualSeller);
        buyerAmount -= amount;
        sellerAmount += amount;
        config.set(actualBuyer, buyerAmount);
        config.set(actualSeller, sellerAmount);
        return trySave();
    }

    public boolean initBuy(SerialSign sign) {
        OwnedBlock ownedBlock = sign.getOwnedBlock();
        add(sign);
        add(ownedBlock);
        return true;
    }

    public void add(SerialSign sign) {
        OwnedBlock ownedBlock = sign.getOwnedBlock();
        Map<Vector3, SerialSign> inner = signs.getOrDefault(ownedBlock.getWorld(), new HashMap<>());
        inner.put(ownedBlock.getLocation(), sign);
        signs.put(ownedBlock.getWorld(), inner);
    }

    public void load() {
        File signs = new File(fileLocation + File.separator + SIGNS_FILE);
        File blocks = new File(fileLocation + File.separator + BLOCKS_FILE);
        readAll(signs, (string) -> {
            SerialSign sign = SerialSign.fromString(string);
            add(sign);
            add(sign.getOwnedBlock());
        });
        readAll(blocks, (string) -> {
            OwnedBlock ownedBlock = OwnedBlock.fromString(string);
            add(ownedBlock);
        });
    }

    private void readAll(File file, Consumer<String> collector) {
        try (BufferedReader r = new BufferedReader(new FileReader(file))) {
            String s;
            while ((s = r.readLine()) != null) {
                collector.accept(s);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void save() {
        File signs = new File(fileLocation + File.separator + SIGNS_FILE);
        File blocks = new File(fileLocation + File.separator + BLOCKS_FILE);
        saveAll(signs, () -> {
            StringBuilder builder = new StringBuilder();
            for (Map<Vector3, SerialSign> map : this.signs.values()) {
                for (SerialSign sign : map.values()) {
                    builder.append(sign.toString());
                    builder.append('\n');
                }
            }
            return builder.toString();
        });
        saveAll(blocks, () -> {
            StringBuilder builder = new StringBuilder();
            for (List<OwnedBlock> ownedBlocks : uuidChestMap.values()) {
                for (OwnedBlock ownedBlock : ownedBlocks) {
                    builder.append(ownedBlock.toString());
                    builder.append('\n');
                }
            }
            return builder.toString();
        });
    }

    private void saveAll(File file, Supplier<String> supplier) {
        try (BufferedWriter w = new BufferedWriter(new FileWriter(file))) {
            w.write(supplier.get());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public int Balance(Player player) {
        return config.getInt(player.getUniqueId().toString());
    }

    public void add(OwnedBlock ownedBlock) {
        List<OwnedBlock> list = uuidChestMap.getOrDefault(ownedBlock.getUUID(), new ArrayList<>());
        list.add(ownedBlock);
        uuidChestMap.put(ownedBlock.getUUID(), list);
        Map<Vector3, OwnedBlock> map = locationChestMap.getOrDefault(ownedBlock.getWorld(), new HashMap<>());
        map.put(ownedBlock.getLocation(), ownedBlock);
        locationChestMap.put(ownedBlock.getWorld(), map);
    }

    public SerialSign getSign(String world, Vector3 location) {
        Map<Vector3, SerialSign> inner = signs.getOrDefault(world, new HashMap<>());
        return inner.get(location);
    }

    public void remove(OwnedBlock ownedBlock) {
        Map<Vector3, OwnedBlock> map = locationChestMap.get(ownedBlock.getWorld());
        map.remove(ownedBlock.getLocation());
        List<OwnedBlock> list = uuidChestMap.get(ownedBlock.getUUID());
        list.remove(ownedBlock);
    }

    public void remove(String world, Vector3 location) {
        OwnedBlock ownedBlock = locationChestMap.getOrDefault(world, new HashMap<>()).get(location);
        if (ownedBlock != null)
            remove(ownedBlock);
    }

    public void removeSign(SerialSign sign) {
        OwnedBlock block = sign.getOwnedBlock();
        Map<Vector3, SerialSign> inner = signs.get(block.getWorld());
        if (inner == null)
            return;
        SerialSign savedSign = inner.get(block.getLocation());
        if (savedSign == null || !savedSign.getOwnedBlock().getUUID().equals(block.getUUID()))
            return;
        inner.remove(block.getLocation());
    }

    public OwnedBlock getInformation(Block block) {
        return getInformation(block.getLocation());
    }

    public OwnedBlock getInformation(Location location) {
        String world = location.getWorld().getName();
        Map<Vector3, OwnedBlock> map = locationChestMap.get(world);
        if (map == null) return null;
        return map.get(new Vector3(location));
    }

    public boolean isProtected(OwnedBlock ownedBlock) {
        return isProtected(ownedBlock.getLocation(), ownedBlock.getWorld());
    }

    public boolean isProtected(Vector3 location, String world) {
        Map<Vector3, OwnedBlock> map = locationChestMap.get(world);
        if (map == null)
            return false;
        return map.get(location) != null;
    }
}

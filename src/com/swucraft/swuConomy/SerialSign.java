package com.swucraft.swuConomy;

import org.bukkit.Location;
import org.bukkit.entity.Player;

public class SerialSign {
    String world;
    int x;
    int y;
    int z;
    String UUID;
    int amount;

    public SerialSign(Location location, Player player, int amount) {
        world = location.getWorld().getName();
        x = location.getBlockX();
        y = location.getBlockY();
        z = location.getBlockZ();
        this.UUID = player.getUniqueId().toString();
        this.amount = amount;
    }

    public SerialSign(String world, int x, int y, int z, String UUID, int amount) {
        this.world = world;
        this.x = x;
        this.y = y;
        this.z = z;
        this.UUID = UUID;
        this.amount = amount;
    }

    public String serialize() {
        return world + '^' +
                x + '^' +
                y + '^' +
                z + '^' +
                UUID + '^' +
                amount;
    }

    public static SerialSign deserialize(String input) {
        String[] items = input.split("\\^");
        return new SerialSign(
                items[0],
                Integer.parseInt(items[1]),
                Integer.parseInt(items[2]),
                Integer.parseInt(items[3]),
                items[4],
                Integer.parseInt(items[5])
        );
    }
}

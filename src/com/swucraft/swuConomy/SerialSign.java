package com.swucraft.swuConomy;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import java.util.Objects;

public class SerialSign {
    private final Material type;
    private final OwnedBlock ownedBlock;
    private final int price;
    private int amount;


    public SerialSign(OwnedBlock ownedBlock, int price, String type) {
        this.ownedBlock = ownedBlock;
        this.price = price;
        this.type = Material.matchMaterial(type);
    }

    public SerialSign(OwnedBlock ownedBlock, int price, String type, int amount) {
        this(ownedBlock, price, type);
        this.amount = amount;
    }

    public SerialSign(Location location, Player player, int price, int amount, String type) {
        this(
                location.getWorld().getName(),
                new Vector3(location),
                player.getUniqueId().toString(),
                price,
                amount,
                type
        );
    }

    public SerialSign(String world, Vector3 location, String UUID, int price, int amount, String type) {
        this(
                new OwnedBlock(
                        world,
                        location,
                        UUID
                ),
                price,
                type,
                amount
        );
    }

    public SerialSign(String world, int x, int y, int z, String UUID, int price, String type) {
        this(
                world,
                new Vector3(x, y, z),
                UUID,
                price,
                1,
                type
        );
    }

    public OwnedBlock getOwnedBlock() {
        return ownedBlock;
    }

    public int getPrice() {
        return price;
    }

    public Material getType() {
        return type;
    }

    public int getAmount() {
        return amount;
    }

    public void setAmount(int amount) {
        this.amount = amount;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SerialSign that = (SerialSign) o;
        return Objects.equals(getOwnedBlock(), that.getOwnedBlock());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getOwnedBlock());
    }

    public String toString() {
        return ownedBlock + "^" + price + '^' + type + '^' + amount;
    }

    public static SerialSign fromString(String input) {
        String[] items = input.split("\\^");
        return new SerialSign(
                OwnedBlock.fromString(items[0]),
                Integer.parseInt(items[1]),
                items[2],
                Integer.parseInt(items[3])
        );
    }
}

package com.swucraft.swuConomy;

import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.Objects;
import java.util.UUID;

public class OwnedBlock {
    final String world;
    final Vector3 location;
    final UUID UUID;

    public OwnedBlock (String world, Vector3 location, UUID UUID) {
        this.world = world;
        this.location = location;
        this.UUID = UUID;
    }

    public OwnedBlock (Location location, Player player) {
        this(
                location.getWorld().getName(),
                new Vector3(location),
                player.getUniqueId()
        );
    }

    public UUID getUUID () {
        return UUID;
    }

    public String getWorld () {
        return world;
    }

    public int getX () {
        return location.x;
    }

    public int getY () {
        return location.y;
    }

    public int getZ () {
        return location.z;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        OwnedBlock that = (OwnedBlock) o;
        return getUUID().equals(that.getUUID()) &&
                getWorld().equals(that.getWorld()) &&
                location.equals(that.location);
    }

    @Override
    public int hashCode() {
        return Objects.hash(getUUID(), getWorld(), location);
    }

    public String toString() {
        return world + "#" + location + "#" + UUID;
    }

    public static OwnedBlock fromString(String input) {
        String[] parts = input.split("#");
        return new OwnedBlock(
                parts[0],
                Vector3.fromString(parts[1]),
                java.util.UUID.fromString(parts[2])
        );
    }

    public Vector3 getLocation() {
        return location;
    }
}

package com.swucraft.swuConomy;

import org.bukkit.Location;
import org.bukkit.block.BlockFace;

import java.util.Objects;

public class Vector3 {
    int x;
    int y;
    int z;

    Vector3 (int x, int y, int z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    Vector3 (Location location) {
        this(location.getBlockX(), location.getBlockY(), location.getBlockZ());
    }

    public Vector3 offset(BlockFace modifier) {
        return new Vector3(x + modifier.getModX(), y + modifier.getModY(), z + modifier.getModZ());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Vector3 vector3 = (Vector3) o;
        return x == vector3.x &&
                y == vector3.y &&
                z == vector3.z;
    }

    @Override
    public int hashCode() {
        return Objects.hash(x, y, z);
    }

    @Override
    public String toString() {
        return "" + '(' + x + ',' + y + ',' + z + ')';
    }

    public static Vector3 fromString (String input) {
        String sub = input.substring(1, input.length() - 1);
        String[] coords = sub.split(",");
        return new Vector3(
                Integer.parseInt(coords[0]),
                Integer.parseInt(coords[1]),
                Integer.parseInt(coords[2])
        );
    }
}

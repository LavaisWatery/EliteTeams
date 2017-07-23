package net.elitemc.eliteteams.util;

import com.google.common.base.Preconditions;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;

/**
 * Created by LavaisWatery on 2017-07-06.
 */
public class BlockPos {

    public BlockPos(Location l) {
        x = l.getBlockX();
        y = l.getBlockY();
        z = l.getBlockZ();
        world = l.getWorld();
    }

    public BlockPos(int x, int y, int z, World world) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.world = world;
    }

    private int x, y, z;
    private World world;

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public int getZ() {
        return z;
    }

    public BlockPos withY(int y) {
        return new BlockPos(x, y, z, world);
    }

    public World getWorld() {
        return world;
    }

    public ChunkPos getChunkPos() {
        return chunkPos;
    }

    public Location toLocation() {
        return new Location(getWorld(), getX(), getY(), getZ());
    }

    public int distanceSquared(BlockPos other) {
        Preconditions.checkArgument(other.getWorld().equals(getWorld()), "Can't compare the distances of different worlds");
        return square(x - other.x) + square(y - other.y) + square(z - other.z);
    }

    public Material getTypeAt() {
        return Material.getMaterial(getWorld().getBlockTypeIdAt(getX(), getY(), getZ()));
    }

    public byte getDataAt() {
        return getWorld().getBlockAt(getX(), getY(), getZ()).getData();
    }

    private final ChunkPos chunkPos = new ChunkPos(getX() >> 4, getZ() >> 4, getWorld());

    private static int square(int i) {
        return i * i;
    }

}

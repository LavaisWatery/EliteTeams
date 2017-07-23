package net.elitemc.eliteteams.util;

import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.World;

/**
 * Created by LavaisWatery on 2017-07-06.
 */
public class ChunkPos {

    public ChunkPos(int x, int z, World world) {
        this.x = x;
        this.z = z;
        this.world = world;
    }

    private int x, z;
    private World world;

    public int getAbsoluteX(int relativeX) {
        return fromRelative(getX(), relativeX);
    }
    public int getAbsoluteZ(int absoluteZ) {
        return fromRelative(getZ(), absoluteZ);
    }

    public static ChunkPos fromChunk(Chunk chunk) {
        return new ChunkPos(chunk.getX(), chunk.getZ(), chunk.getWorld());
    }

    public static ChunkPos fromLocation(Location l) {
        return new ChunkPos(l.getBlockX() >> 4, l.getBlockZ() >> 4, l.getWorld());
    }
    public static int toRelative(int absolute) {
        return absolute & 0xF; //First 16 bits
    }
    public static int fromRelative(int chunk, int relative) {
        return (chunk << 4) | (relative & 0xF);
    }

    public int getX() {
        return x;
    }

    public int getZ() {
        return z;
    }

    public World getWorld() {
        return world;
    }

    public boolean isLoaded() {
        return getWorld().isChunkLoaded(getX(), getZ());
    }

}


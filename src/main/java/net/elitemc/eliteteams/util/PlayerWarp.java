package net.elitemc.eliteteams.util;

import net.elitemc.commons.util.FakeLocation;
import org.bukkit.Location;

/**
 * Created by LavaisWatery on 2017-07-22.
 */
public class PlayerWarp extends FakeLocation {

    public PlayerWarp(String warpName, String worldName, double x, double y, double z) {
        super(worldName, x, y, z);
        this.warpName = warpName;
    }

    public PlayerWarp(Location location, String warpName) {
        super(location);
        this.warpName = warpName;
    }

    public PlayerWarp(String input) {
        super(input);
    }

    private boolean destroyed = false;
    private String warpName;

    public String getWarpName() {
        return warpName;
    }

    public boolean isDestroyed() {
        return destroyed;
    }

    public void setDestroyed(boolean destroyed) {
        this.destroyed = destroyed;
    }

    @Override
    public String serialize() {
        return this.warpName + ":" + getWorldName() + ":" + getX() + ":" + getY() + ":" + getZ() + ":" + getYaw() + ":" + getPitch();
    }

    @Override
    public void deserialize(String str) {
        String[] split = str.split(":");

        this.warpName = split[0];
        setWorldName(split[1]);
        setX(Double.parseDouble(split[2]));
        setY(Double.parseDouble(split[3]));
        setZ(Double.parseDouble(split[4]));
        setYaw(Float.parseFloat(split[5]));
        setPitch(Float.parseFloat(split[6]));
    }
}

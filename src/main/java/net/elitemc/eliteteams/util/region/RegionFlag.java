package net.elitemc.eliteteams.util.region;

import net.elitemc.eliteteams.util.IItem;

/**
 * Created by LavaisWatery on 2017-06-23.
 */
public abstract class RegionFlag implements IItem, FlagSerializable {

    public RegionFlag(Region region, FlagType type) {
        this.region = region;
        this.type = type;
    }

    private Region region;
    private FlagType type;

    public abstract boolean allows();

    public Region getRegion() {
        return region;
    }

    public void setRegion(Region region) {
        this.region = region;
    }

    public void setType(FlagType type) {
        this.type = type;
    }

    public FlagType getType() {
        return type;
    }

}

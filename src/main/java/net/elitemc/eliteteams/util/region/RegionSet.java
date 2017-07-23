package net.elitemc.eliteteams.util.region;

import org.bukkit.Location;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by LavaisWatery on 2017-06-23.
 */
public class RegionSet extends HashSet<Region> {

    public RegionSet(Location point, List<Region> regions) {
        this.point = point;

        addAll(regions);
        for(Region region : regions) {
            if(this.regionType == null && region.getType() != null) this.regionType = region.getType();
        }
    }

    public RegionSet(List<Region> regions) {
        for(Region region : regions) {
            if(this.regionType == null && region.getType() != null) this.regionType = region.getType();
            add(region);
        }
    }

    private Region.RegionType regionType = null;
    private Location point;

    public boolean allows(FlagType type) {
        if(isEmpty()) return true;
        for(Region region : this) {
            if(!region.allows(type)) return false;
        }
        return true;
    }

    public Set<Region> getByFlag(FlagType flag) {
        if(isEmpty()) return null;
        Set<Region> lst = new HashSet<>();
        for(Region region : this) {
            if(region.has(flag)) lst.add(region);
        }
        return lst;
    }

    public Location getPoint() {
        return point;
    }

    public Region.RegionType getRegionType() {
        return regionType;
    }

    public void setRegionType(Region.RegionType regionType) {
        this.regionType = regionType;
    }

}

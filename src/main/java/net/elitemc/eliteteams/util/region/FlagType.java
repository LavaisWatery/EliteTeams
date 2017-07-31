package net.elitemc.eliteteams.util.region;

import net.elitemc.eliteteams.util.region.flags.ExplosionFlag;
import net.elitemc.eliteteams.util.region.flags.FlowFlag;
import net.elitemc.eliteteams.util.region.flags.PvPFlag;

/**
 * Created by LavaisWatery on 2017-06-23.
 */
public enum FlagType {

    PVP(PvPFlag.class),

    FLOW(FlowFlag.class),

    EXPLOSION(ExplosionFlag.class);

    FlagType(Class<? extends RegionFlag> flagClazz) {
        this.flagClazz = flagClazz;
    }

    private Class<? extends RegionFlag> flagClazz;

    public RegionFlag toClass(Region region) throws Exception {
        return flagClazz.getDeclaredConstructor(Region.class).newInstance(region);
    }

    public static String toPrettyList() {
        StringBuilder builder = new StringBuilder();

        for(FlagType region : FlagType.values()) {
            if(builder.length() == 0) {
                builder.append(region.toString());
            }
            else {
                builder.append(", " + region.toString());
            }
        }

        return builder.toString();
    }

    public Class<? extends RegionFlag> getFlagClazz() {
        return flagClazz;
    }

}

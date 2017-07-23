package net.elitemc.eliteteams.util.region;

/**
 * Created by LavaisWatery on 2017-06-23.
 */
public interface FlagSerializable {

    String serialize();

    void deserialize(Region region, String input);

}

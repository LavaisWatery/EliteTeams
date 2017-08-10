package net.elitemc.eliteteams.util;

import org.bukkit.configuration.ConfigurationSection;

/**
 * Created by LavaisWatery on 2017-08-09.
 */
public interface ConfigurationSerializable {

    void serialize(ConfigurationSection section);

    void deserialize(String relPath, ConfigurationSection section);

}

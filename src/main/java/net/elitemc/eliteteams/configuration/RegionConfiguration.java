package net.elitemc.eliteteams.configuration;

import net.elitemc.commons.util.PluginConfiguration;
import net.elitemc.commons.util.json.JSONObject;
import net.elitemc.eliteteams.EliteTeams;
import net.elitemc.eliteteams.handler.RegionHandler;
import net.elitemc.eliteteams.util.region.Region;
import org.bukkit.configuration.file.FileConfiguration;

import java.io.File;
import java.util.List;

/**
 * Created by LavaisWatery on 2017-06-22.
 */
public class RegionConfiguration extends PluginConfiguration {

    public RegionConfiguration(RegionHandler handler) {
        super(EliteTeams.getInstance(), "regions", new PluginConfiguration.CustomDefaults() {
            @Override
            public void run(FileConfiguration config) {
            }
        });
        this.handler = handler;

        setupConfig();
    }

    private RegionHandler handler;

    @Override
    public void setupConfig() {
        FileConfiguration config = getConfig();

        if(config.contains("regions")) {
            try {
                JSONObject found = new JSONObject(config.getString("regions"));

                if(found != null) {
                    for(String index : found.keySet()) {
                        JSONObject foundModuleOj = found.getJSONObject(index);

                        handler.registerRegion(new Region(foundModuleOj));
                    }
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }

    public void saveConfig(List<Region> regionList) {
        File file = getFile();
        FileConfiguration config = getConfig();

        JSONObject regions = new JSONObject();

        for(Region region : regionList) {
            regions.put(region.getIndex(), region.serialize());
        }

        config.set("regions", regions.toString(2));

        try {
            config.save(file);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public RegionHandler getHandler() {
        return handler;
    }

}

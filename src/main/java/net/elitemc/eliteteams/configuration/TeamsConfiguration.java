package net.elitemc.eliteteams.configuration;

import net.elitemc.commons.Init;
import net.elitemc.commons.util.PluginConfiguration;
import net.elitemc.eliteteams.EliteTeams;
import org.bukkit.configuration.file.FileConfiguration;

/**
 * Created by LavaisWatery on 2017-07-22.
 */
public class TeamsConfiguration extends PluginConfiguration {

    public TeamsConfiguration(Init handler) {
        super(EliteTeams.getInstance(), "configuration", new PluginConfiguration.CustomDefaults() {
            @Override
            public void run(FileConfiguration config) {
            }
        });
        this.handler = handler;

        setupConfig();
    }

    private Init handler;

    @Override
    public void setupConfig() {
        FileConfiguration config = getConfig();


    }

//    public void saveConfig() {
//        File file = getFile();
//        FileConfiguration config = getConfig();
//
//        JSONObject subInfoJ = new JSONObject();
//        JSONObject arenasJ = new JSONObject();
//        JSONObject groupsJ = new JSONObject();
//
//        for(ArenaSubInfo info : handler.getSubInfoBasedNames().values()) {
//            subInfoJ.put(info.getIndex(), info.serialize());
//        }
//
//        for(GroupedArenas group : handler.getGroupedArenas().values()) {
//            groupsJ.put(group.getGroupName(), group.serialize());
//        }
//
//        for(Arena arena : arenas) {
//            arenasJ.put(arena.getIndexName(), arena.serialize());
//        }
//
//        config.set("subinfo", subInfoJ.toString(2));
//        config.set("arenas", arenasJ.toString(2));
//        config.set("groups", groupsJ.toString(2));
//
//        try {
//            config.save(file);
//        } catch (Exception ex) {
//            ex.printStackTrace();
//        }
//    }

    public Init getHandler() {
        return handler;
    }

}

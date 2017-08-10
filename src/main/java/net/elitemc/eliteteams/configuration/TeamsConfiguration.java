package net.elitemc.eliteteams.configuration;

import net.elitemc.commons.util.PluginConfiguration;
import net.elitemc.eliteteams.EliteTeams;
import net.elitemc.eliteteams.Init;
import net.elitemc.eliteteams.util.team.EliteTeam;
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

        EliteTeam.MAX_PLAYERS = config.getInt("teams.max-players-per-team");
        EliteTeam.MAX_TEAMNAME_LENGTH = config.getInt("teams.max-team-length");
        EliteTeam.MIN_TEAMNAME_LENGTH = config.getInt("teams.min-team-length");
        EliteTeam.INVITE_TIMEOUT = config.getInt("teams.invite-timeout");
    }

    public Init getHandler() {
        return handler;
    }

}

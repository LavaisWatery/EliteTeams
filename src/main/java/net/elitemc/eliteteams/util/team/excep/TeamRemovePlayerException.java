package net.elitemc.eliteteams.util.team.excep;

import net.elitemc.eliteteams.util.team.EliteTeam;
import org.bukkit.entity.Player;

/**
 * Created by LavaisWatery on 2017-07-31.
 */
public class TeamRemovePlayerException extends Exception {

    public TeamRemovePlayerException(EliteTeam team, Player recieved, String reason) {
        super(reason);
        this.team = team;
        this.player = recieved;
    }

    private EliteTeam team;
    private Player player;

    public EliteTeam getTeam() {
        return team;
    }

    public Player getPlayer() {
        return player;
    }

}

package net.elitemc.eliteteams.util.team.excep;

import net.elitemc.eliteteams.util.team.EliteTeam;
import org.bukkit.entity.Player;

/**
 * Created by Kyle Gosleigh on 3/4/2017.
 */
public class TeamPromoteException extends Exception {

    public TeamPromoteException(EliteTeam team, Player recieved, String reason) {
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

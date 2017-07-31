package net.elitemc.eliteteams.util.team.excep;

import net.elitemc.eliteteams.util.team.EliteTeam;
import org.bukkit.entity.Player;

import java.util.UUID;

/**
 * Created by Kyle Gosleigh on 3/4/2017.
 */
public class TeamAddPlayerException extends Exception {

    public TeamAddPlayerException(EliteTeam team, Player recieved, String reason) {
        super(reason);
        this.team = team;
        this.target = recieved.getUniqueId();
    }

    public TeamAddPlayerException(EliteTeam team, UUID target, String reason) {
        super(reason);
        this.team = team;
        this.target = target;
    }

    private EliteTeam team;
    private UUID target;

    public EliteTeam getTeam() {
        return team;
    }

    public UUID getPlayer() {
        return target;
    }

}

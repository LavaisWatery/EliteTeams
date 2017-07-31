package net.elitemc.eliteteams.util.team.excep;

import net.elitemc.eliteteams.util.team.EliteTeam;
import org.bukkit.entity.Player;

import java.util.UUID;

/**
 * Created by LavaisWatery on 2017-07-31.
 */
public class TeamInvitePlayerException extends Exception {

    public TeamInvitePlayerException(EliteTeam team, Player recieved, String reason) {
        super(reason);
        this.team = team;
        this.target = recieved.getUniqueId();
    }

    public TeamInvitePlayerException(EliteTeam team, UUID target, String reason) {
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

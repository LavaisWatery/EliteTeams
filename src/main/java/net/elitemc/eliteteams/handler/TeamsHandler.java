package net.elitemc.eliteteams.handler;

import com.mongodb.BasicDBObject;
import com.mongodb.DBCursor;
import com.mongodb.QueryBuilder;
import net.elitemc.commons.handler.BoardHandler;
import net.elitemc.commons.util.Handler;
import net.elitemc.commons.util.PlayerUtility;
import net.elitemc.commons.util.scoreboard.V2.Board;
import net.elitemc.eliteteams.util.team.EliteTeam;
import net.elitemc.origin.Init;
import net.elitemc.origin.util.event.PlayerChatEvent;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerJoinEvent;

import java.util.HashMap;
import java.util.UUID;

/**
 * Created by LavaisWatery on 2017-07-31.
 */
public class TeamsHandler extends Handler {
    private static TeamsHandler instance;

    public TeamsHandler() {
        instance = this;
    }

    private HashMap<UUID, EliteTeam> playerTeams = new HashMap<>();
    private HashMap<String, EliteTeam> teams = new HashMap<>();

    @Override
    public void init() {
        for(Player player : PlayerUtility.getOnlinePlayers()) {
            getPlayerTeam(player);
        }
    }

    @Override
    public void unload() {

    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        getPlayerTeam(event.getPlayer());
    }

    @EventHandler
    public void onTeamChat(PlayerChatEvent event) {
        Player player = event.getPlayer();
        EliteTeam team = null;

        if((team = TeamsHandler.getInstance().getPlayerTeam(player)) != null) {
            if(team.getTeamChat().contains(player.getUniqueId())) {
                event.setCancelled(true);
                team.sendTeamChat(player, event.getMessage());
            }
        }
    }

    public EliteTeam getPlayerTeam(UUID uid) {
        if(!playerTeams.containsKey(uid)) {
            playerTeams.put(uid, new EliteTeam("-1"));
            QueryBuilder findPlayerTeamQuery = new QueryBuilder();
            findPlayerTeamQuery.and("members").is(uid.toString());

            DBCursor cursor = Init.getInstance().getMongoDatabase().getDatabase().getCollection(EliteTeam.TEAM_COLLECTION).find(findPlayerTeamQuery.get());

            if(cursor.hasNext()) {
                BasicDBObject found = (BasicDBObject) cursor.next();

                if(found != null) {
                    String teamName = found.getString("teamName");

                    return playerTeams.put(uid, getEliteTeam(teamName));
                }
                else {
                    playerTeams.put(uid, null);
                    return null;
                }
            }
            else {
                playerTeams.put(uid, null);
                return null;
            }
        }

        return playerTeams.get(uid);
    }

    public EliteTeam getPlayerTeam(Player player) {
        return getPlayerTeam(player.getUniqueId());
    }

    public EliteTeam getEliteTeam(String key) {
        try {
            if(!teams.containsKey(key.toLowerCase())) {
                EliteTeam team = new EliteTeam(key);
                teams.put(key.toLowerCase(), team);
                try {
                    if(team.loadData()) {
                        return team;
                    }
                    else {
                        teams.put(key.toLowerCase(), null);
                        return null;
                    }
                } catch (Exception eX) {
                    teams.put(key.toLowerCase(), null);
                    return null;
                }
            }

            return teams.get(key.toLowerCase());
        } catch (Exception ex) {
            return null;
        }
    }

    public void setPlayerTeam(Player player, EliteTeam team) {
        setPlayerTeam(player.getUniqueId(), team);
    }

    public void setPlayerTeam(UUID id, EliteTeam team) {
        playerTeams.put(id, team);
        Board board = BoardHandler.getInstance().getPlayerBoard(id);

        if(board != null) {
            board.getBufferedObjective().updateEntry("team", "");
        }
    }

    public HashMap<UUID, EliteTeam> getPlayerTeams() {
        return playerTeams;
    }

    public HashMap<String, EliteTeam> getTeams() {
        return teams;
    }

    public static TeamsHandler getInstance() {
        return instance;
    }

}

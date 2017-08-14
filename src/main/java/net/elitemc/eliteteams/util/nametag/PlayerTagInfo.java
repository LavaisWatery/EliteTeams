package net.elitemc.eliteteams.util.nametag;

import net.elitemc.commons.util.MessageUtility;
import net.elitemc.commons.util.PlayerUtility;
import net.elitemc.commons.util.hook.PermissionsHook;
import net.elitemc.commons.util.hook.group.PermissionsGroup;
import net.elitemc.commons.util.scoreboard.V2.Board;
import net.elitemc.eliteteams.EliteTeams;
import net.elitemc.eliteteams.handler.TeamsHandler;
import net.elitemc.eliteteams.util.TeamsPlayerWrapper;
import net.elitemc.eliteteams.util.team.EliteTeam;
import net.elitemc.origin.Init;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

import java.util.UUID;

/**
 * Created by LavaisWatery on 2017-08-10.
 */
public class PlayerTagInfo {

    void PlayerTagInfo(UUID uid, Board board) {
        this.ID = uid;
        this.board = board;
    }

    public PlayerTagInfo(Player player, Board board) {
        PlayerTagInfo(player.getUniqueId(), board);
    }

    public PlayerTagInfo(UUID uid, Board board) {
        PlayerTagInfo(uid, board);
    }

    private Board board;
    private UUID ID = null;

    public void refreshPlayer(Player player) {
        Player me = toPlayer();
        PermissionsHook hook = Init.getInstance().getPermissionsHook();
        PermissionsGroup playerGroup = hook.getPlayersGroup(player.getUniqueId());
        Scoreboard sb = board.getScoreboard();
        EliteTeam meTeam = TeamsHandler.getInstance().getPlayerTeam(me), playerTeam = TeamsHandler.getInstance().getPlayerTeam(player);
        boolean isSame = meTeam != null && playerTeam != null && meTeam == playerTeam ? true : false;
        Team team = sb.getTeam((isSame ? "T" : "") + playerGroup.getGroupName());

        if(team != null) {
            team.addPlayer(player);
        }
    }

    public void apply() {
        Player player = toPlayer();
        EliteTeam meTeam = TeamsHandler.getInstance().getPlayerTeam(player);

        if(player == null) return;

        /*
        Team team = sb.getTeam((isSame ? "T" : "") + playerGroup.getGroupName());
         */

        new BukkitRunnable() {
            @Override
            public void run() {
                PermissionsHook hook = Init.getInstance().getPermissionsHook();
                PermissionsGroup playerGroup = hook.getPlayersGroup(ID);
                Scoreboard sb = board.getScoreboard();

                for (Player online : PlayerUtility.getOnlinePlayers()) {
                    PermissionsGroup onlineGroup = null;
                    Team team = null;
                    EliteTeam playerTeam = TeamsHandler.getInstance().getPlayerTeam(online);
                    boolean isSame = meTeam != null && playerTeam != null && meTeam == playerTeam ? true : false;

                    if((onlineGroup = hook.getPlayersGroup(online)) == null || (team = sb.getTeam((isSame ? "T" : "") + onlineGroup.getGroupName())) == null) {
                        continue;
                    }

                    team.addPlayer(online);
                    sb = online.getScoreboard();
                    if(player != null && playerGroup != null) {
                        Team aTeam = sb.getTeam((isSame ? "T" : "") + playerGroup.getGroupName());

                        if(aTeam != null) aTeam.addPlayer(player);
                    }
                }
            }
        }.runTaskLater(EliteTeams.getInstance(), 1L);
    }

    public void create() {
        Scoreboard sb = board.getScoreboard();

        for(PermissionsGroup group : Init.getInstance().getPermissionsHook().getHookGroups().values()) {
            if(sb.getTeam(group.getGroupName()) == null) {
                Team t = sb.registerNewTeam(group.getGroupName());

                t.setPrefix(ChatColor.translateAlternateColorCodes('&', group.getColorStr()));
            }
            if(sb.getTeam("T" + group.getGroupName()) == null) {
                Team t = sb.registerNewTeam("T" + group.getGroupName());

                t.setPrefix(ChatColor.translateAlternateColorCodes('&', "* " + group.getColorStr()));
            }
        }

        for(ChatColor color : ChatColor.values()) {
            if(sb.getTeam(color.toString()) == null) {
                Team t = sb.registerNewTeam(color.name());

                t.setPrefix(color.toString());
            }
        }
    }

    public UUID getID() {
        return ID;
    }

    public Player toPlayer() {
        return Bukkit.getPlayer(ID);
    }

}

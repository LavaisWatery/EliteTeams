package net.elitemc.eliteteams.handler;

import net.elitemc.commons.handler.BoardHandler;
import net.elitemc.commons.util.Handler;
import net.elitemc.commons.util.PlayerUtility;
import net.elitemc.commons.util.scoreboard.V2.Board;
import net.elitemc.eliteteams.EliteTeams;
import net.elitemc.eliteteams.util.nametag.PlayerTagInfo;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;
import java.util.UUID;

/**
 * Created by LavaisWatery on 2017-08-10.
 */
public class NametagHandler extends Handler {
    private static NametagHandler instance;

    public NametagHandler() {
        instance = this;
    }

    private BoardHandler boardHandler = BoardHandler.getInstance();
    private HashMap<UUID, PlayerTagInfo> playerTags = new HashMap<>();

    @Override
    public void init() {
        new BukkitRunnable() {
            @Override
            public void run() {
                for(Player player : PlayerUtility.getOnlinePlayers()) {
                    assure(player);
                }
            }
        }.runTask(EliteTeams.getInstance());
    }

    @Override
    public void unload() {

    }

    public void refreshPlayer(Player player, Player... players) {
        for(Player targ : players) {
            assure(targ).refreshPlayer(player);
        }
    }

    public void refreshPlayer(Player player) {
        refreshPlayer(player, PlayerUtility.getOnlinePlayers());
    }

    public PlayerTagInfo getPlayerTag(UUID uid) {
        return playerTags.get(uid);
    }

    public PlayerTagInfo getPlayerTag(Player player) {
        return getPlayerTag(player.getUniqueId());
    }

    public PlayerTagInfo assure(Player player) {
        UUID uid = player.getUniqueId();

        Board board = boardHandler.assure(player);
        PlayerTagInfo tagInfo = null;
        if((tagInfo = playerTags.get(uid)) == null) {
            tagInfo = new PlayerTagInfo(player, board);
            playerTags.put(uid, tagInfo);
            tagInfo.create();
            tagInfo.apply();
            return tagInfo;
        }
        else return tagInfo;
    }


    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        assure(event.getPlayer());
    }

    public static NametagHandler getInstance() {
        return instance;
    }

}

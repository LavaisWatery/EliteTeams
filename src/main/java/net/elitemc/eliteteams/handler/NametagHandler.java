package net.elitemc.eliteteams.handler;

import net.elitemc.commons.util.Handler;
import net.elitemc.eliteteams.util.nametag.PlayerTagInfo;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerJoinEvent;

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

    private HashMap<UUID, PlayerTagInfo> playerTags = new HashMap<>();

    @Override
    public void init() {

    }

    @Override
    public void unload() {

    }

    public PlayerTagInfo assure(Player player) {
        UUID uid = player.getUniqueId();

        PlayerTagInfo tagInfo = null;
        if((tagInfo = playerTags.get(uid)) == null) {
            tagInfo = new PlayerTagInfo(player);
            playerTags.put(uid, tagInfo);
            return tagInfo;
        }
        else return tagInfo;
    }


    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {

    }

    public static NametagHandler getInstance() {
        return instance;
    }

}

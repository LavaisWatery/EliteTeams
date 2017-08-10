package net.elitemc.eliteteams.util.nametag;

import org.bukkit.entity.Player;

import java.util.UUID;

/**
 * Created by LavaisWatery on 2017-08-10.
 */
public class PlayerTagInfo {

    void PlayerTagInfo(UUID uid) {
        this.ID = uid;
    }

    public PlayerTagInfo(Player player) {
        PlayerTagInfo(player.getUniqueId());
    }

    public PlayerTagInfo(UUID uid) {
        PlayerTagInfo(uid);
    }

    private UUID ID = null;

    public UUID getID() {
        return ID;
    }

}

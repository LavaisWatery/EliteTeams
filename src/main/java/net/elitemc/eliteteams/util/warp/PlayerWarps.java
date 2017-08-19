package net.elitemc.eliteteams.util.warp;

import mkremins.fanciful.FancyMessage;
import net.elitemc.commons.util.MessageUtility;
import net.elitemc.commons.util.interf.JsonSerializable;
import net.elitemc.commons.util.json.JSONObject;
import net.elitemc.commons.util.mongo.pooling.PoolAction;
import net.elitemc.eliteteams.util.Confirmation;
import net.elitemc.eliteteams.util.TeamsPlayerWrapper;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import net.elitemc.eliteteams.util.warp.excep.PlayerCreateWarpException;
import java.util.HashMap;

/**
 * Created by LavaisWatery on 2017-07-22.
 */
public class PlayerWarps implements JsonSerializable {

    public PlayerWarps(TeamsPlayerWrapper wrapper) {
        this.wrapper = wrapper;
    }

    public PlayerWarps(TeamsPlayerWrapper wrapper, String des) {
        this.wrapper = wrapper;

        deserialize(new JSONObject(des));
    }

    public static String MAX_WARPS_SET = ChatColor.RED + "You may not set this many warps.",
                        WARP_EXISTS = ChatColor.RED + "This warp already exists.",
                        WARP_DOESNT_EXIST = ChatColor.RED + "This warp doesn't exist.";

    private TeamsPlayerWrapper wrapper;

    private HashMap<String, PlayerWarp> playerWarps = new HashMap<>();

    public void forceOverrideWarp(Player player, String indexName) {
        { // override
            PlayerWarp warp = getPlayerWarp(indexName);

            if(warp != null) {
                Location loc = player.getLocation();

                if(warp.isDestroyed()) {
                    MessageUtility.message(player, false, ChatColor.RED + "This warp is destroyed.");
                    return;
                }

                warp.setX(loc.getX());
                warp.setY(loc.getY());
                warp.setZ(loc.getZ());
                warp.setYaw(loc.getYaw());
                warp.setPitch(loc.getPitch());
                MessageUtility.message(player, false, "overrode warp");

                return;
            }
        }

        if(playerWarps.size() >= wrapper.getMax_warps()) {
            MessageUtility.message(player, false, MAX_WARPS_SET);
            return;
        }

        try {
            PlayerWarp warp = addWarp(indexName, player.getLocation());

            if(warp != null) {
                MessageUtility.message(player, false, ChatColor.DARK_AQUA + "Warp '" + ChatColor.GRAY + indexName + ChatColor.DARK_AQUA + "' created at location " + ChatColor.GRAY + warp.getX() + ChatColor.DARK_AQUA + "," + ChatColor.GRAY + warp.getY() + ChatColor.DARK_AQUA + "," + ChatColor.GRAY + warp.getZ() + ChatColor.DARK_AQUA + ".");
            }
        } catch (PlayerCreateWarpException ex) {
            MessageUtility.message(player, false, ex.getMessage());
            return;
        }
    }

    public void addPlayerWarp(Player player, String indexName) {
        { // override
            PlayerWarp warp = getPlayerWarp(indexName);

            if(warp != null) {
                wrapper.setConfirmation(new Confirmation(wrapper, "override") {
                    Location loc = player.getLocation();

                    @Override
                    public void accept() {
                        if(warp.isDestroyed()) {
                            MessageUtility.message(player, false, ChatColor.RED + "This warp is destroyed.");
                            return;
                        }

                        warp.setX(loc.getX());
                        warp.setY(loc.getY());
                        warp.setZ(loc.getZ());
                        warp.setYaw(loc.getYaw());
                        warp.setPitch(loc.getPitch());
                        MessageUtility.message(player, false, "overrode warp");

                        clean();
                    }

                    @Override
                    public void deny() {
                        MessageUtility.message(player, false, "denied override");

                        clean();
                    }
                });
                MessageUtility.message(player, false, "Are you sure you want to override '" + indexName + "'?");
                new FancyMessage("Confirm").color(ChatColor.DARK_GREEN).command("/yes").then(" ").then("Deny").color(ChatColor.DARK_RED).command("/no").send(player);

                return;
            }
        }

        if(playerWarps.size() >= wrapper.getMax_warps()) {
            MessageUtility.message(player, false, MAX_WARPS_SET);
            return;
        }

        try {
            PlayerWarp warp = addWarp(indexName, player.getLocation());

            if(warp != null) {
                MessageUtility.message(player, false, ChatColor.DARK_AQUA + "Warp '" + ChatColor.GRAY + indexName + ChatColor.DARK_AQUA + "' created at location " + ChatColor.GRAY + warp.getX() + ChatColor.DARK_AQUA + "," + ChatColor.GRAY + warp.getY() + ChatColor.DARK_AQUA + "," + ChatColor.GRAY + warp.getZ() + ChatColor.DARK_AQUA + ".");
            }
        } catch (PlayerCreateWarpException ex) {
            MessageUtility.message(player, false, ex.getMessage());
            return;
        }
    }

    public PlayerWarp addWarp(String indexName, Location location) throws PlayerCreateWarpException {
        PlayerWarp warp = new PlayerWarp(location, indexName);

        playerWarps.put(warp.getWarpName().toLowerCase(), warp);
        wrapper.queueAction(PoolAction.SAVE);

        return warp;
    }

    public PlayerWarp addWarp(PlayerWarp warp) {
        playerWarps.put(warp.getWarpName().toLowerCase(), warp);
        wrapper.queueAction(PoolAction.SAVE);

        return warp;
    }

    public void removeWarp(PlayerWarp warp) {
        warp.setDestroyed(true);
        getPlayerWarps().remove(warp.getWarpName().toLowerCase());
        wrapper.queueAction(PoolAction.SAVE);
    }

    public PlayerWarp getPlayerWarp(String index) {
        return playerWarps.get(index.toLowerCase());
    }

    public HashMap<String, PlayerWarp> getPlayerWarps() {
        return playerWarps;
    }

    @Override
    public JSONObject serialize() {
        JSONObject oj = new JSONObject();

        for(PlayerWarp warp : playerWarps.values()) {
            oj.put(warp.getWarpName(), warp.serialize());
        }

        return oj;
    }

    @Override
    public void deserialize(JSONObject jsonObject) {
        try {
            for(String index : jsonObject.keySet()) {
                String foundWarp = jsonObject.getString(index);
                PlayerWarp playerWarp = new PlayerWarp(foundWarp);

                addWarp(playerWarp);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public TeamsPlayerWrapper getWrapper() {
        return wrapper;
    }

}

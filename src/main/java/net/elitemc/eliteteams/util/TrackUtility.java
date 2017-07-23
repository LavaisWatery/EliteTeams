package net.elitemc.eliteteams.util;

import net.cravemc.pure.handle.StaffHandler;
import net.elitemc.commons.util.FakeLocation;
import net.elitemc.commons.util.PlayerUtility;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Created by LavaisWatery on 2017-07-22.
 */
public class TrackUtility {

    public static int matchBlocks(FakeLocation start, TrackingArm arm, Material startMat, Material endMat) {
        int matched = 0;
        int x = (int) start.getX(), z = (int) start.getZ();
        Material curmat = startMat;
        Location cur = start.toLocation();

        while(matched < 1000 && (curmat == startMat || curmat == endMat)) {
            cur.add(arm.getxOffset(), 0, arm.getzOffset());
            curmat = cur.getBlock().getType();

            if((curmat == startMat || curmat == endMat)) {
                matched++;
                if(curmat == endMat) {
                    return matched;
                }
            }
            else {
                return 0;
            }

        }

        return 0;
    }

    public static int tempMatchBlocks(FakeLocation start, TrackingArm arm, Material startMat, Material endMat) {
        int matched = 0;
        int x = (int) start.getX(), z = (int) start.getZ();
        Material curmat = startMat;
        Location cur = start.toLocation();
        List<Block> rem = new ArrayList<>();

        while(matched < 1000 && (curmat == startMat || curmat == endMat)) {
            cur.add(arm.getxOffset(), 0, arm.getzOffset());
            curmat = cur.getBlock().getType();

            if((curmat == startMat || curmat == endMat)) {
                matched++;
                rem.add(cur.getBlock());
                if(curmat == endMat) {
                    for(Block b : rem) {
                        b.setType(Material.AIR);
                    }
                    return matched;
                }
            }
            else {
                return 0;
            }

        }

        return 0;
    }

    public static boolean checkPlayer(Player pl, FakeLocation start, int x, int z) {
        int num = 0;
        if (x == 0) {
            int plz = pl.getLocation().getBlockZ();
            num = Math.abs(z);
            if (Math.abs(start.getZ() - plz) < num) {
                if (z < 0) {
                    if (plz < start.getZ()) {
                        return true;
                    }
                }
                else if (plz > start.getZ()) {
                    return true;
                }
            }
        }
        else if (z == 0) {
            int plz = pl.getLocation().getBlockX();
            num = Math.abs(x);
            if (Math.abs(start.getX() - plz) < num) {
                if (x < 0) {
                    if (plz < start.getX()) {
                        return true;
                    }
                }
                else if (plz > start.getX()) {
                    return true;
                }
            }
        }
        return false;
    }

    public static List<UUID> trackDir(Player player, FakeLocation start, TrackingArm arm, int x, int z, Player player2) {
        String compass = arm.getDisplay();
        Player[] players = PlayerUtility.getOnlinePlayers();
        List<UUID> plist = new ArrayList();
        for (int i = 0; i < players.length; i++) {
            if(StaffHandler.getInstance().getVanishedStaffMembers().containsKey(players[i].getUniqueId())) {
                continue;
            }
            plist.add(players[i].getUniqueId());
        }
        List<UUID> in = new ArrayList();
        int num = Math.abs(x) + Math.abs(z);
        if (player2 == null) {
            for (int i = 0; i < plist.size(); i++)
            {
                Player pl = Bukkit.getPlayer(plist.get(i));
                boolean can = checkPlayer(pl, start, x, z);
                if (can) {
                    in.add(pl.getUniqueId());
                }
            }
        }
        else {
            boolean can = checkPlayer(player2, start, x, z);
            if (can) {
                in.add(player2.getUniqueId());
            }
        }

        return in;
    }

}

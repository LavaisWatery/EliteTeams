package net.elitemc.eliteteams.util;

import com.google.common.util.concurrent.AbstractFuture;
import com.google.common.util.concurrent.ListenableFuture;
import net.cravemc.pure.handle.StaffHandler;
import net.elitemc.commons.util.MessageUtility;
import net.elitemc.eliteteams.handler.RegionHandler;
import net.elitemc.eliteteams.handler.TeamsPlayerHandler;
import net.elitemc.eliteteams.util.region.FlagType;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerMoveEvent;

public class MoveRequest extends AbstractFuture implements Runnable, ListenableFuture {

    public MoveRequest(PlayerMoveEvent event) {
        this.event = event;
        this.position = new BlockPos(event.getTo());
    }

    public static int RADIUS = 50;

    private boolean cancelNextVel = false;
    private BlockPos position;

    private TeamsPlayerWrapper wrapper;
    private PlayerMoveEvent event;

    public TeamsPlayerWrapper getWrapper() {
        return wrapper;
    }

    public void cancelNextVel() {
        cancelNextVel = true;
    }

    @Override
    public void run() {
        Player player = event.getPlayer();
        wrapper = TeamsPlayerHandler.getInstance().getPlayerWrapper(player);

        if(player.isDead()) {
            set(null);
            return;
        }

        wrapper.setLastBlock(event.getTo());

        if ((wrapper.getPlayerState() == TeamsPlayerWrapper.KitsPlayerState.PROTECTED || player.getGameMode() == GameMode.CREATIVE)) {
            if (!StaffHandler.getInstance().getVanishedStaffMembers().containsKey(wrapper.getID()) && !cancelNextVel && player.getGameMode() != GameMode.CREATIVE && RegionHandler.getInstance().allows(event.getTo(), FlagType.PVP)) {
                wrapper.setPlayerState(TeamsPlayerWrapper.KitsPlayerState.UNPROTECTED);
                wrapper.setLastUnprotected(event.getTo());
                MessageUtility.message(player, false, ChatColor.GRAY + "You are no longer protected.");
            }
        }

        if(!cancelNextVel) {
            if (wrapper.isFrozen()) {
                boolean careY = wrapper.isFrozen();
                Location from = event.getFrom();
                Location to = event.getTo();
                double x = Math.floor(from.getX());
                double y = Math.floor(from.getY());
                double z = Math.floor(from.getZ());
                if (Math.floor(to.getX()) != x || (careY && Math.floor(to.getY()) != y) || Math.floor(to.getZ()) != z) {
                    x += .5;
                    y += .5;
                    z += .5;
                    event.getPlayer().teleport(new Location(from.getWorld(), x, y, z, from.getYaw(), from.getPitch()));
                    set(null);
                    return;
                }
            }
        }

        set(null);
    }

    public static boolean isInside(int n, int n2, int n3) {
        return Math.abs(n - n2) == Math.abs(n3 - n) + Math.abs(n3 - n2);
    }

    public static int closest(int n, int... array) {
        int n2 = array[0];
        for (int i = 0; i < array.length; ++i) {
            if (Math.abs(n - array[i]) < Math.abs(n - n2)) {
                n2 = array[i];
            }
        }
        return n2;
    }

    public BlockPos getPosition() {
        return position;
    }

    public PlayerMoveEvent getEvent() {
        return event;
    }

}

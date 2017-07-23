package net.elitemc.eliteteams.util.region.event;

import net.elitemc.eliteteams.util.TeamsPlayerWrapper;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.event.block.Action;

/**
 * Created by LavaisWatery on 2017-07-11.
 */
public class SelectionEvent extends Event {

    private static final HandlerList handlers = new HandlerList();

    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

    public SelectionEvent(Player player, TeamsPlayerWrapper wrapper, Action action, Location lcoation) {
        this.player = player;
        this.wrapper = wrapper;
        this.action = action;
        this.location = location;
    }

    private boolean cancelled = false;

    private TeamsPlayerWrapper wrapper;
    private Player player;
    private Action action;
    private Location location;

    public Player getPlayer() { return player; }

    public Action getAction() {
        return action;
    }

    public Location getLocation() {
        return location;
    }

    public TeamsPlayerWrapper getWrapper() {
        return wrapper;
    }

    public void setCancelled(boolean b) {
        this.cancelled = b;
    }

    public boolean isCancelled() {
        return this.cancelled;
    }

}

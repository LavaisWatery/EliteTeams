package net.elitemc.eliteteams.util.region;

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;

import java.util.UUID;

/**
 * Created by LavaisWatery on 2017-06-22.
 */
public class RegionSession {

    public RegionSession(UUID uid) {
        this.ID = uid;
    }

    public RegionSession(Player player) {
        this.ID = player.getUniqueId();
    }

    private UUID ID;

    private Location selectedLocationA = null, selectedLocationB = null;

    public String selectPosition(Action action, Block block) {
        return selectPosition(action, block.getLocation());
    }

    public String selectPosition(Action action, Location selected) {
        if(action == Action.RIGHT_CLICK_BLOCK) {
            selectedLocationB = selected;
            return "position 2";
        }
        else {
            selectedLocationA = selected;
            return "position 1";
        }
    }

    public Location getSelectedLocationA() {
        return selectedLocationA;
    }

    public void setSelectedLocationA(Location selectedLocationA) {
        this.selectedLocationA = selectedLocationA;
    }

    public Location getSelectedLocationB() {
        return selectedLocationB;
    }

    public void setSelectedLocationB(Location selectedLocationB) {
        this.selectedLocationB = selectedLocationB;
    }

    public UUID getID() {
        return ID;
    }

}

package net.elitemc.eliteteams.util.region.flags;

import net.elitemc.commons.util.MessageUtility;
import net.elitemc.eliteteams.util.region.FlagType;
import net.elitemc.eliteteams.util.region.Region;
import net.elitemc.eliteteams.util.region.RegionFlag;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * Created by LavaisWatery on 2017-07-31.
 */
public class FlowFlag extends RegionFlag {

    public FlowFlag(Region region) {
        super(region, FlagType.FLOW);
    }

    private boolean allowed = false;

    @Override
    public boolean allows() {
        return allowed;
    }

    @Override
    public String prettyItemDisplay() {
        return "Flow: " + (allowed ? "allow" : "deny");
    }

    @Override
    public boolean handleChange(CommandSender sender, String[] args) {
        Player player = (Player) sender;

        if(args.length != 0) {
            try {
                this.allowed = Boolean.parseBoolean(args[0]);
            } catch (Exception ex) {
                return false;
            }

            MessageUtility.message(player, false, "Changed " + getType().toString() + " flag to " + (allowed ? "allow" : "deny") + ".");
            return true;
        }

        return false;
    }

    @Override
    public String serialize() {
        return Boolean.toString(allowed);
    }

    @Override
    public void deserialize(Region region, String s) {
        setRegion(region);
        this.allowed = Boolean.parseBoolean(s);
    }

}

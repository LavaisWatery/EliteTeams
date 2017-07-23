package net.elitemc.eliteteams.command;

import net.elitemc.commons.util.MessageUtility;
import net.elitemc.commons.util.cmdfrmwrk.BaseCommand;
import net.elitemc.commons.util.cmdfrmwrk.CommandUsageBy;
import net.elitemc.eliteteams.handler.TeamsPlayerHandler;
import net.elitemc.eliteteams.util.TeamsPlayerWrapper;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * Created by LavaisWatery on 2017-07-22.
 */
public class Command_yes extends BaseCommand {

    public Command_yes() {
        super("yes", "teams.yes", CommandUsageBy.PLAYER);
        setUsage("/<command>");
        setArgRange(0, 0);
    }

    public void execute(CommandSender sender, String[] args) {
        Player player = (Player) sender;
        TeamsPlayerWrapper wrapper = TeamsPlayerHandler.getInstance().getPlayerWrapper(player);

        if(wrapper.getConfirmation() != null) {
            wrapper.getConfirmation().accept();
        }
        else {
            MessageUtility.message(player, false, ChatColor.RED + "You don't have a pending confirmation.");
        }
    }

}

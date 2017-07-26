package net.elitemc.eliteteams.command;

import net.elitemc.commons.util.cmdfrmwrk.BaseCommand;
import net.elitemc.commons.util.cmdfrmwrk.CommandUsageBy;
import net.elitemc.eliteteams.handler.OptionsHandler;
import net.elitemc.eliteteams.handler.TeamsPlayerHandler;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * Created by LavaisWatery on 2017-07-25.
 */
public class Command_options extends BaseCommand {

    public Command_options() {
        super("options", "teams.options", CommandUsageBy.PLAYER, new String[] { "settings" } );
        setUsage("/<command>");
        setArgRange(0, 0);
    }

    public void execute(CommandSender sender, String[] args) {
        Player player = (Player) sender;

        OptionsHandler.getInstance().openPlayerOptions(player, TeamsPlayerHandler.getInstance().getPlayerWrapper(player).getPlayerOptions());
    }

}

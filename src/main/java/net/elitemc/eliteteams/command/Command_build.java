package net.elitemc.eliteteams.command;

import net.elitemc.commons.util.MessageUtility;
import net.elitemc.commons.util.PlayerUtility;
import net.elitemc.commons.util.cmdfrmwrk.BaseCommand;
import net.elitemc.commons.util.cmdfrmwrk.CommandUsageBy;
import net.elitemc.eliteteams.handler.TeamsPlayerHandler;
import net.elitemc.eliteteams.util.TeamsPlayerWrapper;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * Created by LavaisWatery on 2017-07-25.
 */
public class Command_build extends BaseCommand {

    public Command_build() {
        super("build", "teams.build", CommandUsageBy.ALL);
        setUsage("/<command> [playerName]");
        setArgRange(0, 1);
    }

    public void execute(CommandSender sender, String[] args) {
        Player target = args.length > 0 ? Bukkit.getPlayer(args[0]) : null;
        boolean self = args.length == 0;

        if(args.length > 0 && !PlayerUtility.doesPlayerExist(sender, args[0])) {
            PlayerUtility.sendPlayerDoesntExist(sender, args[0]);
            return;
        }
        if(args.length == 0 && !(sender instanceof Player)) {
            mustExecuteByPlayer(sender);
            return;
        }
        else if(args.length == 0 && sender instanceof Player) {
            target = (Player) sender;
        }
        TeamsPlayerWrapper wrapper = TeamsPlayerHandler.getInstance().getPlayerWrapper(target);

        wrapper.setBuilding(!wrapper.isBuilding());
        MessageUtility.message(sender, false, (self ? "You have toggled build mode " : "You have toggled build mode for " + target.getName() + " ") + (wrapper.isBuilding() ? "on" : "off"));
    }

}

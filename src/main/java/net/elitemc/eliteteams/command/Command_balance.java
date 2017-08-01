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
 * Created by LavaisWatery on 2017-07-31.
 */
public class Command_balance extends BaseCommand {

    public Command_balance() {
        super("balance", "teams.balance", CommandUsageBy.PLAYER, new String[] { "money", "bal" });
        setUsage("/<command>");
        setArgRange(0, 0);
    }

    public void execute(CommandSender sender, String[] args) {
        Player player = (Player) sender;
        TeamsPlayerWrapper wrapper = TeamsPlayerHandler.getInstance().getPlayerWrapper(player);

        MessageUtility.message(player, false, ChatColor.DARK_AQUA + "Balance: " + wrapper.getBalance());
    }

}

package net.elitemc.eliteteams.command;

import net.elitemc.commons.util.cmdfrmwrk.BaseCommand;
import net.elitemc.commons.util.cmdfrmwrk.CommandUsageBy;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * Created by LavaisWatery on 2017-08-09.
 */
public class Command_sell extends BaseCommand {

    public Command_sell() {
        super("sell", "teams.sell", CommandUsageBy.PLAYER);
        setUsage("/<command> [itemName] [amount]");
        setArgRange(2, 3);
    }

    public void execute(CommandSender sender, String[] args) {
        Player player = (Player) sender;


    }

}

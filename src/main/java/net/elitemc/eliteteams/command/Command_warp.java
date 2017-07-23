package net.elitemc.eliteteams.command;

import net.elitemc.commons.util.MessageUtility;
import net.elitemc.commons.util.cmdfrmwrk.BaseCommand;
import net.elitemc.commons.util.cmdfrmwrk.CommandUsageBy;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * Created by LavaisWatery on 2017-07-22.
 */
public class Command_warp extends BaseCommand {

    public Command_warp() {
        super("warp", "teams.warp", CommandUsageBy.PLAYER, new String[] { "go" });
        setUsage("/<command> [...]");
        setArgRange(0, 10);
    }

    private String[] usage = { ChatColor.GOLD + "Arena Usage",
            ChatColor.GRAY + "- createarena [indexed]",
            ChatColor.GRAY + "- createsubinfo [indexed]",
            ChatColor.GRAY + "- selectarena [indexed]",
            ChatColor.GRAY + "- selectsubinfo [indexed]",
            ChatColor.GRAY + "- selectgroup [indexed]",
            ChatColor.GRAY + "- change [...]",
            ChatColor.GRAY + "- view",
            ChatColor.GRAY + "- viewcategory [category]" };

    public void execute(CommandSender sender, String[] args) {
        Player player = (Player) sender;

        if(args.length == 0) {
            MessageUtility.sendList(sender, usage);

            return;
        }

        switch(args[0].toLowerCase()) {
            
            default: {
                MessageUtility.sendList(sender, usage);

                break;
            }
        }

    }

}

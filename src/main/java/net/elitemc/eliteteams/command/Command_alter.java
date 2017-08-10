package net.elitemc.eliteteams.command;

import net.elitemc.commons.util.MessageUtility;
import net.elitemc.commons.util.StringUtility;
import net.elitemc.commons.util.cmdfrmwrk.BaseCommand;
import net.elitemc.commons.util.cmdfrmwrk.CommandUsageBy;
import net.elitemc.eliteteams.util.IAlterItem;
import net.elitemc.eliteteams.util.alter.StatAlter;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

import java.util.HashMap;

/**
 * Created by LavaisWatery on 2017-08-09.
 */
public class Command_alter extends BaseCommand {

    public Command_alter() {
        super("alter", "kitpvp.alter", CommandUsageBy.ALL);
        setUsage("/<command> [...]");
        setArgRange(0, 20);
    }

    public static HashMap<String, IAlterItem> alterHash = new HashMap<>();

    static {
        registerItem(new StatAlter());
    }

    public void execute(CommandSender sender, String[] args) {
        if(args.length == 0) {
            sendItems(sender);

            return;
        }
        IAlterItem item = getItemAlter(args[0]);

        if(item == null) {
            MessageUtility.message(sender, false, ChatColor.RED + "Choose one of these items to change.");
            sendItems(sender);
            return;
        }

        item.handleChange(sender, StringUtility.trimList(args, 1));
    }

    public static void sendItems(CommandSender sender) {
        StringBuilder builder = new StringBuilder();

        if(!alterHash.isEmpty()) {
            for (String type : alterHash.keySet()) {
                if (builder.length() == 0) {
                    builder.append(type.toString());
                } else {
                    builder.append(", " + type.toString());
                }
            }
        }

        MessageUtility.message(sender, false, builder.toString());
    }

    public static IAlterItem getItemAlter(String index) {
        return alterHash.get(index.toLowerCase());
    }

    public static void registerItem(IAlterItem item) {
        alterHash.put(item.getItemIndex().toLowerCase(), item);
    }


}

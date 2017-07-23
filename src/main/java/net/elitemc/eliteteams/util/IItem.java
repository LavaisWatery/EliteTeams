package net.elitemc.eliteteams.util;

import org.bukkit.command.CommandSender;

/**
 * Created by Kyle Gosleigh on 5/25/2017.
 */
public interface IItem {

    String prettyItemDisplay();

    boolean handleChange(CommandSender sender, String[] args);

}


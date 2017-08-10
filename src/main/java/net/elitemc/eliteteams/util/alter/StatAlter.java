package net.elitemc.eliteteams.util.alter;

import net.elitemc.commons.util.MessageUtility;
import net.elitemc.commons.util.wrapper.MongoDataObjectException;
import net.elitemc.eliteteams.EliteTeams;
import net.elitemc.eliteteams.handler.TeamsPlayerHandler;
import net.elitemc.eliteteams.util.IAlterItem;
import net.elitemc.eliteteams.util.TeamsPlayerWrapper;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.scheduler.BukkitRunnable;

/**
 * Created by LavaisWatery on 2017-08-09.
 */
public class StatAlter extends IAlterItem {

    public StatAlter() {
        super("statchange");
    }

    public String[] snapshotUsage = { ChatColor.GOLD + "StatChange [player] ",
            ChatColor.GRAY + "- kills",
            ChatColor.GRAY + "- deaths",
            ChatColor.GRAY + "- currentStreak",
            ChatColor.GRAY + "- topStreak",
            ChatColor.GRAY + "- balance",
            ChatColor.GRAY + "- basicKeys",
            ChatColor.GRAY + "- omegaKeys",
            ChatColor.GRAY + "- eventWins",
            ChatColor.GRAY + "- 1v1Wins",
            ChatColor.GRAY + "- 1v1Loses",
            ChatColor.GRAY + "- pvpRank",
    };

    @Override
    public boolean handleChange(CommandSender sender, String[] args) {
        if(args.length == 0) {
            MessageUtility.sendList(sender, snapshotUsage);

            return false;
        }

        new BukkitRunnable() {
            @Override
            public void run() {
                OfflinePlayer player = Bukkit.getOfflinePlayer(args[0]);

                MessageUtility.message(sender, false, player.getName());
                if(player.hasPlayedBefore()) {
                    try {
                        TeamsPlayerWrapper wrapper = TeamsPlayerHandler.getInstance().getPlayerWrapper(player.getUniqueId(), true);

                        switch (args[1].toLowerCase()) {
                            case "kills": {
                                if(args.length < 2) {
                                    MessageUtility.message(sender, false, ChatColor.RED + "You must input an amount");
                                    return;
                                }
                                try {
                                    wrapper.setKills(Integer.parseInt(args[2]));
                                    MessageUtility.message(sender, false, ChatColor.GRAY + "Set kills to " + wrapper.getKills() + ".");
                                } catch (Exception ex) {
                                    MessageUtility.message(sender, false, ChatColor.RED + "Couldn't parse amount.");
                                }
                                break;
                            }
                            case "deaths": {
                                if(args.length < 2) {
                                    MessageUtility.message(sender, false, ChatColor.RED + "You must input an amount");
                                    return;
                                }
                                try {
                                    wrapper.setDeaths(Integer.parseInt(args[2]));
                                    MessageUtility.message(sender, false, ChatColor.GRAY + "Set deaths to " + wrapper.getDeaths() + ".");
                                } catch (Exception ex) {
                                    MessageUtility.message(sender, false, ChatColor.RED + "Couldn't parse amount.");
                                }
                                break;
                            }
                            case "currentstreak": {
                                if(args.length < 2) {
                                    MessageUtility.message(sender, false, ChatColor.RED + "You must input an amount");
                                    return;
                                }
                                try {
                                    wrapper.setCurrent_killstreak(Integer.parseInt(args[2]));
                                    MessageUtility.message(sender, false, ChatColor.GRAY + "Set current streak to " + wrapper.getCurrent_killstreak() + ".");
                                } catch (Exception ex) {
                                    MessageUtility.message(sender, false, ChatColor.RED + "Couldn't parse amount.");
                                }
                                break;
                            }
                            case "topstreak": {
                                if(args.length < 2) {
                                    MessageUtility.message(sender, false, ChatColor.RED + "You must input an amount");
                                    return;
                                }
                                try {
                                    wrapper.setTop_killstreak(Integer.parseInt(args[2]));
                                    MessageUtility.message(sender, false, ChatColor.GRAY + "Set top streak to " + wrapper.getTop_killstreak() + ".");
                                } catch (Exception ex) {
                                    MessageUtility.message(sender, false, ChatColor.RED + "Couldn't parse amount.");
                                }
                                break;
                            }
                            case "balance": {
                                if(args.length < 2) {
                                    MessageUtility.message(sender, false, ChatColor.RED + "You must input an amount");
                                    return;
                                }
                                try {
                                    wrapper.setBalance(Integer.parseInt(args[2]));
                                    MessageUtility.message(sender, false, ChatColor.GRAY + "Set balance to " + wrapper.getBalance() + ".");
                                } catch (Exception ex) {
                                    MessageUtility.message(sender, false, ChatColor.RED + "Couldn't parse amount.");
                                }
                                break;
                            }
                            case "balance(add)": {
                                if(args.length < 2) {
                                    MessageUtility.message(sender, false, ChatColor.RED + "You must input an amount");
                                    return;
                                }
                                try {
                                    wrapper.setBalance(wrapper.getBalance() + Integer.parseInt(args[2]));
                                    MessageUtility.message(sender, false, ChatColor.GRAY + "Set balance to " + wrapper.getBalance() + ".");
                                } catch (Exception ex) {
                                    MessageUtility.message(sender, false, ChatColor.RED + "Couldn't parse amount.");
                                }
                                break;
                            }
                            default: {
                                MessageUtility.sendList(sender, snapshotUsage);

                                break;
                            }
                        }

                    } catch (MongoDataObjectException ex) {
                        MessageUtility.message(sender, false, ChatColor.DARK_RED + ex.getMessage());
                    }
                }
                else {
                    MessageUtility.message(sender, false, "This player hasn't played before:3");
                }

            }
        }.runTaskAsynchronously(EliteTeams.getInstance());

        return true;
    }

}

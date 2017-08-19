package net.elitemc.eliteteams.command;

import net.elitemc.commons.util.MessageUtility;
import net.elitemc.commons.util.cmdfrmwrk.BaseCommand;
import net.elitemc.commons.util.cmdfrmwrk.CommandUsageBy;
import net.elitemc.eliteteams.EliteTeams;
import net.elitemc.eliteteams.handler.TeamsPlayerHandler;
import net.elitemc.eliteteams.util.TeamsPlayerWrapper;
import net.elitemc.eliteteams.util.warp.PlayerWarp;
import net.elitemc.eliteteams.util.warp.PlayerWarps;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.stream.Collectors;

/**
 * Created by LavaisWatery on 2017-08-17.
 */
public class Command_goas extends BaseCommand {

    public Command_goas() {
        super("goas", "kitpvp.goas", CommandUsageBy.PLAYER);
        setUsage("/<command> <playerName> [warpName:set:remove:list]");
        setArgRange(1, 3);
    }

    public void execute(CommandSender sender, String[] args) {
        Player player = (Player) sender;
        OfflinePlayer target = Bukkit.getOfflinePlayer(args[0]);

        if(target != null) {
            new BukkitRunnable() {
                @Override
                public void run() {
                    try {
                        TeamsPlayerWrapper wrapper = TeamsPlayerHandler.getInstance().getPlayerWrapper(target.getUniqueId(), true);

                        if(wrapper.isLoaded() && wrapper.getPlayerWarps() != null) {
                            PlayerWarps warps = wrapper.getPlayerWarps();

                            if(args.length < 2) {
                                displayWarps(player, target, warps);
                            }
                            else {
                                switch(args[1].toLowerCase()) {
                                    case "set": {
                                        if(args.length < 3) {
                                            MessageUtility.message(player, false, "You must input a valid warp");
                                            return;
                                        }
                                        warps.forceOverrideWarp(player, args[2]);

                                        break;
                                    }
                                    case "remove": {
                                        if(args.length < 3) {
                                            MessageUtility.message(player, false, "You must input a valid warp");
                                            return;
                                        }
                                        PlayerWarp warp = warps.getPlayerWarps().get(args[2].toLowerCase());

                                        if(warp == null) {
                                            MessageUtility.message(player, false, ChatColor.RED + "You must input a warp.");
                                            displayWarps(player, target, warps);
                                            return;
                                        }

                                        if(warp.isDestroyed()) {
                                            MessageUtility.message(player, false, ChatColor.RED + "This warp is already destroyed.");
                                            return;
                                        }

                                        if(warps.getPlayerWarps().containsValue(warp)) {
                                            warps.removeWarp(warp);
                                            MessageUtility.message(player, false, "removed warp " + warp.getWarpName());
                                        }

                                        break;
                                    }
                                    case "list": {
                                        displayWarps(player, target, wrapper.getPlayerWarps());
                                        break;
                                    }
                                    default: {
                                        PlayerWarp warp = warps.getPlayerWarps().get(args[1].toLowerCase());

                                        if(warp == null) {
                                            MessageUtility.message(player, false, ChatColor.RED + "You must input a valid warp.");
                                            return;
                                        }

                                        if(warp.isDestroyed()) {
                                            MessageUtility.message(player, false, ChatColor.RED + "This warp is already destroyed.");
                                            return;
                                        }

                                        if(warps.getPlayerWarps().containsValue(warp)) {
                                            player.teleport(warp.toLocation());
                                            MessageUtility.message(player, false, ChatColor.GRAY + "Warped to " + target.getName() + "'s warp " + warp.getWarpName());
                                        }

                                        break;
                                    }
                                }
                            }
                        }
                        else {
                            MessageUtility.message(player, false, ChatColor.RED + "Data not loaded");
                        }
                    } catch (Exception ex) {
                        ex.printStackTrace();
                        MessageUtility.message(player, false, ChatColor.RED + "Unable to check warps.");
                    }


                }
            }.runTaskAsynchronously(EliteTeams.getInstance());
        }
    }

    private void displayWarps(CommandSender sender, OfflinePlayer target, PlayerWarps warps) {
        MessageUtility.message(sender, false, target.getName() + "'s Warps(" + warps.getPlayerWarps().size() + "): " + warps.getPlayerWarps().values().stream().map(PlayerWarp::getWarpName).collect(Collectors.joining(", ")));
    }

}

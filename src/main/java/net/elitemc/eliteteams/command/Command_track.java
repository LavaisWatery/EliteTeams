package net.elitemc.eliteteams.command;

import net.elitemc.commons.util.FakeLocation;
import net.elitemc.commons.util.MessageUtility;
import net.elitemc.commons.util.PlayerUtility;
import net.elitemc.commons.util.cmdfrmwrk.BaseCommand;
import net.elitemc.commons.util.cmdfrmwrk.CommandUsageBy;
import net.elitemc.eliteteams.util.TrackUtility;
import net.elitemc.eliteteams.util.TrackingArm;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.AbstractMap;
import java.util.List;
import java.util.UUID;

/**
 * Created by LavaisWatery on 2017-07-22.
 */
public class Command_track extends BaseCommand {

    public Command_track() {
        super("track", "teams.track", CommandUsageBy.PLAYER);
        setUsage("/<command>");
        setArgRange(0, 10);
    }

    private String[] usage = {ChatColor.DARK_AQUA.toString() + ChatColor.BOLD + "Tracking Help",
            ChatColor.GRAY.toString() + ChatColor.STRIKETHROUGH + "---------------------------",
            ChatColor.DARK_AQUA + "/track help " + ChatColor.GRAY + "- Shows warp help.",
            ChatColor.DARK_AQUA + "/track " + ChatColor.AQUA + "<playerName> " + ChatColor.GRAY + "- Track a specific player.",
            ChatColor.DARK_AQUA + "/track all " + ChatColor.GRAY + "- Track everyone.",
            ChatColor.GRAY.toString() + ChatColor.STRIKETHROUGH + "---------------------------"
    };

    public void execute(CommandSender sender, String[] args) {
        Player player = (Player) sender;

        if(args.length == 0) {
            MessageUtility.sendList(sender, usage);

            return;
        }

        switch(args[0].toLowerCase()) {
            case "all": {
                FakeLocation current = new FakeLocation(player.getLocation().clone().subtract(0, 1, 0));
                Block curBlock = current.toLocation().getBlock();

                if(curBlock.getType() == Material.DIAMOND_BLOCK) {
                    for (TrackingArm arm : TrackingArm.values()) {
                        StringBuilder players = new StringBuilder();
                        int matched = TrackUtility.matchBlocks(current, arm, Material.OBSIDIAN, Material.GOLD_BLOCK);

                        if (matched > 0) {
                            AbstractMap.SimpleEntry<Integer, Integer> dist = arm.getDist(matched);

                            List<UUID> found = TrackUtility.trackDir(player, current, arm, dist.getKey(), dist.getValue(), null);

                            for (UUID f : found) {
                                Player ff = Bukkit.getPlayer(f);

                                if (ff != null) {
                                    players.append(players.length() == 0 ? ff.getName() : ", " + ff.getName());
                                }
                            }
                        }

                        MessageUtility.message(player, false, arm.toString() + "(" + (matched * 25) + "): " + players.toString());
                    }
                }

            }
            default: {
                if(PlayerUtility.doesPlayerExist(sender, args[0])) {
                    Player target = Bukkit.getPlayer(args[0]);
                    FakeLocation current = new FakeLocation(player.getLocation().clone().subtract(0, 1, 0));
                    Block curBlock = current.toLocation().getBlock();

                    if(curBlock.getType() == Material.DIAMOND_BLOCK) {
                        for(TrackingArm arm : TrackingArm.values()) {
                            StringBuilder players = new StringBuilder();
                            int matched = TrackUtility.matchBlocks(current, arm, Material.OBSIDIAN, Material.GOLD_BLOCK);

                            if(matched > 0) {
                                AbstractMap.SimpleEntry<Integer, Integer> dist = arm.getDist(matched);

                                List<UUID> found = TrackUtility.trackDir(player, current, arm, dist.getKey(), dist.getValue(), target);

                                for(UUID f : found) {
                                    Player ff = Bukkit.getPlayer(f);

                                    if(ff != null) {
                                        players.append(players.length() == 0 ? ff.getName() : ", " + ff.getName());
                                    }
                                }
                            }

                            MessageUtility.message(player, false, arm.toString() + "(" + (matched * 25) + "): " + players.toString());
                        }
                    }
                    else if(curBlock.getType() == Material.OBSIDIAN) {
                        boolean mark = false;
                        for(TrackingArm arm : TrackingArm.values()) {
                            StringBuilder players = new StringBuilder();
                            int matched = TrackUtility.tempMatchBlocks(current, arm, Material.COBBLESTONE, Material.STONE);

                            if(matched > 0) {
                                mark = true;
                                AbstractMap.SimpleEntry<Integer, Integer> dist = arm.getDist(matched);

                                List<UUID> found = TrackUtility.trackDir(player, current, arm, dist.getKey(), dist.getValue(), target);

                                for(UUID f : found) {
                                    Player ff = Bukkit.getPlayer(f);

                                    if(ff != null) {
                                        players.append(players.length() == 0 ? ff.getName() : ", " + ff.getName());
                                    }
                                }
                            }

                            MessageUtility.message(player, false, arm.toString() + "(" + (matched * 25) + "): " + players.toString());
                        }
                        if(mark) current.toLocation().getBlock().setType(Material.AIR);
                    }
                }
                else {
                    MessageUtility.sendList(sender, usage);
                }

                break;
            }
        }
    }

}

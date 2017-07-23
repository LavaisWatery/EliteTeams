package net.elitemc.eliteteams.command;

import net.elitemc.commons.util.Cuboid;
import net.elitemc.commons.util.MessageUtility;
import net.elitemc.commons.util.NumberUtility;
import net.elitemc.commons.util.StringUtility;
import net.elitemc.commons.util.cmdfrmwrk.BaseCommand;
import net.elitemc.commons.util.cmdfrmwrk.CommandUsageBy;
import net.elitemc.eliteteams.EliteTeams;
import net.elitemc.eliteteams.handler.RegionHandler;
import net.elitemc.eliteteams.handler.TeamsPlayerHandler;
import net.elitemc.eliteteams.util.TeamsPlayerWrapper;
import net.elitemc.eliteteams.util.region.Region;
import net.elitemc.eliteteams.util.region.RegionSession;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Created by LavaisWatery on 2017-07-22.
 */
public class Command_sets extends BaseCommand {

    public Command_sets() {
        super("sets", "kitpvp.sets", CommandUsageBy.PLAYER);
        setUsage("/<command> [...]");
        setArgRange(0, 50);
    }

    private String[] usage = { ChatColor.GOLD + "Sets Usage",
            ChatColor.GRAY + "- toggleset",
            ChatColor.GRAY + "- edit [index] [...]",
            ChatColor.GRAY + "- claimset [index] [TYPE]",
            ChatColor.GRAY + "- pos1 // pos2",
            ChatColor.GRAY + "- expand [up/down] [amount]"
    };

    private RegionHandler handler = RegionHandler.getInstance();

    public void execute(CommandSender sender, String[] args) {
        Player player = (Player) sender;
        TeamsPlayerWrapper wrapper = TeamsPlayerHandler.getInstance().getPlayerWrapper(player);

        if(args.length == 0) {
            MessageUtility.sendList(player, usage);

            return;
        }

        switch(args[0].toLowerCase()) {
            case "toggleset": {
                wrapper.setRegionSession(wrapper.getRegionSession() != null ? null : new RegionSession(player));
                MessageUtility.message(player, false, "You have toggled region setting " + (wrapper.getRegionSession() != null ? "on" : "off"));

                break;
            }
            case "edit" : {
                if(args.length < 1) {
                    MessageUtility.message(player, false, "You must input an index");
                    return;
                }
                String index = args[1];
                Region region = handler.getRegionByIndex(index);

                if(region == null) {
                    MessageUtility.message(player, false, ChatColor.RED + "A region by this name doesn't exist.");
                    return;
                }

                region.handleChange(player, StringUtility.trimList(args, 2));

                new BukkitRunnable() {
                    @Override
                    public void run() {
                        handler.getConfiguration().saveConfig(handler.getRegions());
                    }
                }.runTaskAsynchronously(EliteTeams.getInstance());

                break;
            }
            case "pos1": {
                if(wrapper.getRegionSession() == null) {
                    MessageUtility.message(player, false, ChatColor.RED + "You must turn on region claiming.");

                    return;
                }
                RegionSession session = wrapper.getRegionSession();

                session.selectPosition(Action.LEFT_CLICK_BLOCK, player.getLocation());
                MessageUtility.message(player, false, ChatColor.YELLOW + "You have selected pos1 at point " + player.getLocation().getX() + ", " + player.getLocation().getY() + ", " + player.getLocation().getZ());

                break;
            }
            case "pos2": {
                if(wrapper.getRegionSession() == null) {
                    MessageUtility.message(player, false, ChatColor.RED + "You must turn on region claiming.");

                    return;
                }
                RegionSession session = wrapper.getRegionSession();

                session.selectPosition(Action.RIGHT_CLICK_BLOCK, player.getLocation());
                MessageUtility.message(player, false, ChatColor.YELLOW + "You have selected pos2 at point " + player.getLocation().getX() + ", " + player.getLocation().getY() + ", " + player.getLocation().getZ());

                break;
            }
            case "expand": {
                if(wrapper.getRegionSession() == null) {
                    MessageUtility.message(player, false, ChatColor.RED + "You must turn on region claiming.");

                    return;
                }
                RegionSession session = wrapper.getRegionSession();

                if(session.getSelectedLocationA() == null || session.getSelectedLocationB() == null) {
                    MessageUtility.message(player, false, ChatColor.RED + "You must select your cuboid before expanding.");
                    return;
                }
                if(args.length <= 2) {
                    MessageUtility.message(player, false, ChatColor.RED + "Expand Types: " + Arrays.stream(Cuboid.CuboidDirection.values()).map(Cuboid.CuboidDirection::toString).collect(Collectors.joining(",")));

                    return;
                }
                if(!NumberUtility.isNumber(args[2])) {
                    MessageUtility.message(player, false, ChatColor.RED + "Amount must be a number.");
                    return;
                }

                int amount = Integer.parseInt(args[2]);

                Cuboid.CuboidDirection dir = null;

                try {
                    dir = Cuboid.CuboidDirection.valueOf(args[1].toUpperCase());
                } catch (Exception ex) {
                    MessageUtility.message(player, false, ChatColor.RED + "Unable to find direction?? ");
                    return;
                }

                if(dir == null) return;

                Cuboid cub = new Cuboid(session.getSelectedLocationA(), session.getSelectedLocationB());

                cub = cub.expand(dir, amount);

                session.setSelectedLocationA(cub.getLowerNE());
                session.setSelectedLocationB(cub.getUpperSW());

                MessageUtility.message(player, false, "Expanded cuboid " + dir.toString() + " by " + amount);

//                if(args[1].equalsIgnoreCase("up") || args[1].equalsIgnoreCase("down")) {
//                    if (args[1].equalsIgnoreCase("up")) {
//                        Location location = session.getSelectedLocationA().getY() > session.getSelectedLocationB().getY() ? session.getSelectedLocationA() : session.getSelectedLocationB();
//
//                        location.setY(location.getY() + amount);
//                    } else if (args[1].equalsIgnoreCase("down")) {
//                        Location location = session.getSelectedLocationA().getY() < session.getSelectedLocationB().getY() ? session.getSelectedLocationA() : session.getSelectedLocationB();
//
//                        location.setY(location.getY() - amount);
//                    }
//                    MessageUtility.message(player, false, "Expanded region " + args[1] + " " + amount + " blocks.");
//                }

                break;
            }
            case "claimset": {
                if(wrapper.getRegionSession() == null) {
                    MessageUtility.message(player, false, ChatColor.RED + "You must turn on region claiming.");

                    return;
                }
                RegionSession session = wrapper.getRegionSession();

                if(args.length < 1) {
                    MessageUtility.message(player, false, "You must input an index");
                    return;
                }
                if(session.getSelectedLocationA() == null || session.getSelectedLocationB() == null) {
                    MessageUtility.message(player, false, ChatColor.RED + "You must select both locations to claim a set.");
                    return;
                }
                String index = args[1];
                Region.RegionType type = null;

                if(args.length >= 2) {
                    try {
                        type = Region.RegionType.valueOf(args[2]);
                    } catch (Exception ex) {
                        MessageUtility.message(player, false, ChatColor.RED + "Region Types: " + Region.RegionType.toPrettyList());
                    }
                }
                if(handler.getRegionByIndex(index) != null) {
                    MessageUtility.message(player, false, ChatColor.RED + "A region by this name already exists.");

                    return;
                }
                Region region = new Region(index, session.getSelectedLocationA(), session.getSelectedLocationB());

                if(type != null) {
                    region.setType(type);
                }

                handler.registerRegion(region);
                MessageUtility.message(player, false, ChatColor.RED + "Claimed region " + index + (type != null ? " with type " + type.toString() : ""));
                new BukkitRunnable() {
                    @Override
                    public void run() {
                        handler.getConfiguration().saveConfig(handler.getRegions());
                    }
                }.runTaskAsynchronously(EliteTeams.getInstance());
                break;
            }
            case "info": {
                Set<Region> applicable = handler.getRegionsApplicable(player);

                if(applicable == null) {
                    MessageUtility.message(player, false, ChatColor.RED + "You aren't inside of any regions.");
                    return;
                }

                MessageUtility.message(player, false, ChatColor.YELLOW + "You are inside " + applicable.size() + " regions: " + Region.RegionType.toPrettyList(applicable));

                break;
            }
        }

    }

}

package net.elitemc.eliteteams.command;

import mkremins.fanciful.FancyMessage;
import net.elitemc.commons.util.LocationUtility;
import net.elitemc.commons.util.MessageUtility;
import net.elitemc.commons.util.cmdfrmwrk.BaseCommand;
import net.elitemc.commons.util.cmdfrmwrk.CommandUsageBy;
import net.elitemc.eliteteams.handler.TeamsPlayerHandler;
import net.elitemc.eliteteams.util.Confirmation;
import net.elitemc.eliteteams.util.warp.PlayerWarp;
import net.elitemc.eliteteams.util.warp.PlayerWarps;
import net.elitemc.eliteteams.util.TeamsPlayerWrapper;
import net.elitemc.origin.util.object.TeleportTarget;
import net.elitemc.origin.util.runnable.Teleporter;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.stream.Collectors;

/**
 * Created by LavaisWatery on 2017-07-22.
 */
public class Command_warp extends BaseCommand {

    public Command_warp() {
        super("warp", "teams.warp", CommandUsageBy.PLAYER, new String[] { "go" });
        setUsage("/<command> [...]");
        setArgRange(0, 10);
    }

    private String[] usage = {ChatColor.DARK_AQUA.toString() + ChatColor.BOLD + "Warps Help",
            ChatColor.GRAY.toString() + ChatColor.STRIKETHROUGH + "---------------------------",
            ChatColor.DARK_AQUA + "/warp help " + ChatColor.GRAY + "- Shows warp help.",
            ChatColor.DARK_AQUA + "/warp list " + ChatColor.GRAY + "- List all your warps.",
            ChatColor.DARK_AQUA + "/warp set " + ChatColor.AQUA + "<warpName> " + ChatColor.GRAY + "- Set a warp at your current location.",
            ChatColor.DARK_AQUA + "/warp [remove/delete] " + ChatColor.AQUA + "<warpName> " + ChatColor.GRAY + "- Remove a warp.",
            ChatColor.DARK_AQUA + "Use " + ChatColor.AQUA + "/settings " + ChatColor.DARK_AQUA + "to toggle to a " + ChatColor.AQUA + "GUI" + ChatColor.DARK_AQUA + ".",
            ChatColor.GRAY.toString() + ChatColor.STRIKETHROUGH + "---------------------------"
    };

    public void execute(CommandSender sender, String[] args) {
        Player player = (Player) sender;

        if(args.length == 0) {
            MessageUtility.sendList(sender, usage);

            return;
        }
        TeamsPlayerWrapper wrapper = TeamsPlayerHandler.getInstance().getPlayerWrapper(player);

        if(wrapper.getPlayerWarps() == null) {
            MessageUtility.message(player, false, ChatColor.RED + "Your profile isn't loaded currently.");
            return;
        }

        switch(args[0].toLowerCase()) {
            case "set": {
                PlayerWarps warps = wrapper.getPlayerWarps();

                warps.addPlayerWarp(player, args[1]);

                break;
            }
            case "delete":
            case "remove":
            {
                PlayerWarps warps = wrapper.getPlayerWarps();
                PlayerWarp warp = warps.getPlayerWarp(args[1]);

                if(warp != null) {
                    new FancyMessage("Confirm").color(ChatColor.DARK_GREEN).command("/yes").then(" ").then("Deny").color(ChatColor.DARK_RED).command("/no").send(player);
                    wrapper.setConfirmation(new Confirmation(wrapper, "removal") {
                        @Override
                        public void accept() {
                            if(warp.isDestroyed()) {
                                MessageUtility.message(player, false, ChatColor.RED + "This warp is already destroyed.");
                                return;
                            }

                            if(warps.getPlayerWarps().containsValue(warp)) {
                                warps.removeWarp(warp);
                                MessageUtility.message(player, false, "removed warp " + warp.getWarpName());
                            }

                            clean();
                        }

                        @Override
                        public void deny() {

                            clean();
                        }
                    });
                }
                else {
                    MessageUtility.message(player, false, PlayerWarps.WARP_DOESNT_EXIST);
                }

                break;
            }
            case "list":
            case "view":
            {
                PlayerWarps warps = wrapper.getPlayerWarps();

                MessageUtility.message(player, false, "Warps(" + warps.getPlayerWarps().size() + "/" + wrapper.getMax_warps() + "): " + warps.getPlayerWarps().values().stream().map(PlayerWarp::getWarpName).collect(Collectors.joining(", ")));

                break;
            }
            case "help": {
                MessageUtility.sendList(sender, usage);

                break;
            }
            default: {
                PlayerWarps warps = wrapper.getPlayerWarps();
                PlayerWarp warp = warps.getPlayerWarp(args[0]);

                if(warp != null) {
                    wrapper.getOriginWrapper().doTeleportWithCheck(new Teleporter(wrapper.getOriginWrapper(), (long)(10 * 20), Teleporter.TeleportType.LOCATION_TARGET, new TeleportTarget(warp.toLocation()), "test") {
                        @Override
                        public void doIt() {
                            LocationUtility.assureChunk(getTarget().getTargetLocation());
                            getWrapper().getPlayer().teleport(getTarget().getTargetLocation());
                            wrapper.doProtectionApplyCheck(player, getTarget().getTargetLocation());
                            MessageUtility.message(getWrapper().getPlayer(), false, "complete");
                        }
                    });
                }
                else {
                    MessageUtility.sendList(sender, usage);
                }

                break;
            }
        }

    }

}

package net.elitemc.eliteteams.command;

import net.elitemc.commons.handler.PlayerHandler;
import net.elitemc.commons.util.*;
import net.elitemc.commons.util.cmdfrmwrk.BaseCommand;
import net.elitemc.commons.util.cmdfrmwrk.CommandFlag;
import net.elitemc.commons.util.cmdfrmwrk.CommandUsageBy;
import net.elitemc.eliteteams.handler.RegionHandler;
import net.elitemc.eliteteams.handler.TeamsPlayerHandler;
import net.elitemc.eliteteams.util.TeamsPlayerWrapper;
import net.elitemc.eliteteams.util.region.FlagType;
import net.elitemc.origin.Init;
import net.elitemc.origin.Origin;
import net.elitemc.origin.OriginConfiguration;
import net.elitemc.origin.handler.WarpHandler;
import net.elitemc.origin.util.OriginPlayerWrapper;
import net.elitemc.origin.util.event.PlayerSpawnEvent;
import net.elitemc.origin.util.object.TeleportTarget;
import net.elitemc.origin.util.runnable.Teleporter;
import org.bukkit.*;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.HashMap;

/**
 * Created by LavaisWatery on 2017-07-22.
 */
public class Command_spawn extends BaseCommand {

    public Command_spawn() {
        super("spawn", "teams.spawn", CommandUsageBy.PLAYER);
        setUsage("/<command> [player] [-world=]");
        setArgRange(0, 2);
    }

    private String spawn_complete = Init.getInstance().getLanguageFile().SPAWN_COMPLETE, warmup = Init.getInstance().getLanguageFile().TELEPORT_WARMUP;

    public void execute(CommandSender sender, String[] args) {
        String[] filtered = filterFlags(args);
        if(filtered.length > 0 && !sender.hasPermission("teams.spawn.others")) {
            MessageUtility.noPermission(sender);

            return;
        }
        HashMap<String, CommandFlag> flags = getFlags(args);
        Player target = filtered.length > 0 ? Bukkit.getPlayer(filtered[0]) : null;
        boolean self = filtered.length == 0;

        if(filtered.length > 0 && !PlayerUtility.doesPlayerExist(sender, filtered[0])) {
            PlayerUtility.sendPlayerDoesntExist(sender, filtered[0]);
            return;
        }
        if(filtered.length == 0 && !(sender instanceof Player)) {
            mustExecuteByPlayer(sender);
            return;
        }
        else if(filtered.length == 0 && sender instanceof Player) {
            target = (Player) sender;
        }
        World world = OriginConfiguration.DEFAULT_WORLD.isEmpty() ? target.getWorld() : target.getServer().getWorld(OriginConfiguration.DEFAULT_WORLD) != null ? target.getServer().getWorld(OriginConfiguration.DEFAULT_WORLD) : target.getWorld();

        if(flags.containsKey("world")) {
            CommandFlag flag = flags.get("world");

            if(Bukkit.getWorld(flag.getVariable()) != null) {
                world = Bukkit.getWorld(flag.getVariable());
            }
            else {
                MessageUtility.message(sender, false, ChatColor.RED + "Invalid world in flag, sending to players current world spawn.");
            }
        }

        if(!LocationUtility.hasAnyBlockUnder(target.getLocation(), 25) && target.getGameMode() == GameMode.SURVIVAL) {
            MessageUtility.message(sender, false, ChatColor.RED + "There's currently no blocks under you.");
            return;
        }

        TeamsPlayerWrapper wrapper = TeamsPlayerHandler.getInstance().getPlayerWrapper(target);
        OriginPlayerWrapper playerWrapper = wrapper.getOriginWrapper();
        Location teleport = WarpHandler.getInstance().getSpawnWarps().containsKey(world.getName()) ? WarpHandler.getInstance().getSpawnWarps().get(world.getName()).getWarpLocation().toLocation() : toHighest(world.getSpawnLocation());
        long time = WarpHandler.getInstance().getSpawnWarps().containsKey(world.getName()) && WarpHandler.getInstance().getSpawnWarps().get(world.getName()).getWarpTime() > -1 ? WarpHandler.getInstance().getSpawnWarps().get(world.getName()).getWarpTime() : OriginConfiguration.TELEPORT_TIME;
        final World finWorld = world;

        if(target == sender) {
            playerWrapper.doTeleportWithCheck(new Teleporter(playerWrapper, (long)(time * 20), Teleporter.TeleportType.LOCATION_TARGET, new TeleportTarget(teleport), LanguageFile.replaceVariable(LanguageFile.replaceVariable(warmup, 0, "spawn"), 1, time + " seconds")) {
                @Override
                public void doIt() {
                    LocationUtility.assureChunk(getTarget().getTargetLocation());
                    getWrapper().getPlayer().teleport(getTarget().getTargetLocation());
                    wrapper.doProtectionApplyCheck(getWrapper().getPlayer(), getTarget().getTargetLocation());
                    MessageUtility.message(getWrapper().getPlayer(), false, LanguageFile.replaceVariable(spawn_complete, 0, finWorld.getName()));
                    PluginUtility.callEvent(new PlayerSpawnEvent(getWrapper().getPlayer(), getTarget().getTargetLocation()));
                }
            });
        }
        else {
            LocationUtility.assureChunk(teleport);
            target.teleport(teleport);
            wrapper.doProtectionApplyCheck(target, teleport);
            MessageUtility.message(sender, false, ChatColor.RED + "You have forced " + target.getName() + " to spawn in world " + world.getName() + ".");
            PluginUtility.callEvent(new PlayerSpawnEvent(target, teleport));
        }

    }

    public static Location toHighest(Location location) {
        while(location.getBlock().getType() != Material.AIR) {
            location = location.add(0, 1, 0);
        }

        return location;
    }

}

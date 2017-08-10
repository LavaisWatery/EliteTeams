package net.elitemc.eliteteams.command;

import net.elitemc.commons.util.*;
import net.elitemc.commons.util.cmdfrmwrk.BaseCommand;
import net.elitemc.commons.util.cmdfrmwrk.CommandUsageBy;
import net.elitemc.commons.util.mongo.pooling.PoolAction;
import net.elitemc.eliteteams.handler.TeamsHandler;
import net.elitemc.eliteteams.handler.TeamsPlayerHandler;
import net.elitemc.eliteteams.util.TeamsPlayerWrapper;
import net.elitemc.eliteteams.util.team.EliteTeam;
import net.elitemc.eliteteams.util.team.excep.TeamAddPlayerException;
import net.elitemc.eliteteams.util.team.excep.TeamInvitePlayerException;
import net.elitemc.origin.util.OriginPlayerWrapper;
import net.elitemc.origin.util.event.PlayerSpawnEvent;
import net.elitemc.origin.util.object.TeleportTarget;
import net.elitemc.origin.util.runnable.Teleporter;
import org.bukkit.*;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * Created by LavaisWatery on 2017-07-31.
 */
public class Command_team extends BaseCommand {

    public Command_team() {
        super("team", "teams.team", CommandUsageBy.PLAYER, new String[] { "t", "f", "faction" } );
        setUsage("/<command> [...]");
        setArgRange(0, 30);
    }

    private String[] usage = {ChatColor.DARK_AQUA.toString() + ChatColor.BOLD + "Teams " + ChatColor.GRAY + ": " + ChatColor.AQUA + "EliteMC Network",
            ChatColor.GRAY.toString() + ChatColor.STRIKETHROUGH + "---------------------------",
            ChatColor.BLUE.toString() + ChatColor.BOLD + "General",
            ChatColor.DARK_AQUA + "/team create " + ChatColor.AQUA + "<teamName> " + ChatColor.GRAY + "- Create a team", //
            ChatColor.DARK_AQUA + "/team info " + ChatColor.AQUA + "<playerName> " + ChatColor.GRAY + "- View information about a player's team", //
            ChatColor.DARK_AQUA + "/team roster " + ChatColor.AQUA + "<teamName> " + ChatColor.GRAY + "- View a team's player roster.", //
            ChatColor.GRAY.toString() + ChatColor.STRIKETHROUGH + "---------------------------",
            ChatColor.BLUE.toString() + ChatColor.BOLD + "Team Owner",
            ChatColor.DARK_AQUA + "/team disband " + ChatColor.GRAY + "- This will disband your team. This will be permenent, there's no going back.", //
            ChatColor.GRAY.toString() + ChatColor.STRIKETHROUGH + "---------------------------",
            ChatColor.BLUE.toString() + ChatColor.BOLD + "Team Manager",
            ChatColor.DARK_AQUA + "/team promote " + ChatColor.AQUA + "<playerName> " + ChatColor.GRAY + "- Promote a member on the team.", //
            ChatColor.DARK_AQUA + "/team demote " + ChatColor.AQUA + "<playerName> " + ChatColor.GRAY + "- Demote a member on the team.", //
            ChatColor.DARK_AQUA + "/team kick " + ChatColor.AQUA + "<playerName> " + ChatColor.GRAY + "- Kick a player from your team.", //
            ChatColor.DARK_AQUA + "/team password " + ChatColor.AQUA + "<password:off> " + ChatColor.GRAY + "- Change the password to join your team.",
            ChatColor.DARK_AQUA + "/team withdraw " + ChatColor.AQUA + "<amount> " + ChatColor.GRAY + "- Withdraw team balance directly to your wallet.",
            ChatColor.DARK_AQUA + "/team sethq " + ChatColor.GRAY + "- Set the location of the team headquarters.",
            ChatColor.DARK_AQUA + "/team setrally " + ChatColor.GRAY + "- Set the location of the team rally.",
            ChatColor.DARK_AQUA + "/team ff " + ChatColor.AQUA + "<on:off> " + ChatColor.GRAY + "- Toggle friendly fire on team members.",
            ChatColor.GRAY.toString() + ChatColor.STRIKETHROUGH + "---------------------------",
            ChatColor.BLUE.toString() + ChatColor.BOLD + "Team Memebers",
            ChatColor.DARK_AQUA + "/team hq " + ChatColor.GRAY + "- Teleport to your team's headquarters.",
            ChatColor.DARK_AQUA + "/team rally " + ChatColor.GRAY + "- Teleport to your team's rally.",
            ChatColor.DARK_AQUA + "/team balance " + ChatColor.GRAY + "- Check your team's balance.",
            ChatColor.DARK_AQUA + "/team deposit " + ChatColor.AQUA + "<amount> " + ChatColor.GRAY + "- Increase your team's worth by putting money into the team's bank.",
            ChatColor.DARK_AQUA + "/team leave " + ChatColor.GRAY + "- Leave your current team.", //
            ChatColor.GRAY.toString() + ChatColor.STRIKETHROUGH + "---------------------------"
    };

    public void execute(CommandSender sender, String[] args) {
        Player player = (Player) sender;

        if(args.length == 0) {
            MessageUtility.sendList(sender, usage);
            return;
        }

        if(args[0].equalsIgnoreCase("create")) {
            if(args.length < 2) {
                MessageUtility.message(player, false, StringUtility.getDefaultUsage() + "create <teamName>");
                return;
            }
            if(TeamsHandler.getInstance().getPlayerTeam(player) != null) {
                MessageUtility.message(player, false, ChatColor.RED + "You are already on a team. " + TeamsHandler.getInstance().getPlayerTeam(player).getTeamName());
                return;
            }
            if(args[1].length() > EliteTeam.MAX_TEAMNAME_LENGTH) {
                MessageUtility.message(player, false, ChatColor.RED + "Team names must not exceed " + EliteTeam.MAX_TEAMNAME_LENGTH + " characters.");
                return;
            }
            if(args[1].length() < EliteTeam.MIN_TEAMNAME_LENGTH) {
                MessageUtility.message(player, false, ChatColor.RED + "Team names must have a minimum of " + EliteTeam.MIN_TEAMNAME_LENGTH + " characters.");
                return;
            }
            if(!StringUtility.isAlphaWithNumber(args[1])) {
                MessageUtility.message(player, false, ChatColor.RED + "Your team name must be alphanumerical.");
                return;
            }
            if(TeamsHandler.getInstance().getEliteTeam(args[1]) != null) {
                MessageUtility.message(player, false, ChatColor.RED + "This team already exists.");
                return;
            }
            EliteTeam team = new EliteTeam(args[1]);
            TeamsHandler.getInstance().getTeams().put(team.getTeamName().toLowerCase(), team);
            try {
                team.setOwner(player);
            } catch (Exception ex) {
                MessageUtility.message(player, false, ChatColor.RED + "Unable to create team: " + ex.getMessage());
                team.dispose();
                return;
            }
            team.setLoaded(true);
            team.queueAction(PoolAction.SAVE);
            MessageUtility.message(player, false, ChatColor.GRAY + "You have created team " + ChatColor.ITALIC + team.getTeamName() + ChatColor.GRAY + ".");
            return;
        }
        else if(args[0].equalsIgnoreCase("info") || args[0].equalsIgnoreCase("i")) {
            EliteTeam targetTeam = null;
            OfflinePlayer targetPlayer = null;
            if(args.length < 2) {
                if(TeamsHandler.getInstance().getPlayerTeam(player) != null) {
                    targetPlayer = player;
                    targetTeam = TeamsHandler.getInstance().getPlayerTeam(targetPlayer.getUniqueId());
                }
            }
            else {
                if(Bukkit.getOfflinePlayer(args[1]) != null) {
                    targetPlayer = Bukkit.getOfflinePlayer(args[1]);
                    targetTeam = TeamsHandler.getInstance().getPlayerTeam(targetPlayer.getUniqueId());
                }
            }

            if(targetTeam != null) {
                targetTeam.showInfo(player, targetTeam.isMember(player));
            }
        }
        else if(args[0].equalsIgnoreCase("roster")) {
            if(args.length < 2) {
                MessageUtility.message(player, false, StringUtility.getDefaultUsage() + "/team roster <teamName>");
                return;
            }

            if(TeamsHandler.getInstance().getEliteTeam(args[1]) != null) {
                EliteTeam targetTeam = TeamsHandler.getInstance().getEliteTeam(args[1]);

                if(targetTeam != null) {
                    targetTeam.showInfo(player, targetTeam.isMember(player));
                }
            }
            else {
                MessageUtility.message(player, false, ChatColor.RED + "This team doesn't exist.");
            }
        }
        else if(args[0].equalsIgnoreCase("kick")) {
            if(args.length < 2) {
                MessageUtility.message(player, false, StringUtility.getDefaultUsage() + "/team kick <playerName>");
                return;
            }
            if(TeamsHandler.getInstance().getPlayerTeam(player) == null) {
                MessageUtility.message(player, false, ChatColor.RED + "You aren't on a team.");
                return;
            }
            EliteTeam team = TeamsHandler.getInstance().getPlayerTeam(player);
            OfflinePlayer targetPlayer;

            if(Bukkit.getPlayer(args[1]) != null) {
                targetPlayer = Bukkit.getPlayer(args[1]);
            }
            else {
                targetPlayer = Bukkit.getOfflinePlayer(args[1]);
            }

            if(targetPlayer.getUniqueId() == player.getUniqueId()) {
                MessageUtility.message(player, false, ChatColor.RED + "You may not kick yourself.");
                return;
            }

            if(team.isManager(player)) {
                if (team.isMember(targetPlayer.getUniqueId())) {
                    if(team.getPlayerRanks().get(targetPlayer.getUniqueId()) > team.getPlayerRanks().get(player.getUniqueId())) {
                        MessageUtility.message(player, false, ChatColor.RED + "You cannot kick higher ranks.");
                        return;
                    }

                    team.leave(targetPlayer.getUniqueId());
                    MessageUtility.message(player, false, ChatColor.GRAY + "You have kicked " + targetPlayer.getName() + ".");
                }
                else {
                    MessageUtility.message(player, false, ChatColor.RED + "This player isn't on your team.");
                }
            }
            else {
                MessageUtility.message(player, false, ChatColor.RED + "You must be a team manager to do this.");
            }
        }
        else if(args[0].equalsIgnoreCase("chat") || args[0].equalsIgnoreCase("c")) {
            if(TeamsHandler.getInstance().getPlayerTeam(player) == null) {
                MessageUtility.message(player, false, ChatColor.RED + "You aren't on a team.");
                return;
            }
            EliteTeam team = TeamsHandler.getInstance().getPlayerTeam(player);

            if(team.inTeamChat(player))
                team.getTeamChat().remove(player.getUniqueId());
            else
                team.getTeamChat().add(player.getUniqueId());

            MessageUtility.message(player, false, ChatColor.GRAY + "You have " + (team.inTeamChat(player) ? "join " : "left ") + "teamchat.");
        }
        else if(args[0].equalsIgnoreCase("invite")) {
            if(args.length < 2) {
                MessageUtility.message(player, false, StringUtility.getDefaultUsage() + "/team invite <playerName>");
                return;
            }
            if(TeamsHandler.getInstance().getPlayerTeam(player) == null) {
                MessageUtility.message(player, false, ChatColor.RED + "You aren't on a team.");
                return;
            }
            EliteTeam team = TeamsHandler.getInstance().getPlayerTeam(player);

            if(team.getPlayerRanks().size() >= EliteTeam.MAX_PLAYERS) {
                MessageUtility.message(player, false, ChatColor.RED + "Your team is currently full.");
                return;
            }

            OfflinePlayer targetPlayer;

            if(Bukkit.getPlayer(args[1]) != null) {
                targetPlayer = Bukkit.getPlayer(args[1]);
            }
            else {
                targetPlayer = Bukkit.getOfflinePlayer(args[1]);
            }

            if(team.isManager(player)) {
                if(team.isInviteExpired(targetPlayer.getUniqueId())) {
                    try {
                        team.invitePlayer(targetPlayer.getUniqueId());
                        MessageUtility.message(player, false, ChatColor.GRAY + "Invited " + targetPlayer.getName() + ".");
                    } catch (TeamInvitePlayerException ex) {
                        MessageUtility.message(player, false, ChatColor.RED + ex.getMessage());
                    }
                }
                else {
                    team.getPlayerInvites().remove(targetPlayer.getUniqueId());
                    if(targetPlayer.isOnline()) MessageUtility.message(targetPlayer.getPlayer(), false, "Your invite to " + team.getTeamName() + " has been revoked.");
                    MessageUtility.message(player, false, ChatColor.GRAY + "You have revoked " + targetPlayer.getName() + "'s team invite.");
                }
            }
            else {
                MessageUtility.message(player, false, ChatColor.RED + "You must be a team manager to do this.");
            }
        }
        else if(args[0].equalsIgnoreCase("join")) {
            if(args.length < 2) {
                MessageUtility.message(player, false, StringUtility.getDefaultUsage() + "/team join <teamName> [password]");
                return;
            }
            if(TeamsHandler.getInstance().getPlayerTeam(player) != null) {
                MessageUtility.message(player, false, ChatColor.RED + "You are already on a team.");
                return;
            }
            EliteTeam targetTeam = null;

            if((targetTeam = TeamsHandler.getInstance().getEliteTeam(args[1])) != null) {
                boolean withPassword = args.length >= 3;
                MessageUtility.message(player, false, "" + withPassword);

                if(!withPassword) {
                    if (!targetTeam.getPlayerInvites().containsKey(player.getUniqueId())) {
                        MessageUtility.message(player, false, ChatColor.RED + "You haven't been invited to this team.");
                        return;
                    }

                    if (targetTeam.getPlayerInvites().containsKey(player.getUniqueId()) && targetTeam.isInviteExpired(player.getUniqueId())) {
                        targetTeam.getPlayerInvites().remove(player.getUniqueId());
                        MessageUtility.message(player, false, ChatColor.RED + "Your invite is expired.");
                        return;
                    }
                }
                else {
                    String password = args[2];

                    if(!targetTeam.getPassword().isEmpty()) {
                        if (!targetTeam.getPassword().contentEquals(password)) {
                            MessageUtility.message(player, false, ChatColor.RED + "That is not the correct password.");
                            return;
                        }
                    }
                }

                if(targetTeam.getPlayerRanks().size() >= EliteTeam.MAX_PLAYERS) {
                    MessageUtility.message(player, false, ChatColor.RED + "This team is currently full.");
                    return;
                }

                if(!withPassword) targetTeam.getPlayerInvites().remove(player.getUniqueId());
                try {
                    targetTeam.sendMassMessage(player.getName() + " has joined the team.");
                    targetTeam.setMember(player);
                    MessageUtility.message(player, false, ChatColor.GRAY + "You have joined " + targetTeam.getTeamName() + ".");
                } catch (TeamAddPlayerException ex) {
                    MessageUtility.message(player, false, ChatColor.RED + ex.getMessage());
                }
            }
            else {
                MessageUtility.message(player, false, ChatColor.RED + "This team doesn't exist.");
            }
        }
        else if(args[0].equalsIgnoreCase("balance") || args[0].equalsIgnoreCase("money")) {
            if(TeamsHandler.getInstance().getPlayerTeam(player) == null) {
                MessageUtility.message(player, false, ChatColor.RED + "You aren't on a team.");
                return;
            }
            EliteTeam team = TeamsHandler.getInstance().getPlayerTeam(player);

            MessageUtility.message(player, false, ChatColor.DARK_AQUA + "Balance: " + team.getBalance());
        }
        else if(args[0].equalsIgnoreCase("deposit") || args[0].equalsIgnoreCase("d")) {
            if(args.length < 2) {
                MessageUtility.message(player, false, StringUtility.getDefaultUsage() + "/team deposit <amount>");
                return;
            }
            if(TeamsHandler.getInstance().getPlayerTeam(player) == null) {
                MessageUtility.message(player, false, ChatColor.RED + "You aren't on a team.");
                return;
            }
            TeamsPlayerWrapper wrapper = TeamsPlayerHandler.getInstance().getPlayerWrapper(player);
            EliteTeam team = TeamsHandler.getInstance().getPlayerTeam(player);

            try {
                double amount = Double.parseDouble(args[1]);

                if(amount <= 0) {
                    MessageUtility.message(player, false, ChatColor.RED + "You are unable to deposit amounts less than 1");
                    return;
                }

                if(wrapper.getBalance() >= amount) {
                    wrapper.setBalance(wrapper.getBalance() - amount);
                    team.setBalance(team.getBalance() + amount);
                }
                else {
                    MessageUtility.message(player, false, ChatColor.RED + "You must have the amount you're depositing.");
                    return;
                }
            } catch (Exception ex) {
                MessageUtility.message(player, false, ChatColor.RED + "You must input a number value.");
                return;
            }
        }
        else if(args[0].equalsIgnoreCase("ff") || args[0].equalsIgnoreCase("friendlyfire")) {
            boolean hasToggle = args.length >= 2;
            if(TeamsHandler.getInstance().getPlayerTeam(player) == null) {
                MessageUtility.message(player, false, ChatColor.RED + "You aren't on a team.");
                return;
            }
            EliteTeam team = TeamsHandler.getInstance().getPlayerTeam(player);

            if(team.isManager(player)) {
                boolean friendly = team.isFriendlyFire();

                if(hasToggle) {
                    try {
                        String tog = args[1];
                        if(tog.equalsIgnoreCase("on")) {
                            tog = "true";
                        }
                        else if(tog.equalsIgnoreCase("off")) {
                            tog = "false";
                        }
                        boolean b = Boolean.parseBoolean(tog);

                        friendly = b;
                    } catch (Exception ex) {
                        MessageUtility.message(player, false, ChatColor.RED + "on:off");
                        return;
                    }
                }
                else {
                    friendly = !friendly;
                }
                team.setFriendlyFire(friendly);

                team.sendMassMessage(ChatColor.DARK_AQUA + player.getName() + " has toggled friendly fire " + (friendly ? "on" : "off") + ".");
            }
            else {
                MessageUtility.message(player, false, ChatColor.RED + "You must be a team manager to do this.");
            }
        }
        else if(args[0].equalsIgnoreCase("password")) {
            if(args.length < 2) {
                MessageUtility.message(player, false, StringUtility.getDefaultUsage() + "/team password <password:off>");
                return;
            }
            if(TeamsHandler.getInstance().getPlayerTeam(player) == null) {
                MessageUtility.message(player, false, ChatColor.RED + "You aren't on a team.");
                return;
            }
            EliteTeam team = TeamsHandler.getInstance().getPlayerTeam(player);

            if(team.isManager(player)) {
                String password = args[1];

                if(password.equalsIgnoreCase("none")) {
                    MessageUtility.message(player, false, ChatColor.RED + "You have toggled the team password off.");
                    password = "";
                }
                team.setPassword(password);
                team.sendMassMessage(ChatColor.GRAY + player.getName() + " has changed the team password.");
            }
            else {
                MessageUtility.message(player, false, ChatColor.RED + "You must be a team manager to do this.");
            }
        }
        else if(args[0].equalsIgnoreCase("withdraw")) {
            if(args.length < 2) {
                MessageUtility.message(player, false, StringUtility.getDefaultUsage() + "/team withdraw <amount>");
                return;
            }
            if(TeamsHandler.getInstance().getPlayerTeam(player) == null) {
                MessageUtility.message(player, false, ChatColor.RED + "You aren't on a team.");
                return;
            }
            EliteTeam team = TeamsHandler.getInstance().getPlayerTeam(player);

            if(team.isManager(player)) {
                String amountRaw = args[1];

                if(NumberUtility.isNumber(amountRaw)) {
                    double amount = Double.parseDouble(amountRaw);

                    if(amount > 0) {
                        team.setBalance(team.getBalance() - amount);
                        team.sendTeamChat(player, "has withdrew " + amount + " from the team Bank Account.");
                    }
                    else {
                        MessageUtility.message(sender, false, ChatColor.RED + "You must withdraw more money.");
                    }
                }
                else {
                    MessageUtility.message(sender, false, ChatColor.RED + "You must input a number amount to withdraw money.");
                }
            }
            else {
                MessageUtility.message(player, false, ChatColor.RED + "You must be a team manager to do this.");
            }
        }
        else if(args[0].equalsIgnoreCase("hq")) {
            if(TeamsHandler.getInstance().getPlayerTeam(player) == null) {
                MessageUtility.message(player, false, ChatColor.RED + "You aren't on a team.");
                return;
            }
            EliteTeam team = TeamsHandler.getInstance().getPlayerTeam(player);
            if(team.getHeadquarters() == null) {
                MessageUtility.message(player, false, ChatColor.RED + "Your team doesn't have a headquarters.");
                return;
            }
            TeamsPlayerWrapper wrapper = TeamsPlayerHandler.getInstance().getPlayerWrapper(player);
            OriginPlayerWrapper playerWrapper = wrapper.getOriginWrapper();
            Location teleport = team.getHeadquarters();
            long time = 1000 * 10;

            playerWrapper.doTeleportWithCheck(new Teleporter(playerWrapper, (long)(time * 20), Teleporter.TeleportType.LOCATION_TARGET, new TeleportTarget(teleport), "Plaiyng minecraft in 10 seconds") {
                @Override
                public void doIt() {
                    LocationUtility.assureChunk(getTarget().getTargetLocation());
                    getWrapper().getPlayer().teleport(getTarget().getTargetLocation());
                    wrapper.doProtectionApplyCheck(getWrapper().getPlayer(), getTarget().getTargetLocation());
                    MessageUtility.message(getWrapper().getPlayer(), false, "You have teleported to team hq");
                    PluginUtility.callEvent(new PlayerSpawnEvent(getWrapper().getPlayer(), getTarget().getTargetLocation()));
                }
            });
        }
        else if(args[0].equalsIgnoreCase("hq")) {
            if(TeamsHandler.getInstance().getPlayerTeam(player) == null) {
                MessageUtility.message(player, false, ChatColor.RED + "You aren't on a team.");
                return;
            }
            EliteTeam team = TeamsHandler.getInstance().getPlayerTeam(player);
            if(team.getHeadquarters() == null) {
                MessageUtility.message(player, false, ChatColor.RED + "Your team doesn't have a headquarters.");
                return;
            }
            TeamsPlayerWrapper wrapper = TeamsPlayerHandler.getInstance().getPlayerWrapper(player);
            OriginPlayerWrapper playerWrapper = wrapper.getOriginWrapper();
            Location teleport = team.getRally();
            long time = 1000 * 10;

            playerWrapper.doTeleportWithCheck(new Teleporter(playerWrapper, (long)(time * 20), Teleporter.TeleportType.LOCATION_TARGET, new TeleportTarget(teleport), "Plaiyng minecraft in 10 seconds") {
                @Override
                public void doIt() {
                    LocationUtility.assureChunk(getTarget().getTargetLocation());
                    getWrapper().getPlayer().teleport(getTarget().getTargetLocation());
                    wrapper.doProtectionApplyCheck(getWrapper().getPlayer(), getTarget().getTargetLocation());
                    MessageUtility.message(getWrapper().getPlayer(), false, "You have teleported to team rally");
                    PluginUtility.callEvent(new PlayerSpawnEvent(getWrapper().getPlayer(), getTarget().getTargetLocation()));
                }
            });
        }
        else if(args[0].equalsIgnoreCase("sethq")) {
            if(TeamsHandler.getInstance().getPlayerTeam(player) == null) {
                MessageUtility.message(player, false, ChatColor.RED + "You aren't on a team.");
                return;
            }
            EliteTeam team = TeamsHandler.getInstance().getPlayerTeam(player);

            if(team.isManager(player)) {
                team.setHeadquarters(player.getLocation());
                team.sendMassMessage(ChatColor.GRAY + player.getName() + " has updated the team headquarters.");
            }
            else {
                MessageUtility.message(player, false, ChatColor.RED + "You must be a team manager to do this.");
            }
        }
        else if(args[0].equalsIgnoreCase("setrally")) {
            if(TeamsHandler.getInstance().getPlayerTeam(player) == null) {
                MessageUtility.message(player, false, ChatColor.RED + "You aren't on a team.");
                return;
            }
            EliteTeam team = TeamsHandler.getInstance().getPlayerTeam(player);

            if(team.isManager(player)) {
                team.setRally(player.getLocation());
                team.sendMassMessage(ChatColor.GRAY + player.getName() + " has updated the team rally.");
            }
            else {
                MessageUtility.message(player, false, ChatColor.RED + "You must be a team manager to do this.");
            }
        }
        else if(args[0].equalsIgnoreCase("promote")) {
            if(args.length < 2) {
                MessageUtility.message(player, false, StringUtility.getDefaultUsage() + "/team promote <playerName>");
                return;
            }
            if(TeamsHandler.getInstance().getPlayerTeam(player) == null) {
                MessageUtility.message(player, false, ChatColor.RED + "You aren't on a team.");
                return;
            }
            EliteTeam team = TeamsHandler.getInstance().getPlayerTeam(player);
            OfflinePlayer targetPlayer;

            if(Bukkit.getPlayer(args[1]) != null) {
                targetPlayer = Bukkit.getPlayer(args[1]);
            }
            else {
                targetPlayer = Bukkit.getOfflinePlayer(args[1]);
            }

            if(team.isManager(player)) {
                if(team.isMember(targetPlayer.getUniqueId())) {
                    if(!team.isManager(targetPlayer.getUniqueId())) {
                        try {
                            team.setManager(targetPlayer.getUniqueId());
                            MessageUtility.message(player, false, ChatColor.GRAY + "You have promoted " + targetPlayer.getName() + ".");
                        } catch (TeamAddPlayerException ex) {
                            MessageUtility.message(player, false, ChatColor.RED + ex.getMessage());
                        }
                    }
                    else {
                        MessageUtility.message(player, false, ChatColor.RED + "This player is already a team " + team.getRankName(team.getMemberRank(targetPlayer.getUniqueId())));
                    }
                }
                else {
                    MessageUtility.message(player, false, ChatColor.RED + "This player isn't part of your team.");
                }
            }
            else {
                MessageUtility.message(player, false, ChatColor.RED + "You must be a team manager to do this.");
            }
        }
        else if(args[0].equalsIgnoreCase("demote")) {
            if(args.length < 2) {
                MessageUtility.message(player, false, StringUtility.getDefaultUsage() + "/team promote <playerName>");
                return;
            }
            if(TeamsHandler.getInstance().getPlayerTeam(player) == null) {
                MessageUtility.message(player, false, ChatColor.RED + "You aren't on a team.");
                return;
            }
            EliteTeam team = TeamsHandler.getInstance().getPlayerTeam(player);
            OfflinePlayer targetPlayer;

            if(Bukkit.getPlayer(args[1]) != null) {
                targetPlayer = Bukkit.getPlayer(args[1]);
            }
            else {
                targetPlayer = Bukkit.getOfflinePlayer(args[1]);
            }

            if(team.isManager(player)) {
                if(team.isMember(targetPlayer.getUniqueId())) {
                    if(team.getPlayerRanks().get(targetPlayer.getUniqueId()) <= team.getPlayerRanks().get(player.getUniqueId())) {
                        if(team.demotePlayer(targetPlayer.getUniqueId())) {
                            MessageUtility.message(player, false, ChatColor.GRAY + "You have demoted " + targetPlayer.getName() + ".");
                        }
                        else {
                            MessageUtility.message(player, false, ChatColor.RED + "Failed to demote " + targetPlayer.getName() + ".");
                        }
                    }
                    else {
                        MessageUtility.message(player, false, ChatColor.RED + "This player is a higher rank than you.");
                    }
                }
                else {
                    MessageUtility.message(player, false, ChatColor.RED + "This player isn't part of your team.");
                }
            }
            else {
                MessageUtility.message(player, false, ChatColor.RED + "You must be a team manager to do this.");
            }
        }
        else if(args[0].equalsIgnoreCase("leave")) {
            if(TeamsHandler.getInstance().getPlayerTeam(player) == null) {
                MessageUtility.message(player, false, ChatColor.RED + "You aren't on a team.");
                return;
            }
            EliteTeam team = TeamsHandler.getInstance().getPlayerTeam(player);

            team.leave(player.getUniqueId());
            team.sendMassMessage(player.getName() + " has left the team.");
            MessageUtility.message(player, false, ChatColor.GRAY + "Left team " + team.getTeamName());
        }
        else if(args[0].equalsIgnoreCase("disband")) {
            if(TeamsHandler.getInstance().getPlayerTeam(player) == null) {
                MessageUtility.message(player, false, ChatColor.RED + "You aren't on a team.");
                return;
            }
            EliteTeam team = TeamsHandler.getInstance().getPlayerTeam(player);

            if(team.isOwner(player)) {
                team.dispose();
                MessageUtility.message(player, false, ChatColor.GRAY + "You have disbanded your team.");
            }
            else {
                MessageUtility.message(player, false, ChatColor.RED + "You must be an owner to do disband.");
            }
        }

    }

}

package net.elitemc.eliteteams.command;

import net.elitemc.commons.util.MessageUtility;
import net.elitemc.commons.util.StringUtility;
import net.elitemc.commons.util.cmdfrmwrk.BaseCommand;
import net.elitemc.commons.util.cmdfrmwrk.CommandUsageBy;
import net.elitemc.commons.util.mongo.pooling.PoolAction;
import net.elitemc.eliteteams.handler.TeamsHandler;
import net.elitemc.eliteteams.util.team.EliteTeam;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

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
            ChatColor.DARK_AQUA + "/team create " + ChatColor.AQUA + "<teamName> " + ChatColor.GRAY + "- Create a team",
            ChatColor.DARK_AQUA + "/team info " + ChatColor.AQUA + "<playerName> " + ChatColor.GRAY + "- View information about a player's team",
            ChatColor.DARK_AQUA + "/team roster " + ChatColor.AQUA + "<teamName> " + ChatColor.GRAY + "- View a team's player roster.",
            ChatColor.GRAY.toString() + ChatColor.STRIKETHROUGH + "---------------------------",
            ChatColor.BLUE.toString() + ChatColor.BOLD + "Team Owner",
            ChatColor.DARK_AQUA + "/team disband " + ChatColor.GRAY + "- This will disband your team. This will be permenent, there's no going back.",
            ChatColor.GRAY.toString() + ChatColor.STRIKETHROUGH + "---------------------------",
            ChatColor.BLUE.toString() + ChatColor.BOLD + "Team Manager",
            ChatColor.DARK_AQUA + "/team kick " + ChatColor.AQUA + "<playerName> " + ChatColor.GRAY + "- Kick a player from your team.",
            ChatColor.DARK_AQUA + "/team password " + ChatColor.AQUA + "<password> " + ChatColor.GRAY + "- Change the password to join your team.",
            ChatColor.DARK_AQUA + "/team sethq " + ChatColor.GRAY + "- Set the location of the team headquarters.",
            ChatColor.DARK_AQUA + "/team setrally " + ChatColor.GRAY + "- Set the location of the team rally.",
            ChatColor.DARK_AQUA + "/team ff " + ChatColor.AQUA + "<on:off> " + ChatColor.GRAY + "- Toggle friendly fire on team members.",
            ChatColor.GRAY.toString() + ChatColor.STRIKETHROUGH + "---------------------------",
            ChatColor.BLUE.toString() + ChatColor.BOLD + "Team Memebers",
            ChatColor.DARK_AQUA + "/team hq " + ChatColor.GRAY + "- Teleport to your team's headquarters.",
            ChatColor.DARK_AQUA + "/team rally " + ChatColor.GRAY + "- Teleport to your team's rally.",
            ChatColor.DARK_AQUA + "/team balance " + ChatColor.GRAY + "- Check your team's balance.",
            ChatColor.DARK_AQUA + "/team deposit " + ChatColor.AQUA + "<amount> " + ChatColor.GRAY + "- Increase your team's worth by putting money into the team's bank.",
            ChatColor.DARK_AQUA + "/team leave " + ChatColor.GRAY + "- Leave your current team.",
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
                MessageUtility.message(player, false, ChatColor.DARK_AQUA + "" + ChatColor.BOLD + targetTeam.getTeamName() + ChatColor.GRAY + "(" + ChatColor.GREEN + targetTeam.getPlayerRanks().size() + "/" + EliteTeam.MAX_PLAYERS + " online" + ChatColor.GRAY + ")");
                if(!targetTeam.getDescription().isEmpty()) MessageUtility.message(player, false, ChatColor.DARK_GRAY + "* " + ChatColor.RESET + targetTeam.getDescription() + ChatColor.DARK_GRAY + " *");
                MessageUtility.message(player, false, ChatColor.GRAY.toString() + ChatColor.STRIKETHROUGH + "---------------------------");

                MessageUtility.message(player, false, ChatColor.AQUA.toString() + ChatColor.BOLD + "Roster:");
                List<UUID> onlineMembers = new ArrayList<>();
                List<UUID> offlineMembers = new ArrayList<>();
                List<UUID> onlineManagers = new ArrayList<>();
                List<UUID> offlineManagers = new ArrayList<>();
                List<UUID> onlineOwner = new ArrayList<>();
                List<UUID> offlineOwner = new ArrayList<>();

                for(UUID member : targetTeam.getPlayerRanks().keySet()) {
                    if(targetTeam.isOwner(member)) {
                        if(Bukkit.getPlayer(member) != null) {
                            if(!onlineOwner.contains(member)) onlineOwner.add(member);
                        }
                        else {
                            if(!offlineOwner.contains(member)) offlineOwner.add(member);
                        }
                    }
                    else if(targetTeam.isManager(member)) {
                        if(Bukkit.getPlayer(member) != null) {
                            if(!onlineManagers.contains(member)) onlineManagers.add(member);
                        }
                        else {
                            if(!offlineManagers.contains(member)) offlineManagers.add(member);
                        }
                    }
                    else {
                        if(Bukkit.getPlayer(member) != null) {
                            if(!onlineMembers.contains(member)) onlineMembers.add(member);
                        }
                        else {
                            if(!offlineMembers.contains(member)) offlineMembers.add(member);
                        }
                    }
                }

                {
                    for(UUID uid : onlineOwner) {
                        MessageUtility.message(player, false, ChatColor.WHITE + "- " + ChatColor.GREEN + "**" + Bukkit.getOfflinePlayer(uid).getName());
                    }
                    for(UUID uid : onlineManagers) {
                        MessageUtility.message(player, false, ChatColor.WHITE + "- " + ChatColor.GREEN + "*" + Bukkit.getOfflinePlayer(uid).getName());
                    }
                    for(UUID uid : onlineMembers) {
                        MessageUtility.message(player, false, ChatColor.WHITE + "- " + ChatColor.GREEN + "" + Bukkit.getOfflinePlayer(uid).getName());
                    }
                    for(UUID uid : offlineOwner) {
                        MessageUtility.message(player, false, ChatColor.WHITE + "- " + ChatColor.GRAY + "**" + Bukkit.getOfflinePlayer(uid).getName());
                    }
                    for(UUID uid : offlineManagers) {
                        MessageUtility.message(player, false, ChatColor.WHITE + "- " + ChatColor.GRAY + "*" + Bukkit.getOfflinePlayer(uid).getName());
                    }
                    for(UUID uid : offlineMembers) {
                        MessageUtility.message(player, false, ChatColor.WHITE + "- " + ChatColor.GRAY + "" + Bukkit.getOfflinePlayer(uid).getName());
                    }
                }

                MessageUtility.message(player, false, ChatColor.DARK_AQUA + "" + ChatColor.BOLD + "Stats:");
                MessageUtility.message(player, false, ChatColor.WHITE + "- " + ChatColor.AQUA + "Kills: " + ChatColor.RESET + targetTeam.getKills());
                MessageUtility.message(player, false, ChatColor.WHITE + "- " + ChatColor.AQUA + "Deaths: " + ChatColor.RESET + targetTeam.getDeaths());
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
                    targetTeam.showInfo();

                    MessageUtility.message(player, false, ChatColor.DARK_AQUA + "" + ChatColor.BOLD + targetTeam.getTeamName() + ChatColor.GRAY + "(" + ChatColor.GREEN + targetTeam.getPlayerRanks().size() + "/" + EliteTeam.MAX_PLAYERS + " online" + ChatColor.GRAY + ")");
                    if(!targetTeam.getDescription().isEmpty()) MessageUtility.message(player, false, ChatColor.DARK_GRAY + "* " + ChatColor.RESET + targetTeam.getDescription() + ChatColor.DARK_GRAY + " *");
                    MessageUtility.message(player, false, ChatColor.GRAY.toString() + ChatColor.STRIKETHROUGH + "---------------------------");

                    MessageUtility.message(player, false, ChatColor.AQUA.toString() + ChatColor.BOLD + "Roster:");

                    List<UUID> onlineMembers = new ArrayList<>();
                    List<UUID> offlineMembers = new ArrayList<>();
                    List<UUID> onlineManagers = new ArrayList<>();
                    List<UUID> offlineManagers = new ArrayList<>();
                    List<UUID> onlineOwner = new ArrayList<>();
                    List<UUID> offlineOwner = new ArrayList<>();

                    for(UUID member : targetTeam.getPlayerRanks().keySet()) {
                        if(targetTeam.isOwner(member)) {
                            if(Bukkit.getPlayer(member) != null) {
                                if(!onlineOwner.contains(member)) onlineOwner.add(member);
                            }
                            else {
                                if(!offlineOwner.contains(member)) offlineOwner.add(member);
                            }
                        }
                        else if(targetTeam.isManager(member)) {
                            if(Bukkit.getPlayer(member) != null) {
                                if(!onlineManagers.contains(member)) onlineManagers.add(member);
                            }
                            else {
                                if(!offlineManagers.contains(member)) offlineManagers.add(member);
                            }
                        }
                        else {
                            if(Bukkit.getPlayer(member) != null) {
                                if(!onlineMembers.contains(member)) onlineMembers.add(member);
                            }
                            else {
                                if(!offlineMembers.contains(member)) offlineMembers.add(member);
                            }
                        }
                    }

                    {
                        for(UUID uid : onlineOwner) {
                            MessageUtility.message(player, false, ChatColor.WHITE + "- " + ChatColor.GREEN + "**" + Bukkit.getOfflinePlayer(uid).getName());
                        }
                        for(UUID uid : onlineManagers) {
                            MessageUtility.message(player, false, ChatColor.WHITE + "- " + ChatColor.GREEN + "*" + Bukkit.getOfflinePlayer(uid).getName());
                        }
                        for(UUID uid : onlineMembers) {
                            MessageUtility.message(player, false, ChatColor.WHITE + "- " + ChatColor.GREEN + "" + Bukkit.getOfflinePlayer(uid).getName());
                        }
                        for(UUID uid : offlineOwner) {
                            MessageUtility.message(player, false, ChatColor.WHITE + "- " + ChatColor.GRAY + "**" + Bukkit.getOfflinePlayer(uid).getName());
                        }
                        for(UUID uid : offlineManagers) {
                            MessageUtility.message(player, false, ChatColor.WHITE + "- " + ChatColor.GRAY + "*" + Bukkit.getOfflinePlayer(uid).getName());
                        }
                        for(UUID uid : offlineMembers) {
                            MessageUtility.message(player, false, ChatColor.WHITE + "- " + ChatColor.GRAY + "" + Bukkit.getOfflinePlayer(uid).getName());
                        }
                    }

                    MessageUtility.message(player, false, ChatColor.DARK_AQUA + "" + ChatColor.BOLD + "Stats:");
                    MessageUtility.message(player, false, ChatColor.WHITE + "- " + ChatColor.AQUA + "Kills: " + ChatColor.RESET + targetTeam.getKills());
                    MessageUtility.message(player, false, ChatColor.WHITE + "- " + ChatColor.AQUA + "Deaths: " + ChatColor.RESET + targetTeam.getDeaths());
                }
            }
            else {
                MessageUtility.message(player, false, ChatColor.RED + "This team doesn't exist.");
            }
        }
        else if(args[0].equalsIgnoreCase("leave")) {
            if(TeamsHandler.getInstance().getPlayerTeam(player) == null) {
                MessageUtility.message(player, false, ChatColor.RED + "You aren't on a team.");
                return;
            }
            EliteTeam team = TeamsHandler.getInstance().getPlayerTeam(player);

            team.leave(player.getUniqueId());
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

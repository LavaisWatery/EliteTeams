package net.elitemc.eliteteams;

import net.elitemc.commons.handler.BoardHandler;
import net.elitemc.commons.util.Handler;
import net.elitemc.commons.util.PlayerUtility;
import net.elitemc.commons.util.abstr.AbstractInit;
import net.elitemc.commons.util.abstr.AbstractPlayerNear;
import net.elitemc.commons.util.cmdfrmwrk.BaseCommand;
import net.elitemc.commons.util.cmdfrmwrk.CommandRegistrar;
import net.elitemc.commons.util.scoreboard.V2.Board;
import net.elitemc.commons.util.scoreboard.V2.BoardEntry;
import net.elitemc.commons.util.scoreboard.V2.BoardPreset;
import net.elitemc.eliteteams.command.*;
import net.elitemc.eliteteams.configuration.TeamsConfiguration;
import net.elitemc.eliteteams.handler.AchievementHandler;
import net.elitemc.eliteteams.handler.OptionsHandler;
import net.elitemc.eliteteams.handler.RegionHandler;
import net.elitemc.eliteteams.handler.TeamsPlayerHandler;
import net.elitemc.eliteteams.util.TeamsPlayerWrapper;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.concurrent.TimeUnit;

/**
 * Created by LavaisWatery on 2017-07-22.
 */
public class Init extends AbstractInit {
    private static Init instance;

    public Init(Plugin plugin) {
        super(plugin);
        instance = this;

        initInstances();
        registerEvents();
        registerCommands();
    }

    private TeamsConfiguration configuration = null;

    private BoardHandler boardHandler = BoardHandler.getInstance();

    @Override
    public void initInstances() {
        configuration = new TeamsConfiguration(this);

        // init handlers
        initHandler(new RegionHandler(), true);
        initHandler(new TeamsPlayerHandler(), true);
        initHandler(new AchievementHandler(), true);
        initHandler(new OptionsHandler(), true);

        // init nearby
        PlayerUtility.setPlayerNear(new AbstractPlayerNear() {
            @Override
            public boolean isPlayerNear(Player player) {
                boolean nearby = false;
                if(player.getGameMode() == GameMode.CREATIVE || TeamsPlayerHandler.getInstance().getPlayerWrapper(player).getPlayerState() == TeamsPlayerWrapper.TeamsPlayerState.PROTECTED) return false;
//                if(PluginUtility.getWorldGuard().getRegionManager(player.getWorld()).getApplicableRegions(player.getLocation()).getFlag(DefaultFlag.POTION_SPLASH) != null && !PluginUtility.getWorldGuard().getRegionManager(player.getWorld()).getApplicableRegions(player.getLocation()).allows(DefaultFlag.POTION_SPLASH)) {
//                    if(!checkState)
//                        return false;
//                    else {
//                        if(PlayerHandler.getInstance().getPlayerState(player) != PlayerState.OUTSIDE) {
//                            return false;
//                        }
//                    }
//                }

                for (Entity e : player.getNearbyEntities(40.0D, 40.0D, 40.0D)) {
                    if (e instanceof Player && e != player) {
                        Player temp = (Player) e;
                        TeamsPlayerWrapper wrapper = TeamsPlayerHandler.getInstance().getPlayerWrapper(temp);
//                        if (temp.getGameMode() == GameMode.CREATIVE || PlayerManager.getInstance().getVanishedPlayers().contains(temp.getUniqueId()) || SpectateHandler.getInstance().getSpectateDate().containsKey(temp.getUniqueId())) continue;
                        if (temp.getGameMode() == GameMode.CREATIVE || wrapper.getPlayerState() == TeamsPlayerWrapper.TeamsPlayerState.PROTECTED) continue;
                        else {
                            nearby = true;
                        }
                    }
                }
                return nearby;
            }
        });

        // init board
        BoardPreset def = null;

        boardHandler.registerBoardPreset(def = new BoardPreset("default", ChatColor.AQUA.toString() + ChatColor.BOLD.toString() + "EliteMC " + ChatColor.GRAY + "[" + ChatColor.DARK_AQUA + "Map 1" + ChatColor.GRAY + "]") {
            @Override
            public void createEntries(Board board) {
                board.addEntry("spacer", 15, new BoardEntry.EntryInfo() {
                    @Override
                    public String getPrefix() {
                        return ChatColor.GRAY + "" + ChatColor.STRIKETHROUGH + "" + ChatColor.BOLD + "---------";
                    }

                    @Override
                    public String getSuffix() {
                        return "---------";
                    }
                });

                board.addEntry("team", 14, new BoardEntry.EntryInfo() {
                    TeamsPlayerWrapper wrapper = TeamsPlayerHandler.getInstance().getPlayerWrapper(board.getId());

                    @Override
                    public String getPrefix() {
                        return ChatColor.GREEN.toString() + ChatColor.BOLD + "Team: ";
                    }

                    @Override
                    public String getSuffix() {
                        return ChatColor.RESET + "none";
                    }
                });

                board.addEntry("protection", 13, new BoardEntry.EntryInfo() {
                    TeamsPlayerWrapper wrapper = TeamsPlayerHandler.getInstance().getPlayerWrapper(board.getId());

                    @Override
                    public String getPrefix() {
                        return ChatColor.GOLD.toString() + ChatColor.BOLD + "Protected: ";
                    }

                    @Override
                    public String getSuffix() {
                        return ChatColor.RESET + (wrapper.getPlayerState() == TeamsPlayerWrapper.TeamsPlayerState.PROTECTED ? "yes" : "no");
                    }
                });

                board.addEntry("spacer2", 11, new BoardEntry.EntryInfo() {
                    @Override
                    public String getPrefix() {
                        return ChatColor.GRAY + "" + ChatColor.STRIKETHROUGH + "" + ChatColor.BOLD + "--------";
                    }

                    @Override
                    public String getSuffix() {
                        return "----------";
                    }
                });

                board.addEntry("pearlcooldown", 11, new BoardEntry.EntryInfo() {
                    TeamsPlayerWrapper wrapper = TeamsPlayerHandler.getInstance().getPlayerWrapper(board.getId());

                    @Override
                    public String getPrefix() {
                        return "Pearl: ";
                    }

                    @Override
                    public String getSuffix() {
                        long expiry = wrapper.getLastPearlThrow();

                        return expiry != -1 && expiry > System.currentTimeMillis() ? (TimeUnit.MILLISECONDS.toSeconds(expiry - System.currentTimeMillis()) + 1) + "s" : "";
                    }
                });
            }
        });
        if(def != null) boardHandler.setDefaultPreset(def);

        final BoardPreset d = def;
        new BukkitRunnable() {
            @Override
            public void run() {
                for(Player player : PlayerUtility.getOnlinePlayers()) {
                    d.apply(boardHandler.assure(player));
                }
            }
        }.runTask(EliteTeams.getInstance());
    }

    @Override
    public void registerEvents() {

    }

    @Override
    public void registerCommands() {
        registerCommand("warp", new Command_warp());
        registerCommand("spawn", new Command_spawn());
        registerCommand("track", new Command_track());
        registerCommand("options", new Command_options());
        registerCommand("yes", new Command_yes());
        registerCommand("no", new Command_no());
        registerCommand("sets", new Command_sets());
        registerCommand("build", new Command_build());
    }

    @Override
    public void unload() {
        for(Handler handler : getHandlerList()) {
            try {
                handler.unload();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }

    public void registerCommand(String name, BaseCommand command) {
        CommandRegistrar registrar = getCommandRegistrar();

        try {
            registrar.registerCommand(name, command);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public TeamsConfiguration getConfiguration() {
        return configuration;
    }

    public static Init getInstance() {
        return instance;
    }

}

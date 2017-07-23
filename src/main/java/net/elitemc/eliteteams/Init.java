package net.elitemc.eliteteams;

import net.elitemc.commons.handler.BoardHandler;
import net.elitemc.commons.util.Handler;
import net.elitemc.commons.util.PlayerUtility;
import net.elitemc.commons.util.abstr.AbstractInit;
import net.elitemc.commons.util.cmdfrmwrk.BaseCommand;
import net.elitemc.commons.util.cmdfrmwrk.CommandRegistrar;
import net.elitemc.commons.util.scoreboard.V2.Board;
import net.elitemc.commons.util.scoreboard.V2.BoardEntry;
import net.elitemc.commons.util.scoreboard.V2.BoardPreset;
import net.elitemc.eliteteams.command.Command_spawn;
import net.elitemc.eliteteams.handler.AchievementHandler;
import net.elitemc.eliteteams.handler.OptionsHandler;
import net.elitemc.eliteteams.handler.RegionHandler;
import net.elitemc.eliteteams.handler.TeamsPlayerHandler;
import net.elitemc.eliteteams.util.TeamsPlayerWrapper;
import org.bukkit.ChatColor;
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

    private BoardHandler boardHandler = BoardHandler.getInstance();

    @Override
    public void initInstances() {
        // init handlers
        initHandler(new RegionHandler(), true);
        initHandler(new TeamsPlayerHandler(), true);
        initHandler(new AchievementHandler(), true);
        initHandler(new OptionsHandler(), true);

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
        registerCommand("spawn", new Command_spawn());
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


    public static Init getInstance() {
        return instance;
    }

}

package net.elitemc.eliteteams.handler;

import com.google.common.util.concurrent.MoreExecutors;
import net.elitemc.commons.handler.BoardHandler;
import net.elitemc.commons.handler.PlayerHandler;
import net.elitemc.commons.util.Debug;
import net.elitemc.commons.util.Handler;
import net.elitemc.commons.util.LocationUtility;
import net.elitemc.commons.util.PlayerUtility;
import net.elitemc.commons.util.mongo.MongoDataObject;
import net.elitemc.commons.util.mongo.pooling.ActionChange;
import net.elitemc.commons.util.wrapper.MongoDataObjectException;
import net.elitemc.eliteteams.EliteTeams;
import net.elitemc.eliteteams.util.AchievementType;
import net.elitemc.eliteteams.util.MoveRequest;
import net.elitemc.eliteteams.util.RankRewards;
import net.elitemc.eliteteams.util.TeamsPlayerWrapper;
import net.elitemc.origin.util.OriginPlayerWrapper;
import net.elitemc.origin.util.event.OriginPlayerJoinEvent;
import net.elitemc.origin.util.event.OriginPlayerUpdatedPlaytime;
import net.elitemc.origin.util.event.PlayerGroupSwitchEvent;
import org.bukkit.Bukkit;
import org.bukkit.entity.EnderPearl;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerTeleportEvent;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by LavaisWatery on 2017-07-22.
 */
public class TeamsPlayerHandler extends Handler {
    private static TeamsPlayerHandler instance;

    public TeamsPlayerHandler() {
        instance = this;
    }

    public static long PEARL_COOLDOWN = (1000 * 5);

    @Override
    public void init() {
        for(Player player : PlayerUtility.getOnlinePlayers()) {
            if(!PlayerHandler.getInstance().isWrapped(EliteTeams.getInstance(), player))
                PlayerHandler.getInstance().addPlayerWrapper(EliteTeams.getInstance(), new TeamsPlayerWrapper(player));
        }
    }

    @Override
    public void unload() {

    }

    public TeamsPlayerWrapper getPlayerWrapper(UUID uid) {
        if(!PlayerHandler.getInstance().isWrapped(EliteTeams.getInstance(), uid)) {
            PlayerHandler.getInstance().addPlayerWrapper(EliteTeams.getInstance(), new TeamsPlayerWrapper(uid));
        }

        return (TeamsPlayerWrapper) PlayerHandler.getInstance().getPlayerWrapper(EliteTeams.getInstance(), uid);
    }

    public TeamsPlayerWrapper getPlayerWrapper(UUID uid, boolean force) throws MongoDataObjectException {
        if(!PlayerHandler.getInstance().isWrapped(EliteTeams.getInstance(), uid)) {
            PlayerHandler.getInstance().addPlayerWrapper(EliteTeams.getInstance(), new TeamsPlayerWrapper(uid), force);
        }

        return (TeamsPlayerWrapper) PlayerHandler.getInstance().getPlayerWrapper(EliteTeams.getInstance(), uid);
    }

    public TeamsPlayerWrapper getPlayerWrapper(Player player) {
        return getPlayerWrapper(player.getUniqueId());
    }

    public void validate(UUID uid) {
        if(!PlayerHandler.getInstance().isWrapped(EliteTeams.getInstance(), uid))
            PlayerHandler.getInstance().addPlayerWrapper(EliteTeams.getInstance(), new TeamsPlayerWrapper(uid));
    }

    public void validate(Player player) {
        validate(player.getUniqueId());
    }

    @EventHandler
    public void onGroupSwitch(PlayerGroupSwitchEvent event) {
        Player player = event.getPlayer();

        if(event.getFrom() != event.getTo()) {
            TeamsPlayerWrapper wrapper = TeamsPlayerHandler.getInstance().getPlayerWrapper(player);
            int maxWarps = RankRewards.DEFAULT.getMaxWarps();

            try {
                RankRewards rewards = RankRewards.valueOf(event.getTo().getGroupName().toUpperCase());

                if(rewards != null) {
                    maxWarps = rewards.getMaxWarps();
                }
            } catch (Exception ex) {}

            wrapper.setMax_warps(maxWarps);
        }
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();

        validate(player);
    }

    @EventHandler
    public void onPlayerLeave(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        TeamsPlayerWrapper wrapper = getPlayerWrapper(player);

        wrapper.setBuilding(false);
    }

    private ConcurrentHashMap<UUID, MoveRequest> currentlyProcessing = new ConcurrentHashMap<>();

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        if(player.isDead()) return;
        TeamsPlayerWrapper wrapper = null;

        if(!LocationUtility.isDifferentBlock(event.getFrom(), event.getTo()) || currentlyProcessing.containsKey(player.getUniqueId())) return;
        MoveRequest request = new MoveRequest(event);
        currentlyProcessing.put(player.getUniqueId(), request);

        Bukkit.getScheduler().runTaskAsynchronously(EliteTeams.getInstance(), request);

        request.addListener(new Runnable() {
            @Override
            public void run() {
                currentlyProcessing.remove(player.getUniqueId());
            }
        }, MoreExecutors.sameThreadExecutor());
    }

    @EventHandler
    public void onPlayerTeleport(PlayerTeleportEvent event) {
        Player player = event.getPlayer();
        if (player.isDead()) return;
        MoveRequest request = new MoveRequest(new PlayerMoveEvent(player, event.getTo(), event.getTo()));

        Bukkit.getScheduler().runTaskAsynchronously(EliteTeams.getInstance(), request);
    }

    public ConcurrentHashMap<UUID, MoveRequest> getCurrentlyProcessing() {
        return currentlyProcessing;
    }

    /**
     * State handling
     */

    @EventHandler
    public void onFoodLoss(FoodLevelChangeEvent event) {
        if(event.getEntity() instanceof Player) {
            Player player = (Player) event.getEntity();

            if(player.getFoodLevel() > event.getFoodLevel()) {
                TeamsPlayerWrapper wrapper = TeamsPlayerHandler.getInstance().getPlayerWrapper(player);

                if(wrapper.getPlayerState() == TeamsPlayerWrapper.TeamsPlayerState.PROTECTED) {
                    event.setCancelled(true);
                }
            }
        }
    }

    @EventHandler
    public void onEntityDamage(EntityDamageEvent event) {
        if((event.getCause() != EntityDamageEvent.DamageCause.VOID && event.getCause() != EntityDamageEvent.DamageCause.CUSTOM) && event.getEntity() instanceof Player) {
            Player player = (Player) event.getEntity();
            TeamsPlayerWrapper wrapper = TeamsPlayerHandler.getInstance().getPlayerWrapper(player);

            if(wrapper.getPlayerState() == TeamsPlayerWrapper.TeamsPlayerState.PROTECTED) {
                event.setCancelled(true);
            }
        }
    }

    /**
     * Handle pearling
     */
    @EventHandler
    public void onProjectileLaunch(ProjectileLaunchEvent event) {
        if (event.getEntity().getShooter() != null && event.getEntity().getShooter() instanceof Player) {
            Player shooter = (Player) event.getEntity().getShooter();
            TeamsPlayerWrapper wrapper = TeamsPlayerHandler.getInstance().getPlayerWrapper(shooter);

            if (event.getEntity() instanceof EnderPearl) {
                EnderPearl pearl = (EnderPearl) event.getEntity();

                wrapper.setLastPearl(pearl);
                wrapper.setLastPearlThrow(System.currentTimeMillis() + PEARL_COOLDOWN);
                BoardHandler.getInstance().getPlayerBoard(shooter).getBoardEntries().get("pearlcooldown").showForTime(PEARL_COOLDOWN);
            }
        }
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player player = event.getEntity();
        TeamsPlayerWrapper wrapper = TeamsPlayerHandler.getInstance().getPlayerWrapper(player);

        wrapper.cleanPearl(player);
    }

    /**
     * Handle achievements
     */

    /**
     * This will do playerjoins along with run an achievement rank check each join
     * @param event
     */
    @EventHandler
    public void onOriginPlayerJoin(OriginPlayerJoinEvent event) {
        Player player = event.getPlayer();
        OriginPlayerWrapper originWrapper = event.getWrapper();
        TeamsPlayerWrapper kitWrapper = (TeamsPlayerWrapper) PlayerHandler.getInstance().getPlayerWrapper(EliteTeams.getInstance(), player);

        if(!kitWrapper.isLoaded()) {
            kitWrapper.makeUnloadedChange(new ActionChange() {
                @Override
                public void change(MongoDataObject mongoDataObject, Object... objects) {
                    kitWrapper.runAchievementCheck(AchievementType.JOINS, originWrapper.getJoins());
                }
            });
        }
        else {
            kitWrapper.runAchievementCheck(AchievementType.JOINS, originWrapper.getJoins());
        }
    }

    @EventHandler
    public void onOriginPlaytimeUpdated(OriginPlayerUpdatedPlaytime event) {
        Player player = event.getPlayer();
        OriginPlayerWrapper wrapper = event.getWrapper();

        TeamsPlayerWrapper kitWrapper = (TeamsPlayerWrapper) PlayerHandler.getInstance().getPlayerWrapper(EliteTeams.getInstance(), player);

        if(!kitWrapper.isLoaded()) {
            kitWrapper.makeUnloadedChange(new ActionChange() {
                @Override
                public void change(MongoDataObject mongoDataObject, Object... objects) {
                    kitWrapper.runAchievementCheck(AchievementType.TIME_PLAYED, wrapper.getPlaytime());
                }
            });
        }
        else {
            kitWrapper.runAchievementCheck(AchievementType.TIME_PLAYED, wrapper.getPlaytime());
        }
    }

    public static TeamsPlayerHandler getInstance() {
        return instance;
    }
    
}

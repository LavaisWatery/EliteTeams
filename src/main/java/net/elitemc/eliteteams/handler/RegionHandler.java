package net.elitemc.eliteteams.handler;

import net.elitemc.commons.util.*;
import net.elitemc.eliteteams.configuration.RegionConfiguration;
import net.elitemc.eliteteams.util.TeamsPlayerWrapper;
import net.elitemc.eliteteams.util.region.*;
import net.elitemc.eliteteams.util.region.event.SelectionEvent;
import org.bukkit.ChatColor;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockFromToEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.entity.PotionSplashEvent;
import org.bukkit.event.hanging.HangingBreakByEntityEvent;
import org.bukkit.event.hanging.HangingPlaceEvent;
import org.bukkit.event.player.PlayerBucketEmptyEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerTeleportEvent;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

/**
 * Created by LavaisWatery on 2017-06-22.
 */
public class RegionHandler extends Handler {
    private static RegionHandler instance;

    public RegionHandler() {
        instance = this;
    }

    public static Material REGION_SELECTION_MATERIAL = Material.FEATHER;

    private RegionConfiguration configuration = null;
    private List<Region> regions = new ArrayList<>();
    private HashMap<String, Region> regionsHash = new HashMap<>();
    //    private HashMap<Chunk, List<Region>> chunkedRegions = new HashMap<>();
    private HashMap<Vector2D, List<Region>> fixed2DRegions = new HashMap<>();

    @Override
    public void init() {
        this.configuration = new RegionConfiguration(this);
    }

    @Override
    public void unload() {

    }

    public boolean allows(Location location, FlagType type) {
        RegionSet applicable = getRegionsApplicable(location);

        return applicable != null ? applicable.allows(type) : true;
    }

    public boolean allows(Player player, FlagType type) {
        return allows(player.getLocation(), type);
    }

    public Region getRegionByIndex(String index) {
        return regionsHash.get(index);
    }

    public RegionSet getRegionsApplicable(Location location) {
        List<Region> chunked = getChunkRegions(location);
        if(chunked == null || chunked.isEmpty()) return new RegionSet(location, Arrays.asList());
        List<Region> regions = new ArrayList<>();
        for(Region region : chunked) {
            if(region.getCuboid().contains(location)) regions.add(region);
        }
        RegionSet set = new RegionSet(location, regions);

        return set;
    }

    public RegionSet getRegionsApplicable(Player player) {
        return getRegionsApplicable(player.getLocation());
    }


    public List<Region> getChunkRegions(Location location) {
        return fixed2DRegions.get(Vector2D.toVec2D(location.getBlockX() >> 4, location.getBlockZ() >> 4));
    }

    public RegionConfiguration getConfiguration() {
        return configuration;
    }

    public void registerRegion(Region region) {
        regions.add(region);
        regionsHash.put(region.getIndex(), region);
        for(Chunk chunk : region.getCuboid().getChunks()) {
            registerRegionChunk(region, chunk);
        }
    }

    public void unregisterRegion(Region region) {
        regions.remove(region);
        regionsHash.remove(region.getIndex());
        for(Chunk chunk : region.getCuboid().getChunks()) {
            unregisterRegionChunk(region, chunk);
        }
    }

    public void unregisterRegionChunk(Region region, Chunk chunk) {
        Vector2D vec = Vector2D.toVec2D(chunk.getX(), chunk.getZ());

        if(fixed2DRegions.containsKey(vec)) {
            fixed2DRegions.get(vec).remove(region);
        }
    }

    public void registerRegionChunk(Region region, Chunk chunk) {
        Vector2D vec = Vector2D.toVec2D(chunk.getX(), chunk.getZ());

        if(!fixed2DRegions.containsKey(vec)) {
            fixed2DRegions.put(vec, new ArrayList<>());
        }

        if(!fixed2DRegions.get(vec).contains(region)) fixed2DRegions.get(vec).add(region);
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        TeamsPlayerWrapper wrapper = TeamsPlayerHandler.getInstance().getPlayerWrapper(player);

        if ((event.getAction() == Action.RIGHT_CLICK_BLOCK || event.getAction() == Action.LEFT_CLICK_BLOCK) && wrapper.getRegionSession() != null && player.getItemInHand() != null && player.getItemInHand().getType() == RegionHandler.REGION_SELECTION_MATERIAL) {
            RegionSession session = wrapper.getRegionSession();
            String pos = session.selectPosition(event.getAction(), event.getClickedBlock());
            PluginUtility.callEvent(new SelectionEvent(player, wrapper, event.getAction(), event.getClickedBlock().getLocation()));
            FakeLocation point = new FakeLocation(event.getAction() == Action.RIGHT_CLICK_BLOCK ? session.getSelectedLocationB() : session.getSelectedLocationA());

            MessageUtility.message(player, false, ChatColor.YELLOW + "You have selected " + pos + " at point " + point.getX() + ", " + point.getY() + ", " + point.getZ());
            return;
        }
    }

    @EventHandler
    public void onPotionSplash(PotionSplashEvent event) {
        for(LivingEntity entity : event.getAffectedEntities()) {
            if(entity instanceof Player) {
                Player target = (Player) entity;
                TeamsPlayerWrapper wrap = TeamsPlayerHandler.getInstance().getPlayerWrapper(target);

                if(wrap.getPlayerState() == TeamsPlayerWrapper.TeamsPlayerState.PROTECTED) event.setIntensity(target, 0);
            }
        }
    }

    /**
     * Explosion flag
     */

    @EventHandler
    public void onEntityExplode(EntityExplodeEvent event) {
        HashMap<Chunk, RegionSet> sets = new HashMap<>();
        List<Block> clear = new ArrayList<>();

        for(Block block : event.blockList()) {
            Chunk chunk = block.getChunk();
            RegionSet set = sets.get(chunk);

            if(set == null) {
                sets.put(chunk, set = getRegionsApplicable(block.getLocation()));
            }

            if(set != null && set.dissallows(FlagType.EXPLOSION)) {
                clear.add(block);
            }
        }

        for(Block b : clear) {
            event.blockList().remove(b);
        }
    }

    /**
     * Block placement
     */

    @EventHandler(priority = EventPriority.LOWEST)
    public void onEntityDamageEvent(EntityDamageByEntityEvent event) {
        if(event.getDamager() instanceof Player) {
            Player damager = (Player) event.getDamager();
            TeamsPlayerWrapper wrapper = TeamsPlayerHandler.getInstance().getPlayerWrapper(damager);

            if(event.getEntity() instanceof EnderCrystal) {
                if(!getRegionsApplicable(event.getEntity().getLocation()).hasRegionType(Region.RegionType.SPAWN)) return;

                if(!wrapper.isBuilding()) {
                    event.setCancelled(true);
                }
            }
        }
        if(event.getDamager() instanceof Projectile) {
            Projectile proj = (Projectile) event.getDamager();
            if(proj.getShooter() instanceof Player) {
                Player shooter = (Player) proj.getShooter();
                TeamsPlayerWrapper wrapper = TeamsPlayerHandler.getInstance().getPlayerWrapper(shooter);
                if(event.getEntity() instanceof EnderCrystal) {
                    if(!getRegionsApplicable(event.getEntity().getLocation()).hasRegionType(Region.RegionType.SPAWN)) return;

                    if(!wrapper.isBuilding()) {
                        event.setCancelled(true);
                    }
                }
            }
        }
    }

    @EventHandler
    public void onPlayerTeleportBuild(PlayerTeleportEvent event) {
        Player player = event.getPlayer();
        if(event.getCause() == PlayerTeleportEvent.TeleportCause.UNKNOWN) return;
        TeamsPlayerWrapper wrapper = TeamsPlayerHandler.getInstance().getPlayerWrapper(player);
        if(wrapper != null)
            wrapper.setBuilding(false);
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event){
        Player player = event.getPlayer();
        TeamsPlayerWrapper wrapper = TeamsPlayerHandler.getInstance().getPlayerWrapper(player);

        if(!getRegionsApplicable(event.getBlock().getLocation()).hasRegionType(Region.RegionType.SPAWN) && (wrapper.getPlayerState() == TeamsPlayerWrapper.TeamsPlayerState.UNPROTECTED || !getRegionsApplicable(player.getLocation()).hasRegionType(Region.RegionType.SPAWN))) return;

        if(!wrapper.isBuilding()) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event){
        Player player = event.getPlayer();
        TeamsPlayerWrapper wrapper = TeamsPlayerHandler.getInstance().getPlayerWrapper(player);

        if(!getRegionsApplicable(event.getBlock().getLocation()).hasRegionType(Region.RegionType.SPAWN) && (wrapper.getPlayerState() == TeamsPlayerWrapper.TeamsPlayerState.UNPROTECTED || !getRegionsApplicable(player.getLocation()).hasRegionType(Region.RegionType.SPAWN))) return;

        if(!wrapper.isBuilding()) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onBucketEmpty(PlayerBucketEmptyEvent event){
        Player player = event.getPlayer();
        TeamsPlayerWrapper wrapper = TeamsPlayerHandler.getInstance().getPlayerWrapper(player);

        if(!getRegionsApplicable(event.getBlockClicked().getLocation()).hasRegionType(Region.RegionType.SPAWN) && (wrapper.getPlayerState() == TeamsPlayerWrapper.TeamsPlayerState.UNPROTECTED || !getRegionsApplicable(player.getLocation()).hasRegionType(Region.RegionType.SPAWN))) return;

        if(!wrapper.isBuilding()) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onFlowChange(BlockFromToEvent event) {
        RegionSet app = getRegionsApplicable(event.getToBlock().getLocation());

        if(app != null && app.dissallows(FlagType.FLOW)) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onPaintingBreakByEntity(HangingBreakByEntityEvent event) {
        if(event.getRemover() instanceof Player) {
            Player player = (Player) event.getRemover();
            TeamsPlayerWrapper wrapper = TeamsPlayerHandler.getInstance().getPlayerWrapper(player);


            if(!getRegionsApplicable(event.getEntity().getLocation()).hasRegionType(Region.RegionType.SPAWN) && (wrapper.getPlayerState() == TeamsPlayerWrapper.TeamsPlayerState.UNPROTECTED || !getRegionsApplicable(player.getLocation()).hasRegionType(Region.RegionType.SPAWN))) return;

            if(!wrapper.isBuilding()) {
                event.setCancelled(true);
            }
        }
        if(event.getRemover() instanceof Projectile) {
            Projectile proj = (Projectile) event.getRemover();

            if(proj.getShooter() instanceof Player) {
                Player player = (Player) proj.getShooter();
                TeamsPlayerWrapper wrapper = TeamsPlayerHandler.getInstance().getPlayerWrapper(player);

                if(!getRegionsApplicable(event.getEntity().getLocation()).hasRegionType(Region.RegionType.SPAWN) && (wrapper.getPlayerState() == TeamsPlayerWrapper.TeamsPlayerState.UNPROTECTED || !getRegionsApplicable(player.getLocation()).hasRegionType(Region.RegionType.SPAWN))) return;

                if(!wrapper.isBuilding()) {
                    event.setCancelled(true);
                }
            }
            else {
                if(!getRegionsApplicable(event.getEntity().getLocation()).hasRegionType(Region.RegionType.SPAWN)) return;

                event.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onItemRemoveFromItemFrame(EntityDamageByEntityEvent event) {
        Entity e = event.getEntity();
        if(e instanceof ItemFrame) {
            if(event.getDamager() instanceof Player) {
                Player player = (Player) event.getDamager();
                TeamsPlayerWrapper wrapper = TeamsPlayerHandler.getInstance().getPlayerWrapper(player);

                if(!getRegionsApplicable(event.getEntity().getLocation()).hasRegionType(Region.RegionType.SPAWN) && (wrapper.getPlayerState() == TeamsPlayerWrapper.TeamsPlayerState.UNPROTECTED || !getRegionsApplicable(player.getLocation()).hasRegionType(Region.RegionType.SPAWN))) return;

                if(!wrapper.isBuilding()) {
                    event.setCancelled(true);
                }
            }
        }
    }

    @EventHandler
    public void onPaintingPlace(HangingPlaceEvent event) {
        Player player = event.getPlayer();
        TeamsPlayerWrapper wrapper = TeamsPlayerHandler.getInstance().getPlayerWrapper(player);

        if(!getRegionsApplicable(event.getEntity().getLocation()).hasRegionType(Region.RegionType.SPAWN) && (wrapper.getPlayerState() == TeamsPlayerWrapper.TeamsPlayerState.UNPROTECTED || !getRegionsApplicable(player.getLocation()).hasRegionType(Region.RegionType.SPAWN))) return;

        if(!wrapper.isBuilding()) {
            event.setCancelled(true);
        }
    }

    public List<Region> getRegions() {
        return regions;
    }

    public static RegionHandler getInstance() {
        return instance;
    }

}
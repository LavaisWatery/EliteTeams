package net.elitemc.eliteteams.handler;

import com.google.common.util.concurrent.MoreExecutors;
import net.elitemc.commons.handler.BoardHandler;
import net.elitemc.commons.handler.PlayerHandler;
import net.elitemc.commons.util.*;
import net.elitemc.commons.util.mongo.MongoDataObject;
import net.elitemc.commons.util.mongo.pooling.ActionChange;
import net.elitemc.commons.util.wrapper.MongoDataObjectException;
import net.elitemc.eliteteams.EliteTeams;
import net.elitemc.eliteteams.util.*;
import net.elitemc.origin.util.OriginPlayerWrapper;
import net.elitemc.origin.util.event.OriginPlayerJoinEvent;
import net.elitemc.origin.util.event.OriginPlayerUpdatedPlaytime;
import net.elitemc.origin.util.event.PlayerGroupSwitchEvent;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.EnderPearl;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.*;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.event.player.*;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapelessRecipe;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by LavaisWatery on 2017-07-22.
 */
public class TeamsPlayerHandler extends Handler {
    private static TeamsPlayerHandler instance;

    public TeamsPlayerHandler() {
        instance = this;

        toolAmount.put("AXE", Integer.valueOf(3));
        toolAmount.put("BOOTS", Integer.valueOf(4));
        toolAmount.put("CHESTPLATE", Integer.valueOf(8));
        toolAmount.put("HELMET", Integer.valueOf(5));
        toolAmount.put("HOE", Integer.valueOf(2));
        toolAmount.put("LEGGINGS", Integer.valueOf(7));
        toolAmount.put("PICKAXE", Integer.valueOf(3));
        toolAmount.put("SPADE", Integer.valueOf(1));
        toolAmount.put("SWORD", Integer.valueOf(2));
        toolAmount.put("BARDING", Integer.valueOf(2));

        faces.add(BlockFace.NORTH);
        faces.add(BlockFace.EAST);
        faces.add(BlockFace.SOUTH);
        faces.add(BlockFace.WEST);

        EliteTeams.getInstance().getServer().addRecipe(new ShapelessRecipe(xpBottles()).addIngredient(1, Material.GLASS_BOTTLE));
    }

    private HashMap<String, Integer> toolAmount = new HashMap<String, Integer>();
    private List<BlockFace> faces = new ArrayList<BlockFace>();

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

        BoardHandler handler = BoardHandler.getInstance();
        handler.getDefaultPreset().apply(handler.getPlayerBoard(player));
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

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerRespawn(PlayerRespawnEvent event) {
        Player player = event.getPlayer();
        TeamsPlayerWrapper wrapper = TeamsPlayerHandler.getInstance().getPlayerWrapper(player);

        wrapper.doProtectionApplyCheck(player, event.getRespawnLocation());

    }

    /**
     * Souping
     */
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerSoup(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        if(player.isDead()) return;
        if (event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK) {
            if (event.getItem() != null && event.getItem().getType() != Material.AIR) {
                if (event.getItem().getType() == Material.MUSHROOM_SOUP) {
                    double health = player.getHealth();
                    double nhealth = health + 7;
                    if (health == player.getMaxHealth()) {
                        return;
                    }
                    if (nhealth >= player.getMaxHealth()) {
                        player.setHealth(player.getMaxHealth());
                    } else {
                        player.setHealth(nhealth);
                    }
                    player.getItemInHand().setType(Material.BOWL);
                    player.updateInventory();
                }
            }
        }
    }

    /**
     * Str fix
     */
    @EventHandler
    public void onPlayerDamage(EntityDamageByEntityEvent event) {
        if (event.getCause().equals(EntityDamageEvent.DamageCause.ENTITY_ATTACK)) {
            if (event.getDamager() != null) {
                if ((event.getDamager() instanceof Player)) {
                    Player player = (Player)event.getDamager();
                    Iterator<PotionEffect> iterator = player.getActivePotionEffects().iterator();
                    while (iterator.hasNext()) {
                        PotionEffect eff = (PotionEffect)iterator.next();
                        if (eff.getType().equals(PotionEffectType.INCREASE_DAMAGE)) {
                            int level = eff.getAmplifier() + 1;
                            event.setDamage(10.0D * event.getDamage() / (10.0D + 13.0D * level) + 13.0D * event.getDamage() * level * 30 / 200.0D / (10.0D + 13.0D * level));
                        }
                    }
                }
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

        broadcastDeath(event);
        wrapper.setBuilding(false);
        wrapper.cleanPearl(player);
    }

    public void broadcastDeath(PlayerDeathEvent event) {
        String message = event.getDeathMessage();

        event.setDeathMessage("");

        for(Player player : PlayerUtility.getOnlinePlayers()) {
            TeamsPlayerWrapper wrapper = TeamsPlayerHandler.getInstance().getPlayerWrapper(player);

            if(((boolean) PlayerOptions.OptionType.DEATH_MESSAGES.getCurrent(wrapper.getPlayerOptions()))) {
                player.sendMessage(message);
            }
        }
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

    /**
     * Salvage / XP
     * @return
     */

    @EventHandler
    public void onPlayerSalvage(PlayerInteractEvent event) {
        Player player = event.getPlayer();

        if(event.getAction() == Action.RIGHT_CLICK_BLOCK) {
            if(player.getItemInHand() != null && player.getItemInHand().getType() != Material.AIR) {
                ItemStack item = player.getItemInHand();
                Block block = event.getClickedBlock();

                if(block.getType() != Material.GOLD_BLOCK && block.getType() != Material.DIAMOND_BLOCK && block.getType() != Material.IRON_BLOCK) {
                    return;
                }

                if ((BlockNear(Material.FURNACE, player.getWorld(), block, -1, 0, 0)) || (BlockNear(Material.FURNACE, player.getWorld(), block, 1, 0, 0)) || (BlockNear(Material.FURNACE, player.getWorld(), block, 0, 0, -1)) || (BlockNear(Material.FURNACE, player.getWorld(), block, 0, 0, 1)) || (BlockNear(Material.FURNACE, player.getWorld(), block, 0, -1, 0)) || (BlockNear(Material.FURNACE, player.getWorld(), block, 0, 1, 0))) {
                    if(isSalvagable(item)) {
                        SalvageType itemSalvageType = getSalvageType(item.getType());
                        SalvageType blockSalvageType = getSalvageType(block.getType());

                        String itemType = item.getType().toString().split("_")[1];

                        if(itemSalvageType == null || blockSalvageType == null) {
                            return;
                        }

                        if(!toolAmount.containsKey(itemType)) {
                            return;
                        }

                        if(itemSalvageType == blockSalvageType) {

                            int amt = calculateSalvageableAmount(item.getDurability(), item.getType().getMaxDurability(), toolAmount.get(itemType));
                            Material mat = null;
                            try {
                                mat = Material.getMaterial(itemSalvageType.toString().toUpperCase() + "_INGOT");
                                if(mat == null) {
                                    mat = Material.getMaterial(itemSalvageType.toString().toUpperCase());
                                }
                            } catch(Exception ex) {
                                MessageUtility.message(player, false, ChatColor.RED + "This shouldn't happen. Report to an admin immediately!");
                                return;
                            }

                            if(mat == null) {
                                return;
                            }
                            if(amt == 0) {
                                MessageUtility.message(player, false, ChatColor.RED + "This item is too damaged!");
                                return;
                            }
                            event.setCancelled(true);

                            player.updateInventory();

                            ItemStack dropItem = new ItemStack(mat, amt);

                            player.playSound(event.getPlayer().getLocation(), Sound.ANVIL_USE, 1.0F, 1.0F);
                            player.getInventory().setItemInHand(null);
                            player.getWorld().dropItem(block.getLocation(), dropItem);
                        }
                        else {
                            return;
                        }
                    }
                    else {
                        event.setCancelled(true);
                        MessageUtility.message(player, false, ChatColor.RED + "This item isn't salvagable.");
                    }
                }
            }
        }

    }

    /// XP Bottles

    @EventHandler
    public void onPlayerCraft(CraftItemEvent event) {
        Player p = (Player)event.getWhoClicked();
        if (event.getCurrentItem().equals(xpBottles())) {
            if (event.isShiftClick()) {
                event.setCancelled(true);
                return;
            }
            ItemStack experiencePotion = event.getCurrentItem();
            ItemMeta experienceMeta = experiencePotion.getItemMeta();
            List<String> stringList = new ArrayList();
            stringList.add("");
            int xpLevel = levelToExp(p.getLevel());
            stringList.add(ChatColor.GOLD + "Exp: " + ChatColor.WHITE + xpLevel);

            experienceMeta.setLore(stringList);
            experiencePotion.setItemMeta(experienceMeta);

            p.setExp(0.0F);
            p.setLevel(0);
        }
    }

    @EventHandler
    public void onExpSplash(PlayerInteractEvent event) {
        Player p = event.getPlayer();
        if (((event.getAction() == Action.RIGHT_CLICK_AIR) || (event.getAction() == Action.RIGHT_CLICK_BLOCK)) && (p.getItemInHand().getType() == Material.EXP_BOTTLE) && (p.getItemInHand().hasItemMeta())) {
            ItemMeta meta = p.getItemInHand().getItemMeta();
            if ((p.getItemInHand().getItemMeta().hasLore()) && (meta.getDisplayName().equals(ChatColor.GOLD + "XP Bottle"))) {
                event.setCancelled(true);
                double exp = Double.parseDouble(((String)meta.getLore().get(1)).split("§f")[1]);
                if (exp < 0.0D) {
                    ItemStack temp = p.getItemInHand().clone();
                    temp.setAmount(p.getItemInHand().getAmount() - 1);
                    p.getInventory().remove(p.getItemInHand());

                    p.getInventory().addItem(new ItemStack[] { temp });
                    p.updateInventory();
                    return;
                }
                ItemStack temp = p.getItemInHand().clone();
                temp.setAmount(p.getItemInHand().getAmount() - 1);
                p.setItemInHand(temp.getAmount() <= 0 ? new ItemStack(Material.AIR) : temp);
                p.updateInventory();
                p.giveExp((int)exp);
            }
        }
    }

    /////

    private int calculateSalvageableAmount(short currentDurability, short maxDurability, int baseAmount) {
        double percentDamaged = (maxDurability <= 0) ? 1D : (double) (maxDurability - currentDurability) / maxDurability;

        return (int) Math.floor(baseAmount * percentDamaged);
    }

    private boolean isSalvagable(ItemStack is) {
        String type = is.getType().toString();

        if(!type.contains("DIAMOND_") && !type.contains("IRON_") && !type.contains("GOLD_")) {
            return false;
        }

        if(is.hasItemMeta() && is.getItemMeta().hasLore() && (is.getItemMeta().getLore().contains(ChatColor.YELLOW + "Unsalvagable") || is.getItemMeta().getLore().contains(ChatColor.YELLOW + "Unsalvagable "))) {
            return false;
        }

        return true;
    }

    private boolean BlockNear(Material mat, World w, Block block, int x, int y, int z) {
        Location loc = new Location(w, block.getX() + x, block.getY() + y, block.getZ() + z);
        Block block2 = w.getBlockAt(loc);
        if (block2.getType() == mat) {
            return true;
        }
        return false;
    }

    private SalvageType getSalvageType(ItemStack item) {
        return getSalvageType(item.getType());
    }

    private SalvageType getSalvageType(Material material) {
        String[] split = material.toString().split("_");

        for(SalvageType type : SalvageType.values()) {
            if(split[0].equalsIgnoreCase(type.toString())) {
                return type;
            }
        }

        return null;
    }

    private enum SalvageType {
        GOLD,
        IRON,
        DIAMOND
    }

    public static int levelToExp(int level) {
        if (level <= 15) {
            return 17 * level;
        } else if (level <= 30) {
            return (3 * level * level / 2) - (59 * level / 2) + 360;
        } else {
            return (7 * level * level / 2) - (303 * level / 2) + 2220;
        }
    }

    public static ItemStack xpBottles() {
        ItemStack xpBottles = new ItemStack(Material.EXP_BOTTLE);
        ItemMeta meta = xpBottles.getItemMeta();
        meta.setDisplayName(ChatColor.GOLD + "XP Bottle");

        xpBottles.setItemMeta(meta);
        return xpBottles;
    }

    public static TeamsPlayerHandler getInstance() {
        return instance;
    }
    
}

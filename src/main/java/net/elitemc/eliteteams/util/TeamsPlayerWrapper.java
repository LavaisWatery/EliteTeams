package net.elitemc.eliteteams.util;

import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import net.elitemc.commons.handler.BoardHandler;
import net.elitemc.commons.util.LocationUtility;
import net.elitemc.commons.util.MessageUtility;
import net.elitemc.commons.util.PlayerUtility;
import net.elitemc.commons.util.json.JSONObject;
import net.elitemc.commons.util.mongo.pooling.PoolAction;
import net.elitemc.commons.util.scoreboard.V2.Board;
import net.elitemc.commons.util.scoreboard.V2.BoardEntry;
import net.elitemc.commons.util.scoreboard.V2.EntryShower;
import net.elitemc.commons.util.wrapper.DataPlayerWrapper;
import net.elitemc.commons.util.wrapper.MongoDataObjectException;
import net.elitemc.eliteteams.EliteTeams;
import net.elitemc.eliteteams.handler.AchievementHandler;
import net.elitemc.eliteteams.handler.RegionHandler;
import net.elitemc.eliteteams.handler.TeamsPlayerHandler;
import net.elitemc.eliteteams.util.region.FlagType;
import net.elitemc.eliteteams.util.region.RegionSession;
import net.elitemc.eliteteams.util.region.RegionSet;
import net.elitemc.eliteteams.util.warp.PlayerWarps;
import net.elitemc.origin.Init;
import net.elitemc.origin.handler.ProfileHandler;
import net.elitemc.origin.handler.WarpHandler;
import net.elitemc.origin.util.OriginPlayerWrapper;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.EnderPearl;
import org.bukkit.entity.Player;

import java.util.*;

/**
 * Created by LavaisWatery on 2017-07-22.
 */
public class TeamsPlayerWrapper extends DataPlayerWrapper {

    public TeamsPlayerWrapper(Player player) {
        super(player, EliteTeams.getInstance(), "teamsprofiles", "teamsprofiles", "Teams Profile");
        kitWrapper = this;
        originWrapper = ProfileHandler.getInstance().getPlayerWrapper(player);
    }

    public TeamsPlayerWrapper(UUID uid) {
        super(uid, EliteTeams.getInstance(), "teamsprofiles", "teamsprofiles", "Teams Profile");
        kitWrapper = this;
        originWrapper = ProfileHandler.getInstance().getPlayerWrapper(uid);
    }

    private BoardHandler boardHandler = BoardHandler.getInstance();

    public static String UNPROTECTED_MESSAGE = ChatColor.GRAY + "You are no longer protected.",
            PROTECTION_APPLIED = ChatColor.GRAY + "You have regained spawn protection.";

    private int kills = 0, deaths = 0, current_killstreak = 0, top_killstreak = 0, max_warps = 5;

    private int basic_keys = 0, omega_keys = 0;

    private double balance = 0.0D;

    private long combatTimer = -1;

    private TeamsPlayerWrapper kitWrapper = null;
    private OriginPlayerWrapper originWrapper = null;

    private TeamsPlayerState playerState = TeamsPlayerState.PROTECTED;
    private Confirmation confirmation = null;

    private TrailParticles selectedTrailParticle = null;

    private Location lastUnprotected = null;
    private Collection<Location> lastShownBlocks;
    private RegionSession regionSession = null;
    private Location lastBlock = null;

    private String description = "";

    private long lastPearlThrow = -1;
    private EnderPearl lastPearl = null;

    private long lastKill = -1;
    private int lastComboKill = 0;

    private long lastDeath = -1;
    private long lastPlatePickup = -1;
    private boolean building = false, frozen = false, matchBypass = false;
    private String lastKit = null;
    private List<GameAchievement> completedAchievements = new ArrayList<>();
    //              kitname -> json info with "dateRewarded"
//    private HashMap<String, JSONObject> rewardedKits = new HashMap<>();
    private HashMap<String, HashMap<String, JSONObject>> rewardedItems = new HashMap<>();

    private PlayerOptions playerOptions;
    private PlayerWarps playerWarps;

    public void cleanPlayer() {
        cleanPlayer(null);
    }

    public void cleanPlayer(Location to) {
        Player player = getPlayer();
        cleanPearl(getID());

        if(player != null) {
            Location respawn = to != null ? to : WarpHandler.getInstance().getRespawnLocation(player.getWorld());
            if(player.isDead()) player.spigot().respawn();
            if(respawn != null) {
                LocationUtility.assureChunk(respawn);
                player.teleport(respawn);
            }
            RegionSet applicable = RegionHandler.getInstance().getRegionsApplicable(respawn);

            if(applicable != null) {
                if(!applicable.allows(FlagType.PVP)) {
                    setPlayerState(TeamsPlayerState.PROTECTED);

                    MoveRequest lastRequest = TeamsPlayerHandler.getInstance().getCurrentlyProcessing().get(player.getUniqueId());

                    if(lastRequest != null) {
                        lastRequest.cancelNextVel();
                    }
                }
            }
        }
    }

    public void doProtectionApplyCheck(Player player, Location target) {
        if(!RegionHandler.getInstance().getRegionsApplicable(target).allows(FlagType.PVP)) {
            setPlayerState(TeamsPlayerState.PROTECTED);
            MessageUtility.message(player, false, PROTECTION_APPLIED);
        }
    }

    public long getCombatTimer() {
        return combatTimer;
    }

    public void scheduleCombat(Player player, long time) {
        this.combatTimer = System.currentTimeMillis() + time;
        if(time > 0) {
            if(player != null && player.isDead()) return;
            Board board = null;

            if((board = BoardHandler.getInstance().getPlayerBoard(getID())) != null) {
                BoardEntry entry = board.getBoardEntries().get("combattimer");

                if(entry != null) {
                    entry.showForTime(time);
                }
            }
        }
        else {
            this.combatTimer = -1;
            Board board = null;

            if((board = BoardHandler.getInstance().getPlayerBoard(getID())) != null) {
                BoardEntry entry = board.getBoardEntries().get("combattimer");

                if(entry != null && entry.getShower() != null) {
                    entry.getShower().cancel();
                }
            }
        }
    }

    public Confirmation getConfirmation() {
        return confirmation;
    }

    public void setConfirmation(Confirmation confirmation) {
        this.confirmation = confirmation;
    }

    public long getLastKill() {
        return lastKill;
    }

    public void setLastKill(long lastKill) {
        this.lastKill = lastKill;
    }

    public int getLastComboKill() {
        return lastComboKill;
    }

    public void setLastComboKill(int lastComboKill) {
        this.lastComboKill = lastComboKill;
    }

    public void cleanPearl(Player player) {
        cleanPearl(player.getUniqueId());
    }

    public void cleanPearl(UUID uid) {
        if(lastPearl != null && !lastPearl.isDead()) lastPearl.remove();
        lastPearlThrow = -1;
        Board board = BoardHandler.getInstance().getPlayerBoard(uid);

        if(board != null) {
            BoardEntry entry = board.getBoardEntries().get("pearlcooldown");

            if(entry != null) {
                EntryShower shower = entry.getShower();

                if (shower != null && !shower.isCancelled()) {
                    shower.cancel();
//                    entry.setShower(null);
                }
            }
        }
    }

    public TrailParticles getSelectedTrailParticle() {
        return selectedTrailParticle;
    }

    public void setSelectedTrailParticle(TrailParticles selectedTrailParticle) {
        this.selectedTrailParticle = selectedTrailParticle;
        if(this.selectedTrailParticle == null) {

        }
    }

    public long getLastPearlThrow() {
        return lastPearlThrow;
    }

    public void setLastPearlThrow(long lastPearlThrow) {
        this.lastPearlThrow = lastPearlThrow;
    }

    public EnderPearl getLastPearl() {
        return lastPearl;
    }

    public void setLastPearl(EnderPearl lastPearl) {
        this.lastPearl = lastPearl;
    }

    public Location getLastUnprotected() {
        return lastUnprotected;
    }

    public void setLastUnprotected(Location lastUnprotected) {
        this.lastUnprotected = lastUnprotected;
    }

    public long getLastDeath() {
        return lastDeath;
    }

    public void setLastDeath(long lastDeath) {
        this.lastDeath = lastDeath;
    }

    public int getMax_warps() {
        return max_warps;
    }

    public void setMax_warps(int max_warps) {
        this.max_warps = max_warps;
        queueAction(PoolAction.SAVE);
    }

    public int getBasic_keys() {
        return basic_keys;
    }

    public void setBasic_keys(int basic_keys) {
        this.basic_keys = basic_keys;
        queueAction(PoolAction.SAVE);
        runAchievementCheck(AchievementType.BASIC_KEYS, basic_keys);
    }

    public int getOmega_keys() {
        return omega_keys;
    }

    public void setOmega_keys(int omega_keys) {
        this.omega_keys = omega_keys;
        queueAction(PoolAction.SAVE);
        runAchievementCheck(AchievementType.OMEGA_KEYS, omega_keys);
    }

    @Override
    public boolean loadData() throws MongoDataObjectException {
        if(getID() == null) throw new MongoDataObjectException(PoolAction.LOAD, "ID is null");
        if(isLoaded()) throw new MongoDataObjectException(PoolAction.LOAD, "EliteTeams profile already loaded");
        DBCollection collection = Init.getInstance().getMongoCollection(getCollectionName());
        BasicDBObject fetched = fetchCurrentObject(collection);

        if(fetched != null) {
            try {
                if(fetched.containsKey("kills")) kills = fetched.getInt("kills");
                if(fetched.containsKey("deaths")) deaths = fetched.getInt("deaths");
                if(fetched.containsKey("current_killstreak")) current_killstreak = fetched.getInt("current_killstreak");
                if(fetched.containsKey("top_killstreak")) top_killstreak = fetched.getInt("top_killstreak");
                if(fetched.containsKey("balance")) balance = fetched.getDouble("balance");
                if(fetched.containsKey("basic_keys")) basic_keys = fetched.getInt("basic_keys");
                if(fetched.containsKey("omega_keys")) omega_keys = fetched.getInt("omega_keys");
                if(fetched.containsKey("max_warps")) max_warps = fetched.getInt("max_warps");

                try {
                    if(fetched.containsKey("state")) playerState = TeamsPlayerState.valueOf(fetched.getString("state"));
                } catch (Exception ex) {}

                if(fetched.containsKey("description")) description = fetched.getString("description");

                if(fetched.containsKey("achievements")) {
                    BasicDBList achievements = (BasicDBList) fetched.get("achievements");

                    for(Object oj : achievements) {
                        String key = (String) oj;
                        if(key.equalsIgnoreCase("_id")) continue;

                        try {
                            GameAchievement achievement = GameAchievement.valueOf(key);

                            if(achievement != null && !completedAchievements.contains(achievement)) completedAchievements.add(achievement);
                        } catch (Exception ex) {
                            queueAction(PoolAction.SAVE);
                        }
                    }
                }

                if(fetched.containsKey("playeroptions")) {
                    playerOptions = new PlayerOptions(this, fetched.getString("playeroptions"));
                }
                else {
                    playerOptions = new PlayerOptions(this);

                    for(PlayerOptions.OptionType type : PlayerOptions.OptionType.values()) {
                        type.initOption(playerOptions);
                    }
                }

                if(fetched.containsKey("playerwarps")) {
                    playerWarps = new PlayerWarps(this, fetched.getString("playerwarps"));
                }
                else {
                    playerWarps = new PlayerWarps(this);
                }

                /**
                 * Display kitname with date recieved,
                 */
                if(fetched.containsKey("playerrewards")) {
                    BasicDBObject rewards = (BasicDBObject) fetched.get("playerrewards");

                    if (!rewards.isEmpty()) {
                        for (Map.Entry<String, Object> entry : rewards.entrySet()) {
                            String category = entry.getKey();
                            if (category.equalsIgnoreCase("_id")) continue;
                            JSONObject catItems = new JSONObject(entry.getValue().toString());

                            if (!rewardedItems.containsKey(category))
                                rewardedItems.put(category, new HashMap<>());

                            HashMap<String, JSONObject> catReward = rewardedItems.get(category);
                            for (Map.Entry<String, Object> catEntry : catItems.toMap().entrySet()) {
                                catReward.put(catEntry.getKey(), new JSONObject(catEntry.getValue()));
                            }
                        }
                    }
                }

                setLoaded(true);
                setLoadedObject(fetched);
                boardHandler.getPlayerBoard(getID()).getBufferedObjective().update();
            } catch (Exception ex) {
                throw new MongoDataObjectException(PoolAction.LOAD, ex.getMessage());
            }
            return true;
        }
        else {
            playerOptions = new PlayerOptions(this);
            playerWarps = new PlayerWarps(this);

            if(originWrapper.getPermissionsGroup() != null) {
                try {
                    RankRewards rewards = RankRewards.valueOf(originWrapper.getPermissionsGroup().getGroupName().toUpperCase());

                    if(rewards != null) {
                        if(rewards.getEntries() != null) {
                            for(Map.Entry<StatType, Object> reward : rewards.getEntries()) {
                                reward.getKey().makeChange(this, reward.getValue());
                            }
                            Player player = getPlayer();

                            if(player != null) {
                                StringBuilder builder = new StringBuilder();

                                for (Map.Entry<StatType, Object> entry : rewards.getEntries()) {
                                    StatType type = entry.getKey();
                                    String to = entry.getValue().toString();

                                    if (builder.length() == 0) {
                                        builder.append((type == StatType.BASIC_KEY || type == StatType.OMEGA_KEY ? "x" : "") + to + " " + type.getDisplay());
                                    } else {
                                        builder.append(", " + (type == StatType.BASIC_KEY || type == StatType.OMEGA_KEY ? "x" : "") + to + " " + type.getDisplay());
                                    }
                                }

                                MessageUtility.message(player, false, "Given Rewards: " + builder.toString());
                                MessageUtility.sendStaffChatMessage(player, "gave " + builder.toString() + " rewards to player.");
                            }
                        }
                    }
                } catch (Exception ex) { }
            }

            queueAction(PoolAction.SAVE);
            setLoaded(true);

            return true;
        }
    }

    @Override
    public boolean saveData() {
        if(getID() == null) return false;
        if(!isLoaded()) {
            try {
                loadData();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
            return false;
        }

        try {
            DBCollection collection = Init.getInstance().getMongoCollection(getCollectionName());
            BasicDBObject fetched = fetchCurrentObject(collection);

            if(fetched != null) {
                collection.findAndModify(fetched, createObject());
            }
            else {
                collection.insert(createObject());
            }

            return true;
        } catch (Exception ex) {
            ex.printStackTrace();
            return false;
        }
    }

    @Override
    public boolean clearData() {
        return false;
    }

    public PlayerOptions getPlayerOptions() {
        return playerOptions;
    }

    public OriginPlayerWrapper getOriginWrapper() {
        return originWrapper;
    }

    public TeamsPlayerState getPlayerState() {
        return playerState;
    }

    public PlayerWarps getPlayerWarps() {
        return playerWarps;
    }

    public void setPlayerState(TeamsPlayerState playerState) {
        this.playerState = playerState;
        queueAction(PoolAction.SAVE);
        boardHandler.getPlayerBoard(getID()).getBufferedObjective().updateEntry("protection", "");
    }

    public Location getLastBlock() {
        return lastBlock;
    }

    public void setLastBlock(Location lastBlock) {
        this.lastBlock = lastBlock;
    }

    public boolean hasItem(String category, String itemName) {
        return rewardedItems.containsKey(category) && rewardedItems.get(category).containsKey(itemName.toLowerCase()) ? true : false;
    }

    public boolean awardItem(String category, String itemname, long rewardedTime) {
        if(!rewardedItems.containsKey(category))
            rewardedItems.put(category, new HashMap<>());

        HashMap<String, JSONObject> rewarded = rewardedItems.get(category);
        if(!rewarded.containsKey(itemname.toLowerCase())) {
            rewarded.put(itemname.toLowerCase(), new JSONObject().put("kitName", itemname).put("rewardedOn", rewardedTime));
            queueAction(PoolAction.SAVE);
            return true;
        }
        return false;
    }

    public boolean awardKit(String kitname, long rewardedTime) {
        return awardItem("kits", kitname, rewardedTime);
    }

    public HashMap<String, HashMap<String, JSONObject>> getRewardedItems() {
        return rewardedItems;
    }

    public RegionSession getRegionSession() {
        return regionSession;
    }

    public void setRegionSession(RegionSession regionSession) {
        this.regionSession = regionSession;
    }

    /**
     * this will return all achievements, including general + rank achievements
     * this will be for optimization purposes
     * @return
     */
    public List<GameAchievement> getAllApplicable(AchievementType type) {
        List<GameAchievement> applicable = new ArrayList<>();

        {
            List<GameAchievement> gen = AchievementHandler.getInstance().getGeneralForType(type);

            if(gen != null && !gen.isEmpty()) {
                for(GameAchievement achievement : gen) {
                    if(!applicable.contains(achievement)) applicable.add(achievement);
                }
            }
        }

        return applicable;
    }

    public List<GameAchievement> getAllApplicable() {
        List<GameAchievement> applicable = new ArrayList<>();

        {
            List<GameAchievement> gen = AchievementHandler.getInstance().getGeneralAchievements();

            if(gen != null && !gen.isEmpty()) {
                for(GameAchievement achievement : gen) {
                    if(!completedAchievements.contains(achievement) && !applicable.contains(achievement)) applicable.add(achievement);
                }
            }
        }

        return applicable;
    }

    public void runAchievementCheck(AchievementType type, Object input) {
        List<GameAchievement> applicable = getAllApplicable(type);
        boolean nex = false;

        for(GameAchievement achievement : applicable) {
            if(completedAchievements.contains(achievement)) continue;
            if(hasPointForAchievement(achievement, input)) {
                awardAchievement(achievement);
                if(nex == false && !achievement.isGeneral()) nex = true;
            }
        }
    }

    public boolean hasPointForAchievement(GameAchievement achievement, Object input) {
        switch (achievement.getType()) {
            case JOINS:
            case BALANCE:
            case TOP_KILLSTREAK:
            case CURRENT_KILLSTREAK:
            case TIME_PLAYED:
            case BASIC_KEYS:
            case OMEGA_KEYS:
            case KILLS:
            {
                return ((Integer) input) >= ((Integer) achievement.getTarget());
            }
            case MAKE_A_TEAM:
            {
                return input != null;
            }
        }

        return false;
    }

    public void awardAchievement(GameAchievement achievement) {
        if(!completedAchievements.contains(achievement)) {
            Player player = getPlayer();

            completedAchievements.add(achievement);
            queueAction(PoolAction.SAVE);
            if(player != null) MessageUtility.message(player, false, ChatColor.GOLD + "You have earned the achievement " + achievement.getDisplay() + ".");
            if(achievement.getReward() != null) {
                StatType type = achievement.getReward().getKey();
                Object oj = achievement.getReward().getValue();

                achievement.getReward().getKey().makeChange(this, achievement.getReward().getValue());
                if(player != null) MessageUtility.message(player, false, ChatColor.GOLD + "You have been rewarded " + ChatColor.YELLOW + oj.toString() + " " + type.getDisplay() + ChatColor.GOLD + ".");
            }
        }
    }

    public long getLastPlatePickup() {
        return lastPlatePickup;
    }

    public void setLastPlatePickup(long lastPlatePickup) {
        this.lastPlatePickup = lastPlatePickup;
    }

    public boolean isFrozen() {
        return frozen;
    }

    public void setFrozen(boolean frozen) {
        this.frozen = frozen;
    }

    public boolean isBuilding() {
        return building;
    }

    public void setBuilding(boolean building) {
        this.building = building;
    }

    public int getKills() {
        return kills;
    }

    public void setKills(int kills) {
        this.kills = kills;
        boardHandler.getPlayerBoard(getID()).getBufferedObjective().updateEntry("kills", "");
        queueAction(PoolAction.SAVE);
        runAchievementCheck(AchievementType.KILLS, kills);
    }

    public int getDeaths() {
        return deaths;
    }

    public void setDeaths(int deaths) {
        this.deaths = deaths;
        boardHandler.getPlayerBoard(getID()).getBufferedObjective().updateEntry("deaths", "");
        queueAction(PoolAction.SAVE);
    }

    public int getCurrent_killstreak() {
        return current_killstreak;
    }

    public void setCurrent_killstreak(int current_killstreak) {
        this.current_killstreak = current_killstreak;
        if(this.top_killstreak < current_killstreak) setTop_killstreak(current_killstreak);
        queueAction(PoolAction.SAVE);
        boardHandler.getPlayerBoard(getID()).getBufferedObjective().updateEntry("streak", "");
        runAchievementCheck(AchievementType.CURRENT_KILLSTREAK, current_killstreak);
    }

    public int getTop_killstreak() {
        return top_killstreak;
    }

    public void setTop_killstreak(int top_killstreak) {
        this.top_killstreak = top_killstreak;
        queueAction(PoolAction.SAVE);
        runAchievementCheck(AchievementType.TOP_KILLSTREAK, top_killstreak);
    }

    public double getBalance() {
        return balance;
    }

    public void setBalance(double balance) {
        this.balance = balance;
        queueAction(PoolAction.SAVE);
        runAchievementCheck(AchievementType.BALANCE, balance);
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
        queueAction(PoolAction.SAVE);
    }

    public List<GameAchievement> getCompletedAchievements() {
        return completedAchievements;
    }
    public void setCompletedAchievements(List<GameAchievement> completedAchievements) {
        this.completedAchievements = completedAchievements;
    }

    public enum TeamsPlayerState {
        PROTECTED,

        UNPROTECTED;
    }

    @Override
    public BasicDBObject fetchCurrentObject(DBCollection collection) {
        DBCursor cursor = collection.find(new BasicDBObject("uid", getID().toString()));

        if(cursor.hasNext()) {
            return (BasicDBObject) cursor.next();
        }
        else {
            return null;
        }
    }

    @Override
    public BasicDBObject createObject() {
        BasicDBObject object = new BasicDBObject("uid", getID().toString());

        object.put("kills", kills);
        object.put("deaths", deaths);
        object.put("current_killstreak", current_killstreak);
        object.put("top_killstreak", top_killstreak);
        object.put("balance", balance);
        object.put("basic_keys", basic_keys);
        object.put("omega_keys", omega_keys);
        object.put("max_warps", max_warps);
        object.put("lastkit", lastKit);

        object.put("state", playerState.toString());

        object.put("description", description);

        if(!completedAchievements.isEmpty()) {
            BasicDBList compl = new BasicDBList();

            for (GameAchievement achievement : completedAchievements) {
                if (!compl.contains(achievement.toString())) compl.add(achievement.toString());
            }

            object.put("achievements", compl);
        }

        if(playerOptions != null) {
            object.put("playeroptions", playerOptions.serialize().toString());
        }

        if(!rewardedItems.isEmpty()) {
            BasicDBObject rewards = new BasicDBObject();

            for(Map.Entry<String, HashMap<String, JSONObject>> entry : rewardedItems.entrySet()) {
                String cat = entry.getKey();

                if(!entry.getValue().isEmpty()) {
                    JSONObject catItems = new JSONObject();

                    for(Map.Entry<String, JSONObject> catEntry : entry.getValue().entrySet()) {
                        catItems.put(catEntry.getKey(), catEntry.getValue());
                    }

                    rewards.put(cat, catItems.toString());
                }
            }

            object.put("playerrewards", rewards);
        }

        if(playerWarps != null && !playerWarps.getPlayerWarps().isEmpty()) {
            object.put("playerwarps", playerWarps.serialize().toString());
        }

        return object;
    }

}

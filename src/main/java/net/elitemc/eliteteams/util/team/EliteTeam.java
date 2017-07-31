package net.elitemc.eliteteams.util.team;

import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import mkremins.fanciful.FancyMessage;
import net.elitemc.commons.util.MessageUtility;
import net.elitemc.commons.util.mongo.MongoDataObject;
import net.elitemc.commons.util.mongo.pooling.PoolAction;
import net.elitemc.commons.util.wrapper.MongoDataObjectException;
import net.elitemc.eliteteams.EliteTeams;
import net.elitemc.eliteteams.handler.TeamsHandler;
import net.elitemc.eliteteams.util.team.excep.TeamAddPlayerException;
import net.elitemc.eliteteams.util.team.excep.TeamInvitePlayerException;
import net.elitemc.origin.Init;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.text.DecimalFormat;
import java.util.*;

/**
 * Created by LavaisWatery on 2017-07-31.
 */
public class EliteTeam extends MongoDataObject {

    public EliteTeam(String teamName) {
        super(EliteTeams.getInstance(), "teamsdata", TEAM_COLLECTION, "Team Profile");
        this.teamName = teamName;
    }

    public static String TEAM_COLLECTION = "teamsdata";
    //TODO make it possible to configure max teams + other
    public static int MAX_PLAYERS = 5, MAX_TEAMNAME_LENGTH = 14, MIN_TEAMNAME_LENGTH = 3;
    public static int MEMBER_DEFAULT = 0, MEMBER_MANAGER = 1, MEMBER_OWNER = 2;
    public static long INVITE_TIMEOUT = 30;

    private String teamName;
    private String description = "";

    private HashMap<UUID, Long> playerInvites = new HashMap<>();
    private List<UUID> teamChat = new ArrayList<>();
    private List<UUID> members = new ArrayList<>();
    private HashMap<UUID, Integer> playerRanks = new HashMap<>();

    private int kills = 0, deaths = 0;

    private BasicDBObject data = null;

    public boolean isInviteExpired(UUID target) {
        if(playerInvites.containsKey(target)) {
            return playerInvites.get(target) <= System.currentTimeMillis();
        }

        return true;
    }

    public void invitePlayer(UUID invite) throws TeamInvitePlayerException {
        if(TeamsHandler.getInstance().getPlayerTeam(invite) != null) throw new TeamInvitePlayerException(this, invite, "Player is already on a team.");
        boolean invited = false;

        if(!playerInvites.containsKey(invite)) {
            invited = true;
            playerInvites.put(invite, System.currentTimeMillis() + (1000 * INVITE_TIMEOUT));
        }
        else {
            playerInvites.remove(invite);
        }

        if (Bukkit.getPlayer(invite) != null) {
            if(invited) new FancyMessage(ChatColor.DARK_AQUA + "You have been invited to " + ChatColor.AQUA + teamName + ChatColor.DARK_AQUA + ". " + ChatColor.GREEN + "(click to join)").command("/team join " + teamName).send(Bukkit.getPlayer(invite));
            else MessageUtility.message(Bukkit.getPlayer(invite), false, ChatColor.RED + "Your invite to " + teamName + " has been revoked.");
        }
    }

    public boolean isMember(UUID uid) {
        return playerRanks.containsKey(uid) && playerRanks.get(uid) >= MEMBER_DEFAULT;
    }

    public boolean isMember(Player player) {
        return isMember(player.getUniqueId());
    }

    public boolean isManager(UUID uid) {
        return playerRanks.containsKey(uid) && playerRanks.get(uid) >= MEMBER_MANAGER;
    }

    public boolean isManager(Player player) {
        return isManager(player.getUniqueId());
    }

    public boolean isOwner(UUID uid) {
        return playerRanks.containsKey(uid) && playerRanks.get(uid) >= MEMBER_OWNER;
    }

    public boolean isOwner(Player player) {
        return isOwner(player.getUniqueId());
    }

    public HashMap<UUID, Long> getPlayerInvites() {
        return playerInvites;
    }

    public List<UUID> getTeamChat() {
        return teamChat;
    }

    public boolean inTeamChat(UUID uid) {
        return playerRanks.containsKey(uid) && teamChat.contains(uid);
    }

    public boolean inTeamChat(Player player) {
        return inTeamChat(player.getUniqueId());
    }

    public void setMember(Player player) throws TeamAddPlayerException {
        setMember(player.getUniqueId());
//        NameTagHandler.getInstance().addToUpdateQueue(player);
    }

    public void setMember(UUID uid) throws TeamAddPlayerException {
        if(!playerRanks.containsKey(uid) && TeamsHandler.getInstance().getPlayerTeam(uid) != null) {
            throw new TeamAddPlayerException(this, uid, "This player is already on a team.");
        }
        playerRanks.put(uid, MEMBER_DEFAULT);
        if(!members.contains(uid)) members.add(uid);
        TeamsHandler.getInstance().setPlayerTeam(uid, this);
        queueAction(PoolAction.SAVE);
    }

    public void setManager(Player player) throws TeamAddPlayerException {
        setManager(player.getUniqueId());
//        NameTagHandler.getInstance().addToUpdateQueue(player);
    }

    public void setManager(UUID uid) throws TeamAddPlayerException {
        if(!playerRanks.containsKey(uid) && TeamsHandler.getInstance().getPlayerTeam(uid) != null) {
            throw new TeamAddPlayerException(this, uid, "This player is already on a team.");
        }

        playerRanks.put(uid, MEMBER_MANAGER);
        if(!members.contains(uid)) members.add(uid);
        TeamsHandler.getInstance().setPlayerTeam(uid, this);

        queueAction(PoolAction.SAVE);
    }

    public void setOwner(Player player) throws TeamAddPlayerException {
        setOwner(player.getUniqueId());
//        NameTagHandler.getInstance().addToUpdateQueue(player);
    }

    public void setOwner(UUID uid) throws TeamAddPlayerException {
        if(!playerRanks.containsKey(uid) && TeamsHandler.getInstance().getPlayerTeam(uid) != null) {
            throw new TeamAddPlayerException(this, uid, "This player is already on a team.");
        }
        playerRanks.put(uid, MEMBER_OWNER);
        if(!members.contains(uid)) members.add(uid);
        TeamsHandler.getInstance().setPlayerTeam(uid, this);

        queueAction(PoolAction.SAVE);
    }

    public boolean demotePlayer(UUID uid) {
        if(isManager(uid)) {
            playerRanks.put(uid, MEMBER_DEFAULT);
            if(!members.contains(uid)) members.add(uid);
            queueAction(PoolAction.SAVE);
            return true;
        }

        return false;
    }

    public void purge(UUID uid) {
        boolean isManager = false;

        playerRanks.remove(uid);
        if(members.contains(uid)) members.remove(uid);
        TeamsHandler.getInstance().setPlayerTeam(uid, null);
//        if(Bukkit.getPlayer(uid) != null) NameTagHandler.getInstance().addToUpdateQueue(uid);
    }

    public void leave(Player player) {
        leave(player.getUniqueId());
//        NameTagHandler.getInstance().addToUpdateQueue(player);
    }

    public void leave(UUID uid) {
        boolean isOwner = isOwner(uid);

        if(isOwner) {
            if(playerRanks.size() <= 1) {
                dispose();
            }
            else {
                if(!getMembersFromRank(MEMBER_MANAGER).isEmpty()) {
                    List<UUID> managers = getMembersFromRank(MEMBER_MANAGER);
                    UUID ranMan = managers.get(new Random().nextInt(managers.size()));

                    if(ranMan != null) {
                        try {
                            setOwner(ranMan);
                            purge(uid);
                        } catch (Exception ex) {
                            ex.printStackTrace();
                            dispose();
                        }
                    }
                    else {
                        dispose();
                    }
                }
                else {
                    if(!getMembersFromRank(MEMBER_DEFAULT).isEmpty()) {
                        List<UUID> defaults = getMembersFromRank(MEMBER_DEFAULT);
                        UUID ranMan = defaults.get(new Random().nextInt(defaults.size()));

                        if (ranMan != null) {
                            try {
                                setOwner(ranMan);
                                purge(uid);
                            } catch (Exception ex) {
                                ex.printStackTrace();
                                dispose();
                            }
                        } else {
                            dispose();
                        }
                    }
                    else {
                        dispose();
                    }
                }
            }
        }
        else {
            purge(uid);
        }

        queueAction(PoolAction.SAVE);
    }

    public int getKills() {
        return kills;
    }

    public int getDeaths() {
        return deaths;
    }

    public void setKills(int kills) {
        this.kills = kills;
        queueAction(PoolAction.SAVE);
    }

    public void setDeaths(int deaths) {
        this.deaths = deaths;
        queueAction(PoolAction.SAVE);
    }

    public List<UUID> getMembersFromRank(int rank) {
        List<UUID> members = new ArrayList<>();

        for(Map.Entry<UUID, Integer> set : playerRanks.entrySet()) {
            if(set.getValue() == rank && !members.contains(set.getKey())) members.add(set.getKey());
        }

        return members;
    }

    public void dispose() {
        HashMap<UUID, Integer> members = new HashMap<>(playerRanks);

        for(UUID teamMember : members.keySet()) {
            purge(teamMember);
        }

        if(TeamsHandler.getInstance().getTeams().containsValue(this)) {
            TeamsHandler.getInstance().getTeams().put(teamName.toLowerCase(), null);
        }

        try {
            if(clearData()) {

            }
        } catch (Exception ex) {
            sendMassMessage("Trouble disposing team.");
        }
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public HashMap<UUID, Integer> getPlayerRanks() {
        return playerRanks;
    }

    public String getTeamName() {
        return teamName;
    }

    public void sendMassMessage(String message) {
        for(UUID uid : members) {
            if(Bukkit.getPlayer(uid) != null) MessageUtility.message(Bukkit.getPlayer(uid), false, message);
        }
    }

    public void sendTeamChat(Player player, String message) {
        sendMassMessage(ChatColor.AQUA.toString() + ChatColor.BOLD.toString() + "TEAM " + ChatColor.DARK_AQUA + player.getName() + ": " + ChatColor.GRAY + message);
    }

    @Override
    public boolean loadData() throws MongoDataObjectException {
        if(isLoaded()) return false;
        DBCollection playerTeamCollection = Init.getInstance().getMongoDatabase().getDatabase().getCollection(getCollectionName());
        DBCursor cursor = playerTeamCollection.find(new BasicDBObject("lowerName", teamName.toLowerCase()));

        if(cursor.hasNext()) {
            BasicDBObject teamObject = (BasicDBObject) cursor.next();

            if(teamObject.containsKey("teamName")) {
                this.teamName = teamObject.getString("teamName");
            }

            if(teamObject.containsKey("description")) {
                this.description = teamObject.getString("description");
            }

            if(teamObject.containsKey("members")) {
                BasicDBList members = (BasicDBList) teamObject.get("members");

                for(String key : members.keySet()) {
                    if(key.equalsIgnoreCase("_id")) continue;
                    UUID member = UUID.fromString((String) members.get(key));

                    if(member != null) {
                        if(!this.members.contains(member)) this.members.add(member);
                        TeamsHandler.getInstance().setPlayerTeam(member, this);
                    }
                }
            }

            if(teamObject.containsKey("ranks")) {
                BasicDBObject ranks = (BasicDBObject) teamObject.get("ranks");

                for(String key : ranks.keySet()) {
                    if(key.equalsIgnoreCase("_id")) continue;
                    UUID member = UUID.fromString(key);

                    if(member != null && !playerRanks.containsKey(member)) {
                        playerRanks.put(member, ranks.getInt(key));
                    }
                }
            }

            { // member fix
                List<UUID> rem = new ArrayList<>();
                for (UUID member : members) {
                    if (!playerRanks.containsKey(member)) {
                        rem.add(member);
                    }
                }
                for(UUID re : rem) {
                    members.remove(re);
                }
            }

            if(teamObject.containsKey("kills")) {
                this.kills = teamObject.getInt("kills");
            }
            if(teamObject.containsKey("deaths")) {
                this.deaths = teamObject.getInt("deaths");
            }

            this.data = teamObject;
            setLoaded(true);

            return true;
        }
        else {
            return false;
        }
    }

    @Override
    public boolean saveData() throws MongoDataObjectException {
        if(!isLoaded()) {
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
    public boolean clearData() throws MongoDataObjectException {
        if(data != null && isLoaded()) {
            if(!isLoaded()) {
                dispose();
            }
            setLoaded(false);
            Init.getInstance().getMongoDatabase().getDatabase().getCollection(getCollectionName()).remove(data);
            return true;
        }

        return false;
    }

    public BasicDBObject fetchCurrentObject(DBCollection collection) {
        DBCursor cursor = collection.find(new BasicDBObject("teamName", teamName));

        if(cursor.hasNext()) {
            return (BasicDBObject) cursor.next();
        }
        else {
            return null;
        }
    }

    public static DecimalFormat decFormat = new DecimalFormat(".##");

    public double calcRatio() {
        if(deaths == 0) {
            return (double) kills;
        }
        else {
            return Double.parseDouble(decFormat.format((double) kills / (double) deaths));
        }
    }

    @Override
    public BasicDBObject createObject() {
        BasicDBObject team = new BasicDBObject("teamName", teamName);
        team.put("lowerName", teamName.toLowerCase());

        team.put("description", description);

        { //REMEMBER to load ranks before members
            BasicDBObject ranks = new BasicDBObject();

            for (Map.Entry<UUID, Integer> set : playerRanks.entrySet()) {
                UUID member = set.getKey();
                int rank = set.getValue();

                if (!ranks.containsKey(member.toString())) ranks.put(member.toString(), rank);
            }

            team.put("ranks", ranks);
        }

        { // members
            BasicDBList members = new BasicDBList();

            for (UUID member : this.members) {
                if (!members.contains(member.toString()) && playerRanks.containsKey(member)) members.add(member.toString());
            }

            team.put("members", members);
        }

        { // stats
            team.put("kills", kills);
            team.put("deaths", deaths);
            team.put("ratio", calcRatio());
        }

        return team;
    }

}

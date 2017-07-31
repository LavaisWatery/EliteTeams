package net.elitemc.eliteteams.util.region;

import net.elitemc.commons.util.Cuboid;
import net.elitemc.commons.util.MessageUtility;
import net.elitemc.commons.util.PlayerUtility;
import net.elitemc.commons.util.StringUtility;
import net.elitemc.commons.util.interf.JsonSerializable;
import net.elitemc.commons.util.json.JSONObject;
import net.elitemc.eliteteams.handler.RegionHandler;
import net.elitemc.eliteteams.util.BlockPos;
import net.elitemc.eliteteams.util.IItem;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by LavaisWatery on 2017-06-22.
 */
public class Region implements JsonSerializable, IItem {

    public Region(String index, Location a, Location b) {
        this.index = index;
        this.cuboid = new Cuboid(a, b);
    }

    public Region(String index, Cuboid cuboid) {
        this.index = index;
        this.cuboid = cuboid;
    }

    public Region(String input) {
        deserialize(new JSONObject(input));
    }

    public Region(JSONObject input) {
        deserialize(input);
    }

    public static String[] regionUsage = { ChatColor.GOLD + "Region Changes",
            ChatColor.GRAY + "- flag [type]",
            ChatColor.GRAY + "- view",
            ChatColor.GRAY + "- delete"
    };

    private String index;
    private Cuboid cuboid;
    private RegionType type = null;
    private HashMap<FlagType, RegionFlag> regionFlags = new HashMap<>();

    public boolean inside(Location location) {
        return cuboid.contains(location);
    }

    public boolean inside(Player player) {
        return inside(player.getLocation());
    }

    public boolean allows(FlagType type) {
        return regionFlags.containsKey(type) ? regionFlags.get(type).allows() : true;
    }

    public boolean has(FlagType type) {
        return regionFlags.containsKey(type);
    }

    public String getIndex() {
        return index;
    }

    public Cuboid getCuboid() {
        return cuboid;
    }

    public void setCuboid(Cuboid cuboid) {
        this.cuboid = cuboid;
    }

    public RegionType getType() {
        return type;
    }

    public void setType(RegionType type) {
        this.type = type;
    }

    public BlockPos getMax() {
        return new BlockPos(cuboid.getUpperSW());
    }

    public BlockPos getMin() {
        return new BlockPos(cuboid.getLowerNE());
    }

    public Collection<BlockPos> getPoints() {
        Set<BlockPos> points = new HashSet<>();

//        points.add(new BlockPos(cuboid.getLowerNE()));
//        points.add(new BlockPos(cuboid.getUpperSW()));

        int x1 = getMin().getX();
        int x2 = getMax().getX();
        int z1 = getMin().getZ();
        int z2 = getMax().getZ();
        int y = getMin().getY();
        World world = getCuboid().getWorld();

        points.add(new BlockPos(x1, y, z1, world));
        points.add(new BlockPos(x2, y, z1, world));
        points.add(new BlockPos(x2, y, z2, world));
        points.add(new BlockPos(x1, y, z2, world));

        return points;
    }

    @Override
    public String prettyItemDisplay() {
        String regionInfo = ChatColor.GOLD + "Region " + index + "\n" +
                ChatColor.YELLOW + cuboid.toString() + "\n" +
                ChatColor.YELLOW + "Type: " + type.toString() + "\n";

        for(RegionFlag flag : regionFlags.values()) {
            regionInfo = regionInfo + flag.prettyItemDisplay();
        }

        return regionInfo;
    }

    @Override
    public boolean handleChange(CommandSender sender, String[] args) {
        if(args.length == 0) {
            MessageUtility.sendList(sender, regionUsage);
            return false;
        }
        Player player = (Player) sender;
        RegionHandler handler = RegionHandler.getInstance();

        switch(args[0].toLowerCase()) {
            case "settype": {
                if(args.length <= 1) {
                    MessageUtility.message(player, false, "You must input proper type.");
                    MessageUtility.message(player, false, ChatColor.RED + "Region Types: " + Region.RegionType.toPrettyList());
                    return false;
                }
                Region.RegionType type = null;

                if(args.length >= 2) {
                    try {
                        type = Region.RegionType.valueOf(args[1]);
                    } catch (Exception ex) {
                        MessageUtility.message(player, false, ChatColor.RED + "Region Types: " + Region.RegionType.toPrettyList());
                        return false;
                    }
                    if(type != null) {
                        this.type = type;
                        MessageUtility.message(player, false, ChatColor.RED + "Set region flag to " + this.type.toString());
                        return true;
                    }
                }
                break;
            }
            case "flag": {
                if(args.length <= 1) {
                    MessageUtility.message(player, false, ChatColor.RED + "Flag Types: " + FlagType.toPrettyList() + ", edit");
                    return false;
                }
                if(args[1].equalsIgnoreCase("edit")) {
                    if(args.length <= 2) {
                        MessageUtility.message(player, false, ChatColor.RED + "Flag Types: " + FlagType.toPrettyList());
                        return false;
                    }
                    FlagType type = null;
                    try {
                        type = FlagType.valueOf(args[2]);
                    } catch (Exception ex) {
                        MessageUtility.message(player, false, ChatColor.RED + "Flag Types: " + FlagType.toPrettyList());
                        return false;
                    }

                    if(!regionFlags.containsKey(type)) {
                        MessageUtility.message(player, false, ChatColor.RED + "This region doesn't contain this flag. Try adding it with flag.");
                        return false;
                    }
                    RegionFlag flag = regionFlags.get(type);

                    if(flag != null) {
                        flag.handleChange(player, StringUtility.trimList(args, 3));
                    }

                }
                else {
                    FlagType type = null;
                    try {
                        type = FlagType.valueOf(args[1]);
                    } catch (Exception ex) {
                        MessageUtility.message(player, false, ChatColor.RED + "Flag Types: " + FlagType.toPrettyList());
                        return false;
                    }

                    if(type != null) {
                        if(regionFlags.containsKey(type)) {
                            MessageUtility.message(player, false, ChatColor.RED + "This flag already exists. Try editing it.");
                            return false;
                        }

                        try {
                            regionFlags.put(type, type.toClass(this));
                            MessageUtility.message(player, false, ChatColor.RED + "Added flag " + type.toString() + " with defaults " + regionFlags.get(type).prettyItemDisplay());
                        } catch (Exception ex) {
                            MessageUtility.message(player, false, ChatColor.RED + "Unable to apply flag.");
                            return false;
                        }
                        return true;
                    }
                }

                break;
            }
            case "view": {
                MessageUtility.message(player, false, prettyItemDisplay());

                return true;
            }
            case "delete": {
                handler.unregisterRegion(this);
                MessageUtility.message(player, false, ChatColor.RED + "You have deleted region " + index + ".");

                return true;
            }
        }

        return false;
    }

    @Override
    public JSONObject serialize() {
        JSONObject rgOj = new JSONObject();

        rgOj.put("index", index);
        rgOj.put("cuboid", cuboid.serialize());
        if(type != null) rgOj.put("type", type.toString());
        if(!regionFlags.isEmpty()) {
            JSONObject flagOj = new JSONObject();

            for(RegionFlag flag : regionFlags.values()) {
                flagOj.put(flag.getType().toString(), flag.serialize());
            }

            rgOj.put("flags", flagOj);
        }

        return rgOj;
    }

    @Override
    public void deserialize(JSONObject rgOj) {
        this.index = rgOj.getString("index");
        cuboid = new Cuboid(rgOj.getString("cuboid"));
        if(rgOj.has("type")) {
            try {
                this.type = RegionType.valueOf(rgOj.getString("type"));
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
        if(rgOj.has("flags")) {
            JSONObject flagOj = rgOj.getJSONObject("flags");

            for(String key : flagOj.keySet()) {
                try {
                    FlagType type = FlagType.valueOf(key);

                    if(type != null) {
                        RegionFlag flag = type.toClass(this);

                        flag.deserialize(this, flagOj.getString(key));

                        regionFlags.put(type, flag);
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        }
    }

    public enum RegionType {
        SPAWN;

        public static String toPrettyList(Set<Region> regions) {
            StringBuilder builder = new StringBuilder();

            for(Region region : regions) {
                if(builder.length() == 0) {
                    builder.append(region.getIndex());
                }
                else {
                    builder.append(", " + region.getIndex());
                }
            }

            return builder.toString();
        }

        public static String toPrettyList() {
            StringBuilder builder = new StringBuilder();

            for(RegionType type : values()) {
                if(builder.length() == 0) {
                    builder.append(type.toString());
                }
                else {
                    builder.append(", " + type.toString());
                }
            }

            return builder.toString();
        }
    }

}

package net.elitemc.eliteteams.util;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import net.elitemc.commons.util.PlayerUtility;
import net.elitemc.commons.util.reflect.ReflectionHandler;
import org.bukkit.Location;
import org.bukkit.entity.Player;

public enum TrailParticles {
    CRIT("crit", "Crit"),
    FLAME("flame", "Flame"),
    LAVA("lava", "Lava"),
    CLOUD("cloud", "Clouds"),
    SLIME("slime", "Slime"),
    ANGRY_VILLAGER("angryVillager", "Angry Villager"),
    HAPPY_VILLAGER("happyVillager", "Happy Villager"),
    DRIP_WATER("dripWater", "Drip Water"),
    DRIP_LAVA("dripLava", "Drip Lava"),
    FIREWORKS_SPARK("fireworksSpark", "Fireworks"),
    SMOKE("smoke", "Smoke"),
    MOB_SPELL("mobSpell", "Mob Spell"),
    MOB_SPELL_AMBIENT("mobSpellAmbient", "Mob Spell Ambient"),
    SPELL("spell", "Spell"),
    INSTANT_SPELL("instantSpell", "Instant Spell"),
    WITCH_MAGIC("witchMagic", "Witch Magic"),
    NOTE("note", "Note"),
    PORTAL("portal", "Portal"),
    ENCHANTMENT_TABLE("enchantmenttable", "Enchantment Table"),
    FOOTSTEP("footstep", "Footstep"),
    WAKE("wake", "Wake"),
    RED_DUST("reddust", "Red Dust"),
    SNOWBALL_POOF("snowballpoof", "Snowball Poof"),
    SNOW_SHOVEL("snowshovel", "Snow Shovel"),
    HEART("heart", "Heart");

    private static final Map<String, TrailParticles> NAME_MAP;
    private static final double MAX_RANGE = 16.0D;
    private static Constructor<?> packetPlayOutWorldParticles;
    private static Method getHandle;
    private static Field playerConnection;
    private static Method sendPacket;
    private final String name, displayName;

    private TrailParticles(String name, String displayName) {
        this.name = name;
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getName() {
        return this.name;
    }

    public static net.elitemc.commons.util.projectile.ProjectileEffect fromName(String name) {
        if(name != null) {
            Iterator var1 = NAME_MAP.entrySet().iterator();

            while(var1.hasNext()) {
                Entry e = (Entry)var1.next();
                if(((String)e.getKey()).equalsIgnoreCase(name)) {
                    return (net.elitemc.commons.util.projectile.ProjectileEffect)e.getValue();
                }
            }
        }

        return null;
    }

    private static List<Player> getPlayers(Location center, double range) {
        ArrayList players = new ArrayList();
        String name = center.getWorld().getName();
        double squared = range * range;
        Player[] var7 = PlayerUtility.getOnlinePlayers();
        int var8 = var7.length;

        for(int var9 = 0; var9 < var8; ++var9) {
            Player p = var7[var9];
            if(p.getWorld().getName().equals(name) && p.getLocation().distanceSquared(center) <= squared) {
                players.add(p);
            }
        }

        return players;
    }

    private static Object instantiatePacket(String name, Location center, float offsetX, float offsetY, float offsetZ, float speed, int amount) {
        if(amount < 1) {
            throw new TrailParticles.PacketInstantiationException("Amount cannot be lower than 1");
        } else {
            try {
                return packetPlayOutWorldParticles.newInstance(new Object[]{name, Float.valueOf((float)center.getX()), Float.valueOf((float)center.getY()), Float.valueOf((float)center.getZ()), Float.valueOf(offsetX), Float.valueOf(offsetY), Float.valueOf(offsetZ), Float.valueOf(speed), Integer.valueOf(amount)});
            } catch (Exception var8) {
                throw new TrailParticles.PacketInstantiationException("Packet instantiation failed", var8);
            }
        }
    }

    private static Object instantiateIconCrackPacket(int id, Location center, float offsetX, float offsetY, float offsetZ, float speed, int amount) {
        return instantiatePacket("iconcrack_" + id, center, offsetX, offsetY, offsetZ, speed, amount);
    }

    private static Object instantiateBlockCrackPacket(int id, byte data, Location center, float offsetX, float offsetY, float offsetZ, int amount) {
        return instantiatePacket("blockcrack_" + id + "_" + data, center, offsetX, offsetY, offsetZ, 0.0F, amount);
    }

    private static Object instantiateBlockDustPacket(int id, byte data, Location center, float offsetX, float offsetY, float offsetZ, float speed, int amount) {
        return instantiatePacket("blockdust_" + id + "_" + data, center, offsetX, offsetY, offsetZ, speed, amount);
    }

    private static void sendPacket(Player p, Object packet) {
        try {
            sendPacket.invoke(playerConnection.get(getHandle.invoke(p, new Object[0])), new Object[]{packet});
        } catch (Exception var3) {
            throw new TrailParticles.PacketSendingException("Failed to send a packet to player \'" + p.getName() + "\'", var3);
        }
    }

    private static void sendPacket(Collection<Player> players, Object packet) {
        Iterator var2 = players.iterator();

        while(var2.hasNext()) {
            Player p = (Player)var2.next();
            sendPacket((Player)p, packet);
        }

    }

    public void display(Location center, float offsetX, float offsetY, float offsetZ, float speed, int amount, Player... players) {
        sendPacket((Collection)Arrays.asList(players), instantiatePacket(this.name, center, offsetX, offsetY, offsetZ, speed, amount));
    }

    public void display(Location center, double range, float offsetX, float offsetY, float offsetZ, float speed, int amount) {
        if(range > 16.0D) {
            throw new IllegalArgumentException("Range cannot exceed the maximum value of 16");
        } else {
            sendPacket((Collection)getPlayers(center, range), instantiatePacket(this.name, center, offsetX, offsetY, offsetZ, speed, amount));
        }
    }

    public void display(Location center, float offsetX, float offsetY, float offsetZ, float speed, int amount) {
        this.display(center, 16.0D, offsetX, offsetY, offsetZ, speed, amount);
    }

    public static void displayIconCrack(Location center, int id, float offsetX, float offsetY, float offsetZ, float speed, int amount, Player... players) {
        sendPacket((Collection)Arrays.asList(players), instantiateIconCrackPacket(id, center, offsetX, offsetY, offsetZ, speed, amount));
    }

    public static void displayIconCrack(Location center, double range, int id, float offsetX, float offsetY, float offsetZ, float speed, int amount) {
        if(range > 16.0D) {
            throw new IllegalArgumentException("Range has to be lower/equal the maximum of 16");
        } else {
            sendPacket((Collection)getPlayers(center, range), instantiateIconCrackPacket(id, center, offsetX, offsetY, offsetZ, speed, amount));
        }
    }

    public static void displayIconCrack(Location center, int id, float offsetX, float offsetY, float offsetZ, float speed, int amount) {
        displayIconCrack(center, 16.0D, id, offsetX, offsetY, offsetZ, speed, amount);
    }

    public static void displayBlockCrack(Location center, int id, byte data, float offsetX, float offsetY, float offsetZ, int amount, Player... players) {
        sendPacket((Collection)Arrays.asList(players), instantiateBlockCrackPacket(id, data, center, offsetX, offsetY, offsetZ, amount));
    }

    public static void displayBlockCrack(Location center, double range, int id, byte data, float offsetX, float offsetY, float offsetZ, int amount) {
        if(range > 16.0D) {
            throw new IllegalArgumentException("Range has to be lower/equal the maximum of 16");
        } else {
            sendPacket((Collection)getPlayers(center, range), instantiateBlockCrackPacket(id, data, center, offsetX, offsetY, offsetZ, amount));
        }
    }

    public static void displayBlockCrack(Location center, int id, byte data, float offsetX, float offsetY, float offsetZ, int amount) {
        displayBlockCrack(center, 16.0D, id, data, offsetX, offsetY, offsetZ, amount);
    }

    public static void displayBlockDust(Location center, int id, byte data, float offsetX, float offsetY, float offsetZ, float speed, int amount, Player... players) {
        sendPacket((Collection)Arrays.asList(players), instantiateBlockDustPacket(id, data, center, offsetX, offsetY, offsetZ, speed, amount));
    }

    public static void displayBlockDust(Location center, double range, int id, byte data, float offsetX, float offsetY, float offsetZ, float speed, int amount) {
        if(range > 16.0D) {
            throw new IllegalArgumentException("Range has to be lower/equal the maximum of 16");
        } else {
            sendPacket((Collection)getPlayers(center, range), instantiateBlockDustPacket(id, data, center, offsetX, offsetY, offsetZ, speed, amount));
        }
    }

    public static void displayBlockDust(Location center, int id, byte data, float offsetX, float offsetY, float offsetZ, float speed, int amount) {
        displayBlockDust(center, 16.0D, id, data, offsetX, offsetY, offsetZ, speed, amount);
    }

    static {
        NAME_MAP = new HashMap();
        TrailParticles[] e = values();
        int var1 = e.length;

        for(int var2 = 0; var2 < var1; ++var2) {
            TrailParticles p = e[var2];
            NAME_MAP.put(p.name, p);
        }

        try {
            packetPlayOutWorldParticles = ReflectionHandler.getConstructor(ReflectionHandler.PacketType.PLAY_OUT_WORLD_PARTICLES.getPacket(), new Class[]{String.class, Float.TYPE, Float.TYPE, Float.TYPE, Float.TYPE, Float.TYPE, Float.TYPE, Float.TYPE, Integer.TYPE});
            getHandle = ReflectionHandler.getMethod("CraftPlayer", ReflectionHandler.SubPackageType.ENTITY, "getHandle", new Class[0]);
            playerConnection = ReflectionHandler.getField("EntityPlayer", ReflectionHandler.PackageType.MINECRAFT_SERVER, "playerConnection");
            sendPacket = ReflectionHandler.getMethod(playerConnection.getType(), "sendPacket", new Class[]{ReflectionHandler.getClass("Packet", ReflectionHandler.PackageType.MINECRAFT_SERVER)});
        } catch (Exception var4) {
            var4.printStackTrace();
        }

    }

    private static final class PacketSendingException extends RuntimeException {
        private static final long serialVersionUID = 3203085387160737484L;

        public PacketSendingException(String message, Throwable cause) {
            super(message, cause);
        }
    }

    private static final class PacketInstantiationException extends RuntimeException {
        private static final long serialVersionUID = 3203085387160737484L;

        public PacketInstantiationException(String message) {
            super(message);
        }

        public PacketInstantiationException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}

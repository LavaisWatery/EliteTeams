package net.elitemc.eliteteams.util;

import net.elitemc.commons.util.Items;
import net.elitemc.commons.util.MessageUtility;
import net.elitemc.commons.util.interf.JsonSerializable;
import net.elitemc.commons.util.json.JSONObject;
import net.elitemc.commons.util.mongo.pooling.PoolAction;
import net.elitemc.origin.command.Command_ptime;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Kyle Gosleigh on 6/8/2017.
 */
public class PlayerOptions implements JsonSerializable {

    public PlayerOptions(TeamsPlayerWrapper wrapper) {
        this.wrapper = wrapper;
    }

    public PlayerOptions(TeamsPlayerWrapper wrapper, String input) {
        this.wrapper = wrapper;
        deserialize(new JSONObject(input));
    }

    private TeamsPlayerWrapper wrapper;

    private HashMap<OptionType, Object> optionToggles = new HashMap<>();

    public HashMap<OptionType, Object> getOptionToggles() {
        return optionToggles;
    }

    public void setOptionToggle(OptionType type, Object toggle) {
        optionToggles.put(type, toggle);
        wrapper.queueAction(PoolAction.SAVE);
    }

    public TeamsPlayerWrapper getWrapper() {
        return wrapper;
    }

    @Override
    public JSONObject serialize() {
        JSONObject optionsOj = new JSONObject();

        for(Map.Entry<OptionType, Object> entry: optionToggles.entrySet()) {
            optionsOj.put(entry.getKey().toString(), entry.getValue());
        }

        return optionsOj;
    }

    @Override
    public void deserialize(JSONObject jsonObject) {
        for(Map.Entry<String, Object> entry : jsonObject.toMap().entrySet()) {
            try {
                OptionType type = OptionType.valueOf(entry.getKey());

                optionToggles.put(type, entry.getValue());
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }

    public enum OptionType {
        TOGGLE_PMS("Toggle PMs", false, 10) {
            @Override
            public ItemStack createItem(PlayerOptions options) {
                return Items.builder().setMaterial(Material.PAPER).setName(ChatColor.AQUA + "Private Messages " + ChatColor.GRAY + "(" + ChatColor.DARK_AQUA + (options.getWrapper().getOriginWrapper().isBusy() ? "OFF" : "ON") + ChatColor.GRAY + ")").build();
            }

            @Override
            public void toggleOption(Player player, PlayerOptions options) {
                boolean toggle = options.getWrapper().getOriginWrapper().isBusy();

                options.getWrapper().getOriginWrapper().setBusy(!toggle);
                MessageUtility.message(player, false, "You have toggled your pms " + (options.getWrapper().getOriginWrapper().isBusy() ? "OFF" : "ON") + ".");
            }

            @Override
            public void initOption(PlayerOptions options) {

            }
        },

        SCOREBOARD("Toggle Scoreboard", true, 22) {
            @Override
            public ItemStack createItem(PlayerOptions options) {
                return Items.builder().setMaterial(Material.REDSTONE_COMPARATOR).setName(ChatColor.AQUA + "Scoreboard " + ChatColor.GRAY + "(" + ChatColor.DARK_AQUA + (((boolean) this.getCurrent(options)) ? "ON" : "OFF") + ChatColor.GRAY + ")").build();
            }

            @Override
            public void toggleOption(Player player, PlayerOptions options) {
                boolean toggle = ((boolean) getCurrent(options));

                options.setOptionToggle(this, !toggle);
                MessageUtility.message(player, false, "You have toggled your scoreboard " + (!toggle ? "ON" : "OFF") + ".");
            }

            @Override
            public void initOption(PlayerOptions options) {
                if(!options.getOptionToggles().containsKey(this)) options.getOptionToggles().put(this, getDef());
            }
        },

        DEATH_MESSAGES("Death Messages", true, 4) {
            @Override
            public ItemStack createItem(PlayerOptions options) {
                return Items.builder().setMaterial(Material.DEAD_BUSH).setName(ChatColor.AQUA + "Death Messages " + ChatColor.GRAY + "(" + ChatColor.DARK_AQUA + (((boolean) this.getCurrent(options)) ? "ENABLED" : "DISABLED") + ChatColor.GRAY + ")").build();
            }

            @Override
            public void toggleOption(Player player, PlayerOptions options) {
                boolean toggle = ((boolean) getCurrent(options));

                options.setOptionToggle(this, !toggle);
                MessageUtility.message(player, false, "You have toggled your death messages " + (!toggle ? "ON" : "OFF") + ".");
            }

            @Override
            public void initOption(PlayerOptions options) {
                if(!options.getOptionToggles().containsKey(this)) options.getOptionToggles().put(this, getDef());
            }
        }, //28 warp gui, 34 team gui,

        ALWAYS_SUNNY("Always Sunny", false, 16) {
            @Override
            public ItemStack createItem(PlayerOptions options) {
                return Items.builder().setMaterial(Material.getMaterial(175)).setName(ChatColor.AQUA + "Always Sunny " + ChatColor.GRAY + "(" + ChatColor.DARK_AQUA + (((boolean) this.getCurrent(options)) ? "ENABLED" : "DISABLED") + ChatColor.GRAY + ")").build();
            }

            @Override
            public void toggleOption(Player player, PlayerOptions options) {
                boolean toggle = !((boolean) getCurrent(options));

                if(toggle) options.getWrapper().getOriginWrapper().setPlayerTime(6000L);
                else options.getWrapper().getOriginWrapper().setPlayerTime(-1);

                options.setOptionToggle(this, toggle);
                MessageUtility.message(player, false, "You have toggled always sunny to " + (toggle ? "ENABLED" : "DISABLED") + ".");
            }

            @Override
            public void initOption(PlayerOptions options) {
                if(!options.getOptionToggles().containsKey(this)) options.getOptionToggles().put(this, getDef());
            }
        },

        KIT("Kit List", false, 40) {
            @Override
            public ItemStack createItem(PlayerOptions options) {
                return Items.builder().setMaterial(Material.DIAMOND_SWORD).setName(ChatColor.AQUA + "Kit List " + ChatColor.GRAY + "(" + ChatColor.DARK_AQUA + (((boolean) this.getCurrent(options)) ? "TEXT" : "GUI") + ChatColor.GRAY + ")").build();
            }

            @Override
            public void toggleOption(Player player, PlayerOptions options) {
                boolean toggle = !((boolean) getCurrent(options));

                options.setOptionToggle(this, toggle);
                MessageUtility.message(player, false, "You have toggled your kit list style to " + (toggle ? "TEXT" : "GUI") + ".");
            }

            @Override
            public void initOption(PlayerOptions options) {
                if(!options.getOptionToggles().containsKey(this)) options.getOptionToggles().put(this, getDef());
            }
        },

        WARPS("Warp GUI", true, 28) {
            @Override
            public ItemStack createItem(PlayerOptions options) {
                return Items.builder().setMaterial(Material.STONE_PLATE).setName(ChatColor.AQUA + "Warp GUI " + ChatColor.GRAY + "(" + ChatColor.DARK_AQUA + (((boolean) this.getCurrent(options)) ? "ENABLED" : "DISABLED") + ChatColor.GRAY + ")").build();
            }

            @Override
            public void toggleOption(Player player, PlayerOptions options) {
                boolean toggle = ((boolean) getCurrent(options));

//                options.setOptionToggle(this, !toggle);
                MessageUtility.message(player, false, "You have toggled your Warp GUI " + (!toggle ? "ON" : "OFF") + ".");
            }

            @Override
            public void initOption(PlayerOptions options) {
                if(!options.getOptionToggles().containsKey(this)) options.getOptionToggles().put(this, getDef());
            }
        },

        TEAMS("Team GUI", true, 34) {
            @Override
            public ItemStack createItem(PlayerOptions options) {
                return Items.builder().setMaterial(Material.IRON_CHESTPLATE).setName(ChatColor.AQUA + "Team GUI " + ChatColor.GRAY + "(" + ChatColor.DARK_AQUA + (((boolean) this.getCurrent(options)) ? "ENABLED" : "DISABLED") + ChatColor.GRAY + ")").build();
            }

            @Override
            public void toggleOption(Player player, PlayerOptions options) {
                boolean toggle = ((boolean) getCurrent(options));

//                options.setOptionToggle(this, !toggle);
                MessageUtility.message(player, false, "You have toggled your Team GUI " + (!toggle ? "ON" : "OFF") + ".");
            }

            @Override
            public void initOption(PlayerOptions options) {
                if(!options.getOptionToggles().containsKey(this)) options.getOptionToggles().put(this, getDef());
            }
        };

        private OptionType(String typeDisplay, Object def, int slot) {
            this.typeDisplay = typeDisplay;
            this.def = def;
            this.slot = slot;
        }

        private String typeDisplay;
        private Object def;
        private int slot;

        public abstract ItemStack createItem(PlayerOptions options);

        public abstract void toggleOption(Player player, PlayerOptions options);

        public abstract void initOption(PlayerOptions options);

        public int getSlot() {
            return slot;
        }

        public Object getCurrent(PlayerOptions options) {
            if(!options.getOptionToggles().containsKey(this)) {
                options.getOptionToggles().put(this, def);
            }

            return options.getOptionToggles().get(this);
        }

        public Object getDef() {
            return def;
        }

        public String getTypeDisplay() {
            return typeDisplay;
        }

    }

}

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
        TOGGLE_PMS("Toggle PMs", false) {
            @Override
            public ItemStack createItem(PlayerOptions options) {
                return Items.builder().setMaterial(Material.getMaterial(2256)).setName(ChatColor.AQUA + "Private Messages " + ChatColor.GRAY + "(" + ChatColor.DARK_AQUA + (options.getWrapper().getOriginWrapper().isBusy() ? "OFF" : "ON") + ChatColor.GRAY + ")").build();
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

        SCOREBOARD("Toggle Scoreboard", true) {
            @Override
            public ItemStack createItem(PlayerOptions options) {
                return Items.builder().setMaterial(Material.PAPER).setName(ChatColor.AQUA + "Scoreboard " + ChatColor.GRAY + "(" + ChatColor.DARK_AQUA + (((boolean) this.getCurrent(options)) ? "ON" : "OFF") + ChatColor.GRAY + ")").build();
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

        KIT("Kit List", false) {
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
        };

        private OptionType(String typeDisplay, Object def) {
            this.typeDisplay = typeDisplay;
            this.def = def;
        }

        private String typeDisplay;
        private Object def;

        public abstract ItemStack createItem(PlayerOptions options);

        public abstract void toggleOption(Player player, PlayerOptions options);

        public abstract void initOption(PlayerOptions options);

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

package net.elitemc.eliteteams.handler;

import net.elitemc.commons.util.Handler;
import net.elitemc.commons.util.event.PlayerInventoryEvent;
import net.elitemc.eliteteams.util.PlayerOptions;
import net.elitemc.eliteteams.util.TeamsPlayerWrapper;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.inventory.Inventory;

import java.util.HashMap;

/**
 * Created by LavaisWatery on 2017-07-22.
 */
public class OptionsHandler extends Handler {
    private static OptionsHandler instance;

    public OptionsHandler() {
        instance = this;
    }

    public static String OPTIONS_INVENTORY_TITLE = ChatColor.GOLD + "Options";

    public static HashMap<Material, PlayerOptions.OptionType> optionMatchedMaterial = new HashMap<Material, PlayerOptions.OptionType>() {{
        put(Material.DIAMOND_SWORD, PlayerOptions.OptionType.KIT);
        put(Material.PAPER, PlayerOptions.OptionType.SCOREBOARD);
        put(Material.getMaterial(2256), PlayerOptions.OptionType.TOGGLE_PMS);
    }};

    @Override
    public void init() {

    }

    @Override
    public void unload() {

    }

    public void openPlayerOptions(Player player, PlayerOptions options) {
        Inventory inventory = Bukkit.createInventory(player, 9 * 3, OPTIONS_INVENTORY_TITLE);

        int startIndex = 11;

        for(PlayerOptions.OptionType type : PlayerOptions.OptionType.values()) {
            inventory.setItem(startIndex, type.createItem(options));
            startIndex++;
        }

        player.openInventory(inventory);
    }

    @EventHandler
    public void onInventoryClick(PlayerInventoryEvent event) {
        if(event.getInventory().getTitle().equalsIgnoreCase(OPTIONS_INVENTORY_TITLE)) {
            event.setCancelled(true);
            if(event.getEvent().getAction() == InventoryAction.PICKUP_ALL) {
                Inventory inventory = event.getInventory();

                if(event.getEvent().getCurrentItem() != null && optionMatchedMaterial.containsKey(event.getEvent().getCurrentItem().getType())) {
                    PlayerOptions.OptionType type = optionMatchedMaterial.get(event.getEvent().getCurrentItem().getType());

                    if(type != null) {
                        TeamsPlayerWrapper wrapper = TeamsPlayerHandler.getInstance().getPlayerWrapper(event.getPlayer());

                        type.toggleOption(event.getPlayer(), wrapper.getPlayerOptions());
                        inventory.setItem(event.getEvent().getSlot(), type.createItem(wrapper.getPlayerOptions()));
                    }
                }
            }
        }
    }

    public static OptionsHandler getInstance() {
        return instance;
    }

}

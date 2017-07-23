package net.elitemc.eliteteams.handler;

import net.elitemc.commons.util.Handler;
import net.elitemc.commons.util.Items;
import net.elitemc.commons.util.event.PlayerInventoryEvent;
import net.elitemc.eliteteams.util.AchievementType;
import net.elitemc.eliteteams.util.GameAchievement;
import net.elitemc.eliteteams.util.TeamsPlayerWrapper;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.*;

/**
 * Created by LavaisWatery on 2017-07-22.
 */
public class AchievementHandler extends Handler {
    private static AchievementHandler instance;

    public AchievementHandler() {
        instance = this;
    }

    public static String ACHIEVEMENT_INVENTORY_TITLE = ChatColor.GOLD + "Achievements";

    private List<GameAchievement> generalAchievements = new ArrayList<>();
    private HashMap<AchievementType, List<GameAchievement>> generalAchievementsOrdered = new HashMap<>();

    @Override
    public void init() {
        for(GameAchievement achievement : GameAchievement.values()) {
            if(achievement.isGeneral()){
                if(!generalAchievementsOrdered.containsKey(achievement.getType())) generalAchievementsOrdered.put(achievement.getType(), new ArrayList<>());
                generalAchievementsOrdered.get(achievement.getType()).add(achievement);
                generalAchievements.add(achievement);
            }
        }
    }

    public void updateAchievementShower(Player player, TeamsPlayerWrapper wrapper) {
        boolean shouldOpen = false;
        Inventory inventory = null;

        if(player.getOpenInventory() != null && player.getOpenInventory().getTopInventory() != null && player.getOpenInventory().getTopInventory().getTitle().equalsIgnoreCase(ACHIEVEMENT_INVENTORY_TITLE)) {
            inventory = player.getOpenInventory().getTopInventory();
        }
        else {
            inventory = Bukkit.createInventory(player, 9 * 6, ACHIEVEMENT_INVENTORY_TITLE);
            shouldOpen = true;
        }

        inventory.clear();
        ItemStack spacer = Items.builder().setMaterial(Material.THIN_GLASS).setName("").build();

        int j = 1;
        for(int i = 0; i <= 5; i++) {
            inventory.setItem(j, spacer);
            j = j + 9;
        }

        List<GameAchievement> generalAchievements = getGeneralAchievements();
        List<GameAchievement> has = new ArrayList<>(), not = new ArrayList<>();

        for(GameAchievement achievement : generalAchievements) {
            if(wrapper.getCompletedAchievements().contains(achievement))
                has.add(achievement);
            else
                not.add(achievement);
        }

        int index = 2;
        for(GameAchievement achievement : has) {
            inventory.setItem(index, Items.builder().setMaterial(Material.EMERALD_BLOCK).setName(ChatColor.DARK_GREEN + achievement.getDisplay()).addLore(ChatColor.GREEN + "Completed").build());
            index++;
            if(index % 9 == 0) index = index + 1;
        }
        for(GameAchievement achievement : not) {
            inventory.setItem(index, Items.builder().setMaterial(Material.REDSTONE_BLOCK).setName(ChatColor.RED + achievement.getDisplay()).addLore(ChatColor.DARK_RED + "Locked").build());
            index++;
            if(index % 9 == 0) index = index + 1;
        }

        if(shouldOpen) player.openInventory(inventory);
    }

    @Override
    public void unload() {

    }

    public List<GameAchievement> getGeneralForType(AchievementType type) {
        return generalAchievementsOrdered.get(type);
    }

    public List<GameAchievement> getGeneralAchievements() {
        return generalAchievements;
    }

    public HashMap<AchievementType, List<GameAchievement>> getGeneralAchievementsOrdered() {
        return generalAchievementsOrdered;
    }

    @EventHandler
    public void onInventoryClick(PlayerInventoryEvent event) {
        if(event.getInventory().getTitle().equalsIgnoreCase(ACHIEVEMENT_INVENTORY_TITLE)) {
            event.setCancelled(true);
        }
    }

    public static AchievementHandler getInstance() {
        return instance;
    }

}

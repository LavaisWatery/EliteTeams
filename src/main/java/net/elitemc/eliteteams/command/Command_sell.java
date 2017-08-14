package net.elitemc.eliteteams.command;

import net.elitemc.commons.util.InventoryUtility;
import net.elitemc.commons.util.LocationUtility;
import net.elitemc.commons.util.MessageUtility;
import net.elitemc.commons.util.NumberUtility;
import net.elitemc.commons.util.cmdfrmwrk.BaseCommand;
import net.elitemc.commons.util.cmdfrmwrk.CommandUsageBy;
import net.elitemc.eliteteams.handler.EconomyHandler;
import net.elitemc.eliteteams.handler.TeamsPlayerHandler;
import net.elitemc.eliteteams.util.TeamsPlayerWrapper;
import net.elitemc.eliteteams.util.shop.ShopItem;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.AbstractMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by LavaisWatery on 2017-08-09.
 */
public class Command_sell extends BaseCommand {

    public Command_sell() {
        super("sell", "teams.sell", CommandUsageBy.PLAYER);
        setUsage("/<command> [amount:hand:all]");
        setArgRange(0, 1);
    }

    private EconomyHandler handler = EconomyHandler.getInstance();

    /**
     * remove items from players inventory PROPERLY
     *
     * @param p        - Player to remove Material from
     * @param amount   - Amount to remove from Players Inventory
     */
    public static void removeItem(final Player p, ItemStack clear, final int amount) {
        for (int i = 1; i <= amount; ++i) {
            final ItemStack ite = clear;
            final ItemStack it = new ItemStack(ite.getType(), 1, ite.getDurability());
            it.setItemMeta(ite.getItemMeta());
            p.getInventory().removeItem(new ItemStack[]{it});
        }
    }

    /**
     * Get amount in players inventory of Material
     *
     * @param inv      - Inventory to check
     * @return
     */
    public static int getAmountInInventory(Inventory inv, ItemStack check) {
        int amount = 0;
        ItemStack[] contents = inv.getContents();

        for (ItemStack item : contents) {
            if (item != null && item.getType() == check.getType() && item.getDurability() == check.getDurability()) {
                amount = amount + item.getAmount();
            }
        }

        return amount;
    }

    /**
     * Remove Check if player has X amount in inventory of Material
     *
     * @param i        - Inventory to check if it contains Material
     * @param amount   - Amount to check in Inventory
     * @return
     */
    public static boolean containsAmount(Inventory i, ItemStack check, int amount) {
        ItemStack[] contents = i.getContents();
        int count = 0;
        for (ItemStack item : contents) {
            if (item != null && item.getType() == check.getType() && item.getDurability() == check.getDurability()) {
                count = count + item.getAmount();
            }
        }
        return count >= amount;
    }

    private void sellItemInHand(Player player, TeamsPlayerWrapper wrapper, String[] args) {
        if(player.getItemInHand() == null) {
            MessageUtility.message(player, false, ChatColor.RED + "You must have an item in your hand.");
            return;
        }
        ItemStack item = player.getItemInHand();
        ShopItem shopItem = handler.fromItemStack(item);

        if(shopItem == null || shopItem.getSellMult() == -1) {
            MessageUtility.message(player, false, ChatColor.RED + "This item is not for sale!");
            return;
        }
        if(args.length > 0 && !args[0].equalsIgnoreCase("hand") && !NumberUtility.isNumber(args[0])) {
            MessageUtility.message(player, false, ChatColor.RED + "You must input a number or /sell hand.");
            return;
        }
        int amount = args.length == 0 ? item.getAmount() : args[0].equalsIgnoreCase("hand") ? item.getAmount() : Integer.parseInt(args[0]);
        double perSellPrice = shopItem.getPrice() * shopItem.getSellMult();
        double sellPrice = amount * perSellPrice;

        String display = InventoryUtility.getFriendlyItemStackName(item);

        if(containsAmount(player.getInventory(), item, amount)) {
            removeItem(player, item, amount);
            wrapper.setBalance(wrapper.getBalance() + sellPrice);
            MessageUtility.message(player, false, ChatColor.GRAY + "You have sold " + amount + " " + ChatColor.GREEN + display + ChatColor.GRAY + " for " + ChatColor.GREEN + "$" + NumberUtility.getProperFormat().format(sellPrice) + ChatColor.GRAY + "!");
        }
        else {
            MessageUtility.message(player, false, ChatColor.RED + "You don't have this many items.");
        }
    }

    public void execute(CommandSender sender, String[] args) {
        Player player = (Player) sender;
        TeamsPlayerWrapper wrapper = TeamsPlayerHandler.getInstance().getPlayerWrapper(player);

        if(args.length == 0 || args[0].equalsIgnoreCase("hand") || NumberUtility.isNumber(args[0])) {
            sellItemInHand(player, wrapper, args);
        }
        else {
            if(args[0].equalsIgnoreCase("all")) {
                if(player.getItemInHand() == null) {
                    MessageUtility.message(player, false, ChatColor.RED + "You must have an item in your hand.");
                    return;
                }
                ItemStack item = player.getItemInHand();
                ShopItem shopItem = handler.fromItemStack(item);

                if(shopItem == null || shopItem.getSellMult() == -1) {
                    MessageUtility.message(player, false, ChatColor.RED + "This item is not for sale!");
                    return;
                }
                int amount = getAmountInInventory(player.getInventory(), item);
                double perSellPrice = shopItem.getPrice() * shopItem.getSellMult();
                double sellPrice = amount * perSellPrice;

                String display = InventoryUtility.getFriendlyItemStackName(item);

                if(containsAmount(player.getInventory(), item, amount)) {
                    removeItem(player, item, amount);
                    wrapper.setBalance(wrapper.getBalance() + sellPrice);
                    MessageUtility.message(player, false, ChatColor.GRAY + "You have sold " + amount + " " + ChatColor.GREEN + display + ChatColor.GRAY + " for " + ChatColor.GREEN + "$" + NumberUtility.getProperFormat().format(sellPrice) + ChatColor.GRAY + "!");
                }
                else {
                    MessageUtility.message(player, false, ChatColor.RED + "You don't have this many items.");
                }
            }
            else {
                MessageUtility.message(player, false, ChatColor.RED + "This item is not for sale!");
            }
        }

//        if(args.length > 1 && !NumberUtility.isDouble(args[1])) {
//            MessageUtility.message(player, false, ChatColor.RED + "Amount must be a number!");
//            return;
//        }
//
//        if((item = handler.getShopItem(args[0])) != null) {
//            if(LocationUtility.getDistance(player.getLocation(), player.getWorld().getSpawnLocation()) < 500 && wrapper.getPlayerState() != TeamsPlayerWrapper.TeamsPlayerState.PROTECTED) {
//                MessageUtility.message(player, false, ChatColor.RED + "You must be 500+ blocks from spawn to use the Economy.");
//                return;
//            }
//
//            int amount = args.length > 1 ? Integer.parseInt(args[1]) : 1;
//
//            if(amount <= 0) {
//                MessageUtility.message(player, false, ChatColor.RED + "You may not buy this amount.");
//                return;
//            }
//
//            if(amount > 2304) {
//                MessageUtility.message(player, false, ChatColor.RED + "You may not buy this amount");
//                return;
//            }
//
//            List<ItemStack> stack = null;
//            double buyPrice = -1;
//
//            buyPrice = item.getPrice();
//            stack = item.toItem(amount);
//
//            if(stack == null) {
//                MessageUtility.message(player, false, ChatColor.RED + "Item is null. Report to an administrator.");
//                return;
//            }
//
//            if(buyPrice == -1) {
//                MessageUtility.message(player, false, ChatColor.RED + "This item is not for sale!");
//                return;
//            }
//
//            // Credit to GlobalShop
//
//            if (player.getInventory().firstEmpty() == -1) {
//                player.sendMessage(ChatColor.RED + "Your inventory is full!");
//                return;
//            }
//
//            int spacesNeeded = stack.size();
//            int freeSpace = InventoryUtility.checkSlotsAvailable(player);
//
//            if (freeSpace < spacesNeeded) {
//                player.sendMessage(ChatColor.RED + "You haven't got room in your inventory (" + freeSpace + " spaces, " + spacesNeeded + " needed)!");
//                return;
//            }
//
//            buyPrice = buyPrice * amount;
//
//            if(wrapper.getBalance() >= buyPrice) {
//                for(ItemStack is : stack) {
//                    player.getInventory().addItem(is);
//                    player.updateInventory();
//                }
//
//                wrapper.setBalance(wrapper.getBalance() - buyPrice);
//                MessageUtility.message(player, false, ChatColor.GRAY + "You have purchased " + ChatColor.GREEN + InventoryUtility.getFriendlyItemStackName(stack.get(0)) + ChatColor.GRAY + " for " + ChatColor.GREEN + "$" + NumberUtility.getProperFormat().format(buyPrice) + ChatColor.GRAY + "!");
//            }
//            else {
//                MessageUtility.message(player, false, ChatColor.RED + "You don't have enough money for this item. You need " + NumberUtility.getProperFormat().format(buyPrice) + " to purchase this.");
//            }
//
//        }
//        else {
//            MessageUtility.message(player, false, ChatColor.RED + "This item is not for sale!");
//        }
    }

}

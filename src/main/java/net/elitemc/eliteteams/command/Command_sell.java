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
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

/**
 * Created by LavaisWatery on 2017-08-09.
 *
 * TODO
 */
public class Command_sell extends BaseCommand {

    public Command_sell() {
        super("sell", "teams.sell", CommandUsageBy.PLAYER);
        setUsage("/<command> [amount]");
        setArgRange(2, 3);
    }

    private EconomyHandler handler = EconomyHandler.getInstance();

    public void execute(CommandSender sender, String[] args) {
        Player player = (Player) sender;
        TeamsPlayerWrapper wrapper = TeamsPlayerHandler.getInstance().getPlayerWrapper(player);
        ShopItem item = null;

        if(args.length > 1 && !NumberUtility.isDouble(args[1])) {
            MessageUtility.message(player, false, ChatColor.RED + "Amount must be a number!");
            return;
        }

        if((item = handler.getShopItem(args[0])) != null) {
            if(LocationUtility.getDistance(player.getLocation(), player.getWorld().getSpawnLocation()) < 500 && wrapper.getPlayerState() != TeamsPlayerWrapper.TeamsPlayerState.PROTECTED) {
                MessageUtility.message(player, false, ChatColor.RED + "You must be 500+ blocks from spawn to use the Economy.");
                return;
            }

            int amount = args.length > 1 ? Integer.parseInt(args[1]) : 1;

            if(amount <= 0) {
                MessageUtility.message(player, false, ChatColor.RED + "You may not buy this amount.");
                return;
            }

            ItemStack stack = null;
            double buyPrice = -1;

            buyPrice = item.getPrice();
            stack = item.toItem(amount);

            if(stack == null) {
                MessageUtility.message(player, false, ChatColor.RED + "Item is null. Report to an administrator.");
                return;
            }

            if(buyPrice == -1) {
                MessageUtility.message(player, false, ChatColor.RED + "This item is not for sale!");
                return;
            }

            // Credit to GlobalShop

            if (player.getInventory().firstEmpty() == -1) {
                player.sendMessage(ChatColor.RED + "Your inventory is full!");
                return;
            }

            int spacesNeeded = 0;

            if (stack.getMaxStackSize() < 2) {
                spacesNeeded = stack.getAmount();
            }
            else {
                spacesNeeded = stack.getAmount() / 64;
            }
            int freeSpace = InventoryUtility.checkSlotsAvailable(player);

            if (freeSpace < spacesNeeded) {
                player.sendMessage(ChatColor.RED + "You haven't got room in your inventory (" + freeSpace + " spaces, " + spacesNeeded + " needed)!");
                return;
            }

            buyPrice = buyPrice * amount;

            if(wrapper.getBalance() >= buyPrice) {
                player.getInventory().addItem(stack);

                wrapper.setBalance(wrapper.getBalance() - buyPrice);
                MessageUtility.message(player, false, ChatColor.GRAY + "You have purchased " + ChatColor.GREEN + InventoryUtility.getFriendlyItemStackName(stack) + ChatColor.GRAY + " for " + ChatColor.GREEN + "$" + NumberUtility.getProperFormat().format(buyPrice) + ChatColor.GRAY + "!");
            }
            else {
                MessageUtility.message(player, false, ChatColor.RED + "You don't have enough money for this item. You need " + NumberUtility.getProperFormat().format(buyPrice) + " to purchase this.");
            }

        }
        else {
            MessageUtility.message(player, false, ChatColor.RED + "This item is not for sale!");
        }
    }

}

package net.elitemc.eliteteams.command;

import net.elitemc.commons.util.InventoryUtility;
import net.elitemc.commons.util.MessageUtility;
import net.elitemc.commons.util.NumberUtility;
import net.elitemc.commons.util.cmdfrmwrk.BaseCommand;
import net.elitemc.commons.util.cmdfrmwrk.CommandUsageBy;
import net.elitemc.eliteteams.handler.EconomyHandler;
import net.elitemc.eliteteams.util.shop.ShopItem;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

/**
 * Created by LavaisWatery on 2017-08-10.
 */
public class Command_price extends BaseCommand {

    public Command_price() {
        super("price", "teams.price", CommandUsageBy.PLAYER);
        setUsage("/<command> [itemName]");
        setArgRange(0, 1);
    }

    private EconomyHandler handler = EconomyHandler.getInstance();

    public void execute(CommandSender sender, String[] args) {
        Player player = (Player) sender;
        ShopItem item = null;

        if(args.length == 0) {
            ItemStack held = player.getItemInHand();

            if(held != null) {
                item = handler.fromItemStack(held);
            }
        }
        else {
            item = handler.getShopItem(args[0]);
        }

        if(item != null) {
            MessageUtility.message(player, false, ChatColor.GREEN.toString() + ChatColor.BOLD + InventoryUtility.getFriendlyItemStackName(item.toItem(1).get(0)));
            MessageUtility.message(player, false, ChatColor.GOLD + "Price Per: " + NumberUtility.getProperFormat().format(item.getPrice()));
            MessageUtility.message(player, false, ChatColor.GOLD + "Sell Price: " + NumberUtility.getProperFormat().format(item.getPrice() * item.getSellMult()));

// ChatColor.GRAY + " for " + ChatColor.GREEN + "$" + NumberUtility.getProperFormat().format(buyPrice) + ChatColor.GRAY + "!");
        }
        else {
            MessageUtility.message(player, false, ChatColor.RED + "This item isn't on the market.");
        }
    }

}

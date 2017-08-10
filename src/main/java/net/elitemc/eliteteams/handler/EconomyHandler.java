package net.elitemc.eliteteams.handler;

import net.elitemc.commons.util.Handler;
import net.elitemc.commons.util.MessageUtility;
import net.elitemc.commons.util.PlayerUtility;
import net.elitemc.eliteteams.configuration.EconomyConfiguration;
import net.elitemc.eliteteams.util.shop.ShopItem;

import java.util.HashMap;

/**
 * Created by LavaisWatery on 2017-08-08.
 */
public class EconomyHandler extends Handler {
    private static EconomyHandler instance;

    public EconomyHandler() {
        instance = this;
    }

    private EconomyConfiguration configuration = null;

    private HashMap<String, ShopItem> shopItems = new HashMap<>();

    @Override
    public void init() {
        configuration = new EconomyConfiguration(this);
    }

    @Override
    public void unload() {

    }

    public void registerShopItem(ShopItem item) {
        shopItems.put("" + item.getMaterial().getId() + ":" + item.getData(), item);
        if(item.getData() == 0) {
            shopItems.put("" + item.getMaterial().getId(), item);
            shopItems.put(item.getMaterial().toString().toLowerCase(), item);
            if(item.getMaterial().toString().contains("_")) {
                shopItems.put(item.getMaterial().toString().toLowerCase().replace("_", ""), item);
            }
        }

        if(item.getAliases() != null && !item.getAliases().isEmpty()) {
            for(String alias : item.getAliases()) {
                shopItems.put(alias, item);
            }
        }

        MessageUtility.message(PlayerUtility.getOnlinePlayers(), false, "Registering item " + item.getDisplay());
    }

    public void registerItemAlias(String alias, ShopItem item) {
        if(!shopItems.containsKey(alias)) {
            shopItems.put(alias, item);
        }
    }

    public ShopItem getShopItem(String input) {
        return shopItems.get(input);
    }

    public HashMap<String, ShopItem> getShopItems() {
        return shopItems;
    }

    public EconomyConfiguration getConfiguration() {
        return configuration;
    }

    public static EconomyHandler getInstance() {
        return instance;
    }

}

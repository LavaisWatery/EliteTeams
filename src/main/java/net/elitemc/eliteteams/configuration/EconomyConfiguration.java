package net.elitemc.eliteteams.configuration;

import net.elitemc.commons.util.PluginConfiguration;
import net.elitemc.eliteteams.EliteTeams;
import net.elitemc.eliteteams.handler.EconomyHandler;
import net.elitemc.eliteteams.util.shop.ShopItem;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by LavaisWatery on 2017-08-08.
 */
public class EconomyConfiguration extends PluginConfiguration {

    public EconomyConfiguration(EconomyHandler handler) {
        super(EliteTeams.getInstance(), "shopitems", new PluginConfiguration.CustomDefaults() {
            @Override
            public void run(FileConfiguration config) {
                try {
                    for(Material defMat : Material.values()) {
                        String path = "shopitem." + defMat.getId();

                        config.set(path + ".material", defMat.toString());
                        config.set(path + ".enabled", true);
                        config.set(path + ".price", 5);
                        config.set(path + ".sellmultiplier", .5);
                        config.set(path + ".maxstacksize", 64);
                        {
                            List<String> al = new ArrayList<String>();

                            al.add(defMat.toString().toLowerCase());
                            if(defMat.toString().contains("_")) {
                                al.add(defMat.toString().toLowerCase().replace("_", ""));
                            }

                            config.set(path + ".aliases", al);
                        }
                    }
                } catch (Exception eX) {
                    eX.printStackTrace();
                }
            }
        });
        this.handler = handler;

        setupConfig();
    }

    private EconomyHandler handler;

    @Override
    public void setupConfig() {
        FileConfiguration config = getConfig();

        if(config.contains("shopitem")) {
            try {
                for(String rawItem : config.getConfigurationSection("shopitem").getKeys(false)) {
                    try {
                        if(config.contains("shopitem." + rawItem + ".enabled") && config.getBoolean("shopitem." + rawItem + ".enabled")) {
                            ShopItem shopItem = new ShopItem("shopitem." + rawItem, config.getConfigurationSection("shopitem." + rawItem));

                            handler.registerShopItem(shopItem);
                        }
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }

}

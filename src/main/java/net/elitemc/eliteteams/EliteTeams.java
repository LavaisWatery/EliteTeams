package net.elitemc.eliteteams;

import org.bukkit.plugin.java.JavaPlugin;

/**
 * Created by LavaisWatery on 2017-07-22.
 */
public class EliteTeams extends JavaPlugin {
    private static EliteTeams instance;

    @Override
    public void onEnable() {
        instance = this;

        new Init();
    }

    @Override
    public void onDisable() {

    }

    public static EliteTeams getInstance() {
        return instance;
    }

}

package net.elitemc.eliteteams.util;

import java.util.AbstractMap;
import java.util.Arrays;
import java.util.Map;

/**
 * Created by LavaisWatery on 2017-07-08.
 */
public enum RankRewards {

    DEFAULT("Default", null, 5) {
        @Override
        public RankRewards getNextReward() {
            return RankRewards.VIP;
        }
    },

    VIP("Default", Arrays.asList(new AbstractMap.SimpleEntry<StatType, Object>(StatType.MONEY, 500), new AbstractMap.SimpleEntry<StatType, Object>(StatType.BASIC_KEY, 3)).toArray(new Map.Entry[]{}), 15) {
        @Override
        public RankRewards getNextReward() {
            return RankRewards.PRO;
        }
    },

    PRO("Default", Arrays.asList(new AbstractMap.SimpleEntry<StatType, Object>(StatType.MONEY, 1250), new AbstractMap.SimpleEntry<StatType, Object>(StatType.BASIC_KEY, 5)).toArray(new Map.Entry[]{}), 20) {
        @Override
        public RankRewards getNextReward() {
            return RankRewards.ELITE;
        }
    },

    ELITE("Default", Arrays.asList(new AbstractMap.SimpleEntry<StatType, Object>(StatType.MONEY, 2750), new AbstractMap.SimpleEntry<StatType, Object>(StatType.BASIC_KEY, 5), new AbstractMap.SimpleEntry<StatType, Object>(StatType.OMEGA_KEY, 1)).toArray(new Map.Entry[]{}), 25) {
        @Override
        public RankRewards getNextReward() {
            return RankRewards.MASTER;
        }
    },

    MASTER("Default", Arrays.asList(new AbstractMap.SimpleEntry<StatType, Object>(StatType.MONEY, 5925), new AbstractMap.SimpleEntry<StatType, Object>(StatType.BASIC_KEY, 5), new AbstractMap.SimpleEntry<StatType, Object>(StatType.OMEGA_KEY, 2)).toArray(new Map.Entry[]{}), 28) {
        @Override
        public RankRewards getNextReward() {
            return RankRewards.ULTIMATE;
        }
    },

    ULTIMATE("Default", Arrays.asList(new AbstractMap.SimpleEntry<StatType, Object>(StatType.MONEY, 17385), new AbstractMap.SimpleEntry<StatType, Object>(StatType.BASIC_KEY, 5), new AbstractMap.SimpleEntry<StatType, Object>(StatType.OMEGA_KEY, 2)).toArray(new Map.Entry[]{}), 30) {
        @Override
        public RankRewards getNextReward() {
            return RankRewards.PATRON;
        }
    },

    PATRON("Default", Arrays.asList(new AbstractMap.SimpleEntry<StatType, Object>(StatType.MONEY, 40000)).toArray(new Map.Entry[]{}), 35) {
        @Override
        public RankRewards getNextReward() {
            return null;
        }
    };

    RankRewards(String display, Map.Entry<StatType, Object>[] entries, int maxWarps) {
        this.display = display;
        this.entries = entries;
        this.maxWarps = maxWarps;
    }

    private String display;
    private Map.Entry<StatType, Object>[] entries;
    private int maxWarps;

    public Map.Entry<StatType, Object>[] getEntries() {
        return entries;
    }

    public String getDisplay() {
        return display;
    }

    public int getMaxWarps() {
        return maxWarps;
    }

    public abstract RankRewards getNextReward();
}

package net.elitemc.eliteteams.util;

import java.util.AbstractMap;

public enum TrackingArm {
    NORTH("North", 0, 0, -1) {
        @Override
        public AbstractMap.SimpleEntry<Integer, Integer> getDist(int dist) {
            return new AbstractMap.SimpleEntry<>(0, -dist * 25);
        }
    },
    EAST("East", 1, 0, 0){
        @Override
        public AbstractMap.SimpleEntry<Integer, Integer> getDist(int dist) {
            return new AbstractMap.SimpleEntry<>(dist * 25, 0);
        }
    },
    SOUTH("South", 0, 0, 1){
        @Override
        public AbstractMap.SimpleEntry<Integer, Integer> getDist(int dist) {
            return new AbstractMap.SimpleEntry<>(0, dist * 25);
        }
    },
    WEST("West", -1, 0, 0){
        @Override
        public AbstractMap.SimpleEntry<Integer, Integer> getDist(int dist) {
            return new AbstractMap.SimpleEntry<>(-dist * 25, 0);
        }
    };

    TrackingArm(String display, int xOffset, int yOffset, int zOffset) {
        this.xOffset = xOffset;
        this.yOffset = yOffset;
        this.zOffset = zOffset;
    }

    private String display;
    private int xOffset, yOffset, zOffset;

    public abstract AbstractMap.SimpleEntry<Integer, Integer> getDist(int dist);

    public String getDisplay() {
        return display;
    }

    public int getxOffset() {
        return xOffset;
    }

    public int getyOffset() {
        return yOffset;
    }

    public int getzOffset() {
        return zOffset;
    }
}

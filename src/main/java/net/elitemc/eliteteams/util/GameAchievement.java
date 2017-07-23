package net.elitemc.eliteteams.util;

import java.util.AbstractMap;

/**
 * Created by Kyle Gosleigh on 5/26/2017.
 */
public enum GameAchievement {
;
    /////////////////////////////////////////////// Basic Achievements ///////////////////////////////////////////////////////



    private GameAchievement(String display, boolean general, AchievementType type, Object target, AbstractMap.SimpleEntry<StatType, Object> reward) {
        this.display = display;
        this.general = general;
        this.type = type;
        this.target = target;
        this.reward = reward;
    }

    private String display;
    private boolean general;
    private AchievementType type;
    private Object target;
    private AbstractMap.SimpleEntry<StatType, Object> reward;

    public String getDisplay() {
        return display;
    }

    public boolean isGeneral() {
        return general;
    }

    public AchievementType getType() {
        return type;
    }

    public Object getTarget() {
        return target;
    }

    public AbstractMap.SimpleEntry<StatType, Object> getReward() {
        return reward;
    }

}


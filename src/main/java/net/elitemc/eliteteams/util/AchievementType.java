package net.elitemc.eliteteams.util;

import net.elitemc.eliteteams.handler.TeamsHandler;

import java.util.Map;

/**
 * Created by Kyle Gosleigh on 5/26/2017.
 */
public enum AchievementType {

    JOINS {
        @Override
        public boolean hasAchievement(TeamsPlayerWrapper wrapper, Object input) {
            if(input instanceof Integer) {
                return wrapper.getOriginWrapper().getJoins() >= (Integer) input;
            }

            return false;
        }
    },

    KILLS {
        @Override
        public boolean hasAchievement(TeamsPlayerWrapper wrapper, Object input) {
            if(input instanceof Integer) {
                return wrapper.getKills() >= (Integer) input;
            }

            return false;
        }
    },

    CURRENT_KILLSTREAK {
        @Override
        public boolean hasAchievement(TeamsPlayerWrapper wrapper, Object input) {
            if(wrapper instanceof TeamsPlayerWrapper && input instanceof Integer) {
                return wrapper.getCurrent_killstreak() >= (Integer) input;
            }

            return false;
        }
    },

    TOP_KILLSTREAK {
        @Override
        public boolean hasAchievement(TeamsPlayerWrapper wrapper, Object input) {
            if(wrapper instanceof TeamsPlayerWrapper && input instanceof Integer) {
                return wrapper.getTop_killstreak() >= (Integer) input;
            }

            return false;
        }
    },

    BALANCE {
        @Override
        public boolean hasAchievement(TeamsPlayerWrapper wrapper, Object input) {
            if(wrapper instanceof TeamsPlayerWrapper && input instanceof Integer) {
                return wrapper.getBalance() >= (Integer) input;
            }

            return false;
        }
    },

    BASIC_KEYS {
        @Override
        public boolean hasAchievement(TeamsPlayerWrapper wrapper, Object input) {
            if(wrapper instanceof TeamsPlayerWrapper && input instanceof Integer) {
                return wrapper.getBasic_keys() >= (Integer) input;
            }

            return false;
        }
    },

    OMEGA_KEYS {
        @Override
        public boolean hasAchievement(TeamsPlayerWrapper wrapper, Object input) {
            if(wrapper instanceof TeamsPlayerWrapper && input instanceof Integer) {
                return wrapper.getOmega_keys() >= (Integer) input;
            }

            return false;
        }
    },

    TIME_PLAYED {
        @Override
        public boolean hasAchievement(TeamsPlayerWrapper wrapper, Object input) {
            if(input instanceof Integer) {
                return wrapper.getOriginWrapper().getPlaytime() >= (Integer) input;
            }

            return false;
        }
    },

    MAKE_A_TEAM {
        @Override
        public boolean hasAchievement(TeamsPlayerWrapper wrapper, Object input) {
            return TeamsHandler.getInstance().getPlayerTeam(wrapper.getID()) != null; //TODO make sure this works
        }
    };

    public abstract boolean hasAchievement(TeamsPlayerWrapper wrapper, Object input);

}


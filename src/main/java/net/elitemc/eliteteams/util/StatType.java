package net.elitemc.eliteteams.util;

import net.elitemc.commons.util.mongo.MongoDataObject;
import net.elitemc.commons.util.mongo.pooling.ActionChange;

/**
 * Created by Kyle Gosleigh on 5/30/2017.
 */
public enum StatType {

    BASIC_KEY ("Basic Key") {
        @Override
        public void makeChange(TeamsPlayerWrapper wrapper, Object input) {
            if(input instanceof Integer) {
                if (wrapper.isLoaded()) {
                    wrapper.setBasic_keys(wrapper.getBasic_keys() + (Integer) input);
                } else {
                    wrapper.makeUnloadedChange(new ActionChange() {
                        @Override
                        public void change(MongoDataObject mongoDataObject, Object... objects) {
                            wrapper.setBasic_keys(wrapper.getBasic_keys() + (Integer) input);
                        }
                    });
                }
            }
        }

        @Override
        public Object fromVariable(String str) {
            try {
                return Integer.parseInt(str);
            } catch (Exception ex) {
                return null;
            }
        }
    },

    OMEGA_KEY ("Omega Key") {
        @Override
        public void makeChange(TeamsPlayerWrapper wrapper, Object input) {
            if(input instanceof Integer) {
                if (wrapper.isLoaded()) {
                    wrapper.setOmega_keys(wrapper.getOmega_keys() + (Integer) input);
                } else {
                    wrapper.makeUnloadedChange(new ActionChange() {
                        @Override
                        public void change(MongoDataObject mongoDataObject, Object... objects) {
                            wrapper.setOmega_keys(wrapper.getOmega_keys() + (Integer) input);
                        }
                    });
                }
            }
        }

        @Override
        public Object fromVariable(String str) {
            try {
                return Integer.parseInt(str);
            } catch (Exception ex) {
                return null;
            }
        }
    },

    MONEY ("Money") {
        @Override
        public void makeChange(TeamsPlayerWrapper wrapper, Object input) {
            if(input instanceof Integer) {
                if (wrapper.isLoaded()) {
                    wrapper.setBalance(wrapper.getBalance() + (Integer) input);
                } else {
                    wrapper.makeUnloadedChange(new ActionChange() {
                        @Override
                        public void change(MongoDataObject mongoDataObject, Object... objects) {
                            wrapper.setBalance(wrapper.getBalance() + (Integer) input);
                        }
                    });
                }
            }
        }

        @Override
        public Object fromVariable(String str) {
            try {
                return Integer.parseInt(str);
            } catch (Exception ex) {
                return null;
            }
        }
    };

    private StatType(String display) {
        this.display = display;
    }

    private String display;

    public abstract Object fromVariable(String str);

    public abstract void makeChange(TeamsPlayerWrapper wrapper, Object input);

    public String getDisplay() {
        return display;
    }

}


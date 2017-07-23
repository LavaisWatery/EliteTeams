package net.elitemc.eliteteams.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by LavaisWatery on 2017-07-22.
 *
 * This class is used for things like overriding
 */
public class Confirmations extends HashMap<String, Confirmation> {

    public void createConfirmation(String cat, Confirmation conf) {
        put(cat.toLowerCase(), conf);
    }

    public Confirmation getConfirmation(String cat) {
        return get(cat.toLowerCase());
    }

    public void clean() {
        List<Confirmation> cl = new ArrayList<>();

        for(Confirmation conf : values()) {
            cl.add(conf);
        }

        for(Confirmation conf : cl) {
            conf.clean();
        }
    }

}

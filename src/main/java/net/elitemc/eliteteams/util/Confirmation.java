package net.elitemc.eliteteams.util;

/**
 * Created by LavaisWatery on 2017-07-22.
 */
public abstract class Confirmation {

    public Confirmation(TeamsPlayerWrapper owner, String cat) {
        this.owner = owner;
        this.cat = cat;
    }

    private TeamsPlayerWrapper owner;
    private String cat;

    public abstract void accept();

    public abstract void deny();

    public void clean() {
        owner.setConfirmation(null);
    }

    public TeamsPlayerWrapper getOwner() {
        return owner;
    }

    public String getCat() {
        return cat;
    }

}

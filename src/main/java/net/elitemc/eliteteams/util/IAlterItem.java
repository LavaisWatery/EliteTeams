package net.elitemc.eliteteams.util;

/**
 * Created by withe on 6/13/2017.
 */
public abstract class IAlterItem implements IItem {

    public IAlterItem(String itemIndex) {
        this.itemIndex = itemIndex;
    }

    @Override
    public String prettyItemDisplay() {
        return "";
    }

    private String itemIndex;

    public String getItemIndex() {
        return itemIndex;
    }

}

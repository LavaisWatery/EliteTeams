package net.elitemc.eliteteams.util.shop;

import net.elitemc.commons.util.MessageUtility;
import net.elitemc.commons.util.NumberUtility;
import net.elitemc.commons.util.PlayerUtility;
import net.elitemc.eliteteams.util.ConfigurationSerializable;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by LavaisWatery on 2017-08-09.
 */
public class ShopItem implements ConfigurationSerializable {

    public ShopItem(String relPath, ConfigurationSection section) {
        deserialize(relPath, section);
    }

    private String display = "";
    private Material material = null;
    private short data = 0;
    private int maxStackSize = 64;
    private double price = 0.0D, sellMult = 0.0D;
    private List<String> aliases = null;

    public int getMaxStackSize() {
        return maxStackSize;
    }

    public void setMaxStackSize(int maxStackSize) {
        this.maxStackSize = maxStackSize;
    }

    public Material getMaterial() {
        return material;
    }

    public void setMaterial(Material material) {
        this.material = material;
    }

    public short getData() {
        return data;
    }

    public void setData(short data) {
        this.data = data;
    }

    public String getDisplay() {
        return display;
    }

    public void setDisplay(String display) {
        this.display = display;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public double getSellMult() {
        return sellMult;
    }

    public void setSellMult(double sellMult) {
        this.sellMult = sellMult;
    }

    public List<String> getAliases() {
        return aliases;
    }

    public List<ItemStack> toItem(int amount) {
        if(amount > 2304) amount = 2304;
        List<ItemStack> items = new ArrayList<>();
        int amountLeft = amount;
        int mod = (int) Math.ceil(amount / maxStackSize);

        for(int i = 0; i <= mod; i++) {
            int amnt = amountLeft >= maxStackSize ? maxStackSize : ((int) amountLeft % maxStackSize);
            items.add(new ItemStack(material, amnt, data));
            amountLeft = amountLeft - maxStackSize;
            if(amountLeft <= 0) break;
        }

        return items.isEmpty() ? null : items;
    }

    @Override
    public void serialize(ConfigurationSection section) {
        //
    }

    @Override
    public void deserialize(String relPath, ConfigurationSection section) {
        String rawPathName = section.getName();

        if(section.contains("display")) {
            this.display = section.getString("display");
        }

        this.material = Material.valueOf(section.getString("material"));
        if(rawPathName.contains(":")) {
            try {
                String[] split = rawPathName.split(":");
                String raw2 = split[1];

                if(!raw2.isEmpty() && NumberUtility.isNumber(raw2)) {
                    short d = Short.parseShort(raw2);

                    this.data = d;
                }
            } catch (Exception ex){}
        }

        this.price = section.getDouble("price");
        this.sellMult = section.getDouble("sellmultiplier");

        if(section.contains("aliases")) {
            this.aliases = section.getStringList("aliases");
        }
        if(section.contains("maxstacksize")) {
            this.maxStackSize = section.getInt("maxstacksize");
        }
        /*
        String path = "shopitem." + defMat.getId();

                        config.set(path + ".material", defMat.toString());
                        config.set(path + ".enabled", false);
                        config.set(path + ".price", 5);
                        config.set(path + ".sellmultiplier", .5);
                        {
         */
    }

}

package kezukdev.akyto.kit;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

public interface KitInterface {

    ItemStack[] content();
    ItemStack[] armor();
    ItemStack air = new ItemStack(Material.AIR);

}

package kezukdev.akyto.kit;

import java.util.List;

import org.bukkit.Material;
import org.bukkit.potion.PotionEffect;

import kezukdev.akyto.Practice;
import kezukdev.akyto.arena.ArenaType;

public abstract class Kit {

	// CREDITS TO TETELIE FOR THIS CLASS //
	// I was too lazy to redo a complete system, the code comes from him :)//
	
    public abstract String name();
    public abstract String displayName();
    public abstract Material material();
    public abstract short data();
    public abstract int id();
    public abstract ArenaType arenaType();
    public abstract List<PotionEffect> potionEffect();
    public abstract boolean isAlterable();
    public abstract boolean additionalInventory();

    public static Kit getLadder(String name, final Practice main) {
        return main.getKits().stream().filter(ladder -> ladder.name().equals(name)).findFirst().orElse(null);
    }
    
    public static Kit getLadderByDisplay(String display, final Practice main) {
        return main.getKits().stream().filter(ladder -> ladder.displayName().equals(display)).findFirst().orElse(null);
    }
    
    public static Kit getLadderByID(Integer id, final Practice main) {
        return main.getKits().stream().filter(ladder -> ladder.id() == id).findFirst().orElse(null);
    }

}
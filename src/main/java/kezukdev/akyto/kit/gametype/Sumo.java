package kezukdev.akyto.kit.gametype;

import java.util.List;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;

import kezukdev.akyto.arena.ArenaType;
import kezukdev.akyto.kit.Kit;
import kezukdev.akyto.kit.KitInterface;
import net.md_5.bungee.api.ChatColor;

public class Sumo extends Kit implements KitInterface {

    @Override
    public String name() {
        return "sumo";
    }

    @Override
    public String displayName() {
        return ChatColor.DARK_GRAY + "Sumo";
    }

    @Override
    public Material material() {
        return Material.ANVIL;
    }

    @Override
    public short data() {
        return (short)0;
    }

    @Override
    public boolean isAlterable() {
        return false;
    }

    @Override
    public boolean additionalInventory() {
        return true;
    }

    @Override
    public int id() {
        return 4;
    }

    @Override
    public ItemStack[] armor() {
        return new ItemStack[0];
    }

    @Override
    public ItemStack[] content() {
        return new ItemStack[0];
    }
	
	@Override
	public ArenaType arenaType() {
		return ArenaType.SUMO;
	}

	@Override
	public List<PotionEffect> potionEffect() {
		return null;
	}
}
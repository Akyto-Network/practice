package akyto.practice.kit.gametype;

import java.util.List;

import akyto.practice.kit.Kit;
import akyto.practice.kit.KitInterface;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;

import akyto.practice.arena.ArenaType;
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
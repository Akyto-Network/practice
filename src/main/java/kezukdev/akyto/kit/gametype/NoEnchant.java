package kezukdev.akyto.kit.gametype;

import java.util.List;

import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;

import kezukdev.akyto.arena.ArenaType;
import kezukdev.akyto.kit.Kit;
import kezukdev.akyto.kit.KitInterface;
import net.md_5.bungee.api.ChatColor;

public class NoEnchant extends Kit implements KitInterface {

    @Override
    public String name() {
        return "noenchant";
    }

    @Override
    public String displayName() {
        return ChatColor.DARK_GRAY.toString() + "NoEnchant";
    }

    @Override
    public Material material() {
        return Material.ENCHANTMENT_TABLE;
    }

    @Override
    public short data() {
        return (short)0;
    }

    @Override
    public boolean isAlterable() {
        return true;
    }

    @Override
    public boolean additionalInventory() {
        return true;
    }

    @Override
    public int id() {
        return 1;
    }

    @Override
    public ItemStack[] armor() {
    	ItemStack helmet = new ItemStack(Material.DIAMOND_HELMET);
    	ItemStack chestplate = new ItemStack(Material.DIAMOND_CHESTPLATE);
    	ItemStack leggings = new ItemStack(Material.DIAMOND_LEGGINGS);
    	ItemStack boots = new ItemStack(Material.DIAMOND_BOOTS);
        helmet.addUnsafeEnchantment(Enchantment.DURABILITY, 3);
        chestplate.addUnsafeEnchantment(Enchantment.DURABILITY, 3);
        leggings.addUnsafeEnchantment(Enchantment.DURABILITY, 3);
        boots.addUnsafeEnchantment(Enchantment.PROTECTION_FALL, 4);
        boots.addUnsafeEnchantment(Enchantment.DURABILITY, 3);
        ItemStack[] Armor = {boots, leggings, chestplate, helmet};
        return Armor;
    }
    
    public ItemStack potionNdb = new ItemStack(Material.POTION, 1, (short)16421);

    @Override
    public ItemStack[] content() {
        ItemStack attackItem = new ItemStack(Material.DIAMOND_SWORD);
        ItemStack[] Contents = {
        		attackItem,
        		new ItemStack(Material.ENDER_PEARL, 16, (short)0),
        		potionNdb,
                potionNdb,
                potionNdb,
                potionNdb,
                potionNdb,
                new ItemStack(Material.POTION, 1, (short)8226),
                new ItemStack(Material.COOKED_BEEF, 64),

                potionNdb,
                potionNdb,
                potionNdb,
                potionNdb,
                potionNdb,
                potionNdb,
                potionNdb,
                potionNdb,
                new ItemStack(Material.POTION, 1, (short)8226),

                potionNdb,
                potionNdb,
                potionNdb,
                potionNdb,
                potionNdb,
                potionNdb,
                potionNdb,
                potionNdb,
                new ItemStack(Material.POTION, 1, (short)8226),

                potionNdb,
                potionNdb,
                potionNdb,
                potionNdb,
                potionNdb,
                potionNdb,
                potionNdb,
                potionNdb,
                potionNdb,

        };
        return Contents;
    }
	
	@Override
	public ArenaType arenaType() {
		return ArenaType.NORMAL;
	}

	@Override
	public List<PotionEffect> potionEffect() {
		return null;
	}
}
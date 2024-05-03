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

public class Debuff extends Kit implements KitInterface {

    @Override
    public String name() {
        return "debuff";
    }

    @Override
    public String displayName() {
        return ChatColor.DARK_GRAY + "Debuff";
    }

    @Override
    public Material material() {
        return Material.POTION;
    }

    @Override
    public short data() {
        return (short)16426;
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
        return 2;
    }

    @Override
    public ItemStack[] armor() {
    	ItemStack helmet = new ItemStack(Material.DIAMOND_HELMET);
    	ItemStack chestplate = new ItemStack(Material.DIAMOND_CHESTPLATE);
    	ItemStack leggings = new ItemStack(Material.DIAMOND_LEGGINGS);
    	ItemStack boots = new ItemStack(Material.DIAMOND_BOOTS);
        helmet.addUnsafeEnchantment(Enchantment.PROTECTION_ENVIRONMENTAL, 2);
        helmet.addUnsafeEnchantment(Enchantment.DURABILITY, 3);
        chestplate.addUnsafeEnchantment(Enchantment.PROTECTION_ENVIRONMENTAL, 2);
        chestplate.addUnsafeEnchantment(Enchantment.DURABILITY, 3);
        leggings.addUnsafeEnchantment(Enchantment.PROTECTION_ENVIRONMENTAL, 2);
        leggings.addUnsafeEnchantment(Enchantment.DURABILITY, 3);
        boots.addUnsafeEnchantment(Enchantment.PROTECTION_ENVIRONMENTAL, 2);
        boots.addUnsafeEnchantment(Enchantment.PROTECTION_FALL, 4);
        boots.addUnsafeEnchantment(Enchantment.DURABILITY, 3);
        ItemStack[] armor = {boots, leggings, chestplate, helmet};
        return armor;
    }

    @Override
    public ItemStack[] content() {
        ItemStack attackItem = new ItemStack(Material.DIAMOND_SWORD);
        attackItem.addUnsafeEnchantment(Enchantment.DAMAGE_ALL, 3);
        attackItem.addUnsafeEnchantment(Enchantment.FIRE_ASPECT, 2);
        attackItem.addUnsafeEnchantment(Enchantment.DURABILITY, 3);
        ItemStack[] contents = {attackItem,
                new ItemStack(Material.ENDER_PEARL, 16, (short)0),
                new ItemStack(Material.POTION, 1, (short)8259),
                new ItemStack(Material.POTION, 1, (short)8226),
                new ItemStack(Material.POTION, 1, (short)16388),
                new ItemStack(Material.POTION, 1, (short)16426),
                new ItemStack(Material.POTION, 1, (short)16421),
                new ItemStack(Material.POTION, 1, (short)16421),
                new ItemStack(Material.COOKED_BEEF, 64, (short)0),

                new ItemStack(Material.POTION, 1, (short)16388),
                new ItemStack(Material.POTION, 1, (short)16426),
                new ItemStack(Material.POTION, 1, (short)16421),
                new ItemStack(Material.POTION, 1, (short)16421),
                new ItemStack(Material.POTION, 1, (short)16421),
                new ItemStack(Material.POTION, 1, (short)16421),
                new ItemStack(Material.POTION, 1, (short)16421),
                new ItemStack(Material.POTION, 1, (short)16421),
                new ItemStack(Material.POTION, 1, (short)8226),

                new ItemStack(Material.POTION, 1, (short)16426),
                new ItemStack(Material.POTION, 1, (short)16388),
                new ItemStack(Material.POTION, 1, (short)16421),
                new ItemStack(Material.POTION, 1, (short)16421),
                new ItemStack(Material.POTION, 1, (short)16421),
                new ItemStack(Material.POTION, 1, (short)16421),
                new ItemStack(Material.POTION, 1, (short)16421),
                new ItemStack(Material.POTION, 1, (short)16421),
                new ItemStack(Material.POTION, 1, (short)8226),

                new ItemStack(Material.POTION, 1, (short)16421),
                new ItemStack(Material.POTION, 1, (short)16421),
                new ItemStack(Material.POTION, 1, (short)16421),
                new ItemStack(Material.POTION, 1, (short)16421),
                new ItemStack(Material.POTION, 1, (short)16421),
                new ItemStack(Material.POTION, 1, (short)16421),
                new ItemStack(Material.POTION, 1, (short)16421),
                new ItemStack(Material.POTION, 1, (short)16421),
                new ItemStack(Material.POTION, 1, (short)16421),

        };
        return contents;
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
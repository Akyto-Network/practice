package akyto.practice.kit.gametype;

import java.util.List;

import akyto.practice.kit.Kit;
import akyto.practice.kit.KitInterface;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;

import akyto.practice.arena.ArenaType;
import net.md_5.bungee.api.ChatColor;

public class Axe extends Kit implements KitInterface {

    @Override
    public String name() {
        return "axe";
    }

    @Override
    public String displayName() {
        return ChatColor.DARK_GRAY + "Axe";
    }

    @Override
    public Material material() {
        return Material.IRON_AXE;
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
    	return 6;
    }

    @Override
    public ItemStack[] armor() {
    	ItemStack helmet = new ItemStack(Material.IRON_HELMET);
    	ItemStack chestplate = new ItemStack(Material.IRON_CHESTPLATE);
    	ItemStack leggings = new ItemStack(Material.IRON_LEGGINGS);
    	ItemStack boots = new ItemStack(Material.IRON_BOOTS);
        helmet.addUnsafeEnchantment(Enchantment.PROTECTION_ENVIRONMENTAL, 1);
        helmet.addUnsafeEnchantment(Enchantment.DURABILITY, 3);
        chestplate.addUnsafeEnchantment(Enchantment.PROTECTION_ENVIRONMENTAL, 1);
        chestplate.addUnsafeEnchantment(Enchantment.DURABILITY, 3);
        leggings.addUnsafeEnchantment(Enchantment.PROTECTION_ENVIRONMENTAL, 1);
        leggings.addUnsafeEnchantment(Enchantment.DURABILITY, 3);
        boots.addUnsafeEnchantment(Enchantment.PROTECTION_ENVIRONMENTAL, 1);
        boots.addUnsafeEnchantment(Enchantment.PROTECTION_FALL, 4);
        boots.addUnsafeEnchantment(Enchantment.DURABILITY, 3);
        ItemStack[] armor = {boots, leggings, chestplate, helmet};
        return armor;
    }

    @Override
    public ItemStack[] content() {
    	ItemStack attackItem = new ItemStack(Material.IRON_AXE);
    	attackItem.addUnsafeEnchantment(Enchantment.DAMAGE_ALL, 2);
    	attackItem.addUnsafeEnchantment(Enchantment.DURABILITY, 3);
        ItemStack[] contents = {attackItem,
                new ItemStack(Material.GOLDEN_APPLE, 16, (short)0),
                new ItemStack(Material.POTION, 1, (short)8226),
                new ItemStack(Material.POTION, 1, (short)16421),
                new ItemStack(Material.POTION, 1, (short)16421),
                new ItemStack(Material.POTION, 1, (short)16421),
                new ItemStack(Material.POTION, 1, (short)16421),
                new ItemStack(Material.POTION, 1, (short)16421),
                new ItemStack(Material.COOKED_BEEF, 64, (short)0),

                new ItemStack(Material.POTION, 1, (short)16421),
                new ItemStack(Material.POTION, 1, (short)8226),

                new ItemStack(Material.POTION, 1, (short)8226),

                new ItemStack(Material.POTION, 1, (short)8226),

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
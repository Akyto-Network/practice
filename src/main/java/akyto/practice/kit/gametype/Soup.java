package akyto.practice.kit.gametype;

import java.util.Collections;
import java.util.List;

import akyto.practice.kit.Kit;
import akyto.practice.kit.KitInterface;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import akyto.practice.arena.ArenaType;
import net.md_5.bungee.api.ChatColor;

public class Soup extends Kit implements KitInterface {

    @Override
    public String name() {
        return "soup";
    }

    @Override
    public String displayName() {
        return ChatColor.DARK_GRAY + "Soup";
    }

    @Override
    public Material material() {
        return Material.MUSHROOM_SOUP;
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
        return 5;
    }

    @Override
    public ItemStack[] armor() {
    	ItemStack helmet = new ItemStack(Material.IRON_HELMET);
    	ItemStack chestplate = new ItemStack(Material.IRON_CHESTPLATE);
    	ItemStack leggings = new ItemStack(Material.IRON_LEGGINGS);
    	ItemStack boots = new ItemStack(Material.IRON_BOOTS);
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
    
    public final ItemStack potionNdb = new ItemStack(Material.MUSHROOM_SOUP);

    @Override
    public ItemStack[] content() {
        ItemStack attackItem = new ItemStack(Material.DIAMOND_SWORD);
        attackItem.addUnsafeEnchantment(Enchantment.DAMAGE_ALL, 1);
        attackItem.addUnsafeEnchantment(Enchantment.DURABILITY, 3);
        ItemStack[] contents = {
        		attackItem,
        		potionNdb,
        		potionNdb,
                potionNdb,
                potionNdb,
                potionNdb,
                potionNdb,
                potionNdb,
                potionNdb,

                potionNdb,
                potionNdb,
                potionNdb,
                potionNdb,
                potionNdb,
                potionNdb,
                potionNdb,
                potionNdb,
                potionNdb,

                potionNdb,
                potionNdb,
                potionNdb,
                potionNdb,
                potionNdb,
                potionNdb,
                potionNdb,
                potionNdb,
                potionNdb,

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
        return contents;
    }
	
	@Override
	public ArenaType arenaType() {
		return ArenaType.NORMAL;
	}

	@Override
	public List<PotionEffect> potionEffect() {
		return Collections.singletonList(new PotionEffect(PotionEffectType.SPEED, 99999999, 1));
	}
}
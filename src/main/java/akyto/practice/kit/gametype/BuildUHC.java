package akyto.practice.kit.gametype;

import java.util.List;

import akyto.core.utils.item.ItemUtils;
import akyto.practice.kit.Kit;
import akyto.practice.kit.KitInterface;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;

import akyto.practice.arena.ArenaType;
import net.md_5.bungee.api.ChatColor;

public class BuildUHC extends Kit implements KitInterface {

    @Override
    public String name() {
        return "builduhc";
    }

    @Override
    public String displayName() {
        return ChatColor.DARK_GRAY + "Build-UHC";
    }

    @Override
    public Material material() {
        return Material.LAVA_BUCKET;
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
    public boolean buildable() {
        return true;
    }

    @Override
    public int id() {
        return 7;
    }

    @Override
    public ItemStack[] armor() {
        ItemStack helmet = new ItemStack(Material.DIAMOND_HELMET);
        ItemStack chestplate = new ItemStack(Material.DIAMOND_CHESTPLATE);
        ItemStack leggings = new ItemStack(Material.DIAMOND_LEGGINGS);
        ItemStack boots = new ItemStack(Material.DIAMOND_BOOTS);
        helmet.addUnsafeEnchantment(Enchantment.PROTECTION_PROJECTILE, 2);
        chestplate.addUnsafeEnchantment(Enchantment.PROTECTION_ENVIRONMENTAL, 2);
        leggings.addUnsafeEnchantment(Enchantment.PROTECTION_ENVIRONMENTAL, 2);
        boots.addUnsafeEnchantment(Enchantment.PROTECTION_PROJECTILE, 2);
        ItemStack[] armor = {boots, leggings, chestplate, helmet};
        return armor;
    }

    @Override
    public ItemStack[] content() {
        ItemStack attackItem = new ItemStack(Material.DIAMOND_SWORD);
        attackItem.addUnsafeEnchantment(Enchantment.DAMAGE_ALL, 3);
        ItemStack bowItem = new ItemStack(Material.BOW);
        bowItem.addUnsafeEnchantment(Enchantment.ARROW_DAMAGE, 3);
        ItemStack[] contents = {
                attackItem,
                new ItemStack(Material.FISHING_ROD),
                bowItem,
                new ItemStack(Material.COOKED_BEEF, 64),
                new ItemStack(Material.GOLDEN_APPLE, 6),
                ItemUtils.createItems(Material.GOLDEN_APPLE, ChatColor.GOLD + "Golden Head", 3),
                new ItemStack(Material.DIAMOND_PICKAXE),
                new ItemStack(Material.DIAMOND_AXE),
                new ItemStack(Material.WOOD, 64),

                air,
                air,
                air,
                air,
                air,
                air,
                air,
                air,
                air,

                air,
                air,
                air,
                air,
                air,
                air,
                air,
                air,
                air,

                new ItemStack(Material.ARROW, 20),
                new ItemStack(Material.COBBLESTONE, 64),
                new ItemStack(Material.WATER_BUCKET),
                new ItemStack(Material.WATER_BUCKET),
                new ItemStack(Material.LAVA_BUCKET),
                new ItemStack(Material.LAVA_BUCKET)

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
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

public class Classic extends Kit implements KitInterface {

    @Override
    public String name() {
        return "classic";
    }

    @Override
    public String displayName() {
        return ChatColor.DARK_GRAY + "Classic";
    }

    @Override
    public Material material() {
        return Material.IRON_SWORD;
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
        return false;
    }

    @Override
    public int id() {
        return 9;
    }

    @Override
    public ItemStack[] armor() {
        ItemStack helmet = new ItemStack(Material.DIAMOND_HELMET);
        ItemStack chestplate = new ItemStack(Material.DIAMOND_CHESTPLATE);
        ItemStack leggings = new ItemStack(Material.DIAMOND_LEGGINGS);
        ItemStack boots = new ItemStack(Material.DIAMOND_BOOTS);
        ItemStack[] armor = {boots, leggings, chestplate, helmet};
        return armor;
    }

    @Override
    public ItemStack[] content() {
        ItemStack attackItem = new ItemStack(Material.DIAMOND_SWORD);
        ItemStack bowItem = new ItemStack(Material.BOW);
        ItemStack[] contents = {
                attackItem,
                new ItemStack(Material.FISHING_ROD),
                bowItem,
                new ItemStack(Material.COOKED_BEEF, 64),
                new ItemStack(Material.GOLDEN_APPLE, 8),
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
                air,
                air,
                air,
                air,

                new ItemStack(Material.ARROW, 12),
                air,
                air,
                air,
                air,
                air

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
package akyto.practice.kit.gametype;

import akyto.practice.arena.ArenaType;
import akyto.practice.kit.Kit;
import akyto.practice.kit.KitInterface;
import com.google.common.collect.Lists;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.List;

public class BedFight extends Kit implements KitInterface {

    @Override
    public String name() {
        return "bedfight";
    }

    @Override
    public String displayName() {
        return ChatColor.DARK_GRAY + "Bed-Fight";
    }

    @Override
    public Material material() {
        return Material.BED;
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
        return 3;
    }

    @Override
    public ItemStack[] armor() {
        return new ItemStack[0];
    }

    @Override
    public ItemStack[] content() {
        ItemStack attackItem = new ItemStack(Material.WOOD_SWORD);
        ItemStack axeItem = new ItemStack(Material.WOOD_AXE);
        axeItem.addUnsafeEnchantment(Enchantment.DIG_SPEED, 1);
        ItemStack pickaxeItem = new ItemStack(Material.WOOD_PICKAXE);
        pickaxeItem.addUnsafeEnchantment(Enchantment.DIG_SPEED, 1);
        ItemStack[] contents = {
                attackItem,
                new ItemStack(Material.WOOL, 64, (byte)11),
                new ItemStack(Material.SHEARS),
                axeItem,
                pickaxeItem
        };
        return contents;
    }

    @Override
    public ArenaType arenaType() {
        return ArenaType.NORMAL;
    }

    @Override
    public List<PotionEffect> potionEffect() {
        return Lists.newArrayList(new PotionEffect(PotionEffectType.SPEED, 99999999, 1));
    }
}
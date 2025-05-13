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

public class Boxing extends Kit implements KitInterface {

    @Override
    public String name() {
        return "boxing";
    }

    @Override
    public String displayName() {
        return ChatColor.DARK_GRAY + "Boxing";
    }

    @Override
    public Material material() {
        return Material.DIAMOND_SWORD;
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
    public boolean buildable() {
        return false;
    }

    @Override
    public int id() {
        return 2;
    }

    @Override
    public ItemStack[] armor() {
        return new ItemStack[0];
    }

    @Override
    public ItemStack[] content() {
        ItemStack attackItem = new ItemStack(Material.DIAMOND_SWORD);
        attackItem.addUnsafeEnchantment(Enchantment.DAMAGE_ALL, 10);
        attackItem.addUnsafeEnchantment(Enchantment.DURABILITY, 10);
        ItemStack[] contents = { attackItem };
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
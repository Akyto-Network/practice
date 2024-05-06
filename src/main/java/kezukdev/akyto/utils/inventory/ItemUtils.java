package kezukdev.akyto.utils.inventory;

import java.util.List;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import gym.core.utils.Utils;

public class ItemUtils {
	
	public static ItemStack createItems(final Material material, final String displayName) {
		return createItems(material, 1, (short) 0, (byte) 0, displayName, false, null);
	}
	
	public static ItemStack createItems(final Material material, final String displayName, int amount) {
		return createItems(material, amount, (short) 0, (byte) 0, displayName, false, null);
	}
	
	public static ItemStack createItems(final Material material, final String displayName, short damage) {
		return createItems(material, 1, damage, (byte) 0, displayName, false, null);
	}
	
	public static ItemStack createItems(final Material material, final String displayName, Byte data) {
		return createItems(material, 1, (short) 0, data, displayName, false, null);
	}
	
	public static ItemStack createItems(final Material material, final String displayName, boolean unbreakable) {
		return createItems(material, 1, (short) 0, (byte) 0, displayName, unbreakable, null);
	}
	
	public static ItemStack createItems(final Material material, final String displayName, List<String> lore) {
		return createItems(material, 1, (short) 0, (byte) 0, displayName, false, lore);
	}
	
	public static ItemStack createItems(final Material material, final int amount, final short damage, final Byte data, final String displayName, final boolean unbreakable, final List<String> lore) {
		final ItemStack item = new ItemStack(material, amount, damage, data);
		final ItemMeta meta = item.getItemMeta();
		meta.setDisplayName(Utils.translate(displayName));
		if (lore != null) meta.setLore(lore);
		if (unbreakable) meta.spigot().setUnbreakable(unbreakable);
		item.setItemMeta(meta);
		return item;
	}

}

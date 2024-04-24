package kezukdev.akyto.profile.kiteditor;

import org.bukkit.inventory.ItemStack;

public class Edited {
	
	private String name;
	public String getName() { return name; }
	private ItemStack[] content;
	public ItemStack[] getContent() { return content; }
	private ItemStack[] armorContent;
	public ItemStack[] getArmorContent() { return armorContent; }
	
	public Edited(final String name, final ItemStack[] content, final ItemStack[] armorContent) {
		this.name = name;
		this.content = content;
		this.armorContent = armorContent;
	}
}

package akyto.practice.editor;

import lombok.Getter;
import org.bukkit.inventory.ItemStack;

@Getter
public class Edited {
	
	private final String name;
    private final ItemStack[] content;
    private final ItemStack[] armorContent;

    public Edited(final String name, final ItemStack[] content, final ItemStack[] armorContent) {
		this.name = name;
		this.content = content;
		this.armorContent = armorContent;
	}
}

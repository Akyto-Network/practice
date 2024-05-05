package kezukdev.akyto.handler.manager;

import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import kezukdev.akyto.Practice;
import kezukdev.akyto.duel.Duel.DuelType;
import kezukdev.akyto.kit.Kit;
import kezukdev.akyto.kit.KitInterface;
import kezukdev.akyto.profile.Profile;
import kezukdev.akyto.profile.ProfileState;

public class ItemManager {
	
	private final Practice main;
	
	public ItemManager(final Practice main) { this.main = main; }
	
	public void giveItems(final UUID uuid, final boolean destruct) {
		final Player player = Bukkit.getPlayer(uuid);

		if (player == null) return;

		final Profile profile = this.main.getManagerHandler().getProfileManager().getProfiles().get(uuid);

		player.getInventory().clear();
		player.getInventory().setArmorContents(null);
		if (profile.getProfileState().equals(ProfileState.FREE)) {
			if (this.main.getManagerHandler().getPartyManager().getPartyByUUID(uuid) != null && !destruct) {
				player.getInventory().setItem(0, this.main.getUtils().createItem(Material.REDSTONE_TORCH_ON, 1, (byte)0, ChatColor.RED + (this.main.getManagerHandler().getPartyManager().getPartyByUUID(uuid).getCreator().equals(uuid) ? "Disband" : "Leave") + " Party."));
				player.getInventory().setItem(1, this.main.getUtils().createItem(Material.EMERALD, 1, (byte)0, ChatColor.GRAY + "Settings"));
				player.getInventory().setItem(4, this.main.getUtils().createItem(Material.PAPER, 1, (byte)0, ChatColor.GRAY + "Party Informations"));
				player.getInventory().setItem(7, this.main.getUtils().createItem(Material.CHEST, 1, (byte)0, ChatColor.GRAY + "Other Parties"));
				player.getInventory().setItem(8, this.main.getUtils().createItem(Material.DIAMOND_AXE, 1, (byte)0, ChatColor.GRAY + "Party Events", true));
				player.updateInventory();
				return;
			}
			player.getInventory().setItem(0, this.main.getUtils().createItem(Material.IRON_SWORD, 1, (byte)0, ChatColor.GRAY + "Unranked", true));
			player.getInventory().setItem(1, this.main.getUtils().createItem(Material.DIAMOND_SWORD, 1, (byte)0, ChatColor.GRAY + "Ranked", true));
			player.getInventory().setItem(3, this.main.getUtils().createItem(Material.NAME_TAG, 1, (byte)0, ChatColor.GRAY + "Create party"));
			player.getInventory().setItem(5, this.main.getUtils().createItem(Material.SKULL_ITEM, 1, (byte)0, ChatColor.GRAY + "Profile"));
			player.getInventory().setItem(7, this.main.getUtils().createItem(Material.BOOK, 1, (byte)0, ChatColor.GRAY + "Editor"));
			player.getInventory().setItem(8, this.main.getUtils().createItem(Material.EMERALD, 1, (byte)0, ChatColor.GRAY + "Settings"));
			player.updateInventory();
		}
		if (profile.getProfileState().equals(ProfileState.MOD)) {
			player.getInventory().setItem(0, this.main.getUtils().createItem(Material.PAPER, 1, (byte)0, ChatColor.YELLOW + "View CPS " + ChatColor.GRAY + "(Right-Click)"));
			player.getInventory().setItem(1, this.main.getUtils().createItem(Material.PACKED_ICE, 1, (byte)0, ChatColor.YELLOW + "Freeze " + ChatColor.GRAY + "(Right-Click)"));
			player.getInventory().setItem(4, this.main.getUtils().createItem(Material.NETHER_STAR, 1, (byte)0, ChatColor.YELLOW + "Random Teleport " + ChatColor.GRAY + "(Right-Click)"));
			player.getInventory().setItem(7, this.main.getUtils().createItem(Material.SKULL_ITEM, 1, (byte)0, ChatColor.YELLOW + "View Stats " + ChatColor.GRAY + "(Right-Click)"));
			player.getInventory().setItem(8, this.main.getUtils().createItem(Material.REDSTONE_TORCH_ON, 1, (byte)0, ChatColor.RED + "Leave Staff-Mode."));
			player.updateInventory();
		}
		if (profile.getProfileState().equals(ProfileState.QUEUE)) {
			player.getInventory().setItem(4, this.main.getUtils().createItem(Material.REDSTONE_TORCH_ON, 1, (byte)0, ChatColor.RED + "Leave Queue."));
			player.updateInventory();
		}
		// -> Inversé les status ?
		if (profile.getProfileState().equals(ProfileState.SPECTATE)) {
			player.setAllowFlight(true);
			player.setFlying(true);
			if (this.main.getUtils().getDuelBySpectator(uuid) != null && this.main.getUtils().getDuelBySpectator(uuid).getDuelType().equals(DuelType.SINGLE)) player.getInventory().setItem(0, this.main.getUtils().createItem(Material.CHEST, 1, (byte)0, ChatColor.DARK_GRAY + "Teleport to another " + ChatColor.WHITE + "player" + ChatColor.DARK_GRAY + "."));
			player.getInventory().setItem(this.main.getUtils().getDuelBySpectator(uuid) != null && this.main.getUtils().getDuelBySpectator(uuid).getDuelType().equals(DuelType.SINGLE) ? 3 : 0, this.main.getUtils().createItem(Material.REDSTONE_COMPARATOR, 1, (byte)0, ChatColor.DARK_GRAY + "Settings"));
			if (this.main.getUtils().getDuelBySpectator(uuid) != null && this.main.getUtils().getDuelBySpectator(uuid).getDuelType().equals(DuelType.SINGLE)) player.getInventory().setItem(5, this.main.getUtils().createItem(Material.COMPASS, 1, (byte)0, ChatColor.DARK_GRAY + "Spectate another " + ChatColor.WHITE + "match" + ChatColor.DARK_GRAY + "."));	
			player.getInventory().setItem(8, this.main.getUtils().createItem(Material.REDSTONE_TORCH_ON, 1, (byte)0, ChatColor.RED + "Leave Spectating."));
			player.updateInventory();
		}
		if (profile.getProfileState().equals(ProfileState.EDITOR)) {
			final KitInterface kit = (KitInterface) Kit.getLadder(this.main.getManagerHandler().getProfileManager().getEditing().get(uuid), this.main);
			player.getInventory().setArmorContents(kit.armor());
			player.getInventory().setContents(kit.content());
			player.updateInventory();
		}
		if (profile.getProfileState().equals(ProfileState.FIGHT)) {
			final KitInterface kit = (KitInterface) this.main.getUtils().getDuelByUUID(uuid).getKit();
			if (this.main.getManagerHandler().getProfileManager().getEditor().containsKey(uuid)) {
				if (this.main.getManagerHandler().getProfileManager().getEditor().get(uuid).containsKey(this.main.getUtils().getDuelByUUID(uuid).getKit().name())) {
					player.getInventory().setItem(0, this.main.getUtils().createItem(Material.ENCHANTED_BOOK, 1, (byte) 0, ChatColor.DARK_GRAY + "Edited Kit."));
					player.getInventory().setItem(8, this.main.getUtils().createItem(Material.BOOK, 1, (byte) 0, ChatColor.DARK_GRAY + "Default Kit."));
				}
				else {
					player.getInventory().setArmorContents(kit.armor());
					player.getInventory().setContents(kit.content());
					player.updateInventory();
				}
			}
			else {
				player.getInventory().setArmorContents(kit.armor());
				player.getInventory().setContents(kit.content());
				player.updateInventory();
			}
		}
	}

}

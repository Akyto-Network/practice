package kezukdev.akyto.handler.manager;

import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import gym.core.profile.Profile;
import gym.core.profile.ProfileState;
import gym.core.utils.item.ItemUtils;
import kezukdev.akyto.Practice;
import kezukdev.akyto.duel.Duel;
import kezukdev.akyto.duel.Duel.DuelType;
import kezukdev.akyto.kit.Kit;
import kezukdev.akyto.kit.KitInterface;
import kezukdev.akyto.utils.Utils;

public class ItemManager {
	
	private final Practice main;
	
	public ItemManager(final Practice main) { this.main = main; }
	
	public void giveItems(final UUID uuid, final boolean destruct) {
		final Player player = Bukkit.getPlayer(uuid);

		if (player == null) return;

		final Profile profile = Utils.getProfiles(uuid);

		player.getInventory().clear();
		player.getInventory().setArmorContents(null);
		if (profile.isInState(ProfileState.FREE)) {
			if (Utils.getPartyByUUID(uuid) != null && !destruct) {
				player.getInventory().setItem(0, ItemUtils.createItems(Material.REDSTONE_TORCH_ON, ChatColor.RED + (Utils.getPartyByUUID(uuid).getCreator().equals(uuid) ? "Disband" : "Leave") + " Party."));
				player.getInventory().setItem(1, ItemUtils.createItems(Material.EMERALD, ChatColor.GRAY + "Settings"));
				player.getInventory().setItem(4, ItemUtils.createItems(Material.PAPER, ChatColor.GRAY + "Party Informations"));
				player.getInventory().setItem(7, ItemUtils.createItems(Material.CHEST, ChatColor.GRAY + "Other Parties"));
				player.getInventory().setItem(8, ItemUtils.createItems(Material.DIAMOND_AXE, ChatColor.GRAY + "Party Events", true));
				player.updateInventory();
				return;
			}
			player.getInventory().setItem(0, ItemUtils.createItems(Material.IRON_SWORD, ChatColor.GRAY + "Unranked", true));
			player.getInventory().setItem(1, ItemUtils.createItems(Material.DIAMOND_SWORD, ChatColor.GRAY + "Ranked", true));
			player.getInventory().setItem(3, ItemUtils.createItems(Material.NAME_TAG, ChatColor.GRAY + "Create party"));
			player.getInventory().setItem(5, ItemUtils.createItems(Material.SKULL_ITEM, ChatColor.GRAY + "Profile"));
			player.getInventory().setItem(7, ItemUtils.createItems(Material.BOOK, ChatColor.GRAY + "Editor"));
			player.getInventory().setItem(8, ItemUtils.createItems(Material.EMERALD, ChatColor.GRAY + "Settings"));
			player.updateInventory();
		}
		if (profile.isInState(ProfileState.QUEUE)) {
			player.getInventory().setItem(4, ItemUtils.createItems(Material.REDSTONE_TORCH_ON, ChatColor.RED + "Leave Queue."));
			player.updateInventory();
		}
		// -> Inversé les status ?
		if (profile.isInState(ProfileState.SPECTATE)) {
			Duel duel = Utils.getDuelBySpectator(uuid);
			if (duel == null) duel = Utils.getDuelByUUID(uuid);
			player.setAllowFlight(true);
			player.setFlying(true);
			if (duel.getDuelType().equals(DuelType.SINGLE)) player.getInventory().setItem(0, ItemUtils.createItems(Material.CHEST, ChatColor.DARK_GRAY + "Teleport to another " + ChatColor.WHITE + "player" + ChatColor.DARK_GRAY + "."));
			player.getInventory().setItem(duel.getDuelType().equals(DuelType.SINGLE) ? 3 : 0, ItemUtils.createItems(Material.REDSTONE_COMPARATOR, ChatColor.DARK_GRAY + "Settings"));
			if (duel.getDuelType().equals(DuelType.SINGLE)) player.getInventory().setItem(5, ItemUtils.createItems(Material.COMPASS, ChatColor.DARK_GRAY + "Spectate another " + ChatColor.WHITE + "match" + ChatColor.DARK_GRAY + "."));	
			player.getInventory().setItem(8, ItemUtils.createItems(Material.REDSTONE_TORCH_ON, ChatColor.RED + "Leave Spectating."));
			player.updateInventory();
		}
		if (profile.isInState(ProfileState.EDITOR)) {
			final KitInterface kit = (KitInterface) Kit.getLadder(this.main.getManagerHandler().getProfileManager().getEditing().get(uuid), this.main);
			player.getInventory().setArmorContents(kit.armor());
			player.getInventory().setContents(kit.content());
			player.updateInventory();
		}
		if (profile.isInState(ProfileState.FIGHT)) {
			final Kit kit = Utils.getDuelByUUID(uuid).getKit();
			final KitInterface kitI = (KitInterface) Utils.getDuelByUUID(uuid).getKit();
			if (this.main.getManagerHandler().getProfileManager().getEditor().containsKey(uuid)) {
				if (this.main.getManagerHandler().getProfileManager().getEditor().get(uuid).containsKey(kit.name())) {
					player.getInventory().setItem(0, ItemUtils.createItems(Material.ENCHANTED_BOOK, ChatColor.DARK_GRAY + "Edited Kit."));
					player.getInventory().setItem(8, ItemUtils.createItems(Material.BOOK, ChatColor.DARK_GRAY + "Default Kit."));
				}
				else {
					player.getInventory().setArmorContents(kitI.armor());
					player.getInventory().setContents(kitI.content());
					player.updateInventory();
				}
			}
			else {
				player.getInventory().setArmorContents(kitI.armor());
				player.getInventory().setContents(kitI.content());
				player.updateInventory();
			}
		}
	}

}

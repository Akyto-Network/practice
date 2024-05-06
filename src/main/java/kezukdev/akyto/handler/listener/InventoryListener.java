package kezukdev.akyto.handler.listener;

import kezukdev.akyto.handler.manager.InventoryManager;
import kezukdev.akyto.handler.manager.PartyManager;
import kezukdev.akyto.utils.Utils;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.event.Event.Result;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import kezukdev.akyto.Practice;
import kezukdev.akyto.duel.Duel;
import kezukdev.akyto.duel.Duel.DuelType;
import kezukdev.akyto.kit.Kit;
import kezukdev.akyto.profile.Profile;
import kezukdev.akyto.profile.ProfileState;
import kezukdev.akyto.profile.kiteditor.Edited;
import net.md_5.bungee.api.ChatColor;

public class InventoryListener implements Listener {
	
	private final Practice main;
	private final InventoryManager inventoryManager;
	
	public InventoryListener(final Practice main) {
		this.main = main;
        this.inventoryManager = main.getManagerHandler().getInventoryManager();
    }
	
	@EventHandler
	public void onInventoryClick(final InventoryClickEvent event) {
		if (event == null || event.getClickedInventory() == null || event.getCurrentItem() == null) return;
		if (event.getWhoClicked().getGameMode().equals(GameMode.CREATIVE)) return;
		if (event.getCurrentItem().getType().equals(Material.STAINED_GLASS) || event.getCurrentItem().getType().equals(Material.STAINED_GLASS_PANE) || event.getCurrentItem().getType().equals(Material.GLASS) || event.getCurrentItem().getType().equals(Material.COMPASS)) {
			event.setResult(Result.DENY);
			event.setCancelled(true);
			return;
		}
		final Inventory inventory = event.getClickedInventory();
		final String inventoryName = inventory.getName();
		final Material itemMaterial = event.getCurrentItem().getType();
		final Profile profile = Utils.getProfiles(event.getWhoClicked().getUniqueId());
		if (profile.getProfileState().equals(ProfileState.FREE)) {
			if (event.getClick().equals(ClickType.NUMBER_KEY)) {
				event.setResult(Result.DENY);
				event.setCancelled(true);
				return;
			}
			if (event.getCurrentItem().getType().equals(Material.NETHER_STAR) || event.getCurrentItem().getType().equals(Material.AIR) || event.getClickedInventory().getName().equalsIgnoreCase(event.getWhoClicked().getInventory().getName())) {
				event.setCancelled(true);
				event.setResult(Result.DENY);
				return;
			}
			if (inventoryName.equals(inventoryManager.getQueueInventory()[0].getName())) {
				this.main.getManagerHandler().getQueueManager().addPlayerToQueue(event.getWhoClicked().getUniqueId(), Kit.getLadderByID(event.getSlot(), main), false);
				event.getWhoClicked().closeInventory();
			}
			if (inventoryName.equals(inventoryManager.getQueueInventory()[1].getName())) {
				this.main.getManagerHandler().getQueueManager().addPlayerToQueue(event.getWhoClicked().getUniqueId(), Kit.getLadderByID(event.getSlot(), main), true);
				event.getWhoClicked().closeInventory();
			}
			if (inventoryName.equals(inventoryManager.getQueueInventory()[2].getName())) {
				final UUID target = this.main.getManagerHandler().getRequestManager().getStartRequest().get(event.getWhoClicked().getUniqueId());
				this.main.getManagerHandler().getRequestManager().createDuelRequest(event.getWhoClicked().getUniqueId(), target, Kit.getLadderByID(event.getSlot(), main));
				event.getWhoClicked().closeInventory();
			}
			if (inventoryName.equals(inventoryManager.getPartyEventInventory().getName())) {
				if (itemMaterial.equals(Material.IRON_AXE)) {
					event.getWhoClicked().openInventory(inventoryManager.getQueueInventory()[3]);
				}
				if (itemMaterial.equals(Material.DIAMOND_CHESTPLATE)) {
					event.getWhoClicked().openInventory(inventoryManager.getQueueInventory()[4]);
				}
			}
			if (inventoryName.equals(inventoryManager.getQueueInventory()[3].getName()) || inventoryName.equals(inventoryManager.getQueueInventory()[4].getName())) {
			    final List<UUID> shuffle = new ArrayList<>(Utils.getPartyByUUID(event.getWhoClicked().getUniqueId()).getMembers());
			    Collections.shuffle(shuffle);
			    int size = shuffle.size();
			    final List<UUID> firstTeam = Lists.newArrayList();
			    final List<UUID> secondTeam = Lists.newArrayList();
			    for (int i = 0; i < size / 2; i++) {
			        firstTeam.add(shuffle.get(i));
			    }
			    for (int i = size / 2; i < size; i++) {
			        secondTeam.add(shuffle.get(i));
			    }
			    Kit kit = Kit.getLadderByID(event.getSlot(), main);
			    new Duel(main, Sets.newHashSet(firstTeam), Sets.newHashSet(secondTeam),false,  kit, event.getClickedInventory().getName().equals(inventoryManager.getQueueInventory()[3].getName()) ? DuelType.FFA : DuelType.SPLIT);
			}
			if (inventoryName.equals(inventoryManager.getEditorInventory()[0].getName())) {
				Utils.sendToEditor(event.getWhoClicked().getUniqueId(), Kit.getLadderByDisplay(event.getCurrentItem().getItemMeta().getDisplayName(), this.main));
				event.getWhoClicked().closeInventory();
			}
			event.setCancelled(true);
		}
		if (profile.getProfileState().equals(ProfileState.SPECTATE)) {
			if (event.getClick().equals(ClickType.NUMBER_KEY)) {
				event.setResult(Result.DENY);
				event.setCancelled(true);
				return;
			}
			if (inventoryName.equals(inventoryManager.getSpectateInventory().get(event.getWhoClicked().getUniqueId()).getName())) {
				final String nextName = event.getCurrentItem().getItemMeta().getDisplayName().replace(ChatColor.WHITE.toString(), "");
				event.getWhoClicked().teleport(Bukkit.getPlayer(nextName).getLocation());
				event.getWhoClicked().closeInventory();
			}
			event.setCancelled(true);
		}
		if (profile.getProfileState().equals(ProfileState.EDITOR)) {
			if (itemMaterial.equals(Material.STAINED_GLASS) || itemMaterial.equals(Material.STAINED_GLASS_PANE) || itemMaterial.equals(Material.GLASS) || itemMaterial.equals(Material.COMPASS) || itemMaterial.equals(Material.AIR)) return;
			if (inventoryName.equals(event.getWhoClicked().getInventory().getName())) {
				return;
			}
			if (event.getClick().equals(ClickType.NUMBER_KEY)) {
				event.setResult(Result.DENY);
				event.setCancelled(true);
				return;
			}
			if (inventoryName.equals(inventoryManager.getEditorInventory()[1].getName())) {
				for (ItemStack items : event.getWhoClicked().getInventory().getContents()) {
					if (items.getType().equals(Material.COOKED_BEEF) || items.getType().equals(Material.GRILLED_PORK) || items.getType().equals(Material.GOLDEN_CARROT)) {
						if (event.getCurrentItem().getType().equals(Material.COOKED_BEEF)) items.setType(Material.COOKED_BEEF);
						if (event.getCurrentItem().getType().equals(Material.GRILLED_PORK)) items.setType(Material.GRILLED_PORK);
						if (event.getCurrentItem().getType().equals(Material.GOLDEN_CARROT)) items.setType(Material.GOLDEN_CARROT);
					}
				}
			}
			if (inventoryName.equals(inventoryManager.getEditorInventory()[2].getName())) {
				if (itemMaterial.equals(Material.BOOKSHELF)) {
					final Map<String, Edited> map = new HashMap<>();
					map.put(this.main.getManagerHandler().getProfileManager().getEditing().get(event.getWhoClicked().getUniqueId()), new Edited(this.main.getManagerHandler().getProfileManager().getEditing().get(event.getWhoClicked().getUniqueId()), event.getWhoClicked().getInventory().getContents(), event.getWhoClicked().getInventory().getArmorContents()));
					this.main.getManagerHandler().getProfileManager().getEditor().put(event.getWhoClicked().getUniqueId(), map);
					event.getWhoClicked().closeInventory();
					Bukkit.getPlayer(event.getWhoClicked().getUniqueId()).sendMessage(ChatColor.GREEN + "You have been saved the kit!");
				}
				if (itemMaterial.equals(Material.PAPER)) {
					if (this.main.getManagerHandler().getProfileManager().getEditor().get(event.getWhoClicked().getUniqueId()).get(this.main.getManagerHandler().getProfileManager().getEditing().get(event.getWhoClicked().getUniqueId())) != null) {
						event.getWhoClicked().getInventory().setArmorContents(this.main.getManagerHandler().getProfileManager().getEditor().get(event.getWhoClicked().getUniqueId()).get(this.main.getManagerHandler().getProfileManager().getEditing().get(event.getWhoClicked().getUniqueId())).getArmorContent());
						event.getWhoClicked().getInventory().setContents(this.main.getManagerHandler().getProfileManager().getEditor().get(event.getWhoClicked().getUniqueId()).get(this.main.getManagerHandler().getProfileManager().getEditing().get(event.getWhoClicked().getUniqueId())).getContent());
					}
					event.getWhoClicked().closeInventory();
					Bukkit.getPlayer(event.getWhoClicked().getUniqueId()).sendMessage(this.main.getManagerHandler().getProfileManager().getEditor().get(event.getWhoClicked().getUniqueId()).get(this.main.getManagerHandler().getProfileManager().getEditing().get(event.getWhoClicked().getUniqueId())) != null ? ChatColor.GREEN + "You have been loaded the edited kit!" : ChatColor.RED + "You have not previously edited this kit");
				}
			}
			event.setCancelled(true);
		}
		if (inventoryName.contains("Settings") && !inventoryName.contains("Spectate")) {
			if (itemMaterial.equals(Material.STAINED_GLASS) || itemMaterial.equals(Material.STAINED_GLASS_PANE) || itemMaterial.equals(Material.GLASS) || itemMaterial.equals(Material.COMPASS) || itemMaterial.equals(Material.AIR)) return;
			event.setResult(Result.DENY);
			event.setCancelled(true);
			if (itemMaterial.equals(Material.PAINTING)) {
				profile.getSettings().set(0, profile.getSettings().get(0) ? Boolean.FALSE : Boolean.TRUE);
				event.getWhoClicked().closeInventory();
				inventoryManager.refreshSettingsInventory(event.getWhoClicked().getUniqueId(), 0, false);
				Bukkit.getPlayer(event.getWhoClicked().getUniqueId()).sendMessage(ChatColor.DARK_GRAY + "You've been " + (profile.getSettings().get(0) ? ChatColor.GREEN + "enabled" : ChatColor.RED + "disabled") + ChatColor.DARK_GRAY + " you'r scoreboard");
				return;
			}
			if (itemMaterial.equals(Material.BLAZE_POWDER)) {
				profile.getSettings().set(1, profile.getSettings().get(1) ? Boolean.FALSE : Boolean.TRUE);
				event.getWhoClicked().closeInventory();
				inventoryManager.refreshSettingsInventory(event.getWhoClicked().getUniqueId(), 1, false);
				Bukkit.getPlayer(event.getWhoClicked().getUniqueId()).sendMessage(ChatColor.DARK_GRAY + "You've been " + (profile.getSettings().get(1) ? ChatColor.GREEN + "enabled" : ChatColor.RED + "disabled") + ChatColor.DARK_GRAY + " you'r duel request");
				return;
			}
			if (itemMaterial.equals(Material.WATCH)) {
				profile.getSettings().set(2, profile.getSettings().get(2) ? Boolean.FALSE : Boolean.TRUE);
				event.getWhoClicked().closeInventory();
				inventoryManager.refreshSettingsInventory(event.getWhoClicked().getUniqueId(), 2, false);
				Bukkit.getPlayer(event.getWhoClicked().getUniqueId()).sendMessage(ChatColor.DARK_GRAY + "Time set to " + (profile.getSettings().get(2) ? ChatColor.YELLOW + "day" : ChatColor.BLUE + "night"));
				Bukkit.getPlayer(event.getWhoClicked().getUniqueId()).setPlayerTime(profile.getSettings().get(2) ? 0L : 18000L, true);
				return;
			}
		}
		if (inventoryName.contains("Settings") && inventoryName.contains("Spectate")) {
			if (itemMaterial.equals(Material.STAINED_GLASS) || itemMaterial.equals(Material.STAINED_GLASS_PANE) || itemMaterial.equals(Material.GLASS) || itemMaterial.equals(Material.COMPASS) || itemMaterial.equals(Material.AIR)) return;
			if (itemMaterial.equals(Material.DIAMOND)) {
				profile.getSpectateSettings().set(0, profile.getSpectateSettings().get(0) ? Boolean.FALSE : Boolean.TRUE);
				event.getWhoClicked().closeInventory();
				final Duel duel = Utils.getDuelBySpectator(event.getWhoClicked().getUniqueId());
				if (!duel.getSpectator().isEmpty()) {
					duel.getSpectator().forEach(spectator -> {
						if (profile.getSpectateSettings().get(0)) Bukkit.getPlayer(event.getWhoClicked().getUniqueId()).showPlayer(Bukkit.getPlayer(spectator));
						if (!profile.getSpectateSettings().get(0)) Bukkit.getPlayer(event.getWhoClicked().getUniqueId()).hidePlayer(Bukkit.getPlayer(spectator));
					});
				}
				inventoryManager.refreshSettingsInventory(event.getWhoClicked().getUniqueId(), 0, true);
				Bukkit.getPlayer(event.getWhoClicked().getUniqueId()).sendMessage(ChatColor.DARK_GRAY + "You've been " + (profile.getSpectateSettings().get(0) ? ChatColor.GREEN + "show" : ChatColor.RED + "hide") + ChatColor.DARK_GRAY + " other spectators");
				return;
			}
			if (itemMaterial.equals(Material.FEATHER)) {
				profile.getSpectateSettings().set(1, profile.getSpectateSettings().get(1) ? Boolean.FALSE : Boolean.TRUE);
				event.getWhoClicked().closeInventory();
				if (profile.getSpectateSettings().get(1)) Bukkit.getPlayer(event.getWhoClicked().getUniqueId()).setFlySpeed(0.1f);
				if (!profile.getSpectateSettings().get(1)) Bukkit.getPlayer(event.getWhoClicked().getUniqueId()).setFlySpeed(0.25f);
				inventoryManager.refreshSettingsInventory(event.getWhoClicked().getUniqueId(), 1, true);
				Bukkit.getPlayer(event.getWhoClicked().getUniqueId()).sendMessage(ChatColor.DARK_GRAY + "You've been set the fly speed to " + (profile.getSpectateSettings().get(1) ? ChatColor.YELLOW + "x1.0" : ChatColor.GOLD + "x2.5"));
				return;
			}
			if (itemMaterial.equals(Material.EMERALD)) {
				event.getWhoClicked().openInventory(inventoryManager.getSettingsInventory().get(event.getWhoClicked().getUniqueId()));
				return;
			}
		}
		if (inventoryName.contains("Spectate") && !inventoryName.contains("Settings") ) {
			if (event.getCurrentItem() == null || event.getCurrentItem().getType().equals(Material.AIR)) return;
			event.setResult(Result.DENY);
			event.setCancelled(true);
			if (itemMaterial.equals(Material.STAINED_GLASS) || itemMaterial.equals(Material.STAINED_GLASS_PANE) || itemMaterial.equals(Material.GLASS) || itemMaterial.equals(Material.COMPASS) || itemMaterial.equals(Material.AIR)) return;
            String title = event.getCurrentItem().getItemMeta().getDisplayName();
            String arr[] = title.split(" ", 2);
            String first = arr[0];
            event.getWhoClicked().closeInventory();
            Bukkit.getPlayer(event.getWhoClicked().getUniqueId()).chat("/spectate " + ChatColor.stripColor(first));
		}
		if (inventoryName.contains("preview's")) {
			if (event.getCurrentItem() == null) return;
			event.setResult(Result.DENY);
			event.setCancelled(true);
			if (event.getCurrentItem().getType().equals(Material.LEVER)) {
				final String nextName = event.getCurrentItem().getItemMeta().getDisplayName().replace(ChatColor.DARK_GRAY + "Go to" + ChatColor.RESET + ": ", "");
				event.getWhoClicked().closeInventory();
				event.getWhoClicked().openInventory(inventoryManager.getPreviewInventory().get(Utils.getUUID(nextName)));
			}
		}
	}

}

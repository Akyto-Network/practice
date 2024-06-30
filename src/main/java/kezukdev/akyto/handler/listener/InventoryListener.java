package kezukdev.akyto.handler.listener;

import akyto.core.Core;
import kezukdev.akyto.handler.manager.InventoryManager;
import kezukdev.akyto.request.Request;
import kezukdev.akyto.request.Request.RequestType;
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
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import akyto.core.profile.Profile;
import akyto.core.profile.ProfileState;
import akyto.core.utils.CoreUtils;
import kezukdev.akyto.Practice;
import kezukdev.akyto.duel.Duel;
import kezukdev.akyto.duel.Duel.DuelType;
import kezukdev.akyto.editor.Edited;
import kezukdev.akyto.kit.Kit;
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
		final Material itemMaterial = event.getCurrentItem().getType();
		final Profile profile = Utils.getProfiles(event.getWhoClicked().getUniqueId());
		if (inventory.equals(inventoryManager.getLeaderboardInventory())) {
			event.setResult(Result.DENY);
			event.setCancelled(true);
		}
		if (profile.isInState(ProfileState.FREE)) {
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
			if (inventory.equals(inventoryManager.getQueueInventory()[0])) {
				this.main.getManagerHandler().getQueueManager().addPlayerToQueue(event.getWhoClicked().getUniqueId(), Kit.getLadderByID(event.getSlot(), main), false);
				event.getWhoClicked().closeInventory();
			}
			if (inventory.equals(inventoryManager.getQueueInventory()[1])) {
				this.main.getManagerHandler().getQueueManager().addPlayerToQueue(event.getWhoClicked().getUniqueId(), Kit.getLadderByID(event.getSlot(), main), true);
				event.getWhoClicked().closeInventory();
			}
			if (inventory.equals(inventoryManager.getQueueInventory()[2])) {
				final Request request = Utils.getRequestByUUID(event.getWhoClicked().getUniqueId());
				request.setKit(Kit.getLadderByID(event.getSlot(), main));
				event.getWhoClicked().openInventory(Kit.getLadderByID(event.getSlot(), main).name().equalsIgnoreCase("sumo") ? this.main.getManagerHandler().getInventoryManager().getArenaInventory()[1] : this.main.getManagerHandler().getInventoryManager().getArenaInventory()[0]);
			}
			if (inventory.equals(inventoryManager.getArenaInventory()[0]) || inventory.equals(inventoryManager.getArenaInventory()[1])) {
				final Request request = Utils.getRequestByUUID(event.getWhoClicked().getUniqueId());
				request.setArena(event.getCurrentItem().getType().equals(Material.TRAP_DOOR) ? this.main.getManagerHandler().getArenaManager().getRandomArena(request.getKit().arenaType()) : Utils.getArenaByIcon(event.getCurrentItem().getType(), request.getKit().arenaType()));
				this.main.getManagerHandler().getRequestManager().sendNotification(event.getWhoClicked().getUniqueId(), RequestType.DUEL);
				event.getWhoClicked().closeInventory();
			}
			if (inventory.equals(inventoryManager.getPartyEventInventory())) {
				if (itemMaterial.equals(Material.IRON_AXE)) {
					event.getWhoClicked().openInventory(inventoryManager.getQueueInventory()[3]);
				}
				if (itemMaterial.equals(Material.DIAMOND_CHESTPLATE)) {
					event.getWhoClicked().openInventory(inventoryManager.getQueueInventory()[4]);
				}
			}
			if (inventory.equals(inventoryManager.getQueueInventory()[3]) || inventory.equals(inventoryManager.getQueueInventory()[4])) {
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
			    new Duel(main, Sets.newHashSet(firstTeam), Sets.newHashSet(secondTeam),false,  kit, inventory.equals(inventoryManager.getQueueInventory()[3]) ? DuelType.FFA : DuelType.SPLIT, null);
			}
			if (inventory.equals(inventoryManager.getEditorInventory()[0])) {
				Utils.sendToEditor(event.getWhoClicked().getUniqueId(), Kit.getLadderByDisplay(event.getCurrentItem().getItemMeta().getDisplayName(), this.main));
				event.getWhoClicked().closeInventory();
			}
			event.setCancelled(true);
		}
		if (profile.isInState(ProfileState.SPECTATE)) {
			if (event.getClick().equals(ClickType.NUMBER_KEY)) {
				event.setResult(Result.DENY);
				event.setCancelled(true);
				return;
			}
			if (inventory.equals(inventoryManager.getSpectateInventory().get(event.getWhoClicked().getUniqueId()))) {
				final String nextName = event.getCurrentItem().getItemMeta().getDisplayName().replace(ChatColor.WHITE.toString(), "");
				event.getWhoClicked().teleport(Bukkit.getPlayer(nextName).getLocation());
				event.getWhoClicked().closeInventory();
			}
			event.setCancelled(true);
		}
		boolean is_glass = itemMaterial.equals(Material.STAINED_GLASS) || itemMaterial.equals(Material.STAINED_GLASS_PANE) || itemMaterial.equals(Material.GLASS) || itemMaterial.equals(Material.COMPASS) || itemMaterial.equals(Material.AIR);
		if (profile.isInState(ProfileState.EDITOR)) {
			if (is_glass) return;
			if (inventory.equals(event.getWhoClicked().getInventory())) {
				return;
			}
			if (event.getClick().equals(ClickType.NUMBER_KEY)) {
				event.setResult(Result.DENY);
				event.setCancelled(true);
				return;
			}
			if (inventory.equals(inventoryManager.getEditorInventory()[1])) {
				for (ItemStack items : event.getWhoClicked().getInventory().getContents()) {
					if (items.getType().equals(Material.COOKED_BEEF) || items.getType().equals(Material.GRILLED_PORK) || items.getType().equals(Material.GOLDEN_CARROT)) {
						if (event.getCurrentItem().getType().equals(Material.COOKED_BEEF)) items.setType(Material.COOKED_BEEF);
						if (event.getCurrentItem().getType().equals(Material.GRILLED_PORK)) items.setType(Material.GRILLED_PORK);
						if (event.getCurrentItem().getType().equals(Material.GOLDEN_CARROT)) items.setType(Material.GOLDEN_CARROT);
					}
				}
			}
			if (inventory.equals(inventoryManager.getEditorInventory()[2])) {
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
		if (inventory.equals(inventoryManager.getSettingsInventory().get(event.getWhoClicked().getUniqueId()))) {
			if (is_glass) return;
			event.setResult(Result.DENY);
			event.setCancelled(true);
			if (itemMaterial.equals(Material.PAINTING)) {
				profile.getSettings().set(0, profile.getSettings().getFirst() ? Boolean.FALSE : Boolean.TRUE);
				event.getWhoClicked().closeInventory();
				inventoryManager.refreshSettingsInventory(event.getWhoClicked().getUniqueId(), 0, false);
				Bukkit.getPlayer(event.getWhoClicked().getUniqueId()).sendMessage(ChatColor.DARK_GRAY + "You've been " + (profile.getSettings().getFirst() ? ChatColor.GREEN + "enabled" : ChatColor.RED + "disabled") + ChatColor.DARK_GRAY + " you'r scoreboard");
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
		if (inventory.equals(inventoryManager.getSettingsSpectateInventory().get(event.getWhoClicked().getUniqueId()))) {
			if (is_glass) return;
			if (itemMaterial.equals(Material.DIAMOND)) {
				profile.getSpectateSettings().set(0, profile.getSpectateSettings().getFirst() ? Boolean.FALSE : Boolean.TRUE);
				event.getWhoClicked().closeInventory();
				final Duel duel = Utils.getDuelBySpectator(event.getWhoClicked().getUniqueId());
				if (!duel.getSpectators().isEmpty()) {
					duel.getSpectators().forEach(spectator -> {
						if (profile.getSpectateSettings().getFirst()) Bukkit.getPlayer(event.getWhoClicked().getUniqueId()).showPlayer(Bukkit.getPlayer(spectator));
						if (!profile.getSpectateSettings().getFirst()) Bukkit.getPlayer(event.getWhoClicked().getUniqueId()).hidePlayer(Bukkit.getPlayer(spectator));
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
		final String inventoryName = inventory.getName();
		if (inventoryName.contains("Spectate") && !inventoryName.contains("Settings") ) {
			if (event.getCurrentItem() == null || event.getCurrentItem().getType().equals(Material.AIR)) return;
			event.setResult(Result.DENY);
			event.setCancelled(true);
			if (is_glass) return;
            String title = event.getCurrentItem().getItemMeta().getDisplayName();
            String first = title.split(" ", 2)[0];
            event.getWhoClicked().closeInventory();
            Bukkit.getPlayer(event.getWhoClicked().getUniqueId()).chat("/spectate " + ChatColor.stripColor(first));
		}
		if (inventoryName.contains("preview's")) {
			if (event.getCurrentItem() == null) return;
			event.setResult(Result.DENY);
			event.setCancelled(true);
			if (event.getCurrentItem().getType().equals(Material.LEVER)) {
				String nextName = event.getCurrentItem().getItemMeta().getDisplayName().replace(ChatColor.DARK_GRAY + "Go to" + ChatColor.RESET + ": ", "");
				event.getWhoClicked().closeInventory();
				if (Core.API.getManagerHandler().getProfileManager().getRealNameInDisguised().containsKey(nextName)) {
					nextName = Core.API.getManagerHandler().getProfileManager().getRealNameInDisguised().get(nextName);
				}
				event.getWhoClicked().openInventory(inventoryManager.getPreviewInventory().get(CoreUtils.getUUID(nextName)));
			}
		}
	}

	
	@EventHandler
	public void onCloseInventory(final InventoryCloseEvent event) {
		if (event.getInventory().equals(this.inventoryManager.getQueueInventory()[2])) {
			if (Utils.getRequestByUUID(event.getPlayer().getUniqueId()).getKit() == null) {
				this.main.getManagerHandler().getRequestManager().removeRequest(event.getPlayer().getUniqueId());
				Bukkit.getPlayer(event.getPlayer().getUniqueId()).sendMessage(ChatColor.RED + "You have cancelled your request duel.");
				return;
			}	
		}
		if (event.getInventory().equals(this.inventoryManager.getArenaInventory()[0]) || event.getInventory().equals(this.inventoryManager.getArenaInventory()[1])) {
			if (Utils.getRequestByUUID(event.getPlayer().getUniqueId()).getArena() == null) {
				this.main.getManagerHandler().getRequestManager().removeRequest(event.getPlayer().getUniqueId());
				Bukkit.getPlayer(event.getPlayer().getUniqueId()).sendMessage(ChatColor.RED + "You have cancelled your request duel.");
            }
		}
	}
}

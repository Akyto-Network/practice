package kezukdev.akyto.handler.manager;

import java.text.DateFormat;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import akyto.core.Core;
import akyto.core.handler.manager.ProfileManager;
import kezukdev.akyto.utils.Utils;
import org.apache.commons.lang3.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.scheduler.BukkitRunnable;

import com.google.common.collect.Lists;

import akyto.core.utils.CoreUtils;
import akyto.core.utils.format.FormatUtils;
import akyto.core.utils.inventory.MultipageSerializer;
import akyto.core.utils.item.ItemUtils;
import kezukdev.akyto.Practice;
import kezukdev.akyto.arena.Arena;
import kezukdev.akyto.arena.ArenaType;
import kezukdev.akyto.duel.Duel;
import kezukdev.akyto.duel.cache.DuelStatistics;
import kezukdev.akyto.handler.manager.QueueManager.QueueEntry;
import kezukdev.akyto.kit.Kit;
import kezukdev.akyto.utils.leaderboard.Top;
import lombok.Getter;

@Getter
public class InventoryManager {
	
	DateFormat shortDateFormat = DateFormat.getDateTimeInstance(
            DateFormat.SHORT,
            DateFormat.SHORT);
	
	private final Practice main;
	private final Inventory[] queueInventory = new Inventory[5];
	private final Inventory[] arenaInventory = new Inventory[2];
	private final Inventory[] editorInventory = new Inventory[3];
	private final Inventory leaderboardInventory = Bukkit.createInventory(null, 9, ChatColor.GRAY + "Leaderboard:");
	private final Inventory partyEventInventory = Bukkit.createInventory(null, InventoryType.HOPPER, ChatColor.DARK_GRAY + "Party Event:");
    public MultipageSerializer spectateMultipage;
    public MultipageSerializer partyMultipage;
	private final ConcurrentMap<UUID, Inventory> previewInventory;
	private final ConcurrentMap<UUID, Inventory> spectateInventory;
	private final ConcurrentMap<UUID, Inventory> settingsInventory;
	private final ConcurrentMap<UUID, Inventory> settingsSpectateInventory;
	
	
	public InventoryManager(final Practice main) {
		this.main = main;
		this.previewInventory = new ConcurrentHashMap<>();
		this.spectateInventory = new ConcurrentHashMap<>();
		this.settingsInventory = new ConcurrentHashMap<>();
		this.settingsSpectateInventory = new ConcurrentHashMap<>();
		this.spectateMultipage = new MultipageSerializer(new ArrayList<>(), ChatColor.GRAY + "Spectate", ItemUtils.createItems(Material.COMPASS, ChatColor.GRAY + " * " + ChatColor.DARK_GRAY + "Spectate" + ChatColor.GRAY + " * "));
		this.partyMultipage = new MultipageSerializer(new ArrayList<>(), ChatColor.GRAY + "Partys", ItemUtils.createItems(Material.CHEST, ChatColor.GRAY + " * " + ChatColor.DARK_GRAY + "Other Party" + ChatColor.GRAY + " * "));
		this.queueInventory[0] = Bukkit.createInventory(null, 9, ChatColor.GRAY + "Unranked queue:");
		this.queueInventory[1] = Bukkit.createInventory(null, 9, ChatColor.GRAY + "Ranked queue:");
		this.queueInventory[2] = Bukkit.createInventory(null, 9, ChatColor.GRAY + "Select duel kit:");
		this.queueInventory[3] = Bukkit.createInventory(null, 9, ChatColor.GRAY + "Select ffa kit:");
		this.queueInventory[4] = Bukkit.createInventory(null, 9, ChatColor.GRAY + "Select split kit:");
		this.arenaInventory[0] = Bukkit.createInventory(null, 9*4, ChatColor.GRAY + "Select Normal Arena:");
		this.arenaInventory[1] = Bukkit.createInventory(null, 9*3, ChatColor.GRAY + "Select Sumo Arena:");
		for (Arena arena : this.main.getArenas()) {
			this.arenaInventory[arena.getArenaType().equals(ArenaType.NORMAL) ? 0 : 1].addItem(ItemUtils.createItems(arena.getIcon(), ChatColor.GRAY + " » " + ChatColor.YELLOW + StringUtils.capitalize(arena.getName())));
		}
		this.arenaInventory[0].setItem(35, ItemUtils.createItems(Material.TRAP_DOOR, ChatColor.GRAY + " » " + ChatColor.GOLD + "Random Arena"));
		this.arenaInventory[1].setItem(26, ItemUtils.createItems(Material.TRAP_DOOR, ChatColor.GRAY + " » " + ChatColor.GOLD + "Random Arena"));
		this.editorInventory[0] = Bukkit.createInventory(null, 9, ChatColor.GRAY + "Select kit:");
		this.editorInventory[1] = Bukkit.createInventory(null, InventoryType.HOPPER, ChatColor.GRAY + "More:");
		this.editorInventory[2] = Bukkit.createInventory(null, InventoryType.HOPPER, ChatColor.GRAY + "Management:");
		this.leaderboardInventory.setItem(8, ItemUtils.createItems(Material.NETHER_STAR, ChatColor.GRAY + " » " + ChatColor.RED + "Top #10 " + ChatColor.WHITE + "Global"));
		this.setQueueInventory();
		this.runnableLeaderboardInventory();
		this.setEditorInventory();
	}

	public void refreshPartyInventory() {
        List<ItemStack> partys = new ArrayList<>();
        this.main.getManagerHandler().getPartyManager().getParties().forEach(party -> {
            final ItemStack item = new ItemStack(Material.SKULL_ITEM);
            final ItemMeta meta = item.getItemMeta();
            meta.setDisplayName(ChatColor.GOLD + CoreUtils.getName(party.getCreator()) + "'s party");
            final List<String> lore = new ArrayList<>();
            lore.add(ChatColor.GRAY + "Member(s) [" + party.getMembers().size() + "]");
            party.getMembers().forEach(member -> lore.add(ChatColor.GRAY + " -> " + ChatColor.WHITE + CoreUtils.getName(member)));
            meta.setLore(lore);
            item.setItemMeta(meta);
            partys.add(item);
        });
        this.partyMultipage.refresh(partys);
	}
	
	public void refreshSpectateInventory() {
        List<ItemStack> matchs = new ArrayList<>();
        this.main.getDuels().forEach(duel -> {
            final ItemStack item = new ItemStack(duel.getKit().material(), 1, duel.getKit().data());
            final ItemMeta meta = item.getItemMeta();
            meta.setDisplayName(ChatColor.GREEN + CoreUtils.getName(duel.getFirst().iterator().next()) + ChatColor.GRAY + " vs " + ChatColor.RED + CoreUtils.getName(duel.getSecond().iterator().next()));
            meta.setLore(Arrays.asList(ChatColor.GRAY + "In: " + (duel.isRanked() ? ChatColor.GOLD + "Ranked" : ChatColor.YELLOW + "Unranked"), ChatColor.GRAY + "Kit: " + ChatColor.YELLOW + ChatColor.stripColor(duel.getKit().displayName())));
            item.setItemMeta(meta);
            matchs.add(item);
        });
        this.spectateMultipage.refresh(matchs);
	}

	private void setEditorInventory() {
		this.editorInventory[0].clear();
		for (Kit kit : this.main.getKits()) {
			if (kit.isAlterable()) {
				final ItemStack item = new ItemStack(kit.material(), 1, kit.data());
				final ItemMeta meta = item.getItemMeta();
				meta.setDisplayName(kit.displayName());
				item.setItemMeta(meta);
				this.editorInventory[0].addItem(item);
			}
		}
		this.editorInventory[1].setItem(0, new ItemStack(Material.COOKED_BEEF));
		this.editorInventory[1].setItem(1, new ItemStack(Material.GOLDEN_CARROT));
		this.editorInventory[1].setItem(2, new ItemStack(Material.GRILLED_PORK));
		this.editorInventory[1].setItem(3, new ItemStack(Material.STAINED_GLASS_PANE, 1, (short)8));
		this.editorInventory[1].setItem(4, new ItemStack(Material.STAINED_GLASS_PANE, 1, (short)8));
		this.editorInventory[2].setItem(0, new ItemStack(Material.STAINED_GLASS_PANE, 1, (short)8));
		this.editorInventory[2].setItem(1, ItemUtils.createItems(Material.WRITTEN_BOOK, ChatColor.GRAY + "Load Edited Kit"));
		this.editorInventory[2].setItem(2, new ItemStack(Material.STAINED_GLASS_PANE, 1, (short)8));
		this.editorInventory[2].setItem(3, ItemUtils.createItems(Material.BOOKSHELF, ChatColor.GRAY + "Save Kit"));
		this.editorInventory[2].setItem(4, new ItemStack(Material.STAINED_GLASS_PANE, 1, (short)8));
	}

	private void setQueueInventory() {
		for (Kit kit : this.main.getKits()) {
			final ItemStack item = new ItemStack(kit.material(), 1, kit.data());
			final ItemMeta meta = item.getItemMeta();
			meta.setDisplayName(kit.displayName());
			final List<Inventory> invs = Arrays.asList(this.queueInventory[0], this.queueInventory[1], this.queueInventory[2], this.queueInventory[3], this.queueInventory[4], this.leaderboardInventory);
			invs.forEach(inv -> {
				meta.setLore(Arrays.asList(ChatColor.GRAY + "Queueing: " + ChatColor.RESET + this.getQueuedFromLadder(kit, !inv.equals(this.queueInventory[0])), ChatColor.GRAY + "Fighting: " + ChatColor.RESET + this.getMatchedFromLadder(kit, !inv.equals(this.queueInventory[0]))));
				if (inv.equals(this.queueInventory[2]) || inv.equals(this.leaderboardInventory)) {
					meta.setLore(null);
				}
				item.setItemMeta(meta);
				inv.setItem(kit.id(), item);
			});
		}
		this.partyEventInventory.setItem(1, ItemUtils.createItems(Material.IRON_AXE, ChatColor.DARK_GRAY + "Free For All"));
		this.partyEventInventory.setItem(3, ItemUtils.createItems(Material.DIAMOND_CHESTPLATE, ChatColor.DARK_GRAY + "Split"));
	}
	
	public void refreshQueueInventory(boolean ranked, final Kit kit) {
		final Inventory inv = ranked ? this.queueInventory[1] : this.queueInventory[0];
		final ItemStack item = inv.getItem(kit.id());
		ItemMeta meta = item.getItemMeta();
		meta.setDisplayName(kit.displayName());
        List<String> lore = new ArrayList<>();
        lore.add(ChatColor.GRAY + "Queueing: " + ChatColor.RESET + this.getQueuedFromLadder(kit, ranked));
        lore.add(ChatColor.GRAY + "Fighting: " + ChatColor.RESET + this.getMatchedFromLadder(kit, ranked));
        meta.setLore(lore);
		item.setItemMeta(meta);
		inv.removeItem(inv.getItem(kit.id()));
		inv.setItem(kit.id(), item);
	}
	
	public void generatePreviewInventory(final UUID uuid, final UUID opponent) {
		final Inventory preview = Bukkit.createInventory(null, 54, ChatColor.DARK_GRAY + CoreUtils.getName(uuid) + " preview's");
		final DuelStatistics duelStatistics = this.main.getManagerHandler().getProfileManager().getDuelStatistics().get(uuid);
		final Duel duel = Utils.getDuelByUUID(uuid);
		final Kit kit = duel.getKit();
        preview.setContents(Bukkit.getPlayer(uuid).getInventory().getContents());
        preview.setItem(36, Bukkit.getPlayer(uuid).getInventory().getArmorContents()[3]);
        preview.setItem(37, Bukkit.getPlayer(uuid).getInventory().getArmorContents()[2]);
        preview.setItem(38, Bukkit.getPlayer(uuid).getInventory().getArmorContents()[1]);
        preview.setItem(39, Bukkit.getPlayer(uuid).getInventory().getArmorContents()[0]);
        final ItemStack glass = new ItemStack(Material.STAINED_GLASS_PANE, 1, (short)8);
		final int addition = opponent != null ? 0 : +1;
        for (int i = 45; i < 54; ++i) {
            preview.setItem(i, glass);
        }
        preview.setItem(47, ItemUtils.createItems(Material.SKULL_ITEM, ChatColor.GRAY + " * " + ChatColor.WHITE + "Player Informations" + ChatColor.RESET + ": ", Arrays.asList(ChatColor.RESET + FormatUtils.formatTime((long) Bukkit.getPlayer(uuid).getHealth(), 2.0d) + ChatColor.DARK_RED + "❤")));
		preview.setItem(48, ItemUtils.createItems(Material.COOKED_BEEF, ChatColor.GRAY + " * " + ChatColor.WHITE + "Food Informations" + ChatColor.RESET + ": ", Arrays.asList(ChatColor.DARK_GRAY + "Food Level" + ChatColor.RESET + ": " + FormatUtils.formatTime(Bukkit.getPlayer(uuid).getFoodLevel(), 2.0d)))); // TODO Orginiser a la rimk
        List<String> effectsInfo = new ArrayList<>();
		for (PotionEffect potionEffect : Bukkit.getPlayer(uuid).getActivePotionEffects())
			effectsInfo.add(ChatColor.GRAY + potionEffect.formatted() + ChatColor.WHITE + " for " + ChatColor.RED + (FormatUtils.formatTime(potionEffect.getDuration() / 20)));
        if (Bukkit.getPlayer(uuid).getActivePotionEffects().isEmpty()) {
        	effectsInfo.add(ChatColor.RED + "No Effects.");
        }
		preview.setItem(49+addition, ItemUtils.createItems(Material.BREWING_STAND_ITEM, ChatColor.GRAY + " * " + ChatColor.WHITE + "Effects Informations" + ChatColor.RESET + ": ", effectsInfo));

        final List<String> loreStats = new ArrayList<>();
        if (kit.name().equals("nodebuff") || kit.name().equals("debuff") || kit.name().equals("noenchant") || kit.name().equals("axe")) {
            loreStats.add(ChatColor.DARK_GRAY + "Pots Left" + ChatColor.RESET + ": " + Bukkit.getPlayer(uuid).getInventory().all(new ItemStack(Material.POTION, 1, (short)16421)).size());
        }	
        loreStats.add(ChatColor.DARK_GRAY + "Hits" + ChatColor.RESET + ": " + duelStatistics.getHits());
        loreStats.add(ChatColor.DARK_GRAY + "Best Combo" + ChatColor.RESET + ": " + duelStatistics.getLongestHit());
		preview.setItem(50+addition, ItemUtils.createItems(Material.DIAMOND_SWORD, ChatColor.GRAY + " * " + ChatColor.WHITE + "Statistics" + ChatColor.RESET + ": ", loreStats));
		if (opponent != null) preview.setItem(53, ItemUtils.createItems(Material.LEVER, ChatColor.DARK_GRAY + "Go to" + ChatColor.RESET + ": " + CoreUtils.getName(opponent)));
        this.previewInventory.remove(uuid);
		this.previewInventory.put(uuid, preview);
	}
	
	public void generateSettingsInventory(final UUID uuid) {
		final Inventory profile = Bukkit.createInventory(null, 9, ChatColor.DARK_GRAY + "Settings");
        final ItemStack glass = new ItemStack(Material.STAINED_GLASS_PANE, 1, (short)8);
        for (int i = 0; i < 9; ++i) {
            profile.setItem(i, glass);
        }
		final ProfileManager profileManager = Core.API.getManagerHandler().getProfileManager();
        profile.setItem(0, ItemUtils.createItems(Material.PAINTING, ChatColor.DARK_GRAY + "Scoreboard" + ChatColor.GRAY + ":", Arrays.asList(ChatColor.YELLOW + "Loading...")));
        profile.setItem(1, ItemUtils.createItems(Material.BLAZE_POWDER, ChatColor.DARK_GRAY + "Duel Request" + ChatColor.GRAY + ":", Arrays.asList(ChatColor.YELLOW + "Loading...")));
        profile.setItem(2, ItemUtils.createItems(Material.WATCH, ChatColor.DARK_GRAY + "Time" + ChatColor.GRAY + ":", Arrays.asList(ChatColor.YELLOW + "Loading...")));
		profile.setItem(3, ItemUtils.createItems(Material.REDSTONE, ChatColor.DARK_GRAY + "Spectator" + ChatColor.GRAY + ":", Arrays.asList(ChatColor.YELLOW + "Loading...")));
		profile.setItem(4, ItemUtils.createItems(Material.PAPER, ChatColor.DARK_GRAY + "Private Message" + ChatColor.GRAY + ":", Arrays.asList(ChatColor.YELLOW + "Loading...")));
		profile.setItem(5, ItemUtils.createItems(Material.SKULL_ITEM, ChatColor.DARK_GRAY + "Drops" + ChatColor.GRAY + ":", Arrays.asList(ChatColor.YELLOW + "Loading...")));
		profile.setItem(6, ItemUtils.createItems(Material.BONE, ChatColor.DARK_GRAY + "Clear Inventory" + ChatColor.GRAY + ":", Arrays.asList(ChatColor.YELLOW + "Loading...")));
		profile.setItem(8, ItemUtils.createItems(Material.FIREWORK, ChatColor.DARK_GRAY + "Death Effects" + ChatColor.GRAY + ":", Arrays.asList(ChatColor.YELLOW + "Click here to select a death effect")));
        this.settingsInventory.remove(uuid);
		this.settingsInventory.put(uuid, profile);
		profileManager.refreshSettingsLoreInv(profile, uuid, true);
		this.generateSpectateSettingsInventory(uuid);
	}
	
	public void removeUselessInventory(final UUID uuid) {
		this.settingsInventory.remove(uuid);
		this.settingsSpectateInventory.remove(uuid);
		Core.API.getManagerHandler().getInventoryManager().getProfileInventory().remove(uuid);
	}
	
	private void generateSpectateSettingsInventory(final UUID uuid) {
		final Inventory profile = Bukkit.createInventory(null, InventoryType.HOPPER, ChatColor.DARK_GRAY + "Spectate Settings");
        final ItemStack glass = new ItemStack(Material.STAINED_GLASS_PANE, 1, (short)8);
        for (int i = 0; i < 5; ++i) {
            profile.setItem(i, glass);
        }
        profile.setItem(0, ItemUtils.createItems(Material.DIAMOND, ChatColor.DARK_GRAY + "Display Other Spectators" + ChatColor.GRAY + ":", Arrays.asList(ChatColor.YELLOW + "Loading...")));
        profile.setItem(1, ItemUtils.createItems(Material.FEATHER, ChatColor.DARK_GRAY + "Fly Speed", Arrays.asList(ChatColor.YELLOW + "Loading...")));
        profile.setItem(4, ItemUtils.createItems(Material.EMERALD, ChatColor.DARK_GRAY + "Global Settings", Arrays.asList(ChatColor.YELLOW + "Loading...")));
        this.settingsSpectateInventory.remove(uuid);
		this.settingsSpectateInventory.put(uuid, profile);
		final ProfileManager profileManager = Core.API.getManagerHandler().getProfileManager();
		profileManager.refreshSettingsLoreInv(profile, uuid, false);

	}

	public void refreshSettingsInventory(final UUID uuid, final int id, final boolean spectate) {
		final ProfileManager profileManager = Core.API.getManagerHandler().getProfileManager();
		if (!spectate) {
			final Inventory inv = this.settingsInventory.get(uuid);
			profileManager.refreshSettingsLoreInv(inv, uuid, true);

		} else {
			final Inventory inv = this.settingsSpectateInventory.get(uuid);
			profileManager.refreshSettingsLoreInv(inv, uuid, false);
		}
	}
	
	public void generateChangeSpectateInventory(final UUID playerUUID) {
		final Inventory spectate = Bukkit.createInventory(null, InventoryType.HOPPER, ChatColor.GRAY + "Teleport to:");
		final Duel duel = Utils.getDuelBySpectator(playerUUID);
		List<List<UUID>> spec = Arrays.asList(new ArrayList<>(duel.getFirst()), new ArrayList<>(duel.getSecond()));
        final ItemStack glass = new ItemStack(Material.STAINED_GLASS_PANE, 1, (short)8);
        for (int i = 0; i < 5; ++i) {
            spectate.setItem(i, glass);
        }
		spec.forEach(uuids -> uuids.forEach(uuid -> {
	        ItemStack playerHead = new ItemStack(Material.SKULL_ITEM, 1, (short) 3);
	        SkullMeta meta = (SkullMeta) playerHead.getItemMeta();
	        meta.setOwner(Bukkit.getPlayer(uuid).getName());
	        meta.setDisplayName(Bukkit.getPlayer(uuid).getName());
	        playerHead.setItemMeta(meta);
	        spectate.setItem(duel.getFirst().contains(uuid) ? 1 : 3, playerHead);
		}));
        this.spectateInventory.remove(playerUUID);
		this.spectateInventory.put(playerUUID, spectate);
	}
	
	private void runnableLeaderboardInventory() {
		new BukkitRunnable() {
			
			@Override
			public void run() {
	            main.getManagerHandler().getLeaderboardManager().refresh();
	            Top[] top = main.getManagerHandler().getLeaderboardManager().getTop();
	            Top global_top = main.getManagerHandler().getLeaderboardManager().getGlobal();
				main.getKits().forEach(ladder -> {
					ItemStack current = leaderboardInventory.getItem(ladder.id());
					ItemMeta meta = current.getItemMeta();
					List<String> lore = new ArrayList<>();
					lore.add(ChatColor.DARK_GRAY.toString() + ChatColor.STRIKETHROUGH + "---------------------");
					lore.addAll(top[ladder.id()].getLore());
					lore.add(ChatColor.DARK_GRAY.toString() + ChatColor.STRIKETHROUGH + "---------------------");
					meta.setLore(lore);
					current.setItemMeta(meta);
				});
				ItemStack current = leaderboardInventory.getItem(8);
				ItemMeta meta = current.getItemMeta();
				meta.setLore(global_top.getLore());
				current.setItemMeta(meta);
			}
		}.runTaskLaterAsynchronously(this.main, 2L);
	}
	
	public void refreshLeaderboard() {
    	CompletableFuture<Void> refresh = CompletableFuture.runAsync(() -> this.main.getManagerHandler().getLeaderboardManager().refresh());
    	refresh.whenCompleteAsync((t, u) -> {
            Top[] top = this.main.getManagerHandler().getLeaderboardManager().getTop();
            Top global_top = this.main.getManagerHandler().getLeaderboardManager().getGlobal();
			this.main.getKits().forEach(ladder -> {
				ItemStack current = this.leaderboardInventory.getItem(ladder.id());
				ItemMeta meta = current.getItemMeta();
				List<String> lore = new ArrayList<>();
				lore.add(ChatColor.DARK_GRAY.toString() + ChatColor.STRIKETHROUGH + "---------------------");
				lore.addAll(top[ladder.id()].getLore());
				lore.add(ChatColor.DARK_GRAY.toString() + ChatColor.STRIKETHROUGH + "---------------------");
				meta.setLore(lore);
				current.setItemMeta(meta);
			});
			ItemStack current = leaderboardInventory.getItem(8);
			ItemMeta meta = current.getItemMeta();
			List<String> list = Lists.newArrayList();
			list.add(ChatColor.DARK_GRAY.toString() + ChatColor.STRIKETHROUGH + "---------------------");
			list.addAll(global_top.getLore());
			list.add(ChatColor.DARK_GRAY.toString() + ChatColor.STRIKETHROUGH + "---------------------");
			meta.setLore(list);
			current.setItemMeta(meta);
    	});
	}

	
	public int getQueuedFromLadder(Kit kit, boolean ranked) {
		int count = 0;
		for (Map.Entry<UUID, QueueEntry> map : main.getQueue().entrySet()) {
			QueueEntry value = map.getValue();
			if (value.getKit() == kit && value.isRanked() == ranked) {
				count++;
			}
		}
		return count;
	}
	
	public int getMatchedFromLadder(Kit kit, boolean ranked) {
		int count = 0;
		for (Duel duel : this.main.getDuels()) {
			if (duel.getKit() == kit && duel.isRanked() == ranked) {
				count++;
			}
		}
        return count * 2;
	}

}

package akyto.practice.handler.manager;

import akyto.core.Core;
import akyto.core.profile.ProfileState;
import akyto.core.utils.CoreUtils;
import akyto.core.utils.format.FormatUtils;
import akyto.practice.Practice;
import akyto.practice.arena.Arena;
import akyto.practice.duel.Duel;
import akyto.practice.duel.Duel.DuelType;
import akyto.practice.duel.cache.DuelState;
import akyto.practice.kit.Kit;
import akyto.practice.runnable.countdown.CountdownDuel;
import akyto.practice.runnable.RespawnRunnable;
import akyto.practice.runnable.sumo.SumoRunnable;
import akyto.practice.utils.Utils;
import akyto.practice.utils.chat.MessageUtils;
import akyto.practice.utils.data.DataUtils;
import akyto.practice.utils.match.DurationUtils;
import akyto.practice.utils.match.EloUtils;
import akyto.practice.utils.match.MatchUtils;
import akyto.practice.utils.match.TagUtils;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import gg.potted.idb.DB;
import lombok.Getter;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.*;
import java.util.concurrent.CompletableFuture;

@Getter
public class DuelManager {
	
	private final Practice main;
	
	public DuelManager(final Practice main) { this.main = main; }
	
	public void start(final List<UUID> first, final List<UUID> second, final Kit kit) {
		final Duel duel = Utils.getDuelByUUID(first.getFirst());
		Arena arena = duel.getArena() == null ? this.main.getManagerHandler().getArenaManager().getRandomArena(kit) : duel.getArena();
		duel.setArena(arena);
		final UUID idCopy;
		if (kit.buildable()) {
			idCopy = this.main.getManagerHandler().getArenaManager().createCopy(arena, duel);
			if (idCopy == null) {
				System.err.println("Failed to create arena copy for " + arena.getName());
				return;
			}
		} else {
			idCopy = null;
		}
		this.main.getManagerHandler().getInventoryManager().refreshSpectateInventory();
		if (!duel.getDuelType().equals(DuelType.FFA)){
			TagUtils.setupTeams(first, second);
		}
		if (duel.getDuelType().equals(DuelType.SPLIT)) {
			MessageUtils.sendSplitMessage(first, second, kit);
		}
		Arrays.asList(first, second).forEach(uuids -> uuids.forEach(uuid -> {
			if (Bukkit.getPlayer(uuid) == null) {
				if (duel.getDuelType().equals(DuelType.SINGLE)) {
					this.endSingle(first.getFirst().equals(uuid) ? second.getFirst() : first.getFirst());
					return;
				}
				else {
					MatchUtils.addKill(uuid, null, false);
				}
			}
			MatchUtils.multiArena(uuid, false, false);
			Utils.getOpponents(uuid).forEach(ops -> Bukkit.getPlayer(uuid).hidePlayer(Bukkit.getPlayer(ops))); // Fix Tracker problem
			Utils.getAllies(uuid).forEach(allies -> Bukkit.getPlayer(uuid).hidePlayer(Bukkit.getPlayer(allies))); // Fix Tracker problem
			this.main.getManagerHandler().getInventoryManager().refreshQueueInventory(Utils.getDuelByUUID(uuid).isRanked(), kit);
			Utils.resetPlayer(uuid);
			final Player player = Bukkit.getPlayer(uuid);
			if (duel.getDuelType().equals(DuelType.SINGLE)) {
				player.sendMessage(ChatColor.DARK_GRAY + "Your opponent is " +
				ChatColor.WHITE + (first.getFirst().equals(uuid) ? CoreUtils.getName(second.getFirst()) : CoreUtils.getName(first.getFirst())) +
				(duel.isRanked() ? ChatColor.GRAY + " (" + ChatColor.RED + Utils.getProfiles(Utils.getOpponents(uuid).getFirst()).getStats().get(2)[kit.id()] + "elo" + ChatColor.GRAY + ")" : ""));
			}
			if (duel.getDuelType().equals(DuelType.FFA)) {
				Bukkit.getPlayer(uuid).sendMessage(ChatColor.GRAY + "A match was launched, this time in FFA!");
			}
			DataUtils.addPlayedToData(uuid, kit);
			if (!kit.buildable()) this.teleport(duel, idCopy);
		}));
		if (kit.name().equalsIgnoreCase("sumo")) new SumoRunnable(this.main, duel).runTaskTimer(this.main, 5L, 5L);
		new CountdownDuel(Arrays.asList(first, second), duel, this.main).runTaskTimer(this.main, 20L, 20L);
	}

	public void teleport(final Duel duel, final UUID id) {
		final List<UUID> players = Lists.newArrayList();
		players.addAll(duel.getFirst());
		players.addAll(duel.getSecond());
		final Location locationFirst = duel.getKit().buildable() ? Utils.getArenaCopyID(id).getPosition().getFirst().toBukkitLocation() : duel.getArena().getPosition().getFirst().toBukkitLocation();
		final Location locationSecond = duel.getKit().buildable() ? Utils.getArenaCopyID(id).getPosition().getLast().toBukkitLocation() : duel.getArena().getPosition().getLast().toBukkitLocation();
		players.forEach(uuid -> {
			final Player player = Bukkit.getPlayer(uuid);
			if (player != null) {
				player.teleport(duel.getFirst().contains(uuid) ? locationFirst : locationSecond);
				Utils.getProfiles(uuid).setProfileState(ProfileState.FIGHT);
				this.main.getManagerHandler().getItemManager().giveItems(uuid, false);
				if (duel.getKit().potionEffect() != null) { Bukkit.getPlayer(uuid).addPotionEffects(duel.getKit().potionEffect()); }
			}
		});
	}

	public void endSingle(final UUID winner) {
		final Duel duel = Utils.getDuelByUUID(winner);
		duel.setWinner(Lists.newArrayList(winner));
		final UUID looser = duel.getFirst().contains(winner) ? new ArrayList<>(duel.getSecond()).getFirst() : new ArrayList<>(duel.getFirst()).getFirst();
		List<UUID> players = new ArrayList<>();
		players.add(winner);
		players.add(looser);
		if (!duel.getSpectators().isEmpty()) players.addAll(new ArrayList<>(duel.getSpectators()));
		DataUtils.addWinToData(winner, duel.getKit());
		players.forEach(uuid -> {
			if (!duel.getSpectators().contains(uuid)) {
				this.main.getManagerHandler().getInventoryManager().generatePreviewInventory(uuid, Utils.getOpponents(uuid).getFirst());
				if (Utils.getProfiles(winner).getSettings()[6] != 1){
					Bukkit.getPlayer(winner).getInventory().clear();
				}
			}
		});
		MessageUtils.sendSingleEndMessage(duel, players, winner, looser);
		if (duel.isRanked()) {
			EloUtils.eloChanges(winner, looser, duel.getKit(), main);
		}
		players.forEach(uuid -> {
			if (duel.isRanked() && !duel.getSpectators().contains(uuid)) {
		        final String elos = FormatUtils.getStringValue(Utils.getProfiles(uuid).getStats().get(2), ":");
				CompletableFuture<Void> update = CompletableFuture.runAsync(() -> DB.executeUpdateAsync("UPDATE playersdata SET elos=? WHERE name=?", elos, Bukkit.getServer().getPlayer(uuid).getName()));
		        update.whenCompleteAsync((t, u) -> {
					this.main.getManagerHandler().getInventoryManager().refreshLeaderboard();
					Utils.changeNicknameDiscord(Bukkit.getPlayer(uuid) ,success -> System.out.println("Discord Username successfuly updated"));
				});
			}
			Core.API.getManagerHandler().getInventoryManager().generateProfileInventory(uuid, this.main.getKitNames());
			});
		CoreUtils.matchHistory(CoreUtils.getName(winner), CoreUtils.getName(looser), ChatColor.stripColor(duel.getKit().displayName()), duel.getArena().getName(), DurationUtils.getFormattedDuration(duel), duel.isRanked(), Utils.getProfiles(winner).getStats().get(2)[duel.getKit().id()], Utils.getProfiles(looser).getStats().get(2)[duel.getKit().id()]);
		if (duel.getTimer() != null) {
			duel.getTimer().cancel();
		}
		duel.setState(DuelState.FINISHING);
		TagUtils.clearEntries(Arrays.asList(Sets.newHashSet(winner), Sets.newHashSet(looser)));
		if (!duel.getSpectators().isEmpty()) players.removeAll(duel.getSpectators());
		new RespawnRunnable(Collections.singletonList(Sets.newHashSet(players)), duel.getSpectators().stream().toList(), this.main).runTaskLater(this.main, 70L);
	}

	public void endMultiple(final UUID winner) {
		final Duel duel = Utils.getDuelByUUID(winner);
		final Set<UUID> winners = duel.getFirst().contains(winner) ? Sets.newHashSet(duel.getFirst()) : Sets.newHashSet(duel.getSecond());
		final Set<UUID> losers = duel.getFirst().contains(winner) ? Sets.newHashSet(duel.getSecond()) : Sets.newHashSet(duel.getFirst());
		duel.getWinner().addAll(winners);
		if (duel.getTimer() != null){
			duel.getTimer().cancel();
		}
		winners.forEach(uuid -> Practice.API.getManagerHandler().getInventoryManager().generatePreviewInventory(uuid, losers.stream().toList().getFirst()));
		duel.setState(DuelState.FINISHING);
		MessageUtils.sendPartyComponent(Arrays.asList(winners, losers));
		Arrays.asList(winners, losers).forEach(uuids -> uuids.forEach(uuid -> {
			if (Bukkit.getPlayer(uuid) != null) {
				Core.API.getManagerHandler().getInventoryManager().generateProfileInventory(uuid, this.main.getKitNames());
			}
		}));
		Arrays.asList(winners, losers).forEach(teams -> teams.forEach(players -> {
			if(Bukkit.getPlayer(players) == null) {
				if (winners.contains(players)) winners.remove(players);
				if (losers.contains(players)) losers.remove(players);
			}
		}));
		if (!duel.getDuelType().equals(DuelType.FFA)) {
			TagUtils.clearEntries(Arrays.asList(winners, losers));
		}
		new RespawnRunnable(Arrays.asList(winners, losers), duel.getSpectators().stream().toList(), this.main).runTaskLater(this.main, 70L);
	}
}

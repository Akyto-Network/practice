package kezukdev.akyto.handler.manager;

import java.util.*;
import java.util.stream.Collectors;

import gg.potted.idb.DB;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import com.google.common.collect.Lists;

import gym.core.Core;
import gym.core.profile.ProfileState;
import gym.core.utils.components.ComponentJoiner;
import gym.core.utils.format.FormatUtils;
import kezukdev.akyto.Practice;
import kezukdev.akyto.arena.Arena;
import kezukdev.akyto.duel.Duel;
import kezukdev.akyto.duel.Duel.DuelType;
import kezukdev.akyto.duel.cache.DuelState;
import kezukdev.akyto.kit.Kit;
import kezukdev.akyto.runnable.CountdownRunnable;
import kezukdev.akyto.runnable.RespawnRunnable;
import kezukdev.akyto.runnable.SumoRunnable;
import kezukdev.akyto.utils.Utils;
import kezukdev.akyto.utils.chat.MessageUtils;
import kezukdev.akyto.utils.data.DataUtils;
import kezukdev.akyto.utils.match.EloUtils;
import kezukdev.akyto.utils.match.MatchUtils;
import kezukdev.akyto.utils.match.TagUtils;
import lombok.Getter;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.TextComponent;

@Getter
public class DuelManager {
	
	private final Practice main;
	
	public DuelManager(final Practice main) { this.main = main; }
	
	public void start(final List<UUID> first, final List<UUID> second, final Kit kit) {
		final Duel duel = Utils.getDuelByUUID(first.get(0));
		Arena arena = duel.getArena() != null ? duel.getArena() : this.main.getManagerHandler().getArenaManager().getRandomArena(kit.arenaType());
		this.main.getManagerHandler().getInventoryManager().refreshSpectateInventory();
		TagUtils.setupTeams(first, second);
		if (duel.getDuelType().equals(DuelType.SPLIT)) {
			MessageUtils.sendSplitMessage(first, second, kit);
		}
		Arrays.asList(first, second).forEach(uuids -> {
			uuids.forEach(uuid -> {
				if (Bukkit.getPlayer(uuid) == null) {
					if (duel.getDuelType().equals(DuelType.SINGLE)) {
						this.endSingle(first.get(0).equals(uuid) ? second.get(0) : first.get(0));
						return;
					}
					else {
						MatchUtils.addKill(uuid, null, false);
					}
				}
				MatchUtils.multiArena(uuid, true, false);
				Bukkit.getPlayer(uuid).showPlayer(Bukkit.getPlayer(Utils.getOpponents(uuid).get(0)));
				this.main.getManagerHandler().getInventoryManager().refreshQueueInventory(Utils.getDuelByUUID(uuid).isRanked(), kit);
				Utils.resetPlayer(uuid);
				final Player player = Bukkit.getPlayer(uuid);
				if (duel.getDuelType().equals(DuelType.SINGLE)) {
					player.sendMessage(ChatColor.DARK_GRAY + "Your opponent is " +
					ChatColor.WHITE + (first.get(0).equals(uuid) ? Bukkit.getPlayer(second.get(0)).getName() : Bukkit.getPlayer(first.get(0)).getName()) +
					(duel.isRanked() ? ChatColor.GRAY + " (" + ChatColor.RED + Utils.getProfiles(Utils.getOpponents(uuid).get(0)).getStats().get(2)[kit.id()] + "elo" + ChatColor.GRAY + ")" : ""));
				}
				if (duel.getDuelType().equals(DuelType.FFA)) {
					Bukkit.getPlayer(uuid).sendMessage(ChatColor.GRAY + "A match was launched, this time in FFA!");
				}
				player.teleport(first.contains(uuid) ? arena.getPosition().get(0).toBukkitLocation() : arena.getPosition().get(1).toBukkitLocation());
				DataUtils.addPlayedToData(uuid, kit);
				Utils.getProfiles(uuid).setProfileState(ProfileState.FIGHT);
				this.main.getManagerHandler().getItemManager().giveItems(uuid, false);
				if (kit.potionEffect() != null) { Bukkit.getPlayer(uuid).addPotionEffects(kit.potionEffect()); }
			});
		});
		if (kit.name().equalsIgnoreCase("sumo")) new SumoRunnable(this.main, duel).runTaskTimer(this.main, 5L, 5L);
		new CountdownRunnable(Arrays.asList(first, second), duel, this.main).runTaskTimer(this.main, 20L, 20L);
	}
	
	public void endSingle(final UUID winner) {
		final Duel duel = Utils.getDuelByUUID(winner);
		duel.setWinner(Lists.newArrayList(winner));
		if (duel.getTimer() != null) {
			duel.getTimer().cancel();	
		}
		duel.setState(DuelState.FINISHING);
		final UUID looser = duel.getFirst().contains(winner) ? new ArrayList<>(duel.getSecond()).get(0) : new ArrayList<>(duel.getFirst()).get(0);
		List<UUID> players = new ArrayList<>();
		players.add(winner);
		players.add(looser);
		if (!duel.getSpectators().isEmpty()) players.addAll(new ArrayList<>(duel.getSpectators()));
		DataUtils.addWinToData(winner, duel.getKit());
		players.forEach(uuid -> {
			if (!duel.getSpectators().contains(uuid)) {
				this.main.getManagerHandler().getProfileManager().getDuelStatistics().get(uuid).removeEnderPearlCooldown();
				this.main.getManagerHandler().getInventoryManager().generatePreviewInventory(uuid, Utils.getOpponents(uuid).get(0));
			}
			Player player = Bukkit.getPlayer(uuid);
			if (player != null) {
				player.sendMessage(ChatColor.GRAY.toString() + ChatColor.STRIKETHROUGH + "---------------------------");
				player.sendMessage(ChatColor.YELLOW + "Match Information");
				player.spigot().sendMessage(MessageUtils.endMessage(winner, looser));
				if (duel.isRanked()) {
					player.sendMessage(ChatColor.GRAY + "Elo Changes: " + (winner.equals(uuid) ? ChatColor.GREEN + "+" : ChatColor.RED + "-") + (int) Math.min(Math.max(1.0D / (1.0D + Math.pow(10.0D, (Utils.getProfiles(winner).getStats().get(2)[duel.getKit().id()] - Utils.getProfiles(looser).getStats().get(2)[duel.getKit().id()]) / 400.0D)) * 32.0D, 4), 40));
				}
				if (!duel.getSpectators().isEmpty()) {
					player.sendMessage("");
					String specMsg = ChatColor.GRAY + "Spectators (" + duel.getSpectators().size() + ChatColor.GRAY + "): " +
							duel.getSpectators().stream()
									.map(spec -> ChatColor.WHITE + Bukkit.getPlayer(spec).getName())
									.collect(Collectors.joining(", "));
					player.sendMessage(specMsg);
				}
				player.sendMessage(ChatColor.GRAY.toString() + ChatColor.STRIKETHROUGH + "---------------------------");
			}
		});
		if (duel.isRanked()) {
			EloUtils.eloChanges(winner, looser, duel.getKit(), main);
		}
		players.forEach(uuid -> {
			if (duel.isRanked() && !duel.getSpectators().contains(uuid)) {
		        final String elos = FormatUtils.getStringValue(Utils.getProfiles(uuid).getStats().get(2), ":");
		        DB.executeUpdateAsync("UPDATE playersdata SET elos=? WHERE name=?", elos, Bukkit.getServer().getPlayer(uuid).getName()).join();
		        this.main.getManagerHandler().getInventoryManager().refreshLeaderboard();
			}
			Core.API.getManagerHandler().getInventoryManager().generateProfileInventory(uuid, this.main.getKits().size(), this.main.getKitNames());
			});
		TagUtils.clearEntries(Arrays.asList(Lists.newArrayList(winner), Lists.newArrayList(looser)));
		new RespawnRunnable(Collections.singletonList(players), this.main).runTaskLater(this.main, 70L);
	}

	public void endMultiple(final UUID winner) {
		final Duel duel = Utils.getDuelByUUID(winner);
		final List<UUID> winners = duel.getFirst().contains(winner) ? new ArrayList<>(duel.getFirst()) : new ArrayList<>(duel.getSecond());
		final List<UUID> loosers = duel.getFirst().contains(winner) ? new ArrayList<>(duel.getSecond()) : new ArrayList<>(duel.getFirst());
		duel.getWinner().addAll(winners);
		duel.getTimer().cancel();
		duel.setState(DuelState.FINISHING);
		MessageUtils.sendPartyComponent(Arrays.asList(winners, loosers));
		Arrays.asList(winners, loosers).forEach(uuids -> uuids.forEach(uuid -> Core.API.getManagerHandler().getInventoryManager().generateProfileInventory(uuid, this.main.getKits().size(), this.main.getKitNames())));
		TagUtils.clearEntries(Arrays.asList(winners, loosers));
		new RespawnRunnable(Arrays.asList(winners, loosers), this.main).runTaskLater(this.main, 70L);
	}
}

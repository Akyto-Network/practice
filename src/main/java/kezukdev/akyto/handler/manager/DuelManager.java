package kezukdev.akyto.handler.manager;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import co.aikar.idb.DB;
import kezukdev.akyto.Practice;
import kezukdev.akyto.arena.Arena;
import kezukdev.akyto.duel.Duel;
import kezukdev.akyto.duel.DuelParty;
import kezukdev.akyto.duel.cache.DuelState;
import kezukdev.akyto.kit.Kit;
import kezukdev.akyto.profile.ProfileState;
import kezukdev.akyto.runnable.CountdownRunnable;
import kezukdev.akyto.runnable.RespawnRunnable;
import kezukdev.akyto.runnable.SumoRunnable;
import kezukdev.akyto.utils.EloUtils;
import kezukdev.akyto.utils.chat.ComponentJoiner;
import lombok.Getter;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.TextComponent;

@Getter
public class DuelManager {
	
	private Practice main;
	
	public DuelManager(final Practice main) { this.main = main; }
	
	public void startSingle(final List<UUID> players, final Kit kit) {
		final Arena arena = this.main.getManagerHandler().getArenaManager().getRandomArena(kit.arenaType());
		this.main.getManagerHandler().getInventoryManager().refreshSpectateInventory();
		players.forEach(uuid -> {
			if (Bukkit.getPlayer(uuid) == null) {
				this.endSingle(players.get(0).equals(uuid) ? players.get(1) : players.get(0));
				return;
			}
			this.main.getUtils().multiArena(uuid, this.main.getUtils().getOpponent(uuid), false);
			Bukkit.getPlayer(uuid).showPlayer(Bukkit.getPlayer(this.main.getUtils().getOpponent(uuid)));
			this.main.getManagerHandler().getInventoryManager().refreshQueueInventory(this.main.getUtils().getDuelByUUID(uuid).isRanked(), kit);
			this.main.getUtils().resetPlayer(uuid);
			final Player player = Bukkit.getPlayer(uuid);
			player.sendMessage(ChatColor.DARK_GRAY + "Your opponent is " + ChatColor.WHITE + (players.get(0).equals(uuid) ? Bukkit.getPlayer(players.get(1)).getName() : Bukkit.getPlayer(players.get(0)).getName()) + (this.main.getUtils().getDuelByUUID(uuid).isRanked() ? ChatColor.GRAY + " (" + ChatColor.RED + this.main.getUtils().getProfiles(this.main.getUtils().getOpponent(uuid)).getStats().get(2)[kit.id()] + "elo" + ChatColor.GRAY + ")" : ""));
			player.teleport(players.get(0).equals(uuid) ? arena.getPosition().get(0).toBukkitLocation() : arena.getPosition().get(1).toBukkitLocation());
			this.main.getUtils().addPlayedToData(uuid, kit);
			this.main.getUtils().getProfiles(uuid).setProfileState(ProfileState.FIGHT);
			this.main.getManagerHandler().getItemManager().giveItems(uuid, false);
			if (kit.potionEffect() != null) { Bukkit.getPlayer(uuid).addPotionEffects(kit.potionEffect()); }
		});
		if (kit.name().equalsIgnoreCase("sumo")) new SumoRunnable(this.main, this.main.getUtils().getDuelByUUID(players.get(0))).runTaskTimer(this.main, 5L, 5L);
		new CountdownRunnable(Arrays.asList(players), this.main).runTaskTimer(this.main, 20L, 20L);
	}
	
	public void endSingle(final UUID winner) {
		final Duel duel = this.main.getUtils().getDuelByUUID(winner);
		duel.setWinner(winner);
		if (duel.getTimer() != null) {
			duel.getTimer().cancel();	
		}
		duel.setState(DuelState.FINISHING);
		final UUID looser = duel.getFirst().contains(winner) ? duel.getSecond().stream().collect(Collectors.toList()).get(0) : duel.getFirst().stream().collect(Collectors.toList()).get(0);
		List<UUID> players = new ArrayList<UUID>();
		players.add(winner);
		players.add(looser);
		if (!duel.getSpectator().isEmpty()) players.addAll(duel.getSpectator().stream().collect(Collectors.toList()));
		this.main.getUtils().addWinToData(winner, duel.getKit());
		this.main.getUtils().clearDrops(winner);
		players.forEach(uuid -> {
			if (!duel.getSpectator().contains(uuid)) {
				this.main.getManagerHandler().getProfileManager().getDuelStatistics().get(uuid).removeEnderPearlCooldown();
				this.main.getManagerHandler().getInventoryManager().generatePreviewInventory(uuid, this.main.getUtils().getOpponent(uuid));
			}
			if (Bukkit.getPlayer(uuid) != null) {
				Bukkit.getPlayer(uuid).sendMessage(ChatColor.GRAY.toString() + ChatColor.STRIKETHROUGH + "---------------------------");
				Bukkit.getPlayer(uuid).sendMessage(ChatColor.YELLOW + "Match Information");
				Bukkit.getPlayer(uuid).spigot().sendMessage(this.main.getUtils().endMessage(winner, looser));
				if (duel.isRanked()) {
					Bukkit.getPlayer(uuid).sendMessage(ChatColor.GRAY + "Elo Changes: " + (winner.equals(uuid) ? ChatColor.GREEN + "+" : ChatColor.RED + "-") + String.valueOf((int) Math.min(Math.max(1.0D / (1.0D + Math.pow(10.0D, (main.getManagerHandler().getProfileManager().getProfiles().get(winner).getStats().get(2)[duel.getKit().id()] - main.getManagerHandler().getProfileManager().getProfiles().get(looser).getStats().get(2)[duel.getKit().id()]) / 400.0D)) * 32.0D, 4), 40)));
				}
				if (!duel.getSpectator().isEmpty()) {
					Bukkit.getPlayer(uuid).sendMessage(" ");
				    final ComponentJoiner joiner = new ComponentJoiner(ChatColor.GRAY + ", ");
			        final TextComponent spectxt = new TextComponent(ChatColor.GRAY + "Spectators (" + duel.getSpectator().size() + ChatColor.GRAY + "): ");
			        duel.getSpectator().forEach(spec -> {
			        	final TextComponent stxt = new TextComponent(ChatColor.WHITE + Bukkit.getPlayer(spec).getName());
			        	joiner.add(stxt);
			        });
			        spectxt.addExtra(joiner.toTextComponent());
					Bukkit.getPlayer(uuid).spigot().sendMessage(spectxt);
				}
				Bukkit.getPlayer(uuid).sendMessage(ChatColor.GRAY.toString() + ChatColor.STRIKETHROUGH + "---------------------------");
			}
		});
		if (duel.isRanked()) {
			EloUtils.eloChanges(winner, looser, duel.getKit(), main);
		}
		players.forEach(uuid -> {
			if (duel.isRanked() && !duel.getSpectator().contains(uuid)) {
		        final String elos = this.main.getUtils().getStringValue(this.main.getManagerHandler().getProfileManager().getProfiles().get(uuid).getStats().get(2), ":");
		        DB.executeUpdateAsync("UPDATE playersdata SET elos=? WHERE name=?", elos, Bukkit.getServer().getPlayer(uuid).getName()).join();
		        this.main.getManagerHandler().getInventoryManager().refreshLeaderboard();
			}
			this.main.getManagerHandler().getInventoryManager().generateProfileInventory(uuid);
			});
		new RespawnRunnable(Arrays.asList(players), false, this.main).runTaskLater(this.main, 70L);
	}
	
	public void startMultiple(final List<List<UUID>> players, final Kit kit) {
		final Arena arena = this.main.getManagerHandler().getArenaManager().getRandomArena(kit.arenaType());
		final DuelParty duel = this.main.getUtils().getDuelPartyByUUID(players.get(0).get(0));
		duel.arena = arena;
		players.forEach(uuids -> {
			uuids.forEach(uuid -> {
				if (Bukkit.getPlayer(uuid) == null) {
					this.endMultiple(players.get(0).contains(uuid) ? players.get(1).get(0) : players.get(0).get(0));
					return;
				}
				this.main.getUtils().multiArena(uuid, this.main.getUtils().getOpponent(uuid), false);
				Bukkit.getPlayer(uuid).showPlayer(Bukkit.getPlayer(this.main.getUtils().getOpponent(uuid)));
				this.main.getUtils().resetPlayer(uuid);
				final Player player = Bukkit.getPlayer(uuid);
				if (duel.getDuelPartyType().equals("ffa")) {
					Bukkit.getPlayer(uuid).sendMessage(ChatColor.GRAY + "A match was launched, this time in FFA!");
				}
				player.teleport(players.get(0).contains(uuid) ? arena.getPosition().get(0).toBukkitLocation() : arena.getPosition().get(1).toBukkitLocation());
				this.main.getUtils().addPlayedToData(uuid, kit);
				this.main.getUtils().getProfiles(uuid).setProfileState(ProfileState.FIGHT);
				this.main.getManagerHandler().getItemManager().giveItems(uuid, false);
				if (kit.potionEffect() != null) { Bukkit.getPlayer(uuid).addPotionEffects(kit.potionEffect()); }
			});
		});
		if (duel.getDuelPartyType().equals("split") || duel.getDuelPartyType().equals("duel")) {
			this.main.getUtils().sendSplitMessage(players.get(0), players.get(1), kit);
		}
		new CountdownRunnable(players, this.main).runTaskTimer(this.main, 20L, 20L);
	}

	public void endMultiple(final UUID winner) {
		final DuelParty duel = this.main.getUtils().getDuelPartyByUUID(winner);
		final List<UUID> winners = duel.getFirst().contains(winner) ? duel.getFirst().stream().collect(Collectors.toList()) : duel.getSecond().stream().collect(Collectors.toList());
		final List<UUID> loosers = duel.getFirst().contains(winner) ? duel.getSecond().stream().collect(Collectors.toList()) : duel.getFirst().stream().collect(Collectors.toList());
		duel.getWinner().addAll(winners);
		duel.getTimer().cancel();
		duel.setState(DuelState.FINISHING);
		this.main.getUtils().sendPartyComponent(duel.getDuelPartyType(), Arrays.asList(winners, loosers));
		Arrays.asList(winners, loosers).forEach(uuids -> uuids.forEach(uuid -> this.main.getManagerHandler().getInventoryManager().generateProfileInventory(uuid)));
		new RespawnRunnable(Arrays.asList(winners, loosers), true, this.main).runTaskLater(this.main, 70L);
	}
}

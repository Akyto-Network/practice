package akyto.practice.utils.match;

import akyto.core.Core;
import akyto.core.profile.Profile;
import akyto.core.profile.ProfileState;
import akyto.core.utils.CoreUtils;
import akyto.practice.Practice;
import akyto.practice.duel.Duel;
import akyto.practice.duel.Duel.DuelType;
import akyto.practice.duel.cache.DuelState;
import akyto.practice.duel.cache.DuelStatistics;
import akyto.practice.handler.ManagerHandler;
import akyto.practice.handler.manager.QueueManager;
import akyto.practice.handler.manager.TournamentManager;
import akyto.practice.kit.Kit;
import akyto.practice.tournament.TournamentEntry;
import akyto.practice.utils.Utils;
import akyto.practice.utils.chat.MessageHelper;
import akyto.practice.utils.chat.MessageUtils;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class MatchUtils {
	
    public static void addKill(final UUID uuid, final UUID killer, boolean disconnect) {
        final Duel match = Utils.getDuelByUUID(uuid);
        final ManagerHandler managerHandler = Practice.API.getManagerHandler();
        if (match == null) return;
		if (disconnect) {
			match.getDisconnected().add(uuid);
		}
		managerHandler.getInventoryManager().generatePreviewInventory(uuid, killer);
        if (match.getDuelType().equals(DuelType.FFA)) {
        	match.getAlives().remove(uuid);
			final UUID winner = killer == null ? match.getAlives().stream().toList().getFirst() : killer;
            if (match.getAlives().size() == 1) {
				managerHandler.getDuelManager().endMultiple(winner);
                return;
            }

            List<UUID> allPlayers = Stream.of(match.getFirst(), match.getSecond())
                    .flatMap(Collection::stream)
                    .toList();

            allPlayers.forEach(uuids -> {
                Player player = Bukkit.getPlayer(uuids);
                if (player != null) {
                    player.sendMessage(ChatColor.WHITE + CoreUtils.getName(uuid) +  ChatColor.GRAY + (killer == null ? " died." : " has been killed by " + ChatColor.WHITE + CoreUtils.getName(killer)));
                }
            });
            
            match.getSpectators().forEach(uuids -> {
                Player spectator = Bukkit.getPlayer(uuids);
                if (spectator != null) {
                    spectator.sendMessage(ChatColor.WHITE + CoreUtils.getName(uuid) +  ChatColor.GRAY + (killer == null ? " died." : " has been killed by " + ChatColor.WHITE + CoreUtils.getName(killer)));
                }
            });
            if (Bukkit.getPlayer(uuid) != null) {
				Bukkit.getScheduler().runTaskLater(Practice.API, () -> addSpectateParty(uuid), 2L);
            }
        }

        if (match.getDuelType().equals(DuelType.SPLIT)) {
			match.getFirstAlives().remove(uuid);
			match.getSecondAlives().remove(uuid);
            if ((match.getFirstAlives().isEmpty() || match.getSecondAlives().isEmpty())) {
				TournamentEntry entry = Utils.getTournamentByPlayerUUID(uuid);
				final UUID killerSafe = killer == null ? (match.getFirst().contains(uuid) ? match.getSecondAlives().stream().toList().getFirst() : match.getFirstAlives().stream().toList().getFirst()) : killer;
				if (entry == null) managerHandler.getDuelManager().endMultiple(killerSafe);
				else handleTournamentDeath(Bukkit.getPlayer(uuid), Bukkit.getPlayer(killerSafe), entry);
                return;
            }

			List<UUID> allPlayers = Stream.of(match.getFirst(), match.getSecond())
					.flatMap(Collection::stream)
					.toList();

			allPlayers.forEach(uuids -> {
				Player player = Bukkit.getPlayer(uuids);
				if (player != null) {
					int aliveSize = match.getFirstAlives().contains(uuid) ? match.getFirstAlives().size() : match.getSecondAlives().size();
					int totalSize = match.getFirst().contains(uuid) ? match.getFirst().size() : match.getSecond().size();
					player.sendMessage(ChatColor.WHITE + CoreUtils.getName(uuid) + ChatColor.GRAY + (killer == null ? " died." : " has been killed by " + ChatColor.WHITE + CoreUtils.getName(killer) +ChatColor.GRAY + " (" + ChatColor.GREEN + aliveSize + ChatColor.GRAY + "/" + ChatColor.RED + totalSize + ChatColor.GRAY + ")"));
				}
			});

			match.getSpectators().forEach(uuids -> {
				Player spectator = Bukkit.getPlayer(uuids);
				if (spectator != null) {
					int aliveSize = match.getFirstAlives().contains(uuid) ? match.getFirstAlives().size()-1 : match.getSecondAlives().size()-1;
					int totalSize = match.getFirst().contains(uuid) ? match.getSecond().size() : match.getFirst().size();
					spectator.sendMessage(ChatColor.WHITE + CoreUtils.getName(uuid) + ChatColor.GRAY + (killer == null ? " died." : " has been killed by " + ChatColor.WHITE + CoreUtils.getName(killer) + ChatColor.GRAY + " (" + ChatColor.GREEN + aliveSize + ChatColor.GRAY + "/" + ChatColor.RED + totalSize + ChatColor.GRAY + ")"));
				}
			});
			if (Bukkit.getPlayer(uuid) != null)
				Bukkit.getScheduler().runTaskLater(Practice.API, () -> addSpectateParty(uuid), 2L);
        }
    }
    
    private static void addSpectateParty(final UUID uuid) {
        final ManagerHandler managerHandler = Practice.API.getManagerHandler();
    	final Duel duel = Utils.getDuelByUUID(uuid);
    	if (duel.getDuelType().equals(DuelType.FFA)) {
    		duel.getAlives().forEach(alives -> {
    			Bukkit.getPlayer(alives).hidePlayer(Bukkit.getPlayer(uuid));
    			Utils.getProfiles(uuid).setProfileState(ProfileState.SPECTATE);
    		});
    	}
    	if (duel.getDuelType().equals(DuelType.SPLIT)) {
    		Arrays.asList(duel.getFirstAlives(), duel.getSecondAlives()).forEach(alivesArray -> alivesArray.forEach(alives -> {
                Bukkit.getPlayer(alives).hidePlayer(Bukkit.getPlayer(uuid));
                Utils.getProfiles(uuid).setProfileState(ProfileState.SPECTATE);
            }));
    	}
		final Profile profile = Utils.getProfiles(uuid);
		if (!duel.getSpectators().isEmpty()) {
			duel.getSpectators().forEach(spectator -> {
				if (profile.getSettings()[8] != 1) Bukkit.getPlayer(uuid).showPlayer(Bukkit.getPlayer(spectator));
				if (profile.getSettings()[8] != 0) Bukkit.getPlayer(uuid).hidePlayer(Bukkit.getPlayer(spectator));
			});
		}
		managerHandler.getItemManager().giveItems(uuid, false);
		if (profile.getSettings()[7] != 0) Bukkit.getPlayer(uuid).setFlySpeed(0.1f);
		if (profile.getSettings()[7] != 1) Bukkit.getPlayer(uuid).setFlySpeed(0.25f);
    }
    
	public static void addDrops(Item item, final UUID uuid) {
		Utils.getDuelByUUID(uuid).getDropped().add(item.getUniqueId());	
	}
	
	public static void removeDrops(Item item, final UUID uuid) {
		Utils.getDuelByUUID(uuid).getDropped().remove(item.getUniqueId());	
	}
	
	public static boolean containDrops(Item item, final UUID uuid) {
		return Utils.getDuelByUUID(uuid).getDropped().contains(item.getUniqueId());
	}
	
	public static void clearDrops(final UUID uuid) {
		if ((Utils.getDuelByUUID(uuid) != null && Utils.getDuelByUUID(uuid).getDropped().isEmpty())) return;
		final World world = Bukkit.getWorld("world");
		for (Entity entities : world.getEntities()) {
			if (entities == null || !(entities instanceof Item) && ((Utils.getDuelByUUID(uuid) != null && !Utils.getDuelByUUID(uuid).getDropped().contains(entities.getUniqueId())))) continue;
			entities.remove();
		}
	}

	public static void multiArena(final UUID uuid, final boolean display, boolean spectator) {
		Player targetPlayer = Bukkit.getPlayer(uuid);
		if (targetPlayer == null) return;

		if (!display) {
			final Duel duel = Utils.getDuelByUUID(uuid);

			Practice.API.getDuels().forEach(duels -> {
				boolean isSameDuel = duels == duel || (spectator && duels == Utils.getDuelBySpectator(uuid));

				duels.getFirst().forEach(first -> {
					Player firstPlayer = Bukkit.getPlayer(first);
					if (firstPlayer != null) {
						if (isSameDuel) {
							firstPlayer.showPlayer(targetPlayer);
							targetPlayer.showPlayer(firstPlayer);
						} else {
							firstPlayer.hidePlayer(targetPlayer);
							targetPlayer.hidePlayer(firstPlayer);
						}
					}
				});

				duels.getSecond().forEach(second -> {
					Player secondPlayer = Bukkit.getPlayer(second);
					if (secondPlayer != null) {
						if (isSameDuel) {
							secondPlayer.showPlayer(targetPlayer);
							targetPlayer.showPlayer(secondPlayer);
						} else {
							secondPlayer.hidePlayer(targetPlayer);
							targetPlayer.hidePlayer(secondPlayer);
						}
					}
				});

				if (spectator) {
					final Profile profile = Utils.getProfiles(uuid);
					final Player playerSender = Bukkit.getPlayer(uuid);
					if (!duels.getSpectators().isEmpty()) {
						duels.getSpectators().forEach(spectators -> {
							if (profile.getSettings()[8] != 1) playerSender.showPlayer(Bukkit.getPlayer(spectators));
							if (profile.getSettings()[8] != 0) playerSender.hidePlayer(Bukkit.getPlayer(spectators));
						});
					}
				}
			});
		} else {
			Bukkit.getOnlinePlayers().forEach(players -> {
				if (!players.canSee(targetPlayer)) {
					players.showPlayer(targetPlayer);
					targetPlayer.showPlayer(players);
				}
			});
		}
	}

	// TOURNAMENT //

	public static void handleTournamentDeath(Player killed, Player killer, TournamentEntry tournamentEntry) {
		int teamSize = tournamentEntry.getTeamSize();
		final TournamentManager tournamentManager = Practice.API.getManagerHandler().getTournamentManager();
		Duel duel = Utils.getDuelByUUID(killed.getUniqueId());

		MessageHelper.notifyUUID(tournamentEntry.getParticipants(), TournamentManager.prefix + (killer != null ? "&a" + killer.getName() : "&aEnvironment") + " &rhas eliminated &c" + killed.getName() + " &r!");
		tournamentManager.getActiveDuels().remove(duel);

		if (duel != null) {
			final Set<UUID> winner = duel.getFirst().contains(killed.getUniqueId()) ? duel.getSecond() : duel.getFirst();
			final Set<UUID> loser = duel.getFirst().contains(killed.getUniqueId()) ? duel.getFirst() : duel.getSecond();
			tournamentEntry.getParticipants().removeAll(loser);
			loser.forEach(id -> {
				if (Bukkit.getPlayer(id) != null) {
					Bukkit.getPlayer(id).sendMessage(ChatColor.RED + "Your team was eliminated from the tournament!");
				}
			});
			tournamentEntry.getOriginalTeams().removeIf(team -> team.containsAll(loser));
			TagUtils.clearEntries(Arrays.asList(winner, loser));

			if (duel.getTimer() != null) {
				duel.getTimer().cancel();
			}

			winner.forEach(uuid -> Practice.API.getManagerHandler().getInventoryManager().generatePreviewInventory(uuid, loser.stream().findFirst().orElse(null)));
			duel.setState(DuelState.FINISHING);
			duel.getWinner().addAll(winner);
			final List<UUID> players = Lists.newArrayList();
			players.addAll(loser);
			players.addAll(winner);
			System.out.println(players);
			if (!duel.getSpectators().isEmpty()) players.addAll(duel.getSpectators());
			if (tournamentEntry.getTeamSize() == 1) MessageUtils.sendSingleEndMessage(duel, players, winner.stream().toList().getFirst(), killed.getUniqueId());
			if (tournamentEntry.getTeamSize() != 1) MessageUtils.sendPartyComponent(Arrays.asList(winner, loser));
			Arrays.asList(winner, loser).forEach(uuids -> uuids.forEach(uuid -> {
				if (Bukkit.getPlayer(uuid) != null) {
					Core.API.getManagerHandler().getInventoryManager().generateProfileInventory(uuid, Practice.API.getKitNames());
				}
			}));
			duel.setWinner(Lists.newArrayList(tournamentEntry.getParticipants().getFirst()));
			Bukkit.getScheduler().runTaskLater(Practice.API, () -> {
				players.forEach(uuid -> {
					Utils.sendToSpawn(uuid, true);
					MatchUtils.clearDrops(uuid);
				});
				Practice.API.getDuels().remove(duel);
			}, 20L);
		}

		if (tournamentManager.getActiveDuels().isEmpty() && !tournamentEntry.isCountdownStarted()) {
			if (tournamentEntry.getParticipants().size() <= teamSize) {
				UUID winnerId = tournamentEntry.getParticipants().getFirst();
				Bukkit.broadcastMessage(" ");
				final String winnerName = CoreUtils.getName(winnerId);
				Bukkit.broadcastMessage(TournamentManager.prefix + ChatColor.RED + winnerName + ChatColor.WHITE + " won!");
				Bukkit.broadcastMessage(" ");
				tournamentManager.getTournaments().remove(tournamentEntry.getId());
				return;
			}

			tournamentEntry.getParticipants().forEach(participant -> {
				final Player player = Bukkit.getPlayer(participant);
				if (player != null) {
					player.sendMessage(ChatColor.GREEN + "Next round gonna start!");
				}
			});
			Bukkit.getScheduler().runTaskLater(Practice.API, () -> runTournamentRound(tournamentEntry), 120L);
		}
	}

	public static void runTournamentRound(TournamentEntry entry) {
		int teamSize = entry.getTeamSize();
		final TournamentManager tournamentManager = Practice.API.getManagerHandler().getTournamentManager();

		if (entry.getParticipants().size() <= teamSize) {
			UUID winnerId = entry.getParticipants().getFirst();
			Bukkit.broadcastMessage(" ");
			final String winnerName = CoreUtils.getName(winnerId);
			Bukkit.broadcastMessage(TournamentManager.prefix + ChatColor.RED + winnerName + ChatColor.WHITE + " won the tournament!");
			Bukkit.broadcastMessage(" ");
			tournamentManager.getTournaments().remove(entry.getId());
			return;
		}

		if (entry.getTeamSize() == 1 && entry.getParticipants().size() % 2 != 0) {
			UUID byePlayer = entry.getParticipants().get(entry.getParticipants().size() - 1);
			Player bye = Bukkit.getPlayer(byePlayer);
			if (bye != null) {
				bye.sendMessage(org.bukkit.ChatColor.YELLOW + "You have been qualified for the next round due to an odd number of players.");
			}
			entry.getByePlayer().add(byePlayer);
			entry.getParticipants().remove(byePlayer);
		}

		List<List<UUID>> originalTeams = entry.getOriginalTeams();
		Collections.shuffle(originalTeams);

		for (int i = 0; i < originalTeams.size(); i += 2) {
			List<UUID> team1 = originalTeams.get(i);
			List<UUID> team2 = (i + 1 < originalTeams.size()) ? originalTeams.get(i + 1) : null;

			if (team2 == null) {
				entry.getByePlayer().addAll(team1);
				continue;
			}

			createTournamentMatch(entry, team1, team2);
		}

		entry.getParticipants().addAll(entry.getByePlayer());
		entry.getByePlayer().clear();
		MessageUtils.determineTournamentStage(entry);
	}

	public static void createTournamentMatch(TournamentEntry entry, List<UUID> team1, List<UUID> team2) {
		int teamSize = entry.getTeamSize();
		final TournamentManager tournamentManager = Practice.API.getManagerHandler().getTournamentManager();
		Duel duel = new Duel(Practice.API, new HashSet<>(team1), new HashSet<>(team2), false, entry.getKit(), teamSize > 1 ? DuelType.SPLIT : DuelType.SINGLE, null);
		tournamentManager.getActiveDuels().add(duel);
	}

	// QUEUE

	public static void researchMatch(final UUID uuid, final Kit kit, final boolean ranked) {
		for (Map.Entry<UUID, QueueManager.QueueEntry> entry : Practice.API.getQueue().entrySet()) {
			QueueManager.QueueEntry queueEntry = entry.getValue();
			if (queueEntry.getKit().equals(kit) && queueEntry.isRanked() == ranked && !entry.getKey().equals(uuid)) {
				if (queueEntry.isRanked()) {
					if (!Practice.API.getManagerHandler().getQueueManager().checkCompatibilityElo(entry.getKey(), uuid)) {
						return;
					}
					queueEntry.getEloRangeRunnable().cancel();
				}
				new Duel(Practice.API, Sets.newHashSet(entry.getKey()), Sets.newHashSet(uuid), queueEntry.isRanked(), queueEntry.getKit(), DuelType.SINGLE, null);
				Practice.API.getQueue().remove(entry.getKey());
				if (ranked) Practice.API.getQueue().remove(uuid);
				Practice.API.getManagerHandler().getInventoryManager().refreshQueueInventory(ranked, kit);
				return;
			}
		}
	}

	public static boolean isLocationInZone(Location location, Location corner1, Location corner2) {
		double minX = Math.min(corner1.getX(), corner2.getX());
		double maxX = Math.max(corner1.getX(), corner2.getX());
		double minY = Math.min(corner1.getY(), corner2.getY());
		double maxY = Math.max(corner1.getY(), corner2.getY());
		double minZ = Math.min(corner1.getZ(), corner2.getZ());
		double maxZ = Math.max(corner1.getZ(), corner2.getZ());

		return location.getX() >= minX && location.getX() <= maxX &&
				location.getY() >= minY && location.getY() <= maxY &&
				location.getZ() >= minZ && location.getZ() <= maxZ;
	}

	public static boolean isNearSpawnLocation(Location blockLocation, Location spawnLocation, int radius) {
		double distance = blockLocation.distance(spawnLocation);
		return distance <= radius;
	}

	public static Location findSafeLocationAbove(Location location) {
		Block block = location.getBlock();
		if (!block.getType().isSolid()) {
			return location;
		}
		for (int y = 1; y <= 25; y++) {
			Block blockAbove = location.getBlock().getRelative(0, y, 0);
			if (!blockAbove.getType().isSolid()) {
				return new Location(location.getWorld(), location.getX(), blockAbove.getY(), location.getZ(), location.getYaw(), location.getPitch());
			}
		}
		return location;
	}

	// BEDFIGHT

	public static void deathBedfight(final Player player, final Duel duel) {
		final UUID oppsID = duel.getFirst().contains(player.getUniqueId()) ? duel.getSecond().stream().toList().getFirst() : duel.getFirst().stream().toList().getFirst();
		final Location locationFirst = Utils.getArenaCopyByPlayer(player.getUniqueId()) != null ? Utils.getArenaCopyByPlayer(player.getUniqueId()).getPosition().getFirst().toBukkitLocation() : duel.getArena().getPosition().getFirst().toBukkitLocation();
		final Location locationSecond = Utils.getArenaCopyByPlayer(player.getUniqueId()) != null ? Utils.getArenaCopyByPlayer(player.getUniqueId()).getPosition().getLast().toBukkitLocation() : duel.getArena().getPosition().getLast().toBukkitLocation();
		final DuelStatistics statistics = Practice.API.getManagerHandler().getProfileManager().getDuelStatistics().get(player.getUniqueId());
		if (!statistics.isBedBroke()) {
			Location spawnLocation = duel.getFirst().contains(player.getUniqueId()) ? locationFirst : locationSecond;
			spawnLocation = MatchUtils.findSafeLocationAbove(spawnLocation);
			player.setGameMode(GameMode.ADVENTURE);
			player.setAllowFlight(true);
			player.setFlying(true);
			Bukkit.getPlayer(oppsID).hidePlayer(player);
			player.getInventory().clear();
			Location finalSpawnLocation = spawnLocation;
			Bukkit.getScheduler().runTaskLater(Practice.API, () -> {
				Practice.API.getManagerHandler().getItemManager().giveItems(player.getUniqueId(), false);
				player.setAllowFlight(false);
				player.setFlying(false);
				player.setHealth(player.getMaxHealth());
				player.setGameMode(GameMode.SURVIVAL);
				player.teleport(finalSpawnLocation);
				Bukkit.getPlayer(oppsID).showPlayer(player);
			}, 100L);
		}
	}
}

package kezukdev.akyto.utils.match;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;

import akyto.core.profile.Profile;
import akyto.core.profile.ProfileState;
import akyto.core.utils.CoreUtils;
import kezukdev.akyto.Practice;
import kezukdev.akyto.duel.Duel;
import kezukdev.akyto.duel.Duel.DuelType;
import kezukdev.akyto.handler.ManagerHandler;
import kezukdev.akyto.utils.Utils;
import net.md_5.bungee.api.ChatColor;

public class MatchUtils {
	
    public static void addKill(final UUID uuid, final UUID killer, boolean disconnect) {
        final Duel match = Utils.getDuelByUUID(uuid);
        final ManagerHandler managerHandler = Practice.getAPI().getManagerHandler();
        if (match == null) return;
		if (disconnect) {
			match.getDisconnected().add(uuid);
		}
		managerHandler.getInventoryManager().generatePreviewInventory(uuid, killer);
        if (match.getDuelType().equals(DuelType.FFA)) {
        	match.getAlives().remove(uuid);
            if (match.getAlives().size() == 1) {
				managerHandler.getDuelManager().endMultiple(match.getAlives().stream().toList().getFirst());
                return;
            }

            List<UUID> allPlayers = Stream.of(match.getFirst(), match.getSecond())
                    .flatMap(Collection::stream)
                    .collect(Collectors.toList());

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
				Bukkit.getScheduler().runTaskLater(Practice.getAPI(), () -> {
					addSpectateParty(uuid);
				}, 2L);
            }
        }

        if (match.getDuelType().equals(DuelType.SPLIT)) {
			match.getFirstAlives().remove(uuid);
			match.getSecondAlives().remove(uuid);
            if (match.getFirstAlives().isEmpty() || match.getSecondAlives().isEmpty()) {
                managerHandler.getDuelManager().endMultiple(killer == null ? (match.getFirst().contains(uuid) ? match.getSecondAlives().stream().toList().getFirst() : match.getFirstAlives().stream().toList().getFirst()) : killer);
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
				Bukkit.getScheduler().runTaskLater(Practice.getAPI(), () -> {
					addSpectateParty(uuid);
				}, 2L);
        }
    }
    
    private static void addSpectateParty(final UUID uuid) {
        final ManagerHandler managerHandler = Practice.getAPI().getManagerHandler();
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

			Practice.getAPI().getDuels().forEach(duels -> {
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
					if (profile == null) return;
					final Player playerSender = targetPlayer;

					duels.getSpectators().forEach(spectators -> {
						Player spectatorPlayer = Bukkit.getPlayer(spectators);
						if (spectatorPlayer != null) {
							if (isSameDuel) {
								if (profile.getSettings()[8] != 1) {
									playerSender.showPlayer(spectatorPlayer);
								} else if (profile.getSettings()[8] != 0) {
									playerSender.hidePlayer(spectatorPlayer);
								}
							} else {
								playerSender.hidePlayer(spectatorPlayer);
							}
						}
					});
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

}

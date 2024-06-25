package kezukdev.akyto.utils.match;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;

import gym.core.profile.Profile;
import gym.core.profile.ProfileState;
import gym.core.utils.CoreUtils;
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
        if (match.getDuelType().equals(DuelType.FFA)) {
        	match.getAlives().remove(uuid);
            if (match.getAlives().size() == 1) {
            	if (killer != null) {
            		managerHandler.getInventoryManager().generatePreviewInventory(killer, uuid);
            	}
                managerHandler.getDuelManager().endMultiple(killer);
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
            if (disconnect) {
                match.getFirst().remove(uuid);
                match.getSecond().remove(uuid);
            	return;
            }
            if (Bukkit.getPlayer(uuid) != null) {
                addSpectateParty(uuid);
            }
        }

        if (match.getDuelType().equals(DuelType.SPLIT)) {
            match.getFirstAlives().remove(uuid);
            match.getSecondAlives().remove(uuid);
            if (killer != null) {
            	managerHandler.getInventoryManager().generatePreviewInventory(killer, uuid);
            }
            if (match.getFirstAlives().isEmpty() || match.getSecondAlives().isEmpty()) {
                managerHandler.getDuelManager().endMultiple(killer);
                return;
            }

			List<UUID> allPlayers = Stream.of(match.getFirst(), match.getSecond())
					.flatMap(Collection::stream)
					.collect(Collectors.toList());

			allPlayers.forEach(uuids -> {
				Player player = Bukkit.getPlayer(uuids);
				if (player != null) {
					int aliveSize = match.getFirstAlives().contains(uuid) ? match.getFirstAlives().size() : match.getSecondAlives().size();
					int totalSize = match.getFirst().contains(uuid) ? match.getSecond().size() : match.getFirst().size();
					player.sendMessage(ChatColor.WHITE + CoreUtils.getName(uuid) + ChatColor.GRAY + (killer == null ? " died." : " has been killed by " + ChatColor.WHITE + CoreUtils.getName(killer) +ChatColor.GRAY + " (" + ChatColor.GREEN + aliveSize + ChatColor.GRAY + "/" + ChatColor.RED + totalSize + ChatColor.GRAY + ")"));
				}
			});

			match.getSpectators().forEach(uuids -> {
				Player spectator = Bukkit.getPlayer(uuids);
				if (spectator != null) {
					int aliveSize = match.getFirstAlives().contains(uuid) ? match.getFirstAlives().size() : match.getSecondAlives().size();
					int totalSize = match.getFirst().contains(uuid) ? match.getSecond().size() : match.getFirst().size();
					spectator.sendMessage(ChatColor.WHITE + CoreUtils.getName(uuid) + ChatColor.GRAY + (killer == null ? " died." : " has been killed by " + ChatColor.WHITE + CoreUtils.getName(killer) +ChatColor.GRAY + " (" + ChatColor.GREEN + aliveSize + ChatColor.GRAY + "/" + ChatColor.RED + totalSize + ChatColor.GRAY + ")"));
				}
			});
            if (disconnect) {
                match.getFirst().remove(uuid);
                match.getSecond().remove(uuid);
            	return;
            }
			if (Bukkit.getPlayer(uuid) != null)
				addSpectateParty(uuid);
        }
    }
    
    private static void addSpectateParty(final UUID uuid) {
        final ManagerHandler managerHandler = Practice.getAPI().getManagerHandler();
    	final Duel duel = Utils.getDuelByUUID(uuid);
    	if (duel.getDuelType().equals(DuelType.FFA)) {
    		duel.getAlives().forEach(alives -> {
    			Bukkit.getPlayer(alives).hidePlayer(Bukkit.getPlayer(uuid));
    			Utils.getProfiles(uuid).setProfileState(ProfileState.SPECTATE);
    			managerHandler.getItemManager().giveItems(uuid, false);
    		});
    	}
    	if (duel.getDuelType().equals(DuelType.SPLIT)) {
    		Arrays.asList(duel.getFirstAlives(), duel.getSecondAlives()).forEach(alivesArray -> alivesArray.forEach(alives -> {
                Bukkit.getPlayer(alives).hidePlayer(Bukkit.getPlayer(uuid));
                Utils.getProfiles(uuid).setProfileState(ProfileState.SPECTATE);
                managerHandler.getItemManager().giveItems(uuid, false);
            }));
    	}
		final Profile profile = Utils.getProfiles(uuid);
		if (!duel.getSpectators().isEmpty()) {
			duel.getSpectators().forEach(spectator -> {
				if (profile.getSpectateSettings().get(0)) Bukkit.getPlayer(uuid).showPlayer(Bukkit.getPlayer(spectator));
				if (!profile.getSpectateSettings().get(0)) Bukkit.getPlayer(uuid).hidePlayer(Bukkit.getPlayer(spectator));
			});
		}
		if (profile.getSpectateSettings().get(1)) Bukkit.getPlayer(uuid).setFlySpeed(0.1f);
		if (!profile.getSpectateSettings().get(1)) Bukkit.getPlayer(uuid).setFlySpeed(0.25f);
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
	    if (!display) {
	    	final Duel duel = Utils.getDuelByUUID(uuid);
	    	Practice.getAPI().getDuels().forEach(duels -> {
	    		if (!spectator && duels != duel) return;
	    		if (spectator && duels != Utils.getDuelBySpectator(uuid)) return;
    	    	duels.getFirst().forEach(first -> {
    	    		if (Bukkit.getPlayer(first) == null) return;
    	    		Bukkit.getPlayer(first).hidePlayer(Bukkit.getPlayer(uuid));
    	    		Bukkit.getPlayer(uuid).hidePlayer(Bukkit.getPlayer(first));
    	    	});
    	    	duels.getSecond().forEach(second -> {
    	    		if (Bukkit.getPlayer(second) == null) return;
    	    		Bukkit.getPlayer(second).hidePlayer(Bukkit.getPlayer(uuid));
    	    		Bukkit.getPlayer(uuid).hidePlayer(Bukkit.getPlayer(second));
    	    	});
    	    	if (spectator) {
    				final Profile profile = Utils.getProfiles(uuid);
    				final Player playerSender = Bukkit.getPlayer(uuid);
    		    	if (!duels.getSpectators().isEmpty()) {
    		            duels.getSpectators().forEach(spectators -> {
    		                if (profile.getSpectateSettings().get(0)) playerSender.showPlayer(Bukkit.getPlayer(spectators));
    		                if (!profile.getSpectateSettings().get(0)) playerSender.hidePlayer(Bukkit.getPlayer(spectators));
    		            });	
    		    	}
    	    	}
	    	});
        }
	    else {
	    	Bukkit.getOnlinePlayers().forEach(players -> {
	    		if (!players.canSee(Bukkit.getPlayer(uuid))) {
	    			players.showPlayer(Bukkit.getPlayer(uuid));
	    			Bukkit.getPlayer(uuid).showPlayer(players);
	    		}
	    	});
	    }
	}
}

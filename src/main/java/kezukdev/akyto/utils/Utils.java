package kezukdev.akyto.utils;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import kezukdev.akyto.arena.Arena;
import kezukdev.akyto.arena.ArenaType;

import org.bukkit.*;
import org.bukkit.entity.Player;
import kezukdev.akyto.Practice;
import kezukdev.akyto.duel.Duel;
import kezukdev.akyto.handler.manager.PartyManager.PartyEntry;
import kezukdev.akyto.kit.Kit;
import kezukdev.akyto.profile.Profile;
import kezukdev.akyto.profile.ProfileState;
import kezukdev.akyto.request.Request;

public class Utils {

	public static UUID getUUID(String playerName) {
		Player target = Practice.getAPI().getServer().getPlayer(playerName);
		if (target != null)
			return target.getUniqueId();
		return Practice.getAPI().getServer().getOfflinePlayer(playerName).getUniqueId();
	}

	public static String getName(UUID playerId) {
		Player target = Practice.getAPI().getServer().getPlayer(playerId);
		if (target != null)
			return target.getName();
		return Practice.getAPI().getServer().getOfflinePlayer(playerId).getName();
	}

	public static List<UUID> getOpponents(UUID uuid) {
        Duel duel = getDuelByUUID(uuid);
        return duel.getFirst().contains(uuid) ? new ArrayList<>(duel.getSecond()) : new ArrayList<>(duel.getFirst());
	}

	public static Duel getDuelByUUID(UUID uuid) {
	    return Practice.getAPI().getDuels().stream().filter(duel -> duel.getFirst().contains(uuid) || duel.getSecond().contains(uuid)).findFirst().orElse(null);
	}

	public static Arena getArenaByIcon(Material icon, ArenaType arenaType) {
		return Practice.getAPI().getArenas().stream().filter(arena -> arena.getIcon().equals(icon) && arena.getArenaType().equals(arenaType)).findFirst().orElse(null);
	}
	
	public static Request getRequestByUUID(UUID uuid) {
	    return Practice.getAPI().getManagerHandler().getRequestManager().getRequest().get(uuid);
	}
	
	public static Duel getDuelBySpectator(UUID uuid) {
	    return Practice.getAPI().getDuels().stream().filter(duel -> duel.getSpectators().contains(uuid)).findFirst().orElse(null);
	}

	public static Profile getProfiles(final UUID uuid) {
		return Practice.getAPI().getManagerHandler().getProfileManager().getProfiles().get(uuid);
	}
	
	public static PartyEntry getPartyByUUID(UUID uuid) {
		return Practice.getAPI().getManagerHandler().getPartyManager().getParties().stream().filter(party -> party.getMembers().contains(uuid)).findFirst().orElse(null);
	}
	
	public static void sendToSpawn(final UUID uuid, final boolean teleport) {
		if (Bukkit.getPlayer(uuid) == null) return;
		Bukkit.getOnlinePlayers().forEach(player -> {
			player.showPlayer(Bukkit.getPlayer(uuid));
			Bukkit.getPlayer(uuid).showPlayer(player);
		});
		resetPlayer(uuid);
		getProfiles(uuid).setProfileState(ProfileState.FREE);
		Practice.getAPI().getManagerHandler().getItemManager().giveItems(uuid, false);
		if (teleport) {
			Bukkit.getPlayer(uuid).teleport(Practice.getAPI().getSpawn().getLocation());
		}
	}
	
	public static void sendToEditor(final UUID uuid, final Kit kit) {
		if (Bukkit.getPlayer(uuid) == null) return;
		Bukkit.getOnlinePlayers().forEach(player -> {
			player.hidePlayer(Bukkit.getPlayer(uuid));
			Bukkit.getPlayer(uuid).hidePlayer(player);
		});
		resetPlayer(uuid);
		getProfiles(uuid).setProfileState(ProfileState.EDITOR);
		Practice.getAPI().getManagerHandler().getProfileManager().getEditing().put(uuid, kit.name());
		Practice.getAPI().getManagerHandler().getItemManager().giveItems(uuid, false);
		Bukkit.getPlayer(uuid).teleport(Practice.getAPI().getEditor().getLocation());
	}
	
	public static void resetPlayer(final UUID uuid) {
		final Player player = Bukkit.getPlayer(uuid);
		if (player == null) return;
		player.setAllowFlight(false);
		player.setFlying(false);
		player.setFireTicks(0);
		player.setExhaustion(0.3f);
		player.setFoodLevel(20);
		player.setHealth(player.getMaxHealth());
		player.setLevel(0);
		player.setExp(0);
		player.setWalkSpeed(0.2f);
		player.setSneaking(false);
		player.setSaturation(20.0f);
		player.resetPlayerWeather();
		player.closeInventory();
		player.setGameMode(GameMode.SURVIVAL);
		player.getActivePotionEffects().forEach(potionEffect -> player.removePotionEffect(potionEffect.getType()));
	}
}

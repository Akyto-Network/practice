package akyto.practice.utils;

import java.util.*;

import akyto.core.Core;
import akyto.core.profile.Profile;
import akyto.core.profile.ProfileState;
import akyto.practice.arena.Arena;
import akyto.practice.arena.ArenaType;

import akyto.practice.utils.match.MatchUtils;
import org.bukkit.*;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;

import akyto.practice.Practice;
import akyto.practice.duel.Duel;
import akyto.practice.handler.manager.PartyManager.PartyEntry;
import akyto.practice.kit.Kit;
import akyto.practice.request.Request;
import org.bukkit.inventory.ItemStack;

public class Utils {

	public static List<UUID> getOpponents(UUID uuid) {
        Duel duel = getDuelByUUID(uuid);
        return duel.getFirst().contains(uuid) ? new ArrayList<>(duel.getSecond()) : new ArrayList<>(duel.getFirst());
	}

	public static List<UUID> getAllies(UUID uuid) {
		Duel duel = getDuelByUUID(uuid);
		return duel.getFirst().contains(uuid) ? new ArrayList<>(duel.getFirst()) : new ArrayList<>(duel.getSecond());
	}

	public static Duel getDuelByUUID(UUID uuid) {
	    return Practice.getAPI().getDuels().stream().filter(duel -> duel.getFirst().contains(uuid) || duel.getSecond().contains(uuid)).findFirst().orElse(null);
	}

	public static Arena getArenaByIcon(Material icon, ArenaType arenaType) {
		return Practice.getAPI().getArenas().values().stream().filter(arena -> arena.getIcon().equals(icon) && arena.getArenaType().equals(arenaType)).findFirst().orElse(null);
	}
	
	public static Request getRequestByUUID(UUID uuid) {
	    return Practice.getAPI().getManagerHandler().getRequestManager().getRequest().get(uuid);
	}
	
	public static Duel getDuelBySpectator(UUID uuid) {
	    return Practice.getAPI().getDuels().stream().filter(duel -> duel.getSpectators().contains(uuid)).findFirst().orElse(null);
	}

	public static Profile getProfiles(final UUID uuid) {
		return Core.API.getManagerHandler().getProfileManager().getProfiles().get(uuid);
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
		if (!Core.API.getManagerHandler().getProfileManager().getProfiles().get(uuid).isInState(ProfileState.MOD)) {
			Core.API.getManagerHandler().getProfileManager().getProfiles().get(uuid).setProfileState(ProfileState.FREE);	
		}
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

	public static void drops(final UUID player, final List<ItemStack> drop, final Location deathLoc) {
		final Profile profile = Utils.getProfiles(player);
		if (profile.getSettings()[5] == 1) {
			for (ItemStack item : drop) {
				if (item.getType().toString().contains("DIAMOND_") ||item.getType().toString().contains("IRON_")) {
					final Item items = Bukkit.getPlayer(player).getWorld().dropItemNaturally(deathLoc, new ItemStack(item.clone()), Bukkit.getPlayer(player), true);
					MatchUtils.addDrops(items, player);
				}
			}
		}
		if (profile.getSettings()[5] == 2) {
			for (ItemStack item : drop) {
				final Item items = Bukkit.getPlayer(player).getWorld().dropItemNaturally(deathLoc, new ItemStack(item.clone()), Bukkit.getPlayer(player), true);
				MatchUtils.addDrops(items, player);
			}
		}
	}
}

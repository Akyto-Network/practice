package akyto.practice.handler.listener;

import akyto.core.Core;
import akyto.practice.event.EventState;
import akyto.core.utils.CoreUtils;
import akyto.practice.duel.Duel;
import akyto.practice.handler.manager.EventManager;
import akyto.spigot.aSpigot;
import akyto.spigot.handler.MovementHandler;
import net.minecraft.server.v1_8_R3.PacketPlayInFlying;
import org.bukkit.*;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.player.PlayerPickupItemEvent;

import akyto.core.profile.Profile;
import akyto.core.profile.ProfileState;
import akyto.practice.Practice;
import akyto.practice.duel.cache.DuelState;
import akyto.practice.duel.cache.DuelStatistics;
import akyto.practice.utils.Utils;
import org.bukkit.inventory.ItemStack;

public class EntityListener implements Listener {

	private final Practice main;


	public EntityListener(final Practice main) {
		this.main = main;
		aSpigot.INSTANCE.addMovementHandler(new MovementHandler() {
			@Override
			public void handleUpdateRotation(Player player, Location to, Location from, PacketPlayInFlying packet) {
			}

			@Override
			public void handleUpdateLocation(Player player, Location to, Location from, PacketPlayInFlying packet) {
				// Sumo Event
				final Profile profile = Core.API.getManagerHandler().getProfileManager().getProfile(player.getUniqueId());
				if (profile.isInState(ProfileState.EVENT)) {
					final EventManager eventManager = main.getManagerHandler().getEventManager();
					if (eventManager.getType().equalsIgnoreCase("sumo")) {
						if (eventManager.getPlayersSelected().contains(player.getUniqueId()) && eventManager.getEventState().equals(EventState.STARTING)) {
							player.teleport(to);
						}
					}
				}
				// Sumo Duel
				final Duel duel = Utils.getDuelByUUID(player.getUniqueId());
				if (duel != null) {
					final String kitName = duel.getKit().name();
					if (kitName.equalsIgnoreCase("sumo") && duel.getState().equals(DuelState.STARTING)) {
						player.teleport(to);
					}
				}
			}
		});
	}

	@EventHandler
	public void onDamage(final EntityDamageEvent event) {
		if (!(event.getEntity() instanceof Player))
			return;

		final Profile profile = Utils.getProfiles(event.getEntity().getUniqueId());

		if (profile != null) {
			if (profile.isInState(ProfileState.FREE, ProfileState.QUEUE) && event.getCause().equals(DamageCause.VOID)) {
				event.getEntity().teleport(this.main.getSpawn().getLocation() == null ? event.getEntity().getWorld().getSpawnLocation() : this.main.getSpawn().getLocation());
			}

			if (profile.isInState(ProfileState.FIGHT)) {

				final Duel playerDuel = Utils.getDuelByUUID(event.getEntity().getUniqueId());

				if (playerDuel != null && playerDuel.getState().equals(DuelState.PLAYING)) {
					if (playerDuel.getKit().name().equals("sumo")) {
						event.setDamage(0.0f);
					}
					return;
				}
			}

			if (profile.isInState(ProfileState.EVENT)) {
				final EventManager eventManager = main.getManagerHandler().getEventManager();
				if (eventManager.getType().equalsIgnoreCase("sumo")) {
					if (eventManager.getEventState().equals(EventState.FIGHT) && eventManager.getPlayersSelected().contains(event.getEntity().getUniqueId())) {
						event.setDamage(0.0f);
						return;
					}
				}
				if (eventManager.getType().equalsIgnoreCase("oitc")) {
					if (eventManager.getEventState().equals(EventState.FIGHT)){
						return;
					}
				}
			}
		}
		event.getEntity().setFireTicks(0);
		event.setCancelled(true);
	}

	@EventHandler
	public void onDamageByEntity(final EntityDamageByEntityEvent event) {
		if (event.getEntity() instanceof Player && event.getDamager() instanceof Player) {
			final Profile victimProfile = Utils.getProfiles(event.getEntity().getUniqueId());
			final Profile profileDamager = Utils.getProfiles(event.getDamager().getUniqueId());
			if (victimProfile.isInState(ProfileState.EVENT) && profileDamager.isInState(ProfileState.EVENT)) {
				final EventManager eventManager = main.getManagerHandler().getEventManager();
				if (eventManager.getType().equalsIgnoreCase("sumo")) {
					if (eventManager.getEventState().equals(EventState.FIGHT)) {
						if (eventManager.getPlayersSelected().contains(event.getDamager().getUniqueId()) || eventManager.getPlayersSelected().contains(event.getEntity().getUniqueId())){
							event.setDamage(0.0f);
							return;
						}
					}
				}
				if (eventManager.getType().equalsIgnoreCase("oitc")) {
					event.setDamage(2.5f);
					return;
				}
			}
			if (victimProfile.isInState(ProfileState.FIGHT) && profileDamager.isInState(ProfileState.FIGHT)) {
				if (!profileDamager.isAllowClick()) {
					event.setCancelled(true);
					((Player) event.getDamager()).getPlayer().sendMessage(CoreUtils.translate(Core.API.getLoaderHandler().getMessage().getClickCancel()));
					return;
				}
				final Duel duel = Utils.getDuelByUUID(event.getEntity().getUniqueId());
				final DuelStatistics statisticsDamager = this.main.getManagerHandler().getProfileManager().getDuelStatistics().get(event.getDamager().getUniqueId());
				final DuelStatistics statisticsVictim = this.main.getManagerHandler().getProfileManager().getDuelStatistics().get(event.getEntity().getUniqueId());
				if (duel.getState().equals(DuelState.PLAYING)) {
					if (duel.getFirstAlives() != null) {
						if (duel.getFirstAlives().contains(event.getEntity().getUniqueId()) && duel.getFirstAlives().contains(event.getDamager().getUniqueId()) ||
								duel.getSecondAlives().contains(event.getEntity().getUniqueId()) && duel.getSecondAlives().contains(event.getDamager().getUniqueId())) {
							event.setCancelled(true);
							return;
						}
					}
					if (event.getCause().equals(DamageCause.ENTITY_ATTACK)) {
						statisticsDamager.setHits(statisticsDamager.getHits()+1);
						statisticsDamager.setCombo(statisticsDamager.getCombo()+1);
						statisticsVictim.setCombo(0);
						if (statisticsDamager.getCombo() > statisticsDamager.getLongestHit()) {
							statisticsDamager.setLongestHit(statisticsDamager.getCombo());
						}
					}
					if (duel.getKit().name().equals("sumo")) {
						event.setDamage(0.0f);
					}
					return;
				}
			}
			event.setCancelled(true);
		}
		else if (event.getDamager() instanceof Arrow) {
			Arrow arrow = (Arrow) event.getDamager();
			Player shooter = (Player) arrow.getShooter();
			if (shooter != null) {
				final Profile victimProfile = Utils.getProfiles(event.getEntity().getUniqueId());
				final Profile profileDamager = Utils.getProfiles(shooter.getUniqueId());
				if (victimProfile.isInState(ProfileState.EVENT) && profileDamager.isInState(ProfileState.EVENT)) {
					final EventManager eventManager = main.getManagerHandler().getEventManager();
					if (eventManager.getType().equalsIgnoreCase("oitc")) {
						if (eventManager.getEventState().equals(EventState.FIGHT)) {
							event.setDamage(200.0f);
							victimProfile.setDeath(victimProfile.getDeath() + 1);
							profileDamager.setKill(profileDamager.getKill() + 1);
							eventManager.getMembers().forEach(member -> {
								if (Bukkit.getPlayer(member) != null) {
									final Player player = Bukkit.getPlayer(member);
									player.sendMessage(ChatColor.GRAY + "[" + ChatColor.DARK_RED + "OITC" + ChatColor.GRAY + "] " + ChatColor.GREEN + shooter.getName() + ChatColor.GRAY + " shot " + ChatColor.RED + event.getEntity().getName());
								}
							});
							if (Bukkit.getPlayer(shooter.getUniqueId()) != null) {
								final Player player = Bukkit.getPlayer(shooter.getUniqueId());
								player.getInventory().addItem(new ItemStack(Material.ARROW));
								player.updateInventory();
							}
							if (profileDamager.getKill() >= 5) {
								eventManager.endMatch(shooter.getUniqueId(), event.getEntity().getUniqueId());
							}
							return;
						}
					}
				}
			}
		}
	}

	@EventHandler
	public void onReceiveDroppedItems(PlayerPickupItemEvent event) {
		if (Utils.getProfiles(event.getPlayer().getUniqueId()).isInState(ProfileState.FIGHT)) {
			if (Utils.getDuelByUUID(event.getPlayer().getUniqueId()).getState().equals(DuelState.FINISHING)) {
				event.setCancelled(true);
				return;
			}
			return;
		}
		if (event.getPlayer().getGameMode() == GameMode.SURVIVAL)
			event.setCancelled(true);
	}

}

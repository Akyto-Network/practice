package akyto.practice.handler.listener;

import akyto.core.Core;
import akyto.core.profile.Profile;
import akyto.core.profile.ProfileState;
import akyto.core.utils.CoreUtils;
import akyto.core.utils.format.FormatUtils;
import akyto.practice.Practice;
import akyto.practice.duel.Duel;
import akyto.practice.duel.cache.DuelState;
import akyto.practice.duel.cache.DuelStatistics;
import akyto.practice.event.EventState;
import akyto.practice.handler.manager.EventManager;
import akyto.practice.handler.manager.ProfileManager;
import akyto.practice.utils.Utils;
import akyto.practice.utils.chat.MessageHelper;
import akyto.practice.utils.match.MatchUtils;
import akyto.spigot.aSpigot;
import akyto.spigot.handler.MovementHandler;
import com.google.common.collect.Lists;
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
import org.bukkit.inventory.ItemStack;

import java.util.List;
import java.util.UUID;

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
							player.teleport(eventManager.getPlayersSelected().getFirst().equals(player.getUniqueId()) ? main.getSumoFirst().getLocation() : main.getSumoSecond().getLocation());
						}
					}
				}
				// Duel
				final Duel duel = Utils.getDuelByUUID(player.getUniqueId());
				if (duel != null) {
					final String kitName = duel.getKit().name();
					if ((kitName.equalsIgnoreCase("sumo")) && duel.getState().equals(DuelState.STARTING)) {
						player.teleport(duel.getFirst().contains(player.getUniqueId()) ? duel.getArena().getPosition().getFirst().toBukkitLocation() : duel.getArena().getPosition().getLast().toBukkitLocation());
					}
					if (kitName.equalsIgnoreCase("bedfight") && duel.getState().equals(DuelState.PLAYING)) {
						Location corner1 = Utils.getArenaCopyByPlayer(player.getUniqueId()) != null ? Utils.getArenaCopyByPlayer(player.getUniqueId()).getCorner1().toBukkitLocation() : duel.getArena().getCorner1().toBukkitLocation();
						Location corner2 = Utils.getArenaCopyByPlayer(player.getUniqueId()) != null ? Utils.getArenaCopyByPlayer(player.getUniqueId()).getCorner2().toBukkitLocation() : duel.getArena().getCorner2().toBukkitLocation();
						if (!MatchUtils.isLocationInZone(from, corner1, corner2) && !player.getGameMode().equals(GameMode.ADVENTURE)) {
							final UUID oppsID = duel.getFirst().contains(player.getUniqueId()) ? duel.getSecond().stream().toList().getFirst() : duel.getFirst().stream().toList().getFirst();
							MatchUtils.deathBedfight(player, duel);
							final DuelStatistics statistics = main.getManagerHandler().getProfileManager().getDuelStatistics().get(player.getUniqueId());
							final net.md_5.bungee.api.ChatColor colorKilled = duel.getFirst().contains(player.getUniqueId()) ? net.md_5.bungee.api.ChatColor.BLUE : net.md_5.bungee.api.ChatColor.RED;
							final List<UUID> duelPlayers = Lists.newArrayList();
							duelPlayers.addAll(duel.getFirst());
							duelPlayers.addAll(duel.getSecond());
							duelPlayers.addAll(duel.getSpectators());
							MessageHelper.notifyUUID(duelPlayers.stream().toList(), net.md_5.bungee.api.ChatColor.GRAY + "[" + net.md_5.bungee.api.ChatColor.DARK_RED + "Match" + net.md_5.bungee.api.ChatColor.GRAY + "] " + colorKilled + player.getName() + ChatColor.WHITE + " fall into the void.");
							if (statistics.isBedBroke()) {
								if (duel.getDuelType().equals(Duel.DuelType.SINGLE)) {
									main.getManagerHandler().getDuelManager().endSingle(oppsID);
								}
								else {
									main.getManagerHandler().getDuelManager().endMultiple(oppsID);
								}
							}
							duelPlayers.clear();
						}
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
				if (Core.API.getManagerHandler().getProfileManager().getFrozen().contains(event.getEntity().getUniqueId())) {
					event.setCancelled(true);
					return;
				}
				final Duel playerDuel = Utils.getDuelByUUID(event.getEntity().getUniqueId());

				if (playerDuel != null && playerDuel.getState().equals(DuelState.PLAYING)) {
					if (playerDuel.getKit().name().equals("bedfight") && event.getCause().equals(DamageCause.VOID)) {
						final Location locationFirst = Utils.getArenaCopyByPlayer(event.getEntity().getUniqueId()) != null ? Utils.getArenaCopyByPlayer(event.getEntity().getUniqueId()).getPosition().getFirst().toBukkitLocation() : playerDuel.getArena().getPosition().getFirst().toBukkitLocation();
						final Location locationSecond = Utils.getArenaCopyByPlayer(event.getEntity().getUniqueId()) != null ? Utils.getArenaCopyByPlayer(event.getEntity().getUniqueId()).getPosition().getLast().toBukkitLocation() : playerDuel.getArena().getPosition().getLast().toBukkitLocation();
						Location spawnLocation = playerDuel.getFirst().contains(event.getEntity().getUniqueId()) ? locationFirst : locationSecond;
						spawnLocation = MatchUtils.findSafeLocationAbove(spawnLocation);
						event.getEntity().teleport(spawnLocation);
						event.setCancelled(true);
						return;
					}
					if (playerDuel.getKit().name().equals("sumo") || playerDuel.getKit().name().equals("boxing")) {
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
			if (victimProfile.isInState(ProfileState.FIGHT) && profileDamager.isInState(ProfileState.FIGHT) && !((Player) event.getDamager()).getGameMode().equals(GameMode.ADVENTURE)) {
				if (Core.API.getManagerHandler().getProfileManager().getFrozen().contains(event.getEntity().getUniqueId())) {
					event.getDamager().sendMessage(ChatColor.RED + event.getEntity().getName() + " is currently frozen.");
					event.setCancelled(true);
					return;
				}
				if (!profileDamager.isAllowClick()) {
					event.setDamage(0.0f);
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
						if (duel.getKit().name().equals("sumo") || duel.getKit().name().equals("boxing")) {
							event.setDamage(0.0f);
							if (duel.getKit().name().equals("boxing")) {
								final ProfileManager profileManager = main.getManagerHandler().getProfileManager();
								final Long nextHitTick = profileManager.getDuelStatistics().get(event.getDamager().getUniqueId()).getNextHitTick();
								if (nextHitTick != 0L && nextHitTick  > System.currentTimeMillis()) {
									return;
								}
								profileManager.getDuelStatistics().get(event.getDamager().getUniqueId()).updateNextHitTick();
								final int hit = profileManager.getDuelStatistics().get(event.getDamager().getUniqueId()).getHits();
								final float hitExp = hit / 100.0f;
								Bukkit.getPlayer(event.getDamager().getUniqueId()).setExp(hitExp);
								Bukkit.getPlayer(event.getDamager().getUniqueId()).setLevel(hit);
								if (hit >= 99) {
									if (duel.getDuelType().equals(Duel.DuelType.SINGLE)) {
										main.getManagerHandler().getDuelManager().endSingle(event.getDamager().getUniqueId());
									}
									else {
										main.getManagerHandler().getDuelManager().endMultiple(event.getDamager().getUniqueId());
									}
								}
							}
						}
						statisticsDamager.setHits(statisticsDamager.getHits()+1);
						statisticsDamager.setCombo(statisticsDamager.getCombo()+1);
						statisticsVictim.setCombo(0);
						if (statisticsDamager.getCombo() > statisticsDamager.getLongestHit()) {
							statisticsDamager.setLongestHit(statisticsDamager.getCombo());
						}
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
				if (profileDamager.isInState(ProfileState.FIGHT) && victimProfile.isInState(ProfileState.FIGHT)) {
					shooter.sendMessage(ChatColor.GRAY + "[" + ChatColor.DARK_RED + "Bow" + ChatColor.GRAY + "] " + ChatColor.RED + event.getEntity().getName() + ChatColor.WHITE + " life is " + ChatColor.GRAY + ": " + ChatColor.RED + FormatUtils.formatTime((long) Bukkit.getPlayer(event.getEntity().getUniqueId()).getHealth(), 2.0d) + ChatColor.DARK_RED + "❤");
				}
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
							if (profileDamager.getKill() >= 25) {
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

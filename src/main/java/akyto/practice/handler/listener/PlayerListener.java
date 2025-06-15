package akyto.practice.handler.listener;

import akyto.core.Core;
import akyto.core.particle.ParticleEntry;
import akyto.core.profile.Profile;
import akyto.core.profile.ProfileState;
import akyto.core.utils.CoreUtils;
import akyto.practice.Practice;
import akyto.practice.duel.Duel;
import akyto.practice.duel.Duel.DuelType;
import akyto.practice.duel.cache.DuelState;
import akyto.practice.duel.cache.DuelStatistics;
import akyto.practice.event.EventState;
import akyto.practice.handler.manager.EventManager;
import akyto.practice.kit.Kit;
import akyto.practice.kit.KitInterface;
import akyto.practice.tournament.TournamentEntry;
import akyto.practice.tournament.TournamentState;
import akyto.practice.utils.Utils;
import akyto.practice.utils.chat.MessageHelper;
import akyto.practice.utils.chat.MessageUtils;
import akyto.practice.utils.match.MatchUtils;
import akyto.practice.utils.match.TagUtils;
import com.google.common.collect.Lists;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.craftbukkit.v1_8_R3.CraftServer;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.entity.Item;
import org.bukkit.entity.LightningStrike;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.Event.Result;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.entity.PotionSplashEvent;
import org.bukkit.event.player.*;
import org.bukkit.event.weather.WeatherChangeEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.projectiles.ProjectileSource;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;

public class PlayerListener implements Listener {
	
	private final Practice main;
	
	public PlayerListener(final Practice main) { this.main = main; }
	
	@EventHandler
	public void onPlayerJoin(final PlayerJoinEvent event) {
		Core.API.getManagerHandler().getProfileManager().createProfile(event.getPlayer().getUniqueId());
		final String[] kitNames = new String[this.main.getKits().size()];
		for (Kit kit : this.main.getKits()) { kitNames[kit.id()] = kit.displayName(); }
		Core.API.getDatabaseSetup().update(event.getPlayer().getUniqueId(), this.main.getKits().size(), kitNames);
		this.main.getManagerHandler().getItemManager().giveItems(event.getPlayer().getUniqueId(), false);
		Utils.resetPlayer(event.getPlayer().getUniqueId());
		event.getPlayer().setMaximumNoDamageTicks(20);
		event.getPlayer().teleport(this.main.getSpawn().getLocation() == null ? event.getPlayer().getWorld().getSpawnLocation() : this.main.getSpawn().getLocation());
		this.main.getManagerHandler().getInventoryManager().generateSettingsInventory(event.getPlayer().getUniqueId());
	}

	@EventHandler
	public void onPlayerLeft(final PlayerQuitEvent event) {
		final Player leaver = event.getPlayer();
		final Profile profile = Utils.getProfiles(event.getPlayer().getUniqueId());
		final boolean inTournament = Utils.getTournamentByPlayerUUID(leaver.getUniqueId()) != null;
		if (event.getPlayer().isOnline() && profile != null) {
			if (Utils.getPartyByUUID(event.getPlayer().getUniqueId()) != null) {
				this.main.getManagerHandler().getPartyManager().leaveParty(event.getPlayer().getUniqueId());
			}
			if (profile.isInState(ProfileState.QUEUE)) {
				this.main.getManagerHandler().getQueueManager().removePlayerFromQueue(event.getPlayer().getUniqueId());
			}
			if (profile.isInState(ProfileState.EVENT)) {
				this.main.getManagerHandler().getEventManager().leaveEvent(leaver.getUniqueId(), true);
			}
			if (profile.isInState(ProfileState.SPECTATE)) {
				Duel duel = Utils.getDuelBySpectator(leaver.getUniqueId());
				if (duel == null) duel = Utils.getDuelByUUID(leaver.getUniqueId());
				duel.getSpectators().remove(leaver.getUniqueId());
				Arrays.asList(new ArrayList<>(duel.getFirst()), new ArrayList<>(duel.getSecond())).forEach(uuids -> uuids.forEach(uuid -> Bukkit.getPlayer(uuid).sendMessage(ChatColor.WHITE + event.getPlayer().getName() + ChatColor.DARK_GRAY + " is no longer spectating your match.")));
			}
			if (profile.isInState(ProfileState.FIGHT)) {
				final Duel duel = Utils.getDuelByUUID(leaver.getUniqueId());
				if (duel != null) {
					if (!duel.getState().equals(DuelState.FINISHING) && duel.getDuelType().equals(DuelType.SINGLE) && !inTournament) {
						duel.getDisconnected().add(leaver.getUniqueId());
						this.main.getManagerHandler().getDuelManager().endSingle(duel.getFirst().contains(leaver.getUniqueId()) ? new ArrayList<>(duel.getSecond()).getFirst() : new ArrayList<>(duel.getFirst()).getFirst());
					}
					if (!duel.getState().equals(DuelState.FINISHING) && (duel.getDuelType().equals(DuelType.FFA) || duel.getDuelType().equals(DuelType.SPLIT))) {
						MatchUtils.addKill(leaver.getUniqueId(), null, true);
					}
				}
			}
			if (inTournament) {
				TournamentEntry tournamentEntry = Utils.getTournamentByPlayerUUID(leaver.getUniqueId());
				if (tournamentEntry != null) {
					if (tournamentEntry.getTournamentState().equals(TournamentState.STARTED) && profile.isInState(ProfileState.FIGHT)) {
						final Duel duel = Utils.getDuelByUUID(leaver.getUniqueId());
						if (duel != null && !duel.getState().equals(DuelState.FINISHING)) {
							final UUID oppsUUID = duel.getFirst().contains(leaver.getUniqueId()) ? duel.getSecond().stream().toList().getFirst() : duel.getFirst().stream().toList().getFirst();
							final Player opps = Bukkit.getPlayer(oppsUUID);
							MatchUtils.handleTournamentDeath(leaver, opps, tournamentEntry);
						}
					}
				}
			}
			if (Utils.getTournamentByPlayerUUID(leaver.getUniqueId()) != null) {
				this.main.getManagerHandler().getTournamentManager().leaveTournament(leaver.getUniqueId());
			}
		}
		this.main.getManagerHandler().getInventoryManager().removeUselessInventory(event.getPlayer().getUniqueId());
	}

	// Depend on the spigot build
//	@EventHandler
//	public void onPlayerKick(final PlayerKickEvent event) {
//		this.onPlayerLeft(new PlayerQuitEvent(event.getPlayer(), null));
//	}

	// Limit fire extinguish to fights
	@EventHandler
	public void onFireExtinguish(PlayerInteractEvent event) {
		Player player = event.getPlayer();
		Profile profile = Utils.getProfiles(player.getUniqueId());

		if (!player.getGameMode().equals(GameMode.SURVIVAL))
			return;
		if (profile == null || profile.isInState(ProfileState.FIGHT))
			return;
		if (!event.getAction().equals(Action.LEFT_CLICK_BLOCK))
			return;
		if (event.getClickedBlock().getRelative(event.getBlockFace()).getType().equals(Material.FIRE))
			event.setCancelled(true);
	}

    private boolean isPlants(Material material) {
        return material == Material.CROPS || material == Material.CARROT || material == Material.POTATO || material == Material.NETHER_WARTS || material == Material.COCOA || material == Material.SUGAR_CANE_BLOCK || material == Material.MELON_BLOCK || material == Material.PUMPKIN || material == Material.CACTUS;
    }
	
	@EventHandler
	public void onInteract(final PlayerInteractEvent event) {
		final Player player = event.getPlayer();
		if (player.getGameMode().equals(GameMode.CREATIVE)) return;
		if (event.getItem() == null || event.getItem().getType().equals(Material.AIR)) return;
		final boolean inTournament = Utils.getTournamentByPlayerUUID(player.getUniqueId()) != null;
		final Profile profile = Utils.getProfiles(player.getUniqueId());
		Block clickedBlock = event.getClickedBlock();
        if (clickedBlock != null) {
            if (isPlants(clickedBlock.getType()) && player.getLocation().getBlockY() > clickedBlock.getLocation().getBlockY()) {
                event.setCancelled(true);
            }
        }
		if (event.getAction().equals(Action.RIGHT_CLICK_AIR) || event.getAction().equals(Action.RIGHT_CLICK_BLOCK)) {
			if (profile.isInState(ProfileState.FREE)) {
				if (Utils.getPartyByUUID(player.getUniqueId()) != null) {
					if (event.getItem().getType().equals(Material.REDSTONE_TORCH_ON)) { this.main.getManagerHandler().getPartyManager().leaveParty(player.getUniqueId()); }
					if (event.getItem().getType().equals(Material.PAPER)) { this.main.getManagerHandler().getPartyManager().sendPartyInformation(player.getUniqueId()); }
					if (event.getItem().getType().equals(Material.CHEST)) { this.main.getManagerHandler().getInventoryManager().getPartyMultipage().open(player, 1); }
					if (event.getItem().getType().equals(Material.DIAMOND_AXE)) { 
						if (!Utils.getPartyByUUID(player.getUniqueId()).getCreator().equals(player.getUniqueId())) {
							player.sendMessage(ChatColor.RED + "You're not the party's creator!");
							return;
						}
						if (!(Utils.getPartyByUUID(player.getUniqueId()).getMembers().size() > 1)) {
							player.sendMessage(ChatColor.RED + "You must have at least two members to launch an event!");
							return;
						}
						AtomicBoolean membersInMatch = new AtomicBoolean(false);
						Utils.getPartyByUUID(player.getUniqueId()).getMembers().forEach(members -> {
							final Profile profiles = Utils.getProfiles(members);
							if (profiles.isInState(ProfileState.FIGHT, ProfileState.EDITOR, ProfileState.MOD, ProfileState.SPECTATE, ProfileState.QUEUE)) {
								membersInMatch.set(true);
							}
						});
						if (membersInMatch.get()) {
							player.sendMessage(ChatColor.RED + "You cannot start a match while members are unavailable");
							return;
						}
						player.openInventory(this.main.getManagerHandler().getInventoryManager().getPartyEventInventory());
					}
				}
				if (event.getItem().getType().equals(Material.IRON_SWORD)) { player.openInventory(this.main.getManagerHandler().getInventoryManager().getQueueInventory()[0]); }
				if (event.getItem().getType().equals(Material.DIAMOND_SWORD)) {
					if (!profile.isLinked()) {
						player.sendMessage(org.bukkit.ChatColor.RED + "You can't queue into ranked if your discord account was not linked!");
						return;
					}
					int totalPlayed = 0;
					for (int i = 0; i <= profile.getStats().size(); i++) {
						totalPlayed += profile.getStats().getFirst()[i];
					}
					if (totalPlayed < 10) {
						player.sendMessage(org.bukkit.ChatColor.RED + "You must be played 10 games to queue in ranked!");
						return;
					}
					player.openInventory(this.main.getManagerHandler().getInventoryManager().getQueueInventory()[1]);
				}
				if (event.getItem().getType().equals(Material.NAME_TAG)) { this.main.getManagerHandler().getPartyManager().createParty(player.getUniqueId()); }
				if (event.getItem().getType().equals(Material.NETHER_STAR)) { player.openInventory(this.main.getManagerHandler().getInventoryManager().getUtilsInventory()); }
				if (event.getItem().getType().equals(Material.BOOK)) { player.openInventory(this.main.getManagerHandler().getInventoryManager().getEditorInventory()[0]); }
				if (inTournament) {
					if (event.getItem().getType().equals(Material.REDSTONE_TORCH_ON)) this.main.getManagerHandler().getTournamentManager().leaveTournament(player.getUniqueId());
				}
				return;
			}
			if (profile.isInState(ProfileState.QUEUE, ProfileState.EVENT)) {
				if (event.getItem().getType().equals(Material.REDSTONE_TORCH_ON)) {
					event.setUseInteractedBlock(Result.DENY);
					event.setUseItemInHand(Result.DENY);
					if (profile.isInState(ProfileState.QUEUE)) this.main.getManagerHandler().getQueueManager().removePlayerFromQueue(player.getUniqueId());
					if (profile.isInState(ProfileState.EVENT)) this.main.getManagerHandler().getEventManager().leaveEvent(player.getUniqueId(), false);
				}
				return;
			}	
			if (profile.isInState(ProfileState.SPECTATE)) {
				event.setUseInteractedBlock(Result.DENY);
				event.setUseItemInHand(Result.DENY);
				event.setCancelled(true);
				if (event.getItem().getType().equals(Material.CHEST)) { 
					player.openInventory(this.main.getManagerHandler().getInventoryManager().getSpectateInventory().get(player.getUniqueId()));
					return;
				}
				if (event.getItem().getType().equals(Material.COMPASS)) { 
					this.main.getManagerHandler().getInventoryManager().getSpectateMultipage().open(player, 1);
				}
				if (event.getItem().getType().equals(Material.REDSTONE_COMPARATOR)) { 
					player.openInventory(this.main.getManagerHandler().getInventoryManager().getSettingsSpectateInventory().get(player.getUniqueId()));
				}
				if (event.getItem().getType().equals(Material.REDSTONE_TORCH_ON)) {
					if (Utils.getDuelBySpectator(player.getUniqueId()) != null){
						final Duel duel = Utils.getDuelBySpectator(player.getUniqueId());
						duel.getSpectators().remove(player.getUniqueId());
						Arrays.asList(new ArrayList<>(duel.getFirst()), new ArrayList<>(duel.getSecond())).forEach(uuids -> uuids.forEach(uuid -> {
							Bukkit.getPlayer(uuid).sendMessage(ChatColor.WHITE + player.getName() + ChatColor.DARK_GRAY + " is no longer spectating your match.");
							player.hidePlayer(Bukkit.getPlayer(uuid));
						}));
						Utils.sendToSpawn(player.getUniqueId(), true);
					}
					else {
						final Duel duel = Utils.getDuelByUUID(player.getUniqueId());
						duel.getDisconnected().add(player.getUniqueId());
						if (!duel.getDuelType().equals(DuelType.FFA)) {
							TagUtils.clearNameTags(player);
						}
						Arrays.asList(new ArrayList<>(duel.getFirst()), new ArrayList<>(duel.getSecond())).forEach(uuids -> uuids.forEach(uuid -> {
							if (Bukkit.getPlayer(uuid) != null) {
								Bukkit.getPlayer(uuid).sendMessage(ChatColor.WHITE + player.getName() + ChatColor.DARK_GRAY + " is no longer spectating your match.");
								player.hidePlayer(Bukkit.getPlayer(uuid));
							}
						}));
						profile.setProfileState(ProfileState.FREE);
						Utils.sendToSpawn(player.getUniqueId(), true);
					}
					return;
				}
			}
			if (profile.isInState(ProfileState.EDITOR) && event.getAction().equals(Action.RIGHT_CLICK_BLOCK)) {
				event.setUseInteractedBlock(Result.DENY);
				event.setUseItemInHand(Result.DENY);
				event.setCancelled(true);
				if (event.getClickedBlock().getType().equals(Material.CHEST)) { 
					player.openInventory(this.main.getManagerHandler().getInventoryManager().getEditorInventory()[1]);
					return;
				}
				if (event.getClickedBlock().getType().equals(Material.ANVIL)) { player.openInventory(this.main.getManagerHandler().getInventoryManager().getEditorInventory()[2]); }
				if (event.getClickedBlock().getType().equals(Material.WOODEN_DOOR)) {
					player.sendMessage(ChatColor.GREEN + "You've returned to the spawn");
					Utils.sendToSpawn(player.getUniqueId(), true);
					return;
				}
			}
		}
	}

	@EventHandler
	public void onFightInteract(final PlayerInteractEvent event) {
		final Player player = event.getPlayer();
		final Profile profile = Utils.getProfiles(player.getUniqueId());
		final boolean inTournament = Utils.getTournamentByPlayerUUID(player.getUniqueId()) != null;
		if (event.getAction().equals(Action.RIGHT_CLICK_AIR) || event.getAction().equals(Action.RIGHT_CLICK_BLOCK)) {
			if (player.getItemInHand() == null || event.getItem() == null) return;
			TournamentEntry tournamentEntry = Utils.getTournamentByPlayerUUID(player.getUniqueId());
			if (profile.isInState(ProfileState.FIGHT) || (inTournament && tournamentEntry != null && tournamentEntry.getTournamentState().equals(TournamentState.STARTED))) {
				final Duel duel = Utils.getDuelByUUID(player.getUniqueId());
				if (player.getItemInHand().getType() == Material.ENDER_PEARL) {
					if (duel.getState().equals(DuelState.STARTING)) {
						event.setUseItemInHand(Event.Result.DENY);
						player.sendMessage(ChatColor.RED + "You can't launch an enderpearl yet!");
						player.updateInventory();
						return;
					}
				}

				if (duel.getState().equals(DuelState.STARTING)) {
					if (event.getItem().getType().equals(Material.POTION) && event.getItem().getDurability() == 16421) {
						event.setUseItemInHand(Event.Result.DENY);
						event.setCancelled(true);
					}
				}

				final Kit kit = duel.getKit();
				if (player.getItemInHand().getType().equals(Material.ENCHANTED_BOOK)) {
					event.setUseItemInHand(Event.Result.DENY);
					player.closeInventory();
					player.getInventory().clear();
					player.getInventory().setArmorContents(this.main.getManagerHandler().getProfileManager().getEditor().get(player.getUniqueId()).get(kit.name()).getArmorContent());
					player.getInventory().setContents(this.main.getManagerHandler().getProfileManager().getEditor().get(player.getUniqueId()).get(kit.name()).getContent());
					player.updateInventory();
					return;
				}

				if (player.getItemInHand().getType().equals(Material.BOOK)) {
					final KitInterface kitI = (KitInterface) Utils.getDuelByUUID(player.getUniqueId()).getKit();
					player.getInventory().setArmorContents(kitI.armor());
					player.getInventory().setContents(kitI.content());
					player.updateInventory();
					return;
				}

				if (!player.isDead() && player.getItemInHand().getType() == Material.MUSHROOM_SOUP && player.getHealth() < player.getMaxHealth()) {
					final double newHealth = Math.min(player.getHealth() + 7.0D, player.getMaxHealth());
					player.setHealth(newHealth);
					player.getItemInHand().setType(Material.BOWL);
					player.updateInventory();
					return;
				}
			}
		}
	}

	
	@EventHandler(priority=EventPriority.LOW)
	public void PlayerPlaceBlockEvent(final BlockPlaceEvent event) {
		if (event.getPlayer().getGameMode().equals(GameMode.CREATIVE)) return;
		final Player player = event.getPlayer();
		final Profile profile = Utils.getProfiles(player.getUniqueId());
		if (profile.isInState(ProfileState.FIGHT)) {
			final Duel duel = Utils.getDuelByUUID(player.getUniqueId());
			if (duel.getKit().buildable() && duel.getState().equals(DuelState.PLAYING)) {
				Location corner1 = Utils.getArenaCopyByPlayer(event.getPlayer().getUniqueId()) != null ? Utils.getArenaCopyByPlayer(event.getPlayer().getUniqueId()).getCorner1().toBukkitLocation() : duel.getArena().getCorner1().toBukkitLocation();
				Location corner2 = Utils.getArenaCopyByPlayer(event.getPlayer().getUniqueId()) != null ? Utils.getArenaCopyByPlayer(event.getPlayer().getUniqueId()).getCorner2().toBukkitLocation() : duel.getArena().getCorner2().toBukkitLocation();
				Location blockLocation = event.getBlockPlaced().getLocation();
				if (!MatchUtils.isLocationInZone(blockLocation, corner1, corner2)) {
					event.setCancelled(true);
					player.sendMessage(ChatColor.RED + "You cannot go out of map :)");
					return;
				}
				duel.getBlocks().add(event.getBlockPlaced());
				return;
			}
		}
		event.setCancelled(true);
	}
	
	@EventHandler(priority=EventPriority.LOW)
	public void PlayerBreakBlockEvent(final BlockBreakEvent event) {
		if (event.getPlayer().getGameMode().equals(GameMode.CREATIVE)) return;
		final Player player = event.getPlayer();
		final Profile profile = Utils.getProfiles(player.getUniqueId());
		if (profile.isInState(ProfileState.FIGHT)) {
			final Duel duel = Utils.getDuelByUUID(player.getUniqueId());
			if (duel.getKit().buildable() && duel.getState().equals(DuelState.PLAYING)) {
				if (duel.getKit().name().equals("bedfight")) {
					if (event.getBlock().getType().equals(Material.WOOD) || event.getBlock().getType().equals(Material.WOOL)) return;
					if (event.getBlock().getType().equals(Material.BED_BLOCK)) {
						final Location isLocation = duel.getFirst().contains(event.getPlayer().getUniqueId()) ? (Utils.getArenaCopyByPlayer(event.getPlayer().getUniqueId()) != null ? Utils.getArenaCopyByPlayer(event.getPlayer().getUniqueId()).getPosition().getFirst().toBukkitLocation() : duel.getArena().getPosition().getFirst().toBukkitLocation()) :  (Utils.getArenaCopyByPlayer(event.getPlayer().getUniqueId()) != null ? Utils.getArenaCopyByPlayer(event.getPlayer().getUniqueId()).getPosition().getLast().toBukkitLocation() : duel.getArena().getPosition().getLast().toBukkitLocation());
						if (MatchUtils.isNearSpawnLocation(event.getBlock().getLocation(), isLocation, 20)) {
							event.getPlayer().sendMessage(ChatColor.RED + "You cannot break your bed!");
							event.setCancelled(true);
							return;
						}
						else {
							final DuelStatistics statistics = main.getManagerHandler().getProfileManager().getDuelStatistics().get(duel.getFirst().contains(event.getPlayer().getUniqueId()) ? duel.getSecond().stream().toList().getFirst() : duel.getFirst().stream().toList().getFirst());
							statistics.setBedBroke(true);
							final List<UUID> players = Lists.newArrayList();
							players.addAll(duel.getFirst());
							players.addAll(duel.getSecond());
							players.addAll(duel.getSpectators());
							MessageHelper.notifyUUID(players, ChatColor.GRAY + "[" + ChatColor.DARK_RED + "Match" + ChatColor.GRAY + "] " + MessageUtils.getColorTeam(duel, duel.getFirst().contains(event.getPlayer().getUniqueId()) ? duel.getSecond().stream().toList().getFirst() : duel.getFirst().stream().toList().getFirst()) + ChatColor.WHITE + " bed has destroyed, no respawn available!");
						}
						return;
					}
				}
				if (duel.getBlocks().contains(event.getBlock())) {
					duel.getBlocks().remove(event.getBlock());
					return;
				}
			}
		}
		event.setCancelled(true);
	}
	
	@EventHandler(priority=EventPriority.LOW)
	public void PlayerFoodChange(final FoodLevelChangeEvent event) {
		final Profile profile = Utils.getProfiles(event.getEntity().getUniqueId());
		if (profile.isInState(ProfileState.FIGHT)) {
			final Duel duel = Utils.getDuelByUUID(event.getEntity().getUniqueId());
			if (duel.getKit().name().equals("sumo") || duel.getKit().name().equals("soup") || duel.getKit().name().equals("boxing") || duel.getKit().name().equals("bedfight") || duel.getKit().name().equals("classic")) {
				event.setCancelled(true);
				event.setFoodLevel(20);
				return;	
			}
			return;
		}
		event.setCancelled(true);
	}
	
	@EventHandler
	public void onDrop(final PlayerDropItemEvent event) {
		if (event == null || event.getItemDrop() == null) return;
		final Profile data = Utils.getProfiles(event.getPlayer().getUniqueId());
		if (data.isInState(ProfileState.FIGHT)) {
			if (Utils.getDuelByUUID(event.getPlayer().getUniqueId()).getState().equals(DuelState.PLAYING) || Utils.getDuelByUUID(event.getPlayer().getUniqueId()).getState().equals(DuelState.STARTING)) {
				if (event.getItemDrop().getItemStack().getType().equals(Material.GLASS_BOTTLE)) {
					event.getItemDrop().remove();
					return;
				}
				if (event.getItemDrop().getItemStack().getType().equals(Material.DIAMOND_SWORD) || event.getItemDrop().getItemStack().getType().equals(Material.IRON_SWORD) || event.getItemDrop().getItemStack().getType().equals(Material.STONE_SWORD) || event.getItemDrop().getItemStack().getType().equals(Material.WOOD_SWORD)) {
					event.setCancelled(true);
					return;
				}
				MatchUtils.addDrops(event.getItemDrop(), event.getPlayer().getUniqueId());
				return;	
			}
		}
		event.setCancelled(true);
	}

	@EventHandler
	public void onDeath(final PlayerDeathEvent event) {
		final Player killed = event.getEntity();
		final Player killer = killed.getKiller();
		final Location deathLoc = killed.getLocation();

		event.setDeathMessage(null);

		final Profile victimProfile = Utils.getProfiles(killed.getUniqueId());
		if (victimProfile.isInState(ProfileState.EVENT)) {
			final Profile profileDamager = Utils.getProfiles(killer.getUniqueId());
			event.getDrops().clear();
			final EventManager eventManager = this.main.getManagerHandler().getEventManager();
			if (eventManager.getType().equals("oitc") && eventManager.getEventState().equals(EventState.FIGHT)) {
				if (!killed.getLastDamageCause().getCause().equals(EntityDamageEvent.DamageCause.PROJECTILE)) {
					victimProfile.setDeath(victimProfile.getDeath()+1);
					profileDamager.setKill(profileDamager.getKill()+1);
					if (profileDamager.getKill() >= 25) {
						eventManager.endMatch(killer.getUniqueId(), killed.getUniqueId());
						return;
					}
					eventManager.getMembers().forEach(member -> {
						if (Bukkit.getPlayer(member) != null) {
							final Player player = Bukkit.getPlayer(member);
							player.sendMessage(org.bukkit.ChatColor.GRAY + "[" + org.bukkit.ChatColor.DARK_RED + "OITC" + org.bukkit.ChatColor.GRAY + "] " + org.bukkit.ChatColor.GREEN + killer.getName() + org.bukkit.ChatColor.GRAY + " slashed " + org.bukkit.ChatColor.RED + killed.getName());
						}
					});
					killer.getInventory().addItem(new ItemStack(Material.ARROW));
					killer.updateInventory();
				}
				killed.spigot().respawn();
				Random random = new Random();
				int randomNumber = random.nextInt(15);
				Bukkit.getScheduler().runTaskLater(main, () -> {
					killed.teleport(this.main.getOitcLoc().get(randomNumber).getLocation());
					this.main.getManagerHandler().getItemManager().giveItems(killed.getUniqueId(), false);
				}, 2L);
			}
			return;
		}

		final Duel duel = Utils.getDuelByUUID(killed.getUniqueId());
		if (duel != null) {
			Set<UUID> duelPlayers = new HashSet<>();
			duelPlayers.addAll(duel.getFirst());
			duelPlayers.addAll(duel.getSecond());
			duelPlayers.addAll(duel.getSpectators());

			duelPlayers.stream().map(Bukkit::getPlayer).forEach(player -> {
				if (player == null) return;
				player.playSound(deathLoc, Sound.AMBIENCE_THUNDER, 10000.0F, deathLoc.getPitch());
				if (killer != null) {
					final Profile profileKiller = Utils.getProfiles(killer.getUniqueId());
					if (!profileKiller.getEffect().equals("none")) {
						final ParticleEntry particleEntry = CoreUtils.getParticleBySection(profileKiller.getEffect());
						final Location deathLocationClone = deathLoc.clone();
						killer.spigot().playEffect(deathLocationClone.add(0, 0.5d, 0), particleEntry.getParticle(), 0,0, particleEntry.getXOffSet(), particleEntry.getYOffSet(), particleEntry.getZOffSet(), particleEntry.getSpeed(), particleEntry.getAmount(), 192);
						killed.spigot().playEffect(deathLocationClone.add(0, 0.5d, 0), particleEntry.getParticle(), 0,0, particleEntry.getXOffSet(), particleEntry.getYOffSet(), particleEntry.getZOffSet(), particleEntry.getSpeed(), particleEntry.getAmount(), 192);
					}
				}
			});
			event.setDroppedExp(0);
			killed.setLevel(0);
			killed.setExp(0);
			if (!duel.getDuelType().equals(DuelType.SINGLE)) {
				for (ItemStack item : event.getDrops()) {
					final Item items = killed.getWorld().dropItemNaturally(deathLoc, new ItemStack(item.clone()) ,killed);
					MatchUtils.addDrops(items, killed.getUniqueId());
				}
			}
			Utils.drops(killed.getUniqueId(), event.getDrops(), deathLoc);
			if (killer != null) Utils.drops(killer.getUniqueId(), event.getDrops(), deathLoc);
			event.getDrops().clear();
			final Profile profile = Utils.getProfiles(killed.getUniqueId());
			final boolean inBedfight = duel.getKit().name().equals("bedfight");
			if ((profile.isInState(ProfileState.FIGHT))) {
				new BukkitRunnable() {
					public void run() {
						try {
							CraftServer server = (CraftServer) killed.getServer();
							server.getHandle().moveToWorld(((CraftPlayer) killed).getHandle(), 0, false);
						}
						catch (Exception ex) {
							killed.spigot().respawn();
							ex.printStackTrace();
						}
						killed.teleport(deathLoc);
					}
				}.runTaskLater(this.main, inBedfight ? 2L : 10L);
				new BukkitRunnable() {
					public void run() {
						if (killer != null)
							killer.hidePlayer(killed);
					}
				}.runTaskLater(main, inBedfight ? 2L : 10L);
				TournamentEntry tournamentEntry = Utils.getTournamentByPlayerUUID(killed.getUniqueId());
				if (duel.getDuelType().equals(DuelType.SINGLE) && tournamentEntry == null) {
					LightningStrike lightning = deathLoc.getWorld().strikeLightningEffect(deathLoc);
					lightning.setFireTicks(0);
					lightning.setSilent(true);
					if (inBedfight) {
						final DuelStatistics statistics = main.getManagerHandler().getProfileManager().getDuelStatistics().get(event.getEntity().getUniqueId());
						if (!statistics.isBedBroke()) {
							MatchUtils.deathBedfight(killed, duel);
							final UUID oppsID = duel.getFirst().contains(killed.getUniqueId()) ? duel.getSecond().stream().toList().getFirst() : duel.getFirst().stream().toList().getFirst();
							final ChatColor colorKilled = duel.getFirst().contains(killed.getUniqueId()) ? ChatColor.BLUE : ChatColor.RED;
							final ChatColor colorKiller = duel.getFirst().contains(oppsID) ? ChatColor.BLUE : ChatColor.RED;
							MessageHelper.notifyUUID(duelPlayers.stream().toList(), ChatColor.GRAY + "[" + ChatColor.DARK_RED + "Match" + ChatColor.GRAY + "] " + colorKilled + killed.getName() + ChatColor.WHITE + " killed by " + colorKiller + CoreUtils.getName(oppsID));
							duelPlayers.clear();
							return;
						}
					}
					this.main.getManagerHandler().getDuelManager().endSingle(killed.getUniqueId().equals(new ArrayList<>(duel.getFirst()).getFirst()) ? new ArrayList<>(duel.getSecond()).getFirst() : new ArrayList<>(duel.getFirst()).getFirst());
					return;
				}
				if (tournamentEntry != null) {
					if (tournamentEntry.getTeamSize() == 1) {
						MatchUtils.handleTournamentDeath(killed, killer, tournamentEntry);
						return;
					}
				}
				MatchUtils.addKill(killed.getUniqueId(), killer != null ? killer.getUniqueId() : null, false);
			}
		}
	}

	@EventHandler
	public void onWeatherChange(final WeatherChangeEvent event) {
		event.setCancelled(true);
	}

	@EventHandler
	public void onTeleport(PlayerTeleportEvent event) {
		Player teleported = event.getPlayer();
		Profile profile = Utils.getProfiles(teleported.getUniqueId());

		if (profile == null)
			return;

		// Prevent pearl teleporting after fight MAY NOT BE PERFECT !
		if (event.getCause().equals(PlayerTeleportEvent.TeleportCause.ENDER_PEARL) && !profile.isInState(ProfileState.FIGHT)) {
			event.setCancelled(true);
			return;
		}
	}

	@EventHandler
	public void onPlayerItemConsume(PlayerItemConsumeEvent event) {
		if (event.getItem().getType() == Material.GOLDEN_APPLE) {
			if (!event.getItem().hasItemMeta() || !event.getItem().getItemMeta().getDisplayName().contains("Golden Head")) {
				return;
			}

			Profile profile = Utils.getProfiles(event.getPlayer().getUniqueId());
			if (profile.isInState(ProfileState.FIGHT)) {
				Player player = event.getPlayer();
				event.getPlayer().addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, 200, 1));
				event.getPlayer().addPotionEffect(new PotionEffect(PotionEffectType.ABSORPTION, 2400, 0));
				player.setFoodLevel(Math.min(player.getFoodLevel() + 6, 20));
			}
		}
	}

	@EventHandler
	public void onPotionSplash(PotionSplashEvent event) {
		boolean hasHealEffect = event.getPotion().getEffects().stream()
				.anyMatch(effect -> effect.getType().equals(PotionEffectType.HEAL));

		if (!hasHealEffect) return;

		double healAmount = event.getPotion().getEffects().stream()
				.filter(effect -> effect.getType().equals(PotionEffectType.HEAL))
				.findFirst()
				.map(effect -> (effect.getAmplifier() + 1) * 2)
				.orElse(0);
		Player thrower;
		ProjectileSource shooter = event.getEntity().getShooter();
		if (shooter instanceof Player) {
			thrower = (Player) shooter;
		} else {
            thrower = null;
        }

        if (thrower == null) return;

		final DuelStatistics statistics = main.getManagerHandler().getProfileManager()
				.getDuelStatistics().get(thrower.getUniqueId());

		boolean throwerAffected = event.getAffectedEntities().stream()
				.anyMatch(entity -> entity.equals(thrower));

		statistics.setPotThrown(statistics.getPotThrown() + 1);

		if (throwerAffected) {
			statistics.setPotHeal(statistics.getPotHeal() + healAmount);
		} else {
			statistics.setPotMissed(statistics.getPotMissed() + 1);
		}
	}
}

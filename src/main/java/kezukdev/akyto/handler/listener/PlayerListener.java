package kezukdev.akyto.handler.listener;

import java.util.*;

import akyto.core.utils.database.DatabaseType;
import kezukdev.akyto.runnable.PearlExpireRunnable;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.craftbukkit.v1_8_R3.CraftServer;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.entity.Item;
import org.bukkit.entity.LightningStrike;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.Event.Result;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.*;
import org.bukkit.event.weather.WeatherChangeEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import akyto.core.Core;
import akyto.core.profile.Profile;
import akyto.core.profile.ProfileState;
import akyto.core.utils.format.FormatUtils;
import kezukdev.akyto.Practice;
import kezukdev.akyto.duel.Duel;
import kezukdev.akyto.duel.Duel.DuelType;
import kezukdev.akyto.duel.cache.DuelState;
import kezukdev.akyto.duel.cache.DuelStatistics;
import kezukdev.akyto.kit.Kit;
import kezukdev.akyto.kit.KitInterface;
import kezukdev.akyto.utils.Utils;
import kezukdev.akyto.utils.match.MatchUtils;
import net.md_5.bungee.api.ChatColor;

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
		event.getPlayer().teleport(this.main.getSpawn().getLocation() == null ? event.getPlayer().getWorld().getSpawnLocation() : this.main.getSpawn().getLocation());
		this.main.getManagerHandler().getInventoryManager().generateSettingsInventory(event.getPlayer().getUniqueId());
	}

	@EventHandler
	public void onPlayerLeft(final PlayerQuitEvent event) {
		final Profile profile = Utils.getProfiles(event.getPlayer().getUniqueId());
		if (event.getPlayer().isOnline() && profile != null) {
			if (Utils.getPartyByUUID(event.getPlayer().getUniqueId()) != null) {
				this.main.getManagerHandler().getPartyManager().leaveParty(event.getPlayer().getUniqueId());
			}
			if (profile.isInState(ProfileState.QUEUE)) {
				this.main.getManagerHandler().getQueueManager().removePlayerFromQueue(event.getPlayer().getUniqueId());
			}
			if (profile.isInState(ProfileState.SPECTATE)) {
				Duel duel = Utils.getDuelBySpectator(event.getPlayer().getUniqueId());
				if (duel == null) duel = Utils.getDuelByUUID(event.getPlayer().getUniqueId());
				duel.getSpectators().remove(event.getPlayer().getUniqueId());
				Arrays.asList(new ArrayList<>(duel.getFirst()), new ArrayList<>(duel.getSecond())).forEach(uuids -> uuids.forEach(uuid -> Bukkit.getPlayer(uuid).sendMessage(ChatColor.WHITE + event.getPlayer().getName() + ChatColor.DARK_GRAY + " is no longer spectating your match.")));
			}
			if (profile.isInState(ProfileState.FIGHT)) {
				if (Utils.getDuelByUUID(event.getPlayer().getUniqueId()) != null) {
					final Duel duel = Utils.getDuelByUUID(event.getPlayer().getUniqueId());
					if (!duel.getState().equals(DuelState.FINISHING) && duel.getDuelType().equals(DuelType.SINGLE)) {
						this.main.getManagerHandler().getDuelManager().endSingle(duel.getFirst().contains(event.getPlayer().getUniqueId()) ? new ArrayList<>(duel.getSecond()).get(0) : new ArrayList<>(duel.getFirst()).get(0));
					}
					if (!duel.getState().equals(DuelState.FINISHING) && (duel.getDuelType().equals(DuelType.FFA) || duel.getDuelType().equals(DuelType.SPLIT))) {
						MatchUtils.addKill(event.getPlayer().getUniqueId(), null, true);
					}
				}
			}	
		}
		this.main.getManagerHandler().getInventoryManager().removeUselessInventory(event.getPlayer().getUniqueId());
	}
	
	@EventHandler
	public void onPlayerKick(final PlayerKickEvent event) {
		this.onPlayerLeft(new PlayerQuitEvent(event.getPlayer(), null));
	}

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
						player.openInventory(this.main.getManagerHandler().getInventoryManager().getPartyEventInventory());
					}
				}
				if (event.getItem().getType().equals(Material.IRON_SWORD)) { player.openInventory(this.main.getManagerHandler().getInventoryManager().getQueueInventory()[0]); }
				if (event.getItem().getType().equals(Material.DIAMOND_SWORD)) { player.openInventory(this.main.getManagerHandler().getInventoryManager().getQueueInventory()[1]); }
				if (event.getItem().getType().equals(Material.NAME_TAG)) {
					if (!player.isOp()) {
						player.sendMessage(ChatColor.RED + "Currently under working, exit at the next open :3");
						return;
					}
					this.main.getManagerHandler().getPartyManager().createParty(player.getUniqueId());
				}
				if (event.getItem().getType().equals(Material.BOOK)) { player.openInventory(this.main.getManagerHandler().getInventoryManager().getEditorInventory()[0]); }
				if (event.getItem().getType().equals(Material.SKULL_ITEM)) { player.openInventory(Core.API.getManagerHandler().getInventoryManager().getProfileInventory().get(player.getUniqueId())); }
				if (event.getItem().getType().equals(Material.EMERALD)) { player.openInventory(this.main.getManagerHandler().getInventoryManager().getSettingsInventory().get(player.getUniqueId())); }
				return;
			}
			if (profile.isInState(ProfileState.QUEUE)) {
				if (event.getItem().getType().equals(Material.REDSTONE_TORCH_ON)) {
					this.main.getManagerHandler().getQueueManager().removePlayerFromQueue(player.getUniqueId());
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
					final Duel duel = Utils.getDuelBySpectator(player.getUniqueId());
					duel.getSpectators().remove(player.getUniqueId());
					Arrays.asList(new ArrayList<>(duel.getFirst()), new ArrayList<>(duel.getSecond())).forEach(uuids -> uuids.forEach(uuid -> {
						Bukkit.getPlayer(uuid).sendMessage(ChatColor.WHITE + player.getName() + ChatColor.DARK_GRAY + " is no longer spectating your match.");
						player.hidePlayer(Bukkit.getPlayer(uuid));
					}));
					Utils.sendToSpawn(player.getUniqueId(), true);
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
				if (event.getClickedBlock().getType().equals(Material.TRAP_DOOR)) { 
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
		if (event.getAction().equals(Action.RIGHT_CLICK_AIR) || event.getAction().equals(Action.RIGHT_CLICK_BLOCK)) {
			if (player.getItemInHand() == null) return;
	        if (profile.isInState(ProfileState.FIGHT)) {
	            final Duel duel = Utils.getDuelByUUID(player.getUniqueId());
	            if (player.getItemInHand().getType() == Material.ENDER_PEARL) {
	                if (duel.getState().equals(DuelState.STARTING)) {
	                    event.setUseItemInHand(Result.DENY);
	                    player.sendMessage(ChatColor.RED + "You can't launch an enderpearl yet!");
	                    player.updateInventory();
	                    return;
	                }
	                DuelStatistics duelStatistics = this.main.getManagerHandler().getProfileManager().getDuelStatistics().get(player.getUniqueId());
	                if (duelStatistics.getEnderPearlCooldown() <= 0L) {
	                	event.setUseItemInHand(Result.ALLOW);
						duelStatistics.applyEnderPearlCooldown();
	                    new PearlExpireRunnable(player, duel).runTaskLaterAsynchronously(Practice.getAPI(), 320L);
						event.setCancelled(false);
	                    return;
	                }
	                event.setUseItemInHand(Result.DENY);
	                player.sendMessage(ChatColor.RED + "Cooldown expires in " + ChatColor.WHITE + FormatUtils.formatTime(duelStatistics.getEnderPearlCooldown(), 1000.0d) + ChatColor.RED + " second(s)!");
	                player.updateInventory();
	                return;
	            }
	            if (duel.getState().equals(DuelState.STARTING)) {
	                if (event.getItem().getType().equals(Material.POTION) && event.getItem().getDurability() == 16421) {
	                    event.setUseItemInHand(Result.DENY);
	                    event.setCancelled(true);
	                }
	            } 
				final Kit kit = duel.getKit();
				if (player.getItemInHand().getType().equals(Material.ENCHANTED_BOOK)) {
					event.setUseItemInHand(Result.DENY);
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
					player.updateInventory();
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
		event.setCancelled(true);
	}
	
	@EventHandler(priority=EventPriority.LOW)
	public void PlayerBreakBlockEvent(final BlockBreakEvent event) {
		if (event.getPlayer().getGameMode().equals(GameMode.CREATIVE)) return;
		event.setCancelled(true);
	}
	
	@EventHandler(priority=EventPriority.LOW)
	public void PlayerFoodChange(final FoodLevelChangeEvent event) {
		final Profile profile = Utils.getProfiles(event.getEntity().getUniqueId());
		if (profile.isInState(ProfileState.FIGHT)) {
			final Duel duel = Utils.getDuelByUUID(event.getEntity().getUniqueId());
			if (duel.getKit().name().equals("sumo") || duel.getKit().name().equals("soup")) {
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

		LightningStrike lightning = deathLoc.getWorld().strikeLightningEffect(deathLoc);
		lightning.setFireTicks(0);
		lightning.setSilent(true);

		final Duel duel = Utils.getDuelByUUID(killed.getUniqueId());

		Set<UUID> duelPlayers = new HashSet<>();
		duelPlayers.addAll(duel.getFirst());
		duelPlayers.addAll(duel.getSecond());
		duelPlayers.addAll(duel.getSpectators());

		duelPlayers.stream().map(Bukkit::getPlayer).forEach(player -> {
			if (player == null) return;
			player.playSound(deathLoc, Sound.AMBIENCE_THUNDER, 10000.0F, deathLoc.getPitch());
		});

		if (killer != null) {

			if (killer.getUniqueId().equals(UUID.fromString("fb4f1dd4-e9c3-47ed-8a1b-17de9e80ae1e"))) {
				killer.spigot().playEffect(deathLoc.add(0, 1D, 0), Effect.FLAME, 0, 0, 0.6F, 0.1F, 0.6F, 0.2F, 10, 192);
				for (int i = 0 ; i < 10 ; i++) {
					killer.spigot().playEffect(deathLoc.add(0, 0.75D, 0), Effect.LAVA_POP, 0, 0, 0.5F, 0.2F, 0.5F, 0F, 3, 192);
				}
			}
		}
		event.setDroppedExp(0);
		killed.setLevel(0);
		killed.setExp(0);
		for (ItemStack item : event.getDrops()) {
			final Item items = killed.getWorld().dropItemNaturally(deathLoc, new ItemStack(item.clone()), killed);
			MatchUtils.addDrops(items, killed.getUniqueId());
		}
		event.getDrops().clear();
		final Profile profile = Utils.getProfiles(killed.getUniqueId());
		if ((profile.isInState(ProfileState.FIGHT))) {
            new BukkitRunnable() {
                public void run() {
                    try {
						// Does the same as the reflection code below
						CraftServer server = (CraftServer) killed.getServer();
						server.getHandle().moveToWorld(((CraftPlayer) killed).getHandle(), 0, false);

//                        final Object nmsPlayer = killed.getClass().getMethod("getHandle", new Class[0]).invoke(killed);
//                        final Object con = nmsPlayer.getClass().getDeclaredField("playerConnection").get(nmsPlayer);
//                        final Class<?> EntityPlayer = Class.forName(nmsPlayer.getClass().getPackage().getName() + ".EntityPlayer");
//                        final Field minecraftServer = con.getClass().getDeclaredField("minecraftServer");
//                        minecraftServer.setAccessible(true);
//                        final Object mcserver = minecraftServer.get(con);
//                        final Object playerlist = mcserver.getClass().getDeclaredMethod("getPlayerList", new Class[0]).invoke(mcserver);
//                        final Method moveToWorld = playerlist.getClass().getMethod("moveToWorld", EntityPlayer, Integer.TYPE, Boolean.TYPE);
//                        moveToWorld.invoke(playerlist, nmsPlayer, 0, false);
                    }
                    catch (Exception ex) {
                    	killed.spigot().respawn();
                        ex.printStackTrace();
                    }
        			killed.teleport(deathLoc);
                }
            }.runTaskLater(this.main, 20L);
            new BukkitRunnable() {
            	public void run() {
                    if (killer != null)
                    	killer.hidePlayer(killed);
            	}
            }.runTaskLater(main, 19L);
            if (duel.getDuelType().equals(DuelType.SINGLE)) {
                this.main.getManagerHandler().getDuelManager().endSingle(killed.getUniqueId().equals(new ArrayList<>(duel.getFirst()).get(0)) ? new ArrayList<>(duel.getSecond()).get(0) : new ArrayList<>(duel.getFirst()).get(0));
                return;
            }
			//TODO: Made register last hitter and replace on at the place of : null.
            MatchUtils.addKill(killed.getUniqueId(), killer != null ? killer.getUniqueId() : null, false);
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
//		if (!event.getTo().equals(this.main.getSpawn().getLocation() == null ? teleported.getWorld().getSpawnLocation() : this.main.getSpawn().getLocation()))
//			return;
//
//		if (RgbArmorTask.getArmored().contains(teleported.getUniqueId()))
//			return;
//
//		Profile profile = this.main.getManagerHandler().getProfileManager().getProfiles().get(teleported.getUniqueId());
//
//		if (profile == null || teleported.hasPermission("akyto.rgbchest")) // TODO add setting check
//			return;
//
//		RgbArmorTask rgbArmor = new RgbArmorTask(teleported);
//		BukkitTask rgbArmorTask = this.main.getServer().getScheduler().runTaskTimerAsynchronously(
//				this.main,
//				rgbArmor,
//				40L,
//				1L
//		);
//		rgbArmor.setTask(rgbArmorTask);
	}

}

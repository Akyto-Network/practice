package kezukdev.akyto.handler.listener;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;

import kezukdev.akyto.runnable.RgbArmorTask;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.craftbukkit.v1_7_R4.entity.CraftPlayer;
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

import gym.core.Core;
import kezukdev.akyto.Practice;
import kezukdev.akyto.duel.Duel;
import kezukdev.akyto.duel.Duel.DuelType;
import kezukdev.akyto.duel.cache.DuelState;
import kezukdev.akyto.duel.cache.DuelStatistics;
import kezukdev.akyto.kit.Kit;
import kezukdev.akyto.kit.KitInterface;
import kezukdev.akyto.profile.Profile;
import kezukdev.akyto.profile.ProfileState;
import kezukdev.akyto.runnable.PearlExpireRunnable;
import kezukdev.akyto.utils.FormatUtils;
import kezukdev.akyto.utils.Utils;
import kezukdev.akyto.utils.match.MatchUtils;
import net.md_5.bungee.api.ChatColor;
import net.minecraft.server.v1_7_R4.PacketPlayOutNamedSoundEffect;

public class PlayerListener implements Listener {
	
	private final Practice main;
	
	public PlayerListener(final Practice main) { this.main = main; }
	
	@EventHandler
	public void onPlayerJoin(final PlayerJoinEvent event) {
		if (!event.getPlayer().isOnline())
			return;

		Profile playerProfile = new Profile(event.getPlayer().getUniqueId());
		this.main.getManagerHandler().getProfileManager().getProfiles().put(event.getPlayer().getUniqueId(), playerProfile);
		this.main.getDatabaseSetup().update(event.getPlayer().getUniqueId());
		this.main.getManagerHandler().getItemManager().giveItems(event.getPlayer().getUniqueId(), false);
		Utils.resetPlayer(event.getPlayer().getUniqueId());
		event.getPlayer().teleport(this.main.getSpawn().getLocation() == null ? event.getPlayer().getWorld().getSpawnLocation() : this.main.getSpawn().getLocation());
		this.main.getManagerHandler().getInventoryManager().generateSettingsInventory(event.getPlayer().getUniqueId());
		this.main.getManagerHandler().getInventoryManager().generateProfileInventory(event.getPlayer().getUniqueId());
	}

	@EventHandler
	public void onPlayerLeft(final PlayerQuitEvent event) {
		final Profile profile = Utils.getProfiles(event.getPlayer().getUniqueId());
		if (event.getPlayer().isOnline() && profile != null) {
			if (Utils.getPartyByUUID(event.getPlayer().getUniqueId()) != null) {
				this.main.getManagerHandler().getPartyManager().leaveParty(event.getPlayer().getUniqueId());
			}
			if (profile.isInState(ProfileState.QUEUE)) {
				this.main.getQueue().remove(event.getPlayer().getUniqueId());
			}
			if (profile.isInState(ProfileState.SPECTATE)) {
				final Duel duel = Utils.getDuelBySpectator(event.getPlayer().getUniqueId());
				duel.getSpectator().remove(event.getPlayer().getUniqueId());
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
		this.main.getDatabaseSetup().exitAsync(event.getPlayer().getUniqueId());
	}
	
	@EventHandler
	public void onPlayerKick(final PlayerKickEvent event) {
		this.onPlayerLeft(new PlayerQuitEvent(event.getPlayer(), null));
	}

	// Limit fire extinguish to fights
	@EventHandler
	public void onFireExtinguish(PlayerInteractEvent event) {
		Player player = event.getPlayer();
		Profile profile = this.main.getManagerHandler().getProfileManager().getProfiles().get(player.getUniqueId());

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
            if (isPlants(clickedBlock.getType()) && player.getLocation().getBlockY() > event.getClickedBlock().getLocation().getBlockY()) {
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
				if (event.getItem().getType().equals(Material.SKULL_ITEM)) { player.openInventory(this.main.getManagerHandler().getInventoryManager().getProfileInventory().get(player.getUniqueId())); }
				if (event.getItem().getType().equals(Material.EMERALD)) { player.openInventory(this.main.getManagerHandler().getInventoryManager().getSettingsInventory().get(player.getUniqueId())); }
				return;
			}
			if (profile.isInState(ProfileState.QUEUE)) {
				if (event.getItem().getType().equals(Material.REDSTONE_TORCH_ON)) { this.main.getManagerHandler().getQueueManager().removePlayerFromQueue(player.getUniqueId());}
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
					duel.getSpectator().remove(player.getUniqueId());
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
			if (profile.isInState(ProfileState.FIGHT)) {
				final Duel duel = Utils.getDuelByUUID(player.getUniqueId());
				final Kit kit = duel.getKit();
				if (event.getItem().getType().equals(Material.ENCHANTED_BOOK)) {
					event.setUseItemInHand(Result.DENY);
					player.closeInventory();
					player.getInventory().clear();
					player.getInventory().setArmorContents(this.main.getManagerHandler().getProfileManager().getEditor().get(player.getUniqueId()).get(kit.name()).getArmorContent());
					player.getInventory().setContents(this.main.getManagerHandler().getProfileManager().getEditor().get(player.getUniqueId()).get(kit.name()).getContent());
					player.updateInventory();
					return;
				}
				if (event.getItem().getType().equals(Material.BOOK)) {
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
				final ItemStack item = event.getItem();
				if (item.getType() == Material.ENDER_PEARL) {
					if (duel.getState().equals(DuelState.STARTING)) {
						event.setUseItemInHand(Result.DENY);
						player.sendMessage(ChatColor.RED + "You can't launch an enderpearl yet!");
						player.updateInventory();
						return;
					}
					DuelStatistics duelStatistics = this.main.getManagerHandler().getProfileManager().getDuelStatistics().get(player.getUniqueId());
					if (!duelStatistics.hasPearlCooldown()) {
						duelStatistics.applyEnderPearlCooldown();
						new PearlExpireRunnable(player, duel).runTaskLaterAsynchronously(Practice.getAPI(), 320L);
						return;
					}
					event.setUseItemInHand(Result.DENY);
					player.sendMessage(ChatColor.RED + "Cooldown expires in " + ChatColor.WHITE + FormatUtils.formatTime(duelStatistics.getEnderPearlCooldown(), 1000.0d) + ChatColor.RED + " second(s)!");
					player.updateInventory();
					return;
				}
				if (duel.getState().equals(DuelState.STARTING)) {
					if (item.getType().equals(Material.POTION) && item.getDurability() == 16421) {
						event.setUseItemInHand(Result.DENY);
						event.setCancelled(true);
                    }
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
		final Profile profile = this.main.getManagerHandler().getProfileManager().getProfiles().get(event.getEntity().getUniqueId());
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
	public void PlayerDeathEvent(final PlayerDeathEvent event) {
		event.setDeathMessage(null);
		final Location deathLoc = event.getEntity().getLocation();
		LightningStrike lightning = deathLoc.getWorld().strikeLightningEffect(deathLoc);
		lightning.setFireTicks(0);
		lightning.setSilent(true);
		((CraftPlayer)event.getEntity()).getHandle().playerConnection.sendPacket(new PacketPlayOutNamedSoundEffect("ambient.weather.thunder", deathLoc.getX(), deathLoc.getY(), deathLoc.getZ(), 10000.0F, deathLoc.getPitch()));
		if (event.getEntity().getKiller() != null) {
			((CraftPlayer)event.getEntity().getKiller()).getHandle().playerConnection.sendPacket(new PacketPlayOutNamedSoundEffect("ambient.weather.thunder", event.getEntity().getKiller().getLocation().getX(),  event.getEntity().getKiller().getLocation().getY(),  event.getEntity().getKiller().getLocation().getZ(), 10000.0F,  event.getEntity().getKiller().getLocation().getPitch()));
		}
		final Duel duel = Utils.getDuelByUUID(event.getEntity().getUniqueId());
		if (!duel.getSpectator().isEmpty()) {
			duel.getSpectator().forEach(specs -> {
				((CraftPlayer)Bukkit.getPlayer(specs)).getHandle().playerConnection.sendPacket(new PacketPlayOutNamedSoundEffect("ambient.weather.thunder", event.getEntity().getKiller().getLocation().getX(),  event.getEntity().getKiller().getLocation().getY(),  event.getEntity().getKiller().getLocation().getZ(), 10000.0F,  event.getEntity().getKiller().getLocation().getPitch()));
			});
		}
		event.setDroppedExp(0);
		event.getEntity().setLevel(0);
		event.getEntity().setExp(0);
		for (ItemStack item : event.getDrops()) {
			final Item items = event.getEntity().getWorld().dropItemNaturally(event.getEntity(), deathLoc, new ItemStack(item.clone()));
			MatchUtils.addDrops(items, event.getEntity().getUniqueId());
		}
		event.getDrops().clear();
		final Profile profile = this.main.getManagerHandler().getProfileManager().getProfiles().get(event.getEntity().getUniqueId());
		if (event.getEntity() == null) return;
		if ((profile.isInState(ProfileState.FIGHT))) {
            new BukkitRunnable() {
                public void run() {
                    try {
                        final Object nmsPlayer = event.getEntity().getClass().getMethod("getHandle", new Class[0]).invoke(event.getEntity());
                        final Object con = nmsPlayer.getClass().getDeclaredField("playerConnection").get(nmsPlayer);
                        final Class<?> EntityPlayer = Class.forName(nmsPlayer.getClass().getPackage().getName() + ".EntityPlayer");
                        final Field minecraftServer = con.getClass().getDeclaredField("minecraftServer");
                        minecraftServer.setAccessible(true);
                        final Object mcserver = minecraftServer.get(con);
                        final Object playerlist = mcserver.getClass().getDeclaredMethod("getPlayerList", new Class[0]).invoke(mcserver);
                        final Method moveToWorld = playerlist.getClass().getMethod("moveToWorld", EntityPlayer, Integer.TYPE, Boolean.TYPE);
                        moveToWorld.invoke(playerlist, nmsPlayer, 0, false);
                    }
                    catch (Exception ex) {
                    	event.getEntity().spigot().respawn();
                        ex.printStackTrace();
                    }
        			event.getEntity().teleport(deathLoc);
                }
            }.runTaskLater(this.main, 20L);
            new BukkitRunnable() {
            	public void run() {
                    if (event.getEntity().getKiller() != null) {
                    	event.getEntity().getKiller().hidePlayer(event.getEntity());
                    }	
            	}
            }.runTaskLater(main, 19L);
            if (duel.getDuelType().equals(DuelType.SINGLE)) {
                this.main.getManagerHandler().getDuelManager().endSingle(event.getEntity().getUniqueId().equals(new ArrayList<>(duel.getFirst()).get(0)) ? new ArrayList<>(duel.getSecond()).get(0) : new ArrayList<>(duel.getFirst()).get(0));	
                return;
            }
        	MatchUtils.addKill(event.getEntity().getUniqueId(), event.getEntity().getKiller() != null ? event.getEntity().getKiller().getUniqueId() : null, false);
		}
	}
	
	@EventHandler
	public void onWeatherChange(final WeatherChangeEvent event) {
		event.setCancelled(true);
	}

	public void onTeleport(PlayerTeleportEvent event) {
		Player teleported = event.getPlayer();
		if (!event.getTo().equals(this.main.getSpawn().getLocation() == null ? teleported.getWorld().getSpawnLocation() : this.main.getSpawn().getLocation()))
			return;

		if (RgbArmorTask.getArmored().contains(teleported.getUniqueId()))
			return;

		Profile profile = this.main.getManagerHandler().getProfileManager().getProfiles().get(teleported.getUniqueId());

		if (profile == null || !Core.API.getManagerHandler().getProfileManager().getRank(teleported.getUniqueId()).isStaff())
			return;

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

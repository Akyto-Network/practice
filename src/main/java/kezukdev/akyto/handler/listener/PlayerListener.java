package kezukdev.akyto.handler.listener;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
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
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.weather.WeatherChangeEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import kezukdev.akyto.Practice;
import kezukdev.akyto.duel.Duel;
import kezukdev.akyto.duel.cache.DuelState;
import kezukdev.akyto.duel.cache.DuelStatistics;
import kezukdev.akyto.kit.KitInterface;
import kezukdev.akyto.profile.Profile;
import kezukdev.akyto.profile.ProfileState;
import net.md_5.bungee.api.ChatColor;

public class PlayerListener implements Listener {
	
	private final Practice main;
	
	public PlayerListener(final Practice main) { this.main = main; }
	
	@EventHandler
	public void onPlayerJoin(final PlayerJoinEvent event) {
		if (event.getPlayer().isOnline()) {
			new Profile(main, event.getPlayer().getUniqueId());
			this.main.getDatabaseSetup().update(event.getPlayer().getUniqueId());
			this.main.getManagerHandler().getItemManager().giveItems(event.getPlayer().getUniqueId(), false);
			this.main.getUtils().resetPlayer(event.getPlayer().getUniqueId());
			event.getPlayer().teleport(this.main.getSpawn().getLocation() != null ? this.main.getSpawn().getLocation() : event.getPlayer().getWorld().getSpawnLocation());	
            this.main.getManagerHandler().getInventoryManager().generateSettingsInventory(event.getPlayer().getUniqueId());
            this.main.getManagerHandler().getInventoryManager().generateProfileInventory(event.getPlayer().getUniqueId());
		}
	}

	@EventHandler
	public void onPlayerLeft(final PlayerQuitEvent event) {
		final Profile profile = this.main.getUtils().getProfiles(event.getPlayer().getUniqueId());
		if (event.getPlayer().isOnline() && profile != null) {
			if (this.main.getUtils().getPartyByUUID(event.getPlayer().getUniqueId()) != null) {
				this.main.getManagerHandler().getPartyManager().leaveParty(event.getPlayer().getUniqueId());
			}
			if (profile.getProfileState().equals(ProfileState.QUEUE)) {
				this.main.getQueue().remove(event.getPlayer().getUniqueId());
			}
			if (profile.getProfileState().equals(ProfileState.SPECTATE)) {
				final Duel duel = this.main.getUtils().getDuelBySpectator(event.getPlayer().getUniqueId());
				duel.getSpectator().remove(event.getPlayer().getUniqueId());
				Arrays.asList(new ArrayList<>(duel.getFirst()), new ArrayList<>(duel.getSecond())).forEach(uuids -> uuids.forEach(uuid -> Bukkit.getPlayer(uuid).sendMessage(ChatColor.WHITE + event.getPlayer().getName() + ChatColor.DARK_GRAY + " is no longer spectating your match.")));
			}
			if (profile.getProfileState().equals(ProfileState.FIGHT)) {
				if (this.main.getUtils().getDuelByUUID(event.getPlayer().getUniqueId()) != null) {
					final Duel duel = this.main.getUtils().getDuelByUUID(event.getPlayer().getUniqueId());
					if (!duel.getState().equals(DuelState.FINISHING)) {
						this.main.getManagerHandler().getDuelManager().endSingle(duel.getFirst().contains(event.getPlayer().getUniqueId()) ? new ArrayList<>(duel.getSecond()).get(0) : new ArrayList<>(duel.getFirst()).get(0));
					}	
				}
				if (this.main.getUtils().getDuelPartyByUUID(event.getPlayer().getUniqueId()) != null) {
					this.main.getUtils().addKill(event.getPlayer().getUniqueId(), null);	
				}
			}	
		}
		this.main.getDatabaseSetup().exitAsync(event.getPlayer().getUniqueId());
	}
	
	@EventHandler
	public void onPlayerKick(final PlayerKickEvent event) {
		this.onPlayerLeft(new PlayerQuitEvent(event.getPlayer(), null));
	}
	
	
    private boolean isPlants(Material material) {
        return material == Material.CROPS || material == Material.CARROT || material == Material.POTATO || material == Material.NETHER_WARTS || material == Material.COCOA || material == Material.SUGAR_CANE_BLOCK || material == Material.MELON_BLOCK || material == Material.PUMPKIN || material == Material.CACTUS;
    }
	
	@EventHandler
	public void onInteract(final PlayerInteractEvent event) {
		if (event.getPlayer().getGameMode().equals(GameMode.CREATIVE)) return;
		if (event.getItem() == null || event.getItem().getType().equals(Material.AIR)) return;
		final Profile data = this.main.getUtils().getProfiles(event.getPlayer().getUniqueId());
		final Player player = Bukkit.getPlayer(event.getPlayer().getUniqueId());
		Block clickedBlock = event.getClickedBlock();
        if (clickedBlock != null && isPlants(clickedBlock.getType())) {
            if (player.getLocation().getBlockY() > event.getClickedBlock().getLocation().getBlockY()) {
                event.setCancelled(true);
            }
        }
		if (event.getAction().equals(Action.RIGHT_CLICK_AIR) || event.getAction().equals(Action.RIGHT_CLICK_BLOCK)) {
			if (data.getProfileState().equals(ProfileState.FREE)) {
				if (this.main.getUtils().getPartyByUUID(event.getPlayer().getUniqueId()) != null) {
					if (event.getItem().getType().equals(Material.REDSTONE_TORCH_ON)) { this.main.getManagerHandler().getPartyManager().leaveParty(event.getPlayer().getUniqueId()); }
					if (event.getItem().getType().equals(Material.PAPER)) { this.main.getManagerHandler().getPartyManager().sendPartyInformation(event.getPlayer().getUniqueId()); }
					if (event.getItem().getType().equals(Material.CHEST)) { this.main.getManagerHandler().getInventoryManager().getPartyMultipage().open(event.getPlayer(), 1); }
					if (event.getItem().getType().equals(Material.DIAMOND_AXE)) { 
						if (!this.main.getUtils().getPartyByUUID(event.getPlayer().getUniqueId()).getCreator().equals(event.getPlayer().getUniqueId())) {
							event.getPlayer().sendMessage(ChatColor.RED + "You're not the party's creator!");
							return;
						}
						if (!(this.main.getUtils().getPartyByUUID(event.getPlayer().getUniqueId()).getMembers().size() > 1)) {
							event.getPlayer().sendMessage(ChatColor.RED + "You must have at least two members to launch an event!");
							return;
						}
						event.getPlayer().openInventory(this.main.getManagerHandler().getInventoryManager().getPartyEventInventory());
					}
				}
				if (event.getItem().getType().equals(Material.IRON_SWORD)) { player.openInventory(this.main.getManagerHandler().getInventoryManager().getQueueInventory()[0]); }
				if (event.getItem().getType().equals(Material.DIAMOND_SWORD)) { player.openInventory(this.main.getManagerHandler().getInventoryManager().getQueueInventory()[1]); }
				if (event.getItem().getType().equals(Material.NAME_TAG)) this.main.getManagerHandler().getPartyManager().createParty(event.getPlayer().getUniqueId());
				if (event.getItem().getType().equals(Material.BOOK)) { player.openInventory(this.main.getManagerHandler().getInventoryManager().getEditorInventory()[0]); }
				if (event.getItem().getType().equals(Material.SKULL_ITEM)) { player.openInventory(this.main.getManagerHandler().getInventoryManager().getProfileInventory().get(player.getUniqueId())); }
				if (event.getItem().getType().equals(Material.EMERALD)) { player.openInventory(this.main.getManagerHandler().getInventoryManager().getSettingsInventory().get(player.getUniqueId())); }
				return;
			}
			if (data.getProfileState().equals(ProfileState.QUEUE)) {
				if (event.getItem().getType().equals(Material.REDSTONE_TORCH_ON)) { this.main.getManagerHandler().getQueueManager().removePlayerFromQueue(event.getPlayer().getUniqueId());}
				return;
			}	
			if (data.getProfileState().equals(ProfileState.SPECTATE)) {
				event.setUseInteractedBlock(Result.DENY);
				event.setUseItemInHand(Result.DENY);
				event.setCancelled(true);
				if (event.getItem().getType().equals(Material.CHEST)) { 
					event.getPlayer().openInventory(this.main.getManagerHandler().getInventoryManager().getSpectateInventory().get(player.getUniqueId()));
					return;
				}
				if (event.getItem().getType().equals(Material.COMPASS)) { 
					this.main.getManagerHandler().getInventoryManager().getSpectateMultipage().open(event.getPlayer(), 1);
				}
				if (event.getItem().getType().equals(Material.REDSTONE_COMPARATOR)) { 
					event.getPlayer().openInventory(this.main.getManagerHandler().getInventoryManager().getSettingsSpectateInventory().get(event.getPlayer().getUniqueId()));
				}
				if (event.getItem().getType().equals(Material.REDSTONE_TORCH_ON)) { 
					final Duel duel = this.main.getUtils().getDuelBySpectator(event.getPlayer().getUniqueId());
					duel.getSpectator().remove(event.getPlayer().getUniqueId());
					Arrays.asList(new ArrayList<>(duel.getFirst()), new ArrayList<>(duel.getSecond())).forEach(uuids -> uuids.forEach(uuid -> {
						Bukkit.getPlayer(uuid).sendMessage(ChatColor.WHITE + event.getPlayer().getName() + ChatColor.DARK_GRAY + " is no longer spectating your match.");
						event.getPlayer().hidePlayer(Bukkit.getPlayer(uuid));
					}));
					this.main.getUtils().sendToSpawn(event.getPlayer().getUniqueId(), true);
					return;
				}
			}
			if (data.getProfileState().equals(ProfileState.EDITOR)) {
				event.setUseInteractedBlock(Result.DENY);
				event.setUseItemInHand(Result.DENY);
				event.setCancelled(true);
				if (event.getClickedBlock().getType().equals(Material.CHEST)) { 
					event.getPlayer().openInventory(this.main.getManagerHandler().getInventoryManager().getEditorInventory()[1]);
					return;
				}
				if (event.getClickedBlock().getType().equals(Material.ANVIL)) { event.getPlayer().openInventory(this.main.getManagerHandler().getInventoryManager().getEditorInventory()[2]); }
				if (event.getClickedBlock().getType().equals(Material.TRAP_DOOR)) { 
					event.getPlayer().sendMessage(ChatColor.GREEN + "You've returned to the spawn");
					this.main.getUtils().sendToSpawn(event.getPlayer().getUniqueId(), true);
					return;
				}
			}
			if (data.getProfileState().equals(ProfileState.FIGHT)) {
				if (event.getItem().getType().equals(Material.ENCHANTED_BOOK)) {
					event.setUseItemInHand(Result.DENY);
					event.getPlayer().closeInventory();
					event.getPlayer().getInventory().clear();
					event.getPlayer().getInventory().setArmorContents(this.main.getManagerHandler().getProfileManager().getEditor().get(event.getPlayer().getUniqueId()).get(this.main.getUtils().getDuelByUUID(event.getPlayer().getUniqueId()) != null ? this.main.getUtils().getDuelByUUID(event.getPlayer().getUniqueId()).getKit().name() : this.main.getUtils().getDuelPartyByUUID(event.getPlayer().getUniqueId()).getKit().name()).getArmorContent());
					event.getPlayer().getInventory().setContents(this.main.getManagerHandler().getProfileManager().getEditor().get(event.getPlayer().getUniqueId()).get(this.main.getUtils().getDuelByUUID(event.getPlayer().getUniqueId()) != null ? this.main.getUtils().getDuelByUUID(event.getPlayer().getUniqueId()).getKit().name() : this.main.getUtils().getDuelPartyByUUID(event.getPlayer().getUniqueId()).getKit().name()).getContent());
					event.getPlayer().updateInventory();
					return;
				}
				if (event.getItem().getType().equals(Material.BOOK)) {
					final KitInterface kit = this.main.getUtils().getDuelByUUID(event.getPlayer().getUniqueId()) != null ? (KitInterface) this.main.getUtils().getDuelByUUID(event.getPlayer().getUniqueId()).getKit() : (KitInterface) this.main.getUtils().getDuelPartyByUUID(event.getPlayer().getUniqueId()).getKit();
					event.getPlayer().getInventory().setArmorContents(kit.armor());
					event.getPlayer().getInventory().setContents(kit.content());
					event.getPlayer().updateInventory();
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
					if ((this.main.getUtils().getDuelByUUID(event.getPlayer().getUniqueId()) != null && this.main.getUtils().getDuelByUUID(event.getPlayer().getUniqueId()).getState().equals(DuelState.STARTING)) || (this.main.getUtils().getDuelPartyByUUID(event.getPlayer().getUniqueId()) != null && this.main.getUtils().getDuelPartyByUUID(event.getPlayer().getUniqueId()).getState().equals(DuelState.STARTING))) {
						event.setUseItemInHand(Result.DENY);
						event.getPlayer().sendMessage(ChatColor.RED + "You can't launch an enderpearl yet!");
						event.getPlayer().updateInventory();
						return;
					}
					DuelStatistics duelStatistics = this.main.getManagerHandler().getProfileManager().getDuelStatistics().get(event.getPlayer().getUniqueId());
					if (!duelStatistics.isEnderPearlCooldownActive()) {
						duelStatistics.applyEnderPearlCooldown();
						new BukkitRunnable() {
							
							@Override
							public void run() {
								if (main.getUtils().getDuelByUUID(event.getPlayer().getUniqueId()) != null) {
									if (!main.getUtils().getDuelByUUID(event.getPlayer().getUniqueId()).getState().equals(DuelState.FINISHING)) {
										event.getPlayer().sendMessage(ChatColor.RED + "Enderpearl cooldown has expired, you can launch another pearl!");		
									}
								}
							}
						}.runTaskLaterAsynchronously(main, 320L);
						return;
					}
					event.setUseItemInHand(Result.DENY);
					event.getPlayer().sendMessage(ChatColor.RED + "Cooldown expires in " + ChatColor.WHITE + this.main.getUtils().formatTime(duelStatistics.getEnderPearlCooldown(), 1000.0d) + ChatColor.RED + " second(s)!");
					event.getPlayer().updateInventory();
					return;
				}
				if ((this.main.getUtils().getDuelByUUID(event.getPlayer().getUniqueId()) != null && this.main.getUtils().getDuelByUUID(event.getPlayer().getUniqueId()).getState().equals(DuelState.STARTING)) || (this.main.getUtils().getDuelPartyByUUID(event.getPlayer().getUniqueId()) != null && this.main.getUtils().getDuelPartyByUUID(event.getPlayer().getUniqueId()).getState().equals(DuelState.STARTING))) {
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
		if (profile.getProfileState().equals(ProfileState.FIGHT)) {
			if ((this.main.getUtils().getDuelByUUID(event.getEntity().getUniqueId()) != null && (this.main.getUtils().getDuelByUUID(event.getEntity().getUniqueId()).getKit().name().equals("sumo") || this.main.getUtils().getDuelByUUID(event.getEntity().getUniqueId()).getKit().name().equals("soup"))) || (this.main.getUtils().getDuelPartyByUUID(event.getEntity().getUniqueId()) != null && (this.main.getUtils().getDuelPartyByUUID(event.getEntity().getUniqueId()).getKit().name().equals("soup")))) {
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
		final Profile data = this.main.getUtils().getProfiles(event.getPlayer().getUniqueId());
		if (data.getProfileState().equals(ProfileState.FIGHT)) {
			if ((this.main.getUtils().getDuelByUUID(event.getPlayer().getUniqueId()) != null && (this.main.getUtils().getDuelByUUID(event.getPlayer().getUniqueId()).getState().equals(DuelState.PLAYING) || this.main.getUtils().getDuelByUUID(event.getPlayer().getUniqueId()).getState().equals(DuelState.STARTING))) || (this.main.getUtils().getDuelPartyByUUID(event.getPlayer().getUniqueId()) != null && (this.main.getUtils().getDuelPartyByUUID(event.getPlayer().getUniqueId()).getState().equals(DuelState.PLAYING) || this.main.getUtils().getDuelPartyByUUID(event.getPlayer().getUniqueId()).getState().equals(DuelState.STARTING)))) {
				if (event.getItemDrop().getItemStack().getType().equals(Material.GLASS_BOTTLE)) {
					event.getItemDrop().remove();
					return;
				}
				if (event.getItemDrop().getItemStack().getType().equals(Material.DIAMOND_SWORD) || event.getItemDrop().getItemStack().getType().equals(Material.IRON_SWORD) || event.getItemDrop().getItemStack().getType().equals(Material.STONE_SWORD) || event.getItemDrop().getItemStack().getType().equals(Material.WOOD_SWORD)) {
					event.setCancelled(true);
					return;
				}
				this.main.getUtils().addDrops(event.getItemDrop(), event.getPlayer().getUniqueId());
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
		event.setDroppedExp(0);
		event.getDrops().clear();
		event.getEntity().setLevel(0);
		event.getEntity().setExp(0);
		final Profile profile = this.main.getManagerHandler().getProfileManager().getProfiles().get(event.getEntity().getUniqueId());
		if (event.getEntity() == null) return;
		if ((profile.getProfileState().equals(ProfileState.FIGHT))) {
            new BukkitRunnable() {
                public void run() {
                    try {
                        final Object nmsPlayer = event.getEntity().getClass().getMethod("getHandle", new Class[0]).invoke(event.getEntity(), new Object[0]);
                        final Object con = nmsPlayer.getClass().getDeclaredField("playerConnection").get(nmsPlayer);
                        final Class<?> EntityPlayer = Class.forName(nmsPlayer.getClass().getPackage().getName() + ".EntityPlayer");
                        final Field minecraftServer = con.getClass().getDeclaredField("minecraftServer");
                        minecraftServer.setAccessible(true);
                        final Object mcserver = minecraftServer.get(con);
                        final Object playerlist = mcserver.getClass().getDeclaredMethod("getPlayerList", new Class[0]).invoke(mcserver, new Object[0]);
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
            if (this.main.getUtils().getDuelByUUID(event.getEntity().getUniqueId()) != null) {
                final Duel duel = this.main.getUtils().getDuelByUUID(event.getEntity().getUniqueId());
                this.main.getManagerHandler().getDuelManager().endSingle(event.getEntity().getUniqueId().equals(new ArrayList<>(duel.getFirst()).get(0)) ? new ArrayList<>(duel.getSecond()).get(0) : new ArrayList<>(duel.getFirst()).get(0));
            }
            if (this.main.getUtils().getDuelPartyByUUID(event.getEntity().getUniqueId()) != null) {
            	this.main.getUtils().addKill(event.getEntity().getUniqueId(), event.getEntity().getKiller() != null ? event.getEntity().getKiller().getUniqueId() : null);
            }
		}
	}
	
	@EventHandler
	public void onWeatherChange(final WeatherChangeEvent event) {
		event.setCancelled(true);
	}
}

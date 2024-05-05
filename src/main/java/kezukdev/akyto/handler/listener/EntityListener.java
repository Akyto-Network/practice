package kezukdev.akyto.handler.listener;

import kezukdev.akyto.duel.Duel;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.player.PlayerPickupItemEvent;

import kezuk.npaper.kPaper;
import kezuk.npaper.handler.MovementHandler;
import kezukdev.akyto.Practice;
import kezukdev.akyto.duel.cache.DuelState;
import kezukdev.akyto.duel.cache.DuelStatistics;
import kezukdev.akyto.profile.Profile;
import kezukdev.akyto.profile.ProfileState;
import net.minecraft.server.v1_7_R4.PacketPlayInFlying;

public class EntityListener implements Listener {
	
	private final Practice main;
	
	public EntityListener(final Practice main) { 
		this.main = main;
        kPaper.INSTANCE.addMovementHandler(new MovementHandler() {
            public void handleUpdateLocation(final Player player, final Location location, final Location location1, final PacketPlayInFlying packetPlayInFlying) {
                if (main.getUtils().getDuelByUUID(player.getUniqueId()) != null && main.getUtils().getDuelByUUID(player.getUniqueId()).getKit().name().equalsIgnoreCase("sumo") && main.getUtils().getDuelByUUID(player.getUniqueId()).getState().equals(DuelState.STARTING)) {
                    player.teleport(location1);
                }
            }
            public void handleUpdateRotation(final Player player, final Location location, final Location location1, final PacketPlayInFlying packetPlayInFlying) {
            }
        });
	}
	
	@EventHandler
	public void onDamage(final EntityDamageEvent event) {
		if (!(event.getEntity() instanceof Player))
			return;

		final Profile data = this.main.getUtils().getProfiles(event.getEntity().getUniqueId());

		if (data != null && data.getProfileState().equals(ProfileState.FIGHT)) {

			final Duel playerDuel = this.main.getUtils().getDuelByUUID(event.getEntity().getUniqueId());

			if (playerDuel != null && playerDuel.getState().equals(DuelState.PLAYING)) {
				if (playerDuel.getKit().name().equals("sumo")) {
					event.setDamage(0.0f);
				}
				return;
			}
		}
		event.setCancelled(true);
	}
	
	@EventHandler
	public void onDamageByEntity(final EntityDamageByEntityEvent event) {
		if (event.getEntity() instanceof Player && event.getDamager() instanceof Player) {
			final Profile data = this.main.getUtils().getProfiles(event.getEntity().getUniqueId());
			if (data.getProfileState().equals(ProfileState.FIGHT) && this.main.getUtils().getProfiles(event.getDamager().getUniqueId()).getProfileState().equals(ProfileState.FIGHT)) {
				final DuelStatistics statisticsDamager = this.main.getManagerHandler().getProfileManager().getDuelStatistics().get(event.getDamager().getUniqueId());
				final DuelStatistics statisticsVictim = this.main.getManagerHandler().getProfileManager().getDuelStatistics().get(event.getEntity().getUniqueId());
				if (this.main.getUtils().getDuelByUUID(event.getEntity().getUniqueId()) != null && this.main.getUtils().getDuelByUUID(event.getEntity().getUniqueId()).getState().equals(DuelState.PLAYING)) {
					if (this.main.getUtils().getDuelByUUID(event.getEntity().getUniqueId()) != null && this.main.getUtils().getDuelByUUID(event.getEntity().getUniqueId()).getFirstAlives() != null) {
						if (this.main.getUtils().getDuelByUUID(event.getEntity().getUniqueId()).getFirstAlives().contains(event.getEntity().getUniqueId()) && this.main.getUtils().getDuelByUUID(event.getEntity().getUniqueId()).getFirstAlives().contains(event.getDamager().getUniqueId()) || this.main.getUtils().getDuelByUUID(event.getEntity().getUniqueId()).getSecondAlives().contains(event.getEntity().getUniqueId()) && this.main.getUtils().getDuelByUUID(event.getEntity().getUniqueId()).getSecondAlives().contains(event.getDamager().getUniqueId())) {
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
					if (this.main.getUtils().getDuelByUUID(event.getEntity().getUniqueId()) != null && this.main.getUtils().getDuelByUUID(event.getEntity().getUniqueId()).getKit().name().equals("sumo")) {
						event.setDamage(0.0f);
					}
					return;
				}
			}
			event.setCancelled(true);
		}
	}
	
	  @EventHandler
	  public void onReceiveDroppedItems(PlayerPickupItemEvent event) {
		  if (this.main.getManagerHandler().getProfileManager().getProfiles().get(event.getPlayer().getUniqueId()).getProfileState().equals(ProfileState.FIGHT)) {
			  return;
		  }
		  event.setCancelled(true);
	  }

}

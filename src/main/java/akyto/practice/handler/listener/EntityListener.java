package akyto.practice.handler.listener;

import akyto.core.Core;
import akyto.core.utils.CoreUtils;
import akyto.practice.duel.Duel;
import org.bukkit.Location;
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
import net.minecraft.server.v1_7_R4.PacketPlayInFlying;

public class EntityListener implements Listener {

	private final Practice main;

	public EntityListener(final Practice main) {
		this.main = main;
	}

	@EventHandler
	public void onDamage(final EntityDamageEvent event) {
		if (!(event.getEntity() instanceof Player))
			return;
		if (event.getCause().equals(EntityDamageEvent.DamageCause.VOID)){
			event.getEntity().teleport(this.main.getSpawn().getLocation() == null ? event.getEntity().getWorld().getSpawnLocation() : this.main.getSpawn().getLocation());
		}
		final Profile profile = Utils.getProfiles(event.getEntity().getUniqueId());

		if (profile != null && profile.isInState(ProfileState.FIGHT)) {

			final Duel playerDuel = Utils.getDuelByUUID(event.getEntity().getUniqueId());

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
			final Profile victimProfile = Utils.getProfiles(event.getEntity().getUniqueId());
			final Profile profileDamager = Utils.getProfiles(event.getDamager().getUniqueId());
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
		event.setCancelled(true);
	}

}

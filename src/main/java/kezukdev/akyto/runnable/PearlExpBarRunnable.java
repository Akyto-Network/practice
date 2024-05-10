package kezukdev.akyto.runnable;

import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import kezukdev.akyto.Practice;
import kezukdev.akyto.duel.Duel;
import kezukdev.akyto.duel.cache.DuelState;

public class PearlExpBarRunnable extends BukkitRunnable {
	
	private final Player player;
	private final Duel duel;
	private int tick;
	
	public PearlExpBarRunnable(final Player player, final Duel duel) {
		this.player = player;
		this.duel = duel;
		this.run();
	}

	@Override
	public void run() {
		if (player == null) {
			this.cancel();
			return;
		}
		tick++;
		if (duel == null || duel.getState().equals(DuelState.FINISHING) || !Practice.getAPI().getManagerHandler().getProfileManager().getDuelStatistics().get(player.getUniqueId()).isEnderPearlCooldownActive()) {
			player.setExp(0.0f);
			player.setLevel(0);
			this.cancel();
			return;
		}
		if (tick == 10) {
			final double time = Practice.getAPI().getManagerHandler().getProfileManager().getDuelStatistics().get(player.getUniqueId()).getEnderPearlCooldown() / 1000.0D;	
			player.setLevel((int) time);
			tick = 0;
		}
		final float timeInf = Practice.getAPI().getManagerHandler().getProfileManager().getDuelStatistics().get(player.getUniqueId()).getEnderPearlCooldown() / 16000.0f;
		player.setExp(timeInf);
	}

}

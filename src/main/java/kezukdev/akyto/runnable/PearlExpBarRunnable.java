package kezukdev.akyto.runnable;

import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import kezukdev.akyto.Practice;
import kezukdev.akyto.duel.Duel;
import kezukdev.akyto.duel.cache.DuelState;

public class PearlExpBarRunnable extends BukkitRunnable {
	
	private final Player player;
	private final Duel duel;
	
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
		if (duel == null || duel.getState().equals(DuelState.FINISHING) || !Practice.getAPI().getManagerHandler().getProfileManager().getDuelStatistics().get(player.getUniqueId()).isEnderPearlCooldownActive()) {
			player.setExp(0.0f);
			player.setLevel(0);
			if (Practice.getAPI().getServer().getScheduler().isCurrentlyRunning(this.getTaskId()))
				this.cancel();
			return;
		}
		final double time = Practice.getAPI().getManagerHandler().getProfileManager().getDuelStatistics().get(player.getUniqueId()).getEnderPearlCooldown() / 1000.0D;
		final float timeInf = Practice.getAPI().getManagerHandler().getProfileManager().getDuelStatistics().get(player.getUniqueId()).getEnderPearlCooldown() / 16000.0f;
		player.setLevel((int) time);
		player.setExp(timeInf);
	}

}
